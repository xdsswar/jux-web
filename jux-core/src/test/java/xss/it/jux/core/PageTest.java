package xss.it.jux.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for the {@link Page} base class.
 *
 * <p>Uses a concrete test subclass and simple mock implementations of
 * {@link JuxRequestContext} and {@link JuxMessages} to verify the
 * protected accessor methods and default behavior.</p>
 */
class PageTest {

    // ── Test Page Subclass ────────────────────────────────────────────

    /**
     * Concrete Page for testing. Exposes all protected methods as public.
     */
    static class TestPage extends Page {

        @Override
        public Element render() {
            return main_().children(h1().text("Test Page"));
        }

        // Expose protected methods for testing
        public JuxRequestContext publicContext()                          { return context(); }
        public String publicPathParam(String name)                       { return pathParam(name); }
        public String publicPathParam(String name, String def)           { return pathParam(name, def); }
        public String publicQueryParam(String name)                      { return queryParam(name); }
        public String publicQueryParam(String name, String def)          { return queryParam(name, def); }
        public String publicHeader(String name)                          { return header(name); }
        public String publicCookie(String name)                          { return cookie(name); }
        public <T> T publicSession(String name)                          { return session(name); }
        public JuxMessages publicMessages()                              { return messages(); }
        public Locale publicLocale()                                     { return locale(); }
        public String publicRequestPath()                                { return requestPath(); }
        public boolean publicIsPost()                                    { return isPost(); }
        public boolean publicIsGet()                                     { return isGet(); }
        public String publicMethod()                                     { return method(); }
        public String publicRemoteAddress()                              { return remoteAddress(); }
        public String publicFormParam(String name)                       { return formParam(name); }
    }

    // ── Mock JuxRequestContext ────────────────────────────────────────

    static class MockRequestContext implements JuxRequestContext {

        private final String httpMethod;
        private final String path;
        private final Map<String, String> headers;
        private final Map<String, String> cookies;
        private final Map<String, String> queryParams;
        private final Map<String, String> formParams;
        private final Map<String, Object> sessionAttrs;
        private final Locale locale;
        private final String remoteAddr;

        MockRequestContext(String httpMethod, String path, Locale locale) {
            this.httpMethod = httpMethod;
            this.path = path;
            this.headers = Map.of("Accept-Language", "en-US", "X-Forwarded-For", "10.0.0.1");
            this.cookies = Map.of("theme", "dark", "session-id", "abc123");
            this.queryParams = Map.of("q", "search-term", "page", "2");
            this.formParams = Map.of("name", "John", "email", "john@example.com");
            this.sessionAttrs = Map.of("user", "admin");
            this.locale = locale;
            this.remoteAddr = "192.168.1.100";
        }

        @Override public String method()                         { return httpMethod; }
        @Override public String requestPath()                    { return path; }
        @Override public String requestUrl()                     { return "https://example.com" + path; }
        @Override public Optional<String> header(String name)    { return Optional.ofNullable(headers.get(name)); }
        @Override public Optional<String> cookie(String name)    { return Optional.ofNullable(cookies.get(name)); }
        @Override public Optional<Object> session(String key)    { return Optional.ofNullable(sessionAttrs.get(key)); }
        @Override public void session(String key, Object value)  { /* no-op for test */ }
        @Override public String formParam(String name)           { return formParams.get(name); }
        @Override public Map<String, String[]> formParams()      { return Map.of(); }
        @Override public Optional<String> queryParam(String name){ return Optional.ofNullable(queryParams.get(name)); }
        @Override public String remoteAddress()                  { return remoteAddr; }
        @Override public Locale locale()                         { return locale; }
        @Override public void responseHeader(String n, String v) { /* no-op */ }
        @Override public void status(int code)                   { /* no-op */ }
        @Override public void redirect(String url, int status)   { /* no-op */ }
    }

    // ── Mock JuxMessages ─────────────────────────────────────────────

    static class MockMessages implements JuxMessages {

        private final Locale locale;

        MockMessages(Locale locale) {
            this.locale = locale;
        }

        @Override public String getString(String key)                     { return key; }
        @Override public String getString(String key, Object... args)     { return key; }
        @Override public Locale currentLocale()                           { return locale; }
        @Override public List<Locale> availableLocales()                  { return List.of(Locale.ENGLISH, Locale.of("es")); }
        @Override public boolean isRtl()                                  { return false; }
    }

