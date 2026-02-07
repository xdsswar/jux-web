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

package xss.it.jux.server.autoconfigure;

import xss.it.jux.a11y.JuxAccessibilityEngine;
import xss.it.jux.core.Component;
import xss.it.jux.core.routing.JuxRouter;
import xss.it.jux.i18n.*;
import xss.it.jux.server.cache.SsrCache;
import xss.it.jux.server.render.JuxRenderer;
import xss.it.jux.server.render.MetadataResolver;
import xss.it.jux.server.WebApplication;
import xss.it.jux.server.render.ResourceCollector;
import xss.it.jux.server.routing.*;
import xss.it.jux.server.security.RouteSecurityInterceptor;
import xss.it.jux.server.theme.JuxThemeController;
import xss.it.jux.server.theme.JuxThemeResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

/**
 * Spring Boot auto-configuration for the JUX web framework.
 *
 * <p>Automatically activates when {@code jux-server} is on the classpath,
 * detected by the presence of the {@link Component} class. This configuration
 * registers all framework beans required for server-side rendering, route
 * discovery, resource pipeline, accessibility auditing, security enforcement,
 * internationalization, and caching.</p>
 *
 * <p><b>Consumer projects do not need to configure any of these beans manually.</b>
 * Adding {@code jux-server} as a dependency is sufficient. All behaviour is
 * customizable via {@code application.yml} under the {@code jux.*} prefix,
 * as defined by {@link JuxProperties}.</p>
 *
 * <p><b>Bean registration order matters:</b> beans are declared in dependency
 * order so that Spring can wire them correctly. The route registrar scans
 * the classpath during bean creation, and the handler mapping compiles
 * route patterns immediately after.</p>
 *
 * @see JuxProperties
 * @see xss.it.jux.server.routing.JuxRouteHandler
 * @see xss.it.jux.server.render.JuxRenderer
 */
@AutoConfiguration
@ConditionalOnClass(Component.class)
@EnableConfigurationProperties(JuxProperties.class)
public class JuxAutoConfiguration {

    /**
     * Create the SSR renderer bean.
     *
     * <p>The renderer serializes {@link xss.it.jux.core.Element} trees and
     * {@link xss.it.jux.core.PageMeta} into complete HTML5 document strings,
     * including all head metadata, CSS/JS resources, and body content.</p>
     *
     * @return a new {@link JuxRenderer} instance
     */
    @Bean
    public JuxRenderer juxRenderer() {
        return new JuxRenderer();
    }

    /**
     * Create the CSS/JS resource collector bean.
     *
     * <p>Gathers {@code @Css} and {@code @Js} annotations from page components
     * and their layouts, merges them with resources from
     * {@link xss.it.jux.core.PageMeta}, deduplicates by path, and sorts
     * by declared order.</p>
     *
     * @return a new {@link ResourceCollector} instance
     */
    @Bean
    public ResourceCollector resourceCollector(ObjectProvider<WebApplication> webApp) {
        ResourceCollector collector = new ResourceCollector();
        webApp.ifAvailable(app -> collector.setApplicationClass(
            ClassUtils.getUserClass(app.getClass())
        ));
        return collector;
    }

    /**
     * Create the metadata resolver bean.
     *
     * <p>Merges annotation-declared metadata ({@code @Title}, {@code @Meta},
     * {@code @Canonical}, {@code @Favicon}) with the programmatic
     * {@link xss.it.jux.core.PageMeta} returned by a component's
     * {@code pageMeta()} method. Programmatic values override annotations.</p>
     *
     * @return a new {@link MetadataResolver} instance
     */
    @Bean
    public MetadataResolver metadataResolver(ObjectProvider<WebApplication> webApp) {
        MetadataResolver resolver = new MetadataResolver();
        webApp.ifAvailable(app -> {
            resolver.setApplicationClass(ClassUtils.getUserClass(app.getClass()));
            resolver.setWebApplication(app);
        });
        return resolver;
    }

    /**
     * Create the WCAG 2.2 AA accessibility audit engine bean.
     *
     * <p>Scans element trees for accessibility violations (missing alt text,
     * missing labels, contrast issues, etc.) and optionally auto-fixes safe
     * issues. Behaviour is controlled by {@link JuxProperties.A11y}.</p>
     *
     * @return a new {@link JuxAccessibilityEngine} instance
     */
    @Bean
    public JuxAccessibilityEngine juxAccessibilityEngine() {
        return new JuxAccessibilityEngine();
    }

