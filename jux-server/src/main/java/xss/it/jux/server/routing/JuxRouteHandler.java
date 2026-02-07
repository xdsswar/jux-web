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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import xss.it.jux.a11y.A11ySeverity;
import xss.it.jux.a11y.A11yViolation;
import xss.it.jux.a11y.JuxAccessibilityEngine;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxRequestContext;
import xss.it.jux.core.Page;
import xss.it.jux.core.PageMeta;
import xss.it.jux.i18n.Messages;
import xss.it.jux.server.autoconfigure.JuxProperties;
import xss.it.jux.server.cache.SsrCache;
import xss.it.jux.server.render.JuxRenderer;
import xss.it.jux.server.render.MetadataResolver;
import xss.it.jux.server.security.RouteSecurityInterceptor;
import xss.it.jux.server.theme.JuxThemeResolver;
import xss.it.jux.i18n.JuxLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Central request handler that orchestrates the full JUX page rendering lifecycle.
 *
 * <p>Implements Spring MVC's {@link Controller} interface so it can be returned
 * by {@link JuxRouteHandlerMapping} as the handler for matched JUX routes.
 * For each incoming request, this handler executes the following pipeline:</p>
 *
 * <ol>
 *   <li><b>Security:</b> Check {@code @Route(roles)} via {@link RouteSecurityInterceptor}.
 *       Returns 403 if the user lacks required roles.</li>
 *   <li><b>Locale resolution:</b> Determine the request locale via
 *       {@link JuxLocaleResolver} (URL prefix, cookie, header chain).</li>
 *   <li><b>Cache check:</b> If the route has {@code cacheTtl > 0}, look up
 *       the pre-rendered HTML in {@link SsrCache}. On cache hit, write the
 *       cached HTML directly and return.</li>
 *   <li><b>Component instantiation:</b> Obtain a Spring-managed instance of
 *       the route's component class via the {@link ApplicationContext}.</li>
 *   <li><b>Parameter injection:</b> Inject path variables, query params,
 *       headers, cookies, session, request context, and locale into the
 *       component's annotated fields via {@link ParameterInjector}.</li>
 *   <li><b>Metadata resolution:</b> Merge annotation-declared metadata with
 *       the component's programmatic {@code pageMeta()} via {@link MetadataResolver}.</li>
 *   <li><b>Redirect check:</b> If the resolved metadata requests a redirect,
 *       send the redirect response and return.</li>
 *   <li><b>SSR rendering:</b> Render the component and its metadata into a
 *       complete HTML5 document via {@link JuxRenderer}.</li>
 *   <li><b>Accessibility audit:</b> If enabled, audit the rendered element tree
 *       for WCAG 2.2 AA violations. Log violations and optionally fail on errors.</li>
 *   <li><b>Cache store:</b> If the route has {@code cacheTtl > 0}, store the
 *       rendered HTML in the cache for future requests.</li>
 *   <li><b>Response:</b> Write the HTML to the response with the appropriate
 *       HTTP status code, content type, and custom headers.</li>
 * </ol>
 *
 * <p><b>Thread safety:</b> This handler is a singleton bean shared across
 * all requests. It is stateless (all mutable state is request-scoped via
 * method parameters and thread-local locale) and safe for concurrent use.</p>
 *
 * @see JuxRouteHandlerMapping
 * @see JuxRenderer
 * @see ParameterInjector
 * @see MetadataResolver
 */
public class JuxRouteHandler implements Controller {

    /** Logger for request handling, accessibility violations, and error reporting. */
    private static final Logger log = LoggerFactory.getLogger(JuxRouteHandler.class);

    /** Spring application context for obtaining component bean instances via DI. */
    private final ApplicationContext springContext;

    /** SSR renderer that serializes element trees into HTML5 documents. */
    private final JuxRenderer renderer;

    /** Resolver that merges annotation metadata with programmatic PageMeta. */
    private final MetadataResolver metadataResolver;

    /** Injector for populating annotated component fields from request data. */
    private final ParameterInjector parameterInjector;

    /** Resolver for determining the current request's locale. */
    private final JuxLocaleResolver localeResolver;

    /** i18n messages service; locale is set per-request as a thread-local. */
    private final Messages messages;

    /** Caffeine-backed cache for rendered HTML pages. */
    private final SsrCache cache;

    /** WCAG 2.2 AA audit engine for checking rendered element trees. */
    private final JuxAccessibilityEngine a11yEngine;

    /** Security interceptor for checking route role requirements. */
    private final RouteSecurityInterceptor securityInterceptor;

    /** JUX configuration properties for accessibility and other settings. */
    private final JuxProperties properties;

    /** Theme resolver for determining the active theme mode from cookies. */
    private final JuxThemeResolver themeResolver;

