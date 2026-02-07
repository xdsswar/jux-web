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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xss.it.jux.core.JuxRequestContext;

import java.util.*;

/**
 * Servlet-based implementation of {@link JuxRequestContext} that wraps
 * Jakarta Servlet {@link HttpServletRequest} and {@link HttpServletResponse}.
 *
 * <p>This class is the bridge between JUX's framework-agnostic request
 * abstraction and the underlying Servlet container (Tomcat, Jetty, Undertow).
 * It is instantiated per-request by {@link JuxRouteHandler} and injected
 * into components annotated with {@code @RequestContext}.</p>
 *
 * <p><b>Thread safety:</b> This class is not thread-safe, but that is acceptable
 * because each instance is scoped to a single HTTP request and is never shared
 * between threads.</p>
 *
 * @see JuxRequestContext
 * @see JuxRouteHandler
 */
public class JuxRequestContextImpl implements JuxRequestContext {

    /** The underlying Jakarta Servlet request providing access to all HTTP request data. */
    private final HttpServletRequest request;

    /** The underlying Jakarta Servlet response for setting headers, status, and writing output. */
    private final HttpServletResponse response;

    /**
     * Create a new request context wrapping the given servlet request and response.
     *
     * @param request  the HTTP servlet request, never null
     * @param response the HTTP servlet response, never null
     */
    public JuxRequestContextImpl(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /** {@inheritDoc} */
    @Override
    public String method() {
        return request.getMethod();
    }

    /** {@inheritDoc} */
    @Override
    public String requestPath() {
        return request.getRequestURI();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reconstructs the full URL including the query string by combining
     * {@code getRequestURL()} and {@code getQueryString()} from the servlet request.</p>
     */
    @Override
    public String requestUrl() {
        String query = request.getQueryString();
        String url = request.getRequestURL().toString();
        return query != null ? url + "?" + query : url;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> header(String name) {
        return Optional.ofNullable(request.getHeader(name));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Iterates through the servlet request's cookie array to find a
     * cookie with the given name. Cookie name matching is case-sensitive
     * per the HTTP specification.</p>
     */
    @Override
    public Optional<String> cookie(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
            .filter(c -> c.getName().equals(name))
            .map(Cookie::getValue)
            .findFirst();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code getSession(false)} to avoid creating a new session
     * when one does not already exist.</p>
     */
    @Override
    public Optional<Object> session(String key) {
        var session = request.getSession(false);
        if (session == null) return Optional.empty();
        return Optional.ofNullable(session.getAttribute(key));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code getSession(true)} to create a new session if one
     * does not already exist, ensuring the attribute can always be stored.</p>
     */
    @Override
    public void session(String key, Object value) {
        request.getSession(true).setAttribute(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public String formParam(String name) {
        return request.getParameter(name);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String[]> formParams() {
        return request.getParameterMap();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> queryParam(String name) {
        return Optional.ofNullable(request.getParameter(name));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Checks the {@code X-Forwarded-For} header first to correctly identify
     * the client IP when the application is behind a reverse proxy or load balancer.
     * If the header contains multiple comma-separated addresses (proxy chain),
     * the first address (the original client) is returned. Falls back to
     * {@code getRemoteAddr()} when no forwarding header is present.</p>
     */
    @Override
    public String remoteAddress() {
        // Check X-Forwarded-For header for proxy/load-balancer scenarios
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // First IP in the chain is the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** {@inheritDoc} */
    @Override
    public Locale locale() {
        return request.getLocale();
    }

    /** {@inheritDoc} */
    @Override
    public void responseHeader(String name, String value) {
        response.setHeader(name, value);
    }

    /** {@inheritDoc} */
    @Override
    public void status(int code) {
        response.setStatus(code);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Performs a redirect by setting the HTTP status code and the
     * {@code Location} header. The caller is responsible for choosing
     * the appropriate status code (301 permanent, 302 temporary, 307, 308).</p>
     */
    @Override
    public void redirect(String url, int status) {
        response.setStatus(status);
        response.setHeader("Location", url);
    }

    /** Access the underlying HttpServletRequest. */
    public HttpServletRequest getRequest() {
        return request;
    }

    /** Access the underlying HttpServletResponse. */
    public HttpServletResponse getResponse() {
        return response;
    }
}
