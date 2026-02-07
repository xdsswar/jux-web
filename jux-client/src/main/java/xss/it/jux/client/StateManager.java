/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.teavm.jso.dom.html.HTMLElement;

import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

/**
 * Manages reactive {@link State @State} fields on client-side JUX components,
 * triggering re-render and DOM patching when state changes are detected.
 *
 * <h2>Reactive State Model</h2>
 * <p>JUX components declare reactive fields with the {@link State @State}
 * annotation. When a {@code @State} field's value changes, the component
 * automatically re-renders and the resulting virtual tree is diffed against
 * the previous tree. Only the minimal set of DOM mutations is applied.</p>
 *
 * <h2>How It Works</h2>
 * <ol>
 *   <li><b>Registration:</b> When a component is hydrated (in
 *       {@link ClientMain#hydrateComponent}), it is registered with this
 *       manager via {@link #registerComponent(Object, HTMLElement, Element)}.
 *       The manager introspects the component's class to discover all
 *       {@code @State} fields and records a snapshot of their current values.</li>
 *   <li><b>Change notification:</b> When component code modifies a
 *       {@code @State} field, it calls {@link #notifyStateChange(Object)}
 *       (either manually or via generated proxy code from the annotation
 *       processor). The manager compares the current field values against
 *       the stored snapshot to determine if any state actually changed.</li>
 *   <li><b>Re-render:</b> If state has changed, the manager calls
 *       {@link Component#render()} to produce a new virtual {@link Element}
 *       tree.</li>
 *   <li><b>Patch:</b> The new tree is diffed against the stored previous
 *       tree using {@link JuxDomBridge#patch(HTMLElement, Element, Element)},
 *       and the real DOM is updated with minimal mutations.</li>
 *   <li><b>Snapshot update:</b> The state snapshot and stored virtual tree
 *       are updated to reflect the new values, ready for the next change.</li>
 * </ol>
 *
 * <h2>State Snapshot Strategy</h2>
 * <p>The snapshot stores the {@link Object#toString()} representation of each
 * {@code @State} field's value. This is a structural simplification; a
 * production implementation would use deep equality checks (via
 * {@link Object#equals(Object)}) or a serialization-based comparison.
 * The toString approach is sufficient for primitives, strings, and simple
 * objects, which covers the vast majority of component state.</p>
 *
 * <h2>TeaVM Reflection Limitations</h2>
 * <p>TeaVM supports a subset of Java reflection. {@link Field#get(Object)}
 * and {@link Field#set(Object, Object)} work for fields that TeaVM's
 * dead-code analyzer determines are reachable. The {@code @State}
 * annotation's {@code RUNTIME} retention ensures the annotation is
 * available for reflective inspection.</p>
 *
 * @see State
 * @see JuxDomBridge#patch(HTMLElement, Element, Element)
 * @see ClientMain
 */
public class StateManager {

    /**
     * Internal record tracking a registered component's render context.
     *
     * <p>Stores everything needed to detect state changes, re-render the
     * component, and patch the DOM:</p>
     * <ul>
     *   <li>The component instance (to call {@code render()} on)</li>
     *   <li>The root DOM element (the patch target)</li>
     *   <li>The last-rendered virtual tree (the "old" tree for diffing)</li>
     *   <li>The list of {@code @State} fields (cached to avoid re-scanning)</li>
     *   <li>A snapshot of field values from the last render (for change detection)</li>
     * </ul>
     */
    private static class ComponentEntry {

        /** The live component instance. */
        final Object component;

        /** The root DOM element that this component hydrated into. */
        HTMLElement rootElement;

        /** The virtual tree from the most recent render. */
        Element currentTree;

        /**
         * Cached list of {@code @State}-annotated fields discovered
         * via reflection during registration. Stored to avoid
         * re-scanning the class on every state change notification.
         */
        final List<Field> stateFields;

        /**
         * Snapshot of {@code @State} field values from the most recent
         * render, keyed by field name. Used for change detection:
         * if the current values match the snapshot, no re-render is needed.
         */
        final Map<String, String> valueSnapshot;

        /**
         * Construct a new component entry.
         *
         * @param component   the component instance
         * @param rootElement the root DOM element
         * @param currentTree the initial virtual tree
         * @param stateFields the discovered {@code @State} fields
         */
        ComponentEntry(Object component, HTMLElement rootElement,
                        Element currentTree, List<Field> stateFields) {
            this.component = component;
            this.rootElement = rootElement;
            this.currentTree = currentTree;
            this.stateFields = stateFields;
            this.valueSnapshot = new HashMap<>();
        }
    }

