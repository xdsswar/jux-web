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

package xss.it.jux.core;

import xss.it.jux.annotation.CssPosition;
import xss.it.jux.annotation.JsPosition;

import java.util.*;
import java.util.function.Consumer;

/**
 * Programmatic page metadata builder -- the dynamic counterpart to annotations.
 *
 * <p>Returned by {@link Page#pageMeta()} to set page title, meta tags,
 * CSS/JS resources, HTTP headers, OpenGraph, Twitter Cards, and more
 * at render time based on runtime data (database content, user preferences,
 * environment, locale).</p>
 *
 * <p><b>Relationship to annotations:</b></p>
 * <ul>
 *   <li>Annotations ({@code @Title}, {@code @Css}, {@code @Meta}) are the static baseline</li>
 *   <li>{@code PageMeta} values <b>override</b> matching annotations</li>
 *   <li>Return {@code null} from {@code pageMeta()} to use annotations only</li>
 * </ul>
 *
 * <p><b>Example -- blog post with database-driven metadata:</b></p>
 * <pre>{@code
 * @Override
 * public PageMeta pageMeta() {
 *     var post = blogRepo.findBySlug(slug);
 *     if (post == null) return PageMeta.create().status(404).title("Not Found");
 *
 *     return PageMeta.create()
 *         .title(post.getTitle())
 *         .titleTemplate("%s | My Blog")
 *         .description(post.getExcerpt())
 *         .ogTitle(post.getTitle())
 *         .ogImage(post.getCoverImage())
 *         .ogImageAlt(post.getCoverImageAlt())
 *         .canonical("https://myblog.com/blog/" + slug)
 *         .htmlLang(post.getLanguage())
 *         .autoDir()
 *         .when(post.hasCodeBlocks(), meta ->
 *             meta.css("vendor/prism.css", 50)
 *                 .js("vendor/prism.js", 50)
 *         );
 * }
 * }</pre>
 *
 * <p>All builder methods return {@code this} for fluent chaining.</p>
 *
 * @see Page#pageMeta()
 */
public class PageMeta {

    // ── Title ────────────────────────────────────────────────────

    /** Page title text. Overrides {@code @Title} annotation when non-null. */
    private String title;

    /**
     * Title template with a {@code %s} placeholder for the title.
     * Example: {@code "%s | My Site"} renders as {@code "About | My Site"}.
     */
    private String titleTemplate;

    // ── Meta Tags ────────────────────────────────────────────────

    /**
     * Meta tags by name attribute. Keys are meta names (e.g. "description",
     * "robots", "twitter:card"), values are the content attribute.
     */
    private final Map<String, String> metaNames = new LinkedHashMap<>();

    /**
     * Meta tags by property attribute, primarily for OpenGraph.
     * Keys are property names (e.g. "og:title", "og:image"),
     * values are the content attribute.
     */
    private final Map<String, String> metaProperties = new LinkedHashMap<>();

    /**
     * HTTP-equiv meta tags. Keys are http-equiv names (e.g. "Content-Type",
     * "refresh"), values are the content attribute.
     */
    private final Map<String, String> httpEquivs = new LinkedHashMap<>();

    /** Character encoding meta tag. Defaults to "UTF-8". */
    private String charset = "UTF-8";

    /** Viewport meta tag content. Defaults to "width=device-width, initial-scale=1". */
    private String viewport = "width=device-width, initial-scale=1";

    // ── Link Tags ────────────────────────────────────────────────

    /** Canonical URL for this page. Rendered as {@code <link rel="canonical">}. */
    private String canonical;

    /**
     * Alternate language URLs keyed by hreflang code.
     * Rendered as {@code <link rel="alternate" hreflang="..." href="...">} tags.
     */
    private final Map<String, String> alternates = new LinkedHashMap<>();

    /** Favicon URL path. Overrides {@code @Favicon} annotation. */
    private String faviconHref;

    /** Favicon MIME type (e.g. "image/x-icon", "image/png"). */
    private String faviconType;

    /** Favicon size hint (e.g. "32x32", "192x192"). */
    private String faviconSizes;

    /** Apple touch icon URL for iOS home screen bookmarks. */
    private String appleTouchIconHref;

    // ── CSS/JS ───────────────────────────────────────────────────

    /** CSS resources to add to the resource pipeline alongside annotation resources. */
    private final List<CssResource> cssResources = new ArrayList<>();

    /** JavaScript resources to add to the resource pipeline alongside annotation resources. */
    private final List<JsResource> jsResources = new ArrayList<>();

    /** CSS paths to remove from the resource pipeline (e.g. unwanted layout CSS). */
    private final Set<String> removedCss = new LinkedHashSet<>();

    /** JavaScript paths to remove from the resource pipeline. */
    private final Set<String> removedJs = new LinkedHashSet<>();

    /** Inline CSS blocks to inject as {@code <style>} tags. */
    private final List<InlineResource> inlineCss = new ArrayList<>();

    /** Inline JavaScript blocks to inject as {@code <script>} tags. */
    private final List<InlineResource> inlineJs = new ArrayList<>();

    // ── HTTP Response ────────────────────────────────────────────

    /** Custom HTTP response headers. */
    private final Map<String, String> headers = new LinkedHashMap<>();

    /** HTTP response status code. 0 means use the default (200 OK). */
    private int status;

    /** Redirect target URL. Non-null triggers a redirect response. */
    private String redirectUrl;

    /** HTTP status code for the redirect (e.g. 301, 302, 307, 308). */
    private int redirectStatus;

    // ── HTML Root ────────────────────────────────────────────────

