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
 * Injects a URL query parameter into a component field.
 *
 * <p>For a URL like {@code /search?q=hello&page=2}, query parameters are extracted
 * by name and injected into the annotated field with automatic type coercion.
 * The same type coercion rules as {@link PathParam} apply (String, int, long, UUID,
 * LocalDate, boolean, double, Enum).</p>
 *
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * @Route("/search")
 * public class SearchPage extends Component {
 *     @QueryParam private String q;                          // "hello"
 *     @QueryParam(defaultValue = "1") private int page;      // 2
 *     @QueryParam(defaultValue = "20") private int size;     // 20 (absent, uses default)
 * }
 * }</pre>
 *
 * <p><b>Example -- required parameter:</b></p>
 * <pre>{@code
 * @Route("/search")
 * public class SearchPage extends Component {
 *     @QueryParam(required = true) private String q;   // 400 Bad Request if missing
 * }
 * }</pre>
 *
 * @see Route
 * @see PathParam
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryParam {

    /**
     * The query parameter name to bind to this field.
     *
     * <p>If empty (the default), the annotated field's name is used as the
     * query parameter name. For example, a field named {@code q} binds to
     * the {@code ?q=...} query parameter.</p>
     *
     * @return the query parameter name, or empty to use the field name
     */
    String value() default "";

    /**
     * Fallback value used when the query parameter is absent from the URL.
     *
     * <p>The string value is coerced to the field's type using the same coercion
     * rules as path variables. An empty string (the default) is treated as
     * "no default" unless the field type is {@code String}.</p>
     *
     * @return the default value string, or empty for no default
     */
    String defaultValue() default "";

    /**
     * Whether the query parameter is required.
     *
     * <p>When {@code true}, a request that does not include this query parameter
     * results in a 400 Bad Request response. When {@code false} (the default),
     * a missing parameter uses the {@link #defaultValue()} or the field type's
     * zero value.</p>
     *
     * @return {@code true} if a missing parameter triggers 400; defaults to {@code false}
     */
    boolean required() default false;
}
