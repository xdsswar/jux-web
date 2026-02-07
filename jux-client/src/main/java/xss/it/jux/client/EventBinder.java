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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.NodeList;

import xss.it.jux.annotation.On;
import xss.it.jux.core.DomEvent;
import xss.it.jux.core.Element;
import xss.it.jux.core.EventHandler;

/**
 * Binds event handlers to real DOM elements from two sources:
 * <ol>
 *   <li><b>Virtual tree handlers:</b> {@link EventHandler} callbacks registered
 *       on {@link Element} nodes via the {@code .on("click", handler)} API.</li>
 *   <li><b>Annotated handlers:</b> methods on {@link xss.it.jux.core.Component}
 *       subclasses annotated with {@link On @On}, which declare event bindings
 *       at the class level with optional CSS selector targeting.</li>
 * </ol>
 *
 * <h2>Virtual Tree Handlers</h2>
 * <p>These are the inline event handlers defined in the component's
 * {@code render()} method via the fluent API:</p>
 * <pre>{@code
 * button().text("Click me").on("click", e -> count++)
 * }</pre>
 * <p>The {@link #bindEventHandlers(Element, HTMLElement)} method walks the
 * virtual tree and the real DOM in parallel (similar to hydration) and
 * attaches each handler's {@link EventHandler} to the corresponding real
 * DOM node.</p>
 *
 * <h2>Annotated Handlers ({@code @On})</h2>
 * <p>These are method-level event bindings declared with annotations:</p>
 * <pre>{@code
 * @On("click")
 * public void handleClick(DomEvent event) { ... }
 *
 * @On(value = "input", target = "#search")
 * public void handleSearch(DomEvent event) { ... }
 * }</pre>
 * <p>The {@link #bindAnnotatedHandlers(Object, HTMLElement)} method uses
 * reflection to discover all {@code @On}-annotated methods on the component
 * class and binds them to the appropriate DOM elements.</p>
 *
 * <h2>Target Resolution</h2>
 * <p>For {@code @On} annotations, the {@code target()} attribute specifies
 * which DOM element(s) should receive the listener:</p>
 * <ul>
 *   <li><b>Empty string (default):</b> the event listener is attached to
 *       the component's root DOM element. Events bubble up from children,
 *       so the handler fires for any event originating within the component.</li>
 *   <li><b>CSS selector:</b> the selector is resolved against the component's
 *       root element using {@code querySelectorAll()}, and the listener is
 *       attached to every matching element. Examples: {@code "#myId"},
 *       {@code ".my-class"}, {@code "button[type=submit]"}.</li>
 * </ul>
 *
 * <h2>DomEvent Bridging</h2>
 * <p>Both binding methods convert native browser {@link Event} objects into
 * JUX {@link DomEvent} wrappers before dispatching to handlers. This provides
 * a consistent, platform-agnostic event API to component code.</p>
 *
 * <h2>State Change Notification</h2>
 * <p>After an event handler executes, this binder automatically calls
 * {@link StateManager#notifyStateChange(Object)} on the owning component.
 * This ensures that any {@code @State} field modifications made within the
 * handler trigger an automatic re-render without requiring the developer
 * to manually notify the state manager.</p>
 *
 * @see On
 * @see EventHandler
 * @see DomEvent
 * @see JuxDomBridge
 * @see StateManager
 */
public class EventBinder {

    /**
     * Construct a new EventBinder.
     *
     * <p>Called once during {@link ClientMain} initialization. The resulting
     * instance is shared across all component hydrations and re-render cycles.</p>
     */
    public EventBinder() {
        // No initialization required; all state is per-invocation.
    }

    // ====================================================================
    //  Virtual tree event binding
    // ====================================================================