    /**
     * Map from component identity (using {@link System#identityHashCode})
     * to its tracking entry.
     *
     * <p>We use identity hash code (not {@link Object#hashCode()}) because
     * components are mutable objects and their hashCode may change as state
     * fields are modified. The identity hash code is stable for the lifetime
     * of the object.</p>
     */
    private final Map<Integer, ComponentEntry> entries = new HashMap<>();

    /**
     * Construct a new StateManager.
     *
     * <p>Called once during {@link ClientMain} initialization. The resulting
     * instance is shared across all component registrations.</p>
     */
    public StateManager() {
        // No initialization needed; entries are populated via registerComponent.
    }

    /**
     * Register a hydrated component for reactive state tracking.
     *
     * <p>This method is called by {@link ClientMain} immediately after
     * a component has been hydrated. It performs the following steps:</p>
     * <ol>
     *   <li>Introspects the component's class to discover all fields
     *       annotated with {@link State @State}.</li>
     *   <li>Makes each {@code @State} field accessible via
     *       {@link Field#setAccessible(boolean)} (necessary for private fields).</li>
     *   <li>Takes an initial snapshot of all {@code @State} field values.</li>
     *   <li>Stores the component, its root DOM element, the initial virtual
     *       tree, and the snapshot in the internal registry.</li>
     * </ol>
     *
     * <p>If the component has no {@code @State} fields, it is still registered
     * (for consistency) but will never trigger a re-render since
     * {@link #notifyStateChange(Object)} will always detect "no change".</p>
     *
     * @param component   the component instance that was just hydrated;
     *                     must be a {@link Component} subclass
     * @param root        the root DOM element that this component is rendered into
     * @param initialTree the virtual Element tree from the initial render
     *                     (used as the "old" tree for the first diff)
     * @throws NullPointerException if any argument is null
     */
    public void registerComponent(Object component, HTMLElement root, Element initialTree) {
        Objects.requireNonNull(component, "Component must not be null");
        Objects.requireNonNull(root, "Root DOM element must not be null");
        Objects.requireNonNull(initialTree, "Initial virtual tree must not be null");

        /*
         * Discover all @State-annotated fields on the component's class.
         * We check declared fields (including private) on the concrete class.
         * Superclass @State fields are also included by walking up the hierarchy.
         */
        List<Field> stateFields = discoverStateFields(component.getClass());

        /* Create the tracking entry. */
        ComponentEntry entry = new ComponentEntry(
                component, root, initialTree, stateFields);

        /*
         * Take the initial value snapshot. This records the current values
         * of all @State fields so we can detect changes later.
         */
        takeSnapshot(entry);

        /*
         * Store the entry using the component's identity hash code as key.
         * This ensures each component instance has exactly one entry.
         */
        int key = System.identityHashCode(component);
        entries.put(key, entry);
    }

    /**
     * Notify the state manager that a component's {@code @State} field(s)
     * may have changed, potentially triggering a re-render and DOM patch.
     *
     * <p>This method is the core of JUX's client-side reactivity. It is
     * called whenever a {@code @State} field is modified. The call site
     * can be:</p>
     * <ul>
     *   <li><b>Manual:</b> The developer calls
     *       {@code ClientMain.getStateManager().notifyStateChange(this)}
     *       after modifying a {@code @State} field.</li>
     *   <li><b>Generated:</b> The annotation processor generates setter
     *       proxies for {@code @State} fields that automatically call
     *       this method after assignment.</li>
     *   <li><b>Event handler:</b> After an event handler modifies state,
     *       the framework automatically calls this method.</li>
     * </ul>
     *
     * <h3>Algorithm:</h3>
     * <ol>
     *   <li>Look up the component's tracking entry by identity hash code.</li>
     *   <li>Compare current {@code @State} field values against the stored
     *       snapshot.</li>
     *   <li>If no values changed, return immediately (no-op).</li>
     *   <li>If any value changed:
     *     <ol>
     *       <li>Call {@code component.render()} to produce a new virtual tree.</li>
     *       <li>Call {@code JuxDomBridge.patch()} to diff the old tree against
     *           the new tree and apply DOM mutations.</li>
     *       <li>Update the stored virtual tree to the new tree.</li>
     *       <li>Take a fresh value snapshot.</li>
     *     </ol>
     *   </li>
     * </ol>
     *
     * @param component the component instance whose state may have changed
     * @throws NullPointerException if component is null
     */
    public void notifyStateChange(Object component) {
        Objects.requireNonNull(component, "Component must not be null");

        int key = System.identityHashCode(component);
        ComponentEntry entry = entries.get(key);

        if (entry == null) {
            /*
             * The component was not registered. This could happen if
             * notifyStateChange is called on a server-only component
             * or before hydration completes. Log and return.
             */
            System.err.println("[JUX WARN] notifyStateChange called for "
                    + "unregistered component: " + component.getClass().getName());
            return;
        }

        /*
         * Check if any @State field value has actually changed since
         * the last render. This avoids unnecessary re-renders when
         * notifyStateChange is called but the state hasn't changed
         * (e.g. setting a field to its current value).
         */
        if (!hasStateChanged(entry)) {
            return;
        }

        /*
         * State has changed. Perform a re-render cycle:
         * 1. Render a new virtual tree from the component.
         * 2. Diff the old tree against the new tree.
         * 3. Patch the real DOM with minimal mutations.
         * 4. Update the stored tree and snapshot.
         */
        performReRender(entry);
    }