    /**
     * Language attribute for the {@code <html>} tag. Required by WCAG 3.1.1.
     * Uses BCP 47 language tags (e.g. "en", "es", "ar", "zh-CN").
     */
    private String htmlLang;

    /**
     * Text direction attribute for the {@code <html>} tag.
     * Either "ltr" (left-to-right) or "rtl" (right-to-left).
     * Required for RTL languages like Arabic, Hebrew, and Farsi.
     */
    private String htmlDir;

    /** CSS classes to add to the {@code <body>} tag. */
    private final List<String> bodyClasses = new ArrayList<>();

    /** Arbitrary attributes to add to the {@code <body>} tag. */
    private final Map<String, String> bodyAttrs = new LinkedHashMap<>();

    /** Arbitrary attributes to add to the {@code <html>} tag (e.g. data-theme). */
    private final Map<String, String> htmlAttrs = new LinkedHashMap<>();

    // ── Structured Data ──────────────────────────────────────────

    /**
     * JSON-LD structured data scripts for search engine rich results.
     * Each entry is rendered as a {@code <script type="application/ld+json">} tag.
     */
    private final List<String> jsonLdScripts = new ArrayList<>();

    // ── Performance Hints ────────────────────────────────────────

    /** Resource preload hints rendered as {@code <link rel="preload">} tags. */
    private final List<PreloadHint> preloads = new ArrayList<>();

    /** Resource prefetch hints rendered as {@code <link rel="prefetch">} tags. */
    private final List<String> prefetches = new ArrayList<>();

    /** Origin preconnect hints rendered as {@code <link rel="preconnect">} tags. */
    private final List<String> preconnects = new ArrayList<>();

    /** DNS prefetch hints rendered as {@code <link rel="dns-prefetch">} tags. */
    private final List<String> dnsPrefetches = new ArrayList<>();

    /**
     * Private constructor -- use {@link #create()} to obtain a new instance.
     */
    private PageMeta() {}

    /**
     * Creates a new empty {@code PageMeta} builder.
     *
     * @return a fresh PageMeta instance ready for fluent configuration
     */
    public static PageMeta create() { return new PageMeta(); }

    // ── Title ────────────────────────────────────────────────────

    /**
     * Set the page title. Overrides the {@code @Title} annotation.
     *
     * <p>Rendered inside the {@code <title>} HTML tag. If a
     * {@link #titleTemplate(String)} is also set, the template is applied.</p>
     *
     * @param title the page title text
     * @return this builder for chaining
     */
    public PageMeta title(String title) { this.title = title; return this; }

    /**
     * Set a title template with a {@code %s} placeholder for the title.
     *
     * <p>When both title and template are set, the resolved title becomes
     * {@code String.format(template, title)}. Example:
     * {@code titleTemplate("%s | My Site")} with {@code title("About")}
     * produces {@code "About | My Site"}.</p>
     *
     * @param template the template string with a {@code %s} placeholder
     * @return this builder for chaining
     */
    public PageMeta titleTemplate(String template) { this.titleTemplate = template; return this; }

    // ── Meta Tags ────────────────────────────────────────────────

    /**
     * Add a {@code <meta name="..." content="...">} tag.
     *
     * <p>If a meta tag with the same name already exists, it is replaced.
     * Overrides matching {@code @Meta} annotations.</p>
     *
     * @param name    the meta name attribute (e.g. "description", "robots", "keywords")
     * @param content the meta content value
     * @return this builder for chaining
     */
    public PageMeta meta(String name, String content) { metaNames.put(name, content); return this; }

    /**
     * Add a {@code <meta property="..." content="...">} tag for OpenGraph.
     *
     * <p>If a meta property with the same name already exists, it is replaced.</p>
     *
     * @param property the meta property attribute (e.g. "og:title", "og:image")
     * @param content  the meta content value
     * @return this builder for chaining
     */
    public PageMeta metaProperty(String property, String content) { metaProperties.put(property, content); return this; }

    /**
     * Add a {@code <meta http-equiv="..." content="...">} tag.
     *
     * @param equiv   the http-equiv attribute (e.g. "Content-Type", "refresh")
     * @param content the meta content value
     * @return this builder for chaining
     */
    public PageMeta httpEquiv(String equiv, String content) { httpEquivs.put(equiv, content); return this; }

    /**
     * Set the charset meta tag. Defaults to "UTF-8".
     *
     * @param charset the character encoding (e.g. "UTF-8")
     * @return this builder for chaining
     */
    public PageMeta charset(String charset) { this.charset = charset; return this; }

    /**
     * Set the viewport meta tag content. Defaults to "width=device-width, initial-scale=1".
     *
     * @param content the viewport content string
     * @return this builder for chaining
     */
    public PageMeta viewport(String content) { this.viewport = content; return this; }

    /**
     * Set the meta description -- used by search engines and as og:description fallback.
     *
     * <p>Shorthand for {@code meta("description", desc)}.</p>
     *
     * @param desc the page description text
     * @return this builder for chaining
     */
    public PageMeta description(String desc) { return meta("description", desc); }

    /**
     * Set meta keywords (comma-separated in the rendered output).
     *
     * <p>Shorthand for {@code meta("keywords", joined)}.</p>
     *
     * @param kw one or more keyword strings
     * @return this builder for chaining
     */
    public PageMeta keywords(String... kw) { return meta("keywords", String.join(", ", kw)); }

    /**
     * Set the meta author.
     *
     * <p>Shorthand for {@code meta("author", author)}.</p>
     *
     * @param author the author name
     * @return this builder for chaining
     */
    public PageMeta author(String author) { return meta("author", author); }

