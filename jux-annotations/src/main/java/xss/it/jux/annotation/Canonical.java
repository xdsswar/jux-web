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
 * Sets the canonical URL for this page, rendered as
 * {@code <link rel="canonical" href="...">} in the HTML {@code <head>}.
 *
 * <p>The canonical URL tells search engines which URL is the "official" version of a page
 * when the same content is accessible via multiple URLs (e.g., with and without trailing
 * slashes, with different query parameters, or across HTTP and HTTPS). This prevents
 * duplicate content issues and consolidates page ranking signals.</p>
 *
 * <p>When the value is empty (the default), the framework auto-generates the canonical
 * URL from the current request URL, stripping tracking parameters and normalizing the
 * path. This can be overridden programmatically via {@code PageMeta.canonical()} in
 * the component's {@code pageMeta()} method.</p>
 *
 * <p><b>Example -- explicit canonical URL:</b></p>
 * <pre>{@code
 * @Route("/blog/{slug}")
 * @Canonical("https://myblog.com/blog/")
 * public class BlogPostPage extends Component {
 *     // canonical is typically set dynamically in pageMeta() for parameterized routes
 * }
 * }</pre>
 *
 * <p><b>Example -- auto-generated canonical (recommended for most cases):</b></p>
 * <pre>{@code
 * @Route("/about")
 * @Canonical
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * @see Title
 * @see Meta
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Canonical {

    /**
     * The canonical URL for this page.
     *
     * <p>Should be an absolute URL (e.g., {@code "https://example.com/about"}) for
     * maximum compatibility with search engines. An empty string (the default) instructs
     * the framework to auto-generate the canonical URL from the current request.</p>
     *
     * @return the canonical URL, or empty for auto-generation
     */
    String value() default "";
}
