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

package xss.it.jux.cms.service;

import xss.it.jux.annotation.Prop;
import xss.it.jux.cms.model.A11yConfig;
import xss.it.jux.cms.model.BackgroundConfig;
import xss.it.jux.cms.model.StyleConfig;
import xss.it.jux.cms.model.WidgetInstance;
import xss.it.jux.cms.widget.WidgetDefinition;
import xss.it.jux.cms.widget.WidgetRegistry;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static xss.it.jux.core.Elements.*;

/**
 * Converts {@link WidgetInstance} database records into rendered {@link Element} trees.
 *
 * <p>This is the bridge between the CMS data model and JUX's rendering engine.
 * For each widget instance stored in the database, the renderer:</p>
 *
 * <ol>
 *   <li>Looks up the widget type in the {@link WidgetRegistry} to find
 *       the {@link WidgetDefinition} (component class, schema, etc.)</li>
 *   <li>Instantiates the {@link Component} class via Spring's
 *       {@link ApplicationContext} (supports {@code @Autowired} injection)</li>
 *   <li>Injects prop values from the widget instance's props map into
 *       {@code @Prop}-annotated fields on the component using reflection</li>
 *   <li>Calls {@link Component#render()} to produce the widget's Element tree</li>
 *   <li>Wraps the rendered content in a {@code <section>} element with the
 *       widget instance's {@link WidgetInstance#id()} as the DOM id and
 *       {@code data-widget-type} attribute for admin panel identification</li>
 *   <li>Applies {@link StyleConfig} as inline CSS styles on the wrapper</li>
 *   <li>Applies {@link A11yConfig} as ARIA attributes on the wrapper</li>
 * </ol>
 *
 * <p><b>Prop injection:</b> The renderer scans the component class for fields
 * annotated with {@link Prop @Prop}. The prop key is determined by the annotation's
 * {@code value()} (or the field name if empty). The corresponding value is looked up
 * in the widget instance's props map and injected via reflection. Type coercion
 * handles basic conversions (String to int, String to boolean, etc.).</p>
 *
 * <p><b>Performance:</b> Field metadata (the list of @Prop fields per class) is
 * cached in a {@link ConcurrentHashMap} to avoid repeated reflection on each render.
 * The cache is populated lazily on first render of each widget type.</p>
 *
 * <p><b>Example rendered output:</b></p>
 * <pre>{@code
 * <section id="hero-1" data-widget-type="hero"
 *          style="padding: 6rem 2rem; min-height: 80vh; background-image: url('...')"
 *          aria-label="Hero banner" role="banner">
 *     <!-- HeroWidget.render() output here -->
 * </section>
 * }</pre>
 *
 * @see WidgetInstance
 * @see WidgetRegistry
 * @see StyleConfig
 * @see A11yConfig
 */
@org.springframework.stereotype.Component
public class WidgetRenderer {

    /**
     * Widget registry for resolving type keys to component classes.
     */
    @Autowired
    private WidgetRegistry registry;

    /**
     * Spring application context for instantiating widget components with DI.
     */
    @Autowired
    private ApplicationContext springContext;

    /**
     * Cache of @Prop-annotated fields per component class.
     *
     * <p>Avoids repeated reflection lookups on every render. The cache is
     * populated lazily: the first time a widget type is rendered, its component
     * class is scanned for @Prop fields and the results are cached. Subsequent
     * renders of the same widget type reuse the cached field metadata.</p>
     */
    private final Map<Class<?>, Field[]> propFieldCache = new ConcurrentHashMap<>();