    /**
     * Set the meta robots directive controlling search engine behavior.
     *
     * <p>Common values: {@code "index,follow"} (default), {@code "noindex,nofollow"}
     * (hide from search engines), {@code "noindex"} (hide page, follow links).</p>
     *
     * @param robots the robots directive string
     * @return this builder for chaining
     */
    public PageMeta robots(String robots) { return meta("robots", robots); }

    /**
     * Set the theme-color meta tag controlling the browser address bar color on mobile.
     *
     * @param color a CSS color value (e.g. "#3b82f6", "rgb(59, 130, 246)")
     * @return this builder for chaining
     */
    public PageMeta themeColor(String color) { return meta("theme-color", color); }

    // ── OpenGraph ────────────────────────────────────────────────
    // Facebook/LinkedIn/WhatsApp link preview tags.

    /**
     * Set og:title -- shown in social media link previews.
     *
     * @param title the OpenGraph title text
     * @return this builder for chaining
     */
    public PageMeta ogTitle(String title) { return metaProperty("og:title", title); }

    /**
     * Set og:description -- shown below the title in link previews.
     *
     * @param desc the OpenGraph description text
     * @return this builder for chaining
     */
    public PageMeta ogDescription(String desc) { return metaProperty("og:description", desc); }

    /**
     * Set og:image -- the preview image URL for social media link previews.
     *
     * <p>Should be an absolute URL. Recommended minimum size: 1200x630 pixels.
     * ADA: set {@link #ogImageAlt(String)} whenever an og:image is set.</p>
     *
     * @param url absolute URL to the preview image
     * @return this builder for chaining
     */
    public PageMeta ogImage(String url) { return metaProperty("og:image", url); }

    /**
     * Set og:image:alt -- alt text for the OpenGraph image.
     *
     * <p>ADA requires alt text for all images, including OpenGraph images.
     * Always set this when {@link #ogImage(String)} is set.</p>
     *
     * @param alt description of the OG image for accessibility
     * @return this builder for chaining
     */
    public PageMeta ogImageAlt(String alt) { return metaProperty("og:image:alt", alt); }

    /**
     * Set og:type -- the type of content (e.g. "website", "article", "product").
     *
     * @param type the OpenGraph type string
     * @return this builder for chaining
     */
    public PageMeta ogType(String type) { return metaProperty("og:type", type); }

    /**
     * Set og:url -- the canonical URL for this content in link previews.
     *
     * @param url the canonical URL (should be absolute)
     * @return this builder for chaining
     */
    public PageMeta ogUrl(String url) { return metaProperty("og:url", url); }

    /**
     * Set og:site_name -- the overall site name (e.g. "My Blog").
     *
     * @param name the site name
     * @return this builder for chaining
     */
    public PageMeta ogSiteName(String name) { return metaProperty("og:site_name", name); }

    /**
     * Set og:locale -- the content locale (e.g. "en_US", "es_ES").
     *
     * @param locale the locale string in OpenGraph format (underscore-separated)
     * @return this builder for chaining
     */
    public PageMeta ogLocale(String locale) { return metaProperty("og:locale", locale); }

    // ── Twitter Cards ────────────────────────────────────────────
    // Twitter-specific link preview tags. Falls back to OG tags if not set.

    /**
     * Set twitter:card -- the Twitter Card type.
     *
     * <p>Only sets the value if not already present (first-write-wins), to
     * avoid overriding an explicitly set card type.</p>
     *
     * <p>Common values: {@code "summary"}, {@code "summary_large_image"},
     * {@code "app"}, {@code "player"}.</p>
     *
     * @param card the Twitter Card type
     * @return this builder for chaining
     */
    public PageMeta twitterCard(String card) { return metaNames.containsKey("twitter:card") ? this : meta("twitter:card", card); }

    /**
     * Set twitter:site -- the @handle of the website's Twitter account.
     *
     * @param handle the Twitter handle including the @ prefix (e.g. "@MySite")
     * @return this builder for chaining
     */
    public PageMeta twitterSite(String handle) { return meta("twitter:site", handle); }

    /**
     * Set twitter:creator -- the @handle of the content author's Twitter account.
     *
     * @param handle the Twitter handle including the @ prefix
     * @return this builder for chaining
     */
    public PageMeta twitterCreator(String handle) { return meta("twitter:creator", handle); }

    /**
     * Set twitter:title. Falls back to og:title if not set.
     *
     * @param title the Twitter Card title
     * @return this builder for chaining
     */
    public PageMeta twitterTitle(String title) { return meta("twitter:title", title); }

    /**
     * Set twitter:description. Falls back to og:description if not set.
     *
     * @param desc the Twitter Card description
     * @return this builder for chaining
     */
    public PageMeta twitterDescription(String desc) { return meta("twitter:description", desc); }

    /**
     * Set twitter:image. Falls back to og:image if not set.
     *
     * @param url the Twitter Card image URL
     * @return this builder for chaining
     */
    public PageMeta twitterImage(String url) { return meta("twitter:image", url); }

    /**
     * Set twitter:image:alt -- alt text for the Twitter Card image.
     * Falls back to og:image:alt if not set.
     *
     * @param alt description of the image for accessibility
     * @return this builder for chaining
     */
    public PageMeta twitterImageAlt(String alt) { return meta("twitter:image:alt", alt); }

    // ── Link Tags ────────────────────────────────────────────────