    // ── Test Setup ────────────────────────────────────────────────────

    private TestPage page;

    @BeforeEach
    void setUp() {
        page = new TestPage();
    }

    // ── Default Behavior (no initRequest called) ──────────────────────

    @Nested
    @DisplayName("Default behavior without initRequest()")
    class Defaults {

        @Test
        @DisplayName("pageMeta() returns null by default")
        void pageMetaDefaultsToNull() {
            assertThat(page.pageMeta()).isNull();
        }

        @Test
        @DisplayName("render() returns the expected element tree")
        void renderProducesTree() {
            Element el = page.render();
            assertThat(el.getTag()).isEqualTo("main");
            assertThat(el.getChildren()).hasSize(1);
            assertThat(el.getChildren().getFirst().getTextContent()).isEqualTo("Test Page");
        }

        @Test
        @DisplayName("context() is null before initRequest()")
        void contextIsNull() {
            assertThat(page.publicContext()).isNull();
        }

        @Test
        @DisplayName("pathParam() returns null before initRequest()")
        void pathParamIsNull() {
            assertThat(page.publicPathParam("slug")).isNull();
        }

        @Test
        @DisplayName("queryParam() returns null before initRequest()")
        void queryParamIsNull() {
            assertThat(page.publicQueryParam("q")).isNull();
        }

        @Test
        @DisplayName("header() returns null before initRequest()")
        void headerIsNull() {
            assertThat(page.publicHeader("Accept")).isNull();
        }

        @Test
        @DisplayName("cookie() returns null before initRequest()")
        void cookieIsNull() {
            assertThat(page.publicCookie("theme")).isNull();
        }

        @Test
        @DisplayName("messages() is null before initRequest()")
        void messagesIsNull() {
            assertThat(page.publicMessages()).isNull();
        }

        @Test
        @DisplayName("locale() returns JVM default before initRequest()")
        void localeDefaultsToJvmDefault() {
            assertThat(page.publicLocale()).isEqualTo(Locale.getDefault());
        }

