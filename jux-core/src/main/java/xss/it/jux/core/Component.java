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

package xss.it.jux.core;

/**
 * Base class for every JUX UI component -- widgets, layouts, cards, navbars.
 *
 * <p>A Component is the fundamental building block of a JUX application.
 * It produces an {@link Element} tree via {@link #render()} which is
 * serialized to HTML during server-side rendering (SSR) and mapped to
 * real DOM nodes on the client via {@code org.teavm.jso.dom.html.HTMLElement}.</p>
 *
 * <p>Components are <b>reusable UI pieces</b>: navbars, footers, cards,
 * modals, form fields, etc. For routable pages with metadata and request
 * access, extend {@link Page} instead.</p>
 *
 * <p><b>Lifecycle (SSR):</b></p>
 * <ol>
 *   <li>Spring instantiates the component (supports {@code @Autowired} injection)</li>
 *   <li>{@link #render()} called -- returns the Element tree</li>
 *   <li>Element tree serialized to HTML string</li>
 * </ol>
 *
 * <p><b>Lifecycle (Client, if {@code @JuxComponent(clientSide = true)}):</b></p>
 * <ol>
 *   <li>Hydrated from server-rendered HTML via {@code data-jux-id} attributes</li>
 *   <li>{@code @OnMount} method called after hydration</li>
 *   <li>{@code @State} changes trigger re-render + DOM diff/patch</li>
 *   <li>{@code @OnUnmount} method called on removal</li>
 * </ol>
 *
 * <p><b>Example -- a reusable component:</b></p>
 * <pre>{@code
 * public class Navbar extends Component {
 *     @Override
 *     public Element render() {
 *         return nav().cls("navbar").children(
 *             a().attr("href", "/").text("Home"),
 *             a().attr("href", "/about").text("About")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see Page
 * @see Element
 * @see Elements
 */
public abstract class Component {

    /**
     * Build the virtual DOM element tree for this component.
     *
     * <p>Called during SSR to generate HTML and during client-side re-renders
     * when {@code @State} fields change. Must return a single root Element.
     * Use {@link Elements} static factories to build the tree:
     * {@code div()}, {@code h1()}, {@code section()}, etc.</p>
     *
     * <p>This method should be <b>pure</b> -- given the same props and state,
     * it should produce the same tree. Side effects belong in {@link #onMount()}.</p>
     *
     * @return the root Element of this component's rendered output, never null
     */
    public abstract Element render();

    /**
     * Client-side lifecycle hook invoked after this component is hydrated.
     *
     * <p>Override this method to perform initialization that requires browser APIs:
     * setting up timers, opening WebSocket connections, drawing on canvas, fetching
     * data from REST endpoints, etc.</p>
     *
     * <p>This method is called once by the JUX client runtime after the component's
     * server-rendered HTML has been hydrated with event listeners and reactive state.
     * It is <b>not</b> called during server-side rendering.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @see #onUnmount()
     */
    public void onMount() {
        // No-op by default. Override to perform post-hydration initialization.
    }

    /**
     * Client-side lifecycle hook invoked before this component is removed from the DOM.
     *
     * <p>Override this method to perform cleanup: cancelling timers, closing
     * WebSocket connections, aborting in-flight HTTP requests, removing global
     * event listeners, etc.</p>
     *
     * <p>This method is called by the JUX client runtime when a component is
     * destroyed (e.g. during navigation or parent re-render). It is <b>not</b>
     * called during server-side rendering.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @see #onMount()
     */
    public void onUnmount() {
        // No-op by default. Override to perform pre-removal cleanup.
    }
}
