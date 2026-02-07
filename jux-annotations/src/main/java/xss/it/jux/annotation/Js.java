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
 * Attaches a JavaScript file to a page or component.
 *
 * <p>This annotation is repeatable -- use multiple {@code @Js} annotations to attach
 * several scripts to a single component. The framework collects JS resources from
 * the page, its layout, and all child components, deduplicates by path, sorts by
 * {@link #order()}, and renders {@code <script>} tags in the HTML output.</p>
 *
 * <p>The default position is {@link JsPosition#BODY_END} (best for performance), and
 * scripts are deferred by default ({@link #defer()} is {@code true}).</p>
 *
 * <h2>Resource Pipeline</h2>
 * <ol>
 *   <li>Gather {@code @Js} from: Layout, Page, and child Components (recursive)</li>
 *   <li>Add any programmatic JS from {@code PageMeta.js()}</li>
 *   <li>Remove entries listed in {@code PageMeta.removeJs()}</li>
 *   <li>Deduplicate by path/URL</li>
 *   <li>Partition by {@link #position()} (HEAD vs. BODY_END)</li>
 *   <li>Sort by {@link #order()} ascending within each partition</li>
 *   <li>Evaluate SpEL {@link #condition()} expressions</li>
 *   <li>Render {@code <script>} tags</li>
 * </ol>
 *
 * <p><b>Example -- CDN script with defer:</b></p>
 * <pre>{@code
 * @Route("/dashboard")
 * @Js(value = "https://cdn.jsdelivr.net/npm/chart.js", defer = true, order = 1)
 * @Js(value = "pages/dashboard.js", module = true, order = 10)
 * public class DashboardPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- conditional script inclusion:</b></p>
 * <pre>{@code
 * @Js(value = "vendor/prism.js", condition = "#{showCode}", order = 50)
 * }</pre>
 *
 * @see JsPosition
 * @see InlineJs
 * @see Css
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Js.List.class)
@Documented
public @interface Js {

    /**
     * Path to the JavaScript file, either relative to the {@code /static} directory
     * or an absolute CDN URL.
     *
     * <p>Relative paths are resolved against the application's static resources
     * directory (e.g., {@code "pages/dashboard.js"} resolves to
     * {@code /static/pages/dashboard.js}). Absolute URLs (starting with
     * {@code http://} or {@code https://}) are used as-is.</p>
     *
     * @return the JavaScript file path or URL
     */
    String value();

    /**
     * Where to inject the {@code <script>} tag in the HTML document.
     *
     * <p>Defaults to {@link JsPosition#BODY_END}, which is recommended for most
     * scripts as it avoids blocking HTML parsing. Use {@link JsPosition#HEAD}
     * for scripts that must execute before page content is rendered (e.g.,
     * analytics configuration, critical polyfills).</p>
     *
     * @return the injection position; defaults to {@link JsPosition#BODY_END}
     */
    JsPosition position() default JsPosition.BODY_END;

    /**
     * Sort order for this script relative to other JavaScript resources.
     *
     * <p>Lower numbers are loaded first. This controls the order of
     * {@code <script>} tags within the same {@link #position()} partition.
     * Dependencies should have lower order numbers than the scripts that
     * depend on them.</p>
     *
     * @return the sort order; defaults to {@code 100}
     */
    int order() default 100;

    /**
     * Whether to load this script asynchronously.
     *
     * <p>When {@code true}, renders {@code <script async>}. The browser fetches
     * the script in parallel with HTML parsing and executes it as soon as it is
     * available, regardless of DOM readiness. This means execution order among
     * async scripts is not guaranteed.</p>
     *
     * <p>Use for independent scripts that do not depend on DOM state or other
     * scripts (e.g., analytics, ads).</p>
     *
     * @return {@code true} for async loading; defaults to {@code false}
     */
    boolean async() default false;

    /**
     * Whether to defer script execution until after HTML parsing completes.
     *
     * <p>When {@code true} (the default), renders {@code <script defer>}. The
     * browser fetches the script in parallel with HTML parsing but delays
     * execution until after the document is fully parsed. Deferred scripts
     * execute in their document order, making this safe for scripts with
     * dependencies.</p>
     *
     * <p>This is the default because it provides the best balance of performance
     * and predictability for most use cases.</p>
     *
     * @return {@code true} for deferred execution; defaults to {@code true}
     */
    boolean defer() default true;

    /**
     * Whether this script is an ES module.
     *
     * <p>When {@code true}, renders {@code <script type="module">}. ES modules
     * use {@code import}/{@code export} syntax, have strict mode enabled by
     * default, and are deferred automatically. Module scripts also support
     * top-level {@code await} and have their own scope (no global pollution).</p>
     *
     * @return {@code true} to load as an ES module; defaults to {@code false}
     */
    boolean module() default false;

    /**
     * Subresource Integrity (SRI) hash for verifying CDN-hosted resources.
     *
     * <p>Rendered as the {@code integrity} attribute on the {@code <script>} tag.
     * The browser verifies the fetched resource against this hash before executing it,
     * protecting against CDN compromise or tampering.</p>
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
     * <p>When set, the script is only included if this SpEL expression evaluates
     * to {@code true} at render time. This allows environment-specific or
     * state-dependent script loading.</p>
     *
     * <p>Examples: {@code "#{profile == 'prod'}"}, {@code "#{showCode}"},
     * {@code "#{user.isPremium()}"}.</p>
     *
     * <p>An empty string (the default) means the script is always included.</p>
     *
     * @return the SpEL condition expression, or empty for unconditional inclusion
     */
    String condition() default "";

    /**
     * Container annotation for repeatable {@code @Js} annotations.
     *
     * <p>This is used internally by the Java compiler to hold multiple {@code @Js}
     * annotations on a single type. Developers should not use this directly -- simply
     * apply multiple {@code @Js} annotations to the same class.</p>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * The array of {@link Js} annotations applied to the annotated type.
         *
         * @return the JS annotations
         */
        Js[] value();
    }
}
