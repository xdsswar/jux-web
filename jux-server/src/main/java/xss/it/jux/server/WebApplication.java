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

package xss.it.jux.server;

import xss.it.jux.core.PageMeta;

/**
 * Marker interface for JUX web applications.
 *
 * <p>Implement this in your {@code @SpringBootApplication} class and annotate
 * it with {@code @Css} and {@code @Js} to define global resources that are
 * automatically included on every page — without repeating annotations on
 * each page class.</p>
 *
 * <pre>{@code
 * @SpringBootApplication
 * @Css(value = "/css/bootstrap.min.css", order = 1)
 * @Css(value = "/css/app.css", order = 10)
 * @Js(value = "/js/bootstrap.bundle.min.js", defer = true, order = 1)
 * public class MyApp implements WebApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApp.class, args);
 *     }
 * }
 * }</pre>
 *
 * <p>Individual pages can still declare their own {@code @Css}/{@code @Js}
 * annotations for page-specific resources — those are merged with the
 * application-level ones. Pages can also remove inherited resources via
 * {@code PageMeta.removeCss()} and {@code PageMeta.removeJs()}.</p>
 *
 * <p>Application-level resources are injected first (lowest priority).
 * Page-level resources with the same path override the application-level
 * definition. Resources are deduplicated by path and sorted by order.</p>
 *
 * <h3>Programmatic application-wide defaults</h3>
 *
 * <p>Override {@link #defaultPageMeta()} to set application-wide metadata
 * defaults programmatically — for example a title template, site name,
 * theme color, or og:site_name that should apply to every page unless
 * a page explicitly overrides it:</p>
 *
 * <pre>{@code
 * @SpringBootApplication
 * @Css(value = "/css/bootstrap.min.css", order = 1)
 * public class MyApp implements WebApplication {
 *
 *     @Override
 *     public PageMeta defaultPageMeta() {
 *         return PageMeta.create()
 *             .titleTemplate("%s | My Site")
 *             .ogSiteName("My Site")
 *             .themeColor("#3b82f6");
 *     }
 *
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApp.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>Metadata merge pipeline</h3>
 *
 * <p>The final metadata for each page is resolved in this order:</p>
 * <ol>
 *   <li>Application-level {@code @Css}/{@code @Js} annotations (global resources)</li>
 *   <li>Page-level annotations ({@code @Title}, {@code @Meta}, {@code @Css}, {@code @Js})</li>
 *   <li>{@link #defaultPageMeta()} — application-wide programmatic baseline</li>
 *   <li>Page's {@code pageMeta()} — page-specific overrides (highest priority)</li>
 * </ol>
 */
public interface WebApplication {

    /**
     * Application-wide programmatic metadata defaults.
     *
     * <p>Override this to provide a {@link PageMeta} baseline that is merged
     * into every page's metadata after annotation processing but before the
     * page's own {@code pageMeta()} override. This is the right place for
     * settings that should apply to all pages unless explicitly overridden:</p>
     *
     * <ul>
     *   <li>{@code titleTemplate("%s | My Site")} — consistent title suffix</li>
     *   <li>{@code ogSiteName("My Site")} — OpenGraph site name</li>
     *   <li>{@code themeColor("#3b82f6")} — mobile browser chrome color</li>
     *   <li>{@code favicon("/img/favicon.ico")} — site-wide favicon</li>
     *   <li>{@code preconnect("https://cdn.example.com")} — performance hint</li>
     * </ul>
     *
     * <p>Returns {@code null} by default, meaning no application-wide programmatic
     * defaults are applied.</p>
     *
     * @return a {@link PageMeta} with application-wide defaults, or {@code null}
     */
    default PageMeta defaultPageMeta() {
        return null;
    }
}
