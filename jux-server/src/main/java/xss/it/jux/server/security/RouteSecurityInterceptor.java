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

package xss.it.jux.server.security;

import xss.it.jux.annotation.Route;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;

/**
 * Security interceptor that enforces role-based access control on
 * {@code @Route}-annotated JUX components.
 *
 * <p>Checks the {@code roles} attribute of the {@code @Route} annotation
 * against the authenticated user's granted roles. Uses the standard
 * Servlet API's {@link HttpServletRequest#isUserInRole(String)} method,
 * which integrates seamlessly with Spring Security when it is on the
 * classpath.</p>
 *
 * <p><b>Access rules:</b></p>
 * <ul>
 *   <li>If {@code @Route(roles)} is empty: the route is public, no
 *       authentication required. Access is always granted.</li>
 *   <li>If roles are specified and no user is authenticated
 *       ({@code getUserPrincipal()} returns null): access is denied.</li>
 *   <li>If roles are specified and a user is authenticated: access is
 *       granted if the user has <b>at least one</b> of the required roles.</li>
 * </ul>
 *
 * <p>This interceptor implements {@link HandlerInterceptor} for potential
 * use in Spring MVC's interceptor chain, but its primary usage is direct
 * invocation from {@link xss.it.jux.server.routing.JuxRouteHandler}
 * via the {@link #checkAccess(HttpServletRequest, Route)} method.</p>
 *
 * @see xss.it.jux.annotation.Route#roles()
 * @see xss.it.jux.server.routing.JuxRouteHandler
 */
public class RouteSecurityInterceptor implements HandlerInterceptor {

    /**
     * Spring MVC pre-handle hook.
     *
     * <p>Always returns {@code true} because the actual security check is
     * performed by {@link #checkAccess(HttpServletRequest, Route)} during
     * route handling in {@link xss.it.jux.server.routing.JuxRouteHandler}.
     * This interceptor method exists for potential future use as a standard
     * Spring MVC interceptor in the chain.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Actual security enforcement happens in JuxRouteHandler via checkAccess().
        // This pass-through allows the interceptor to participate in the Spring MVC
        // interceptor chain without duplicating the access check.
        return true;
    }

    /**
     * Check if the current request has at least one of the required roles
     * for accessing a given route.
     *
     * <p>The check follows these steps:</p>
     * <ol>
     *   <li>If the route declares no required roles, access is always granted
     *       (the route is public).</li>
     *   <li>If roles are required but no user is authenticated
     *       ({@code getUserPrincipal()} is null), access is denied.</li>
     *   <li>If roles are required and a user is authenticated, the method
     *       checks each required role against the Servlet API's
     *       {@code isUserInRole()}, which delegates to Spring Security's
     *       {@code SecurityContext} when available. Access is granted if
     *       the user has <b>any one</b> of the required roles (OR semantics).</li>
     * </ol>
     *
     * @param request the HTTP servlet request containing the authenticated principal
     * @param route   the {@code @Route} annotation from the matched component class
     * @return {@code true} if access is allowed (public route or user has a required role),
     *         {@code false} if the user is unauthenticated or lacks all required roles
     */
    public boolean checkAccess(HttpServletRequest request, Route route) {
        String[] requiredRoles = route.roles();

        // No roles declared = public route, always accessible
        if (requiredRoles.length == 0) {
            return true;
        }

        // Roles required but no authenticated user
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return false;
        }

        // Check if the authenticated user has at least one of the required roles.
        // Uses the Servlet API's isUserInRole(), which Spring Security's
        // SecurityContextHolderAwareRequestFilter integrates with automatically.
        for (String role : requiredRoles) {
            if (request.isUserInRole(role)) {
                return true;
            }
        }

        // User is authenticated but lacks all required roles
        return false;
    }
}
