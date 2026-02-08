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

import xss.it.jux.core.Element;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Orchestrates resolution of all expression types in template text and Element trees.
 *
 * <p>This resolver handles two expression syntaxes:</p>
 * <ul>
 *   <li>{@code #{key}} and {@code #{key(params)}} -- i18n message expressions,
 *       delegated to {@link I18nResolver}</li>
 *   <li>{@code @{directive}} and {@code @{directive(args)}} -- format expressions,
 *       delegated to {@link FormatResolver}</li>
 * </ul>
 *
 * <p>Expressions can appear in:</p>
 * <ul>
 *   <li>Element text content</li>
 *   <li>Element attribute values</li>
 * </ul>
 *
 * <p>The {@link #resolveTree(Element)} method walks an entire Element tree and resolves
 * all expressions in all text content and attribute values, mutating the tree in place.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * // Template contains:  <h1>#{welcome}</h1>  <html lang="@{lang}" dir="@{dir}">
 *
 * var i18n = new I18nResolver(myMessageSource, false);
 * var format = new FormatResolver(Locale.US);
 * var resolver = new ExpressionResolver(i18n, format);
 *
 * Element tree = HtmlParser.parse(template);
 * resolver.resolveTree(tree);
 * // Now <h1> contains "Welcome!" and <html> has lang="en" dir="ltr"
 * }</pre>
 *
 * @see I18nResolver
 * @see FormatResolver
 */
public class ExpressionResolver {

    private final I18nResolver i18nResolver;
    private final FormatResolver formatResolver;

    /**
     * Create a new expression resolver with both i18n and format resolvers.
     *
     * @param i18nResolver   the i18n expression resolver for #{...} expressions;
     *                       may be null to skip i18n resolution
     * @param formatResolver the format expression resolver for @{...} expressions;
     *                       may be null to skip format resolution
     */
    public ExpressionResolver(I18nResolver i18nResolver, FormatResolver formatResolver) {
        this.i18nResolver = i18nResolver;
        this.formatResolver = formatResolver;
    }

    /**
     * Resolve all {@code #{...}} and {@code @{...}} expressions in the given string.
     *
     * <p>Both i18n and format expressions are resolved in a single pass.
     * I18n expressions are resolved first, followed by format expressions.
     * This ordering allows i18n messages to contain format expressions that
     * are subsequently resolved.</p>
     *
     * @param text the text containing expressions
     * @return the text with all expressions resolved
     */
    public String resolve(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        /* Resolve i18n expressions first (#{...}). */
        if (i18nResolver != null && result.contains("#{")) {
            result = i18nResolver.resolveAll(result);
        }

        /* Then resolve format expressions (@{...}). */
        if (formatResolver != null && result.contains("@{")) {
            result = formatResolver.resolveAll(result);
        }

        return result;
    }

    /**
     * Resolve all expressions in all text content and attribute values of an Element tree.
     *
     * <p>Walks the entire tree iteratively (breadth-first) and resolves expressions
     * in:</p>
     * <ul>
     *   <li>Each element's text content (via {@link Element#text(String)})</li>
     *   <li>Each element's attribute values</li>
     * </ul>
     *
     * <p>The tree is mutated in place. Since Element's attribute accessors return
     * unmodifiable views, the resolver recreates elements with resolved values
     * by applying resolved attributes via the builder API.</p>
     *
     * <p><b>Note:</b> This method modifies the Element tree in place where possible.
     * Because Element uses a mutable builder pattern during construction, text content
     * is updated directly. For attributes, since the internal map is accessed through
     * the public API, the resolver reads current attributes, resolves expressions,
     * and re-applies changed values.</p>
     *
     * @param root the root element of the tree to process
     * @return the same root element, with all expressions in the tree resolved
     */
    public Element resolveTree(Element root) {
        if (root == null) {
            return null;
        }

        Deque<Element> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Element current = stack.pop();

            /* Resolve text content. */
            String textContent = current.getTextContent();
            if (textContent != null && containsExpression(textContent)) {
                current.text(resolve(textContent));
            }

            /* Resolve attribute values. */
            Map<String, String> attrs = current.getAttributes();
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                /*
                 * Skip class and style attributes -- they are computed from internal
                 * structures (cssClasses list and styles map). Resolving them through
                 * attr() would create duplicates. If expressions are needed in class
                 * or style values, they should be set programmatically.
                 */
                if ("class".equals(key) || "style".equals(key)) {
                    continue;
                }

                if (value != null && containsExpression(value)) {
                    String resolved = resolve(value);
                    current.attr(key, resolved);
                }
            }

            /* Push children onto the stack for processing. */
            for (Element child : current.getChildren()) {
                stack.push(child);
            }
        }

        return root;
    }

    /**
     * Quick check whether a string contains any expression markers.
     *
     * @param text the text to check
     * @return true if the text contains #{...} or @{...} markers
     */
    private boolean containsExpression(String text) {
        return text.contains("#{") || text.contains("@{");
    }
}