    /**
     * Set the canonical URL for this page. Overrides {@code @Canonical}.
     *
     * <p>Rendered as {@code <link rel="canonical" href="...">}. Important for
     * SEO when the same content is accessible via multiple URLs.</p>
     *
     * @param url the canonical URL (should be absolute for best SEO)
     * @return this builder for chaining
     */
    public PageMeta canonical(String url) { this.canonical = url; return this; }

    /**
     * Add an hreflang alternate link for multi-language SEO.
     *
     * <p>Rendered as {@code <link rel="alternate" hreflang="..." href="...">}.
     * For {@code @Localized} routes, these are auto-generated. Use this method
     * for manual control when auto-generation is not sufficient.</p>
     *
     * @param hreflang the language tag (e.g. "en", "es", "x-default")
     * @param url      the alternate URL for that language
     * @return this builder for chaining
     */
    public PageMeta alternate(String hreflang, String url) { alternates.put(hreflang, url); return this; }

    /**
     * Set the favicon path. Overrides {@code @Favicon}.
     *
     * @param href path to the favicon file (relative to /static or absolute URL)
     * @return this builder for chaining
     */
    public PageMeta favicon(String href) { this.faviconHref = href; return this; }

    /**
     * Set the favicon with explicit type and sizes.
     *
     * @param href  path to the favicon file
     * @param type  MIME type (e.g. "image/png", "image/x-icon", "image/svg+xml")
     * @param sizes size hint (e.g. "32x32", "192x192", "any")
     * @return this builder for chaining
     */
    public PageMeta favicon(String href, String type, String sizes) {
        this.faviconHref = href; this.faviconType = type; this.faviconSizes = sizes; return this;
    }

    /**
     * Add an Apple touch icon for iOS home screen bookmarks.
     *
     * <p>Rendered as {@code <link rel="apple-touch-icon" href="...">}.</p>
     *
     * @param href path to the touch icon image (recommended 180x180px)
     * @return this builder for chaining
     */
    public PageMeta appleTouchIcon(String href) { this.appleTouchIconHref = href; return this; }

    // ── CSS/JS Resources ─────────────────────────────────────────

    /**
     * Add a CSS file to the resource pipeline with default settings (HEAD, order 100).
     *
     * <p>Appended to annotation-declared CSS. Deduplicated by path.</p>
     *
     * @param path path to the CSS file (relative to /static or absolute CDN URL)
     * @return this builder for chaining
     */
    public PageMeta css(String path) { cssResources.add(new CssResource(path)); return this; }

    /**
     * Add a CSS file with a custom sort order.
     *
     * @param path  path to the CSS file
     * @param order sort order -- lower numbers load first
     * @return this builder for chaining
     */
    public PageMeta css(String path, int order) { cssResources.add(new CssResource(path, order)); return this; }

    /**
     * Add a CSS file with custom sort order and injection position.
     *
     * @param path  path to the CSS file
     * @param order sort order -- lower numbers load first
     * @param pos   where to inject: {@link CssPosition#HEAD HEAD} or
     *              {@link CssPosition#BODY_END BODY_END}
     * @return this builder for chaining
     */
    public PageMeta css(String path, int order, CssPosition pos) { cssResources.add(new CssResource(path, order, pos)); return this; }

    /**
     * Add a fully configured CSS resource to the pipeline.
     *
     * @param resource the CSS resource descriptor
     * @return this builder for chaining
     * @see CssResource
     */
    public PageMeta css(CssResource resource) { cssResources.add(resource); return this; }

    /**
     * Remove a CSS file added by annotations (e.g. layout CSS not needed on this page).
     *
     * <p>The path must match exactly the path used in the {@code @Css} annotation
     * or the {@link CssResource#path()}.</p>
     *
     * @param path the CSS file path to remove
     * @return this builder for chaining
     */
    public PageMeta removeCss(String path) { removedCss.add(path); return this; }

    /**
     * Add a JavaScript file to the resource pipeline with default settings
     * (BODY_END, order 100, deferred).
     *
     * <p>Appended to annotation-declared JS. Deduplicated by path.</p>
     *
     * @param path path to the JS file (relative to /static or absolute CDN URL)
     * @return this builder for chaining
     */
    public PageMeta js(String path) { jsResources.add(new JsResource(path)); return this; }

    /**
     * Add a JavaScript file with a custom sort order.
     *
     * @param path  path to the JS file
     * @param order sort order -- lower numbers load first
     * @return this builder for chaining
     */
    public PageMeta js(String path, int order) { jsResources.add(new JsResource(path, order)); return this; }

    /**
     * Add a JavaScript file with custom sort order and injection position.
     *
     * @param path  path to the JS file
     * @param order sort order -- lower numbers load first
     * @param pos   where to inject: {@link JsPosition#HEAD HEAD} or
     *              {@link JsPosition#BODY_END BODY_END}
     * @return this builder for chaining
     */
    public PageMeta js(String path, int order, JsPosition pos) { jsResources.add(new JsResource(path, order, pos)); return this; }

    /**
     * Add a fully configured JavaScript resource to the pipeline.
     *
     * @param resource the JS resource descriptor
     * @return this builder for chaining
     * @see JsResource
     */
    public PageMeta js(JsResource resource) { jsResources.add(resource); return this; }

    /**
     * Remove a JavaScript file added by annotations.
     *
     * <p>The path must match exactly the path used in the {@code @Js} annotation
     * or the {@link JsResource#path()}.</p>
     *
     * @param path the JS file path to remove
     * @return this builder for chaining
     */
    public PageMeta removeJs(String path) { removedJs.add(path); return this; }

