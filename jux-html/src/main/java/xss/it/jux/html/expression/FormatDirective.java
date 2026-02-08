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

package xss.it.jux.html.expression;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A custom format directive that can be used in {@code @{name}} or
 * {@code @{name(args)}} expressions within HTML templates.
 *
 * <p>Implementations are discovered by Spring and registered with the
 * {@link FormatResolver}. Custom directives are checked before built-in
 * ones, so they can override default behavior.</p>
 *
 * <p><b>Usage in a Spring {@code @Configuration}:</b></p>
 * <pre>{@code
 * @Configuration
 * public class MyFormats {
 *
 *     @Bean
 *     FormatDirective percentFormat() {
 *         return FormatDirective.of("percent", (args, locale) -> {
 *             double val = Double.parseDouble(args.trim());
 *             return NumberFormat.getPercentInstance(locale).format(val / 100);
 *         });
 *     }
 *
 *     @Bean
 *     FormatDirective appVersion() {
 *         return FormatDirective.simple("appVersion", locale -> "2.1.0");
 *     }
 * }
 * }</pre>
 *
 * <p>Then in templates:</p>
 * <pre>{@code
 * <span>@{percent(75)}</span>    <!-- renders "75%" -->
 * <span>v@{appVersion}</span>    <!-- renders "v2.1.0" -->
 * }</pre>
 *
 * @see FormatResolver
 */
public interface FormatDirective {

    /**
     * The directive name used in templates. Must be a simple identifier
     * (letters, digits, underscores). Case-insensitive matching is used.
     *
     * <p>For example, a name of {@code "percent"} matches both
     * {@code @{percent(75)}} and {@code @{Percent(75)}}.</p>
     *
     * @return the directive name, never null or empty
     */
    String name();

    /**
     * Resolve the directive to a string value.
     *
     * @param args   the raw argument string from inside the parentheses,
     *               or {@code null} if the directive was used without arguments
     *               (e.g. {@code @{appVersion}} vs {@code @{currency(29.99)}})
     * @param locale the current locale for formatting
     * @return the resolved string value, never null
     * @throws IllegalArgumentException if the arguments are invalid
     */
    String resolve(String args, Locale locale);

    /**
     * Creates a directive that accepts arguments.
     *
     * <p>Example:</p>
     * <pre>{@code
     * FormatDirective.of("percent", (args, locale) -> {
     *     double val = Double.parseDouble(args.trim());
     *     return NumberFormat.getPercentInstance(locale).format(val / 100);
     * });
     * }</pre>
     *
     * @param name     the directive name
     * @param resolver the resolve function (args, locale) -&gt; result
     * @return a new format directive
     */
    static FormatDirective of(String name, BiFunction<String, Locale, String> resolver) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(resolver, "resolver");
        return new FormatDirective() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String resolve(String args, Locale locale) {
                return resolver.apply(args, locale);
            }
        };
    }

    /**
     * Creates a simple directive with no arguments.
     *
     * <p>Example:</p>
     * <pre>{@code
     * FormatDirective.simple("appVersion", locale -> "2.1.0");
     * }</pre>
     *
     * @param name     the directive name
     * @param resolver the resolve function (locale) -&gt; result
     * @return a new format directive
     */
    static FormatDirective simple(String name, Function<Locale, String> resolver) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(resolver, "resolver");
        return new FormatDirective() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String resolve(String args, Locale locale) {
                return resolver.apply(locale);
            }
        };
    }
}
