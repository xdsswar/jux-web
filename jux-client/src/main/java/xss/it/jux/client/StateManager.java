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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.teavm.jso.dom.html.HTMLElement;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

/**
 * Manages reactive state for client-side JUX components, triggering re-render
 * and DOM patching when state changes are notified.
 *
 * <h2>Reactive State Model</h2>
 * <p>JUX components declare reactive fields with the {@code @State} annotation.
 * When a component's event handler modifies state, the framework calls
 * {@link #notifyStateChange(Object)} which unconditionally re-renders the
 * component and patches the DOM.</p>
 *
 * <h2>TeaVM Compatibility</h2>
 * <p>This implementation avoids all Java reflection (no {@code getDeclaredFields},
 * no {@code Field.get()}, no {@code isAnnotationPresent}) because TeaVM's
 * ahead-of-time compiler does not reliably support reflective field access.
 * Instead, the state manager takes a simple approach: every notification
 * triggers a re-render. The DOM diff/patch algorithm in {@link JuxDomBridge}
 * ensures only actual changes are applied to the DOM.</p>
 *
 * @see JuxDomBridge#patch(HTMLElement, Element, Element)
 * @see ClientMain
 */
public class StateManager {

    /**
     * Internal record tracking a registered component's render context.
     *
     * <p>Stores everything needed to re-render the component and patch the DOM:</p>
     * <ul>
     *   <li>The component instance (to call {@code render()} on)</li>
     *   <li>The root DOM element (the patch target)</li>
     *   <li>The last-rendered virtual tree (the "old" tree for diffing)</li>
     * </ul>
     */
    private static class ComponentEntry {

        /** The live component instance. */
        final Component component;

        /** The root DOM element that this component hydrated into. */
        HTMLElement rootElement;

        /** The virtual tree from the most recent render. */
        Element currentTree;

        /**
         * Construct a new component entry.
         *
         * @param component   the component instance
         * @param rootElement the root DOM element
         * @param currentTree the initial virtual tree
         */
        ComponentEntry(Component component, HTMLElement rootElement, Element currentTree) {
            this.component = component;
            this.rootElement = rootElement;
            this.currentTree = currentTree;
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
     * a component has been hydrated. It stores the component, its root
     * DOM element, and the initial virtual tree for future re-render cycles.</p>
     *
     * @param component   the component instance that was just hydrated
     * @param root        the root DOM element that this component is rendered into
     * @param initialTree the virtual Element tree from the initial render
     * @throws NullPointerException if any argument is null
     */
    public void registerComponent(Object component, HTMLElement root, Element initialTree) {
        Objects.requireNonNull(component, "Component must not be null");
        Objects.requireNonNull(root, "Root DOM element must not be null");
        Objects.requireNonNull(initialTree, "Initial virtual tree must not be null");

        ComponentEntry entry = new ComponentEntry(
                (Component) component, root, initialTree);

        int key = System.identityHashCode(component);
        entries.put(key, entry);
    }

    /**
     * Notify the state manager that a component's state may have changed,
     * triggering a re-render and DOM patch.
     *
     * <p>This method is the core of JUX's client-side reactivity. It is
     * called after every event handler execution. The method unconditionally
     * re-renders the component and patches the DOM — the diff algorithm
     * in {@link JuxDomBridge#patch} ensures only actual changes are applied.</p>
     *
     * <p>This approach avoids reflection-based field tracking, which is
     * incompatible with TeaVM's ahead-of-time compilation.</p>
     *
     * @param component the component instance whose state may have changed
     * @throws NullPointerException if component is null
     */
    public void notifyStateChange(Object component) {
        Objects.requireNonNull(component, "Component must not be null");

        int key = System.identityHashCode(component);
        ComponentEntry entry = entries.get(key);

        if (entry == null) {
            return;
        }

        performReRender(entry);
    }

    /**
     * Unregister a component from state tracking.
     *
     * <p>Called during component teardown. Removes all references to the
     * component, its DOM element, and its virtual tree.</p>
     *
     * @param component the component instance to unregister
     */
    public void unregisterComponent(Object component) {
        if (component != null) {
            int key = System.identityHashCode(component);
            entries.remove(key);
        }
    }

    /**
     * Perform a full re-render cycle for a component.
     *
     * <ol>
     *   <li>Call {@code component.render()} to produce the new virtual tree.</li>
     *   <li>Call {@code bridge.patch(rootElement, oldTree, newTree)} to diff
     *       the trees and apply minimal DOM mutations.</li>
     *   <li>Re-bind event handlers from the new tree.</li>
     *   <li>Update the entry's stored tree.</li>
     * </ol>
     *
     * @param entry the component entry to re-render
     */
    private void performReRender(ComponentEntry entry) {
        try {
            Element newTree = entry.component.render();

            if (newTree == null) {
                System.err.println("[JUX ERROR] Component "
                        + entry.component.getClass().getName()
                        + ".render() returned null; skipping re-render.");
                return;
            }

            Element oldTree = entry.currentTree;
            JuxDomBridge bridge = ClientMain.getDomBridge();

            /*
             * Set the active component on the bridge so that any NEW DOM
             * elements created during patching (e.g. new list items) get
             * the correct component reference for state notification.
             */
            bridge.setActiveComponent(entry.component);
            bridge.patch(entry.rootElement, oldTree, newTree);
            bridge.setActiveComponent(null);

            /*
             * Event handlers are updated by the handler registry inside
             * patch() — no separate bindEventHandlers() call needed.
             * The registry swaps handler references without adding
             * duplicate addEventListener calls.
             */

            entry.currentTree = newTree;

        } catch (Exception e) {
            System.err.println("[JUX ERROR] Re-render failed for "
                    + entry.component.getClass().getName()
                    + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