    /**
     * Unregister a component from state tracking.
     *
     * <p>Called during component teardown (when {@link ClientMain#destroyComponent}
     * is invoked). Removes all references to the component, its DOM element,
     * and its virtual tree, allowing garbage collection.</p>
     *
     * @param component the component instance to unregister
     */
    public void unregisterComponent(Object component) {
        if (component != null) {
            int key = System.identityHashCode(component);
            entries.remove(key);
        }
    }

    // ====================================================================
    //  Private: field discovery
    // ====================================================================

    /**
     * Discover all {@link State @State}-annotated fields on a component class,
     * including fields declared in superclasses.
     *
     * <p>Walks the class hierarchy from the concrete class up to (but not
     * including) {@link Component}, collecting all fields annotated with
     * {@code @State}. Each discovered field is made accessible to allow
     * reading private fields via reflection.</p>
     *
     * @param clazz the component class to introspect
     * @return a list of all {@code @State} fields, empty if none found
     */
    private List<Field> discoverStateFields(Class<?> clazz) {
        List<Field> stateFields = new ArrayList<>();

        /*
         * Walk up the class hierarchy. Stop at Component (the base class)
         * since it has no @State fields and going further into Object
         * is pointless.
         */
        Class<?> current = clazz;
        while (current != null && current != Component.class
                && current != Object.class) {

            Field[] declaredFields = current.getDeclaredFields();

            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(State.class)) {
                    /*
                     * Make the field accessible so we can read its value
                     * via reflection even if it is private. TeaVM supports
                     * this basic reflective access.
                     */
                    field.setAccessible(true);
                    stateFields.add(field);
                }
            }

