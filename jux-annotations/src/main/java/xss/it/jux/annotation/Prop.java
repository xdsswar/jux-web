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

package xss.it.jux.annotation;

import java.lang.annotation.*;

/**
 * Injects a property value into a component field from a parent component
 * or from the CMS widget instance's props map.
 *
 * <p>Props are the primary mechanism for passing data from parent components to
 * child components. When a component is embedded as a child (via
 * {@code Element.child(component)}), the parent can set prop values that are
 * injected into fields annotated with {@code @Prop}. In CMS mode, props come
 * from the {@code WidgetInstance}'s serialized props map stored in the database.</p>
 *
 * <p>The framework performs automatic type coercion from the source value to the
 * field type, supporting strings, numbers, booleans, lists, and nested objects.</p>
 *
 * <p><b>Example -- component with props:</b></p>
 * <pre>{@code
 * @JuxComponent
 * public class PricingCard extends Component {
 *     @Prop private String planName;
 *     @Prop private double price;
 *     @Prop(required = false) private String badge;    // optional, may be null
 *
 *     @Override
 *     public Element render() {
 *         var card = div().cls("pricing-card").children(
 *             h3().text(planName),
 *             span().cls("price").text("$" + price)
 *         );
 *         if (badge != null) {
 *             card = card.children(span().cls("badge").text(badge));
 *         }
 *         return card;
 *     }
 * }
 * }</pre>
 *
 * @see JuxComponent
 * @see State
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Prop {

    /**
     * The prop key name used to look up the value.
     *
     * <p>If empty (the default), the annotated field's name is used as the prop
     * key. For example, a field named {@code planName} maps to a prop key
     * {@code "planName"} in the parent's prop map or the CMS widget's JSON props.</p>
     *
     * @return the prop key name, or empty to use the field name
     */
    String value() default "";

    /**
     * Whether this prop is required for the component to render.
     *
     * <p>When {@code true} (the default), the component fails to render with an
     * error if this prop is not provided by the parent or the CMS widget instance.
     * When {@code false}, a missing prop results in the field retaining its default
     * value (typically {@code null} for objects, {@code 0} for primitives).</p>
     *
     * @return {@code true} if the prop is required; defaults to {@code true}
     */
    boolean required() default true;
}
