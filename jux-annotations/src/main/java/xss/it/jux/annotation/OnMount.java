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
 * Client-side lifecycle hook that runs once after the component's server-rendered
 * HTML has been hydrated with event listeners and state on the client.
 *
 * <p>This annotation is only active on components marked with
 * {@code @JuxComponent(clientSide = true)}. The annotated method is called after
 * the TeaVM runtime has fully hydrated the component -- meaning the DOM is ready,
 * event listeners are attached, and {@link State} fields are initialized.</p>
 *
 * <p>Use this hook for:</p>
 * <ul>
 *   <li>Initializing third-party JavaScript libraries</li>
 *   <li>Setting up WebSocket connections</li>
 *   <li>Starting animations or timers</li>
 *   <li>Accessing browser-specific APIs (localStorage, geolocation, etc.)</li>
 *   <li>Drawing on {@code <canvas>} elements</li>
 *   <li>Performing initial data fetches</li>
 * </ul>
 *
 * <p>The method has full access to the real DOM via the TeaVM JSO APIs
 * ({@code org.teavm.jso.dom.html.*}). The method must take no parameters
 * and return {@code void}.</p>
 *
 * <p><b>Example -- canvas initialization:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class ChartWidget extends Component {
 *
 *     @Override
 *     public Element render() {
 *         return canvas().id("myChart").attr("width", "400").attr("height", "200")
 *             .aria("label", "Monthly data chart").role("img");
 *     }
 *
 *     @OnMount
 *     public void onMount() {
 *         HTMLDocument doc = Window.current().getDocument();
 *         HTMLCanvasElement canvas = (HTMLCanvasElement) doc.getElementById("myChart");
 *         CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
 *         ctx.setFillStyle("#3b82f6");
 *         ctx.fillRect(10, 10, 100, 50);
 *     }
 * }
 * }</pre>
 *
 * @see OnUnmount
 * @see JuxComponent
 * @see State
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnMount {
}