    /**
     * Extract and expose the i18n configuration properties as a standalone bean.
     *
     * <p>This allows other beans (locale resolver, message registry) to depend
     * on {@link I18nProperties} directly without requiring the full
     * {@link JuxProperties} object.</p>
     *
     * @param juxProperties the root JUX configuration properties
     * @return the i18n properties subset
     */
    @Bean
    public I18nProperties i18nProperties(JuxProperties juxProperties) {
        return juxProperties.getI18n();
    }

    /**
     * Create the locale resolver bean.
     *
     * <p>Resolves the current request locale using a priority chain:
     * URL prefix, query parameter, cookie, session attribute,
     * {@code Accept-Language} header, and finally the configured default locale.</p>
     *
     * @param i18nProperties the i18n configuration (supported locales, cookie name, etc.)
     * @return a new {@link JuxLocaleResolver} instance
     */
    @Bean
    public JuxLocaleResolver juxLocaleResolver(I18nProperties i18nProperties) {
        return new JuxLocaleResolver(i18nProperties);
    }

    /**
     * Create the message bundle registry bean.
     *
     * <p>Discovers all {@code @MessageBundle} interfaces and their
     * {@code @MessageLocale} implementations on the classpath, and provides
     * locale-aware lookup of typed translation bundles at runtime.</p>
     *
     * @param i18nProperties the i18n configuration (supported locales, fallback strategy)
     * @return a new {@link MessageBundleRegistry} instance
     */
    @Bean
    public MessageBundleRegistry messageBundleRegistry(I18nProperties i18nProperties) {
        return new MessageBundleRegistry(i18nProperties);
    }

    /**
     * Create the messages service bean.
     *
     * <p>Provides the primary developer-facing i18n API: typed translation
     * bundle lookup, locale-aware date/number/currency formatting,
     * pluralization, and RTL detection.</p>
     *
     * @param registry       the message bundle registry for bundle lookup
     * @param i18nProperties the i18n configuration
     * @return a new {@link Messages} instance
     */
    @Bean
    public Messages messages(MessageBundleRegistry registry, I18nProperties i18nProperties) {
        return new Messages(registry, i18nProperties);
    }

    /**
     * Create the parameter injector bean.
     *
     * <p>Injects values from path variables, query parameters, headers,
     * cookies, session attributes, request context, and locale into
     * component fields annotated with {@code @PathParam}, {@code @QueryParam},
     * {@code @HeaderParam}, {@code @CookieParam}, {@code @SessionParam},
     * {@code @RequestContext}, and {@code @LocaleParam}.</p>
     *
     * @return a new {@link ParameterInjector} instance
     */
    @Bean
    public ParameterInjector parameterInjector() {
        return new ParameterInjector();
    }

    /**
     * Create the SSR HTML cache bean.
     *
     * <p>Backed by Caffeine, this cache stores rendered HTML strings keyed
     * by request path, query string, and locale. Configuration (enabled,
     * max size, TTL) is read from {@link JuxProperties.Ssr.Cache}.</p>
     *
     * @param juxProperties the root JUX configuration properties
     * @return a new {@link SsrCache} instance
     */
    @Bean
    public SsrCache ssrCache(JuxProperties juxProperties) {
        return new SsrCache(juxProperties.getSsr().getCache());
    }

    /**
     * Create the route security interceptor bean.
     *
     * <p>Checks {@code @Route(roles = ...)} against the current Spring Security
     * principal using the Servlet API's {@code isUserInRole()} method.
     * Public routes (empty roles array) pass through without authentication.</p>
     *
     * @return a new {@link RouteSecurityInterceptor} instance
     */
    @Bean
    public RouteSecurityInterceptor routeSecurityInterceptor() {
        return new RouteSecurityInterceptor();
    }

    /**
     * Create the route registrar bean and trigger classpath scanning.
     *
     * <p>Scans the configured base package for {@code @Route}-annotated
     * component classes, filters by active Spring profiles, creates
     * {@link RouteDefinition} records, registers localized route variants
     * for {@code @Localized} routes, and sorts all routes by priority.</p>
     *
     * @param juxProperties the root JUX configuration (provides base-package)
     * @param environment   the Spring environment (provides active profiles)
     * @return a new {@link JuxRouteRegistrar} with routes already scanned
     */
    @Bean
    public JuxRouteRegistrar juxRouteRegistrar(JuxProperties juxProperties, Environment environment) {
        JuxRouteRegistrar registrar = new JuxRouteRegistrar(juxProperties, environment);
        registrar.scan();
        return registrar;
    }

