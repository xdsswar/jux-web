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
 * Functional interface for DOM event handlers in the JUX component model.
 *
 * <p>Event handlers are registered on {@link Element} nodes via
 * {@link Element#on(String, EventHandler)} and are invoked when the
 * corresponding DOM event fires on the client. During SSR, handlers are
 * serialized as {@code data-jux-event} attributes so the client-side
 * hydration layer ({@code JuxDomBridge}) can reattach them to real
 * {@code org.teavm.jso.dom.html.HTMLElement} nodes.</p>
 *
 * <p>Because this is a {@link FunctionalInterface}, it can be used with
 * lambda expressions or method references:</p>
 * <pre>{@code
 * button().text("Click me").on("click", e -> handleClick(e))
 * input().on("input", this::onInput)
 * }</pre>
 *
 * <p><b>Note:</b> Event handlers are only active on client-side components
 * annotated with {@code @JuxComponent(clientSide = true)}. Server-only
 * components ignore event handlers during rendering.</p>
 *
 * @see DomEvent
 * @see Element#on(String, EventHandler)
 */
@FunctionalInterface
public interface EventHandler {

    /**
     * Handle a DOM event.
     *
     * <p>Called by the client-side hydration layer when the registered
     * DOM event fires. The provided {@link DomEvent} contains the event
     * type, target element ID, input value (for form elements), keyboard
     * modifiers, and mouse coordinates.</p>
     *
     * <p>Call {@link DomEvent#preventDefault()} to suppress the browser's
     * default behavior, or {@link DomEvent#stopPropagation()} to prevent
     * the event from bubbling up the DOM tree.</p>
     *
     * @param event the event data wrapper, never null
     */
    void handle(DomEvent event);
}
