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
 * Injects an HTTP request header value into a component field.
 *
 * <p>The specified header name is looked up case-insensitively from the incoming
 * HTTP request. If the header is present, its value is injected into the annotated
 * field. If absent, the {@link #defaultValue()} is used. The field type should
 * typically be {@code String}.</p>
 *
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * @Route("/content")
 * public class ContentPage extends Component {
 *     @HeaderParam("Accept-Language") private String acceptLang;
 *     @HeaderParam("X-Forwarded-For") private String clientIp;
 *     @HeaderParam("User-Agent") private String userAgent;
 * }
 * }</pre>
 *
 * @see Route
 * @see PathParam
 * @see QueryParam
 * @see CookieParam
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HeaderParam {

    /**
     * The HTTP header name to extract from the request.
     *
     * <p>Header lookup is case-insensitive, following the HTTP specification.
     * Common examples: {@code "Accept-Language"}, {@code "X-Forwarded-For"},
     * {@code "User-Agent"}, {@code "Authorization"}.</p>
     *
     * @return the header name (case-insensitive)
     */
    String value();

    /**
     * Fallback value used when the header is absent from the request.
     *
     * <p>Defaults to an empty string. If the header is not present in the
     * incoming request, this value is injected into the field instead.</p>
     *
     * @return the default value string, or empty for no default
     */
    String defaultValue() default "";
}