    /**
     * Create the theme resolver bean.
     *
     * <p>Resolves the active theme mode (e.g. "light", "dark") for each
     * request by reading the theme cookie. Falls back to the configured
     * default theme when no cookie is present.</p>
     *
     * @param juxProperties the root JUX configuration (provides theme settings)
     * @return a new {@link JuxThemeResolver} instance
     */
    @Bean
    public JuxThemeResolver juxThemeResolver(JuxProperties juxProperties) {
        return new JuxThemeResolver(juxProperties.getTheme());
    }

    /**
     * Create the theme switching REST controller bean.
     *
     * <p>Provides a {@code POST /api/theme?value=dark} endpoint that sets
     * the theme cookie without a page redirect. Client-side code calls this
     * endpoint and then swaps the {@code data-theme} attribute on
     * {@code <html>} for instant theme switching without state loss.</p>
     *
     * @param themeResolver the theme resolver for validation
     * @return a new {@link JuxThemeController} instance
     */
    @Bean
    public JuxThemeController juxThemeController(JuxThemeResolver themeResolver) {
        return new JuxThemeController(themeResolver);
    }

    /**
     * Create the route request handler bean.
     *
     * <p>This is the core request handler that orchestrates the full
     * request lifecycle: security check, locale resolution, theme resolution,
     * cache lookup, component instantiation via Spring DI, parameter injection,
     * metadata resolution, SSR rendering, accessibility auditing, and
     * response output.</p>
     *
     * @param springContext       the Spring application context for component instantiation
     * @param renderer            the SSR HTML renderer
     * @param metadataResolver    the annotation/programmatic metadata merger
     * @param parameterInjector   the route parameter injector
     * @param localeResolver      the i18n locale resolver
     * @param messages            the i18n messages service
     * @param cache               the SSR HTML cache
     * @param a11yEngine          the WCAG accessibility audit engine
     * @param securityInterceptor the route security checker
     * @param juxProperties       the root JUX configuration
     * @param themeResolver       the theme mode resolver
     * @return a new {@link JuxRouteHandler} instance
     */
    @Bean
    public JuxRouteHandler juxRouteHandler(ApplicationContext springContext, JuxRenderer renderer,
                                            MetadataResolver metadataResolver, ParameterInjector parameterInjector,
                                            JuxLocaleResolver localeResolver, Messages messages,
                                            SsrCache cache, JuxAccessibilityEngine a11yEngine,
                                            RouteSecurityInterceptor securityInterceptor,
                                            JuxProperties juxProperties, JuxThemeResolver themeResolver) {
        return new JuxRouteHandler(springContext, renderer, metadataResolver, parameterInjector,
            localeResolver, messages, cache, a11yEngine, securityInterceptor, juxProperties, themeResolver);
    }

    /**
     * Create the Spring MVC handler mapping bean for JUX routes.
     *
     * <p>Compiles all registered route patterns into regex {@link java.util.regex.Pattern}
     * objects and integrates with Spring MVC's {@code DispatcherServlet} to
     * match incoming HTTP requests to {@code @Route} components. This mapping
     * runs at order 100 (after standard Spring MVC controller mappings).</p>
     *
     * @param registrar the route registrar containing all discovered routes
     * @param handler   the route request handler to delegate matched requests to
     * @return a new {@link JuxRouteHandlerMapping} with compiled route patterns
     */
    @Bean
    public JuxRouteHandlerMapping juxRouteHandlerMapping(JuxRouteRegistrar registrar, JuxRouteHandler handler) {
        JuxRouteHandlerMapping mapping = new JuxRouteHandlerMapping(registrar, handler);
        mapping.initializeRoutes();
        return mapping;
    }

    /**
     * Create the reverse URL router bean.
     *
     * <p>Provides URL generation from route names or component classes,
     * with support for path variable substitution, query parameter appending,
     * and locale-prefixed URL generation for internationalized routes.</p>
     *
     * @param registrar     the route registrar for route definition lookup
     * @param juxProperties the root JUX configuration (provides default locale)
     * @return a new {@link JuxRouter} implementation
     */
    @Bean
    public JuxRouter juxRouter(JuxRouteRegistrar registrar, JuxProperties juxProperties) {
        return new JuxRouterImpl(registrar, juxProperties.getI18n().getDefaultLocale());
    }
}
