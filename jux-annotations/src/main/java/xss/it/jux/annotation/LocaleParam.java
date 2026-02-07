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
 * Injects the resolved {@link java.util.Locale} for the current request.
 *
 * <p>The locale is determined by the framework's i18n locale resolution chain,
 * which evaluates the following sources in priority order:</p>
 * <ol>
 *   <li>URL prefix (e.g., {@code /es/about} resolves to {@code Locale("es")})</li>
 *   <li>Query parameter (e.g., {@code ?lang=fr})</li>
 *   <li>Cookie (configured via {@code jux.i18n.cookie-name}, default {@code "jux-lang"})</li>
 *   <li>Session attribute</li>
 *   <li>{@code Accept-Language} HTTP header</li>
 *   <li>Default locale from {@code jux.i18n.default-locale} configuration</li>
 * </ol>
 *
 * <p>The annotated field must be of type {@link java.util.Locale}.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @Route("/products")
 * @Localized
 * public class ProductsPage extends Component {
 *
 *     @LocaleParam private Locale locale;
 *
 *     @Override
 *     public Element render() {
 *         String lang = locale.getLanguage();  // "en", "es", "ar"
 *         // render locale-specific content...
 *     }
 * }
 * }</pre>
 *
 * @see Localized
 * @see Route
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocaleParam {
}
