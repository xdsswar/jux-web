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

package xss.it.jux.annotation;

import java.lang.annotation.*;

/**
 * Binds a method to a DOM event on the client side.
 *
 * <p>This annotation is only active on components marked with
 * {@code @JuxComponent(clientSide = true)}. The annotated method is called
 * when the specified DOM event fires on the targeted element(s) within the
 * component's rendered DOM tree.</p>
 *
 * <p>During server-side rendering, event bindings are serialized as
 * {@code data-jux-event} attributes on the relevant elements. When the client-side
 * TeaVM runtime hydrates the component, it attaches real DOM event listeners
 * based on these attributes.</p>
 *
 * <p>The annotated method may accept a {@code DomEvent} parameter that provides
 * access to event details (target element, mouse position, key pressed, etc.).</p>
 *
 * <p><b>Example -- global click handler:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class ClickTracker extends Component {
 *
 *     @On("click")
 *     public void handleClick(DomEvent event) {
 *         // runs on client when any element in this component is clicked
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example -- targeted event handler:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class SearchBox extends Component {
 *
 *     @On(value = "input", target = "#search")
 *     public void handleSearch(DomEvent event) {
 *         // runs when the element with id="search" fires an input event
 *     }
 * }
 * }</pre>
 *
 * @see JuxComponent
 * @see State
 * @see OnMount
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface On {

    /**
     * The DOM event name to listen for.
     *
     * <p>Must be a valid DOM event name. Common values include:</p>
     * <ul>
     *   <li>{@code "click"} -- mouse click or touch tap</li>
     *   <li>{@code "input"} -- text input value change (fires on every keystroke)</li>
     *   <li>{@code "change"} -- form control value committed (fires on blur/select)</li>
     *   <li>{@code "submit"} -- form submission</li>
     *   <li>{@code "keydown"} / {@code "keyup"} -- keyboard key press/release</li>
     *   <li>{@code "focus"} / {@code "blur"} -- element focus gain/loss</li>
     *   <li>{@code "mouseover"} / {@code "mouseout"} -- mouse hover enter/exit</li>
     *   <li>{@code "scroll"} -- scroll event on the element</li>
     * </ul>
     *
     * @return the DOM event name
     */
    String value();

    /**
     * CSS selector for the target element(s) within this component.
     *
     * <p>When set, the event handler only fires for events originating from
     * elements matching this CSS selector (event delegation). When empty
     * (the default), the handler fires for events on any element within
     * the component's DOM tree.</p>
     *
     * <p>Examples: {@code "#search"}, {@code ".btn-primary"}, {@code "input[type=text]"},
     * {@code "form"}, {@code "button.submit"}.</p>
     *
     * @return the CSS selector, or empty for all elements in the component
     */
    String target() default "";
}