    /**
     * Create a new route handler with all required dependencies.
     *
     * @param springContext       the Spring application context for component DI
     * @param renderer            the SSR HTML renderer
     * @param metadataResolver    the annotation/programmatic metadata merger
     * @param parameterInjector   the route parameter injector
     * @param localeResolver      the i18n locale resolver
     * @param messages            the i18n messages service
     * @param cache               the SSR HTML cache
     * @param a11yEngine          the WCAG accessibility audit engine
     * @param securityInterceptor the route security checker
     * @param properties          the JUX configuration properties
     * @param themeResolver       the theme mode resolver
     */
    public JuxRouteHandler(ApplicationContext springContext, JuxRenderer renderer,
                           MetadataResolver metadataResolver, ParameterInjector parameterInjector,
                           JuxLocaleResolver localeResolver, Messages messages,
                           SsrCache cache, JuxAccessibilityEngine a11yEngine,
                           RouteSecurityInterceptor securityInterceptor,
                           JuxProperties properties, JuxThemeResolver themeResolver) {
        this.springContext = springContext;
        this.renderer = renderer;
        this.metadataResolver = metadataResolver;
        this.parameterInjector = parameterInjector;
        this.localeResolver = localeResolver;
        this.messages = messages;
        this.cache = cache;
        this.a11yEngine = a11yEngine;
        this.securityInterceptor = securityInterceptor;
        this.properties = properties;
        this.themeResolver = themeResolver;
    }

    /**
     * Handle an incoming HTTP request matched to a JUX route.
     *
     * <p>Executes the full rendering pipeline: security check, locale resolution,
     * cache lookup, component instantiation, parameter injection, metadata
     * resolution, redirect check, SSR rendering, accessibility audit,
     * cache storage, and response output.</p>
     *
     * <p>Always returns {@code null} because the response is written directly
     * to the {@link HttpServletResponse} output stream (no Spring MVC view
     * resolution is needed).</p>
     *
     * @param request  the HTTP servlet request (contains route definition and path variables as attributes)
     * @param response the HTTP servlet response to write the rendered HTML to
     * @return always {@code null} (response is written directly)
     * @throws Exception if rendering fails due to an unexpected error
     */
    @Override
    @SuppressWarnings("unchecked")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Retrieve route metadata stored by JuxRouteHandlerMapping during path matching
        RouteDefinition routeDef = (RouteDefinition) request.getAttribute("jux.route");
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute("jux.pathVariables");

        // This should not happen in normal flow since the handler mapping only
        // delegates here on a successful match, but guard defensively.
        if (routeDef == null) {
            response.sendError(404);
            return null;
        }

        // Security check
        if (!securityInterceptor.checkAccess(request, routeDef.annotation())) {
            response.sendError(403);
            return null;
        }

        // Create request context
        JuxRequestContext ctx = new JuxRequestContextImpl(request, response);

        // Resolve locale using the priority chain: URL prefix > cookie > session > header > default.
        // The resolved locale is set as a thread-local on the Messages service so that
        // components can access translations for the current request's language.
        Locale locale = localeResolver.resolve(ctx);
        messages.setCurrentLocale(locale);

        // If ?lang= query param is present, set a locale cookie and redirect to the
        // clean URL (without ?lang=). This makes the ?lang= a one-time trigger that
        // persists the choice in a cookie. All subsequent navigation uses the cookie.
        String langParam = request.getParameter("lang");
        if (langParam != null && !langParam.isBlank()) {
            var i18nProps = properties.getI18n();
            Cookie langCookie = new Cookie(i18nProps.getCookieName(), langParam.strip());
            langCookie.setMaxAge(i18nProps.getCookieMaxAge());
            langCookie.setPath("/");
            response.addCookie(langCookie);

            // Redirect to the same path without ?lang= to produce a clean URL
            String redirectPath = request.getRequestURI();
            // Rebuild query string excluding the "lang" parameter
            StringBuilder remainingQuery = new StringBuilder();
            var parameterMap = request.getParameterMap();
            for (var entry : parameterMap.entrySet()) {
                if (!"lang".equals(entry.getKey())) {
                    for (String val : entry.getValue()) {
                        if (!remainingQuery.isEmpty()) remainingQuery.append('&');
                        remainingQuery.append(entry.getKey()).append('=').append(val);
                    }
                }
            }
            if (!remainingQuery.isEmpty()) {
                redirectPath += "?" + remainingQuery;
            }
            response.sendRedirect(redirectPath);
            messages.clearCurrentLocale();
            return null;
        }

