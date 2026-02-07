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
 * Assigns a layout component that wraps this page, providing shared chrome
 * such as headers, navigation bars, footers, and sidebars.
 *
 * <p>The layout's {@code render()} method is called during server-side rendering,
 * and the annotated page's element tree is injected as a child of the layout.
 * This allows multiple pages to share the same structural wrapper without
 * duplicating markup.</p>
 *
 * <p>Layouts are regular JUX {@code Component} subclasses. They can use
 * {@code @Autowired} dependency injection, declare their own {@code @Css} and
 * {@code @Js} resources (which are merged into the resource pipeline), and
 * define their own accessibility features (e.g., skip navigation links).</p>
 *
 * <p>A layout can also be specified via {@link Route#layout()}, but this
 * dedicated annotation is preferred for clarity when multiple annotations
 * are present on the class. If both are set, this annotation takes precedence.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @Route("/admin/dashboard")
 * @Layout(AdminLayout.class)
 * @Title("Admin Dashboard")
 * public class AdminDashboardPage extends Component {
 *     @Override
 *     public Element render() {
 *         return main_().children(
 *             h1().text("Dashboard")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see Route#layout()
 * @see Route
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Layout {

    /**
     * The layout {@code Component} class that wraps this page.
     *
     * <p>The specified class must extend {@code Component} and is instantiated
     * by Spring (supporting {@code @Autowired} injection). During rendering,
     * the page's element tree is inserted as a child within the layout's
     * element tree.</p>
     *
     * @return the layout Component class
     */
    Class<?> value();
}
