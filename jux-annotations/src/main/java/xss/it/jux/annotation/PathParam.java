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
 * Injects a URL path variable into a component field.
 *
 * <p>The variable name must match a {@code {name}} placeholder in the
 * {@link Route#value()} pattern. Type coercion is performed automatically
 * based on the field type and the optional type hint in the route pattern.</p>
 *
 * <h2>Supported Type Coercions</h2>
 * <table>
 *   <tr><th>Field Type</th><th>Pattern</th><th>Example URL</th><th>Result</th></tr>
 *   <tr><td>{@code String}</td><td>{@code {slug}}</td><td>{@code /blog/hello}</td><td>{@code "hello"}</td></tr>
 *   <tr><td>{@code long}</td><td>{@code {id:long}}</td><td>{@code /users/42}</td><td>{@code 42L}</td></tr>
 *   <tr><td>{@code int}</td><td>{@code {page:int}}</td><td>{@code /list/3}</td><td>{@code 3}</td></tr>
 *   <tr><td>{@code UUID}</td><td>{@code {id:uuid}}</td><td>{@code /items/550e...}</td><td>UUID instance</td></tr>
 *   <tr><td>{@code LocalDate}</td><td>{@code {date:date}}</td><td>{@code /events/2026-02-06}</td><td>LocalDate instance</td></tr>
 *   <tr><td>{@code boolean}</td><td>{@code {active:bool}}</td><td>{@code /filter/true}</td><td>{@code true}</td></tr>
 *   <tr><td>{@code Enum}</td><td>{@code {status:enum(Status)}}</td><td>{@code /orders/PENDING}</td><td>enum constant</td></tr>
 *   <tr><td>{@code String}</td><td>{@code {path:regex(.+)}}</td><td>{@code /files/a/b.txt}</td><td>{@code "a/b.txt"}</td></tr>
 * </table>
 *
 * <p>For custom type conversions, implement {@code JuxTypeConverter<T>} and register
 * it as a Spring bean.</p>
 *
 * <p><b>Examples:</b></p>
 * <pre>{@code
 * @Route("/blog/{slug}")
 * public class BlogPage extends Component {
 *     @PathParam private String slug;          // slug = "hello-world"
 * }
 *
 * @Route("/users/{id:long}")
 * public class UserPage extends Component {
 *     @PathParam private long id;              // id = 42
 * }
 *
 * @Route("/docs/**")
 * public class DocsPage extends Component {
 *     @PathParam("**") private String path;    // path = "guide/getting-started"
 * }
 * }</pre>
 *
 * @see Route
 * @see QueryParam
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathParam {

    /**
     * The path variable name to bind to this field.
     *
     * <p>Must match a {@code {name}} placeholder in the {@link Route#value()} pattern.
     * If empty (the default), the annotated field's name is used as the variable name.
     * Use {@code "**"} to capture the remainder of a wildcard route.</p>
     *
     * @return the path variable name, or empty to use the field name
     */
    String value() default "";

    /**
     * Fallback value used when the path segment is missing.
     *
     * <p>Only applicable for optional path segments. The string value is coerced
     * to the field's type using the same coercion rules as the path variable itself.
     * An empty string (the default) is treated as "no default" unless the field type
     * is {@code String}.</p>
     *
     * @return the default value string, or empty for no default
     */
    String defaultValue() default "";

    /**
     * Whether the path segment is required.
     *
     * <p>When {@code true} (the default), a request URL that does not contain the
     * expected path segment results in a 404 Not Found response. Set to {@code false}
     * for optional path segments that should fall back to {@link #defaultValue()}.</p>
     *
     * @return {@code true} if a missing segment triggers a 404; defaults to {@code true}
     */
    boolean required() default true;
}
