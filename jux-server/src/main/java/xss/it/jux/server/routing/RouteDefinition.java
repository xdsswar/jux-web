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

import xss.it.jux.annotation.HttpMethod;
import xss.it.jux.annotation.Route;
import xss.it.jux.core.Component;

/**
 * Immutable record holding the parsed data from a discovered {@code @Route} annotation.
 *
 * <p>A {@code RouteDefinition} is created during classpath scanning by
 * {@link JuxRouteRegistrar} and is used throughout the request lifecycle:
 * by {@link JuxRouteHandlerMapping} for URL matching, by
 * {@link JuxRouteHandler} for security, caching, and rendering, and by
 * {@link JuxRouterImpl} for reverse URL generation.</p>
 *
 * <p>Instances are immutable and safe for concurrent access.</p>
 *
 * @param pattern        the URL path pattern from {@code @Route.value()} (e.g. {@code "/blog/{slug}"})
 * @param name           the route name for reverse URL generation; defaults to the class simple name
 * @param methods        the allowed HTTP methods (e.g. GET, POST)
 * @param priority       the route matching priority; lower numbers match first
 * @param cacheTtl       the SSR cache TTL in seconds; 0 means no caching
 * @param roles          the Spring Security roles required to access this route; empty means public
 * @param profiles       the Spring profiles that must be active for this route; empty means always active
 * @param componentClass the {@link Component} class that renders this route's page
 * @param layoutClass    the layout component class that wraps this page; {@code Void.class} means no layout
 * @param annotation     the original {@code @Route} annotation instance for access to any additional attributes
 *
 * @see xss.it.jux.annotation.Route
 * @see JuxRouteRegistrar
 */
public record RouteDefinition(
    String pattern,
    String name,
    HttpMethod[] methods,
    int priority,
    int cacheTtl,
    String[] roles,
    String[] profiles,
    Class<? extends Component> componentClass,
    Class<?> layoutClass,
    Route annotation
) {
    /**
     * Create a {@code RouteDefinition} from a {@code @Route}-annotated component class.
     *
     * <p>Extracts all relevant attributes from the {@code @Route} annotation and
     * derives the route name from the annotation's {@code name()} attribute,
     * falling back to the class simple name if the annotation name is empty.</p>
     *
     * @param clazz the class annotated with {@code @Route}
     * @return a new {@code RouteDefinition} populated from the annotation
     * @throws IllegalArgumentException if the class is not annotated with {@code @Route}
     */
    @SuppressWarnings("unchecked")
    public static RouteDefinition from(Class<?> clazz) {
        Route route = clazz.getAnnotation(Route.class);
        if (route == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Route");
        }
        String name = route.name().isEmpty() ? clazz.getSimpleName() : route.name();
        return new RouteDefinition(
            route.value(),
            name,
            route.methods(),
            route.priority(),
            route.cacheTtl(),
            route.roles(),
            route.profiles(),
            (Class<? extends Component>) clazz,
            route.layout(),
            route
        );
    }
}
