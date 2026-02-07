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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes the complete set of props that a widget type accepts.
 *
 * <p>Every widget type registered in the {@link WidgetRegistry} has an associated
 * {@code PropSchema} that declares what configurable properties the widget supports.
 * This schema serves three purposes:</p>
 *
 * <ol>
 *   <li><b>Admin panel form generation:</b> The CMS admin panel reads the schema
 *       to auto-generate form fields for each prop. Each {@link PropField}'s
 *       {@link PropType} determines the rendered form control (text input, image
 *       picker, color picker, toggle switch, etc.).</li>
 *   <li><b>Validation:</b> When a CMS editor saves a widget instance, the entered
 *       values are validated against the schema. Required fields must be filled,
 *       enum values must be in the allowed list, URLs must be valid, etc.</li>
 *   <li><b>Documentation:</b> The schema serves as a machine-readable contract
 *       describing what each prop does, making widgets self-documenting.</li>
 * </ol>
 *
 * <p><b>Usage — building a schema for a Hero widget:</b></p>
 * <pre>{@code
 * PropSchema schema = PropSchema.builder()
 *     .prop("heading", PropType.STRING, "Main heading text", true)
 *     .prop("subheading", PropType.STRING, "Supporting text below heading", false)
 *     .prop("ctaText", PropType.STRING, "Call-to-action button label", false)
 *     .prop("ctaUrl", PropType.URL, "CTA button link destination", false)
 *     .prop("alignment", PropType.ENUM, "Text alignment", false,
 *            List.of("left", "center", "right"))
 *     .prop("backgroundImage", PropType.IMAGE, "Background image", false)
 *     .prop("backgroundImageAlt", PropType.STRING, "Background image alt text (ADA)", false)
 *     .build();
 * }</pre>
 *
 * @param fields the ordered list of prop field definitions; the order determines
 *               the display order in the admin panel's editing form
 *
 * @see PropField
 * @see PropType
 * @see WidgetDefinition
 * @see WidgetRegistry
 */
public record PropSchema(

        /**
         * The ordered list of prop field definitions.
         *
         * <p>Fields are displayed in this order in the admin panel.
         * The list is unmodifiable after construction.</p>
         */
        List<PropField> fields

) {

    /**
     * Create a new {@link PropSchemaBuilder} for fluent schema construction.
     *
     * <p>The builder pattern allows incremental addition of prop fields
     * with a readable, chainable API.</p>
     *
     * @return a new empty builder instance
     */
    public static PropSchemaBuilder builder() {
        return new PropSchemaBuilder();
    }

    /**
     * Fluent builder for constructing {@link PropSchema} instances.
     *
     * <p>Provides convenience methods for adding prop fields with various
     * levels of detail. Fields are added in the order they are declared,
     * which determines their display order in the admin panel.</p>
     *
     * <p><b>Usage:</b></p>
     * <pre>{@code
     * PropSchema schema = PropSchema.builder()
     *     .prop("heading", PropType.STRING, "Main heading text", true)
     *     .prop("alignment", PropType.ENUM, "Text alignment", false,
     *            List.of("left", "center", "right"))
     *     .build();
     * }</pre>
     */
    public static final class PropSchemaBuilder {

        /** Accumulates prop fields in declaration order. */
        private final List<PropField> fields = new ArrayList<>();

        /**
         * Private constructor -- use {@link PropSchema#builder()} to create instances.
         */
        PropSchemaBuilder() {
            // Package-private constructor; instances created via PropSchema.builder()
        }

        /**
         * Add a prop field with the essential parameters.
         *
         * <p>Creates a {@link PropField} with null defaults for
         * {@code defaultValue}, empty {@code enumValues}, and null
         * {@code placeholder}. Use this for simple props that do not
         * need enum constraints or default values.</p>
         *
         * @param key         the prop key matching the {@code @Prop} field name
         * @param type        the data type determining the admin form control
         * @param description human-readable description for the admin panel
         * @param required    whether this prop must be filled
         * @return this builder for chaining
         */
        public PropSchemaBuilder prop(String key, PropType type, String description, boolean required) {
            fields.add(new PropField(key, type, description, required, null, List.of(), null));
            return this;
        }

        /**
         * Add a prop field with enum values.
         *
         * <p>Use this overload for {@link PropType#ENUM} props where the
         * allowed values must be constrained to a predefined list. The admin
         * panel renders these values as dropdown options.</p>
         *
         * @param key         the prop key matching the {@code @Prop} field name
         * @param type        the data type (typically {@link PropType#ENUM})
         * @param description human-readable description for the admin panel
         * @param required    whether this prop must be filled
         * @param enumValues  the list of allowed values for enum selection
         * @return this builder for chaining
         */
        public PropSchemaBuilder prop(String key, PropType type, String description,
                                      boolean required, List<String> enumValues) {
            fields.add(new PropField(key, type, description, required, null, enumValues, null));
            return this;
        }

        /**
         * Build the immutable {@link PropSchema} from the accumulated fields.
         *
         * <p>The returned schema contains an unmodifiable copy of the field list.
         * Further modifications to this builder do not affect the built schema.</p>
         *
         * @return a new immutable PropSchema containing all added fields
         */
        public PropSchema build() {
            /* Return an unmodifiable copy so the schema is truly immutable. */
            return new PropSchema(Collections.unmodifiableList(new ArrayList<>(fields)));
        }
    }
}
