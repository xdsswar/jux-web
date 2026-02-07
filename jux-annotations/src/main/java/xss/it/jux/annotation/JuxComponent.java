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
 * Marks a class as a JUX component and registers it as a Spring-managed bean.
 *
 * <p>Components annotated with {@code @JuxComponent} are auto-discovered by Spring's
 * component scan and can use {@code @Autowired} for dependency injection. This is
 * the primary annotation for non-page components -- reusable UI widgets, cards,
 * navigation elements, and interactive controls that are composed into page
 * components.</p>
 *
 * <h2>Server-Only vs. Client-Side Components</h2>
 * <ul>
 *   <li><b>Server-only</b> ({@code clientSide = false}, the default): The component is
 *       rendered only during server-side rendering (SSR). No JavaScript is generated.
 *       This is appropriate for static content components that do not require
 *       interactivity.</li>
 *   <li><b>Client-side</b> ({@code clientSide = true}): The component is compiled to
 *       JavaScript via TeaVM and hydrated on the client after SSR. This enables:
 *       <ul>
 *         <li>DOM event handling via {@link On} and {@code Element.on()}</li>
 *         <li>Reactive state management via {@link State}</li>
 *         <li>Lifecycle hooks via {@link OnMount} and {@link OnUnmount}</li>
 *         <li>Direct DOM access via {@code org.teavm.jso.dom.html.*} APIs</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Example -- server-only card component:</b></p>
 * <pre>{@code
 * @JuxComponent
 * public class PricingCard extends Component {
 *     @Prop private String planName;
 *     @Prop private double price;
 *
 *     @Override
 *     public Element render() {
 *         return div().cls("pricing-card").children(
 *             h3().text(planName),
 *             span().cls("price").text("$" + price + "/mo")
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example -- interactive client-side component:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class Counter extends Component {
 *     @State private int count = 0;
 *
 *     @Override
 *     public Element render() {
 *         return div().children(
 *             span().text("Count: " + count),
 *             button().text("+").on("click", e -> count++)
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see Prop
 * @see State
 * @see On
 * @see OnMount
 * @see OnUnmount
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JuxComponent {

    /**
     * Spring bean name for this component.
     *
     * <p>If empty (the default), Spring auto-generates a bean name from the class
     * name using its default naming strategy (e.g., {@code PricingCard} becomes
     * {@code "pricingCard"}). Set explicitly only when you need a specific bean
     * name for programmatic lookup or disambiguation.</p>
     *
     * @return the Spring bean name, or empty for auto-generated
     */
    String value() default "";

    /**
     * Whether this component should be compiled to JavaScript via TeaVM and
     * hydrated on the client after server-side rendering.
     *
     * <p>When {@code true}, the component is included in the TeaVM compilation
     * pipeline and the resulting JavaScript enables:</p>
     * <ul>
     *   <li>DOM event handling ({@link On}, {@code Element.on()})</li>
     *   <li>Reactive state with automatic re-rendering ({@link State})</li>
     *   <li>Post-hydration initialization ({@link OnMount})</li>
     *   <li>Cleanup on removal ({@link OnUnmount})</li>
     *   <li>Direct browser DOM access via {@code org.teavm.jso.dom.html.*}</li>
     * </ul>
     *
     * <p>When {@code false} (the default), the component is rendered server-side
     * only and no JavaScript is generated, resulting in zero client-side overhead.</p>
     *
     * @return {@code true} for client-side hydration; defaults to {@code false}
     */
    boolean clientSide() default false;
}
