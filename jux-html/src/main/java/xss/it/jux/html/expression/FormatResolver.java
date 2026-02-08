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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code @{directive}} formatting expressions in template text
 * and attribute values.
 *
 * <p>Format expressions provide locale-aware output for common formatting needs
 * without requiring Java code. They are evaluated at template processing time
 * using the configured locale.</p>
 *
 * <h2>Built-in directives</h2>
 *
 * <p>The following directives are available out of the box:</p>
 *
 * <table>
 *   <caption>Format expression directives</caption>
 *   <tr><th>Expression</th><th>Output (en-US)</th><th>Description</th></tr>
 *   <tr><td>{@code @{dir}}</td><td>{@code ltr}</td><td>Text direction from locale</td></tr>
 *   <tr><td>{@code @{lang}}</td><td>{@code en}</td><td>Language code from locale</td></tr>
 *   <tr><td>{@code @{locale}}</td><td>{@code en-US}</td><td>Full BCP 47 language tag</td></tr>
 *   <tr><td>{@code @{country}}</td><td>{@code US}</td><td>Country code from locale</td></tr>
 *   <tr><td>{@code @{displayLang}}</td><td>{@code English}</td><td>Localized language name</td></tr>
 *   <tr><td>{@code @{displayCountry}}</td><td>{@code United States}</td><td>Localized country name</td></tr>
 *   <tr><td>{@code @{currency(29.99)}}</td><td>{@code $29.99}</td><td>Formatted currency</td></tr>
 *   <tr><td>{@code @{date(yyyy-MM-dd)}}</td><td>{@code 2026-02-08}</td><td>Formatted current date</td></tr>
 *   <tr><td>{@code @{number(1250.5, 2)}}</td><td>{@code 1,250.50}</td><td>Formatted number with decimals</td></tr>
 * </table>
 *
 * <h2>Custom directives</h2>
 *
 * <p>Applications can register custom directives by declaring
 * {@link FormatDirective} beans in a Spring {@code @Configuration} class.
 * Custom directives are checked <b>before</b> built-in ones, so they can
 * override default behavior if needed.</p>
 *
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
 * @see FormatDirective
 * @see ExpressionResolver
 */
public class FormatResolver {

    // ── Constants ─────────────────────────────────────────────────

    /**
     * Regex pattern matching {@code @{...}} expressions in template text.
     *
     * <p>The capturing group extracts the content between {@code @&#123;}
     * and {@code &#125;} — that content is then passed to
     * {@link #resolve(String)} for directive lookup and evaluation.</p>
     *
     * <p>Example matches:</p>
     * <ul>
     *   <li>{@code @{dir}} → captures "dir"</li>
     *   <li>{@code @{currency(29.99)}} → captures "currency(29.99)"</li>
     * </ul>
     */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("@\\{([^}]+)}");

    /**
     * Regex pattern to decompose a directive with parenthesized arguments.
     *
     * <p>Group 1 is the directive name (one or more word characters),
     * and group 2 is everything inside the parentheses (the raw
     * argument string, which may contain commas).</p>
     *
     * <p>Example: {@code "currency(29.99)"} →
     * group(1)={@code "currency"}, group(2)={@code "29.99"}.</p>
     */
    private static final Pattern DIRECTIVE_WITH_ARGS =
            Pattern.compile("^(\\w+)\\((.*)\\)$");

    /**
     * Right-to-left language codes per IANA/BCP 47.
     *
     * <p>These languages are written right-to-left and are used by
     * the {@code @{dir}} directive to return {@code "rtl"} instead
     * of {@code "ltr"}. The set covers Arabic, Hebrew, Farsi, Urdu,
     * Pashto, Sindhi, Yiddish, Kurdish (Sorani), Uyghur, Divehi,
     * Hausa (Ajami), Khowar, Kashmiri, Syriac, Aramaic, and N'Ko.</p>
     */
    private static final Set<String> RTL_LANGUAGES = Set.of(
            "ar", "he", "fa", "ur", "ps", "sd", "yi", "ku", "ug",
            "dv", "ha", "khw", "ks", "syr", "arc", "nqo"
    );