    /**
     * Walk a virtual {@link Element} tree and a real DOM tree in parallel,
     * attaching event handlers from the virtual tree's nodes to the
     * corresponding real DOM elements.
     *
     * <p>This method is used in two contexts:</p>
     * <ul>
     *   <li><b>Initial hydration:</b> after server-rendered HTML is loaded,
     *       this attaches the interactive event handlers that were defined
     *       in the component's {@code render()} method.</li>
     *   <li><b>Post-patch rebinding:</b> after a state-change re-render and
     *       DOM patch, some nodes may have been replaced. This method ensures
     *       all event handlers are properly attached to the (potentially new)
     *       DOM nodes.</li>
     * </ul>
     *
     * <h3>Algorithm:</h3>
     * <ol>
     *   <li>For the given virtual Element, iterate over its
     *       {@link Element#getEventHandlers()} and attach each to the
     *       real DOM element via {@code addEventListener}.</li>
     *   <li>Recursively process each virtual child, mapping it to the
     *       corresponding real DOM child (by index, skipping non-element
     *       nodes in the real DOM).</li>
     * </ol>
     *
     * <p><b>Note:</b> This walks the trees by index (the i-th virtual child
     * maps to the i-th real element child), consistent with the hydration
     * strategy in {@link JuxDomBridge}.</p>
     *
     * @param virtualTree the virtual Element tree containing event handlers
     * @param realElement the corresponding real DOM element
     * @throws NullPointerException if either argument is null
     */
    public void bindEventHandlers(Element virtualTree, HTMLElement realElement) {
        Objects.requireNonNull(virtualTree, "Virtual tree must not be null");
        Objects.requireNonNull(realElement, "Real element must not be null");

        /*
         * Step 1: Bind event handlers on this node.
         *
         * Each handler in the virtual tree is wrapped in a TeaVM
         * EventListener that bridges native Event -> DomEvent.
         */
        Map<String, EventHandler> handlers = virtualTree.getEventHandlers();

        for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
            String eventName = entry.getKey();
            EventHandler handler = entry.getValue();

            realElement.addEventListener(eventName, createListener(handler, null));
        }

        /*
         * Step 2: Recursively bind handlers on children.
         *
         * Walk virtual children and real DOM element children in parallel.
         * Non-element real nodes (text nodes, comments) are skipped.
         */
        List<Element> virtualChildren = virtualTree.getChildren();
        int realChildIndex = 0;

