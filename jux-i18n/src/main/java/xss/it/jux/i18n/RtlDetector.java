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

package xss.it.jux.i18n;

import java.util.Locale;
import java.util.Set;

/**
 * Utility class that determines whether a {@link Locale} uses a right-to-left (RTL) script.
 *
 * <p>JUX needs to know the text direction of the current locale so that it can set the
 * {@code <html dir="rtl">} attribute and apply the {@code jux-rtl} CSS class to
 * {@code <body>}. Built-in components also use this information to mirror their
 * layouts (breadcrumbs, pagination, navbars) for RTL languages.</p>
 *
 * <p>The detection is based on a static set of ISO 639 language codes whose primary
 * scripts are written right-to-left. The set covers Arabic, Hebrew, Persian, Urdu,
 * Pashto, Sindhi, Yiddish, Kurdish (Sorani), Uyghur, Dhivehi, Kashmiri, Hausa
 * (when written in Arabic script), Khowar, and Central Kurdish.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * Locale arabic = Locale.forLanguageTag("ar");
 * boolean rtl = RtlDetector.isRtl(arabic);        // true
 * String dir  = RtlDetector.direction(arabic);     // "rtl"
 *
 * Locale english = Locale.forLanguageTag("en");
 * boolean ltr = RtlDetector.isRtl(english);        // false
 * String dirEn = RtlDetector.direction(english);   // "ltr"
 * }</pre>
 *
 * <p>This class is stateless and cannot be instantiated. All methods are static.</p>
 *
 * @see Messages#isRtl()
 * @see xss.it.jux.core.PageMeta#autoDir()
 */
public final class RtlDetector {

    /**
     * ISO 639-1 (and a few ISO 639-3) language codes for languages whose primary
     * script is written right-to-left. This set is intentionally conservative --
     * it includes only languages where the dominant script direction is RTL.
     *
     * <ul>
     *   <li>{@code ar}  -- Arabic</li>
     *   <li>{@code he}  -- Hebrew</li>
     *   <li>{@code fa}  -- Persian (Farsi)</li>
     *   <li>{@code ur}  -- Urdu</li>
     *   <li>{@code ps}  -- Pashto</li>
     *   <li>{@code sd}  -- Sindhi</li>
     *   <li>{@code yi}  -- Yiddish</li>
     *   <li>{@code ku}  -- Kurdish (Sorani, when written in Arabic script)</li>
     *   <li>{@code ug}  -- Uyghur</li>
     *   <li>{@code dv}  -- Dhivehi (Maldivian, Thaana script)</li>
     *   <li>{@code ks}  -- Kashmiri</li>
     *   <li>{@code ha}  -- Hausa (Ajami / Arabic script variant)</li>
     *   <li>{@code khw} -- Khowar</li>
     *   <li>{@code ckb} -- Central Kurdish (Sorani)</li>
     * </ul>
     */
    private static final Set<String> RTL_LANGUAGES = Set.of(
        "ar", "he", "fa", "ur", "ps", "sd", "yi", "ku", "ug", "dv", "ks", "ha", "khw", "ckb"
    );

    /** Private constructor prevents instantiation of this utility class. */
    private RtlDetector() {}

    /**
     * Determines whether the given locale uses a right-to-left script.
     *
     * <p>The check is based solely on the locale's {@linkplain Locale#getLanguage() language code}.
     * Region and variant subtags are ignored -- for example, {@code ar-EG} (Arabic, Egypt)
     * and {@code ar-SA} (Arabic, Saudi Arabia) both return {@code true}.</p>
     *
     * <p>A {@code null} locale is treated as LTR (returns {@code false}).</p>
     *
     * @param locale the locale to check, may be {@code null}
     * @return {@code true} if the locale's language is in the known RTL set;
     *         {@code false} if the locale is {@code null} or uses a LTR script
     */
    public static boolean isRtl(Locale locale) {
        if (locale == null) return false;
        return RTL_LANGUAGES.contains(locale.getLanguage());
    }

    /**
     * Returns the CSS text-direction value for the given locale.
     *
     * <p>This value is suitable for the {@code dir} attribute on the {@code <html>}
     * element. WCAG 2.2 (Success Criterion 1.3.2 -- Meaningful Sequence) requires
     * that the text direction be correctly declared for assistive technology to
     * present content in the intended reading order.</p>
     *
     * @param locale the locale to evaluate, may be {@code null}
     * @return {@code "rtl"} if the locale uses a right-to-left script;
     *         {@code "ltr"} otherwise (including when locale is {@code null})
     */
    public static String direction(Locale locale) {
        return isRtl(locale) ? "rtl" : "ltr";
    }
}