    // ── Instance fields ──────────────────────────────────────────

    /**
     * The locale used for all locale-sensitive formatting operations
     * (currency symbols, date patterns, number grouping separators,
     * language names, text direction detection, etc.).
     */
    private final Locale locale;

    /**
     * Custom format directives keyed by their <b>lowercase</b> name.
     *
     * <p>These directives are registered by the application (typically
     * as Spring beans) and are checked <em>before</em> the built-in
     * directives during resolution. This allows applications to extend
     * the expression language with domain-specific formatters or to
     * override built-in behavior entirely.</p>
     *
     * <p>The map preserves insertion order ({@link LinkedHashMap}) and
     * is wrapped in an unmodifiable view to prevent accidental mutation
     * after construction.</p>
     */
    private final Map<String, FormatDirective> customDirectives;

    // ── Constructors ─────────────────────────────────────────────

    /**
     * Creates a new format resolver with the given locale and no
     * custom directives.
     *
     * <p>This constructor is equivalent to calling
     * {@link #FormatResolver(Locale, Collection)} with an empty
     * collection. Only the built-in directives will be available.</p>
     *
     * @param locale the locale to use for formatting; must not be null
     * @throws NullPointerException if {@code locale} is null
     */
    public FormatResolver(Locale locale) {
        this(locale, Collections.emptyList());
    }

    /**
     * Creates a new format resolver with the given locale and a
     * collection of custom directives.
     *
     * <p>Custom directives are indexed by their
     * {@linkplain FormatDirective#name() name} (lowercased for
     * case-insensitive matching). If two directives share the same
     * name (case-insensitively), the <b>last one</b> in iteration
     * order wins.</p>
     *
     * <p>Custom directives are always checked <b>before</b> built-in
     * directives, so they can override defaults. For example, a custom
     * directive named {@code "currency"} would completely replace the
     * built-in currency formatting.</p>
     *
     * @param locale           the locale to use for formatting; must not be null
     * @param customDirectives the custom directives to register; must not be null
     *                         (but may be empty)
     * @throws NullPointerException if {@code locale} or {@code customDirectives}
     *                              is null, or if any element in the collection
     *                              is null
     */
    public FormatResolver(Locale locale, Collection<FormatDirective> customDirectives) {
        Objects.requireNonNull(locale, "locale must not be null");
        Objects.requireNonNull(customDirectives, "customDirectives must not be null");
        this.locale = locale;

        /*
         * Build an unmodifiable lookup map keyed by lowercase directive name.
         * LinkedHashMap preserves insertion order, which is useful for
         * debugging and predictable iteration. The Collections.unmodifiableMap
         * wrapper prevents accidental mutation after construction.
         */
        Map<String, FormatDirective> map = new LinkedHashMap<>();
        for (FormatDirective directive : customDirectives) {
            Objects.requireNonNull(directive, "custom directive must not be null");
            map.put(directive.name().toLowerCase(), directive);
        }
        this.customDirectives = Collections.unmodifiableMap(map);
    }

    // ── Public API ───────────────────────────────────────────────

    /**
     * Resolve a single {@code @{...}} expression (without the
     * surrounding {@code @&#123;} and {@code &#125;} delimiters).
     *
     * <p>The input should be the raw content captured from inside the
     * {@code @{...}} markers. For example:</p>
     * <ul>
     *   <li>{@code "dir"} — simple directive, no arguments</li>
     *   <li>{@code "lang"} — simple directive, no arguments</li>
     *   <li>{@code "currency(29.99)"} — directive with arguments</li>
     *   <li>{@code "myCustom(foo, bar)"} — custom directive with arguments</li>
     * </ul>
     *
     * <p><b>Resolution order:</b></p>
     * <ol>
     *   <li>Custom directives (registered via constructor) — checked first</li>
     *   <li>Built-in directives (dir, lang, currency, date, number, etc.)</li>
     * </ol>
     *
     * @param expression the expression content (directive name or
     *                   directive with parenthesized arguments)
     * @return the resolved formatted value, never null
     * @throws IllegalArgumentException if the directive is not recognized
     *                                  by any custom or built-in handler
     */
    public String resolve(String expression) {
        /* Null or empty expressions resolve to an empty string. */
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        String trimmed = expression.trim();

        /*
         * Attempt to parse as a directive with arguments — e.g.
         * "currency(29.99)" → directive="currency", args="29.99".
         */
        Matcher argMatcher = DIRECTIVE_WITH_ARGS.matcher(trimmed);
        if (argMatcher.matches()) {
            String directive = argMatcher.group(1).toLowerCase();
            String args = argMatcher.group(2).trim();
            return resolveDirectiveWithArgs(directive, args);
        }

        /*
         * No parentheses found — treat as a simple directive
         * (no arguments). Lowercase for case-insensitive matching.
         */
        return resolveSimpleDirective(trimmed.toLowerCase());
    }

