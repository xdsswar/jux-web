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
 * Declares the locale of a {@link MessageBundle} implementation.
 *
 * <p>This annotation is placed on interfaces that extend a {@link MessageBundle}-annotated
 * parent interface, identifying which locale the translation applies to. Each locale
 * that the application supports should have a corresponding interface annotated with
 * {@code @MessageLocale} that overrides the parent's methods with translated messages.</p>
 *
 * <p>The locale value must be a valid BCP 47 language tag. The framework matches it
 * against the locales configured in {@code jux.i18n.locales} and uses the appropriate
 * implementation based on the resolved request locale.</p>
 *
 * <p>Methods that are not overridden in the locale-specific interface fall back to the
 * parent interface's {@link Message} default value (i.e., the default language).</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @MessageBundle
 * @MessageLocale("es")
 * public interface HomeMessagesEs extends HomeMessages {
 *     @Override @Message("Bienvenido a nuestro sitio")
 *     String welcome();
 *
 *     @Override @Message("Hola, {0}")
 *     String greeting(String name);
 *     // itemCount() not overridden -- falls back to English default
 * }
 * }</pre>
 *
 * @see MessageBundle
 * @see Message
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageLocale {

    /**
     * The BCP 47 language tag identifying this translation's locale.
     *
     * <p>Examples of valid locale tags:</p>
     * <ul>
     *   <li>{@code "es"} -- Spanish</li>
     *   <li>{@code "fr"} -- French</li>
     *   <li>{@code "de"} -- German</li>
     *   <li>{@code "pt-BR"} -- Brazilian Portuguese</li>
     *   <li>{@code "zh-CN"} -- Simplified Chinese</li>
     *   <li>{@code "ar"} -- Arabic</li>
     *   <li>{@code "ja"} -- Japanese</li>
     * </ul>
     *
     * @return the BCP 47 locale tag
     */
    String value();
}
