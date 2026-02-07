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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring MVC {@link AbstractHandlerMapping} implementation that matches
 * incoming HTTP request paths against compiled {@code @Route} patterns
 * and delegates to {@link JuxRouteHandler}.
 *
 * <p>This handler mapping integrates JUX's route engine with Spring MVC's
 * {@code DispatcherServlet}. It converts each registered {@link RouteDefinition}
 * pattern into a compiled {@link Pattern regex}, then on every request,
 * iterates through patterns in priority order until a match is found.</p>
 *
 * <p><b>Matching process:</b></p>
 * <ol>
 *   <li>Iterate through compiled routes (pre-sorted by priority)</li>
 *   <li>Test the request URI against each regex pattern</li>
 *   <li>Verify the HTTP method is allowed for the matched route</li>
 *   <li>Extract path variables from regex capture groups</li>
 *   <li>Store the {@link RouteDefinition} and path variables as request attributes</li>
 *   <li>Return the {@link JuxRouteHandler} as the handler</li>
 * </ol>
 *
 * <p><b>Pattern compilation supports:</b></p>
 * <ul>
 *   <li>Static segments: {@code /about}</li>
 *   <li>Path variables: {@code /blog/{slug}} (matches one segment)</li>
 *   <li>Typed variables: {@code /users/{id:long}}, {@code /items/{id:uuid}},
 *       {@code /events/{date:date}}, {@code /active/{flag:bool}},
 *       {@code /price/{amount:double}}</li>
 *   <li>Regex variables: {@code /files/{path:regex(.+)}}</li>
 *   <li>Wildcards: {@code /docs/**} (matches any depth)</li>
 *   <li>Optional trailing slash</li>
 * </ul>
 *
 * <p>This mapping runs at order 100, which is after standard Spring MVC
 * controller mappings (typically order 0), ensuring that explicit
 * {@code @Controller} endpoints take precedence over JUX routes.</p>
 *
 * @see JuxRouteHandler
 * @see RouteDefinition
 * @see JuxRouteRegistrar
 */
public class JuxRouteHandlerMapping extends AbstractHandlerMapping {

    /** The route registrar containing all discovered route definitions. */
    private final JuxRouteRegistrar registrar;

    /** The route handler that processes matched requests. */
    private final JuxRouteHandler handler;

    /** Compiled route patterns, pre-sorted by priority for efficient matching. */
    private final List<CompiledRoute> compiledRoutes = new ArrayList<>();

    /**
     * Create a new handler mapping for JUX routes.
     *
     * <p>Sets the handler mapping order to 100, placing it after standard
     * Spring MVC controller mappings so that explicit {@code @Controller}
     * endpoints take precedence.</p>
     *
     * @param registrar the route registrar containing discovered routes
     * @param handler   the route handler to delegate matched requests to
     */
    public JuxRouteHandlerMapping(JuxRouteRegistrar registrar, JuxRouteHandler handler) {
        this.registrar = registrar;
        this.handler = handler;
        setOrder(100); // After standard Spring MVC mappings
    }

    /**
     * Compile all registered route patterns into regex {@link Pattern} objects.
     *
     * <p>Must be called once after route scanning is complete (typically during
     * bean initialization in {@link xss.it.jux.server.autoconfigure.JuxAutoConfiguration}).
     * Each {@link RouteDefinition} is paired with its compiled regex pattern
     * in a {@link CompiledRoute} record for efficient matching at request time.</p>
     */
    public void initializeRoutes() {
        for (RouteDefinition def : registrar.getRoutes()) {
            compiledRoutes.add(new CompiledRoute(def, compilePattern(def.pattern())));
        }
    }

    /**
     * Match the incoming request against compiled JUX route patterns.
     *
     * <p>Iterates through all compiled routes in priority order. For each route,
     * the request URI is tested against the regex pattern. If the pattern matches
     * and the HTTP method is allowed, the route definition and extracted path
     * variables are stored as request attributes and the handler is returned.</p>
     *
     * <p>Request attributes set on match:</p>
     * <ul>
     *   <li>{@code "jux.route"} - the matched {@link RouteDefinition}</li>
     *   <li>{@code "jux.pathVariables"} - a {@code Map<String, String>} of
     *       variable names to captured values</li>
     * </ul>
     *
     * @param request the incoming HTTP servlet request
     * @return the {@link JuxRouteHandler} if a route matches, or {@code null} if no match
     */
    @Override
    protected Object getHandlerInternal(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip paths that look like static resource requests (contain a file extension).
        // This allows Spring Boot's default ResourceHandlerMapping to serve files from
        // src/main/resources/static/ without the catch-all /** route intercepting them.
        int lastSlash = path.lastIndexOf('/');
        String lastSegment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        if (lastSegment.contains(".")) {
            return null;
        }

        // Routes are pre-sorted by priority — first match wins.
        // This means lower-priority-number routes (more specific) are checked
        // before higher-priority-number routes (catch-all / wildcard).
        for (CompiledRoute compiled : compiledRoutes) {
            Matcher matcher = compiled.pattern().matcher(path);
            if (matcher.matches()) {
                // Pattern matched; now verify the HTTP method is allowed.
                // A pattern match with the wrong method should NOT stop iteration,
                // as a different route pattern might match the same path with the correct method.
                RouteDefinition def = compiled.definition();
                boolean methodAllowed = false;
                for (var m : def.methods()) {
                    if (m.name().equalsIgnoreCase(method)) {
                        methodAllowed = true;
                        break;
                    }
                }
                if (!methodAllowed) continue;

                // Extract named path variables from the regex capture groups
                Map<String, String> pathVars = extractPathVariables(def.pattern(), matcher);

                // Store route metadata as request attributes for JuxRouteHandler to retrieve
                request.setAttribute("jux.route", def);
                request.setAttribute("jux.pathVariables", pathVars);
                return handler;
            }
        }
        // No route matched this request path + method combination
        return null;
    }

    /**
     * Compile a JUX route pattern string into a Java regex {@link Pattern}.
     *
     * <p>The compilation translates JUX route syntax into regex capture groups:</p>
     * <ul>
     *   <li>Static segments are quoted with {@link Pattern#quote(String)} for literal matching</li>
     *   <li>{@code {var}} becomes {@code ([^/]+)} - matches one path segment</li>
     *   <li>{@code {id:long}} and {@code {id:int}} become {@code (\d+)} - digits only</li>
     *   <li>{@code {id:uuid}} becomes {@code ([a-fA-F0-9\-]{36})} - UUID format</li>
     *   <li>{@code {date:date}} becomes {@code (\d{4}-\d{2}-\d{2})} - ISO date format</li>
     *   <li>{@code {flag:bool}} becomes {@code (true|false)}</li>
     *   <li>{@code {amount:double}} becomes {@code ([\d.]+)}</li>
     *   <li>{@code {path:regex(.+)}} extracts and uses the embedded regex</li>
     *   <li>{@code **} becomes {@code (.+)} - matches any depth</li>
     * </ul>
     *
     * <p>All patterns are anchored ({@code ^...$}) and allow an optional trailing slash.</p>
     *
     * @param routePattern the JUX route pattern string (e.g. {@code "/blog/{slug}"})
     * @return the compiled regex pattern
     */
    private Pattern compilePattern(String routePattern) {
        // Anchor the pattern at the start of the string
        StringBuilder regex = new StringBuilder("^");

        // Split by "/" and process each segment independently.
        // Leading "/" produces an empty first segment, which is skipped.
        String[] segments = routePattern.split("/");
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (seg.isEmpty()) continue;

            // Each non-empty segment is preceded by a literal "/"
            regex.append("/");

            if ("**".equals(seg)) {
                // Wildcard: match one or more characters across path boundaries
                regex.append("(.+)");
            } else if (seg.startsWith("{") && seg.endsWith("}")) {
                // Path variable: strip the braces and inspect the type hint
                String varDef = seg.substring(1, seg.length() - 1);
                if (varDef.contains(":regex(")) {
                    // Custom regex: extract the pattern between ":regex(" and the last ")"
                    int start = varDef.indexOf(":regex(") + 7;
                    int end = varDef.lastIndexOf(')');
                    String pattern = varDef.substring(start, end);
                    regex.append("(").append(pattern).append(")");
                } else if (varDef.contains(":long") || varDef.contains(":int")) {
                    // Numeric types: digits only
                    regex.append("(\\d+)");
                } else if (varDef.contains(":uuid")) {
                    // UUID: hex digits and hyphens, exactly 36 characters
                    regex.append("([a-fA-F0-9\\-]{36})");
                } else if (varDef.contains(":date")) {
                    // ISO 8601 date: YYYY-MM-DD
                    regex.append("(\\d{4}-\\d{2}-\\d{2})");
                } else if (varDef.contains(":bool")) {
                    // Boolean: literal "true" or "false"
                    regex.append("(true|false)");
                } else if (varDef.contains(":double")) {
                    // Floating-point number: digits and dots
                    regex.append("([\\d.]+)");
                } else {
                    // Plain variable with no type hint: match exactly one path segment
                    // (everything except "/")
                    regex.append("([^/]+)");
                }
            } else {
                // Static segment: quote it so special regex characters are treated literally
                regex.append(Pattern.quote(seg));
            }
        }

        // Allow an optional trailing slash so both "/about" and "/about/" match
        regex.append("/?$");
        return Pattern.compile(regex.toString());
    }

    /**
     * Extract named path variables from a matched regex against the original route pattern.
     *
     * <p>Walks the route pattern's segments in parallel with the regex matcher's
     * capture groups. For each variable segment ({@code {name}} or {@code {name:type}}),
     * the corresponding capture group value is extracted and stored in the result map.</p>
     *
     * <p>Wildcard segments ({@code **}) are stored under the key {@code "**"}.</p>
     *
     * @param routePattern the original JUX route pattern (e.g. {@code "/blog/{slug}"})
     * @param matcher      the regex matcher after a successful match against the request URI
     * @return a map of variable names to their captured string values, preserving insertion order
     */
    private Map<String, String> extractPathVariables(String routePattern, Matcher matcher) {
        Map<String, String> vars = new LinkedHashMap<>();
        String[] segments = routePattern.split("/");

        // Regex capture groups are 1-indexed; group 0 is always the entire match.
        // We advance groupIndex for every variable segment in the pattern,
        // keeping it in sync with the capture groups in the compiled regex.
        int groupIndex = 1;

        for (String seg : segments) {
            if (seg.isEmpty()) continue;

            if ("**".equals(seg)) {
                // Wildcard captures: stored under the literal key "**"
                vars.put("**", matcher.group(groupIndex++));
            } else if (seg.startsWith("{") && seg.endsWith("}")) {
                // Variable segment: extract name from "name" or "name:type"
                String varDef = seg.substring(1, seg.length() - 1);
                String varName = varDef.split(":")[0];
                if (groupIndex <= matcher.groupCount()) {
                    vars.put(varName, matcher.group(groupIndex++));
                }
            }
            // Static segments have no capture group, so groupIndex is NOT incremented
        }
        return vars;
    }

    /**
     * A pre-compiled route pairing a {@link RouteDefinition} with its regex {@link Pattern}.
     *
     * <p>Created once during {@link #initializeRoutes()} and reused for every
     * request match, avoiding repeated pattern compilation.</p>
     *
     * @param definition the route definition containing metadata and component class
     * @param pattern    the compiled regex pattern for URL matching
     */
    private record CompiledRoute(RouteDefinition definition, Pattern pattern) {}
}