        try {
            // Check cache
            if (routeDef.cacheTtl() > 0) {
                Optional<String> cached = cache.get(
                    request.getRequestURI(),
                    request.getQueryString(),
                    locale
                );
                if (cached.isPresent()) {
                    writeHtml(response, cached.get(), 200);
                    return null;
                }
            }

            // Instantiate component via Spring DI.
            // Try getBean() first (for @JuxComponent classes registered as beans),
            // then fall back to createBean() for @Route-only classes that aren't
            // registered in the application context but still need @Autowired support.
            Component component;
            try {
                component = springContext.getBean(routeDef.componentClass());
            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                component = (Component) springContext.getAutowireCapableBeanFactory()
                    .createBean(routeDef.componentClass());
            }

            // Initialize Page with request context, path params, and messages (before parameter injection)
            if (component instanceof Page page) {
                page.initRequest(ctx, pathVariables != null ? pathVariables : Map.of(), messages);
            }

            // Inject parameters
            parameterInjector.inject(component, pathVariables != null ? pathVariables : Map.of(), ctx, locale);

            // Get programmatic metadata (only Pages have pageMeta())
            PageMeta programmaticMeta = (component instanceof Page page) ? page.pageMeta() : null;

            // Resolve final metadata (annotations merged with programmatic)
            PageMeta finalMeta = metadataResolver.resolve(routeDef.componentClass(), programmaticMeta);

            // Auto-set htmlLang from the resolved locale if not explicitly set
            if (finalMeta.getHtmlLang() == null || finalMeta.getHtmlLang().isEmpty()) {
                finalMeta.htmlLang(locale.getLanguage());
            }

            // Resolve active theme from cookie and set data-theme attribute on <html>.
            // This allows CSS to target [data-theme="dark"] for theme-specific styling
            // without a page reload -- the client swaps the attribute directly.
            String resolvedTheme = themeResolver.resolve(request);
            finalMeta.htmlAttr("data-theme", resolvedTheme);

            // Check for redirect
            if (finalMeta.hasRedirect()) {
                response.setStatus(finalMeta.getRedirectStatus());
                response.setHeader("Location", finalMeta.getRedirectUrl());
                return null;
            }

            // Render the component
            String html = renderer.render(component, finalMeta);

            // Run WCAG 2.2 AA accessibility audit on the rendered element tree.
            // This is intentionally a separate render() call from the one used for HTML
            // generation, because the audit needs the raw Element tree (not the HTML string).
            // In production, both auditOnRender and enabled should be false for zero overhead.
            if (properties.getA11y().isEnabled() && properties.getA11y().isAuditOnRender()) {
                Element tree = component.render();
                List<A11yViolation> violations = a11yEngine.audit(tree);
                if (!violations.isEmpty() && properties.getA11y().isLogViolations()) {
                    for (A11yViolation v : violations) {
                        if (v.severity() == A11ySeverity.ERROR) {
                            log.error("A11y ERROR [{}] {}: {} at {} - {}", v.wcagCriterion(), v.rule(), v.message(), v.elementPath(), v.suggestion());
                        } else {
                            log.warn("A11y {} [{}] {}: {} at {} - {}", v.severity(), v.wcagCriterion(), v.rule(), v.message(), v.elementPath(), v.suggestion());
                        }
                    }
                    if (properties.getA11y().isFailOnError()) {
                        boolean hasError = violations.stream().anyMatch(v -> v.severity() == A11ySeverity.ERROR);
                        if (hasError) {
                            response.sendError(500, "Accessibility violations detected");
                            return null;
                        }
                    }
                }
            }

            // Use the status from PageMeta if explicitly set (e.g. 404 for not-found pages,
            // 410 for gone), otherwise default to 200 OK
            int status = finalMeta.getStatus() > 0 ? finalMeta.getStatus() : 200;

            // Set custom headers
            finalMeta.getHeaders().forEach(response::setHeader);

            // Cache if configured
            if (routeDef.cacheTtl() > 0) {
                cache.put(request.getRequestURI(), request.getQueryString(), locale, html, routeDef.cacheTtl());
            }

            writeHtml(response, html, status);

        } finally {
            // Clear the thread-local locale to prevent leaking request state
            // between requests in thread-pool-based servlet containers.
            messages.clearCurrentLocale();
        }

        return null;
    }

    /**
     * Write an HTML response with the given status code and content type.
     *
     * <p>Sets the content type to {@code text/html; charset=UTF-8}, writes
     * the HTML string to the response output stream, and flushes the writer
     * to ensure all bytes are sent to the client.</p>
     *
     * @param response the HTTP servlet response to write to
     * @param html     the complete HTML5 document string
     * @param status   the HTTP status code (e.g. 200, 404, 500)
     * @throws Exception if writing to the response output stream fails
     */
    private void writeHtml(HttpServletResponse response, String html, int status) throws Exception {
        response.setStatus(status);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }
}
