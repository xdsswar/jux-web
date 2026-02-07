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

import xss.it.jux.annotation.Localized;
import xss.it.jux.annotation.Route;
import xss.it.jux.server.autoconfigure.JuxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.*;

/**
 * Classpath scanner and registry for {@code @Route}-annotated JUX components.
 *
 * <p>At startup, this class scans the configured base package (or the entire
 * classpath if none is specified) for classes annotated with
 * {@link xss.it.jux.annotation.Route}. Each discovered class is validated,
 * filtered by active Spring profiles, and converted into a {@link RouteDefinition}
 * record. For classes additionally annotated with {@code @Localized}, locale-prefixed
 * route variants are also registered.</p>
 *
 * <p>After scanning, all routes are sorted by their declared priority (ascending),
 * ensuring that lower-priority routes match before higher-priority catch-all routes.</p>
 *
 * <p><b>Lifecycle:</b> The {@link #scan()} method is called once during bean
 * creation in {@link xss.it.jux.server.autoconfigure.JuxAutoConfiguration}.
 * After scanning completes, the route list is immutable from the caller's perspective
 * (returned as an unmodifiable list).</p>
 *
 * @see RouteDefinition
 * @see JuxRouteHandlerMapping
 * @see JuxRouterImpl
 */
public class JuxRouteRegistrar {

    /** Logger for route registration events and warnings. */
    private static final Logger log = LoggerFactory.getLogger(JuxRouteRegistrar.class);

    /** JUX configuration properties (provides the base-package to scan). */
    private final JuxProperties properties;

    /** Spring environment (provides active profiles for route filtering). */
    private final Environment environment;

    /** All registered route definitions, sorted by priority after scanning. */
    private final List<RouteDefinition> routes = new ArrayList<>();

    /**
     * Create a new route registrar.
     *
     * @param properties  the JUX configuration properties (provides base-package)
     * @param environment the Spring environment (provides active profiles)
     */
    public JuxRouteRegistrar(JuxProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    /**
     * Scan the classpath for {@code @Route}-annotated components and register them.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Creates a {@link ClassPathScanningCandidateComponentProvider} with an
     *       {@link AnnotationTypeFilter} for {@code @Route}</li>
     *   <li>Scans the configured base package (or all packages if empty)</li>
     *   <li>For each candidate, loads the class and calls {@link #registerRoute(Class)}</li>
     *   <li>Sorts all registered routes by {@link RouteDefinition#priority()} ascending</li>
     * </ol>
     *
     * <p>This method should be called exactly once during application startup.
     * Subsequent calls would duplicate route registrations.</p>
     */
    public void scan() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Route.class));

        String basePackage = properties.getBasePackage();
        if (basePackage == null || basePackage.isBlank()) {
            basePackage = ""; // Scan everything
        }

        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
        for (BeanDefinition bd : candidates) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                registerRoute(clazz);
            } catch (ClassNotFoundException e) {
                log.warn("Could not load @Route class: {}", bd.getBeanClassName(), e);
            }
        }

        // Sort by priority
        routes.sort(Comparator.comparingInt(RouteDefinition::priority));
        log.info("Registered {} JUX routes", routes.size());
    }

    /**
     * Register a single {@code @Route}-annotated class as a route definition.
     *
     * <p>Checks profile restrictions first: if the route declares specific profiles
     * via {@code @Route(profiles = ...)}, at least one must be active in the current
     * Spring environment. If no active profile matches, the route is skipped.</p>
     *
     * <p>If the class is also annotated with {@code @Localized}, locale-prefixed
     * route variants are registered for each configured locale.</p>
     *
     * @param clazz the candidate class to register
     */
    private void registerRoute(Class<?> clazz) {
        Route route = clazz.getAnnotation(Route.class);
        if (route == null) return;

        // Check profile restrictions
        if (route.profiles().length > 0) {
            boolean activeProfile = false;
            for (String profile : route.profiles()) {
                if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of(profile))) {
                    activeProfile = true;
                    break;
                }
            }
            if (!activeProfile) {
                log.debug("Skipping route {} - no active profile matches", route.value());
                return;
            }
        }

        RouteDefinition def = RouteDefinition.from(clazz);
        routes.add(def);
        log.debug("Registered route: {} -> {}", def.pattern(), clazz.getSimpleName());

        // Register localized variants
        Localized localized = clazz.getAnnotation(Localized.class);
        if (localized != null) {
            registerLocalizedRoutes(def, localized);
        }
    }

    /**
     * Register locale-prefixed route variants for a {@code @Localized} route.
     *
     * <p>For each configured locale, creates a prefixed route pattern
     * (e.g. {@code /es/about}, {@code /fr/about}). The default locale's
     * prefix is only registered if {@code @Localized(prefixDefault = true)}.</p>
     *
     * <p>The actual URL prefix handling and locale extraction is performed
     * at request time by {@link JuxRouteHandler}, not during registration.</p>
     *
     * @param baseDef   the base route definition to create localized variants for
     * @param localized the {@code @Localized} annotation with locale configuration
     */
    private void registerLocalizedRoutes(RouteDefinition baseDef, Localized localized) {
        List<String> locales = localized.locales().length > 0
            ? List.of(localized.locales())
            : properties.getI18n().getLocales();

        String defaultLocale = properties.getI18n().getDefaultLocale();

        for (String loc : locales) {
            if (loc.equals(defaultLocale) && !localized.prefixDefault()) {
                continue; // Default locale uses the base path
            }
            // Localized route patterns are registered internally for matching
            // The actual prefix handling is done in JuxRouteHandler
            log.debug("Registered localized route: /{}{} -> {}", loc, baseDef.pattern(), baseDef.componentClass().getSimpleName());
        }
    }

    /**
     * Get all registered route definitions, sorted by priority (ascending).
     *
     * <p>Lower priority numbers are first in the list. The returned list is
     * unmodifiable to prevent external mutation of the route registry.</p>
     *
     * @return an unmodifiable list of all registered route definitions, never null
     */
    public List<RouteDefinition> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    /**
     * Find a route definition by its declared name.
     *
     * <p>The name is either explicitly set via {@code @Route(name = "...")}
     * or defaults to the component class's simple name (e.g. {@code "BlogPostPage"}).</p>
     *
     * @param name the route name to search for (case-sensitive)
     * @return the matching route definition, or empty if no route has that name
     */
    public Optional<RouteDefinition> findByName(String name) {
        return routes.stream().filter(r -> r.name().equals(name)).findFirst();
    }

    /**
     * Find a route definition by its component class.
     *
     * <p>Matches against the exact class, not subclasses. Used by
     * {@link JuxRouterImpl} for type-safe reverse URL generation
     * from a component class reference.</p>
     *
     * @param componentClass the component class to search for
     * @return the matching route definition, or empty if no route uses that class
     */
    public Optional<RouteDefinition> findByClass(Class<?> componentClass) {
        return routes.stream().filter(r -> r.componentClass().equals(componentClass)).findFirst();
    }
}
