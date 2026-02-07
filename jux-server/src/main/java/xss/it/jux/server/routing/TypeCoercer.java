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

package xss.it.jux.server.routing;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Utility class that converts string values extracted from URL path segments,
 * query parameters, headers, and cookies into strongly-typed Java objects
 * for injection into component fields.
 *
 * <p>Type coercion is the bridge between the stringly-typed world of HTTP
 * and Java's type system. When a route like {@code /users/{id:long}} is matched,
 * the captured string {@code "42"} must be converted to a {@code long} before
 * being injected into a field annotated with {@code @PathParam}.</p>
 *
 * <p><b>Supported target types:</b></p>
 * <table>
 *   <tr><th>Java Type</th><th>Example Input</th><th>Result</th></tr>
 *   <tr><td>{@code String}</td><td>"hello"</td><td>"hello"</td></tr>
 *   <tr><td>{@code int}/{@code Integer}</td><td>"42"</td><td>42</td></tr>
 *   <tr><td>{@code long}/{@code Long}</td><td>"1234567890"</td><td>1234567890L</td></tr>
 *   <tr><td>{@code double}/{@code Double}</td><td>"19.99"</td><td>19.99</td></tr>
 *   <tr><td>{@code float}/{@code Float}</td><td>"3.14"</td><td>3.14f</td></tr>
 *   <tr><td>{@code boolean}/{@code Boolean}</td><td>"true"</td><td>true</td></tr>
 *   <tr><td>{@code UUID}</td><td>"550e8400-e29b-..."</td><td>UUID instance</td></tr>
 *   <tr><td>{@code LocalDate}</td><td>"2026-02-06"</td><td>LocalDate instance</td></tr>
 *   <tr><td>Any {@code Enum}</td><td>"PENDING"</td><td>Enum constant</td></tr>
 *   <tr><td>{@code short}/{@code Short}</td><td>"100"</td><td>(short) 100</td></tr>
 *   <tr><td>{@code byte}/{@code Byte}</td><td>"7"</td><td>(byte) 7</td></tr>
 * </table>
 *
 * <p>This class is stateless, contains only static methods, and cannot be instantiated.</p>
 *
 * @see xss.it.jux.server.routing.ParameterInjector
 */
public final class TypeCoercer {

    /** Private constructor prevents instantiation of this utility class. */
    private TypeCoercer() {}

    /**
     * Coerce a string value to the specified target type.
     *
     * @param value      the string value to convert
     * @param targetType the target Java type
     * @return the converted value
     * @throws IllegalArgumentException if conversion fails
     */
    // SuppressWarnings for the unchecked cast in Enum.valueOf which is safe
    // because we verify targetType.isEnum() before calling it.
    @SuppressWarnings("unchecked")
    public static Object coerce(String value, Class<?> targetType) {
        if (value == null) return null;

        // Identity case: String to String requires no conversion
        if (targetType == String.class) {
            return value;
        }
        // Each branch handles both the primitive and its boxed wrapper type
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetType == UUID.class) {
            return UUID.fromString(value);
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value);
        }
        if (targetType.isEnum()) {
            // Safe unchecked cast: we verified isEnum() so the class is definitely an Enum subclass.
            // Enum.valueOf performs case-sensitive matching against the enum constant name.
            return Enum.valueOf((Class<Enum>) targetType, value);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(value);
        }

        throw new IllegalArgumentException(
            "Cannot coerce '" + value + "' to type " + targetType.getName());
    }

    /**
     * Get the JLS-defined default value for a primitive type.
     *
     * <p>Used when an optional parameter is missing and no explicit default
     * value is specified in the annotation. Primitive fields cannot be null,
     * so they receive the Java language specification default for their type.</p>
     *
     * <p>For reference types (non-primitives), returns {@code null}.</p>
     *
     * @param type the primitive type class (e.g. {@code int.class}, {@code boolean.class})
     * @return the default value for the type (e.g. {@code 0}, {@code false}, {@code 0L}),
     *         or {@code null} for reference types
     */
    public static Object defaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }
}
