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

package xss.it.jux.cms.widget;

import xss.it.jux.core.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central registry of all available widget types in the CMS.
 *
 * <p>The widget registry is the single source of truth for what widget types
 * are available in the system. The framework registers built-in widgets at
 * startup (hero, text, image, gallery, form, CTA, pricing, etc.), and consumer
 * projects can register custom widgets via Spring beans.</p>
 *
 * <p>Each registered widget has:</p>
 * <ul>
 *   <li>A unique type key (e.g. {@code "hero"}, {@code "form"}, {@code "pricing"})</li>
 *   <li>The {@link Component} class that renders it</li>
 *   <li>A human-readable label and icon for the admin panel</li>
 *   <li>A {@link PropSchema} describing what props the widget accepts</li>
 *   <li>A {@link WidgetCategory} for grouping in the admin panel</li>
 * </ul>
 *
 * <p><b>Registering a custom widget in a consumer project:</b></p>
 * <pre>{@code
 * @Configuration
 * public class MyWidgets {
 *     @Bean
 *     public WidgetRegistration testimonialCarousel(WidgetRegistry registry) {
 *         return registry.register(
 *             "testimonial-carousel",
 *             TestimonialCarouselWidget.class,
 *             "Testimonial Carousel",
 *             "chat-bubbles",
 *             WidgetCategory.SOCIAL_PROOF,
 *             PropSchema.builder()
 *                 .prop("testimonials", PropType.LIST, "List of testimonials", true)
 *                 .prop("autoplay", PropType.BOOLEAN, "Auto-rotate slides", false)
 *                 .prop("interval", PropType.INT, "Rotation interval in ms", false)
 *                 .build()
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Thread safety:</b> Registration is expected to happen at startup during
 * Spring context initialization. After startup, the registry is effectively
 * read-only. The internal map uses {@link LinkedHashMap} to preserve insertion
 * order, which determines the display order within categories in the admin panel.</p>
 *
 * @see WidgetDefinition
 * @see WidgetCategory
 * @see PropSchema
 * @see xss.it.jux.cms.service.WidgetRenderer
 */
@org.springframework.stereotype.Component
public class WidgetRegistry {

    /**
     * Internal map of widget type key to widget definition.
     *
     * <p>Uses {@link LinkedHashMap} to preserve insertion order, which determines
     * the display order within each category in the admin panel's widget picker.
     * Built-in widgets are registered first, then consumer-defined widgets.</p>
     */
    private final Map<String, WidgetDefinition> widgets = new LinkedHashMap<>();

    /**
     * Register a new widget type in the registry.
     *
     * <p>Each widget type must have a unique key. If a widget with the same key
     * is already registered, it is silently replaced -- this allows consumer projects
     * to override built-in widgets with custom implementations.</p>
     *
     * <p>Registration is typically performed at application startup via Spring
     * {@code @Bean} methods in {@code @Configuration} classes. The returned
     * {@link WidgetRegistration} marker object can be used as a Spring bean
     * to ensure proper initialization ordering.</p>
     *
     * @param type           unique widget type key (e.g. {@code "hero"}, {@code "pricing-table"});
     *                       this key is stored in the database with each widget instance
     * @param componentClass the {@link Component} class that renders this widget;
     *                       must be a Spring-managed bean (annotated with {@code @JuxComponent}
     *                       or registered via {@code @Bean})
     * @param label          human-readable name shown in the admin panel widget picker
     * @param icon           icon identifier for the admin panel (e.g. {@code "image"},
     *                       {@code "text"}, {@code "layout"})
     * @param category       admin panel grouping category
     * @param schema         prop schema defining what properties this widget accepts
     * @return a {@link WidgetRegistration} marker bean for Spring wiring
     * @throws NullPointerException if any parameter is null
     */
    public WidgetRegistration register(String type,
                                       Class<? extends Component> componentClass,
                                       String label,
                                       String icon,
                                       WidgetCategory category,
                                       PropSchema schema) {
        /* Construct the immutable definition and store it keyed by type. */
        var definition = new WidgetDefinition(type, componentClass, label, icon, category, schema);
        widgets.put(type, definition);
        return new WidgetRegistration(type);
    }

    /**
     * Look up a widget definition by its type key.
     *
     * <p>Used by the {@link xss.it.jux.cms.service.WidgetRenderer} at render time
     * to resolve a {@link xss.it.jux.cms.model.WidgetInstance}'s type to its
     * Component class and prop schema.</p>
     *
     * @param type the widget type key to look up
     * @return the widget definition wrapped in an Optional, or empty if no widget
     *         with that type key is registered
     */
    public Optional<WidgetDefinition> get(String type) {
        return Optional.ofNullable(widgets.get(type));
    }

    /**
     * Get all registered widgets grouped by category.
     *
     * <p>Used by the admin panel to render the widget picker. The returned map
     * preserves the {@link WidgetCategory} enum order for consistent display.
     * Within each category, widgets appear in registration order.</p>
     *
     * @return an unmodifiable map of category to list of widget definitions;
     *         categories with no registered widgets are omitted
     */
    public Map<WidgetCategory, List<WidgetDefinition>> allByCategory() {
        /*
         * Group widgets by their category. The resulting map is ordered
         * by the natural order in which categories appear (determined by
         * the order widgets were registered and their categories).
         */
        return Collections.unmodifiableMap(
                widgets.values().stream()
                        .collect(Collectors.groupingBy(
                                WidgetDefinition::category,
                                LinkedHashMap::new,
                                Collectors.toUnmodifiableList()
                        ))
        );
    }

    /**
     * Get all registered widget type keys.
     *
     * <p>Returns the type keys in registration order. Useful for validation:
     * check whether a widget instance's type key corresponds to a known widget.</p>
     *
     * @return an unmodifiable list of all registered type keys
     */
    public List<String> allTypes() {
        return Collections.unmodifiableList(new ArrayList<>(widgets.keySet()));
    }

    /**
     * Marker record returned by {@link #register} for Spring bean wiring.
     *
     * <p>This record serves as a Spring bean handle so that consumer projects
     * can declare widget registrations as {@code @Bean} methods. It carries the
     * registered type key for identification but has no functional behavior.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * @Bean
     * public WidgetRegistration myWidget(WidgetRegistry registry) {
     *     return registry.register("my-widget", MyWidget.class, ...);
     * }
     * }</pre>
     *
     * @param type the widget type key that was registered
     */
    public record WidgetRegistration(

            /**
             * The widget type key that was registered.
             *
             * <p>Can be used to verify registration or to reference the
             * widget type programmatically.</p>
             */
            String type

    ) {}
}
