package xss.it.jux.server.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.*;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.PageMeta;
import xss.it.jux.server.WebApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static xss.it.jux.core.Elements.div;

/**
 * Tests for {@link MetadataResolver} -- verifies that annotation-declared
 * metadata is correctly scanned, merged with programmatic PageMeta, and
 * that the WebApplication default metadata is applied in the right priority.
 */
class MetadataResolverTest {

    private MetadataResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MetadataResolver();
    }

    // ── Test Components (inner classes with annotations) ────────────

    @Title("Hello")
    static class TitledComponent extends Component {
        @Override
        public Element render() {
            return div().text("titled");
        }
    }

    @Meta(name = "description", content = "test description")
    static class MetaDescComponent extends Component {
        @Override
        public Element render() {
            return div().text("meta");
        }
    }

    @Css("main.css")
    static class CssComponent extends Component {
        @Override
        public Element render() {
            return div().text("css");
        }
    }

    @Js("app.js")
    static class JsComponent extends Component {
        @Override
        public Element render() {
            return div().text("js");
        }
    }

    @Favicon("/icon.ico")
    static class FaviconComponent extends Component {
        @Override
        public Element render() {
            return div().text("favicon");
        }
    }

    @Canonical("/about")
    static class CanonicalComponent extends Component {
        @Override
        public Element render() {
            return div().text("canonical");
        }
    }

    @Title("Base Title")
    @Meta(name = "description", content = "base desc")
    @Css("base.css")
    @Js("base.js")
    @Favicon("/base-icon.ico")
    @Canonical("/base")
    static class FullAnnotatedComponent extends Component {
        @Override
        public Element render() {
            return div().text("full");
        }
    }

    static class PlainComponent extends Component {
        @Override
        public Element render() {
            return div().text("plain");
        }
    }

    @Meta(property = "og:type", content = "website")
    static class OgComponent extends Component {
        @Override
        public Element render() {
            return div().text("og");
        }
    }

    @Css(value = "theme.css", order = 1)
    @Css(value = "page.css", order = 10)
    static class MultipleCssComponent extends Component {
        @Override
        public Element render() {
            return div().text("multi-css");
        }
    }

    @Title("Parent Title")
    static class ParentComponent extends Component {
        @Override
        public Element render() {
            return div().text("parent");
        }
    }

    @Title("Child Title")
    static class ChildComponent extends ParentComponent {
        @Override
        public Element render() {
            return div().text("child");
        }
    }

    // ── Test WebApplication ──

    @Css("global.css")
    @Js("global.js")
    static class TestWebApp implements WebApplication {
        @Override
        public PageMeta defaultPageMeta() {
            return PageMeta.create()
                .titleTemplate("%s | Test Site")
                .ogSiteName("Test Site");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Annotation scanning tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Annotation scanning")
    class AnnotationScanningTests {

        @Test
        @DisplayName("@Title annotation produces title in resolved meta")
        void titleAnnotationResolved() {
            PageMeta resolved = resolver.resolve(TitledComponent.class, null);
            assertThat(resolved.getTitle()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("@Meta name annotation produces meta name in resolved meta")
        void metaNameAnnotationResolved() {
            PageMeta resolved = resolver.resolve(MetaDescComponent.class, null);
            assertThat(resolved.getMetaNames()).containsEntry("description", "test description");
        }

        @Test
        @DisplayName("@Meta property annotation produces meta property in resolved meta")
        void metaPropertyAnnotationResolved() {
            PageMeta resolved = resolver.resolve(OgComponent.class, null);
            assertThat(resolved.getMetaProperties()).containsEntry("og:type", "website");
        }

        @Test
        @DisplayName("@Css annotation produces CSS resource in resolved meta")
        void cssAnnotationResolved() {
            PageMeta resolved = resolver.resolve(CssComponent.class, null);
            assertThat(resolved.getCssResources()).hasSize(1);
            assertThat(resolved.getCssResources().get(0).path()).isEqualTo("main.css");
        }

        @Test
        @DisplayName("@Js annotation produces JS resource in resolved meta")
        void jsAnnotationResolved() {
            PageMeta resolved = resolver.resolve(JsComponent.class, null);
            assertThat(resolved.getJsResources()).hasSize(1);
            assertThat(resolved.getJsResources().get(0).path()).isEqualTo("app.js");
        }

        @Test
        @DisplayName("@Favicon annotation produces favicon in resolved meta")
        void faviconAnnotationResolved() {
            PageMeta resolved = resolver.resolve(FaviconComponent.class, null);
            assertThat(resolved.getFaviconHref()).isEqualTo("/icon.ico");
        }

        @Test
        @DisplayName("@Canonical annotation produces canonical in resolved meta")
        void canonicalAnnotationResolved() {
            PageMeta resolved = resolver.resolve(CanonicalComponent.class, null);
            assertThat(resolved.getCanonical()).isEqualTo("/about");
        }

        @Test
        @DisplayName("multiple @Css annotations produce multiple CSS resources")
        void multipleCssAnnotationsResolved() {
            PageMeta resolved = resolver.resolve(MultipleCssComponent.class, null);
            assertThat(resolved.getCssResources()).hasSize(2);
            assertThat(resolved.getCssResources())
                .extracting("path")
                .containsExactlyInAnyOrder("theme.css", "page.css");
        }

        @Test
        @DisplayName("child class annotation overrides parent annotation")
        void childAnnotationOverridesParent() {
            PageMeta resolved = resolver.resolve(ChildComponent.class, null);
            assertThat(resolved.getTitle()).isEqualTo("Child Title");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Programmatic override tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Programmatic metadata overrides")
    class ProgrammaticOverrideTests {

        @Test
        @DisplayName("programmatic title overrides annotation title")
        void programmaticTitleOverridesAnnotation() {
            PageMeta programmatic = PageMeta.create().title("Override Title");
            PageMeta resolved = resolver.resolve(TitledComponent.class, programmatic);
            assertThat(resolved.getTitle()).isEqualTo("Override Title");
        }

        @Test
        @DisplayName("programmatic CSS is added on top of annotation CSS")
        void programmaticCssAddsToAnnotation() {
            PageMeta programmatic = PageMeta.create().css("extra.css");
            PageMeta resolved = resolver.resolve(CssComponent.class, programmatic);
            assertThat(resolved.getCssResources())
                .extracting("path")
                .contains("main.css", "extra.css");
        }

        @Test
        @DisplayName("programmatic canonical overrides annotation canonical")
        void programmaticCanonicalOverridesAnnotation() {
            PageMeta programmatic = PageMeta.create().canonical("/new-canonical");
            PageMeta resolved = resolver.resolve(CanonicalComponent.class, programmatic);
            assertThat(resolved.getCanonical()).isEqualTo("/new-canonical");
        }

        @Test
        @DisplayName("programmatic description overrides annotation description")
        void programmaticDescriptionOverrides() {
            PageMeta programmatic = PageMeta.create().description("new desc");
            PageMeta resolved = resolver.resolve(MetaDescComponent.class, programmatic);
            assertThat(resolved.getMetaNames()).containsEntry("description", "new desc");
        }

        @Test
        @DisplayName("programmatic favicon overrides annotation favicon")
        void programmaticFaviconOverrides() {
            PageMeta programmatic = PageMeta.create().favicon("/new-icon.png");
            PageMeta resolved = resolver.resolve(FaviconComponent.class, programmatic);
            assertThat(resolved.getFaviconHref()).isEqualTo("/new-icon.png");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  No annotations tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("No annotations")
    class NoAnnotationsTests {

        @Test
        @DisplayName("class without annotations returns meta from programmatic")
        void classWithoutAnnotationsReturnsProgrammatic() {
            PageMeta programmatic = PageMeta.create().title("Prog Title");
            PageMeta resolved = resolver.resolve(PlainComponent.class, programmatic);
            assertThat(resolved.getTitle()).isEqualTo("Prog Title");
        }

        @Test
        @DisplayName("class without annotations and no programmatic returns empty meta")
        void classWithoutAnnotationsReturnsEmptyMeta() {
            PageMeta resolved = resolver.resolve(PlainComponent.class, null);
            assertThat(resolved.getTitle()).isNull();
            assertThat(resolved.getCssResources()).isEmpty();
            assertThat(resolved.getJsResources()).isEmpty();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  WebApplication integration tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("WebApplication defaults")
    class WebApplicationDefaultsTests {

        @BeforeEach
        void setUpWebApp() {
            TestWebApp webApp = new TestWebApp();
            resolver.setApplicationClass(TestWebApp.class);
            resolver.setWebApplication(webApp);
        }

        @Test
        @DisplayName("WebApplication defaultPageMeta is applied as baseline")
        void webAppDefaultsApplied() {
            PageMeta resolved = resolver.resolve(PlainComponent.class, null);
            assertThat(resolved.getTitleTemplate()).isEqualTo("%s | Test Site");
            assertThat(resolved.getMetaProperties()).containsEntry("og:site_name", "Test Site");
        }

        @Test
        @DisplayName("WebApplication @Css/@Js annotations add global resources")
        void webAppCssJsAnnotationsAdded() {
            PageMeta resolved = resolver.resolve(PlainComponent.class, null);
            assertThat(resolved.getCssResources())
                .extracting("path")
                .contains("global.css");
            assertThat(resolved.getJsResources())
                .extracting("path")
                .contains("global.js");
        }

        @Test
        @DisplayName("page programmatic overrides WebApplication defaults")
        void pageProgrammaticOverridesWebAppDefaults() {
            PageMeta programmatic = PageMeta.create().titleTemplate("%s - Override");
            PageMeta resolved = resolver.resolve(PlainComponent.class, programmatic);
            assertThat(resolved.getTitleTemplate()).isEqualTo("%s - Override");
        }

        @Test
        @DisplayName("page annotations merge with WebApplication annotations")
        void pageAnnotationsMergeWithWebApp() {
            PageMeta resolved = resolver.resolve(CssComponent.class, null);
            // Should have both global.css and main.css
            assertThat(resolved.getCssResources())
                .extracting("path")
                .contains("global.css", "main.css");
        }
    }
}