    /**
     * Inject inline CSS into the page as a {@code <style>} tag in the {@code <head>}.
     *
     * <p>Uses default sort order 100.</p>
     *
     * @param content raw CSS text to inject
     * @return this builder for chaining
     */
    public PageMeta inlineCss(String content) { inlineCss.add(new InlineResource(content, JsPosition.HEAD, 100)); return this; }

    /**
     * Inject inline CSS with a custom sort order.
     *
     * @param content raw CSS text to inject
     * @param order   sort order relative to other CSS resources
     * @return this builder for chaining
     */
    public PageMeta inlineCss(String content, int order) { inlineCss.add(new InlineResource(content, JsPosition.HEAD, order)); return this; }

    /**
     * Inject inline JavaScript into the page as a {@code <script>} tag at BODY_END.
     *
     * <p>Uses default sort order 100 and BODY_END position.</p>
     *
     * @param content raw JavaScript text to inject
     * @return this builder for chaining
     */
    public PageMeta inlineJs(String content) { inlineJs.add(new InlineResource(content, JsPosition.BODY_END, 100)); return this; }

    /**
     * Inject inline JavaScript with custom position and sort order.
     *
     * @param content raw JavaScript text to inject
     * @param pos     where to inject: HEAD or BODY_END
     * @param order   sort order relative to other JS resources
     * @return this builder for chaining
     */
    public PageMeta inlineJs(String content, JsPosition pos, int order) { inlineJs.add(new InlineResource(content, pos, order)); return this; }

    // ── HTTP Response ────────────────────────────────────────────

    /**
     * Add a custom HTTP response header.
     *
     * <p>If the header already exists, its value is replaced.</p>
     *
     * @param name  the response header name (e.g. "X-Custom-Header")
     * @param value the header value
     * @return this builder for chaining
     */
    public PageMeta header(String name, String value) { headers.put(name, value); return this; }

    /**
     * Set the Cache-Control header.
     *
     * <p>Shorthand for {@code header("Cache-Control", directive)}.</p>
     *
     * @param directive the Cache-Control directive (e.g. "public, max-age=3600",
     *                  "no-cache, no-store")
     * @return this builder for chaining
     */
    public PageMeta cacheControl(String directive) { return header("Cache-Control", directive); }

    /**
     * Set the Content-Security-Policy header.
     *
     * <p>Shorthand for {@code header("Content-Security-Policy", policy)}.</p>
     *
     * @param policy the CSP directive string
     * @return this builder for chaining
     */
    public PageMeta contentSecurityPolicy(String policy) { return header("Content-Security-Policy", policy); }

    /**
     * Set the HTTP response status code.
     *
     * <p>Common values: 200 (OK, default), 404 (Not Found), 410 (Gone),
     * 500 (Internal Server Error).</p>
     *
     * @param code the HTTP status code
     * @return this builder for chaining
     */
    public PageMeta status(int code) { this.status = code; return this; }

    /**
     * Redirect to another URL with HTTP 302 (temporary). Halts rendering.
     *
     * <p>When a redirect is set, the renderer sends a redirect response
     * instead of rendering the page body.</p>
     *
     * @param url the target URL (relative path or absolute URL)
     * @return this builder for chaining
     */
    public PageMeta redirectTo(String url) { this.redirectUrl = url; this.redirectStatus = 302; return this; }

    /**
     * Redirect to another URL with a specific HTTP status code.
     *
     * @param url    the target URL (relative path or absolute URL)
     * @param status the redirect status code (301 permanent, 302 temporary, 307, 308)
     * @return this builder for chaining
     */
    public PageMeta redirectTo(String url, int status) { this.redirectUrl = url; this.redirectStatus = status; return this; }

    // ── HTML Root ────────────────────────────────────────────────

    /**
     * Set the {@code <html lang="...">} attribute. Required by WCAG 3.1.1.
     *
     * @param lang BCP 47 language tag (e.g. "en", "es", "ar", "zh-CN")
     * @return this builder for chaining
     */
    public PageMeta htmlLang(String lang) { this.htmlLang = lang; return this; }

    /**
     * Set the {@code <html dir="...">} attribute for text direction.
     *
     * <p>Required for RTL languages. Use {@link #autoDir()} to auto-detect
     * from the current locale.</p>
     *
     * @param dir text direction: "ltr" (left-to-right) or "rtl" (right-to-left)
     * @return this builder for chaining
     */
    public PageMeta htmlDir(String dir) { this.htmlDir = dir; return this; }

    /**
     * Add CSS classes to the {@code <body>} tag.
     *
     * <p>Useful for page-specific body styling or theme classes.</p>
     *
     * @param classes one or more CSS class names
     * @return this builder for chaining
     */
    public PageMeta bodyClass(String... classes) { Collections.addAll(bodyClasses, classes); return this; }

    /**
     * Add arbitrary attributes to the {@code <body>} tag.
     *
     * @param attrs map of attribute name to value
     * @return this builder for chaining
     */
    public PageMeta bodyAttrs(Map<String, String> attrs) { bodyAttrs.putAll(attrs); return this; }

    /**
     * Set an arbitrary attribute on the {@code <html>} tag.
     *
     * <p>Use for custom data attributes like {@code data-theme="dark"}.
     * The {@code lang} and {@code dir} attributes have dedicated methods
     * ({@link #htmlLang(String)} and {@link #htmlDir(String)}) and should
     * not be set via this method.</p>
     *
     * @param key   attribute name (e.g. "data-theme")
     * @param value attribute value (e.g. "dark")
     * @return this builder for chaining
     */
    public PageMeta htmlAttr(String key, String value) { htmlAttrs.put(key, value); return this; }

    // ── Structured Data ──────────────────────────────────────────

