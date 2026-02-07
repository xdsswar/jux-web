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

package xss.it.jux.server.render;

import xss.it.jux.annotation.CssPosition;
import xss.it.jux.annotation.JsPosition;
import xss.it.jux.core.CssResource;
import xss.it.jux.core.Element;
import xss.it.jux.core.JsResource;
import xss.it.jux.core.Page;
import xss.it.jux.core.PageMeta;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core SSR renderer that serializes a {@link xss.it.jux.core.Component}
 * and its {@link PageMeta} into a complete HTML5 document string.
 *
 * <p>This renderer is the heart of JUX's server-side rendering pipeline.
 * It takes a component (whose {@code render()} method produces a virtual
 * {@link Element} tree) and page metadata, and produces a fully formed
 * HTML5 document including {@code <!DOCTYPE html>}, {@code <head>} with
 * all meta tags, CSS/JS resources, structured data, and the rendered
 * body content.</p>
 *
 * <p><b>Performance:</b> Uses {@link StringBuilder} throughout for
 * allocation-efficient string building. Target: SSR render &lt; 5ms
 * for typical pages (warm, excluding DB queries).</p>
 *
 * <p><b>Security:</b> All text content and attribute values are
 * HTML-escaped to prevent XSS injection.</p>
 *
 * @see xss.it.jux.core.Component
 * @see Element
 * @see PageMeta
 */
@org.springframework.stereotype.Component
public class JuxRenderer {

    /** HTML5 self-closing (void) element tags that must not have a closing tag. */
    private static final Set<String> VOID_ELEMENTS = Set.of(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "source", "track", "wbr"
    );

    /**
     * Render a complete HTML5 document from a component and page metadata.
     *
     * <p>The rendering process:</p>
     * <ol>
     *   <li>If component is a {@link Page}, calls {@code pageMeta()} to get page-level metadata</li>
     *   <li>Merges page metadata with the passed-in {@code meta} (passed-in overrides)</li>
     *   <li>Calls {@code component.render()} to get the virtual DOM element tree</li>
     *   <li>Builds the complete HTML5 document with head and body sections</li>
     * </ol>
     *
     * @param component the page component to render
     * @param meta      external page metadata (e.g. from annotations); may be null
     * @return a complete HTML5 document string
     */
    public String render(xss.it.jux.core.Component component, PageMeta meta) {
        // 1. Resolve page metadata: Page's own pageMeta() + externally provided
        PageMeta componentMeta = (component instanceof Page page) ? page.pageMeta() : null;
        PageMeta resolved = resolvePageMeta(componentMeta, meta);

        // 2. Render the component's element tree
        Element body = component.render();

        // 3. Build the full HTML5 document
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<!DOCTYPE html>\n");

        // <html> tag with lang, dir, and custom attributes (e.g. data-theme)
        sb.append("<html");
        if (resolved.getHtmlLang() != null && !resolved.getHtmlLang().isEmpty()) {
            sb.append(" lang=\"").append(escapeHtml(resolved.getHtmlLang())).append('"');
        }
        if (resolved.getHtmlDir() != null && !resolved.getHtmlDir().isEmpty()) {
            sb.append(" dir=\"").append(escapeHtml(resolved.getHtmlDir())).append('"');
        }
        for (Map.Entry<String, String> entry : resolved.getHtmlAttrs().entrySet()) {
            sb.append(' ').append(escapeHtml(entry.getKey()))
              .append("=\"").append(escapeHtml(entry.getValue())).append('"');
        }
        sb.append(">\n");

        // <head>
        renderHead(sb, resolved);

        // <body>
        renderBody(sb, resolved, body);

        sb.append("</html>\n");
        return sb.toString();
    }