        @Test
        @DisplayName("requestPath() returns '/' before initRequest()")
        void requestPathDefault() {
            assertThat(page.publicRequestPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("isPost() returns false before initRequest()")
        void isPostDefault() {
            assertThat(page.publicIsPost()).isFalse();
        }

        @Test
        @DisplayName("isGet() returns false before initRequest()")
        void isGetDefault() {
            assertThat(page.publicIsGet()).isFalse();
        }

        @Test
        @DisplayName("method() returns 'GET' before initRequest()")
        void methodDefault() {
            assertThat(page.publicMethod()).isEqualTo("GET");
        }

        @Test
        @DisplayName("remoteAddress() returns null before initRequest()")
        void remoteAddressDefault() {
            assertThat(page.publicRemoteAddress()).isNull();
        }

        @Test
        @DisplayName("formParam() returns null before initRequest()")
        void formParamDefault() {
            assertThat(page.publicFormParam("name")).isNull();
        }
    }

    // ── Behavior After initRequest() ──────────────────────────────────

    @Nested
    @DisplayName("After initRequest()")
    class AfterInit {

        private MockRequestContext ctx;
        private MockMessages messages;

        @BeforeEach
        void initPage() {
            ctx = new MockRequestContext("POST", "/blog/hello", Locale.of("es"));
            messages = new MockMessages(Locale.of("es"));
            page.initRequest(ctx, Map.of("slug", "hello"), messages);
        }

        @Test
        @DisplayName("context() returns the injected context")
        void contextReturnsInjected() {
            assertThat(page.publicContext()).isSameAs(ctx);
        }

        @Test
        @DisplayName("pathParam() returns the value from the map")
        void pathParamReturnsValue() {
            assertThat(page.publicPathParam("slug")).isEqualTo("hello");
        }

        @Test
        @DisplayName("pathParam() with default returns value when present")
        void pathParamWithDefaultPresent() {
            assertThat(page.publicPathParam("slug", "fallback")).isEqualTo("hello");
        }

        @Test
        @DisplayName("pathParam() with default returns fallback when missing")
        void pathParamWithDefaultMissing() {
            assertThat(page.publicPathParam("missing", "fallback")).isEqualTo("fallback");
        }

        @Test
        @DisplayName("queryParam() returns the value from the context")
        void queryParamReturnsValue() {
            assertThat(page.publicQueryParam("q")).isEqualTo("search-term");
        }

        @Test
        @DisplayName("queryParam() with default returns fallback when missing")
        void queryParamDefaultFallback() {
            assertThat(page.publicQueryParam("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("header() returns the header value")
        void headerReturnsValue() {
            assertThat(page.publicHeader("Accept-Language")).isEqualTo("en-US");
        }

        @Test
        @DisplayName("header() returns null for missing header")
        void headerMissingReturnsNull() {
            assertThat(page.publicHeader("X-Missing")).isNull();
        }

        @Test
        @DisplayName("cookie() returns the cookie value")
        void cookieReturnsValue() {
            assertThat(page.publicCookie("theme")).isEqualTo("dark");
        }

        @Test
        @DisplayName("cookie() returns null for missing cookie")
        void cookieMissingReturnsNull() {
            assertThat(page.publicCookie("missing-cookie")).isNull();
        }

        @Test
        @DisplayName("session() returns the session attribute")
        void sessionReturnsValue() {
            String user = page.publicSession("user");
            assertThat(user).isEqualTo("admin");
        }

        @Test
        @DisplayName("messages() returns the injected messages")
        void messagesReturnsInjected() {
            assertThat(page.publicMessages()).isSameAs(messages);
        }

        @Test
        @DisplayName("locale() returns the context locale")
        void localeFromContext() {
            assertThat(page.publicLocale()).isEqualTo(Locale.of("es"));
        }

        @Test
        @DisplayName("requestPath() returns the context path")
        void requestPathFromContext() {
            assertThat(page.publicRequestPath()).isEqualTo("/blog/hello");
        }

        @Test
        @DisplayName("isPost() returns true for POST method")
        void isPostTrue() {
            assertThat(page.publicIsPost()).isTrue();
        }

        @Test
        @DisplayName("isGet() returns false for POST method")
        void isGetFalse() {
            assertThat(page.publicIsGet()).isFalse();
        }

        @Test
        @DisplayName("method() returns the HTTP method")
        void methodReturnsValue() {
            assertThat(page.publicMethod()).isEqualTo("POST");
        }

        @Test
        @DisplayName("remoteAddress() returns the client IP")
        void remoteAddressReturnsValue() {
            assertThat(page.publicRemoteAddress()).isEqualTo("192.168.1.100");
        }

        @Test
        @DisplayName("formParam() returns the form parameter value")
        void formParamReturnsValue() {
            assertThat(page.publicFormParam("name")).isEqualTo("John");
        }

        @Test
        @DisplayName("formParam() returns null for missing parameter")
        void formParamMissingReturnsNull() {
            assertThat(page.publicFormParam("missing")).isNull();
        }
    }

    // ── initRequest with null pathParams ──────────────────────────────

    @Test
    @DisplayName("initRequest with null pathParams defaults to empty map")
    void initWithNullPathParams() {
        page.initRequest(
                new MockRequestContext("GET", "/", Locale.ENGLISH),
                null,
                new MockMessages(Locale.ENGLISH)
        );
        assertThat(page.publicPathParam("anything")).isNull();
    }

    // ── Custom Page with overridden pageMeta() ────────────────────────

    @Test
    @DisplayName("pageMeta() can be overridden to return custom metadata")
    void pageMetaCanBeOverridden() {
        Page customPage = new Page() {
            @Override
            public PageMeta pageMeta() {
                return PageMeta.create().title("Custom").status(404);
            }

            @Override
            public Element render() {
                return div().text("not found");
            }
        };

        PageMeta meta = customPage.pageMeta();
        assertThat(meta).isNotNull();
        assertThat(meta.getTitle()).isEqualTo("Custom");
        assertThat(meta.getStatus()).isEqualTo(404);
    }

    // ── GET request ───────────────────────────────────────────────────

    @Test
    @DisplayName("isGet() returns true for GET requests")
    void isGetReturnsTrueForGet() {
        page.initRequest(
                new MockRequestContext("GET", "/", Locale.ENGLISH),
                Map.of(),
                new MockMessages(Locale.ENGLISH)
        );
        assertThat(page.publicIsGet()).isTrue();
        assertThat(page.publicIsPost()).isFalse();
    }
}
