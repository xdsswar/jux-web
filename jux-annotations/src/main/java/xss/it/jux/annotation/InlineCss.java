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
 * Embeds inline CSS from a method's return value directly into the page's HTML.
 *
 * <p>The annotated method must return a {@code String} containing raw CSS. The framework
 * calls this method at render time and injects the returned CSS inside a {@code <style>}
 * tag in the HTML {@code <head>}. The {@code <style>} tag is positioned among other
 * CSS resources according to the {@link #order()} value.</p>
 *
 * <p>Use this annotation for small, page-specific styles that do not warrant a separate
 * CSS file, or for dynamically generated styles that depend on runtime data (e.g.,
 * theme variables from a database, user-configured colors).</p>
 *
 * <p>For external stylesheets, use {@link Css} instead. For programmatic CSS injection,
 * use {@code PageMeta.inlineCss()}.</p>
 *
 * <p><b>Example -- dynamic theme variables:</b></p>
 * <pre>{@code
 * @Route("/")
 * public class HomePage extends Component {
 *
 *     @Autowired private ThemeService theme;
 *
 *     @InlineCss(order = 5)
 *     public String dynamicTheme() {
 *         return ":root { --primary: " + theme.getPrimaryColor() + "; "
 *              + "--font-family: " + theme.getFontFamily() + "; }";
 *     }
 *
 *     @Override
 *     public Element render() { ... }
 * }
 * }</pre>
 *
 * @see Css
 * @see InlineJs
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InlineCss {

    /**
     * Sort order relative to other CSS resources (both external and inline).
     *
     * <p>Lower numbers are rendered first. This controls the position of the
     * {@code <style>} tag relative to {@code <link>} tags from {@link Css}
     * annotations and other inline CSS blocks. Use a low order number to
     * ensure CSS variables or resets are available before other styles.</p>
     *
     * @return the sort order; defaults to {@code 100}
     */
    int order() default 100;
}
