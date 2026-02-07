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
 * Client-side lifecycle hook that runs before the component is removed from the DOM.
 *
 * <p>This annotation is only active on components marked with
 * {@code @JuxComponent(clientSide = true)}. The annotated method is called just
 * before the component's DOM nodes are removed from the document, giving the
 * component a chance to clean up resources acquired during its lifetime.</p>
 *
 * <p>Use this hook for:</p>
 * <ul>
 *   <li>Closing WebSocket connections</li>
 *   <li>Cancelling timers ({@code clearInterval}, {@code clearTimeout})</li>
 *   <li>Removing global event listeners (e.g., window resize, scroll)</li>
 *   <li>Releasing browser resources (media streams, IndexedDB connections)</li>
 *   <li>Aborting in-flight network requests</li>
 *   <li>Saving component state to localStorage or sessionStorage</li>
 * </ul>
 *
 * <p>The method must take no parameters and return {@code void}. Failing to
 * clean up resources (especially timers and global listeners) can cause memory
 * leaks and unexpected behavior.</p>
 *
 * <p><b>Example -- cleanup on removal:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class LiveFeed extends Component {
 *     private WebSocket websocket;
 *     private int timerId;
 *
 *     @OnMount
 *     public void onMount() {
 *         websocket = new WebSocket("wss://api.example.com/feed");
 *         timerId = Window.current().setInterval(() -> refresh(), 5000);
 *     }
 *
 *     @OnUnmount
 *     public void onUnmount() {
 *         if (websocket != null) websocket.close();
 *         if (timerId != 0) Window.current().clearInterval(timerId);
 *     }
 * }
 * }</pre>
 *
 * @see OnMount
 * @see JuxComponent
 * @see State
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnUnmount {
}
