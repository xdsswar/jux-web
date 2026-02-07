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
 * Sets the page favicon, rendered as a {@code <link rel="icon">} tag in the HTML {@code <head>}.
 *
 * <p>The favicon is the small icon displayed in browser tabs, bookmarks, and history.
 * This annotation supports standard ICO files as well as modern PNG and SVG favicons
 * with explicit MIME types and size hints for different display contexts.</p>
 *
 * <p>This annotation can be overridden programmatically via {@code PageMeta.favicon()}
 * in the component's {@code pageMeta()} method.</p>
 *
 * <p><b>Example -- standard ICO favicon:</b></p>
 * <pre>{@code
 * @Route("/")
 * @Favicon("/images/favicon.ico")
 * public class HomePage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- PNG favicon with size hint:</b></p>
 * <pre>{@code
 * @Route("/")
 * @Favicon(value = "/images/icon-192.png", type = "image/png", sizes = "192x192")
 * public class HomePage extends Component { ... }
 * }</pre>
 *
 * @see Canonical
 * @see Title
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Favicon {

    /**
     * Path to the favicon file.
     *
     * <p>Can be a relative path resolved against the static resources directory
     * (e.g., {@code "/images/favicon.ico"}) or an absolute URL.</p>
     *
     * @return the favicon file path or URL
     */
    String value();

    /**
     * MIME type of the favicon file.
     *
     * <p>Rendered as the {@code type} attribute on the {@code <link>} tag.
     * Common values:</p>
     * <ul>
     *   <li>{@code "image/x-icon"} -- for ICO files (the default)</li>
     *   <li>{@code "image/png"} -- for PNG favicons</li>
     *   <li>{@code "image/svg+xml"} -- for SVG favicons (scalable, modern)</li>
     * </ul>
     *
     * @return the MIME type; defaults to {@code "image/x-icon"}
     */
    String type() default "image/x-icon";

    /**
     * Size hint for the favicon image.
     *
     * <p>Rendered as the {@code sizes} attribute on the {@code <link>} tag.
     * Helps the browser select the appropriate icon for different display
     * contexts (tab icon, home screen icon, taskbar, etc.).</p>
     *
     * <p>Examples: {@code "16x16"}, {@code "32x32"}, {@code "192x192"},
     * {@code "any"} (for SVG). An empty string (the default) omits the attribute.</p>
     *
     * @return the size hint string, or empty to omit
     */
    String sizes() default "";
}