    /**
     * Render a single {@link Element} tree to an HTML string fragment.
     *
     * <p>This is useful for rendering partial content (e.g. AJAX responses,
     * widget previews) without a full HTML document wrapper.</p>
     *
     * @param element the root element to render
     * @return the HTML string for the element and all its descendants
     */
    public String renderElement(Element element) {
        StringBuilder sb = new StringBuilder(1024);
        renderElementTo(sb, element);
        return sb.toString();
    }

    // ── Head Section ────────────────────────────────────────────────

    /**
     * Render the complete {@code <head>} section of the HTML5 document.
     *
     * <p>Outputs the following elements in order:</p>
     * <ol>
     *   <li>Charset meta tag</li>
     *   <li>Viewport meta tag</li>
     *   <li>{@code <title>} tag</li>
     *   <li>Name-based meta tags ({@code <meta name="...">})</li>
     *   <li>Property-based meta tags ({@code <meta property="og:...">})</li>
     *   <li>HTTP-equiv meta tags ({@code <meta http-equiv="...">})</li>
     *   <li>Canonical link</li>
     *   <li>Alternate/hreflang links for i18n</li>
     *   <li>Favicon and Apple touch icon links</li>
     *   <li>Resource hints (preconnect, dns-prefetch, preload)</li>
     *   <li>HEAD-positioned CSS {@code <link>} tags (sorted by order)</li>
     *   <li>Inline CSS {@code <style>} tags (sorted by order)</li>
     *   <li>HEAD-positioned JS {@code <script>} tags (sorted by order)</li>
     *   <li>JSON-LD structured data scripts</li>
     * </ol>
     *
     * @param sb   the StringBuilder to append the head HTML to
     * @param meta the resolved page metadata containing all head elements
     */
    private void renderHead(StringBuilder sb, PageMeta meta) {
        sb.append("<head>\n");

        // Charset
        sb.append("    <meta charset=\"").append(escapeHtml(meta.getCharset())).append("\">\n");

        // Viewport
        if (meta.getViewport() != null && !meta.getViewport().isEmpty()) {
            sb.append("    <meta name=\"viewport\" content=\"")
              .append(escapeHtml(meta.getViewport())).append("\">\n");
        }

        // Title
        String resolvedTitle = meta.getResolvedTitle();
        sb.append("    <title>").append(resolvedTitle != null ? escapeHtml(resolvedTitle) : "").append("</title>\n");

        // Meta name tags
        for (Map.Entry<String, String> entry : meta.getMetaNames().entrySet()) {
            sb.append("    <meta name=\"").append(escapeHtml(entry.getKey()))
              .append("\" content=\"").append(escapeHtml(entry.getValue())).append("\">\n");
        }

        // Meta property tags (og:*, twitter:* via metaProperty)
        for (Map.Entry<String, String> entry : meta.getMetaProperties().entrySet()) {
            sb.append("    <meta property=\"").append(escapeHtml(entry.getKey()))
              .append("\" content=\"").append(escapeHtml(entry.getValue())).append("\">\n");
        }

        // HTTP-equiv tags
        for (Map.Entry<String, String> entry : meta.getHttpEquivs().entrySet()) {
            sb.append("    <meta http-equiv=\"").append(escapeHtml(entry.getKey()))
              .append("\" content=\"").append(escapeHtml(entry.getValue())).append("\">\n");
        }

        // Canonical link
        if (meta.getCanonical() != null && !meta.getCanonical().isEmpty()) {
            sb.append("    <link rel=\"canonical\" href=\"")
              .append(escapeHtml(meta.getCanonical())).append("\">\n");
        }

        // Alternate / hreflang links
        for (Map.Entry<String, String> entry : meta.getAlternates().entrySet()) {
            sb.append("    <link rel=\"alternate\" hreflang=\"")
              .append(escapeHtml(entry.getKey()))
              .append("\" href=\"").append(escapeHtml(entry.getValue())).append("\">\n");
        }

        // Favicon
        if (meta.getFaviconHref() != null && !meta.getFaviconHref().isEmpty()) {
            sb.append("    <link rel=\"icon\" href=\"").append(escapeHtml(meta.getFaviconHref())).append('"');
            if (meta.getFaviconType() != null && !meta.getFaviconType().isEmpty()) {
                sb.append(" type=\"").append(escapeHtml(meta.getFaviconType())).append('"');
            }
            if (meta.getFaviconSizes() != null && !meta.getFaviconSizes().isEmpty()) {
                sb.append(" sizes=\"").append(escapeHtml(meta.getFaviconSizes())).append('"');
            }
            sb.append(">\n");
        }

        // Apple touch icon
        if (meta.getAppleTouchIconHref() != null && !meta.getAppleTouchIconHref().isEmpty()) {
            sb.append("    <link rel=\"apple-touch-icon\" href=\"")
              .append(escapeHtml(meta.getAppleTouchIconHref())).append("\">\n");
        }

        // Preconnect hints
        for (String origin : meta.getPreconnects()) {
            sb.append("    <link rel=\"preconnect\" href=\"")
              .append(escapeHtml(origin)).append("\">\n");
        }

        // DNS-prefetch hints
        for (String origin : meta.getDnsPrefetches()) {
            sb.append("    <link rel=\"dns-prefetch\" href=\"")
              .append(escapeHtml(origin)).append("\">\n");
        }

        // Preload hints
        for (PageMeta.PreloadHint hint : meta.getPreloads()) {
            sb.append("    <link rel=\"preload\" href=\"")
              .append(escapeHtml(hint.href()))
              .append("\" as=\"").append(escapeHtml(hint.as())).append("\">\n");
        }

        // HEAD CSS links (sorted by order)
        List<CssResource> headCss = meta.getCssResources().stream()
                .filter(r -> r.position() == CssPosition.HEAD)
                .filter(r -> !meta.getRemovedCss().contains(r.path()))
                .sorted(Comparator.comparingInt(CssResource::order))
                .toList();
        for (CssResource css : headCss) {
            renderCssLink(sb, css);
        }

        // Inline CSS (<style> tags)
        List<PageMeta.InlineResource> sortedInlineCss = meta.getInlineCss().stream()
                .sorted(Comparator.comparingInt(PageMeta.InlineResource::order))
                .toList();
        for (PageMeta.InlineResource inline : sortedInlineCss) {
            sb.append("    <style>").append(inline.content()).append("</style>\n");
        }

        // HEAD JS scripts (sorted by order)
        List<JsResource> headJs = meta.getJsResources().stream()
                .filter(r -> r.position() == JsPosition.HEAD)
                .filter(r -> !meta.getRemovedJs().contains(r.path()))
                .sorted(Comparator.comparingInt(JsResource::order))
                .toList();
        for (JsResource js : headJs) {
            renderJsScript(sb, js);
        }

        // JSON-LD structured data scripts
        for (String jsonLd : meta.getJsonLdScripts()) {
            sb.append("    <script type=\"application/ld+json\">").append(jsonLd).append("</script>\n");
        }

        sb.append("</head>\n");
    }

