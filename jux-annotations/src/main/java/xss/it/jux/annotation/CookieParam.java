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
 * Injects an HTTP cookie value into a component field.
 *
 * <p>The specified cookie name is looked up case-sensitively from the incoming
 * HTTP request's {@code Cookie} header. If the cookie is present, its value
 * is injected into the annotated field. If absent, the {@link #defaultValue()}
 * is used.</p>
 *
 * <p>This is useful for reading user preferences (e.g., theme, locale),
 * session identifiers, or tracking cookies. The field type should typically
 * be {@code String}.</p>
 *
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * @Route("/dashboard")
 * public class DashboardPage extends Component {
 *     @CookieParam("session-id") private String sessionId;
 *     @CookieParam("theme") private String theme;
 *     @CookieParam(value = "lang", defaultValue = "en") private String lang;
 * }
 * }</pre>
 *
 * @see Route
 * @see HeaderParam
 * @see SessionParam
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CookieParam {

    /**
     * The cookie name to extract from the request.
     *
     * <p>Cookie names are case-sensitive per the HTTP specification. The value
     * must match the cookie name exactly as set by the server or client.</p>
     *
     * @return the cookie name (case-sensitive)
     */
    String value();

    /**
     * Fallback value used when the cookie is absent from the request.
     *
     * <p>Defaults to an empty string. If the cookie is not present in the
     * incoming request, this value is injected into the field instead.</p>
     *
     * @return the default value string, or empty for no default
     */
    String defaultValue() default "";
}
