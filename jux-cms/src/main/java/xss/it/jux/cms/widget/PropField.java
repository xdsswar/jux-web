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

import java.util.List;

/**
 * A single prop field definition within a widget's {@link PropSchema}.
 *
 * <p>Each prop field describes one configurable property of a widget type.
 * The CMS admin panel uses this metadata to auto-generate the appropriate
 * form control (text input, color picker, image picker, etc.) and to validate
 * the entered values before saving to the database.</p>
 *
 * <p>At render time, the prop field's {@link #key} is used to look up the
 * value in the {@link xss.it.jux.cms.model.WidgetInstance#props()} map
 * and inject it into the corresponding {@code @Prop}-annotated field on
 * the widget's {@link xss.it.jux.core.Component} class.</p>
 *
 * <p><b>Example:</b> A hero widget's heading prop:</p>
 * <pre>{@code
 * new PropField(
 *     "heading",              // key — matches @Prop field name on HeroWidget
 *     PropType.STRING,        // type — renders a text input in admin
 *     "Main heading text",    // description — shown as label/tooltip
 *     true,                   // required — validation fails if empty
 *     null,                   // defaultValue — no default
 *     List.of(),              // enumValues — not applicable for STRING
 *     "Enter the hero heading" // placeholder — shown in empty input
 * )
 * }</pre>
 *
 * @param key          the prop key; must match the key in
 *                     {@link xss.it.jux.cms.model.WidgetInstance#props()} and the
 *                     {@code @Prop}-annotated field name (or {@code @Prop("key")})
 *                     on the widget Component class
 * @param type         the data type; determines the admin panel form control and
 *                     validation rules
 * @param description  human-readable description shown in the admin panel as a label
 *                     or tooltip; helps CMS editors understand what this prop controls
 * @param required     whether this prop must have a value; if {@code true}, the admin
 *                     panel prevents saving without a value and the widget renderer
 *                     may throw if the prop is missing
 * @param defaultValue default value used when the prop is not explicitly set in the
 *                     widget instance; {@code null} means no default (the prop is
 *                     either required or optional with no fallback)
 * @param enumValues   allowed values for {@link PropType#ENUM} type props; ignored for
 *                     other types; the admin panel renders these as dropdown options
 * @param placeholder  placeholder text shown in text/url inputs in the admin panel;
 *                     provides a hint about the expected format or content; {@code null}
 *                     means no placeholder
 *
 * @see PropType
 * @see PropSchema
 * @see xss.it.jux.annotation.Prop
 */
public record PropField(

        /**
         * The prop key.
         *
         * <p>This key is used to look up the value in the widget instance's
         * props map and to match it to the {@code @Prop}-annotated field
         * on the widget Component class.</p>
         */
        String key,

        /**
         * The data type of this prop.
         *
         * <p>Determines the form control rendered in the admin panel,
         * the validation rules applied, and the Java type used for
         * injection into the widget component.</p>
         */
        PropType type,

        /**
         * Human-readable description of this prop.
         *
         * <p>Displayed in the admin panel as a label, tooltip, or help text
         * to guide CMS editors.</p>
         */
        String description,

        /**
         * Whether this prop is required.
         *
         * <p>When {@code true}, the admin panel marks the field as required
         * and prevents saving without a value.</p>
         */
        boolean required,

        /**
         * Default value when the prop is not set.
         *
         * <p>Null means no default. The actual type of this object depends
         * on the {@link #type}: String for STRING/TEXT, Integer for INT,
         * Boolean for BOOLEAN, etc.</p>
         */
        Object defaultValue,

        /**
         * Allowed values for ENUM-type props.
         *
         * <p>Rendered as dropdown options in the admin panel. Empty or null
         * for non-ENUM types.</p>
         */
        List<String> enumValues,

        /**
         * Placeholder text for text-based inputs in the admin panel.
         *
         * <p>Shown inside the input field when it is empty. Provides a hint
         * about the expected value format.</p>
         */
        String placeholder

) {}