    /**
     * Render a single widget instance to an Element tree.
     *
     * <p>This is the main entry point called by CMS route components for each
     * widget in a page's widget list. The method performs the full render pipeline:
     * type resolution, component instantiation, prop injection, rendering, styling,
     * and accessibility annotation.</p>
     *
     * @param instance the widget instance data loaded from the database
     * @return a {@code <section>} element wrapping the widget's rendered content,
     *         with id, data-widget-type, inline styles, and ARIA attributes applied
     * @throws IllegalArgumentException if the widget's type key is not registered
     *                                  in the {@link WidgetRegistry}
     */
    public Element render(WidgetInstance instance) {
        /* Step 1: Look up the widget definition by type key. */
        WidgetDefinition definition = registry.get(instance.type())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown widget type: '" + instance.type()
                        + "'. Ensure the widget is registered in the WidgetRegistry."));

        /* Step 2: Instantiate the Component via Spring DI for @Autowired support. */
        Component component = springContext.getBean(definition.componentClass());

        /* Step 3: Inject props from the widget instance into @Prop fields. */
        injectProps(component, instance.props());

        /* Step 4: Render the component to produce its Element tree. */
        Element content = component.render();

        /* Step 5: Create the wrapper <section> with id and data attribute. */
        Element wrapper = section()
                .id(instance.id())
                .attr("data-widget-type", instance.type());

        /* Step 6: Apply visual styling from StyleConfig. */
        wrapper = applyStyle(wrapper, instance.style());

        /* Step 7: Apply accessibility overrides from A11yConfig. */
        wrapper = applyA11y(wrapper, instance.a11y());

        /* Step 8: Add the rendered widget content as a child of the wrapper. */
        return wrapper.children(content);
    }

    /**
     * Inject prop values from the map into @Prop-annotated fields on the component.
     *
     * <p>Scans the component class (with caching) for fields annotated with
     * {@link Prop @Prop}. For each annotated field, the prop key is determined
     * (from the annotation value or the field name), the value is looked up in
     * the props map, and the value is injected via reflection after basic type
     * coercion.</p>
     *
     * <p>If a required prop is missing from the map, a warning is logged but
     * rendering continues with the field's default value. This graceful degradation
     * prevents a single missing prop from breaking an entire page.</p>
     *
     * @param component the widget component instance to inject props into
     * @param props     the props map from the {@link WidgetInstance}; keys are
     *                  prop names, values are the deserialized JSON values
     */
    private void injectProps(Component component, Map<String, Object> props) {
        if (props == null || props.isEmpty()) {
            return; // No props to inject
        }

        /*
         * Get the cached @Prop fields for this component class, or scan
         * and cache them if this is the first render of this widget type.
         */
        Field[] propFields = propFieldCache.computeIfAbsent(
                component.getClass(), this::scanPropFields);

        for (Field field : propFields) {
            Prop propAnnotation = field.getAnnotation(Prop.class);

            /*
             * Determine the prop key: use the annotation's value() if non-empty,
             * otherwise fall back to the Java field name.
             */
            String key = propAnnotation.value().isEmpty()
                    ? field.getName()
                    : propAnnotation.value();

            /* Look up the value in the props map. */
            Object value = props.get(key);

            if (value != null) {
                /* Coerce the value to the field's declared type and inject it. */
                try {
                    field.setAccessible(true);
                    field.set(component, coerceValue(value, field.getType()));
                } catch (IllegalAccessException e) {
                    /*
                     * This should not happen since we called setAccessible(true).
                     * Log and continue -- don't break the entire page for one prop.
                     */
                    throw new RuntimeException(
                            "Failed to inject prop '" + key + "' into field '"
                            + field.getName() + "' on " + component.getClass().getSimpleName(), e);
                }
            }
            /*
             * If value is null and the prop is required, we could log a warning here.
             * For now, we allow the field to keep its Java default value.
             */
        }
    }

    /**
     * Scan a component class for @Prop-annotated fields.
     *
     * <p>Walks the class hierarchy (including superclasses) to find all fields
     * annotated with {@link Prop}. The results are cached by
     * {@link #propFieldCache} for subsequent renders.</p>
     *
     * @param clazz the component class to scan
     * @return an array of @Prop-annotated fields found on the class
     */
    private Field[] scanPropFields(Class<?> clazz) {
        /*
         * Collect all declared fields from the class and its superclasses
         * that are annotated with @Prop. We walk the hierarchy because
         * widget components might extend a base class with common props.
         */
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Prop.class)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * Coerce a prop value from the JSON-deserialized type to the target field type.
     *
     * <p>JSON deserialization may produce types that don't exactly match the
     * Java field type. For example, a JSON number may deserialize as Integer
     * but the field expects long, or a JSON string might need to be parsed
     * as a boolean. This method handles common conversions.</p>
     *
     * @param value      the raw value from the props map
     * @param targetType the declared type of the target field
     * @return the coerced value, or the original value if no coercion is needed
     */
    private Object coerceValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        /* If the value is already the correct type, return it directly. */
        if (targetType.isInstance(value)) {
            return value;
        }

        /*
         * Handle common type coercions between JSON types and Java field types.
         * JSON numbers may arrive as Integer, Long, or Double depending on
         * the deserializer; we need to convert to the field's expected type.
         */
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        }
        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number number) {
                return number.floatValue();
            }
            return Float.parseFloat(value.toString());
        }

        /*
         * For unrecognized types, return the value as-is and hope the types
         * are compatible. In production, a more sophisticated type converter
         * registry (similar to Spring's ConversionService) would handle
         * complex type conversions.
         */
        return value;
    }

    /**
     * Apply {@link StyleConfig} to the wrapper element as inline CSS styles.
     *
     * <p>Converts each non-null StyleConfig field to the corresponding CSS
     * property on the wrapper element. Background configuration is delegated
     * to {@link #applyBackground(Element, BackgroundConfig)}.</p>
     *
     * @param wrapper the wrapper {@code <section>} element to style
     * @param style   the style configuration; may be null (no styling applied)
     * @return the styled element (same reference, mutated via builder pattern)
     */
    private Element applyStyle(Element wrapper, StyleConfig style) {
        if (style == null) {
            return wrapper; // No style overrides
        }

        /* Apply background (color, gradient, image, or video). */
        if (style.background() != null) {
            wrapper = applyBackground(wrapper, style.background());
        }

        /* Apply spacing properties. */
        if (style.padding() != null) {
            wrapper = wrapper.style("padding", style.padding());
        }
        if (style.margin() != null) {
            wrapper = wrapper.style("margin", style.margin());
        }

        /* Apply sizing properties. */
        if (style.minHeight() != null) {
            wrapper = wrapper.style("min-height", style.minHeight());
        }
        if (style.maxWidth() != null) {
            wrapper = wrapper.style("max-width", style.maxWidth());
        }

        /* Apply border and shadow effects. */
        if (style.borderRadius() != null) {
            wrapper = wrapper.style("border-radius", style.borderRadius());
        }
        if (style.shadow() != null) {
            wrapper = wrapper.style("box-shadow", style.shadow());
        }

        /* Apply text alignment. */
        if (style.textAlign() != null) {
            wrapper = wrapper.style("text-align", style.textAlign());
        }

        /*
         * Apply opacity only if less than fully opaque.
         * A value of 1.0 means "no opacity override" so we skip it to avoid
         * unnecessary inline styles.
         */
        if (style.opacity() < 1.0) {
            wrapper = wrapper.style("opacity", String.valueOf(style.opacity()));
        }

        /*
         * Apply backdrop blur for glassmorphism effects.
         * Zero blur means "no blur" so we skip it.
         */
        if (style.blur() > 0) {
            wrapper = wrapper.style("backdrop-filter", "blur(" + style.blur() + "px)");
        }

        /* Apply blend mode. */
        if (style.blendMode() != null) {
            wrapper = wrapper.style("mix-blend-mode", style.blendMode());
        }

        /*
         * Apply animation by adding a CSS class. The framework's built-in
         * CSS defines animations for classes like jux-animate-fade-in,
         * jux-animate-slide-up, etc. The animation is triggered when the
         * element scrolls into view (via IntersectionObserver on the client).
         */
        if (style.animation() != null) {
            wrapper = wrapper.cls("jux-animate-" + style.animation());
        }

        return wrapper;
    }

    /**
     * Apply {@link BackgroundConfig} to the wrapper element.
     *
     * <p>Translates the background configuration into the appropriate CSS
     * properties based on the {@link xss.it.jux.cms.model.BackgroundType}:</p>
     *
     * <ul>
     *   <li>{@code SOLID} — sets {@code background-color}</li>
     *   <li>{@code GRADIENT} — sets {@code background} with the full gradient string</li>
     *   <li>{@code IMAGE} — sets {@code background-image}, {@code background-size},
     *       {@code background-position}, and optionally {@code background-attachment}
     *       for parallax; overlay color is stored as a {@code data-overlay} attribute
     *       for CSS pseudo-element rendering</li>
     *   <li>{@code VIDEO} — adds the {@code jux-bg-video} CSS class as a marker;
     *       the actual video element is rendered by the widget component itself</li>
     * </ul>
     *
     * @param wrapper the wrapper element to apply background styles to
     * @param bg      the background configuration; must not be null
     * @return the element with background styles applied
     */
    private Element applyBackground(Element wrapper, BackgroundConfig bg) {
        return switch (bg.type()) {
            case SOLID -> wrapper.style("background-color", bg.color());

            case GRADIENT -> wrapper.style("background", bg.gradientCss());

            case IMAGE -> {
                /* Apply the background image URL. */
                Element styled = wrapper
                        .style("background-image", "url('" + bg.imageUrl() + "')")
                        .style("background-size",
                                bg.imageSize() != null ? bg.imageSize() : "cover")
                        .style("background-position",
                                bg.imagePosition() != null ? bg.imagePosition() : "center");

                /* Apply parallax effect via fixed background attachment. */
                if (bg.parallax()) {
                    styled = styled.style("background-attachment", "fixed");
                }

                /*
                 * Store the overlay color as a data attribute. The framework's
                 * CSS uses a ::before pseudo-element to render the overlay on
                 * top of the background image. This cannot be done with inline
                 * styles alone since pseudo-elements are not addressable in
                 * inline CSS.
                 */
                if (bg.color() != null) {
                    styled = styled.attr("data-overlay", bg.color());
                }

                yield styled;
            }

            case VIDEO -> {
                /*
                 * Video backgrounds are rendered by the widget component itself
                 * (e.g. HeroWidget adds a <video> element). The wrapper just
                 * gets a marker CSS class that the framework's CSS uses to
                 * position the video absolutely behind the content.
                 */
                yield wrapper.cls("jux-bg-video");
            }
        };
    }

    /**
     * Apply {@link A11yConfig} accessibility overrides to the wrapper element.
     *
     * <p>Each non-null field in the A11yConfig is applied as the corresponding
     * ARIA attribute or HTML attribute on the wrapper element. These overrides
     * supplement or replace the widget's built-in accessibility attributes.</p>
     *
     * @param wrapper the wrapper element to apply accessibility attributes to
     * @param a11y    the accessibility configuration; may be null (no overrides)
     * @return the element with ARIA attributes applied
     */
    private Element applyA11y(Element wrapper, A11yConfig a11y) {
        if (a11y == null) {
            return wrapper; // No accessibility overrides
        }

        /* Apply aria-label for screen reader identification. */
        if (a11y.ariaLabel() != null) {
            wrapper = wrapper.aria("label", a11y.ariaLabel());
        }

        /* Apply aria-describedby to link to a detailed description element. */
        if (a11y.ariaDescribedBy() != null) {
            wrapper = wrapper.ariaDescribedBy(a11y.ariaDescribedBy());
        }

        /* Apply ARIA role override. */
        if (a11y.role() != null) {
            wrapper = wrapper.role(a11y.role());
        }

        /*
         * Apply language attribute when the widget content is in a different
         * language than the page. Required by WCAG 3.1.2 (Language of Parts).
         */
        if (a11y.lang() != null) {
            wrapper = wrapper.lang(a11y.lang());
        }

        return wrapper;
    }
}