    /**
     * Inject a raw JSON-LD script into the {@code <head>} for search engine
     * rich results and knowledge panels.
     *
     * <p>Rendered as {@code <script type="application/ld+json">content</script>}.</p>
     *
     * @param script the raw JSON-LD string
     * @return this builder for chaining
     */
    public PageMeta jsonLd(String script) { jsonLdScripts.add(script); return this; }

    /**
     * Inject a JSON-LD script from a Java object.
     *
     * <p>The object is converted to a string via {@code toString()}. Full JSON
     * serialization via Jackson happens at render time in the {@code jux-server}
     * module.</p>
     *
     * @param data the structured data object (will be serialized to JSON)
     * @return this builder for chaining
     */
    public PageMeta jsonLd(Object data) {
        // Serialization to JSON happens at render time in jux-server
        jsonLdScripts.add(data.toString());
        return this;
    }

    // ── Performance Hints ────────────────────────────────────────

    /**
     * Add a resource preload hint.
     *
     * <p>Rendered as {@code <link rel="preload" href="..." as="...">}.
     * Instructs the browser to fetch a critical resource early in the page
     * load. Common uses: fonts, hero images, critical CSS.</p>
     *
     * @param href the resource URL to preload
     * @param as   the resource type (e.g. "font", "image", "style", "script")
     * @return this builder for chaining
     */
    public PageMeta preload(String href, String as) { preloads.add(new PreloadHint(href, as)); return this; }

    /**
     * Add a resource prefetch hint for likely future navigation.
     *
     * <p>Rendered as {@code <link rel="prefetch" href="...">}. Instructs the
     * browser to fetch a resource in idle time that is likely needed for a
     * future navigation (e.g. the next page the user is likely to visit).</p>
     *
     * @param href the resource URL to prefetch
     * @return this builder for chaining
     */
    public PageMeta prefetch(String href) { prefetches.add(href); return this; }

    /**
     * Add an origin preconnect hint.
     *
     * <p>Rendered as {@code <link rel="preconnect" href="...">}. Establishes
     * TCP and TLS connections early to a remote origin, reducing latency for
     * subsequent requests. Use for CDNs, API servers, and font providers.</p>
     *
     * @param origin the origin URL to preconnect to (e.g. "https://cdn.example.com")
     * @return this builder for chaining
     */
    public PageMeta preconnect(String origin) { preconnects.add(origin); return this; }

    /**
     * Add a DNS prefetch hint.
     *
     * <p>Rendered as {@code <link rel="dns-prefetch" href="...">}. Resolves
     * DNS for a remote origin early. Lighter than {@link #preconnect(String)}
     * -- only does DNS resolution, not TCP/TLS.</p>
     *
     * @param origin the origin URL to DNS-prefetch (e.g. "https://analytics.example.com")
     * @return this builder for chaining
     */
    public PageMeta dnsPrefetch(String origin) { dnsPrefetches.add(origin); return this; }

    // ── Conditional ──────────────────────────────────────────────

    /**
     * Apply metadata conditionally based on a boolean flag.
     *
     * <p>The block is only executed if the condition is {@code true}.
     * Useful for conditionally adding resources or meta tags based on
     * content properties:</p>
     * <pre>{@code
     * .when(post.hasCodeBlocks(), meta ->
     *     meta.css("vendor/prism.css").js("vendor/prism.js")
     * )
     * }</pre>
     *
     * @param condition the condition to evaluate
     * @param block     the metadata modifications to apply if condition is true
     * @return this builder for chaining
     */
    public PageMeta when(boolean condition, Consumer<PageMeta> block) {
        if (condition) block.accept(this);
        return this;
    }

    /**
     * Apply metadata only when a specific Spring profile is active.
     *
     * <p>The condition is evaluated at render time by the {@code jux-server}
     * module. Example: add noindex on staging:</p>
     * <pre>{@code
     * .whenProfile("staging", meta -> meta.robots("noindex,nofollow"))
     * }</pre>
     *
     * <p><b>Note:</b> This is a placeholder in jux-core. The actual profile
     * check is performed by the server module at render time.</p>
     *
     * @param profile the Spring profile name to check
     * @param block   the metadata modifications to apply if the profile is active
     * @return this builder for chaining
     */
    public PageMeta whenProfile(String profile, Consumer<PageMeta> block) {
        // Evaluated at render time by jux-server.
        // For now, store the profile condition for later evaluation.
        return this;
    }

    // ── i18n ─────────────────────────────────────────────────────

    /**
     * Auto-detect text direction (LTR/RTL) from the current locale and set
     * the {@code <html dir>} attribute accordingly.
     *
     * <p>RTL is detected for Arabic (ar), Hebrew (he), Farsi (fa), Urdu (ur),
     * and other right-to-left languages. The actual detection is performed at
     * render time by the {@code jux-server} module.</p>
     *
     * @return this builder for chaining
     */
    public PageMeta autoDir() {
        // Auto-detect RTL from locale -- resolved at render time by jux-server.
        return this;
    }

    /**
     * Manually set hreflang alternate URLs per locale.
     *
     * <p>Overrides auto-generation of hreflang tags for {@code @Localized} routes.
     * Each entry maps a locale to the corresponding page URL.</p>
     *
     * @param urls map of locale to URL string
     * @return this builder for chaining
     */
    public PageMeta alternateLocales(Map<Locale, String> urls) {
        urls.forEach((locale, url) -> alternates.put(locale.toLanguageTag(), url));
        return this;
    }

