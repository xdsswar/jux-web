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
 * Attaches a CSS stylesheet to a page or component.
 *
 * <p>This annotation is repeatable -- use multiple {@code @Css} annotations to attach
 * several stylesheets to a single component. The framework collects CSS resources from
 * the page, its layout, and all child components, deduplicates by path, sorts by
 * {@link #order()}, and renders {@code <link rel="stylesheet">} tags in the HTML output.</p>
 *
 * <h2>Resource Pipeline</h2>
 * <ol>
 *   <li>Gather {@code @Css} from: Layout, Page, and child Components (recursive)</li>
 *   <li>Add any programmatic CSS from {@code PageMeta.css()}</li>
 *   <li>Remove entries listed in {@code PageMeta.removeCss()}</li>
 *   <li>Deduplicate by path/URL</li>
 *   <li>Partition by {@link #position()} (HEAD vs. BODY_END)</li>
 *   <li>Sort by {@link #order()} ascending within each partition</li>
 *   <li>Evaluate SpEL {@link #condition()} expressions</li>
 *   <li>Render {@code <link>} tags</li>
 * </ol>
 *
 * <p><b>Example -- multiple stylesheets with ordering:</b></p>
 * <pre>{@code
 * @Route("/about")
 * @Css(value = "themes/default.css", order = 1)
 * @Css(value = "pages/about.css", order = 10)
 * @Css(value = "https://cdn.example.com/prism.css", condition = "#{showCode}")
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- CDN resource with integrity check:</b></p>
 * <pre>{@code
 * @Css(value = "https://cdn.example.com/lib.css",
 *      integrity = "sha384-abc123...",
 *      order = 1)
 * }</pre>
 *
 * @see CssPosition
 * @see InlineCss
 * @see Js
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Css.List.class)
@Documented
public @interface Css {

    /**
     * Path to the CSS file, either relative to the {@code /static} directory
     * or an absolute CDN URL.
     *
     * <p>Relative paths are resolved against the application's static resources
     * directory (e.g., {@code "themes/default.css"} resolves to
     * {@code /static/themes/default.css}). Absolute URLs (starting with
     * {@code http://} or {@code https://}) are used as-is.</p>
     *
     * @return the CSS file path or URL
     */
    String value();

    /**
     * Where to inject the {@code <link>} tag in the HTML document.
     *
     * <p>Defaults to {@link CssPosition#HEAD}, which is recommended for most
     * stylesheets to prevent a flash of unstyled content. Use
     * {@link CssPosition#BODY_END} for non-critical styles that can load late.</p>
     *
     * @return the injection position; defaults to {@link CssPosition#HEAD}
     */
    CssPosition position() default CssPosition.HEAD;

    /**
     * Sort order for this stylesheet relative to other CSS resources.
     *
     * <p>Lower numbers are loaded first. This controls the order of
     * {@code <link>} tags within the same {@link #position()} partition.
     * Use low numbers (1-10) for base/reset/theme CSS and higher numbers
     * (50+) for page-specific overrides.</p>
     *
     * @return the sort order; defaults to {@code 100}
     */
    int order() default 100;

    /**
     * CSS media query for conditional stylesheet loading.
     *
     * <p>Rendered as the {@code media} attribute on the {@code <link>} tag.
     * When set, the browser only applies this stylesheet when the media query
     * matches. Examples: {@code "print"}, {@code "screen and (max-width: 768px)"},
     * {@code "(prefers-color-scheme: dark)"}.</p>
     *
     * <p>An empty string (the default) means the stylesheet applies to all media.</p>
     *
     * @return the CSS media query, or empty for all media
     */
    String media() default "";

    /**
     * Whether to load this stylesheet asynchronously (non-render-blocking).
     *
     * <p>When {@code true}, the stylesheet is loaded without blocking page rendering.
     * This is achieved by initially setting {@code media="print"} and swapping to
     * {@code media="all"} on load, or using an equivalent non-blocking technique.
     * Use for below-the-fold styles that are not critical for initial paint.</p>
     *
     * @return {@code true} for async loading; defaults to {@code false}
     */
    boolean async() default false;

    /**
     * Subresource Integrity (SRI) hash for verifying CDN-hosted resources.
     *
     * <p>Rendered as the {@code integrity} attribute on the {@code <link>} tag.
     * The browser verifies the fetched resource against this hash before applying it,
     * protecting against CDN compromise or tampering. Format example:
     * {@code "sha384-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC"}.</p>
     *
     * <p>Only applicable for absolute CDN URLs. Empty (the default) means no
     * integrity check.</p>
     *
     * @return the SRI hash string, or empty for no integrity check
     */
    String integrity() default "";

    /**
     * Spring Expression Language (SpEL) condition for conditional inclusion.
     *
     * <p>When set, the stylesheet is only included if this SpEL expression evaluates
     * to {@code true} at render time. This allows environment-specific or state-dependent
     * stylesheet loading.</p>
     *
     * <p>Examples: {@code "#{profile == 'prod'}"}, {@code "#{showCode}"},
     * {@code "#{user.isPremium()}"}.</p>
     *
     * <p>An empty string (the default) means the stylesheet is always included.</p>
     *
     * @return the SpEL condition expression, or empty for unconditional inclusion
     */
    String condition() default "";

    /**
     * Container annotation for repeatable {@code @Css} annotations.
     *
     * <p>This is used internally by the Java compiler to hold multiple {@code @Css}
     * annotations on a single type. Developers should not use this directly -- simply
     * apply multiple {@code @Css} annotations to the same class.</p>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * The array of {@link Css} annotations applied to the annotated type.
         *
         * @return the CSS annotations
         */
        Css[] value();
    }
}