    // ── Body Section ────────────────────────────────────────────────

    /**
     * Render the {@code <body>} section of the HTML5 document.
     *
     * <p>Outputs the following in order:</p>
     * <ol>
     *   <li>Opening {@code <body>} tag with optional classes and attributes</li>
     *   <li>The rendered element tree (page content from {@code Component.render()})</li>
     *   <li>BODY_END-positioned CSS {@code <link>} tags (sorted by order)</li>
     *   <li>BODY_END-positioned JS {@code <script>} tags (sorted by order)</li>
     *   <li>Inline JS {@code <script>} tags (sorted by order)</li>
     *   <li>Closing {@code </body>} tag</li>
     * </ol>
     *
     * @param sb          the StringBuilder to append the body HTML to
     * @param meta        the resolved page metadata (body classes, attributes, resources)
     * @param bodyContent the root element of the page's rendered component tree; may be null
     */
    private void renderBody(StringBuilder sb, PageMeta meta, Element bodyContent) {
        sb.append("<body");

        // Body classes
        if (!meta.getBodyClasses().isEmpty()) {
            sb.append(" class=\"").append(escapeHtml(String.join(" ", meta.getBodyClasses()))).append('"');
        }

        // Body attributes
        for (Map.Entry<String, String> entry : meta.getBodyAttrs().entrySet()) {
            sb.append(' ').append(escapeHtml(entry.getKey()))
              .append("=\"").append(escapeHtml(entry.getValue())).append('"');
        }

        sb.append(">\n");

        // Rendered element tree (the page content)
        if (bodyContent != null) {
            renderElementTo(sb, bodyContent);
            sb.append('\n');
        }

        // BODY_END CSS links (sorted by order)
        List<CssResource> bodyEndCss = meta.getCssResources().stream()
                .filter(r -> r.position() == CssPosition.BODY_END)
                .filter(r -> !meta.getRemovedCss().contains(r.path()))
                .sorted(Comparator.comparingInt(CssResource::order))
                .toList();
        for (CssResource css : bodyEndCss) {
            renderCssLink(sb, css);
        }

        // BODY_END JS scripts (sorted by order)
        List<JsResource> bodyEndJs = meta.getJsResources().stream()
                .filter(r -> r.position() == JsPosition.BODY_END)
                .filter(r -> !meta.getRemovedJs().contains(r.path()))
                .sorted(Comparator.comparingInt(JsResource::order))
                .toList();
        for (JsResource js : bodyEndJs) {
            renderJsScript(sb, js);
        }

        // Inline JS (<script> tags, typically BODY_END)
        List<PageMeta.InlineResource> sortedInlineJs = meta.getInlineJs().stream()
                .sorted(Comparator.comparingInt(PageMeta.InlineResource::order))
                .toList();
        for (PageMeta.InlineResource inline : sortedInlineJs) {
            sb.append("    <script>").append(inline.content()).append("</script>\n");
        }

        sb.append("</body>\n");
    }