            /* Move up to the superclass. */
            current = current.getSuperclass();
        }

        return stateFields;
    }

    // ====================================================================
    //  Private: snapshot management
    // ====================================================================

    /**
     * Take a snapshot of all {@code @State} field values on the component.
     *
     * <p>The snapshot is a map of field name to the string representation
     * of the field's current value. This snapshot is stored in the
     * {@link ComponentEntry} and used later by {@link #hasStateChanged}
     * to detect whether a re-render is needed.</p>
     *
     * <p><b>String representation:</b> Using {@code toString()} is a
     * structural simplification. It correctly handles:
     * <ul>
     *   <li>Primitives ({@code int}, {@code boolean}, etc.) via autoboxing</li>
     *   <li>Strings (identity)</li>
     *   <li>Collections and arrays (if their {@code toString()} is stable)</li>
     * </ul>
     * A production implementation would use deep equality via
     * {@link Object#equals(Object)} or structural serialization.</p>
     *
     * @param entry the component entry containing the state fields and snapshot map
     */
    private void takeSnapshot(ComponentEntry entry) {
        entry.valueSnapshot.clear();

        for (Field field : entry.stateFields) {
            try {
                Object value = field.get(entry.component);

                /*
                 * Convert to string representation for comparison.
                 * Null values are represented as the literal string "null"
                 * to distinguish them from fields that were never read.
                 */
                String stringValue = (value != null) ? value.toString() : "null";
                entry.valueSnapshot.put(field.getName(), stringValue);
            } catch (IllegalAccessException e) {
                /*
                 * This should not happen because we called setAccessible(true)
                 * during field discovery. Log and skip the field.
                 */
                System.err.println("[JUX WARN] Cannot read @State field '"
                        + field.getName() + "' on "
                        + entry.component.getClass().getName()
                        + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check whether any {@code @State} field has changed since the last
     * snapshot.
     *
     * <p>Compares the current value of each {@code @State} field against
     * the stored snapshot. If any field's string representation differs,
     * returns {@code true} indicating a re-render is needed.</p>
     *
     * @param entry the component entry to check
     * @return {@code true} if at least one {@code @State} field has changed
     */
    private boolean hasStateChanged(ComponentEntry entry) {
        for (Field field : entry.stateFields) {
            try {
                Object currentValue = field.get(entry.component);
                String currentString = (currentValue != null)
                        ? currentValue.toString() : "null";

                String snapshotValue = entry.valueSnapshot.get(field.getName());

                /*
                 * If the current string representation differs from the
                 * snapshot, state has changed.
                 */
                if (!currentString.equals(snapshotValue)) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                /*
                 * If we can't read the field, assume it might have changed
                 * to be safe. Better to re-render unnecessarily than to
                 * miss an update.
                 */
                return true;
            }
        }

        /* All fields match their snapshot values; no change detected. */
        return false;
    }

    // ====================================================================
    //  Private: re-render cycle
    // ====================================================================

    /**
     * Perform a full re-render cycle for a component whose state has changed.
     *
     * <p>This is the core re-render pipeline:</p>
     * <ol>
     *   <li>Call {@code component.render()} to produce the new virtual tree.</li>
     *   <li>Get the shared {@link JuxDomBridge} from {@link ClientMain}.</li>
     *   <li>Call {@code bridge.patch(rootElement, oldTree, newTree)} to diff
     *       the trees and apply minimal DOM mutations.</li>
     *   <li>Re-bind event handlers from the new tree onto the (potentially
     *       mutated) DOM via {@link EventBinder}.</li>
     *   <li>Update the entry's stored tree and value snapshot to reflect
     *       the new state.</li>
     * </ol>
     *
     * <p>If the render or patch throws an exception, the error is logged
     * and the component continues to display its previous state. This
     * prevents a broken re-render from crashing the entire page.</p>
     *
     * @param entry the component entry to re-render
     */
    private void performReRender(ComponentEntry entry) {
        try {
            /*
             * Step 1: Produce the new virtual tree.
             *
             * This calls the component's render() method, which reads the
             * (now-modified) @State fields and produces an updated Element tree.
             */
            Component component = (Component) entry.component;
            Element newTree = component.render();

            if (newTree == null) {
                System.err.println("[JUX ERROR] Component "
                        + component.getClass().getName()
                        + ".render() returned null; skipping re-render.");
                return;
            }

            /*
             * Step 2: Diff the old tree against the new tree and patch the DOM.
             *
             * The JuxDomBridge handles the diffing algorithm and applies
             * only the mutations needed to transform the old DOM into
             * the new DOM.
             */
            Element oldTree = entry.currentTree;
            JuxDomBridge bridge = ClientMain.getDomBridge();
            bridge.patch(entry.rootElement, oldTree, newTree);

            /*
             * Step 3: Re-bind event handlers.
             *
             * After patching, some DOM nodes may have been replaced (e.g.
             * when the tag changed). We re-bind the event handlers from
             * the new virtual tree to ensure all interactive elements
             * are wired up correctly.
             *
             * The EventBinder also re-binds @On annotated methods.
             */
            EventBinder eventBinder = ClientMain.getEventBinder();
            eventBinder.bindEventHandlers(newTree, entry.rootElement);

            /*
             * Step 4: Update the stored tree and snapshot.
             *
             * The new tree becomes the "old" tree for the next diff cycle.
             * The snapshot is refreshed to reflect the current @State values.
             */
            entry.currentTree = newTree;
            takeSnapshot(entry);

        } catch (Exception e) {
            /*
             * If the re-render fails, log the error but leave the DOM
             * in its previous state. The component remains functional
             * with stale UI; the next state change will retry the render.
             */
            System.err.println("[JUX ERROR] Re-render failed for "
                    + entry.component.getClass().getName()
                    + ": " + e.getMessage());
        }
    }
}