    /**
     * Set the {@code hreflang="x-default"} URL -- the fallback for users
     * whose language does not match any of the declared alternates.
     *
     * <p>Shorthand for {@code alternate("x-default", url)}.</p>
     *
     * @param url the default/fallback URL for unmatched locales
     * @return this builder for chaining
     */
    public PageMeta xDefault(String url) { return alternate("x-default", url); }

    // ── Merge ────────────────────────────────────────────────────

    /**
     * Merge another {@code PageMeta} into this one.
     *
     * <p>The other instance's non-null scalar values (title, canonical, status, etc.)
     * override this instance's values. Collection values (meta tags, CSS/JS resources,
     * headers, body classes, etc.) are additive -- entries from both instances are
     * combined. Map entries from the other instance override matching keys in this
     * instance.</p>
     *
     * <p>Used by the rendering pipeline to merge annotation-derived metadata with
     * programmatic metadata from {@link Page#pageMeta()}.</p>
     *
     * @param other the PageMeta to merge into this one, or null (no-op)
     * @return this builder for chaining
     */
    public PageMeta merge(PageMeta other) {
        if (other == null) return this;
        if (other.title != null) this.title = other.title;
        if (other.titleTemplate != null) this.titleTemplate = other.titleTemplate;
        this.metaNames.putAll(other.metaNames);
        this.metaProperties.putAll(other.metaProperties);
        this.httpEquivs.putAll(other.httpEquivs);
        if (other.canonical != null) this.canonical = other.canonical;
        this.alternates.putAll(other.alternates);
        if (other.faviconHref != null) {
            this.faviconHref = other.faviconHref;
            this.faviconType = other.faviconType;
            this.faviconSizes = other.faviconSizes;
        }
        this.cssResources.addAll(other.cssResources);
        this.jsResources.addAll(other.jsResources);
        this.removedCss.addAll(other.removedCss);
        this.removedJs.addAll(other.removedJs);
        this.inlineCss.addAll(other.inlineCss);
        this.inlineJs.addAll(other.inlineJs);
        this.headers.putAll(other.headers);
        if (other.status != 0) this.status = other.status;
        if (other.redirectUrl != null) { this.redirectUrl = other.redirectUrl; this.redirectStatus = other.redirectStatus; }
        if (other.htmlLang != null) this.htmlLang = other.htmlLang;
        if (other.htmlDir != null) this.htmlDir = other.htmlDir;
        this.bodyClasses.addAll(other.bodyClasses);
        this.bodyAttrs.putAll(other.bodyAttrs);
        this.htmlAttrs.putAll(other.htmlAttrs);
        this.jsonLdScripts.addAll(other.jsonLdScripts);
        this.preloads.addAll(other.preloads);
        this.prefetches.addAll(other.prefetches);
        this.preconnects.addAll(other.preconnects);
        this.dnsPrefetches.addAll(other.dnsPrefetches);
        return this;
    }

    // ── Getters (used by JuxRenderer) ────────────────────────────

    /**
     * Returns the raw page title, or null if not set.
     *
     * @return the page title text, or null
     */
    public String getTitle() { return title; }

    /**
     * Returns the title template string, or null if not set.
     *
     * @return the title template with {@code %s} placeholder, or null
     */
    public String getTitleTemplate() { return titleTemplate; }

    /**
     * Returns the fully resolved title with the template applied.
     *
     * <p>If a title template is set (e.g. {@code "%s | My Site"}) and a title
     * is set (e.g. {@code "About"}), this returns the formatted result
     * ({@code "About | My Site"}). If no template is set, returns the raw
     * title. Returns null if no title is set.</p>
     *
     * @return the resolved title string, or null if no title is set
     */
    public String getResolvedTitle() {
        if (title == null) return null;
        if (titleTemplate != null) return String.format(titleTemplate, title);
        return title;
    }

    /**
     * Returns all name-based meta tags as an unmodifiable map.
     *
     * @return map of meta name to content value
     */
    public Map<String, String> getMetaNames() { return Collections.unmodifiableMap(metaNames); }

    /**
     * Returns all property-based meta tags (OpenGraph) as an unmodifiable map.
     *
     * @return map of meta property to content value
     */
    public Map<String, String> getMetaProperties() { return Collections.unmodifiableMap(metaProperties); }

    /**
     * Returns all http-equiv meta tags as an unmodifiable map.
     *
     * @return map of http-equiv name to content value
     */
    public Map<String, String> getHttpEquivs() { return Collections.unmodifiableMap(httpEquivs); }

    /**
     * Returns the charset meta tag value.
     *
     * @return the character encoding, defaults to "UTF-8"
     */
    public String getCharset() { return charset; }

    /**
     * Returns the viewport meta tag content.
     *
     * @return the viewport string, defaults to "width=device-width, initial-scale=1"
     */
    public String getViewport() { return viewport; }

    /**
     * Returns the canonical URL, or null if not set.
     *
     * @return the canonical URL string, or null
     */
    public String getCanonical() { return canonical; }

    /**
     * Returns all hreflang alternate URLs as an unmodifiable map.
     *
     * @return map of hreflang code to URL
     */
    public Map<String, String> getAlternates() { return Collections.unmodifiableMap(alternates); }

    /**
     * Returns the favicon URL path, or null if not set.
     *
     * @return the favicon href, or null
     */
    public String getFaviconHref() { return faviconHref; }

    /**
     * Returns the favicon MIME type, or null if not set.
     *
     * @return the favicon type (e.g. "image/png"), or null
     */
    public String getFaviconType() { return faviconType; }

