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
 * Enables locale-prefixed URL variants for a {@link Route}-annotated component.
 *
 * <p>When a route is annotated with {@code @Localized}, the framework automatically:</p>
 * <ol>
 *   <li>Registers prefixed URL variants for each configured locale
 *       (e.g., {@code /about} generates {@code /en/about}, {@code /es/about},
 *       {@code /fr/about})</li>
 *   <li>Generates {@code <link rel="alternate" hreflang="...">} tags in the HTML
 *       {@code <head>} for SEO (telling search engines about the language variants)</li>
 *   <li>Resolves the current locale from the URL prefix</li>
 *   <li>Optionally redirects the bare path to the default locale's prefixed variant</li>
 * </ol>
 *
 * <p>The list of available locales is configured in {@code application.yml} under
 * {@code jux.i18n.locales}. This annotation can optionally restrict a route to a
 * subset of those locales via {@link #locales()}.</p>
 *
 * <p><b>Example -- all configured locales, default has no prefix:</b></p>
 * <pre>{@code
 * // Available at: /about (English), /es/about (Spanish), /fr/about (French)
 * @Route("/about")
 * @Localized
 * @Title("About Us")
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- restricted locales:</b></p>
 * <pre>{@code
 * // Only available in English and Spanish, not other configured locales
 * @Route("/legal/terms")
 * @Localized(locales = {"en", "es"})
 * public class TermsPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- all locales get a prefix, bare path redirects:</b></p>
 * <pre>{@code
 * // /shop redirects to /en/shop; all locales: /en/shop, /es/shop, /fr/shop
 * @Route("/shop")
 * @Localized(prefixDefault = true, redirectBare = true)
 * public class ShopPage extends Component { ... }
 * }</pre>
 *
 * @see Route
 * @see LocaleParam
 * @see MessageBundle
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Localized {

    /**
     * Specific locales this route should be available in.
     *
     * <p>When non-empty, only the specified locales have URL variants registered for
     * this route. When empty (the default), all locales configured in
     * {@code jux.i18n.locales} are used.</p>
     *
     * <p>Values must be valid BCP 47 language tags matching entries in the
     * application's locale configuration. Examples: {@code "en"}, {@code "es"},
     * {@code "fr"}, {@code "pt-BR"}, {@code "zh-CN"}.</p>
     *
     * @return the locale tags to register, or empty for all configured locales
     */
    String[] locales() default {};

    /**
     * Whether the default locale also gets a URL prefix.
     *
     * <p>When {@code false} (the default), the default locale is served at the bare
     * path (e.g., {@code /about} for English), while other locales get prefixed paths
     * (e.g., {@code /es/about}). When {@code true}, all locales including the default
     * get prefixed paths (e.g., {@code /en/about}, {@code /es/about}).</p>
     *
     * @return {@code true} to prefix the default locale; defaults to {@code false}
     */
    boolean prefixDefault() default false;

    /**
     * Whether the bare (unprefixed) path should redirect to the default locale's
     * prefixed variant.
     *
     * <p>Only relevant when {@link #prefixDefault()} is {@code true}. When enabled,
     * a request to {@code /about} issues a 302 redirect to {@code /en/about}
     * (assuming "en" is the default locale). When {@code false} (the default), the
     * bare path either serves the default locale directly or returns 404, depending
     * on configuration.</p>
     *
     * @return {@code true} to redirect bare paths; defaults to {@code false}
     */
    boolean redirectBare() default false;
}
