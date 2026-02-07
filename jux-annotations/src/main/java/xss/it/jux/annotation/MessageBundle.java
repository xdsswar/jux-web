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
 * Marks an interface as a translation message bundle for the JUX i18n system.
 *
 * <p>Translation bundles are Java interfaces where each method represents a translatable
 * message. The default (fallback) language is defined directly on the interface methods
 * via the {@link Message} annotation. Locale-specific translations are provided by
 * sub-interfaces annotated with {@link MessageLocale}.</p>
 *
 * <p>The JUX annotation processor generates implementations of these interfaces at
 * compile time. At runtime, the {@code Messages} service resolves the correct
 * implementation based on the current request's locale. Missing translations fall
 * back to the parent interface's default language automatically.</p>
 *
 * <h2>Compile-Time Validation</h2>
 * <p>The annotation processor validates:</p>
 * <ul>
 *   <li>Every configured locale has implementations for all bundles (WARNING if missing)</li>
 *   <li>Locale interfaces do not add methods not present in the parent (compile ERROR)</li>
 *   <li>All {@link Message} patterns are valid {@code java.text.MessageFormat} syntax
 *       (compile ERROR)</li>
 * </ul>
 *
 * <p><b>Example -- default language bundle (English):</b></p>
 * <pre>{@code
 * @MessageBundle
 * public interface HomeMessages {
 *     @Message("Welcome to our site")
 *     String welcome();
 *
 *     @Message("Hello, {0}")
 *     String greeting(String name);
 *
 *     @Message("{0,choice,0#No items|1#1 item|1<{0} items}")
 *     String itemCount(int count);
 * }
 * }</pre>
 *
 * <p><b>Example -- Spanish translation:</b></p>
 * <pre>{@code
 * @MessageBundle
 * @MessageLocale("es")
 * public interface HomeMessagesEs extends HomeMessages {
 *     @Override @Message("Bienvenido a nuestro sitio")
 *     String welcome();
 *
 *     @Override @Message("Hola, {0}")
 *     String greeting(String name);
 * }
 * }</pre>
 *
 * <p><b>Usage in a component:</b></p>
 * <pre>{@code
 * @Autowired private Messages messages;
 *
 * @Override
 * public Element render() {
 *     var t = messages.get(HomeMessages.class);
 *     return h1().text(t.welcome());
 * }
 * }</pre>
 *
 * @see MessageLocale
 * @see Message
 * @see Localized
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageBundle {

    /**
     * Optional bundle name for programmatic lookup.
     *
     * <p>If empty (the default), the class simple name is used as the bundle
     * name (e.g., {@code "HomeMessages"}). The bundle name is used internally
     * by the message resolution system to identify and cache bundle instances.</p>
     *
     * @return the bundle name, or empty to use the class simple name
     */
    String value() default "";
}