    /**
     * Returns the favicon size hint, or null if not set.
     *
     * @return the favicon sizes (e.g. "32x32"), or null
     */
    public String getFaviconSizes() { return faviconSizes; }

    /**
     * Returns the Apple touch icon URL, or null if not set.
     *
     * @return the apple-touch-icon href, or null
     */
    public String getAppleTouchIconHref() { return appleTouchIconHref; }

    /**
     * Returns all programmatically added CSS resources as an unmodifiable list.
     *
     * @return the CSS resource list
     */
    public List<CssResource> getCssResources() { return Collections.unmodifiableList(cssResources); }

    /**
     * Returns all programmatically added JavaScript resources as an unmodifiable list.
     *
     * @return the JS resource list
     */
    public List<JsResource> getJsResources() { return Collections.unmodifiableList(jsResources); }

    /**
     * Returns the set of CSS paths to remove from the annotation-declared pipeline.
     *
     * @return unmodifiable set of CSS paths to exclude
     */
    public Set<String> getRemovedCss() { return Collections.unmodifiableSet(removedCss); }

    /**
     * Returns the set of JS paths to remove from the annotation-declared pipeline.
     *
     * @return unmodifiable set of JS paths to exclude
     */
    public Set<String> getRemovedJs() { return Collections.unmodifiableSet(removedJs); }

    /**
     * Returns all inline CSS blocks as an unmodifiable list.
     *
     * @return the inline CSS resource list
     */
    public List<InlineResource> getInlineCss() { return Collections.unmodifiableList(inlineCss); }

    /**
     * Returns all inline JavaScript blocks as an unmodifiable list.
     *
     * @return the inline JS resource list
     */
    public List<InlineResource> getInlineJs() { return Collections.unmodifiableList(inlineJs); }

    /**
     * Returns all custom HTTP response headers as an unmodifiable map.
     *
     * @return map of header name to value
     */
    public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }

    /**
     * Returns the HTTP response status code.
     *
     * @return the status code, or 0 if not explicitly set (defaults to 200)
     */
    public int getStatus() { return status; }

    /**
     * Returns the redirect target URL, or null if no redirect is configured.
     *
     * @return the redirect URL, or null
     */
    public String getRedirectUrl() { return redirectUrl; }

    /**
     * Returns the HTTP status code for the redirect.
     *
     * @return the redirect status (e.g. 301, 302), or 0 if no redirect
     */
    public int getRedirectStatus() { return redirectStatus; }

    /**
     * Returns the HTML lang attribute value, or null if not set.
     *
     * @return BCP 47 language tag, or null
     */
    public String getHtmlLang() { return htmlLang; }

    /**
     * Returns the HTML dir attribute value, or null if not set.
     *
     * @return "ltr" or "rtl", or null
     */
    public String getHtmlDir() { return htmlDir; }

    /**
     * Returns the CSS classes to add to the body tag as an unmodifiable list.
     *
     * @return body class names
     */
    public List<String> getBodyClasses() { return Collections.unmodifiableList(bodyClasses); }

    /**
     * Returns the attributes to add to the body tag as an unmodifiable map.
     *
     * @return map of attribute name to value
     */
    public Map<String, String> getBodyAttrs() { return Collections.unmodifiableMap(bodyAttrs); }

    /**
     * Returns the attributes to add to the html tag as an unmodifiable map.
     *
     * @return map of attribute name to value
     */
    public Map<String, String> getHtmlAttrs() { return Collections.unmodifiableMap(htmlAttrs); }

    /**
     * Returns all JSON-LD scripts as an unmodifiable list.
     *
     * @return list of JSON-LD content strings
     */
    public List<String> getJsonLdScripts() { return Collections.unmodifiableList(jsonLdScripts); }

    /**
     * Returns all preload hints as an unmodifiable list.
     *
     * @return list of preload hint descriptors
     */
    public List<PreloadHint> getPreloads() { return Collections.unmodifiableList(preloads); }

    /**
     * Returns all prefetch URLs as an unmodifiable list.
     *
     * @return list of prefetch URLs
     */
    public List<String> getPrefetches() { return Collections.unmodifiableList(prefetches); }

    /**
     * Returns all preconnect origins as an unmodifiable list.
     *
     * @return list of preconnect origin URLs
     */
    public List<String> getPreconnects() { return Collections.unmodifiableList(preconnects); }

    /**
     * Returns all DNS prefetch origins as an unmodifiable list.
     *
     * @return list of DNS prefetch origin URLs
     */
    public List<String> getDnsPrefetches() { return Collections.unmodifiableList(dnsPrefetches); }

    /**
     * Returns whether a redirect is configured on this PageMeta.
     *
     * @return {@code true} if {@link #redirectTo(String)} or
     *         {@link #redirectTo(String, int)} has been called
     */
    public boolean hasRedirect() { return redirectUrl != null; }

    // ── Inner Types ──────────────────────────────────────────────

    /**
     * Descriptor for an inline CSS or JavaScript resource to be rendered as a
     * {@code <style>} or {@code <script>} tag in the HTML output.
     *
     * @param content  the raw CSS or JavaScript text content
     * @param position where to inject in the HTML document: HEAD or BODY_END
     * @param order    sort order relative to other resources -- lower numbers render first
     */
    public record InlineResource(String content, JsPosition position, int order) {}

    /**
     * Descriptor for a resource preload hint, rendered as
     * {@code <link rel="preload" href="..." as="...">} in the HTML {@code <head>}.
     *
     * @param href the URL of the resource to preload
     * @param as   the resource type hint (e.g. "font", "image", "style", "script", "fetch")
     */
    public record PreloadHint(String href, String as) {}
}