    // ── Element Tree Rendering ──────────────────────────────────────

    /**
     * Recursively render an {@link Element} tree into a {@link StringBuilder}.
     *
     * <p>Handles void (self-closing) elements, text content, attributes,
     * event handler markers, and recursive child rendering.</p>
     *
     * @param sb      the StringBuilder to append HTML to
     * @param element the element to render
     */
    private void renderElementTo(StringBuilder sb, Element element) {
        String tag = element.getTag();

        // Opening tag
        sb.append('<').append(tag);

        // Attributes
        String attrs = renderAttributes(element.getAttributes());
        if (!attrs.isEmpty()) {
            sb.append(attrs);
        }

        // Event handler markers for client-side hydration
        Map<String, ?> handlers = element.getEventHandlers();
        if (handlers != null && !handlers.isEmpty()) {
            sb.append(" data-jux-events=\"")
              .append(escapeHtml(String.join(",", handlers.keySet())))
              .append('"');
        }

        // Void (self-closing) elements
        if (VOID_ELEMENTS.contains(tag)) {
            sb.append('>');
            return;
        }

        sb.append('>');

        // Content: text takes precedence over children
        String text = element.getTextContent();
        if (text != null) {
            sb.append(escapeHtml(text));
        } else {
            // Render children recursively
            List<Element> children = element.getChildren();
            if (children != null && !children.isEmpty()) {
                for (Element child : children) {
                    renderElementTo(sb, child);
                }
            }
        }

        // Closing tag
        sb.append("</").append(tag).append('>');
    }

    // ── Resource Rendering Helpers ──────────────────────────────────

