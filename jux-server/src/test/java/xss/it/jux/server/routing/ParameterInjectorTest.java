package xss.it.jux.server.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.*;
import xss.it.jux.core.JuxRequestContext;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ParameterInjector} -- the reflection-based injector that
 * populates annotated component fields from HTTP request data.
 */
class ParameterInjectorTest {

    private ParameterInjector injector;

    @BeforeEach
    void setUp() {
        injector = new ParameterInjector();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Mock JuxRequestContext
    // ══════════════════════════════════════════════════════════════════

    /**
     * Simple mock implementation of JuxRequestContext for testing.
     */
    static class MockRequestContext implements JuxRequestContext {
        private final Map<String, String> queryParams;
        private final Map<String, String> headers;
        private final Map<String, String> cookies;
        private final Map<String, Object> sessionAttrs;

        MockRequestContext(Map<String, String> queryParams,
                           Map<String, String> headers,
                           Map<String, String> cookies,
                           Map<String, Object> sessionAttrs) {
            this.queryParams = queryParams != null ? queryParams : Map.of();
            this.headers = headers != null ? headers : Map.of();
            this.cookies = cookies != null ? cookies : Map.of();
            this.sessionAttrs = sessionAttrs != null ? sessionAttrs : Map.of();
        }

        static MockRequestContext empty() {
            return new MockRequestContext(Map.of(), Map.of(), Map.of(), Map.of());
        }

        static MockRequestContext withQueryParams(Map<String, String> params) {
            return new MockRequestContext(params, Map.of(), Map.of(), Map.of());
        }

        static MockRequestContext withHeaders(Map<String, String> headers) {
            return new MockRequestContext(Map.of(), headers, Map.of(), Map.of());
        }

        static MockRequestContext withCookies(Map<String, String> cookies) {
            return new MockRequestContext(Map.of(), Map.of(), cookies, Map.of());
        }

        static MockRequestContext withSession(Map<String, Object> session) {
            return new MockRequestContext(Map.of(), Map.of(), Map.of(), session);
        }

        @Override public String method() { return "GET"; }
        @Override public String requestPath() { return "/"; }
        @Override public String requestUrl() { return "http://localhost/"; }
        @Override public Optional<String> header(String name) { return Optional.ofNullable(headers.get(name)); }
        @Override public Optional<String> cookie(String name) { return Optional.ofNullable(cookies.get(name)); }
        @Override public Optional<Object> session(String key) { return Optional.ofNullable(sessionAttrs.get(key)); }
        @Override public void session(String key, Object value) {}
        @Override public String formParam(String name) { return null; }
        @Override public Map<String, String[]> formParams() { return Map.of(); }
        @Override public Optional<String> queryParam(String name) { return Optional.ofNullable(queryParams.get(name)); }
        @Override public String remoteAddress() { return "127.0.0.1"; }
        @Override public Locale locale() { return Locale.ENGLISH; }
        @Override public void responseHeader(String name, String value) {}
        @Override public void status(int code) {}
        @Override public void redirect(String url, int status) {}
    }

    // ══════════════════════════════════════════════════════════════════
    //  Test component classes with annotated fields
    // ══════════════════════════════════════════════════════════════════

    static class PathParamStringComponent {
        @PathParam
        private String slug;
    }

    static class PathParamLongComponent {
        @PathParam
        private long id;
    }

    static class PathParamNamedComponent {
        @PathParam("item-slug")
        private String slug;
    }

    static class PathParamRequiredComponent {
        @PathParam(required = true)
        private String slug;
    }

    static class PathParamDefaultValueComponent {
        @PathParam(defaultValue = "default-slug")
        private String slug;
    }

    static class QueryParamComponent {
        @QueryParam
        private String q;
    }

    static class QueryParamDefaultComponent {
        @QueryParam(defaultValue = "10")
        private int page;
    }

    static class QueryParamRequiredComponent {
        @QueryParam(required = true)
        private String q;
    }

    static class HeaderParamComponent {
        @HeaderParam("Accept")
        private String accept;
    }

    static class HeaderParamDefaultComponent {
        @HeaderParam(value = "X-Custom", defaultValue = "fallback")
        private String custom;
    }

    static class CookieParamComponent {
        @CookieParam("session")
        private String sessionId;
    }

    static class CookieParamDefaultComponent {
        @CookieParam(value = "theme", defaultValue = "light")
        private String theme;
    }

    static class SessionParamComponent {
        @SessionParam("cart")
        private Object cart;
    }

    static class RequestContextComponent {
        @xss.it.jux.annotation.RequestContext
        private JuxRequestContext ctx;
    }

    static class LocaleParamComponent {
        @LocaleParam
        private Locale locale;
    }

    static class MultiParamComponent {
        @PathParam
        private String slug;

        @QueryParam(defaultValue = "1")
        private int page;

        @HeaderParam("Accept-Language")
        private String acceptLang;
    }

    // ══════════════════════════════════════════════════════════════════
    //  @PathParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@PathParam injection")
    class PathParamTests {

        @Test
        @DisplayName("String field injected from pathVariables map")
        void stringFieldInjectedFromPathVariables() {
            PathParamStringComponent comp = new PathParamStringComponent();
            injector.inject(comp, Map.of("slug", "hello-world"), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.slug).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("long field coerced from string path variable")
        void longFieldCoercedFromString() {
            PathParamLongComponent comp = new PathParamLongComponent();
            injector.inject(comp, Map.of("id", "42"), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.id).isEqualTo(42L);
        }

        @Test
        @DisplayName("named @PathParam uses annotation value as key")
        void namedPathParamUsesAnnotationValue() {
            PathParamNamedComponent comp = new PathParamNamedComponent();
            injector.inject(comp, Map.of("item-slug", "my-item"), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.slug).isEqualTo("my-item");
        }

        @Test
        @DisplayName("required @PathParam missing throws IllegalArgumentException")
        void requiredPathParamMissingThrows() {
            PathParamRequiredComponent comp = new PathParamRequiredComponent();
            assertThatThrownBy(
                () -> injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH)
            ).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("@PathParam with defaultValue uses default when missing")
        void pathParamDefaultValueUsedWhenMissing() {
            PathParamDefaultValueComponent comp = new PathParamDefaultValueComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.slug).isEqualTo("default-slug");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @QueryParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@QueryParam injection")
    class QueryParamTests {

        @Test
        @DisplayName("String field injected from query param")
        void stringFieldInjectedFromQueryParam() {
            QueryParamComponent comp = new QueryParamComponent();
            MockRequestContext ctx = MockRequestContext.withQueryParams(Map.of("q", "search-term"));
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.q).isEqualTo("search-term");
        }

        @Test
        @DisplayName("@QueryParam with defaultValue uses default when missing")
        void queryParamDefaultValueUsedWhenMissing() {
            QueryParamDefaultComponent comp = new QueryParamDefaultComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.page).isEqualTo(10);
        }

        @Test
        @DisplayName("@QueryParam value overrides default when present")
        void queryParamValueOverridesDefault() {
            QueryParamDefaultComponent comp = new QueryParamDefaultComponent();
            MockRequestContext ctx = MockRequestContext.withQueryParams(Map.of("page", "5"));
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.page).isEqualTo(5);
        }

        @Test
        @DisplayName("required @QueryParam missing throws exception")
        void requiredQueryParamMissingThrows() {
            QueryParamRequiredComponent comp = new QueryParamRequiredComponent();
            assertThatThrownBy(
                () -> injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH)
            ).isInstanceOf(RuntimeException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @HeaderParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@HeaderParam injection")
    class HeaderParamTests {

        @Test
        @DisplayName("header value injected into field")
        void headerValueInjected() {
            HeaderParamComponent comp = new HeaderParamComponent();
            MockRequestContext ctx = MockRequestContext.withHeaders(Map.of("Accept", "text/html"));
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.accept).isEqualTo("text/html");
        }

        @Test
        @DisplayName("@HeaderParam with defaultValue uses default when header missing")
        void headerParamDefaultValueUsedWhenMissing() {
            HeaderParamDefaultComponent comp = new HeaderParamDefaultComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.custom).isEqualTo("fallback");
        }

        @Test
        @DisplayName("missing header without default leaves field null")
        void missingHeaderWithoutDefaultLeavesNull() {
            HeaderParamComponent comp = new HeaderParamComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.accept).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @CookieParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@CookieParam injection")
    class CookieParamTests {

        @Test
        @DisplayName("cookie value injected into field")
        void cookieValueInjected() {
            CookieParamComponent comp = new CookieParamComponent();
            MockRequestContext ctx = MockRequestContext.withCookies(Map.of("session", "abc123"));
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.sessionId).isEqualTo("abc123");
        }

        @Test
        @DisplayName("@CookieParam with defaultValue uses default when cookie missing")
        void cookieParamDefaultValueUsedWhenMissing() {
            CookieParamDefaultComponent comp = new CookieParamDefaultComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.theme).isEqualTo("light");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @SessionParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@SessionParam injection")
    class SessionParamTests {

        @Test
        @DisplayName("session attribute injected into field")
        void sessionAttributeInjected() {
            SessionParamComponent comp = new SessionParamComponent();
            Object cartData = new Object();
            MockRequestContext ctx = MockRequestContext.withSession(Map.of("cart", cartData));
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.cart).isSameAs(cartData);
        }

        @Test
        @DisplayName("missing session attribute leaves field null")
        void missingSessionAttributeLeavesNull() {
            SessionParamComponent comp = new SessionParamComponent();
            injector.inject(comp, Map.of(), MockRequestContext.empty(), Locale.ENGLISH);
            assertThat(comp.cart).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @RequestContext tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@RequestContext injection")
    class RequestContextTests {

        @Test
        @DisplayName("JuxRequestContext instance injected into field")
        void requestContextInjected() {
            RequestContextComponent comp = new RequestContextComponent();
            MockRequestContext ctx = MockRequestContext.empty();
            injector.inject(comp, Map.of(), ctx, Locale.ENGLISH);
            assertThat(comp.ctx).isSameAs(ctx);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  @LocaleParam tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("@LocaleParam injection")
    class LocaleParamTests {

        @Test
        @DisplayName("locale injected into field")
        void localeInjected() {
            LocaleParamComponent comp = new LocaleParamComponent();
            Locale locale = Locale.FRENCH;
            injector.inject(comp, Map.of(), MockRequestContext.empty(), locale);
            assertThat(comp.locale).isEqualTo(Locale.FRENCH);
        }

        @Test
        @DisplayName("different locale values are respected")
        void differentLocalesRespected() {
            LocaleParamComponent comp = new LocaleParamComponent();
            Locale locale = Locale.forLanguageTag("ar");
            injector.inject(comp, Map.of(), MockRequestContext.empty(), locale);
            assertThat(comp.locale.getLanguage()).isEqualTo("ar");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Multiple annotations on one component
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Multiple parameter types on one component")
    class MultiParamTests {

        @Test
        @DisplayName("all parameter types injected simultaneously")
        void allParameterTypesInjected() {
            MultiParamComponent comp = new MultiParamComponent();
            MockRequestContext ctx = new MockRequestContext(
                Map.of("page", "3"),
                Map.of("Accept-Language", "en-US"),
                Map.of(),
                Map.of()
            );
            injector.inject(comp, Map.of("slug", "my-post"), ctx, Locale.ENGLISH);

            assertThat(comp.slug).isEqualTo("my-post");
            assertThat(comp.page).isEqualTo(3);
            assertThat(comp.acceptLang).isEqualTo("en-US");
        }

        @Test
        @DisplayName("missing optional params use defaults")
        void missingOptionalParamsUseDefaults() {
            MultiParamComponent comp = new MultiParamComponent();
            injector.inject(comp, Map.of("slug", "post"), MockRequestContext.empty(), Locale.ENGLISH);

            assertThat(comp.slug).isEqualTo("post");
            assertThat(comp.page).isEqualTo(1); // default value "1" coerced to int
            assertThat(comp.acceptLang).isNull(); // no header, no default
        }
    }
}
