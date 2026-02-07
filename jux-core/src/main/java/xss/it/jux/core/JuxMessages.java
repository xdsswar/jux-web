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

package xss.it.jux.core;

import java.util.List;
import java.util.Locale;

/**
 * Consumer-facing interface for accessing translated messages and locale information.
 *
 * <p>This interface lives in {@code jux-core} so that {@link Page} subclasses can
 * access i18n functionality without a direct dependency on the {@code jux-i18n} module.
 * The concrete implementation ({@code Messages} in {@code jux-i18n}) is injected by
 * the framework into every {@link Page} before {@link Page#pageMeta()} and
 * {@link Component#render()} are called.</p>
 *
 * <h2>Usage in a Page</h2>
 * <pre>{@code
 * @Route("/")
 * @Title("Home")
 * public class HomePage extends Page {
 *
 *     @Override
 *     public Element render() {
 *         return main_().children(
 *             h1().text(messages().getString("hero.title")),
 *             p().text(messages().getString("hero.subtitle"))
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h2>Properties-file lookup</h2>
 * <p>When the {@code jux.i18n.base-name} property is configured (e.g. {@code "lang"}),
 * {@link #getString(String)} loads translations from standard Java
 * {@link java.util.ResourceBundle} files on the classpath:
 * {@code lang_en.properties}, {@code lang_es.properties}, etc.</p>
 *
 * <h2>Typed message bundles</h2>
 * <p>For compile-time type safety, the full {@code Messages} class in {@code jux-i18n}
 * also supports {@code @MessageBundle} Java interfaces via {@code Messages.get(Class)}.
 * To use that API, inject or cast to {@code Messages} directly.</p>
 *
 * @see Page#messages()
 */
public interface JuxMessages {

    /**
     * Look up a translated string by key for the current request locale.
     *
     * <p>If the key is not found, the key itself is returned as a fallback,
     * making it safe to use in templates without null checks.</p>
     *
     * @param key the message key (e.g. {@code "nav.home"}, {@code "hero.title"})
     * @return the translated string, or the key itself if not found
     */
    String getString(String key);

    /**
     * Look up a translated string and format it with the given arguments
     * using {@link java.text.MessageFormat}.
     *
     * <p>Example property: {@code greeting=Hello, {0}!}<br>
     * Usage: {@code messages().getString("greeting", "World")} returns {@code "Hello, World!"}</p>
     *
     * @param key  the message key
     * @param args arguments to substitute into the message pattern
     * @return the formatted translated string, or the key itself if not found
     */
    String getString(String key, Object... args);

    /**
     * Returns the locale resolved for the current HTTP request.
     *
     * <p>Determined by the i18n resolution chain: URL prefix, {@code ?lang=} query
     * param, {@code jux-lang} cookie, session, {@code Accept-Language} header,
     * and finally the configured default locale.</p>
     *
     * @return the current request locale, never {@code null}
     */
    Locale currentLocale();

    /**
     * Returns all locales that the application is configured to support.
     *
     * <p>Typically used by language-switcher components to render links for
     * each supported language. The list order matches the configuration order.</p>
     *
     * @return an unmodifiable list of supported locales; never {@code null} or empty
     */
    List<Locale> availableLocales();

    /**
     * Indicates whether the current request locale uses a right-to-left script.
     *
     * <p>Use this to set {@code <html dir="rtl">} and apply RTL-specific styling.
     * Returns {@code true} for Arabic, Hebrew, Persian, Urdu, and other RTL locales.</p>
     *
     * @return {@code true} if the current locale is RTL; {@code false} otherwise
     */
    boolean isRtl();
}
