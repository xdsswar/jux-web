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
 * Embeds inline JavaScript from a method's return value directly into the page's HTML.
 *
 * <p>The annotated method must return a {@code String} containing raw JavaScript code.
 * The framework calls this method at render time and injects the returned code inside
 * a {@code <script>} tag at the specified {@link #position()} in the HTML document.
 * The script is positioned among other JS resources according to the {@link #order()} value.</p>
 *
 * <p>Use this annotation for page-specific initialization scripts, configuration injection,
 * or any small inline scripts that do not warrant a separate JS file. The method has access
 * to Spring-injected dependencies, so it can produce scripts that embed server-side
 * configuration into the client.</p>
 *
 * <p>For external script files, use {@link Js} instead. For programmatic JS injection,
 * use {@code PageMeta.inlineJs()}.</p>
 *
 * <p><b>Example -- analytics configuration injection:</b></p>
 * <pre>{@code
 * @Route("/")
 * public class HomePage extends Component {
 *
 *     @Autowired private AppConfig config;
 *
 *     @InlineJs(position = JsPosition.HEAD, order = 1)
 *     public String analyticsConfig() {
 *         return "window.GA_ID = '" + config.getGaId() + "';";
 *     }
 *
 *     @Override
 *     public Element render() { ... }
 * }
 * }</pre>
 *
 * @see Js
 * @see InlineCss
 * @see JsPosition
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InlineJs {

    /**
     * Where to inject the inline {@code <script>} tag in the HTML document.
     *
     * <p>Defaults to {@link JsPosition#BODY_END}. Use {@link JsPosition#HEAD}
     * for scripts that must execute before the page body is parsed (e.g.,
     * global configuration variables, feature flags, analytics setup).</p>
     *
     * @return the injection position; defaults to {@link JsPosition#BODY_END}
     */
    JsPosition position() default JsPosition.BODY_END;

    /**
     * Sort order relative to other JavaScript resources (both external and inline).
     *
     * <p>Lower numbers are rendered first within the same {@link #position()}
     * partition. This controls the position of the inline {@code <script>} tag
     * relative to {@code <script src="...">} tags from {@link Js} annotations
     * and other inline script blocks.</p>
     *
     * @return the sort order; defaults to {@code 100}
     */
    int order() default 100;
}
