package xss.it.jux.server.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.PageMeta;

import static org.assertj.core.api.Assertions.assertThat;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link JuxRenderer} -- the SSR engine that serializes Element trees
 * and full Component + PageMeta into HTML5 document strings.
 */
class JuxRendererTest {

    private JuxRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new JuxRenderer();
    }

    // ── Test Component ──────────────────────────────────────────────

    /**
     * Minimal Component subclass that returns a fixed Element tree for testing.
     */
    static class TestComponent extends Component {
        private final Element tree;

        TestComponent(Element tree) {
            this.tree = tree;
        }

        @Override
        public Element render() {
            return tree;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  renderElement() tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("renderElement()")
    class RenderElementTests {

        @Test
        @DisplayName("simple div renders open and close tags")
        void simpleDivRendersOpenAndCloseTags() {
            String html = renderer.renderElement(div());
            assertThat(html).isEqualTo("<div></div>");
        }

        @Test
        @DisplayName("div with text content renders text inside tags")
        void divWithTextContent() {
            String html = renderer.renderElement(div().text("Hello"));
            assertThat(html).isEqualTo("<div>Hello</div>");
        }

        @Test
        @DisplayName("children render in order")
        void childrenRenderInOrder() {
            Element el = div().children(
                span().text("A"),
                span().text("B")
            );
            String html = renderer.renderElement(el);
            assertThat(html).isEqualTo("<div><span>A</span><span>B</span></div>");
        }

        @Test
        @DisplayName("text content is HTML-escaped to prevent XSS")
        void textContentIsHtmlEscaped() {
            String html = renderer.renderElement(div().text("<script>"));
            assertThat(html).contains("&lt;script&gt;");
            assertThat(html).doesNotContain("<script>");
        }

        @Test
        @DisplayName("img is self-closing (void element)")
        void imgIsSelfClosing() {
            Element el = Element.of("img").attr("src", "x.png").attr("alt", "test");
            String html = renderer.renderElement(el);
            assertThat(html).startsWith("<img");
            assertThat(html).contains("src=\"x.png\"");
            assertThat(html).contains("alt=\"test\"");
            assertThat(html).doesNotContain("</img>");
        }

        @Test
        @DisplayName("br is self-closing (void element)")
        void brIsSelfClosing() {
            String html = renderer.renderElement(br());
            assertThat(html).startsWith("<br");
            assertThat(html).doesNotContain("</br>");
        }

        @Test
        @DisplayName("hr is self-closing (void element)")
        void hrIsSelfClosing() {
            String html = renderer.renderElement(hr());
            assertThat(html).startsWith("<hr");
            assertThat(html).doesNotContain("</hr>");
        }

        @Test
        @DisplayName("input is self-closing (void element)")
        void inputIsSelfClosing() {
            String html = renderer.renderElement(input().attr("type", "text"));
            assertThat(html).startsWith("<input");
            assertThat(html).doesNotContain("</input>");
        }

        @Test
        @DisplayName("id and cls attributes render correctly")
        void idAndClsAttributes() {
            String html = renderer.renderElement(div().id("x").cls("a", "b"));
            assertThat(html).contains("id=\"x\"");
            assertThat(html).contains("class=\"a b\"");
        }

        @Test
        @DisplayName("inline styles render as style attribute")
        void inlineStylesRender() {
            String html = renderer.renderElement(
                div().style("color", "red").style("padding", "1rem")
            );
            assertThat(html).contains("style=\"color: red; padding: 1rem\"");
        }

        @Test
        @DisplayName("role attribute renders")
        void roleAttributeRenders() {
            String html = renderer.renderElement(div().role("button"));
            assertThat(html).contains("role=\"button\"");
        }

        @Test
        @DisplayName("aria attributes render with aria- prefix")
        void ariaAttributeRenders() {
            String html = renderer.renderElement(div().aria("label", "Close"));
            assertThat(html).contains("aria-label=\"Close\"");
        }

        @Test
        @DisplayName("nested elements render recursively")
        void nestedElementsRenderRecursively() {
            Element el = div().children(
                section().children(
                    h1().text("Title"),
                    p().text("Content")
                )
            );
            String html = renderer.renderElement(el);
            assertThat(html).contains("<section>");
            assertThat(html).contains("<h1>Title</h1>");
            assertThat(html).contains("<p>Content</p>");
            assertThat(html).contains("</section>");
            assertThat(html).contains("</div>");
        }

        @Test
        @DisplayName("attribute values are HTML-escaped")
        void attributeValuesAreHtmlEscaped() {
            String html = renderer.renderElement(
                div().attr("data-value", "a\"b<c>d")
            );
            assertThat(html).contains("data-value=\"a&quot;b&lt;c&gt;d\"");
        }

        @Test
        @DisplayName("empty div has no extra whitespace")
        void emptyDivNoWhitespace() {
            String html = renderer.renderElement(div());
            assertThat(html).isEqualTo("<div></div>");
        }

        @Test
        @DisplayName("semantic elements render with their correct tags")
        void semanticElementsRender() {
            String html = renderer.renderElement(
                main_().children(
                    nav().text("Nav"),
                    article().text("Article")
                )
            );
            assertThat(html).contains("<main>");
            assertThat(html).contains("<nav>Nav</nav>");
            assertThat(html).contains("<article>Article</article>");
            assertThat(html).contains("</main>");
        }

        @Test
        @DisplayName("event handlers produce data-jux-events attribute")
        void eventHandlersProduceDataAttribute() {
            String html = renderer.renderElement(
                div().on("click", e -> {})
            );
            assertThat(html).contains("data-jux-events=\"click\"");
        }

        @Test
        @DisplayName("link (void element) is self-closing")
        void linkIsSelfClosing() {
            Element el = Element.of("link").attr("rel", "stylesheet").attr("href", "style.css");
            String html = renderer.renderElement(el);
            assertThat(html).startsWith("<link");
            assertThat(html).doesNotContain("</link>");
        }

        @Test
        @DisplayName("meta (void element) is self-closing")
        void metaIsSelfClosing() {
            Element el = Element.of("meta").attr("charset", "UTF-8");
            String html = renderer.renderElement(el);
            assertThat(html).startsWith("<meta");
            assertThat(html).doesNotContain("</meta>");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  render(Component, PageMeta) tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("render(Component, PageMeta)")
    class RenderFullDocumentTests {

        @Test
        @DisplayName("returns full HTML5 document starting with DOCTYPE")
        void returnsHtml5Doctype() {
            Component comp = new TestComponent(div().text("Hello"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).startsWith("<!DOCTYPE html>");
        }

        @Test
        @DisplayName("contains html open and close tags")
        void containsHtmlTags() {
            Component comp = new TestComponent(div().text("Test"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<html");
            assertThat(html).contains("</html>");
        }

        @Test
        @DisplayName("contains head open and close tags")
        void containsHeadTags() {
            Component comp = new TestComponent(div().text("Test"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<head>");
            assertThat(html).contains("</head>");
        }

        @Test
        @DisplayName("contains body open and close tags")
        void containsBodyTags() {
            Component comp = new TestComponent(div().text("Test"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<body>");
            assertThat(html).contains("</body>");
        }

        @Test
        @DisplayName("page title renders in head section")
        void pageTitleRendersInHead() {
            PageMeta meta = PageMeta.create().title("Hello");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<title>Hello</title>");
        }

        @Test
        @DisplayName("htmlLang renders on html tag")
        void htmlLangRendersOnHtmlTag() {
            PageMeta meta = PageMeta.create().htmlLang("en");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<html lang=\"en\"");
        }

        @Test
        @DisplayName("htmlDir renders on html tag")
        void htmlDirRendersOnHtmlTag() {
            PageMeta meta = PageMeta.create().htmlDir("rtl");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("dir=\"rtl\"");
        }

        @Test
        @DisplayName("meta name tag renders in head")
        void metaNameTagRendersInHead() {
            PageMeta meta = PageMeta.create().meta("description", "test");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<meta name=\"description\" content=\"test\"");
        }

        @Test
        @DisplayName("CSS resource renders as link tag")
        void cssResourceRendersAsLinkTag() {
            PageMeta meta = PageMeta.create().css("main.css");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<link rel=\"stylesheet\" href=\"main.css\"");
        }

        @Test
        @DisplayName("JS resource renders as script tag")
        void jsResourceRendersAsScriptTag() {
            PageMeta meta = PageMeta.create().js("app.js");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<script src=\"app.js\"");
        }

        @Test
        @DisplayName("favicon renders as link rel=icon")
        void faviconRendersAsLink() {
            PageMeta meta = PageMeta.create().favicon("/icon.ico");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<link rel=\"icon\" href=\"/icon.ico\"");
        }

        @Test
        @DisplayName("canonical renders as link rel=canonical")
        void canonicalRendersAsLink() {
            PageMeta meta = PageMeta.create().canonical("/about");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<link rel=\"canonical\" href=\"/about\"");
        }

        @Test
        @DisplayName("CSS defaults to HEAD position")
        void cssDefaultsToHeadPosition() {
            PageMeta meta = PageMeta.create().css("head-style.css");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            // CSS link should appear inside <head>...</head>
            int headEnd = html.indexOf("</head>");
            int linkPos = html.indexOf("head-style.css");
            assertThat(linkPos).isGreaterThan(0);
            assertThat(linkPos).isLessThan(headEnd);
        }

        @Test
        @DisplayName("JS defaults to BODY_END position")
        void jsDefaultsToBodyEndPosition() {
            PageMeta meta = PageMeta.create().js("body-script.js");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            // JS script should appear inside <body>...</body> after the content
            int bodyEnd = html.indexOf("</body>");
            int scriptPos = html.indexOf("body-script.js");
            assertThat(scriptPos).isGreaterThan(0);
            assertThat(scriptPos).isLessThan(bodyEnd);
        }

        @Test
        @DisplayName("title with template applies formatting")
        void titleWithTemplateApplied() {
            PageMeta meta = PageMeta.create().title("About").titleTemplate("%s | My Site");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<title>About | My Site</title>");
        }

        @Test
        @DisplayName("charset meta renders in head")
        void charsetMetaRendersInHead() {
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<meta charset=\"UTF-8\"");
        }

        @Test
        @DisplayName("viewport meta renders in head")
        void viewportMetaRendersInHead() {
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<meta name=\"viewport\"");
        }

        @Test
        @DisplayName("body classes render on body tag")
        void bodyClassesRender() {
            PageMeta meta = PageMeta.create().bodyClass("dark", "admin");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<body class=\"dark admin\"");
        }

        @Test
        @DisplayName("component render output appears in body")
        void componentOutputAppearsInBody() {
            Component comp = new TestComponent(
                main_().children(h1().text("Welcome"))
            );
            String html = renderer.render(comp, PageMeta.create());

            assertThat(html).contains("<main><h1>Welcome</h1></main>");
        }

        @Test
        @DisplayName("null PageMeta uses sensible defaults")
        void nullPageMetaUsesDefaults() {
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, null);

            assertThat(html).startsWith("<!DOCTYPE html>");
            assertThat(html).contains("<meta charset=\"UTF-8\"");
            assertThat(html).contains("<title></title>");
        }

        @Test
        @DisplayName("OpenGraph meta property renders in head")
        void ogMetaPropertyRendersInHead() {
            PageMeta meta = PageMeta.create().ogTitle("OG Title");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<meta property=\"og:title\" content=\"OG Title\"");
        }

        @Test
        @DisplayName("inline CSS renders as style tag in head")
        void inlineCssRendersAsStyleTag() {
            PageMeta meta = PageMeta.create().inlineCss("body { margin: 0; }");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<style>body { margin: 0; }</style>");
        }

        @Test
        @DisplayName("JSON-LD renders as script type application/ld+json")
        void jsonLdRendersAsScript() {
            PageMeta meta = PageMeta.create().jsonLd("{\"@type\":\"Article\"}");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<script type=\"application/ld+json\">{\"@type\":\"Article\"}</script>");
        }

        @Test
        @DisplayName("preconnect hint renders in head")
        void preconnectHintRendersInHead() {
            PageMeta meta = PageMeta.create().preconnect("https://cdn.example.com");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<link rel=\"preconnect\" href=\"https://cdn.example.com\"");
        }

        @Test
        @DisplayName("alternate hreflang link renders in head")
        void alternateHreflangRendersInHead() {
            PageMeta meta = PageMeta.create().alternate("es", "/es/about");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("<link rel=\"alternate\" hreflang=\"es\" href=\"/es/about\"");
        }

        @Test
        @DisplayName("htmlAttrs render on the html tag")
        void htmlAttrsRenderOnHtmlTag() {
            PageMeta meta = PageMeta.create().htmlAttr("data-theme", "dark");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("data-theme=\"dark\"");
            // Verify it appears on the <html> tag, not elsewhere
            int htmlTagStart = html.indexOf("<html");
            int htmlTagEnd = html.indexOf(">", htmlTagStart);
            String htmlTag = html.substring(htmlTagStart, htmlTagEnd);
            assertThat(htmlTag).contains("data-theme=\"dark\"");
        }

        @Test
        @DisplayName("multiple htmlAttrs render on the html tag")
        void multipleHtmlAttrsRenderOnHtmlTag() {
            PageMeta meta = PageMeta.create()
                    .htmlAttr("data-theme", "dark")
                    .htmlAttr("data-version", "2");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("data-theme=\"dark\"");
            assertThat(html).contains("data-version=\"2\"");
        }

        @Test
        @DisplayName("htmlAttr values are HTML-escaped")
        void htmlAttrValuesAreEscaped() {
            PageMeta meta = PageMeta.create().htmlAttr("data-info", "a\"b<c>");
            Component comp = new TestComponent(div().text("Body"));
            String html = renderer.render(comp, meta);

            assertThat(html).contains("data-info=\"a&quot;b&lt;c&gt;\"");
            assertThat(html).doesNotContain("data-info=\"a\"b<c>\"");
        }
    }
}
