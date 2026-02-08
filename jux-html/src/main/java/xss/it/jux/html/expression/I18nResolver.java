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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code #{key}} and {@code #{key(param1, param2)}} i18n expressions
 * in template text and attribute values.
 *
 * <p>The resolver delegates actual message lookup to a {@link MessageSource}
 * functional interface, decoupling this module from any specific i18n
 * implementation. The message source can be backed by JUX's i18n module,
 * Spring's MessageSource, Java ResourceBundles, or any custom provider.</p>
 *
 * <p><b>Expression syntax:</b></p>
 * <ul>
 *   <li>{@code #{welcome}} -- simple key lookup, no parameters</li>
 *   <li>{@code #{greeting(World)}} -- key with a single string parameter</li>
 *   <li>{@code #{item.count(5)}} -- key with a numeric parameter</li>
 *   <li>{@code #{range(1, 10, items)}} -- key with multiple parameters</li>
 * </ul>
 *
 * <p>When {@code strict} mode is enabled, unresolved keys throw an exception.
 * In non-strict mode (default for production), unresolved keys are returned
 * as-is (e.g. {@code #{unknown.key}} stays in the output).</p>
 *
 * @see ExpressionResolver
 */
public class I18nResolver {

    /**
     * Functional interface for i18n message lookup.
     *
     * <p>Implementations should return the translated message for the given key
     * and locale, with parameter substitution applied. Return {@code null} if
     * the key is not found.</p>
     */
    @FunctionalInterface
    public interface MessageSource {

        /**
         * Look up a translated message by key.
         *
         * @param key    the message key (e.g. "welcome", "greeting", "item.count")
         * @param params parameters to substitute into the message pattern
         * @return the resolved message, or null if the key is not found
         */
        String getMessage(String key, Object... params);
    }

    /**
     * Pattern matching {@code #{...}} expressions.
     * Captures the content between #{ and }, which can be a simple key or key(params).
     */
    private static final Pattern I18N_PATTERN = Pattern.compile("#\\{([^}]+)}");

    /**
     * Pattern to parse a key with optional parameters: {@code key(param1, param2)}.
     */
    private static final Pattern KEY_WITH_PARAMS = Pattern.compile("^([^(]+)\\((.*)\\)$");

    private final MessageSource messageSource;
    private final boolean strict;

    /**
     * Create a new i18n resolver.
     *
     * @param messageSource the message lookup implementation
     * @param strict        if true, unresolved keys throw an exception;
     *                      if false, unresolved expressions are left as-is
     * @throws NullPointerException if messageSource is null
     */
    public I18nResolver(MessageSource messageSource, boolean strict) {
        if (messageSource == null) {
            throw new NullPointerException("messageSource must not be null");
        }
        this.messageSource = messageSource;
        this.strict = strict;
    }

    /**
     * Resolve a single {@code #{...}} expression (without the surrounding delimiters).
     *
     * <p>The input should be the content inside #{...}, for example:
     * {@code "welcome"} or {@code "greeting(World)"}.</p>
     *
     * @param expression the expression content (key or key with params)
     * @return the resolved message, or the original expression marker if unresolved
     *         and not in strict mode
     * @throws IllegalArgumentException if strict mode is enabled and the key is not found
     */
    public String resolve(String expression) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        String key;
        Object[] params;

        Matcher paramMatcher = KEY_WITH_PARAMS.matcher(expression.trim());
        if (paramMatcher.matches()) {
            key = paramMatcher.group(1).trim();
            String paramsStr = paramMatcher.group(2).trim();
            params = parseParams(paramsStr);
        } else {
            key = expression.trim();
            params = new Object[0];
        }

        String result = messageSource.getMessage(key, params);

        if (result == null) {
            if (strict) {
                throw new IllegalArgumentException("Unresolved i18n key: " + key);
            }
            return "#{" + expression + "}";
        }

        return result;
    }

    /**
     * Resolve all {@code #{...}} expressions in the given text.
     *
     * <p>Scans the text for all occurrences of {@code #{...}} and replaces each
     * with the resolved message. Text outside of expressions is preserved as-is.</p>
     *
     * @param text the text containing i18n expressions
     * @return the text with all expressions resolved
     */
    public String resolveAll(String text) {
        if (text == null || text.isEmpty() || !text.contains("#{")) {
            return text;
        }

        Matcher matcher = I18N_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1);
            String resolved = resolve(expression);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Parse a comma-separated parameter string into an array of typed objects.
     *
     * <p>Each parameter is trimmed and then type-coerced:</p>
     * <ul>
     *   <li>Quoted strings ({@code 'hello'} or {@code "hello"}) become String</li>
     *   <li>Integer-like values become Integer</li>
     *   <li>Decimal-like values become Double</li>
     *   <li>Everything else becomes String</li>
     * </ul>
     *
     * @param paramsStr the comma-separated parameter string
     * @return array of parsed parameter values
     */
    private Object[] parseParams(String paramsStr) {
        if (paramsStr == null || paramsStr.isEmpty()) {
            return new Object[0];
        }

        List<Object> params = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < paramsStr.length(); i++) {
            char ch = paramsStr.charAt(i);

            if (inQuote) {
                if (ch == quoteChar) {
                    inQuote = false;
                }
                current.append(ch);
            } else if (ch == '\'' || ch == '"') {
                inQuote = true;
                quoteChar = ch;
                current.append(ch);
            } else if (ch == '(') {
                depth++;
                current.append(ch);
            } else if (ch == ')') {
                depth--;
                current.append(ch);
            } else if (ch == ',' && depth == 0) {
                params.add(coerceParam(current.toString().trim()));
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        /* Add the last parameter. */
        String lastParam = current.toString().trim();
        if (!lastParam.isEmpty()) {
            params.add(coerceParam(lastParam));
        }

        return params.toArray();
    }

    /**
     * Coerce a raw parameter string to a typed value.
     *
     * @param raw the raw parameter text
     * @return the coerced value (String, Integer, Double, or Boolean)
     */
    private Object coerceParam(String raw) {
        if (raw.isEmpty()) {
            return "";
        }

        /* Strip quotes if present. */
        if ((raw.startsWith("'") && raw.endsWith("'"))
                || (raw.startsWith("\"") && raw.endsWith("\""))) {
            return raw.substring(1, raw.length() - 1);
        }

        /* Try boolean. */
        if ("true".equalsIgnoreCase(raw)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return Boolean.FALSE;
        }

        /* Try integer. */
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            /* Not an integer. */
        }

        /* Try double. */
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
            /* Not a double. */
        }

        /* Default to string. */
        return raw;
    }
}
