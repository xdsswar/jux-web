package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.CssPosition;
import xss.it.jux.annotation.JsPosition;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link PageMeta} programmatic metadata builder.
 *
 * <p>Covers all builder methods, getter consistency, the {@code when()} conditional,
 * the {@code merge()} method, and the title template resolution logic.</p>
 */
class PageMetaTest {

    // ── Factory ───────────────────────────────────────────────────────

    @Test
    @DisplayName("create() returns a non-null instance")
    void createReturnsNonNull() {
        assertThat(PageMeta.create()).isNotNull();
    }

    // ── Title ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Title")
    class Title {

        @Test
        @DisplayName("title() stores the title")
        void titleSetsValue() {
            PageMeta meta = PageMeta.create().title("Hello");
            assertThat(meta.getTitle()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("titleTemplate() stores the template")
        void titleTemplateSetsValue() {
            PageMeta meta = PageMeta.create().titleTemplate("%s | Site");
            assertThat(meta.getTitleTemplate()).isEqualTo("%s | Site");
        }

        @Test
        @DisplayName("getResolvedTitle() applies template to title")
        void resolvedTitleAppliesTemplate() {
            PageMeta meta = PageMeta.create().title("Hello").titleTemplate("%s | Site");
            assertThat(meta.getResolvedTitle()).isEqualTo("Hello | Site");
        }

        @Test
        @DisplayName("getResolvedTitle() returns raw title when no template is set")
        void resolvedTitleWithoutTemplate() {
            PageMeta meta = PageMeta.create().title("Hello");
            assertThat(meta.getResolvedTitle()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("getResolvedTitle() returns null when no title is set")
        void resolvedTitleNullWhenNoTitle() {
            PageMeta meta = PageMeta.create().titleTemplate("%s | Site");
            assertThat(meta.getResolvedTitle()).isNull();
        }
    }

    // ── Meta Tags ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Meta tags")
    class MetaTags {

        @Test
        @DisplayName("meta(name, content) stores in metaNames")
        void metaNameStored() {
            PageMeta meta = PageMeta.create().meta("description", "desc");
            assertThat(meta.getMetaNames().get("description")).isEqualTo("desc");
        }

        @Test
        @DisplayName("metaProperty(property, content) stores in metaProperties")
        void metaPropertyStored() {
            PageMeta meta = PageMeta.create().metaProperty("og:title", "T");
            assertThat(meta.getMetaProperties().get("og:title")).isEqualTo("T");
        }

        @Test
        @DisplayName("description() is shorthand for meta('description', ...)")
        void descriptionShorthand() {
            PageMeta meta = PageMeta.create().description("d");
            assertThat(meta.getMetaNames().get("description")).isEqualTo("d");
        }

        @Test
        @DisplayName("httpEquiv() stores in httpEquivs")
        void httpEquivStored() {
            PageMeta meta = PageMeta.create().httpEquiv("refresh", "5");
            assertThat(meta.getHttpEquivs().get("refresh")).isEqualTo("5");
        }

        @Test
        @DisplayName("charset defaults to UTF-8")
        void charsetDefault() {
            assertThat(PageMeta.create().getCharset()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("viewport defaults to 'width=device-width, initial-scale=1'")
        void viewportDefault() {
            assertThat(PageMeta.create().getViewport()).isEqualTo("width=device-width, initial-scale=1");
        }
    }

    // ── OpenGraph ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("OpenGraph methods")
    class OpenGraph {

        @Test void ogTitle()       { assertThat(PageMeta.create().ogTitle("T").getMetaProperties().get("og:title")).isEqualTo("T"); }
        @Test void ogDescription() { assertThat(PageMeta.create().ogDescription("D").getMetaProperties().get("og:description")).isEqualTo("D"); }
        @Test void ogImage()       { assertThat(PageMeta.create().ogImage("/img.png").getMetaProperties().get("og:image")).isEqualTo("/img.png"); }
        @Test void ogImageAlt()    { assertThat(PageMeta.create().ogImageAlt("alt").getMetaProperties().get("og:image:alt")).isEqualTo("alt"); }
        @Test void ogType()        { assertThat(PageMeta.create().ogType("article").getMetaProperties().get("og:type")).isEqualTo("article"); }
        @Test void ogUrl()         { assertThat(PageMeta.create().ogUrl("/u").getMetaProperties().get("og:url")).isEqualTo("/u"); }
        @Test void ogSiteName()    { assertThat(PageMeta.create().ogSiteName("MySite").getMetaProperties().get("og:site_name")).isEqualTo("MySite"); }
        @Test void ogLocale()      { assertThat(PageMeta.create().ogLocale("en_US").getMetaProperties().get("og:locale")).isEqualTo("en_US"); }
    }

    // ── Twitter Cards ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Twitter Card methods")
    class Twitter {

        @Test void twitterCard()        { assertThat(PageMeta.create().twitterCard("summary").getMetaNames().get("twitter:card")).isEqualTo("summary"); }
        @Test void twitterSite()        { assertThat(PageMeta.create().twitterSite("@site").getMetaNames().get("twitter:site")).isEqualTo("@site"); }
        @Test void twitterCreator()     { assertThat(PageMeta.create().twitterCreator("@me").getMetaNames().get("twitter:creator")).isEqualTo("@me"); }
        @Test void twitterTitle()       { assertThat(PageMeta.create().twitterTitle("T").getMetaNames().get("twitter:title")).isEqualTo("T"); }
        @Test void twitterDescription() { assertThat(PageMeta.create().twitterDescription("D").getMetaNames().get("twitter:description")).isEqualTo("D"); }
        @Test void twitterImage()       { assertThat(PageMeta.create().twitterImage("/i.png").getMetaNames().get("twitter:image")).isEqualTo("/i.png"); }
        @Test void twitterImageAlt()    { assertThat(PageMeta.create().twitterImageAlt("alt").getMetaNames().get("twitter:image:alt")).isEqualTo("alt"); }
    }

    // ── Link Tags ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Link tags")
    class LinkTags {

        @Test
        @DisplayName("canonical() stores canonical URL")
        void canonical() {
            assertThat(PageMeta.create().canonical("/about").getCanonical()).isEqualTo("/about");
        }

        @Test
        @DisplayName("favicon(href) stores favicon href")
        void faviconSimple() {
            assertThat(PageMeta.create().favicon("/icon.png").getFaviconHref()).isEqualTo("/icon.png");
        }

        @Test
        @DisplayName("favicon(href, type, sizes) stores all three fields")
        void faviconFull() {
            PageMeta meta = PageMeta.create().favicon("/icon.png", "image/png", "32x32");
            assertThat(meta.getFaviconHref()).isEqualTo("/icon.png");
            assertThat(meta.getFaviconType()).isEqualTo("image/png");
            assertThat(meta.getFaviconSizes()).isEqualTo("32x32");
        }

        @Test
        @DisplayName("appleTouchIcon() stores the href")
        void appleTouchIcon() {
            assertThat(PageMeta.create().appleTouchIcon("/touch.png").getAppleTouchIconHref())
                    .isEqualTo("/touch.png");
        }
    }

    // ── CSS Resources ─────────────────────────────────────────────────

    @Nested
    @DisplayName("CSS resources")
    class CssResources {

        @Test
        @DisplayName("css(path) adds resource with path")
        void cssSimple() {
            PageMeta meta = PageMeta.create().css("main.css");
            assertThat(meta.getCssResources()).hasSize(1);
            assertThat(meta.getCssResources().getFirst().path()).isEqualTo("main.css");
        }

        @Test
        @DisplayName("css(path, order) sets custom order")
        void cssWithOrder() {
            PageMeta meta = PageMeta.create().css("main.css", 10);
            assertThat(meta.getCssResources().getFirst().order()).isEqualTo(10);
        }

        @Test
        @DisplayName("css(path, order, position) sets custom order and position")
        void cssWithOrderAndPosition() {
            PageMeta meta = PageMeta.create().css("main.css", 5, CssPosition.BODY_END);
            CssResource res = meta.getCssResources().getFirst();
            assertThat(res.order()).isEqualTo(5);
            assertThat(res.position()).isEqualTo(CssPosition.BODY_END);
        }

        @Test
        @DisplayName("removeCss() adds path to removed set")
        void removeCss() {
            PageMeta meta = PageMeta.create().removeCss("x.css");
            assertThat(meta.getRemovedCss()).contains("x.css");
        }
    }

    // ── JS Resources ──────────────────────────────────────────────────

    @Nested
    @DisplayName("JS resources")
    class JsResources {

        @Test
        @DisplayName("js(path) adds a JS resource")
        void jsSimple() {
            PageMeta meta = PageMeta.create().js("app.js");
            assertThat(meta.getJsResources()).hasSize(1);
            assertThat(meta.getJsResources().getFirst().path()).isEqualTo("app.js");
        }

        @Test
        @DisplayName("js(path, order) sets custom order")
        void jsWithOrder() {
            PageMeta meta = PageMeta.create().js("app.js", 10);
            assertThat(meta.getJsResources().getFirst().order()).isEqualTo(10);
        }

        @Test
        @DisplayName("removeJs() adds path to removed set")
        void removeJs() {
            PageMeta meta = PageMeta.create().removeJs("x.js");
            assertThat(meta.getRemovedJs()).contains("x.js");
        }
    }

    // ── Inline Resources ──────────────────────────────────────────────

    @Nested
    @DisplayName("Inline resources")
    class InlineResources {

        @Test
        @DisplayName("inlineCss() adds inline CSS block")
        void inlineCss() {
            PageMeta meta = PageMeta.create().inlineCss("body{}");
            assertThat(meta.getInlineCss()).hasSize(1);
            assertThat(meta.getInlineCss().getFirst().content()).isEqualTo("body{}");
        }

        @Test
        @DisplayName("inlineJs() adds inline JS block")
        void inlineJs() {
            PageMeta meta = PageMeta.create().inlineJs("alert(1)");
            assertThat(meta.getInlineJs()).hasSize(1);
            assertThat(meta.getInlineJs().getFirst().content()).isEqualTo("alert(1)");
        }

        @Test
        @DisplayName("inlineCss(content, order) sets custom order")
        void inlineCssWithOrder() {
            PageMeta meta = PageMeta.create().inlineCss("body{}", 5);
            assertThat(meta.getInlineCss().getFirst().order()).isEqualTo(5);
        }

        @Test
        @DisplayName("inlineJs(content, position, order) sets custom position and order")
        void inlineJsWithPositionAndOrder() {
            PageMeta meta = PageMeta.create().inlineJs("init()", JsPosition.HEAD, 1);
            PageMeta.InlineResource res = meta.getInlineJs().getFirst();
            assertThat(res.content()).isEqualTo("init()");
            assertThat(res.position()).isEqualTo(JsPosition.HEAD);
            assertThat(res.order()).isEqualTo(1);
        }
    }

    // ── HTTP Response ─────────────────────────────────────────────────

    @Nested
    @DisplayName("HTTP response")
    class HttpResponse {

        @Test
        @DisplayName("status() sets status code")
        void statusCode() {
            assertThat(PageMeta.create().status(404).getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("redirectTo(url) sets redirect with 302")
        void redirectDefault() {
            PageMeta meta = PageMeta.create().redirectTo("/login");
            assertThat(meta.hasRedirect()).isTrue();
            assertThat(meta.getRedirectUrl()).isEqualTo("/login");
            assertThat(meta.getRedirectStatus()).isEqualTo(302);
        }

        @Test
        @DisplayName("redirectTo(url, status) sets redirect with custom status")
        void redirectCustomStatus() {
            PageMeta meta = PageMeta.create().redirectTo("/login", 301);
            assertThat(meta.getRedirectStatus()).isEqualTo(301);
        }

        @Test
        @DisplayName("hasRedirect() is false when no redirect is set")
        void noRedirectByDefault() {
            assertThat(PageMeta.create().hasRedirect()).isFalse();
        }

        @Test
        @DisplayName("header() adds custom response header")
        void headerAdds() {
            PageMeta meta = PageMeta.create().header("X-Custom", "val");
            assertThat(meta.getHeaders().get("X-Custom")).isEqualTo("val");
        }

        @Test
        @DisplayName("cacheControl() is shorthand for Cache-Control header")
        void cacheControl() {
            PageMeta meta = PageMeta.create().cacheControl("no-cache");
            assertThat(meta.getHeaders().get("Cache-Control")).isEqualTo("no-cache");
        }

        @Test
        @DisplayName("contentSecurityPolicy() is shorthand for CSP header")
        void csp() {
            PageMeta meta = PageMeta.create().contentSecurityPolicy("default-src 'self'");
            assertThat(meta.getHeaders().get("Content-Security-Policy")).isEqualTo("default-src 'self'");
        }
    }

    // ── HTML Root Element ─────────────────────────────────────────────

    @Nested
    @DisplayName("HTML root attributes")
    class HtmlRoot {

        @Test
        @DisplayName("htmlLang() sets lang")
        void htmlLang() {
            assertThat(PageMeta.create().htmlLang("en").getHtmlLang()).isEqualTo("en");
        }

        @Test
        @DisplayName("htmlDir() sets dir")
        void htmlDir() {
            assertThat(PageMeta.create().htmlDir("rtl").getHtmlDir()).isEqualTo("rtl");
        }

        @Test
        @DisplayName("bodyClass() adds body CSS classes")
        void bodyClass() {
            PageMeta meta = PageMeta.create().bodyClass("dark", "compact");
            assertThat(meta.getBodyClasses()).containsExactly("dark", "compact");
        }
    }

    // ── HTML Attributes ──────────────────────────────────────────────

    @Nested
    @DisplayName("HTML attributes")
    class HtmlAttrs {

        @Test
        @DisplayName("htmlAttr() sets an attribute on the html tag")
        void htmlAttr_setsAttribute() {
            PageMeta meta = PageMeta.create().htmlAttr("data-theme", "dark");
            assertThat(meta.getHtmlAttrs()).containsEntry("data-theme", "dark");
        }

        @Test
        @DisplayName("htmlAttr() supports multiple attributes")
        void htmlAttr_multipleAttributes() {
            PageMeta meta = PageMeta.create()
                    .htmlAttr("data-theme", "dark")
                    .htmlAttr("data-version", "2");
            assertThat(meta.getHtmlAttrs())
                    .containsEntry("data-theme", "dark")
                    .containsEntry("data-version", "2");
        }

        @Test
        @DisplayName("getHtmlAttrs() returns an unmodifiable map")
        void htmlAttr_returnsUnmodifiableMap() {
            PageMeta meta = PageMeta.create().htmlAttr("data-theme", "dark");
            assertThatThrownBy(() -> meta.getHtmlAttrs().put("new", "val"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getHtmlAttrs() is empty by default")
        void htmlAttrs_emptyByDefault() {
            assertThat(PageMeta.create().getHtmlAttrs()).isEmpty();
        }
    }

    // ── Structured Data ───────────────────────────────────────────────

    @Test
    @DisplayName("jsonLd(String) adds a script to the list")
    void jsonLdString() {
        PageMeta meta = PageMeta.create().jsonLd("{}");
        assertThat(meta.getJsonLdScripts()).containsExactly("{}");
    }

    @Test
    @DisplayName("jsonLd(Object) adds the toString() representation")
    void jsonLdObject() {
        Object data = new Object() {
            @Override public String toString() { return "{\"type\":\"Article\"}"; }
        };
        PageMeta meta = PageMeta.create().jsonLd(data);
        assertThat(meta.getJsonLdScripts()).containsExactly("{\"type\":\"Article\"}");
    }

    // ── Performance Hints ─────────────────────────────────────────────

    @Nested
    @DisplayName("Performance hints")
    class PerfHints {

        @Test
        @DisplayName("preload() adds a preload hint")
        void preload() {
            PageMeta meta = PageMeta.create().preload("/f.woff2", "font");
            assertThat(meta.getPreloads()).hasSize(1);
            assertThat(meta.getPreloads().getFirst().href()).isEqualTo("/f.woff2");
            assertThat(meta.getPreloads().getFirst().as()).isEqualTo("font");
        }

        @Test
        @DisplayName("prefetch() adds a prefetch URL")
        void prefetch() {
            PageMeta meta = PageMeta.create().prefetch("/next");
            assertThat(meta.getPrefetches()).containsExactly("/next");
        }

        @Test
        @DisplayName("preconnect() adds a preconnect origin")
        void preconnect() {
            PageMeta meta = PageMeta.create().preconnect("https://cdn.example.com");
            assertThat(meta.getPreconnects()).containsExactly("https://cdn.example.com");
        }

        @Test
        @DisplayName("dnsPrefetch() adds a DNS prefetch origin")
        void dnsPrefetch() {
            PageMeta meta = PageMeta.create().dnsPrefetch("https://cdn.example.com");
            assertThat(meta.getDnsPrefetches()).containsExactly("https://cdn.example.com");
        }
    }

    // ── Conditional ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Conditional when()")
    class Conditional {

        @Test
        @DisplayName("when(true) applies the block")
        void whenTrue() {
            PageMeta meta = PageMeta.create().when(true, m -> m.title("Yes"));
            assertThat(meta.getTitle()).isEqualTo("Yes");
        }

        @Test
        @DisplayName("when(false) does not apply the block")
        void whenFalse() {
            PageMeta meta = PageMeta.create().when(false, m -> m.title("No"));
            assertThat(meta.getTitle()).isNull();
        }
    }

    // ── Merge ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("merge()")
    class Merge {

        @Test
        @DisplayName("merge overrides scalar values from other")
        void mergeOverridesScalars() {
            PageMeta base = PageMeta.create().title("Base").htmlLang("en").status(200);
            PageMeta other = PageMeta.create().title("Other").htmlLang("es").status(404);
            base.merge(other);
            assertThat(base.getTitle()).isEqualTo("Other");
            assertThat(base.getHtmlLang()).isEqualTo("es");
            assertThat(base.getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("merge adds collections from other")
        void mergeAddsCollections() {
            PageMeta base = PageMeta.create().css("a.css").jsonLd("{}");
            PageMeta other = PageMeta.create().css("b.css").jsonLd("{\"x\":1}");
            base.merge(other);
            assertThat(base.getCssResources()).hasSize(2);
            assertThat(base.getJsonLdScripts()).hasSize(2);
        }

        @Test
        @DisplayName("merge with null is a no-op")
        void mergeNullIsNoOp() {
            PageMeta meta = PageMeta.create().title("Hi");
            meta.merge(null);
            assertThat(meta.getTitle()).isEqualTo("Hi");
        }

        @Test
        @DisplayName("merge does not override scalar with null")
        void mergeDoesNotOverrideWithNull() {
            PageMeta base = PageMeta.create().title("Base").canonical("/about");
            PageMeta other = PageMeta.create(); // title and canonical are null
            base.merge(other);
            assertThat(base.getTitle()).isEqualTo("Base");
            assertThat(base.getCanonical()).isEqualTo("/about");
        }

        @Test
        @DisplayName("merge overrides redirect from other")
        void mergeOverridesRedirect() {
            PageMeta base = PageMeta.create();
            PageMeta other = PageMeta.create().redirectTo("/new", 301);
            base.merge(other);
            assertThat(base.hasRedirect()).isTrue();
            assertThat(base.getRedirectUrl()).isEqualTo("/new");
            assertThat(base.getRedirectStatus()).isEqualTo(301);
        }

        @Test
        @DisplayName("merge combines meta name maps, other overrides matching keys")
        void mergeCombinesMetaNames() {
            PageMeta base = PageMeta.create().meta("description", "old").meta("robots", "index");
            PageMeta other = PageMeta.create().meta("description", "new").meta("author", "jux");
            base.merge(other);
            assertThat(base.getMetaNames().get("description")).isEqualTo("new");
            assertThat(base.getMetaNames().get("robots")).isEqualTo("index");
            assertThat(base.getMetaNames().get("author")).isEqualTo("jux");
        }

        @Test
        @DisplayName("merge combines htmlAttrs from both sides")
        void merge_combinesHtmlAttrs() {
            PageMeta base = PageMeta.create().htmlAttr("data-theme", "dark");
            PageMeta other = PageMeta.create().htmlAttr("data-version", "2");
            base.merge(other);
            assertThat(base.getHtmlAttrs())
                    .containsEntry("data-theme", "dark")
                    .containsEntry("data-version", "2");
        }

        @Test
        @DisplayName("merge htmlAttrs from other override existing keys")
        void merge_htmlAttrsFromOtherOverrideExistingKeys() {
            PageMeta base = PageMeta.create().htmlAttr("data-theme", "light");
            PageMeta other = PageMeta.create().htmlAttr("data-theme", "dark");
            base.merge(other);
            assertThat(base.getHtmlAttrs().get("data-theme")).isEqualTo("dark");
        }
    }
}
