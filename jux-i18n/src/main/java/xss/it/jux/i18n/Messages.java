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

import xss.it.jux.core.JuxMessages;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.Temporal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Central i18n service -- the primary API that JUX components use to access
 * translated text and locale-aware formatting.
 *
 * <p>This class is the main entry point for all internationalization tasks in
 * a JUX application. It provides:</p>
 * <ul>
 *   <li><b>Typed message bundles</b> -- {@link #get(Class)} returns a proxy
 *       implementing a {@code @MessageBundle} interface with translations
 *       resolved for the current request locale.</li>
 *   <li><b>Locale metadata</b> -- {@link #currentLocale()}, {@link #isRtl()},
 *       and {@link #availableLocales()} expose information about the active
 *       locale and the set of configured locales.</li>
 *   <li><b>Locale-aware formatting</b> -- {@link #formatDate(LocalDate)},
 *       {@link #formatNumber(Number)}, {@link #formatCurrency(Number, String)},
 *       and {@link #formatRelative(Temporal)} produce properly localized
 *       representations of dates, numbers, currencies, and relative times.</li>
 * </ul>
 *
 * <p><b>Per-request locale binding:</b> The {@code jux-server} module sets the
 * current locale at the beginning of each HTTP request via
 * {@link #setCurrentLocale(Locale)} and clears it after the response via
 * {@link #clearCurrentLocale()}. The locale is stored in a {@link ThreadLocal}
 * so that all components rendered during the same request share the same locale
 * without having to pass it explicitly.</p>
 *
 * <p><b>Usage example in a component:</b></p>
 * <pre>{@code
 * @Autowired private Messages messages;
 *
 * @Override
 * public Element render() {
 *     var t = messages.get(HomeMessages.class);
 *     return main_().children(
 *         h1().text(t.welcome()),
 *         p().text(t.greeting("World")),
 *         span().text(messages.formatCurrency(29.99, "USD"))
 *     );
 * }
 * }</pre>
 *
 * <p>This class has no Spring annotations itself so that it can be used and
 * tested without Spring. In a Spring Boot application the {@code jux-server}
 * auto-configuration registers it as a singleton bean.</p>
 *
 * @see MessageBundleRegistry
 * @see I18nProperties
 * @see RtlDetector
 */
public class Messages implements JuxMessages {

    /** Registry that holds all discovered bundle interfaces and their locale mappings. */
    private final MessageBundleRegistry registry;

    /** Configuration properties for the i18n subsystem. */
    private final I18nProperties properties;

    /**
     * Per-request locale holder. Set by {@link #setCurrentLocale(Locale)} at the
     * beginning of request processing and cleared by {@link #clearCurrentLocale()}
     * after the response has been sent. Using a {@link ThreadLocal} ensures that
     * concurrent requests on different threads do not interfere with each other.
     */
    private static final ThreadLocal<Locale> currentLocaleHolder = new ThreadLocal<>();

    /**
     * Creates a new {@code Messages} instance.
     *
     * @param registry   the bundle registry containing all discovered message bundles
     * @param properties the i18n configuration properties
     */
    public Messages(MessageBundleRegistry registry, I18nProperties properties) {
        this.registry = registry;
        this.properties = properties;
    }

    /**
     * Binds the given locale to the current thread for the duration of a request.
     *
     * <p>This method is called by the {@code jux-server} request processing
     * pipeline at the very beginning of each HTTP request, after the
     * {@link JuxLocaleResolver} has determined the locale. All subsequent calls
     * to {@link #currentLocale()}, {@link #get(Class)}, and the formatting
     * methods on the same thread will use this locale.</p>
     *
     * <p><b>Important:</b> Every call to this method must be paired with a
     * call to {@link #clearCurrentLocale()} (typically in a {@code finally}
     * block) to prevent locale leakage between requests on pooled threads.</p>
     *
     * @param locale the locale resolved for the current request; must not be {@code null}
     */
    public void setCurrentLocale(Locale locale) {
        currentLocaleHolder.set(locale);
    }

    /**
     * Removes the per-request locale binding from the current thread.
     *
     * <p>Called by the {@code jux-server} request processing pipeline after the
     * response has been sent. Failing to call this method can cause locale
     * leakage between requests when servlet container threads are reused.</p>
     */
    public void clearCurrentLocale() {
        currentLocaleHolder.remove();
    }

    /**
     * Returns a typed message bundle proxy for the current request locale.
     *
     * <p>The returned object implements the given {@code bundleType} interface.
     * Calling any of its methods returns the translated, formatted string for
     * the locale that was bound to this thread via {@link #setCurrentLocale(Locale)}.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * var t = messages.get(HomeMessages.class);
     * String text = t.welcome();           // "Bienvenido a nuestro sitio" (if locale is es)
     * String greeting = t.greeting("Ada"); // "Hola, Ada"
     * }</pre>
     *
     * @param bundleType the {@code @MessageBundle}-annotated interface class
     * @param <T>        the bundle interface type
     * @return a proxy implementing {@code T} with messages for the current locale;
     *         never {@code null}
     */
    public <T> T get(Class<T> bundleType) {
        return registry.getBundle(bundleType, currentLocale());
    }

    /**
     * Returns a typed message bundle proxy for an explicitly specified locale.
     *
     * <p>Use this overload when you need translations in a locale different from
     * the current request -- for example, when generating an email in the
     * recipient's language or when rendering a language-comparison page.</p>
     *
     * @param bundleType the {@code @MessageBundle}-annotated interface class
     * @param locale     the desired locale (does not affect the per-request locale)
     * @param <T>        the bundle interface type
     * @return a proxy implementing {@code T} with messages for {@code locale};
     *         never {@code null}
     */
    public <T> T get(Class<T> bundleType, Locale locale) {
        return registry.getBundle(bundleType, locale);
    }

    /**
     * Looks up a translated string from the configured {@link ResourceBundle}
     * for the current request locale.
     *
     * <p>This method requires {@link I18nProperties#getBaseName()} to be set
     * (e.g. {@code "lang"}), which causes property files like
     * {@code lang_en.properties} and {@code lang_es.properties} to be loaded
     * from the classpath.</p>
     *
     * <p>If the key is not found, the key itself is returned as a fallback.</p>
     *
     * @param key the message key (e.g. {@code "nav.home"})
     * @return the translated string, or the key itself if not found
     */
    @Override
    public String getString(String key) {
        String baseName = properties.getBaseName();
        if (baseName == null || baseName.isEmpty()) {
            return key;
        }
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(baseName, currentLocale());
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Looks up a translated string and formats it with the given arguments
     * using {@link MessageFormat}.
     *
     * <p>Example property: {@code greeting=Hello, {0}!}<br>
     * Usage: {@code messages.getString("greeting", "World")} → {@code "Hello, World!"}</p>
     *
     * @param key  the message key
     * @param args arguments to substitute into the message pattern
     * @return the formatted translated string, or the key itself if not found
     */
    @Override
    public String getString(String key, Object... args) {
        String pattern = getString(key);
        if (pattern.equals(key)) {
            return key;
        }
        return MessageFormat.format(pattern, args);
    }

    /**
     * Returns the locale currently bound to this thread.
     *
     * <p>If no locale has been set (e.g. outside of an HTTP request context),
     * the {@linkplain I18nProperties#getDefaultLocaleObj() default locale}
     * from the configuration is returned.</p>
     *
     * @return the current request locale, or the configured default; never {@code null}
     */
    @Override
    public Locale currentLocale() {
        Locale locale = currentLocaleHolder.get();
        return locale != null ? locale : properties.getDefaultLocaleObj();
    }

    /**
     * Indicates whether the current request locale uses a right-to-left script.
     *
     * <p>Components typically use this to set the {@code <html dir="rtl">}
     * attribute and to apply RTL-specific CSS classes. Delegates to
     * {@link RtlDetector#isRtl(Locale)}.</p>
     *
     * @return {@code true} if the current locale is RTL (e.g. Arabic, Hebrew,
     *         Persian, Urdu); {@code false} otherwise
     * @see RtlDetector#isRtl(Locale)
     */
    @Override
    public boolean isRtl() {
        return RtlDetector.isRtl(currentLocale());
    }

    /**
     * Returns all locales that the application is configured to support.
     *
     * <p>The list preserves the order declared in the
     * {@link I18nProperties#getLocales()} configuration. It is typically used
     * by language-switcher components to render links for each supported language.</p>
     *
     * @return an unmodifiable list of supported {@link Locale} instances;
     *         never {@code null} or empty (at minimum contains the default locale)
     */
    @Override
    public List<Locale> availableLocales() {
        return properties.getLocaleObjects();
    }

    /**
     * Formats a {@link LocalDate} using {@link FormatStyle#MEDIUM} for the current locale.
     *
     * <p>Output examples by locale:</p>
     * <ul>
     *   <li>{@code en}: "Feb 6, 2026"</li>
     *   <li>{@code es}: "6 feb 2026"</li>
     *   <li>{@code de}: "06.02.2026"</li>
     * </ul>
     *
     * @param date the date to format, or {@code null}
     * @return the formatted date string, or an empty string if {@code date} is {@code null}
     */
    public String formatDate(LocalDate date) {
        return formatDate(date, FormatStyle.MEDIUM);
    }

    /**
     * Formats a {@link LocalDate} using the specified {@link FormatStyle} for the
     * current locale.
     *
     * @param date  the date to format, or {@code null}
     * @param style the formatting style ({@link FormatStyle#SHORT},
     *              {@link FormatStyle#MEDIUM}, {@link FormatStyle#LONG},
     *              or {@link FormatStyle#FULL})
     * @return the formatted date string, or an empty string if {@code date} is {@code null}
     */
    public String formatDate(LocalDate date, FormatStyle style) {
        if (date == null) return "";
        return DateTimeFormatter.ofLocalizedDate(style).withLocale(currentLocale()).format(date);
    }

    /**
     * Formats a {@link LocalDateTime} (date and time) using {@link FormatStyle#MEDIUM}
     * for the current locale.
     *
     * <p>Output examples by locale:</p>
     * <ul>
     *   <li>{@code en}: "Feb 6, 2026, 3:45:00 PM"</li>
     *   <li>{@code de}: "06.02.2026, 15:45:00"</li>
     * </ul>
     *
     * @param dateTime the date-time to format, or {@code null}
     * @return the formatted date-time string, or an empty string if {@code dateTime}
     *         is {@code null}
     */
    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(currentLocale()).format(dateTime);
    }

    /**
     * Formats a number using the default grouping and decimal rules for the
     * current locale.
     *
     * <p>Output examples for the value {@code 1250.5}:</p>
     * <ul>
     *   <li>{@code en}: "1,250.5"</li>
     *   <li>{@code de}: "1.250,5"</li>
     *   <li>{@code fr}: "1 250,5"</li>
     * </ul>
     *
     * @param number the number to format, or {@code null}
     * @return the formatted number string, or an empty string if {@code number}
     *         is {@code null}
     */
    public String formatNumber(Number number) {
        if (number == null) return "";
        return NumberFormat.getNumberInstance(currentLocale()).format(number);
    }

    /**
     * Formats a number with a fixed number of decimal places for the current locale.
     *
     * <p>Both the minimum and maximum fraction digits are set to {@code decimals},
     * ensuring consistent output length. Locale-specific grouping separators and
     * decimal separators are applied.</p>
     *
     * @param number   the number to format, or {@code null}
     * @param decimals the exact number of decimal places to show (e.g. {@code 2}
     *                 for monetary amounts)
     * @return the formatted number string, or an empty string if {@code number}
     *         is {@code null}
     */
    public String formatNumber(Number number, int decimals) {
        if (number == null) return "";
        NumberFormat fmt = NumberFormat.getNumberInstance(currentLocale());
        fmt.setMinimumFractionDigits(decimals);
        fmt.setMaximumFractionDigits(decimals);
        return fmt.format(number);
    }

    /**
     * Formats a monetary amount with the given ISO 4217 currency code for the
     * current locale.
     *
     * <p>The locale determines the symbol position, grouping separator, and
     * decimal separator, while the {@code currencyCode} determines the currency
     * symbol. Output examples for {@code 1299.99} with currency {@code "USD"}:</p>
     * <ul>
     *   <li>{@code en}: "$1,299.99"</li>
     *   <li>{@code de}: "1.299,99 $"</li>
     *   <li>{@code ja}: "$1,299.99"</li>
     * </ul>
     *
     * @param amount       the monetary amount to format, or {@code null}
     * @param currencyCode ISO 4217 currency code (e.g. {@code "USD"}, {@code "EUR"},
     *                     {@code "GBP"})
     * @return the formatted currency string, or an empty string if {@code amount}
     *         is {@code null}
     * @throws IllegalArgumentException if {@code currencyCode} is not a valid
     *                                  ISO 4217 code
     */
    public String formatCurrency(Number amount, String currencyCode) {
        if (amount == null) return "";
        NumberFormat fmt = NumberFormat.getCurrencyInstance(currentLocale());
        fmt.setCurrency(Currency.getInstance(currencyCode));
        return fmt.format(amount);
    }

    /**
     * Formats a temporal value as a human-readable relative time string such as
     * "3 hours ago" or "in 2 days".
     *
     * <p>This is a simplified, English-only implementation intended for quick
     * prototyping. For full ICU-level relative-time formatting with proper
     * locale-aware pluralization and translations, consider integrating an
     * external library (e.g. ICU4J {@code RelativeDateTimeFormatter}).</p>
     *
     * <p><b>Supported input types:</b></p>
     * <ul>
     *   <li>{@link LocalDate} -- converted to start-of-day for comparison.</li>
     *   <li>{@link LocalDateTime} -- used directly.</li>
     *   <li>{@link Instant} -- converted to {@link LocalDateTime} using the
     *       {@linkplain ZoneId#systemDefault() system default time zone}.</li>
     *   <li>Any other {@link Temporal} subtype -- falls back to {@code toString()}.</li>
     * </ul>
     *
     * <p><b>Time buckets:</b></p>
     * <ul>
     *   <li>&lt; 60 seconds: "just now" / "in a moment"</li>
     *   <li>&lt; 60 minutes: "{n} minute(s) ago" / "in {n} minute(s)"</li>
     *   <li>&lt; 24 hours: "{n} hour(s) ago" / "in {n} hour(s)"</li>
     *   <li>&lt; 30 days: "{n} day(s) ago" / "in {n} day(s)"</li>
     *   <li>&lt; 365 days: "{n} month(s) ago" / "in {n} month(s)"</li>
     *   <li>&ge; 365 days: "{n} year(s) ago" / "in {n} year(s)"</li>
     * </ul>
     *
     * @param temporal the point in time to express relative to now, or {@code null}
     * @return a relative time string in English, or an empty string if
     *         {@code temporal} is {@code null}
     */
    public String formatRelative(Temporal temporal) {
        if (temporal == null) return "";

        // Normalize the input to LocalDateTime for uniform Duration computation
        LocalDateTime target;
        if (temporal instanceof LocalDate ld) {
            target = ld.atStartOfDay();
        } else if (temporal instanceof LocalDateTime ldt) {
            target = ldt;
        } else if (temporal instanceof Instant inst) {
            target = LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        } else {
            // Unsupported Temporal subtype -- fall back to its toString()
            return temporal.toString();
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(target, now);
        long seconds = duration.getSeconds();
        boolean past = seconds >= 0;  // positive means target is in the past
        long abs = Math.abs(seconds);

        String unit;
        long value;

        // Select the appropriate time bucket based on the absolute difference
        if (abs < 60) {
            return past ? "just now" : "in a moment";
        } else if (abs < 3600) {           // < 1 hour
            value = abs / 60;
            unit = value == 1 ? "minute" : "minutes";
        } else if (abs < 86400) {           // < 1 day  (86400 = 24 * 3600)
            value = abs / 3600;
            unit = value == 1 ? "hour" : "hours";
        } else if (abs < 2592000) {         // < 30 days (2592000 = 30 * 86400)
            value = abs / 86400;
            unit = value == 1 ? "day" : "days";
        } else if (abs < 31536000) {        // < 365 days (31536000 = 365 * 86400)
            value = abs / 2592000;
            unit = value == 1 ? "month" : "months";
        } else {                            // >= 365 days
            value = abs / 31536000;
            unit = value == 1 ? "year" : "years";
        }

        // Build the final string: "3 hours ago" or "in 3 hours"
        return past ? value + " " + unit + " ago" : "in " + value + " " + unit;
    }
}
