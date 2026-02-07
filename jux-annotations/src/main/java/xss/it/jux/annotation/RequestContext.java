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
 * Injects the {@code JuxRequestContext} into a component field, providing full
 * access to the HTTP request, response, session, and request metadata.
 *
 * <p>Use this annotation when you need more than what individual parameter
 * annotations ({@link PathParam}, {@link QueryParam}, {@link HeaderParam},
 * {@link CookieParam}) provide. The {@code JuxRequestContext} exposes the
 * complete HTTP request/response lifecycle, including:</p>
 * <ul>
 *   <li>Form data access (for POST requests)</li>
 *   <li>HTTP method detection ({@code isGet()}, {@code isPost()})</li>
 *   <li>Custom response headers and status codes</li>
 *   <li>Remote address and request metadata</li>
 *   <li>Direct access to the underlying {@code HttpServletRequest} and
 *       {@code HttpServletResponse}</li>
 * </ul>
 *
 * <p><b>Example -- form handling with GET and POST:</b></p>
 * <pre>{@code
 * @Route(value = "/contact", methods = {HttpMethod.GET, HttpMethod.POST})
 * @Title("Contact Us")
 * public class ContactPage extends Component {
 *
 *     @RequestContext private JuxRequestContext ctx;
 *
 *     @Override
 *     public Element render() {
 *         if (ctx.isPost()) {
 *             String name = ctx.formParam("name");
 *             String email = ctx.formParam("email");
 *             // process form...
 *             return div().cls("success").children(h1().text("Thank you!"));
 *         }
 *         return form().attr("method", "post").children(
 *             // form fields...
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example -- setting custom response status:</b></p>
 * <pre>{@code
 * @RequestContext private JuxRequestContext ctx;
 *
 * @Override
 * public Element render() {
 *     ctx.status(404);
 *     ctx.header("X-Custom", "value");
 *     String ip = ctx.remoteAddress();
 *     // ...
 * }
 * }</pre>
 *
 * @see Route
 * @see PathParam
 * @see QueryParam
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestContext {
}
