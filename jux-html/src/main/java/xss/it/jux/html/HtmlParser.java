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

package xss.it.jux.html;

import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses an HTML5 string into a JUX {@link Element} tree.
 *
 * <p>This parser handles standard HTML5 constructs including:</p>
 * <ul>
 *   <li>Regular opening and closing tags ({@code <div>...</div>})</li>
 *   <li>Void/self-closing elements ({@code <br>}, {@code <img>}, {@code <input>})</li>
 *   <li>Attributes with double or single quotes, unquoted values, and boolean attributes</li>
 *   <li>Text content between tags</li>
 *   <li>Nested element hierarchies</li>
 *   <li>HTML comments ({@code <!-- ... -->})</li>
 *   <li>DOCTYPE declarations (skipped)</li>
 *   <li>XHTML-style self-closing tags ({@code <br/>})</li>
 *   <li>HTML character entities ({@code &amp;}, {@code &lt;}, {@code &gt;},
 *       {@code &quot;}, {@code &apos;}, {@code &#NNN;}, {@code &#xHHH;})</li>
 * </ul>
 *
 * <p>The parser uses a stack-based approach for efficiency and clear error reporting.
 * Errors include the template path and line number where the issue was detected.</p>
 *
 * <p>If the HTML contains multiple root-level elements or a mix of root-level text
 * and elements, they are automatically wrapped in a synthetic {@code <div>} element.
 * A single root element is returned directly.</p>
 *
 * @see Element
 * @see TemplateException
 */
public final class HtmlParser {

    /**
     * HTML5 void elements that have no closing tag.
     * Per the HTML5 specification, these elements cannot have children.
     */
    private static final Set<String> VOID_ELEMENTS = Set.of(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    );

    /** Current position in the source string. */
    private final String source;

    /** Template path for error reporting. */
    private final String templatePath;

    /** Current index into the source string. */
    private int pos;

    /** Current line number (1-based) for error reporting. */
    private int line;

    /** Index of the start of the current line, used to compute column numbers. */
    private int lineStart;

    /**
     * Private constructor -- use the static {@link #parse(String)} or
     * {@link #parse(String, String)} factory methods.
     *
     * @param source       the raw HTML string to parse
     * @param templatePath the template file path for error messages
     */
    private HtmlParser(String source, String templatePath) {
        this.source = source;
        this.templatePath = templatePath;
        this.pos = 0;
        this.line = 1;
        this.lineStart = 0;
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Parse an HTML5 string into a JUX Element tree.
     *
     * @param html the HTML string to parse
     * @return the root Element of the parsed tree
     * @throws TemplateException if the HTML contains syntax errors
     */
    public static Element parse(String html) {
        return parse(html, null);
    }

    /**
     * Parse an HTML5 string into a JUX Element tree with path context for error messages.
     *
     * @param html         the HTML string to parse
     * @param templatePath the template file path for error reporting, may be null
     * @return the root Element of the parsed tree
     * @throws TemplateException if the HTML contains syntax errors
     */
    public static Element parse(String html, String templatePath) {
        if (html == null || html.isEmpty()) {
            return Element.of("div");
        }
        HtmlParser parser = new HtmlParser(html, templatePath);
        return parser.doParse();
    }

    // ── Main parse loop ───────────────────────────────────────────

    /**
     * Main entry point for the parser. Parses all top-level nodes and returns
     * a single root Element. If there are multiple top-level elements or a mix
     * of text and elements, wraps them in a synthetic div.
     */
    private Element doParse() {
        List<Element> roots = new ArrayList<>();

        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) {
                break;
            }

            if (lookingAt("<!--")) {
                skipComment();
            } else if (lookingAt("<!")) {
                skipDeclaration();
            } else if (lookingAt("</")) {
                /* Unexpected closing tag at the root level -- error. */
                error("Unexpected closing tag at root level");
            } else if (lookingAt("<")) {
                roots.add(parseElement());
            } else {
                /* Root-level text content. */
                String text = parseTextContent();
                if (text != null && !text.isBlank()) {
                    roots.add(Element.of("span").text(text));
                }
            }
        }

        if (roots.isEmpty()) {
            return Element.of("div");
        }
        if (roots.size() == 1) {
            return roots.getFirst();
        }
        /* Multiple root nodes -- wrap in a synthetic div. */
        return Element.of("div").children(roots);
    }

    // ── Element parsing ───────────────────────────────────────────

    /**
     * Parse a single HTML element starting at the current {@code <} character.
     * Handles the opening tag, attributes, children (if not void), and closing tag.
     */
    private Element parseElement() {
        int tagStartLine = line;
        expect('<');

        String tagName = parseTagName();
        if (tagName.isEmpty()) {
            error("Expected tag name after '<'");
        }

        String lowerTag = tagName.toLowerCase();
        Element element = Element.of(lowerTag);

        /* Parse attributes. */
        parseAttributes(element);

        skipWhitespace();

        /* Check for self-closing syntax: /> */
        boolean selfClosing = false;
        if (pos < source.length() && source.charAt(pos) == '/') {
            selfClosing = true;
            pos++;
        }

        expect('>');

        /* Void elements and self-closing tags have no children or closing tag. */
        if (selfClosing || isVoidElement(lowerTag)) {
            return element;
        }

        /* Parse children and text content until the matching closing tag. */
        parseChildren(element, lowerTag, tagStartLine);

        return element;
    }

    /**
     * Parse child nodes (text and elements) until the matching closing tag is found.
     */
    private void parseChildren(Element parent, String parentTag, int openLine) {
        StringBuilder textBuffer = new StringBuilder();

        while (pos < source.length()) {
            if (lookingAt("<!--")) {
                /* Flush accumulated text before the comment. */
                flushText(parent, textBuffer);
                skipComment();
            } else if (lookingAt("</")) {
                /* Flush any accumulated text. */
                flushText(parent, textBuffer);

                /* Parse the closing tag. */
                int closeTagLine = line;
                pos += 2; /* Skip </ */
                String closingTag = parseTagName().toLowerCase();
                skipWhitespace();
                if (pos < source.length() && source.charAt(pos) == '>') {
                    pos++;
                } else {
                    error("Expected '>' in closing tag </" + closingTag + ">");
                }

                if (!closingTag.equals(parentTag)) {
                    error("Unexpected closing tag </" + closingTag + ">, expected </" + parentTag + ">. "
                            + "Opening tag was at line " + openLine);
                }
                return;
            } else if (lookingAt("<")) {
                /* Flush accumulated text before the child element. */
                flushText(parent, textBuffer);
                parent.children(parseElement());
            } else {
                /* Accumulate text content, including entity decoding. */
                char ch = source.charAt(pos);
                if (ch == '&') {
                    textBuffer.append(parseEntity());
                } else {
                    if (ch == '\n') {
                        line++;
                        lineStart = pos + 1;
                    }
                    textBuffer.append(ch);
                    pos++;
                }
            }
        }

        /* Reached end of input without finding the closing tag. */
        error("Unexpected end of input, unclosed tag <" + parentTag + "> opened at line " + openLine);
    }

    /**
     * Flush accumulated text into the parent element.
     * If the parent already has children, text is wrapped in a span.
     * Leading/trailing whitespace is preserved but pure-whitespace text between
     * elements is trimmed to a single space if not empty.
     */
    private void flushText(Element parent, StringBuilder textBuffer) {
        if (textBuffer.isEmpty()) {
            return;
        }
        String text = textBuffer.toString();
        textBuffer.setLength(0);

        if (text.isBlank()) {
            /* Purely whitespace text between tags -- skip to avoid empty spans. */
            return;
        }

        /*
         * If the parent already has children, we cannot use parent.text() since Element
         * treats text and children as somewhat exclusive. Wrap in a span instead.
         * If the parent has no children yet and no text, set it directly.
         */
        if (parent.getChildren().isEmpty() && parent.getTextContent() == null) {
            parent.text(text);
        } else {
            parent.children(Element.of("span").text(text));
        }
    }

    // ── Tag name parsing ──────────────────────────────────────────

    /**
     * Parse a tag name starting at the current position.
     * Tag names consist of ASCII letters, digits, and hyphens (for custom elements).
     */
    private String parseTagName() {
        int start = pos;
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.') {
                pos++;
            } else {
                break;
            }
        }
        return source.substring(start, pos);
    }

    // ── Attribute parsing ─────────────────────────────────────────

    /**
     * Parse all attributes of the current tag until {@code >} or {@code />} is reached.
     * Handles quoted values (double and single), unquoted values, and boolean attributes.
     */
    private void parseAttributes(Element element) {
        while (pos < source.length()) {
            skipWhitespace();

            if (pos >= source.length()) {
                break;
            }

            char ch = source.charAt(pos);

            /* End of opening tag. */
            if (ch == '>' || ch == '/') {
                break;
            }

            /* Parse attribute name. */
            String attrName = parseAttributeName();
            if (attrName.isEmpty()) {
                /* Unexpected character in attribute position -- skip it. */
                pos++;
                continue;
            }

            skipWhitespace();

            /* Check for = sign (attribute with value). */
            if (pos < source.length() && source.charAt(pos) == '=') {
                pos++; /* Skip = */
                skipWhitespace();

                String value = parseAttributeValue();
                applyAttribute(element, attrName, value);
            } else {
                /* Boolean attribute (e.g. required, disabled, checked). */
                applyAttribute(element, attrName, "");
            }
        }
    }

    /**
     * Parse an attribute name. Attribute names can include letters, digits,
     * hyphens, underscores, periods, colons (for namespaced attributes like xml:lang),
     * and the @ prefix (for framework directives).
     */
    private String parseAttributeName() {
        int start = pos;
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.'
                    || ch == ':' || ch == '@') {
                pos++;
            } else {
                break;
            }
        }
        return source.substring(start, pos);
    }

    /**
     * Parse an attribute value. Handles:
     * <ul>
     *   <li>Double-quoted values: {@code "hello world"}</li>
     *   <li>Single-quoted values: {@code 'hello world'}</li>
     *   <li>Unquoted values: {@code hello} (terminated by whitespace or {@code >})</li>
     * </ul>
     */
    private String parseAttributeValue() {
        if (pos >= source.length()) {
            return "";
        }

        char ch = source.charAt(pos);

        if (ch == '"' || ch == '\'') {
            return parseQuotedValue(ch);
        }

        /* Unquoted attribute value. */
        return parseUnquotedValue();
    }

    /**
     * Parse a quoted attribute value, handling HTML entities within.
     *
     * @param quote the quote character (" or ')
     */
    private String parseQuotedValue(char quote) {
        pos++; /* Skip opening quote. */
        StringBuilder sb = new StringBuilder();

        while (pos < source.length()) {
            char ch = source.charAt(pos);

            if (ch == quote) {
                pos++; /* Skip closing quote. */
                return sb.toString();
            }

            if (ch == '&') {
                sb.append(parseEntity());
            } else {
                if (ch == '\n') {
                    line++;
                    lineStart = pos + 1;
                }
                sb.append(ch);
                pos++;
            }
        }

        error("Unterminated attribute value, expected closing " + quote);
        return sb.toString(); /* Unreachable, error() always throws. */
    }

    /**
     * Parse an unquoted attribute value (terminated by whitespace or tag end).
     */
    private String parseUnquotedValue() {
        int start = pos;
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r'
                    || ch == '>' || ch == '/' || ch == '"' || ch == '\'' || ch == '=') {
                break;
            }
            if (ch == '\n') {
                line++;
                lineStart = pos + 1;
            }
            pos++;
        }
        return source.substring(start, pos);
    }

    /**
     * Apply a parsed attribute to the element. Handles special attribute names:
     * {@code class} is split and applied via {@link Element#cls(String...)},
     * {@code id} is applied via {@link Element#id(String)},
     * {@code style} is parsed into individual properties via {@link Element#style(String, String)}.
     */
    private void applyAttribute(Element element, String name, String value) {
        String lowerName = name.toLowerCase();

        switch (lowerName) {
            case "class" -> {
                /* Split class value by whitespace and apply each class name. */
                if (value != null && !value.isEmpty()) {
                    String[] classes = value.trim().split("\\s+");
                    element.cls(classes);
                }
            }
            case "id" -> element.id(value);
            case "style" -> {
                /* Parse inline style declarations into individual properties. */
                if (value != null && !value.isEmpty()) {
                    parseInlineStyle(element, value);
                }
            }
            default -> element.attr(lowerName, value);
        }
    }

    /**
     * Parse an inline CSS style string and apply individual properties to the element.
     * Example input: {@code "color: red; padding: 1rem; background-color: #fff"}
     */
    private void parseInlineStyle(Element element, String styleString) {
        String[] declarations = styleString.split(";");
        for (String declaration : declarations) {
            String trimmed = declaration.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex > 0 && colonIndex < trimmed.length() - 1) {
                String property = trimmed.substring(0, colonIndex).trim();
                String propValue = trimmed.substring(colonIndex + 1).trim();
                if (!property.isEmpty() && !propValue.isEmpty()) {
                    element.style(property, propValue);
                }
            }
        }
    }

    // ── Text content parsing ──────────────────────────────────────

    /**
     * Parse text content at the current position until a {@code <} is encountered.
     */
    private String parseTextContent() {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (ch == '<') {
                break;
            }
            if (ch == '&') {
                sb.append(parseEntity());
            } else {
                if (ch == '\n') {
                    line++;
                    lineStart = pos + 1;
                }
                sb.append(ch);
                pos++;
            }
        }
        return sb.toString();
    }

    // ── HTML entity parsing ───────────────────────────────────────

    /**
     * Parse an HTML character entity starting at the {@code &} character.
     * Supports named entities ({@code &amp;}, {@code &lt;}, {@code &gt;},
     * {@code &quot;}, {@code &apos;}, {@code &nbsp;}) and numeric entities
     * ({@code &#NNN;}, {@code &#xHHH;}).
     *
     * <p>If the entity is not recognized, the raw text is returned as-is.</p>
     */
    private String parseEntity() {
        int start = pos;
        pos++; /* Skip & */

        if (pos >= source.length()) {
            return "&";
        }

        if (source.charAt(pos) == '#') {
            /* Numeric entity. */
            pos++;
            if (pos >= source.length()) {
                return source.substring(start, pos);
            }

            boolean hex = false;
            if (source.charAt(pos) == 'x' || source.charAt(pos) == 'X') {
                hex = true;
                pos++;
            }

            int numStart = pos;
            while (pos < source.length() && source.charAt(pos) != ';') {
                pos++;
            }

            if (pos >= source.length()) {
                /* No closing semicolon -- return raw text. */
                return source.substring(start, pos);
            }

            String numStr = source.substring(numStart, pos);
            pos++; /* Skip ; */

            try {
                int codePoint = hex ? Integer.parseInt(numStr, 16) : Integer.parseInt(numStr);
                return String.valueOf((char) codePoint);
            } catch (NumberFormatException e) {
                return source.substring(start, pos);
            }
        }

        /* Named entity. */
        int nameStart = pos;
        while (pos < source.length() && Character.isLetterOrDigit(source.charAt(pos))) {
            pos++;
        }

        String entityName = source.substring(nameStart, pos);

        if (pos < source.length() && source.charAt(pos) == ';') {
            pos++; /* Skip ; */
        }

        return switch (entityName) {
            case "amp" -> "&";
            case "lt" -> "<";
            case "gt" -> ">";
            case "quot" -> "\"";
            case "apos" -> "'";
            case "nbsp" -> "\u00A0";
            case "copy" -> "\u00A9";
            case "reg" -> "\u00AE";
            case "trade" -> "\u2122";
            case "mdash" -> "\u2014";
            case "ndash" -> "\u2013";
            case "laquo" -> "\u00AB";
            case "raquo" -> "\u00BB";
            case "bull" -> "\u2022";
            case "hellip" -> "\u2026";
            case "prime" -> "\u2032";
            case "Prime" -> "\u2033";
            case "lsquo" -> "\u2018";
            case "rsquo" -> "\u2019";
            case "ldquo" -> "\u201C";
            case "rdquo" -> "\u201D";
            case "euro" -> "\u20AC";
            case "pound" -> "\u00A3";
            case "yen" -> "\u00A5";
            case "cent" -> "\u00A2";
            case "times" -> "\u00D7";
            case "divide" -> "\u00F7";
            case "plusmn" -> "\u00B1";
            case "frac12" -> "\u00BD";
            case "frac14" -> "\u00BC";
            case "frac34" -> "\u00BE";
            case "deg" -> "\u00B0";
            case "micro" -> "\u00B5";
            case "para" -> "\u00B6";
            case "middot" -> "\u00B7";
            case "larr" -> "\u2190";
            case "rarr" -> "\u2192";
            case "uarr" -> "\u2191";
            case "darr" -> "\u2193";
            case "harr" -> "\u2194";
            case "ensp" -> "\u2002";
            case "emsp" -> "\u2003";
            case "thinsp" -> "\u2009";
            default -> "&" + entityName + ";";
        };
    }

    // ── Comment and declaration handling ──────────────────────────

    /**
     * Skip an HTML comment ({@code <!-- ... -->}). Advances the position past
     * the closing {@code -->}.
     */
    private void skipComment() {
        pos += 4; /* Skip <!-- */
        while (pos < source.length()) {
            if (lookingAt("-->")) {
                pos += 3;
                return;
            }
            if (source.charAt(pos) == '\n') {
                line++;
                lineStart = pos + 1;
            }
            pos++;
        }
        /* Unterminated comment -- not a fatal error, just consume to end. */
    }

    /**
     * Skip a declaration ({@code <!DOCTYPE ...>} or similar). Advances past
     * the closing {@code >}.
     */
    private void skipDeclaration() {
        while (pos < source.length()) {
            if (source.charAt(pos) == '>') {
                pos++;
                return;
            }
            if (source.charAt(pos) == '\n') {
                line++;
                lineStart = pos + 1;
            }
            pos++;
        }
    }

    // ── Utility methods ───────────────────────────────────────────

    /**
     * Check if the given element tag is an HTML5 void element.
     */
    private boolean isVoidElement(String tag) {
        return VOID_ELEMENTS.contains(tag);
    }

    /**
     * Skip whitespace characters (space, tab, newline, carriage return)
     * at the current position, updating line numbers as newlines are crossed.
     */
    private void skipWhitespace() {
        while (pos < source.length()) {
            char ch = source.charAt(pos);
            if (ch == ' ' || ch == '\t' || ch == '\r') {
                pos++;
            } else if (ch == '\n') {
                line++;
                lineStart = pos + 1;
                pos++;
            } else {
                break;
            }
        }
    }

    /**
     * Check if the source at the current position starts with the given string.
     * Case-insensitive comparison is NOT used -- the input must match exactly.
     */
    private boolean lookingAt(String expected) {
        if (pos + expected.length() > source.length()) {
            return false;
        }
        return source.startsWith(expected, pos);
    }

    /**
     * Expect and consume a specific character at the current position.
     *
     * @param expected the character expected at the current position
     * @throws TemplateException if the expected character is not found
     */
    private void expect(char expected) {
        if (pos >= source.length() || source.charAt(pos) != expected) {
            error("Expected '" + expected + "' but found "
                    + (pos >= source.length() ? "end of input" : "'" + source.charAt(pos) + "'"));
        }
        pos++;
    }

    /**
     * Throw a {@link TemplateException} with the current position context.
     *
     * @param message the error description
     * @throws TemplateException always
     */
    private void error(String message) {
        throw new TemplateException(message, templatePath, line);
    }

    private HtmlParser() {
        throw new AssertionError("Use static parse() methods");
    }
}
