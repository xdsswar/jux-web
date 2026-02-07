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

package xss.it.jux.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compile-time validator for {@code @Route} path patterns.
 *
 * <p>This utility class is used by {@link JuxAnnotationProcessor} during annotation
 * processing to validate route path syntax, extract path variable names, and verify
 * that declared {@code @PathParam} fields match the variables present in the route
 * pattern. It operates entirely on strings and sets -- no reflection or runtime
 * component access is needed.</p>
 *
 * <h3>Supported path syntax</h3>
 * <ul>
 *   <li>Static segments: {@code /about}, {@code /blog/archive}</li>
 *   <li>Path variables: {@code /blog/{slug}}</li>
 *   <li>Typed variables: {@code /users/{id:long}}, {@code /items/{id:uuid}}</li>
 *   <li>Regex variables: {@code /files/{path:regex(.+)}}</li>
 *   <li>Wildcards: {@code /docs/**}</li>
 * </ul>
 *
 * <h3>Supported variable types</h3>
 * <p>When a variable specifies an explicit type hint (e.g. {@code {id:long}}), the
 * type must be one of the recognized JUX type coercion targets:</p>
 * <ul>
 *   <li>{@code long}, {@code int}, {@code double}, {@code bool} -- primitives</li>
 *   <li>{@code uuid} -- {@code java.util.UUID}</li>
 *   <li>{@code date} -- {@code java.time.LocalDate}</li>
 *   <li>{@code enum(...)} -- an enum class reference</li>
 *   <li>{@code regex(...)} -- a custom regex capture</li>
 * </ul>
 *
 * <p>This class is stateless and all methods are static. It is not intended for
 * instantiation.</p>
 *
 * @see JuxAnnotationProcessor
 * @see xss.it.jux.annotation.Route
 * @see xss.it.jux.annotation.PathParam
 */
public final class RouteValidator {

    /**
     * Regex pattern that matches a single path variable expression inside braces.
     *
     * <p>Captures:</p>
     * <ul>
     *   <li>Group 1 -- the variable name (e.g. {@code "slug"}, {@code "id"})</li>
     *   <li>Group 2 -- the optional type specifier including the colon
     *       (e.g. {@code ":long"}, {@code ":regex(.+)"}), or {@code null} if absent</li>
     * </ul>
     *
     * <p>Examples that match:</p>
     * <pre>
     *   {slug}           -> name="slug", type=null
     *   {id:long}        -> name="id",   type=":long"
     *   {path:regex(.+)} -> name="path", type=":regex(.+)"
     * </pre>
     */
    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)(:[^}]+)?}");

    /**
     * Regex pattern that matches a segment containing only valid static characters.
     *
     * <p>Valid static segment characters are alphanumerics, hyphens, underscores,
     * dots, and tildes. An empty segment (caused by double slashes) will fail
     * this check.</p>
     */
    private static final Pattern STATIC_SEGMENT_PATTERN =
            Pattern.compile("[a-zA-Z0-9_.~-]+");

    /**
     * The set of recognized type hints that can appear after the colon in a
     * typed path variable (e.g. {@code {id:long}}).
     *
     * <p>The special prefixes {@code "regex("} and {@code "enum("} are checked
     * separately because they contain a parenthesized argument.</p>
     */
    private static final Set<String> SUPPORTED_SIMPLE_TYPES = Set.of(
            "long", "int", "double", "bool", "uuid", "date"
    );

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This is a pure utility class with only static methods.</p>
     */
    private RouteValidator() {
        // Utility class -- do not instantiate.
    }

    /**
     * Validates a route path pattern and returns a list of human-readable error messages.
     *
     * <p>An empty list indicates the path is valid. Each string in the returned list
     * describes a specific problem found in the path pattern. Multiple errors may be
     * returned for a single path.</p>
     *
     * <h4>Checks performed</h4>
     * <ol>
     *   <li>Path must not be null or blank.</li>
     *   <li>Path must start with {@code /}.</li>
     *   <li>Path must not contain consecutive slashes ({@code //}) except as part
     *       of a protocol (which is not valid in a route anyway).</li>
     *   <li>Path must not contain whitespace.</li>
     *   <li>Path must not end with {@code /} unless it is exactly {@code "/"}.</li>
     *   <li>Each segment that is not a variable must contain only valid static characters.</li>
     *   <li>Variable expressions must have valid names (start with letter or underscore,
     *       followed by alphanumerics or underscores).</li>
     *   <li>Variable type hints (if present) must be one of the supported types.</li>
     *   <li>Braces must be properly balanced -- no stray {@code {}} or {@code }}.</li>
     *   <li>The wildcard segment {@code **} must appear only as the last segment.</li>
     *   <li>Variable names must not be duplicated within the same path.</li>
     * </ol>
     *
     * @param path the route path pattern to validate, e.g. {@code "/blog/{slug}"}
     * @return an unmodifiable list of error messages; empty if the path is valid
     */
    public static List<String> validatePath(String path) {
        List<String> errors = new ArrayList<>();

        // -- Null/blank check --
        if (path == null || path.isBlank()) {
            errors.add("Route path must not be null or blank.");
            return Collections.unmodifiableList(errors);
        }

        // -- Must start with / --
        if (!path.startsWith("/")) {
            errors.add("Route path must start with '/'. Found: \"" + path + "\".");
        }

        // -- No whitespace --
        if (path.chars().anyMatch(Character::isWhitespace)) {
            errors.add("Route path must not contain whitespace. Found: \"" + path + "\".");
        }

        // -- No double slashes (except the leading one which is a single /) --
        if (path.contains("//")) {
            errors.add("Route path must not contain consecutive slashes '//'. Found: \"" + path + "\".");
        }

        // -- Must not end with / unless it is exactly "/" --
        if (path.length() > 1 && path.endsWith("/")) {
            errors.add("Route path must not end with '/' (unless it is the root path \"/\"). Found: \""
                    + path + "\".");
        }

        // -- Check for unbalanced braces --
        validateBraceBalance(path, errors);

        // -- If the path is just "/", it is the root route and is always valid --
        if ("/".equals(path)) {
            return Collections.unmodifiableList(errors);
        }

        // -- Split into segments and validate each one --
        // Remove the leading slash before splitting so we don't get an empty first element.
        String pathBody = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = pathBody.split("/", -1);

        // Track variable names to detect duplicates.
        Set<String> seenVariables = new LinkedHashSet<>();
        boolean wildcardSeen = false;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            // An empty segment would indicate a double-slash; already caught above.
            if (segment.isEmpty()) {
                continue;
            }

            // -- Wildcard segment: ** --
            if ("**".equals(segment)) {
                if (wildcardSeen) {
                    errors.add("Route path must not contain more than one '**' wildcard segment.");
                }
                wildcardSeen = true;
                if (i != segments.length - 1) {
                    errors.add("The '**' wildcard segment must be the last segment in the path. "
                            + "Found '**' at position " + (i + 1) + " of " + segments.length + ".");
                }
                continue;
            }

            // -- If wildcard was already seen, nothing should follow it --
            if (wildcardSeen) {
                errors.add("No segments are allowed after the '**' wildcard. Found: \"" + segment + "\".");
            }

            // -- Check if the segment is or contains a variable expression --
            if (segment.contains("{")) {
                validateVariableSegment(segment, seenVariables, errors);
            } else {
                // Pure static segment -- validate characters.
                if (!STATIC_SEGMENT_PATTERN.matcher(segment).matches()) {
                    errors.add("Static path segment contains invalid characters: \"" + segment
                            + "\". Only alphanumerics, hyphens, underscores, dots, and tildes are allowed.");
                }
            }
        }

        return Collections.unmodifiableList(errors);
    }

    /**
     * Extracts all path variable names from a route pattern.
     *
     * <p>For example, given the path {@code "/shop/{category}/{id:long}"}, this
     * method returns the set {@code {"category", "id"}}. For a wildcard path like
     * {@code "/docs/**"}, the special name {@code "**"} is included in the result.</p>
     *
     * <p>Variable names are returned in the order they appear in the path, using a
     * {@link LinkedHashSet} to preserve insertion order while preventing duplicates.</p>
     *
     * @param path the route path pattern to extract variable names from
     * @return an unmodifiable set of variable names found in the path; empty if none
     * @throws IllegalArgumentException if the path is null
     */
    public static Set<String> extractVariableNames(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Route path must not be null.");
        }

        Set<String> names = new LinkedHashSet<>();

        // Match all {name} and {name:type} expressions in the path.
        Matcher matcher = VARIABLE_PATTERN.matcher(path);
        while (matcher.find()) {
            // Group 1 is the variable name (without the type specifier).
            names.add(matcher.group(1));
        }

        // Check for the special wildcard segment **.
        // It can appear as the last segment: /docs/**
        if (path.endsWith("/**")) {
            names.add("**");
        }

        return Collections.unmodifiableSet(names);
    }

    /**
     * Validates that a set of {@code @PathParam} field names match the variable names
     * declared in a route path pattern.
     *
     * <p>This method performs two checks:</p>
     * <ol>
     *   <li><b>Unmatched params:</b> Any {@code @PathParam} name that does not correspond
     *       to a variable in the route pattern is reported as an error.</li>
     *   <li><b>Missing params:</b> Any required variable in the route pattern that has no
     *       corresponding {@code @PathParam} field is reported as a warning (not an error,
     *       because the variable might be injected via other means at runtime).</li>
     * </ol>
     *
     * <p>The wildcard variable {@code "**"} is treated specially: a {@code @PathParam("**")}
     * field is valid if the route path ends with {@code /**}.</p>
     *
     * @param routePath      the validated route path pattern
     * @param pathParamNames the set of {@code @PathParam} names declared on component fields;
     *                       for fields where {@code @PathParam.value()} is empty, the field
     *                       name itself should be used
     * @return an unmodifiable list of error messages; empty if all params match
     */
    public static List<String> validatePathParamsMatch(String routePath, Set<String> pathParamNames) {
        List<String> errors = new ArrayList<>();

        // Extract the variable names that are actually declared in the route pattern.
        Set<String> routeVariables = extractVariableNames(routePath);

        // Check for @PathParam names that don't appear in the route pattern.
        for (String paramName : pathParamNames) {
            if (!routeVariables.contains(paramName)) {
                errors.add("@PathParam(\"" + paramName + "\") does not match any path variable in "
                        + "route pattern \"" + routePath + "\". Declared variables: " + routeVariables + ".");
            }
        }

        // Check for route variables that have no corresponding @PathParam.
        // This is a softer check -- fields might be optional or handled by other injection mechanisms.
        for (String routeVar : routeVariables) {
            if (!pathParamNames.contains(routeVar)) {
                errors.add("Route variable \"{" + routeVar + "}\" in pattern \"" + routePath
                        + "\" has no corresponding @PathParam field. "
                        + "Ensure a field annotated with @PathParam exists for this variable.");
            }
        }

        return Collections.unmodifiableList(errors);
    }

    /**
     * Validates that a declared {@code @PathParam} field type is one of the supported
     * JUX type coercion targets.
     *
     * <p>The following fully-qualified type names are recognized:</p>
     * <ul>
     *   <li>{@code java.lang.String}</li>
     *   <li>{@code long} / {@code java.lang.Long}</li>
     *   <li>{@code int} / {@code java.lang.Integer}</li>
     *   <li>{@code double} / {@code java.lang.Double}</li>
     *   <li>{@code boolean} / {@code java.lang.Boolean}</li>
     *   <li>{@code java.util.UUID}</li>
     *   <li>{@code java.time.LocalDate}</li>
     * </ul>
     *
     * <p>Enum types are also supported but must be detected by the caller by checking
     * whether the type element is a subtype of {@code java.lang.Enum}.</p>
     *
     * @param qualifiedTypeName the fully-qualified type name of the {@code @PathParam} field
     * @return {@code true} if the type is a supported coercion target, {@code false} otherwise
     */
    public static boolean isSupportedParamType(String qualifiedTypeName) {
        if (qualifiedTypeName == null) {
            return false;
        }

        return switch (qualifiedTypeName) {
            case "java.lang.String",
                 "long", "java.lang.Long",
                 "int", "java.lang.Integer",
                 "double", "java.lang.Double",
                 "boolean", "java.lang.Boolean",
                 "java.util.UUID",
                 "java.time.LocalDate" -> true;
            default -> false;
        };
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Validates that braces in the path are properly balanced.
     *
     * <p>Every opening brace {@code {}} must have a corresponding closing brace
     * {@code }}, and they must not be nested. Errors are appended to the provided
     * list.</p>
     *
     * @param path   the route path to check
     * @param errors the list to append error messages to
     */
    private static void validateBraceBalance(String path, List<String> errors) {
        int depth = 0;

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '{') {
                depth++;
                if (depth > 1) {
                    errors.add("Nested braces are not allowed in route paths. "
                            + "Found nested '{' at position " + i + " in: \"" + path + "\".");
                    return; // Bail out -- further brace checking would be unreliable.
                }
            } else if (c == '}') {
                depth--;
                if (depth < 0) {
                    errors.add("Unmatched closing brace '}' at position " + i
                            + " in route path: \"" + path + "\".");
                    return;
                }
            }
        }

        if (depth != 0) {
            errors.add("Unmatched opening brace '{' in route path: \"" + path + "\".");
        }
    }

    /**
     * Validates a path segment that contains one or more variable expressions.
     *
     * <p>A variable segment may be an entire variable ({@code {slug}}) or a segment
     * that mixes static text with a variable ({@code prefix-{id}}). This method
     * extracts each variable from the segment, validates its name and optional type,
     * and checks for duplicate variable names.</p>
     *
     * @param segment       the segment string that contains at least one {@code {}}
     * @param seenVariables a mutable set tracking variable names already encountered
     *                      in previous segments; new names are added here
     * @param errors        the list to append error messages to
     */
    private static void validateVariableSegment(String segment, Set<String> seenVariables,
                                                 List<String> errors) {
        Matcher matcher = VARIABLE_PATTERN.matcher(segment);
        boolean foundVariable = false;

        while (matcher.find()) {
            foundVariable = true;
            String varName = matcher.group(1);
            String typeSpec = matcher.group(2); // Includes the leading colon, or null.

            // Check for duplicate variable names.
            if (!seenVariables.add(varName)) {
                errors.add("Duplicate path variable name \"{" + varName + "}\" in route pattern.");
            }

            // Validate the type specifier if present.
            if (typeSpec != null) {
                validateVariableType(varName, typeSpec, errors);
            }
        }

        // If the segment contains a '{' but the regex didn't match, the syntax is malformed.
        if (!foundVariable && segment.contains("{")) {
            errors.add("Malformed variable expression in path segment: \"" + segment
                    + "\". Variables must follow the pattern {name} or {name:type}.");
        }
    }

    /**
     * Validates the type specifier portion of a path variable.
     *
     * <p>The {@code typeSpec} parameter includes the leading colon, e.g. {@code ":long"},
     * {@code ":regex(.+)"}, or {@code ":enum(Status)"}.</p>
     *
     * <p>Recognized types:</p>
     * <ul>
     *   <li>Simple types: {@code long}, {@code int}, {@code double}, {@code bool},
     *       {@code uuid}, {@code date}</li>
     *   <li>Parameterized types: {@code regex(...)}, {@code enum(...)}</li>
     * </ul>
     *
     * @param varName  the variable name (for error messages)
     * @param typeSpec the type specifier including the leading colon
     * @param errors   the list to append error messages to
     */
    private static void validateVariableType(String varName, String typeSpec, List<String> errors) {
        // Strip the leading colon to get the raw type string.
        String type = typeSpec.substring(1);

        // Check for parameterized types first: regex(...) and enum(...)
        if (type.startsWith("regex(") && type.endsWith(")")) {
            // The regex body is between the parentheses. We don't validate the regex itself
            // at compile time because the full Java regex engine isn't available in all
            // annotation processing environments, and complex patterns may appear valid
            // only in specific contexts.
            String regexBody = type.substring(6, type.length() - 1);
            if (regexBody.isEmpty()) {
                errors.add("Path variable \"{" + varName + "}\" has an empty regex pattern. "
                        + "Provide a non-empty pattern inside regex(...).");
            }
            return;
        }

        if (type.startsWith("enum(") && type.endsWith(")")) {
            // The enum class name is between the parentheses.
            String enumClassName = type.substring(5, type.length() - 1);
            if (enumClassName.isBlank()) {
                errors.add("Path variable \"{" + varName + "}\" has an empty enum type. "
                        + "Provide a class name inside enum(...).");
            }
            return;
        }

        // Check against the set of recognized simple type names.
        if (!SUPPORTED_SIMPLE_TYPES.contains(type)) {
            errors.add("Path variable \"{" + varName + "}\" has unsupported type hint: \"" + type
                    + "\". Supported types: " + SUPPORTED_SIMPLE_TYPES
                    + ", regex(...), enum(...).");
        }
    }
}
