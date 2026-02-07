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

/**
 * Immutable definition of a registered widget type.
 *
 * <p>A widget definition is the metadata record created when a widget type is
 * registered in the {@link WidgetRegistry}. It captures everything needed to
 * instantiate, configure, and display a widget in the CMS:</p>
 *
 * <ul>
 *   <li>The unique {@link #type} key used to reference this widget in
 *       {@link xss.it.jux.cms.model.WidgetInstance} records</li>
 *   <li>The {@link #componentClass} that renders the widget's UI</li>
 *   <li>Display metadata ({@link #label}, {@link #icon}, {@link #category})
 *       for the admin panel's widget picker</li>
 *   <li>The {@link #schema} defining what props the widget accepts, used for
 *       admin form generation and validation</li>
 * </ul>
 *
 * <p>Widget definitions are created once at registration time and never modified.
 * They are stored in the {@link WidgetRegistry}'s internal map, keyed by
 * {@link #type}.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * new WidgetDefinition(
 *     "hero",                    // type
 *     HeroWidget.class,          // componentClass
 *     "Hero Banner",             // label
 *     "image",                   // icon
 *     WidgetCategory.HERO,       // category
 *     PropSchema.builder()
 *         .prop("heading", PropType.STRING, "Main heading", true)
 *         .build()               // schema
 * )
 * }</pre>
 *
 * @param type           unique type key; matches the {@code type} field in
 *                       {@link xss.it.jux.cms.model.WidgetInstance}; must be
 *                       unique across all registered widgets; examples:
 *                       {@code "hero"}, {@code "pricing-table"}, {@code "faq"}
 * @param componentClass the {@link Component} class that renders this widget;
 *                       instantiated via Spring DI at render time, supporting
 *                       {@code @Autowired} injection of services and repositories
 * @param label          human-readable name displayed in the admin panel's widget
 *                       picker; should be short and descriptive (e.g. "Hero Banner",
 *                       "Pricing Table", "Contact Form")
 * @param icon           icon identifier for the admin panel's widget picker; maps
 *                       to the framework's built-in icon set (e.g. "image", "text",
 *                       "layout", "form", "chart")
 * @param category       admin panel grouping; widgets are organized by category
 *                       in the widget picker dialog
 * @param schema         the prop schema describing what configurable properties
 *                       this widget type accepts; used for admin form generation
 *                       and value validation
 *
 * @see WidgetRegistry
 * @see WidgetCategory
 * @see PropSchema
 */
public record WidgetDefinition(

        /**
         * Unique type key for this widget.
         *
         * <p>Used as the lookup key in the {@link WidgetRegistry} and stored
         * in the database as part of each {@link xss.it.jux.cms.model.WidgetInstance}.</p>
         */
        String type,

        /**
         * The Component class that renders this widget.
         *
         * <p>Must extend {@link Component} and have a no-arg constructor
         * (or be a Spring-managed bean). Instantiated via
         * {@link org.springframework.context.ApplicationContext#getBean(Class)}
         * at render time.</p>
         */
        Class<? extends Component> componentClass,

        /**
         * Human-readable label for the admin panel.
         *
         * <p>Displayed as the widget name in the widget picker, the widget
         * editor header, and the page outline view.</p>
         */
        String label,

        /**
         * Icon identifier for the admin panel.
         *
         * <p>Maps to an icon in the framework's built-in icon set.
         * Displayed alongside the {@link #label} in the widget picker.</p>
         */
        String icon,

        /**
         * Admin panel grouping category.
         *
         * <p>Determines which section of the widget picker this widget
         * appears in. Categories are displayed in the order defined by
         * the {@link WidgetCategory} enum.</p>
         */
        WidgetCategory category,

        /**
         * Prop schema defining the widget's configurable properties.
         *
         * <p>Used by the admin panel to auto-generate editing forms and
         * by the validation layer to ensure saved props match the schema.</p>
         */
        PropSchema schema

) {}