    /**
     * Render a CSS {@code <link>} tag for the given resource.
     *
     * @param sb  the StringBuilder to append to
     * @param css the CSS resource descriptor
     */
    private void renderCssLink(StringBuilder sb, CssResource css) {
        sb.append("    <link rel=\"stylesheet\" href=\"").append(escapeHtml(css.path())).append('"');
        if (css.media() != null && !css.media().isEmpty()) {
            sb.append(" media=\"").append(escapeHtml(css.media())).append('"');
        }
        if (css.integrity() != null && !css.integrity().isEmpty()) {
            sb.append(" integrity=\"").append(escapeHtml(css.integrity()))
              .append("\" crossorigin=\"anonymous\"");
        }
        sb.append(">\n");
    }

    /**
     * Render a JS {@code <script>} tag for the given resource.
     *
     * @param sb the StringBuilder to append to
     * @param js the JS resource descriptor
     */
    private void renderJsScript(StringBuilder sb, JsResource js) {
        sb.append("    <script src=\"").append(escapeHtml(js.path())).append('"');
        if (js.defer()) {
            sb.append(" defer");
        }
        if (js.async()) {
            sb.append(" async");
        }
        if (js.module()) {
            sb.append(" type=\"module\"");
        }
        if (js.integrity() != null && !js.integrity().isEmpty()) {
            sb.append(" integrity=\"").append(escapeHtml(js.integrity()))
              .append("\" crossorigin=\"anonymous\"");
        }
        sb.append("></script>\n");
    }

    // ── Attribute Rendering ─────────────────────────────────────────

    /**
     * Render an attribute map to an HTML attribute string.
     *
     * <p>Each attribute is rendered as {@code key="escaped-value"}.
     * The returned string starts with a leading space if non-empty,
     * so it can be directly appended after a tag name.</p>
     *
     * @param attributes the attribute map (name to value)
     * @return the rendered attribute string (with leading space), or empty string
     */
    private String renderAttributes(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(attributes.size() * 20);
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            sb.append(' ').append(entry.getKey())
              .append("=\"").append(escapeHtml(entry.getValue())).append('"');
        }
        return sb.toString();
    }

    // ── HTML Escaping ───────────────────────────────────────────────

    /**
     * Escape special HTML characters to prevent XSS injection.
     *
     * <p>Escapes the five characters that have special meaning in HTML:</p>
     * <ul>
     *   <li>{@code &} to {@code &amp;}</li>
     *   <li>{@code <} to {@code &lt;}</li>
     *   <li>{@code >} to {@code &gt;}</li>
     *   <li>{@code "} to {@code &quot;}</li>
     *   <li>{@code '} to {@code &#39;}</li>
     * </ul>
     *
     * @param text the raw text to escape; may be null
     * @return the escaped text, or empty string if input is null
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        // Fast path: check if escaping is needed at all
        boolean needsEscape = false;
        for (int i = 0, len = text.length(); i < len; i++) {
            char c = text.charAt(i);
            if (c == '&' || c == '<' || c == '>' || c == '"' || c == '\'') {
                needsEscape = true;
                break;
            }
        }
        if (!needsEscape) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0, len = text.length(); i < len; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    // ── PageMeta Resolution ─────────────────────────────────────────

    /**
     * Merge component-level and external page metadata.
     *
     * <p>If neither source provides metadata, a default PageMeta with
     * sensible defaults (charset UTF-8, standard viewport) is returned.
     * When both are present, the external metadata overrides the component's.</p>
     *
     * @param componentMeta metadata from {@code page.pageMeta()}, may be null
     * @param externalMeta  metadata from annotations or caller, may be null
     * @return the resolved, non-null PageMeta
     */
    private PageMeta resolvePageMeta(PageMeta componentMeta, PageMeta externalMeta) {
        if (componentMeta == null && externalMeta == null) {
            return PageMeta.create();
        }
        if (componentMeta == null) {
            return externalMeta;
        }
        if (externalMeta == null) {
            return componentMeta;
        }
        // Both present: start with the component's meta, then merge external on top
        // (external / annotation meta is the baseline; component overrides it)
        return externalMeta.merge(componentMeta);
    }
}
