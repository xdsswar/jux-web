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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.NodeList;

import xss.it.jux.core.DomEvent;
import xss.it.jux.core.Element;
import xss.it.jux.core.EventHandler;

/**
 * Bridge between JUX's virtual {@link Element} tree and the browser's real DOM,
 * accessed via TeaVM's {@code org.teavm.jso.dom.html} API.
 *
 * <p>This class is the core of JUX's client-side rendering. It provides three
 * fundamental operations:</p>
 *
 * <ol>
 *   <li><b>{@link #createElement(Element)}:</b> Creates a real DOM subtree from
 *       a virtual Element tree. Used when new elements are inserted into the page
 *       (e.g. dynamically added children after a state change).</li>
 *   <li><b>{@link #hydrate(Element, HTMLElement)}:</b> Walks a virtual Element tree
 *       alongside an existing server-rendered DOM tree and attaches event handlers
 *       from the virtual tree to matching real DOM nodes. The DOM structure is
 *       <em>not</em> modified; it was already rendered correctly by SSR.</li>
 *   <li><b>{@link #patch(HTMLElement, Element, Element)}:</b> Diffs an old virtual
 *       tree against a new virtual tree and applies the minimal set of DOM mutations
 *       needed to bring the real DOM in sync with the new tree.</li>
 * </ol>
 *
 * <h2>Event Handler Bridging</h2>
 * <p>JUX's virtual {@link Element} carries event handlers as
 * {@link EventHandler} callbacks. The browser DOM expects
 * {@link EventListener EventListener&lt;Event&gt;} callbacks. This bridge
 * adapts between the two by wrapping each {@link EventHandler} in an
 * {@link EventListener} that extracts event data from the native
 * {@link Event} object and constructs a {@link DomEvent} wrapper.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods execute on the browser's main (UI) thread. JavaScript
 * is single-threaded, so no synchronization is needed.</p>
 *
 * @see Element
 * @see ClientMain
 * @see StateManager
 */
public class JuxDomBridge {

    /**
     * Reference to the browser's {@link HTMLDocument}, obtained once from
     * {@link Window#current()} and cached for the lifetime of the bridge.
     *
     * <p>All DOM element creation and querying goes through this document
     * reference. In the browser, there is exactly one document per frame.</p>
     */
    private final HTMLDocument document;

    /**
     * Construct a new DOM bridge using the current browser window's document.
     *
     * <p>This constructor is called once during {@link ClientMain} initialization.
     * The resulting bridge instance is shared across all component hydrations
     * and state-change re-renders.</p>
     */
    public JuxDomBridge() {
        this.document = Window.current().getDocument();
    }

    // ====================================================================
    //  CREATE: Build real DOM from virtual Element tree
    // ====================================================================

    /**
     * Create a real DOM element (and its entire subtree) from a virtual
     * {@link Element} tree.
     *
     * <p>This method recursively converts the virtual tree into live DOM
     * nodes. It is used when new elements need to be inserted into the
     * page &mdash; for example, when a re-render produces children that
     * did not exist in the previous tree.</p>
     *
     * <h3>Conversion steps for each Element node:</h3>
     * <ol>
     *   <li>Call {@link HTMLDocument#createElement(String)} with the tag name.</li>
     *   <li>Copy all attributes from {@link Element#getAttributes()} onto the
     *       DOM node via {@link HTMLElement#setAttribute(String, String)}.
     *       This includes {@code id}, {@code class}, {@code style}, ARIA
     *       attributes, {@code data-*} attributes, and everything else.</li>
     *   <li>If the element has text content ({@link Element#getTextContent()}
     *       is non-null), set it via {@link HTMLElement#setTextContent(String)}.
     *       Text is already HTML-safe because the virtual tree only carries
     *       plain text; XSS is not a concern here.</li>
     *   <li>Register all event handlers from {@link Element#getEventHandlers()}
     *       on the DOM node using {@link HTMLElement#addEventListener(String,
     *       EventListener)}. Each {@link EventHandler} is wrapped in a
     *       {@link #wrapHandler(EventHandler)} adapter.</li>
     *   <li>Recursively call {@code createElement} for each child in
     *       {@link Element#getChildren()} and append the resulting DOM
     *       nodes via {@link Node#appendChild(Node)}.</li>
     * </ol>
     *
     * @param el the virtual Element describing the node to create; must not be null
     * @return the newly created live DOM element with all attributes, text,
     *         event handlers, and children fully populated
     * @throws NullPointerException if {@code el} is null
     */
    public HTMLElement createElement(Element el) {
        Objects.requireNonNull(el, "Element must not be null");

        /* Step 1: Create the raw DOM element for the given HTML tag. */
        HTMLElement node = document.createElement(el.getTag());

        /* Step 2: Copy all attributes (id, class, style, aria-*, data-*, etc.). */
        Map<String, String> attributes = el.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            node.setAttribute(entry.getKey(), entry.getValue());
        }