    /**
     * Resolve all {@code @{...}} expressions in the given text.
     *
     * <p>Scans the text for every occurrence of the {@code @{...}}
     * pattern and replaces each one with the resolved formatted value.
     * Text outside of expressions is preserved verbatim. If the text
     * is {@code null}, empty, or contains no {@code @{} markers, it
     * is returned as-is for maximum efficiency.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Given locale en-US:
     * resolveAll("Direction: @{dir}, Language: @{lang}")
     * // Returns: "Direction: ltr, Language: en"
     * }</pre>
     *
     * @param text the text containing zero or more format expressions
     * @return the text with all expressions replaced by their resolved
     *         values; returns the original text unchanged if it contains
     *         no expressions
     */
    public String resolveAll(String text) {
        /*
         * Fast-path: skip regex scanning entirely when the text
         * cannot possibly contain any format expressions.
         */
        if (text == null || text.isEmpty() || !text.contains("@{")) {
            return text;
        }

        Matcher matcher = FORMAT_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        /*
         * For each match, resolve the captured expression and append
         * the result into the output buffer. Matcher.quoteReplacement
         * is used to escape any '$' or '\' characters in the resolved
         * value, which would otherwise be interpreted as replacement
         * group references by appendReplacement.
         */
        while (matcher.find()) {
            String expression = matcher.group(1);
            String resolved = resolve(expression);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }

        /* Append any trailing text after the last match. */
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Returns the locale used by this resolver for all formatting
     * operations.
     *
     * @return the locale, never null
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns an unmodifiable view of the custom directives registered
     * with this resolver, keyed by their lowercase name.
     *
     * <p>This is useful for introspection, debugging, and testing.
     * Built-in directives are not included in this map.</p>
     *
     * @return an unmodifiable map of lowercase directive name to
     *         {@link FormatDirective} instance; never null, may be empty
     */
    public Map<String, FormatDirective> getCustomDirectives() {
        return customDirectives;
    }

    // ── Directive resolution ─────────────────────────────────────

    /**
     * Resolve a simple directive (no parenthesized arguments).
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li><b>Custom directives</b> — looked up by lowercase name in
     *       the {@link #customDirectives} map. If found, the directive's
     *       {@link FormatDirective#resolve(String, Locale)} method is
     *       called with a {@code null} argument string.</li>
     *   <li><b>Built-in directives</b> — matched by the switch
     *       expression below (dir, lang, locale, country, displayLang,
     *       displayCountry).</li>
     * </ol>
     *
     * @param directive the directive name, already lowercased
     * @return the resolved string value
     * @throws IllegalArgumentException if no custom or built-in
     *                                  directive matches the name
     */
    private String resolveSimpleDirective(String directive) {
        /*
         * Check custom directives first. This allows applications
         * to override built-in directives (e.g. replace "dir" with
         * a custom implementation) or add entirely new ones.
         */
        FormatDirective custom = customDirectives.get(directive);
        if (custom != null) {
            return custom.resolve(null, locale);
        }

        /* Fall back to built-in directive handling. */
        return switch (directive) {
            case "dir" -> isRtl() ? "rtl" : "ltr";
            case "lang" -> locale.getLanguage();
            case "locale" -> locale.toLanguageTag();
            case "country" -> locale.getCountry();
            case "displayLang", "displaylang" -> locale.getDisplayLanguage(locale);
            case "displayCountry", "displaycountry" -> locale.getDisplayCountry(locale);
            default -> throw new IllegalArgumentException(
                    "Unknown format directive: @{" + directive + "}. "
                            + "Supported: dir, lang, locale, country, displayLang, displayCountry, "
                            + "currency(amount), date(pattern), number(value, decimals)"
                            + (customDirectives.isEmpty() ? ""
                            : ". Custom: " + String.join(", ", customDirectives.keySet())));
        };
    }

    /**
     * Resolve a directive that has parenthesized arguments.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li><b>Custom directives</b> — looked up by lowercase name in
     *       the {@link #customDirectives} map. If found, the directive's
     *       {@link FormatDirective#resolve(String, Locale)} method is
     *       called with the raw argument string.</li>
     *   <li><b>Built-in directives</b> — matched by the switch
     *       expression below (currency, date, number).</li>
     * </ol>
     *
     * @param directive the directive name, already lowercased
     * @param args      the raw argument string from inside the
     *                  parentheses (e.g. {@code "29.99"} for
     *                  {@code currency(29.99)})
     * @return the resolved string value
     * @throws IllegalArgumentException if no custom or built-in
     *                                  directive matches the name
     */
    private String resolveDirectiveWithArgs(String directive, String args) {
        /*
         * Check custom directives first. This allows applications
         * to add new directives with arguments (e.g. "percent(75)")
         * or to replace built-in ones (e.g. custom "currency" logic).
         */
        FormatDirective custom = customDirectives.get(directive);
        if (custom != null) {
            return custom.resolve(args, locale);
        }

        /* Fall back to built-in directive handling. */
        return switch (directive) {
            case "currency" -> resolveCurrency(args);
            case "date" -> resolveDate(args);
            case "number" -> resolveNumber(args);
            default -> throw new IllegalArgumentException(
                    "Unknown format directive: @{" + directive + "(...)}. "
                            + "Supported directives with arguments: currency, date, number"
                            + (customDirectives.isEmpty() ? ""
                            : ". Custom: " + String.join(", ", customDirectives.keySet())));
        };
    }

    // ── Built-in directive implementations ───────────────────────

    /**
     * Resolve the built-in {@code currency(amount)} directive.
     *
     * <p>Formats the given numeric amount as a locale-specific currency
     * string. The currency symbol and formatting rules are determined
     * by the resolver's locale. If the locale has an associated country
     * code, the corresponding {@link Currency} is used; otherwise, the
     * JVM's default currency format for the locale is applied.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>{@code @{currency(29.99)}} with en-US → {@code $29.99}</li>
     *   <li>{@code @{currency(29.99)}} with de-DE → {@code 29,99 €}</li>
     *   <li>{@code @{currency(1500)}} with ja-JP → {@code ￥1,500}</li>
     * </ul>
     *
     * @param args the amount as a numeric string (e.g. {@code "29.99"})
     * @return the formatted currency string
     * @throws IllegalArgumentException if {@code args} is not a valid number
     */
    private String resolveCurrency(String args) {
        String trimmed = args.trim();
        try {
            double amount = Double.parseDouble(trimmed);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);

            /*
             * Attempt to resolve the currency from the locale's country
             * code. For example, Locale("en", "US") → Currency "USD",
             * Locale("de", "DE") → Currency "EUR". If the locale has no
             * country or the country has no associated currency, we
             * silently fall back to the default currency format.
             */
            try {
                String country = locale.getCountry();
                if (country != null && !country.isEmpty()) {
                    Currency currency = Currency.getInstance(locale);
                    currencyFormat.setCurrency(currency);
                }
            } catch (IllegalArgumentException ignored) {
                /* No currency for this locale — use default format. */
            }

            return currencyFormat.format(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number in @{currency(" + trimmed + ")}: " + e.getMessage());
        }
    }

    /**
     * Resolve the built-in {@code date(pattern)} directive.
     *
     * <p>Formats the current date (and optionally time) using the given
     * {@link DateTimeFormatter} pattern. The pattern follows the standard
     * {@link java.time.format.DateTimeFormatter} syntax. The locale is
     * used for locale-sensitive pattern letters (month names, day names,
     * AM/PM markers, etc.).</p>
     *
     * <p>The resolver first attempts to format using
     * {@link LocalDateTime#now()} (which includes time fields). If the
     * pattern contains only date fields and the formatter throws, it
     * falls back to {@link LocalDate#now()} automatically.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>{@code @{date(yyyy-MM-dd)}} → {@code 2026-02-08}</li>
     *   <li>{@code @{date(MMMM d, yyyy)}} with en-US → {@code February 8, 2026}</li>
     *   <li>{@code @{date(d 'de' MMMM 'de' yyyy)}} with es → {@code 8 de febrero de 2026}</li>
     *   <li>{@code @{date(HH:mm)}} → {@code 14:30} (current time)</li>
     * </ul>
     *
     * @param args the {@link DateTimeFormatter} pattern string
     * @return the formatted date/time string
     * @throws IllegalArgumentException if the pattern is empty or invalid
     */
    private String resolveDate(String args) {
        String pattern = args.trim();
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException(
                    "Empty pattern in @{date()}. Provide a DateTimeFormatter pattern.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);

            /*
             * Try formatting with LocalDateTime first (supports both
             * date and time pattern letters). If the pattern only uses
             * date fields but the formatter still works with LocalDateTime,
             * this succeeds. If it doesn't (unlikely but defensive), fall
             * back to date-only formatting.
             */
            try {
                return LocalDateTime.now().format(formatter);
            } catch (Exception ignored) {
                return LocalDate.now().format(formatter);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid date pattern in @{date(" + pattern + ")}: " + e.getMessage());
        }
    }

    /**
     * Resolve the built-in {@code number(value, decimals)} directive.
     *
     * <p>Formats a numeric value with locale-specific grouping separators
     * and decimal separators, using the specified number of decimal
     * places. If the decimal count is omitted, zero decimal places are
     * used (integer formatting).</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>{@code @{number(1250.5, 2)}} with en-US → {@code 1,250.50}</li>
     *   <li>{@code @{number(1250.5, 2)}} with de-DE → {@code 1.250,50}</li>
     *   <li>{@code @{number(1250.5, 0)}} with en-US → {@code 1,251}</li>
     *   <li>{@code @{number(42)}} → {@code 42} (zero decimals by default)</li>
     * </ul>
     *
     * @param args the value and optional decimal count, comma-separated
     *             (e.g. {@code "1250.5, 2"} or just {@code "42"})
     * @return the formatted number string
     * @throws IllegalArgumentException if the value is missing or not a
     *                                  valid number
     */
    private String resolveNumber(String args) {
        String[] parts = args.split(",");
        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Missing value in @{number()}.");
        }

        try {
            double value = Double.parseDouble(parts[0].trim());
            int decimals = 0;

            /*
             * If a second comma-separated argument is present, parse
             * it as the number of decimal places. An empty second
             * argument is silently ignored (defaults to 0 decimals).
             */
            if (parts.length >= 2) {
                String decStr = parts[1].trim();
                if (!decStr.isEmpty()) {
                    decimals = Integer.parseInt(decStr);
                }
            }

            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            numberFormat.setMinimumFractionDigits(decimals);
            numberFormat.setMaximumFractionDigits(decimals);

            return numberFormat.format(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number in @{number(" + args + ")}: " + e.getMessage());
        }
    }

    // ── RTL detection ────────────────────────────────────────────

    /**
     * Determines whether the current locale's language is a
     * right-to-left (RTL) language.
     *
     * <p>This method checks the locale's ISO 639-1 language code
     * against the {@link #RTL_LANGUAGES} set. It is used by the
     * built-in {@code @{dir}} directive to return {@code "rtl"} or
     * {@code "ltr"} as appropriate.</p>
     *
     * @return {@code true} if the locale's language is RTL
     *         (Arabic, Hebrew, Farsi, Urdu, etc.); {@code false}
     *         otherwise
     */
    private boolean isRtl() {
        return RTL_LANGUAGES.contains(locale.getLanguage());
    }
}
