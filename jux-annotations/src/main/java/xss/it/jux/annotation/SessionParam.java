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
 * Injects an HTTP session attribute value into a component field.
 *
 * <p>The specified session attribute key is looked up from the current
 * {@code HttpSession}. If the attribute exists, its value is injected
 * into the annotated field. If absent, the field retains its default
 * (typically {@code null}).</p>
 *
 * <p>Session attributes are server-side state tied to a user's session
 * (identified by a session cookie). Use this for accessing shopping carts,
 * user profiles, wizard step data, or any per-session state stored
 * by other parts of the application.</p>
 *
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * @Route("/checkout")
 * public class CheckoutPage extends Component {
 *     @SessionParam("cart") private ShoppingCart cart;
 *     @SessionParam("user") private UserProfile user;
 * }
 * }</pre>
 *
 * @see Route
 * @see CookieParam
 * @see RequestContext
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionParam {

    /**
     * The session attribute key to look up.
     *
     * <p>This must match the key used when the attribute was stored in the
     * {@code HttpSession} (e.g., via {@code session.setAttribute("cart", cart)}).</p>
     *
     * @return the session attribute key
     */
    String value();
}