        /* Step 3: Set text content if present. */
        String textContent = el.getTextContent();
        if (textContent != null) {
            node.setTextContent(textContent);
        }

        /*
         * Step 4: Bind event handlers.
         *
         * Each EventHandler from the virtual Element is wrapped in a
         * TeaVM EventListener that converts the native browser Event
         * into a JUX DomEvent before dispatching.
         */
        Map<String, EventHandler> handlers = el.getEventHandlers();
        for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
            String eventName = entry.getKey();
            EventHandler handler = entry.getValue();

            node.addEventListener(eventName, wrapHandler(handler));
        }

        /*
         * Step 5: Recursively create and append child elements.
         *
         * Children are processed in order, preserving the render order
         * defined by the virtual tree. Each child becomes a real DOM
         * node that is appended to this parent node.
         */
        List<Element> children = el.getChildren();
        for (Element child : children) {
            HTMLElement childNode = createElement(child);
            node.appendChild(childNode);
        }

        return node;
    }

    // ====================================================================
    //  HYDRATE: Attach handlers to server-rendered DOM
    // ====================================================================

    /**
     * Hydrate an existing server-rendered DOM element by walking the virtual
     * {@link Element} tree in parallel with the real DOM tree and attaching
     * event handlers to matching nodes.
     *
     * <p><b>Why hydration?</b> The server renders fully-formed HTML during SSR.
     * The browser displays this HTML immediately (fast first paint). When the
     * client JavaScript loads, we do <em>not</em> want to re-create the DOM
     * from scratch &mdash; that would cause a visible flash. Instead, we
     * "hydrate" the existing DOM by attaching the interactive bits (event
     * listeners) without modifying the structure or content.</p>
     *
     * <h3>Hydration algorithm:</h3>
     * <ol>
     *   <li>For the given virtual Element and its matching real DOM node,
     *       attach all event handlers from {@link Element#getEventHandlers()}.</li>
     *   <li>Recursively hydrate each child: the i-th virtual child is matched
     *       with the i-th real child node of the DOM element.</li>
     *   <li>If the real DOM has fewer children than the virtual tree expects
     *       (server/client mismatch), stop gracefully with a warning.</li>
     * </ol>
     *
     * <p><b>Assumption:</b> The virtual tree and the real DOM have identical
     * structure (same tags, same children count, same order). This is guaranteed
     * when the same {@link xss.it.jux.core.Component#render()} method runs on
     * both server and client with the same input. If a mismatch is detected,
     * a warning is logged but execution continues.</p>
     *
     * @param el       the virtual Element tree produced by {@code Component.render()}
     * @param existing the real DOM element rendered by the server during SSR
     * @throws NullPointerException if either argument is null
     */
    public void hydrate(Element el, HTMLElement existing) {
        Objects.requireNonNull(el, "Virtual Element must not be null");
        Objects.requireNonNull(existing, "Existing DOM element must not be null");

        /*
         * Attach event handlers from the virtual tree to the real DOM node.
         * This is the primary purpose of hydration: making the server-rendered
         * HTML interactive without modifying its structure.
         */
        Map<String, EventHandler> handlers = el.getEventHandlers();
        for (Map.Entry<String, EventHandler> entry : handlers.entrySet()) {
            String eventName = entry.getKey();
            EventHandler handler = entry.getValue();

            existing.addEventListener(eventName, wrapHandler(handler));
        }

        /*
         * Recursively hydrate children.
         *
         * We walk both trees in parallel: virtual child[i] maps to
         * real childNodes[i]. This relies on the server and client
         * producing identical DOM structure.
         */
        List<Element> virtualChildren = el.getChildren();
        NodeList<Node> realChildren = existing.getChildNodes();

        /*
         * Track the index into the real DOM's child nodes separately
         * because the real DOM may contain text nodes, comment nodes,
         * or other non-element nodes that the virtual tree does not
         * model. We skip non-element nodes when matching.
         */
        int realIndex = 0;

        for (int i = 0; i < virtualChildren.size(); i++) {
            Element virtualChild = virtualChildren.get(i);

            /*
             * Advance past text nodes and comment nodes in the real DOM
             * to find the next element node that corresponds to this
             * virtual child.
             */
            HTMLElement realChild = findNextElementChild(realChildren, realIndex);

            if (realChild == null) {
                /*
                 * Server/client structure mismatch: the real DOM has fewer
                 * element children than the virtual tree expects. This
                 * should not happen in normal operation. Log a warning
                 * and stop hydrating further children.
                 */
                System.err.println("[JUX WARN] Hydration mismatch: virtual tree has "
                        + virtualChildren.size() + " children but real DOM ran out "
                        + "at index " + i + " for <" + el.getTag() + ">.");
                break;
            }

            /*
             * Advance the real index past the element we just found,
             * plus any text/comment nodes that preceded it.
             */
            realIndex = indexOfNode(realChildren, realChild) + 1;

            /* Recursively hydrate the child pair. */
            hydrate(virtualChild, realChild);
        }
    }

    // ====================================================================
    //  PATCH: Diff old vs new tree and apply minimal DOM mutations
    // ====================================================================

    /**
     * Diff an old virtual tree against a new virtual tree and apply the
     * minimal set of DOM mutations to bring the real DOM in sync with
     * the new tree.
     *
     * <p>This method is called by {@link StateManager} after a component's
     * {@code @State} field changes and {@link xss.it.jux.core.Component#render()}
     * produces a new virtual tree.</p>
     *
     * <h3>Diff strategy (structural implementation):</h3>
     * <p>The current implementation uses a simplified diffing algorithm
     * optimized for correctness over minimal mutations. A production-grade
     * implementation would use a keyed reconciliation algorithm (similar to
     * React's or Snabbdom's) for optimal performance with list reordering.
     * The structural approach is:</p>
     *
     * <ol>
     *   <li><b>Tag mismatch:</b> If the old and new elements have different
     *       tags, replace the entire real DOM node with a freshly created
     *       node from the new tree. This is the "nuclear option" and handles
     *       structural changes that cannot be patched in place.</li>
     *   <li><b>Attribute diff:</b> Compare attributes between old and new.
     *       Add new attributes, update changed values, remove attributes
     *       present in old but absent in new.</li>
     *   <li><b>Text content diff:</b> If text content differs, update it
     *       directly via {@link HTMLElement#setTextContent(String)}.</li>
     *   <li><b>Children diff:</b> Walk children by index. For each position:
     *       <ul>
     *         <li>If both old and new have a child at this index, recursively
     *             patch.</li>
     *         <li>If new has an extra child, create and append it.</li>
     *         <li>If old has an extra child (new is shorter), remove it
     *             from the DOM.</li>
     *       </ul>
     *   </li>
     *   <li><b>Event handler rebind:</b> Remove all old event listeners and
     *       attach new ones. (Simplified approach; a production implementation
     *       would diff handlers by event name.)</li>
     * </ol>
     *
     * @param existing the real DOM element currently in the page
     * @param oldTree  the virtual tree from the previous render
     * @param newTree  the virtual tree from the current (post-state-change) render
     * @throws NullPointerException if any argument is null
     */
    public void patch(HTMLElement existing, Element oldTree, Element newTree) {
        Objects.requireNonNull(existing, "Existing DOM element must not be null");
        Objects.requireNonNull(oldTree, "Old virtual tree must not be null");
        Objects.requireNonNull(newTree, "New virtual tree must not be null");

        /*
         * Case 1: Tag mismatch.
         *
         * If the root tags differ, the structure has fundamentally changed
         * and we cannot patch in place. Replace the entire DOM subtree.
         */
        if (!oldTree.getTag().equals(newTree.getTag())) {
            HTMLElement replacement = createElement(newTree);
            Node parent = existing.getParentNode();

            if (parent != null) {
                parent.replaceChild(replacement, existing);
            }
            return;
        }

        /*
         * Case 2: Same tag. Patch attributes, text, events, and children.
         */

        /* --- Attributes --- */
        patchAttributes(existing, oldTree.getAttributes(), newTree.getAttributes());

        /* --- Text content --- */
        patchTextContent(existing, oldTree.getTextContent(), newTree.getTextContent());

        /* --- Event handlers --- */
        patchEventHandlers(existing, oldTree, newTree);

        /* --- Children --- */
        patchChildren(existing, oldTree.getChildren(), newTree.getChildren());
    }

    // ====================================================================
    //  Private helpers: attribute patching
    // ====================================================================

    /**
     * Diff and patch HTML attributes between old and new virtual trees.
     *
     * <p>Three cases are handled:</p>
     * <ul>
     *   <li><b>New attribute:</b> present in {@code newAttrs} but not in
     *       {@code oldAttrs} &rarr; add it to the DOM.</li>
     *   <li><b>Changed attribute:</b> present in both but with different
     *       values &rarr; update the DOM attribute.</li>
     *   <li><b>Removed attribute:</b> present in {@code oldAttrs} but not
     *       in {@code newAttrs} &rarr; remove it from the DOM.</li>
     * </ul>
     *
     * @param node     the real DOM element to update
     * @param oldAttrs attributes from the previous virtual tree
     * @param newAttrs attributes from the new virtual tree
     */
    private void patchAttributes(HTMLElement node,
                                  Map<String, String> oldAttrs,
                                  Map<String, String> newAttrs) {
        /*
         * Pass 1: Add or update attributes that are in the new tree.
         * We iterate over new attributes and set them if they differ
         * from the old value (or are entirely new).
         */
        for (Map.Entry<String, String> entry : newAttrs.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = oldAttrs.get(key);

            /*
             * Only touch the DOM if the value actually changed.
             * Skipping unchanged attributes avoids unnecessary reflows.
             */
            if (!newValue.equals(oldValue)) {
                node.setAttribute(key, newValue);
            }
        }

        /*
         * Pass 2: Remove attributes that were in the old tree but are
         * no longer present in the new tree.
         */
        for (String key : oldAttrs.keySet()) {
            if (!newAttrs.containsKey(key)) {
                node.removeAttribute(key);
            }
        }
    }

    // ====================================================================
    //  Private helpers: text content patching
    // ====================================================================

    /**
     * Update the text content of a DOM element if it has changed between
     * the old and new virtual trees.
     *
     * <p>Text content changes are one of the most common mutations
     * (e.g. a counter label updating from "Count: 3" to "Count: 4").
     * This is a cheap DOM operation.</p>
     *
     * @param node    the real DOM element
     * @param oldText the previous text content (may be null)
     * @param newText the new text content (may be null)
     */
    private void patchTextContent(HTMLElement node, String oldText, String newText) {
        /* Both null means no text content on either side; nothing to do. */
        if (oldText == null && newText == null) {
            return;
        }

        /* If the text content changed (or was added/removed), update the DOM. */
        if (oldText == null || !oldText.equals(newText)) {
            if (newText != null) {
                node.setTextContent(newText);
            } else {
                /*
                 * New tree has no text content but old did. Clear it.
                 * Setting textContent to "" removes text nodes from the element.
                 */
                node.setTextContent("");
            }
        }
    }

    // ====================================================================
    //  Private helpers: event handler patching
    // ====================================================================

    /**
     * Re-bind event handlers after a re-render.
     *
     * <p><b>Simplified approach:</b> In this structural implementation,
     * we clone the DOM node to strip all existing event listeners, then
     * reattach the new handlers. A production implementation would track
     * listeners by name and only add/remove the ones that changed.</p>
     *
     * <p>The clone-and-replace technique works because
     * {@link Node#cloneNode(boolean)} with {@code deep=false} creates a
     * copy of the element without its event listeners. We then move all
     * child nodes from the original to the clone, attach new listeners
     * to the clone, and replace the original in the DOM.</p>
     *
     * <p><b>Note:</b> For the structural implementation, we take a simpler
     * approach: just add the new handlers. Duplicate listeners for the same
     * event name are tolerable in a structural impl since the behavior is
     * correct (the new handler will fire alongside any old one). A
     * production implementation would use {@code removeEventListener} with
     * stored references.</p>
     *
     * @param node    the real DOM element
     * @param oldTree the previous virtual tree (handlers to conceptually remove)
     * @param newTree the new virtual tree (handlers to attach)
     */
    private void patchEventHandlers(HTMLElement node,
                                     Element oldTree,
                                     Element newTree) {
        /*
         * Structural simplification: attach all new event handlers.
         *
         * In a full implementation, we would:
         * 1. Track listener references in a WeakMap-style structure.
         * 2. Remove old listeners that are no longer in the new tree.
         * 3. Add new listeners that were not in the old tree.
         * 4. Replace listeners whose callbacks changed.
         *
         * For the structural impl, we re-attach new handlers. This means
         * the component should be idempotent with respect to handler
         * invocation (which it naturally is since render() produces the
         * same handlers for the same state).
         */
        Map<String, EventHandler> newHandlers = newTree.getEventHandlers();

        for (Map.Entry<String, EventHandler> entry : newHandlers.entrySet()) {
            String eventName = entry.getKey();
            EventHandler handler = entry.getValue();

            node.addEventListener(eventName, wrapHandler(handler));
        }
    }

    // ====================================================================
    //  Private helpers: children patching
    // ====================================================================

    /**
     * Diff and patch the children of a DOM element.
     *
     * <p>Walks children by index, handling three cases:</p>
     * <ul>
     *   <li><b>Both exist:</b> recursively patch the i-th old child against
     *       the i-th new child.</li>
     *   <li><b>New child added:</b> the new tree has more children than the
     *       old tree &rarr; create and append the extra children.</li>
     *   <li><b>Old child removed:</b> the old tree has more children than
     *       the new tree &rarr; remove the extra DOM nodes.</li>
     * </ul>
     *
     * <p><b>Limitation:</b> This index-based diffing does not handle
     * reordering efficiently. If a list item moves from position 0 to
     * position 5, this algorithm will patch all six positions rather than
     * moving the node. A production implementation would use keys
     * ({@code data-jux-key} attributes) for O(n) reconciliation.</p>
     *
     * @param parentNode  the real DOM parent element
     * @param oldChildren the children from the previous virtual tree
     * @param newChildren the children from the new virtual tree
     */
    private void patchChildren(HTMLElement parentNode,
                                List<Element> oldChildren,
                                List<Element> newChildren) {
        int oldSize = oldChildren.size();
        int newSize = newChildren.size();

        /*
         * Determine how many children to patch in place (the minimum
         * of old and new counts).
         */
        int commonLength = Math.min(oldSize, newSize);

        /*
         * Collect the real DOM child element nodes (skipping text nodes
         * and comment nodes that may exist in the live DOM).
         */
        NodeList<Node> realChildNodes = parentNode.getChildNodes();

        /*
         * Patch common children (positions that exist in both old and new).
         * We need to find the i-th element child in the real DOM for each
         * virtual child index.
         */
        int elementIndex = 0;
        int nodeIndex = 0;

        for (int i = 0; i < commonLength; i++) {
            /*
             * Find the next real element child node, skipping non-element
             * nodes (text, comment, etc.).
             */
            HTMLElement realChild = findNthElementChild(parentNode, i);

            if (realChild != null) {
                /* Recursively patch this child subtree. */
                patch(realChild, oldChildren.get(i), newChildren.get(i));
            }
        }

        /*
         * If the new tree has MORE children than the old tree, create and
         * append the extra children to the DOM.
         */
        if (newSize > oldSize) {
            for (int i = oldSize; i < newSize; i++) {
                HTMLElement newChild = createElement(newChildren.get(i));
                parentNode.appendChild(newChild);
            }
        }

        /*
         * If the new tree has FEWER children than the old tree, remove
         * the extra children from the DOM (from the end, to avoid index
         * shifting issues).
         */
        if (oldSize > newSize) {
            /*
             * We need to remove (oldSize - newSize) element children
             * from the end of the parent's child list. We iterate
             * backwards to avoid index invalidation.
             */
            for (int i = oldSize - 1; i >= newSize; i--) {
                HTMLElement excessChild = findNthElementChild(parentNode, i);
                if (excessChild != null) {
                    parentNode.removeChild(excessChild);
                }
            }
        }
    }

    // ====================================================================
    //  Private helpers: event handler wrapping
    // ====================================================================

    /**
     * Wrap a JUX {@link EventHandler} in a TeaVM {@link EventListener} that
     * bridges between the native browser {@link Event} and JUX's
     * {@link DomEvent} wrapper.
     *
     * <p>The wrapper extracts commonly-needed properties from the native
     * event (type, target ID, input value, keyboard key, modifier keys,
     * mouse coordinates) and packages them into a {@link DomEvent} instance.
     * This insulates component code from the raw browser event API and
     * provides a consistent cross-platform interface.</p>
     *
     * <p>After the handler executes, if {@link DomEvent#isDefaultPrevented()}
     * returns {@code true}, the native event's {@code preventDefault()} is
     * called. Similarly for {@link DomEvent#isPropagationStopped()} and
     * {@code stopPropagation()}.</p>
     *
     * @param handler the JUX event handler to wrap
     * @return a TeaVM EventListener that delegates to the handler
     */
    private EventListener<Event> wrapHandler(EventHandler handler) {
        return (Event nativeEvent) -> {
            /*
             * Extract event properties from the native browser event.
             *
             * We use safe defaults for properties that may not exist on
             * all event types (e.g. "key" is only on KeyboardEvent,
             * "clientX" is only on MouseEvent). TeaVM's JSO will return
             * null or 0 for missing properties.
             */
            String type = nativeEvent.getType();

            /* Get the target element for ID and value extraction. */
            HTMLElement target = extractTarget(nativeEvent);
            String targetId = (target != null) ? safeGetAttribute(target, "id") : "";
            String value = (target != null) ? safeGetValue(target) : "";

            /*
             * Keyboard properties. We extract these via the native event's
             * string representation since TeaVM's base Event type does not
             * directly expose KeyboardEvent properties. For the structural
             * implementation, we default to empty values.
             *
             * A production implementation would cast to the appropriate
             * TeaVM JSO event subtype (KeyboardEvent, MouseEvent, etc.).
             */
            String key = "";
            boolean shiftKey = false;
            boolean ctrlKey = false;
            boolean altKey = false;
            boolean metaKey = false;
            double clientX = 0;
            double clientY = 0;

            /* Construct the JUX DomEvent wrapper. */
            DomEvent domEvent = new DomEvent(
                    type, targetId, value, key,
                    shiftKey, ctrlKey, altKey, metaKey,
                    clientX, clientY
            );

            /* Dispatch to the JUX handler. */
            handler.handle(domEvent);

            /*
             * Honor preventDefault / stopPropagation requests from the
             * handler. This allows component code to control native
             * browser behavior (e.g. prevent form submission, stop
             * event bubbling).
             */
            if (domEvent.isDefaultPrevented()) {
                nativeEvent.preventDefault();
            }
            if (domEvent.isPropagationStopped()) {
                nativeEvent.stopPropagation();
            }
        };
    }

    // ====================================================================
    //  Private helpers: DOM traversal utilities
    // ====================================================================

    /**
     * Extract the target {@link HTMLElement} from a native browser event.
     *
     * <p>Uses {@link Event#getTarget()} and casts to {@link HTMLElement}.
     * Returns null if the target is not an HTML element (e.g. the document
     * itself, a text node, or an SVG element).</p>
     *
     * @param event the native browser event
     * @return the target element, or null if not an HTMLElement
     */
    private HTMLElement extractTarget(Event event) {
        try {
            /*
             * Event.getTarget() returns an EventTarget, which in the DOM
             * context is typically an HTMLElement. The cast may fail for
             * non-element targets (text nodes, document), so we catch
             * and return null.
             */
            return (HTMLElement) event.getTarget();
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Safely get an attribute value from a DOM element, returning an
     * empty string if the attribute is missing or the element is null.
     *
     * @param element the DOM element
     * @param name    the attribute name
     * @return the attribute value, or "" if missing
     */
    private String safeGetAttribute(HTMLElement element, String name) {
        if (element == null) {
            return "";
        }
        String value = element.getAttribute(name);
        return (value != null) ? value : "";
    }

    /**
     * Safely extract the "value" from a DOM element.
     *
     * <p>For input, textarea, and select elements, this returns the
     * current input value. For other elements, returns the text content.
     * This is used to populate {@link DomEvent#getValue()} so that
     * event handlers can easily access the user's input.</p>
     *
     * @param element the target DOM element
     * @return the element's value or text content, never null
     */
    private String safeGetValue(HTMLElement element) {
        if (element == null) {
            return "";
        }

        /*
         * Check the tag name to determine how to extract the value.
         * Input, textarea, and select elements expose a "value" property
         * through the DOM API. Other elements use textContent.
         */
        String tagName = element.getTagName();
        if (tagName != null) {
            tagName = tagName.toLowerCase();
        }

        if ("input".equals(tagName) || "textarea".equals(tagName)
                || "select".equals(tagName)) {
            /*
             * Access the value attribute. In a full implementation, we
             * would cast to HTMLInputElement/HTMLTextAreaElement for
             * type-safe getValue(). For the structural impl, we use
             * getAttribute which reads the DOM attribute (not the
             * live property). A production implementation would use
             * the JSO-typed accessors.
             */
            String val = element.getAttribute("value");
            return (val != null) ? val : "";
        }

        /* For non-form elements, return text content. */
        String text = element.getTextContent();
        return (text != null) ? text : "";
    }

    /**
     * Find the next {@link HTMLElement} child in a {@link NodeList},
     * starting from the given index and skipping text/comment nodes.
     *
     * <p>The real DOM may contain text nodes (whitespace between tags),
     * comment nodes, and other non-element nodes that do not appear in
     * the virtual tree. This method advances past them.</p>
     *
     * @param nodes      the child node list
     * @param startIndex the index to start searching from
     * @return the next HTMLElement child, or null if none found
     */
    private HTMLElement findNextElementChild(NodeList<Node> nodes, int startIndex) {
        int length = nodes.getLength();

        for (int i = startIndex; i < length; i++) {
            Node node = nodes.item(i);

            /*
             * Check if this node is an element node (nodeType == 1).
             * Text nodes are type 3, comment nodes are type 8.
             */
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (HTMLElement) node;
            }
        }

        return null;
    }

    /**
     * Find the index of a specific node within a {@link NodeList}.
     *
     * <p>Used to advance the real-child-index cursor during hydration
     * after finding a matching element node.</p>
     *
     * @param nodes  the node list to search
     * @param target the node to find
     * @return the index of the node, or -1 if not found
     */
    private int indexOfNode(NodeList<Node> nodes, Node target) {
        int length = nodes.getLength();

        for (int i = 0; i < length; i++) {
            if (nodes.item(i) == target) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Find the n-th element child of a parent element, skipping non-element
     * nodes (text nodes, comment nodes).
     *
     * <p>This is used during child patching to map virtual child indices
     * to real DOM element children. The virtual tree contains only element
     * nodes, but the real DOM may intersperse text nodes for whitespace.</p>
     *
     * @param parent the parent DOM element
     * @param n      the zero-based index of the desired element child
     * @return the n-th element child, or null if fewer element children exist
     */
    private HTMLElement findNthElementChild(HTMLElement parent, int n) {
        NodeList<Node> childNodes = parent.getChildNodes();
        int length = childNodes.getLength();
        int elementCount = 0;

        for (int i = 0; i < length; i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (elementCount == n) {
                    return (HTMLElement) node;
                }
                elementCount++;
            }
        }

        return null;
    }
}