        for (int i = 0; i < virtualChildren.size(); i++) {
            Element virtualChild = virtualChildren.get(i);

            /*
             * Find the next element child in the real DOM, skipping
             * text nodes and comment nodes.
             */
            HTMLElement realChild = findNthElementChild(realElement, i);

            if (realChild == null) {
                /*
                 * Mismatch: fewer real children than virtual children.
                 * This can happen if the DOM was modified externally or
                 * if there's a server/client rendering inconsistency.
                 * Stop binding further children.
                 */
                System.err.println("[JUX WARN] EventBinder: virtual tree has "
                        + virtualChildren.size() + " children but real DOM has "
                        + "fewer element children for <"
                        + virtualTree.getTag() + ">.");
                break;
            }

            /* Recursively bind handlers on the child subtree. */
            bindEventHandlers(virtualChild, realChild);
        }
    }

    // ====================================================================
    //  Annotated handler binding (@On)
    // ====================================================================

    /**
     * Discover and bind all {@link On @On}-annotated methods on a component
     * to their target DOM elements.
     *
     * <p>This method performs reflective introspection on the component's
     * class (including superclasses up to {@link xss.it.jux.core.Component})
     * to find all methods annotated with {@code @On}. For each annotated
     * method, it:</p>
     * <ol>
     *   <li>Reads the event name from {@link On#value()} (e.g. "click").</li>
     *   <li>Reads the target CSS selector from {@link On#target()} (e.g.
     *       "#search-input", ".btn"). An empty target means the component's
     *       root element.</li>
     *   <li>Resolves the target element(s) within the component's root DOM
     *       element using {@code querySelectorAll} (for CSS selectors) or
     *       the root element itself (for empty targets).</li>
     *   <li>Attaches a DOM event listener that invokes the annotated method
     *       via reflection when the event fires.</li>
     * </ol>
     *
     * <h3>Method Signatures</h3>
     * <p>An {@code @On}-annotated method should follow one of these signatures:</p>
     * <ul>
     *   <li>{@code public void handleClick(DomEvent event)} &mdash; receives
     *       the event data wrapper.</li>
     *   <li>{@code public void handleClick()} &mdash; no-arg; the event data
     *       is discarded.</li>
     * </ul>
     * <p>The method may be public, protected, package-private, or private.
     * It is made accessible via {@link Method#setAccessible(boolean)}.</p>
     *
     * <h3>Auto State Notification</h3>
     * <p>After each annotated handler invocation, the binder automatically
     * calls {@link StateManager#notifyStateChange(Object)} to trigger a
     * re-render if any {@code @State} fields were modified within the handler.</p>
     *
     * @param component the component instance containing {@code @On} methods
     * @param root      the component's root DOM element (the scope for
     *                   CSS selector resolution)
     * @throws NullPointerException if either argument is null
     */
    public void bindAnnotatedHandlers(Object component, HTMLElement root) {
        Objects.requireNonNull(component, "Component must not be null");
        Objects.requireNonNull(root, "Root DOM element must not be null");

        /*
         * Scan the component class hierarchy for @On-annotated methods.
         * We walk from the concrete class up to (but not including) the
         * Component base class.
         */
        Class<?> clazz = component.getClass();

        while (clazz != null && clazz != xss.it.jux.core.Component.class
                && clazz != Object.class) {

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                On onAnnotation = method.getAnnotation(On.class);

                if (onAnnotation == null) {
                    /* Not an @On method; skip. */
                    continue;
                }

                /* Extract the event name and target selector. */
                String eventName = onAnnotation.value();
                String targetSelector = onAnnotation.target();

                /* Make the method accessible (it may be private). */
                method.setAccessible(true);

                /*
                 * Determine which DOM elements should receive the listener.
                 *
                 * Empty target = bind to the root element (events bubble up).
                 * Non-empty target = CSS selector resolved against root.
                 */
                if (targetSelector == null || targetSelector.isEmpty()) {
                    /*
                     * Bind to the root element. Any matching event that
                     * bubbles up from a descendant will trigger the handler.
                     */
                    root.addEventListener(eventName,
                            createAnnotatedListener(component, method));
                } else {
                    /*
                     * Resolve the CSS selector against the component's root
                     * element. This limits the selector scope to within
                     * this component (not the entire document).
                     */
                    bindToSelectorTargets(component, method, eventName,
                            targetSelector, root);
                }
            }

            /* Move up to the superclass. */
            clazz = clazz.getSuperclass();
        }
    }

    // ====================================================================
    //  Private: listener creation for virtual tree handlers
    // ====================================================================

    /**
     * Create a TeaVM {@link EventListener} that wraps a JUX
     * {@link EventHandler} and optionally notifies the state manager
     * after execution.
     *
     * <p>The listener extracts event data from the native browser
     * {@link Event}, constructs a {@link DomEvent} wrapper, and
     * dispatches to the handler. If a component instance is provided,
     * state change notification is triggered after the handler returns.</p>
     *
     * @param handler   the JUX event handler to wrap
     * @param component the owning component instance for state notification,
     *                   or null to skip state notification
     * @return a TeaVM EventListener that delegates to the handler
     */
    private EventListener<Event> createListener(EventHandler handler,
                                                  Object component) {
        return (Event nativeEvent) -> {
            /* Build a DomEvent from the native event properties. */
            DomEvent domEvent = buildDomEvent(nativeEvent);

            /* Dispatch to the JUX handler. */
            handler.handle(domEvent);

            /* Honor preventDefault / stopPropagation from the handler. */
            if (domEvent.isDefaultPrevented()) {
                nativeEvent.preventDefault();
            }
            if (domEvent.isPropagationStopped()) {
                nativeEvent.stopPropagation();
            }

            /*
             * If we have a component reference, notify the state manager
             * that state may have changed. This triggers a re-render
             * cycle if any @State fields were modified.
             */
            if (component != null) {
                ClientMain.getStateManager().notifyStateChange(component);
            }
        };
    }

    // ====================================================================
    //  Private: listener creation for @On annotated methods
    // ====================================================================

    /**
     * Create a TeaVM {@link EventListener} that invokes an
     * {@code @On}-annotated method on a component via reflection.
     *
     * <p>The listener handles two method signatures:</p>
     * <ul>
     *   <li><b>One-arg ({@link DomEvent}):</b> the event data is passed
     *       to the method.</li>
     *   <li><b>No-arg:</b> the method is invoked without arguments; the
     *       event data is discarded.</li>
     * </ul>
     *
     * <p>After the method returns, the state manager is notified to check
     * for {@code @State} field changes and trigger a re-render if needed.</p>
     *
     * @param component the component instance to invoke the method on
     * @param method    the {@code @On}-annotated method
     * @return a TeaVM EventListener that reflectively invokes the method
     */
    private EventListener<Event> createAnnotatedListener(Object component,
                                                           Method method) {
        /*
         * Determine the method's parameter count at binding time
         * (not at invocation time) for efficiency.
         */
        int paramCount = method.getParameterCount();

        return (Event nativeEvent) -> {
            try {
                if (paramCount == 1) {
                    /*
                     * The method accepts a DomEvent parameter.
                     * Build the event wrapper and pass it.
                     */
                    DomEvent domEvent = buildDomEvent(nativeEvent);
                    method.invoke(component, domEvent);

                    /* Honor preventDefault / stopPropagation. */
                    if (domEvent.isDefaultPrevented()) {
                        nativeEvent.preventDefault();
                    }
                    if (domEvent.isPropagationStopped()) {
                        nativeEvent.stopPropagation();
                    }
                } else {
                    /*
                     * No-arg method. Just invoke it without arguments.
                     * The developer doesn't need event details.
                     */
                    method.invoke(component);
                }
            } catch (Exception e) {
                System.err.println("[JUX ERROR] @On handler '"
                        + method.getName() + "' on "
                        + component.getClass().getName()
                        + " threw: " + e.getMessage());
            }

            /*
             * After every annotated handler invocation, notify the state
             * manager. If the handler modified any @State fields, this
             * will trigger a re-render and DOM patch.
             */
            ClientMain.getStateManager().notifyStateChange(component);
        };
    }

    // ====================================================================
    //  Private: CSS selector target resolution
    // ====================================================================

    /**
     * Resolve a CSS selector against a root element and bind an event
     * listener to every matching element.
     *
     * <p>Uses {@code querySelectorAll} on the root element to find all
     * elements matching the selector within the component's DOM subtree.
     * The listener is attached to each matching element individually.</p>
     *
     * <p>If no elements match the selector, a warning is logged. This
     * commonly indicates a typo in the selector or a mismatch between
     * the {@code @On(target = "...")} annotation and the rendered HTML.</p>
     *
     * @param component      the component instance
     * @param method         the {@code @On}-annotated method to invoke
     * @param eventName      the DOM event name (e.g. "click", "input")
     * @param targetSelector the CSS selector string (e.g. "#search", ".btn")
     * @param root           the component's root DOM element (selector scope)
     */
    private void bindToSelectorTargets(Object component, Method method,
                                        String eventName, String targetSelector,
                                        HTMLElement root) {
        /*
         * querySelectorAll returns all elements within root that match
         * the CSS selector. The search is scoped to the component's
         * subtree, not the entire document.
         */
        NodeList<? extends Node> matches = root.querySelectorAll(targetSelector);
        int matchCount = matches.getLength();

        if (matchCount == 0) {
            /*
             * No elements matched the selector. This is likely a
             * developer error (typo in selector, or the element hasn't
             * been rendered yet). Log a warning.
             */
            System.err.println("[JUX WARN] @On(value=\"" + eventName
                    + "\", target=\"" + targetSelector + "\") on "
                    + component.getClass().getName() + "."
                    + method.getName() + " matched 0 elements.");
            return;
        }

        /*
         * Create one listener instance and attach it to all matching
         * elements. The listener is the same for all targets since
         * it invokes the same method on the same component.
         */
        EventListener<Event> listener = createAnnotatedListener(component, method);

        for (int i = 0; i < matchCount; i++) {
            Node node = matches.item(i);

            /*
             * querySelectorAll only returns Element nodes, so this cast
             * is safe. Attach the listener.
             */
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                HTMLElement targetElement = (HTMLElement) node;
                targetElement.addEventListener(eventName, listener);
            }
        }
    }

    // ====================================================================
    //  Private: DomEvent construction from native Event
    // ====================================================================

    /**
     * Build a JUX {@link DomEvent} wrapper from a native browser {@link Event}.
     *
     * <p>Extracts commonly-needed properties from the native event and
     * packages them into JUX's platform-agnostic event wrapper. Properties
     * that are not available on the specific event type (e.g. "key" on a
     * MouseEvent) default to safe values (empty string, 0, false).</p>
     *
     * <p><b>Extracted properties:</b></p>
     * <ul>
     *   <li>{@code type} &mdash; the event type string (e.g. "click")</li>
     *   <li>{@code targetId} &mdash; the {@code id} attribute of the target element</li>
     *   <li>{@code value} &mdash; the input value for form elements, or text content</li>
     *   <li>{@code key} &mdash; the key identifier for keyboard events</li>
     *   <li>{@code shiftKey, ctrlKey, altKey, metaKey} &mdash; modifier key states</li>
     *   <li>{@code clientX, clientY} &mdash; mouse position for pointer events</li>
     * </ul>
     *
     * <p><b>Structural simplification:</b> In this implementation, keyboard
     * and mouse properties default to zero/empty/false since extracting them
     * requires casting to the specific TeaVM event subtypes (KeyboardEvent,
     * MouseEvent). A production implementation would perform these casts and
     * extract the full property set.</p>
     *
     * @param nativeEvent the native browser event from TeaVM JSO
     * @return a JUX DomEvent wrapper containing the extracted properties
     */
    private DomEvent buildDomEvent(Event nativeEvent) {
        String type = nativeEvent.getType();

        /*
         * Extract target element properties.
         *
         * We attempt to cast the event target to HTMLElement to read
         * its id and value. If the target is not an element (e.g.
         * it's the document or a text node), we use safe defaults.
         */
        String targetId = "";
        String value = "";

        try {
            HTMLElement target = (HTMLElement) nativeEvent.getTarget();
            if (target != null) {
                /* Read the element's id attribute. */
                String id = target.getAttribute("id");
                targetId = (id != null) ? id : "";

                /*
                 * Read the element's value. For input/textarea/select,
                 * this is the current input value. For other elements,
                 * we read textContent as a fallback.
                 */
                value = extractElementValue(target);
            }
        } catch (ClassCastException e) {
            /*
             * Target is not an HTMLElement (could be Document, Window, etc.).
             * Use empty defaults.
             */
        }

        /*
         * Keyboard and mouse properties.
         *
         * Structural simplification: these default to zero/empty/false.
         * A production implementation would cast to KeyboardEvent or
         * MouseEvent using TeaVM's JSO type system to extract real values.
         */
        String key = "";
        boolean shiftKey = false;
        boolean ctrlKey = false;
        boolean altKey = false;
        boolean metaKey = false;
        double clientX = 0;
        double clientY = 0;

        return new DomEvent(
                type, targetId, value, key,
                shiftKey, ctrlKey, altKey, metaKey,
                clientX, clientY
        );
    }

    /**
     * Extract the "value" from a DOM element, handling different element
     * types appropriately.
     *
     * <p>For form elements ({@code <input>}, {@code <textarea>},
     * {@code <select>}), returns the input value. For other elements,
     * returns the text content. Returns an empty string if no value
     * can be determined.</p>
     *
     * @param element the DOM element to extract a value from
     * @return the element's value or text content, never null
     */
    private String extractElementValue(HTMLElement element) {
        if (element == null) {
            return "";
        }

        /*
         * Check the element's tag name to determine value extraction
         * strategy. Form elements have a "value" property; other
         * elements use textContent.
         */
        String tag = element.getTagName();
        if (tag != null) {
            tag = tag.toLowerCase();
        }

        if ("input".equals(tag) || "textarea".equals(tag) || "select".equals(tag)) {
            /*
             * For form elements, read the "value" attribute.
             *
             * Note: getAttribute("value") reads the HTML attribute, not the
             * live DOM property. For the structural implementation this is
             * acceptable. A production implementation would cast to
             * HTMLInputElement and call getValue() for the live property.
             */
            String val = element.getAttribute("value");
            return (val != null) ? val : "";
        }

        /* For non-form elements, return text content. */
        String text = element.getTextContent();
        return (text != null) ? text : "";
    }

    // ====================================================================
    //  Private: DOM child traversal
    // ====================================================================

    /**
     * Find the n-th element child of a parent element, skipping non-element
     * nodes (text nodes, comment nodes, etc.).
     *
     * <p>The virtual tree models only element nodes, but the real DOM may
     * contain interspersed text nodes (whitespace between tags), comment
     * nodes, and processing instructions. This method skips all non-element
     * nodes to find the element at the given logical index.</p>
     *
     * @param parent the parent DOM element
     * @param n      the zero-based index of the desired element child
     * @return the n-th element child, or null if fewer than {@code n+1}
     *         element children exist
     */
    private HTMLElement findNthElementChild(HTMLElement parent, int n) {
        NodeList<Node> childNodes = parent.getChildNodes();
        int length = childNodes.getLength();
        int elementCount = 0;

        for (int i = 0; i < length; i++) {
            Node node = childNodes.item(i);

            /*
             * Node.ELEMENT_NODE (value 1) identifies element nodes.
             * Text nodes are type 3, comment nodes are type 8,
             * document nodes are type 9, etc.
             */
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (elementCount == n) {
                    return (HTMLElement) node;
                }
                elementCount++;
            }
        }

        /* Fewer element children than requested index. */
        return null;
    }
}
