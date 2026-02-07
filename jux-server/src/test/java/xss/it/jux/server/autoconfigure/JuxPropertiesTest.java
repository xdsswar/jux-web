package xss.it.jux.server.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JuxProperties} -- verifies that all configuration properties
 * have the correct default values and that setter/getter round-trips work.
 */
class JuxPropertiesTest {

    private JuxProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JuxProperties();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Top-level defaults
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("basePackage defaults to empty string")
    void basePackageDefault() {
        assertThat(properties.getBasePackage()).isEqualTo("");
    }

    @Test
    @DisplayName("basePackage setter/getter round-trip")
    void basePackageSetterGetter() {
        properties.setBasePackage("com.example.pages");
        assertThat(properties.getBasePackage()).isEqualTo("com.example.pages");
    }

    // ══════════════════════════════════════════════════════════════════
    //  SSR defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SSR configuration defaults")
    class SsrDefaults {

        @Test
        @DisplayName("ssr.cache.enabled defaults to true")
        void cacheEnabledDefault() {
            assertThat(properties.getSsr().getCache().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("ssr.cache.maxSize defaults to 1000")
        void cacheMaxSizeDefault() {
            assertThat(properties.getSsr().getCache().getMaxSize()).isEqualTo(1000);
        }

        @Test
        @DisplayName("ssr.cache.ttl defaults to '300s'")
        void cacheTtlDefault() {
            assertThat(properties.getSsr().getCache().getTtl()).isEqualTo("300s");
        }

        @Test
        @DisplayName("ssr.cache.enabled setter/getter round-trip")
        void cacheEnabledSetterGetter() {
            properties.getSsr().getCache().setEnabled(false);
            assertThat(properties.getSsr().getCache().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("ssr.cache.maxSize setter/getter round-trip")
        void cacheMaxSizeSetterGetter() {
            properties.getSsr().getCache().setMaxSize(5000);
            assertThat(properties.getSsr().getCache().getMaxSize()).isEqualTo(5000);
        }

        @Test
        @DisplayName("ssr.cache.ttl setter/getter round-trip")
        void cacheTtlSetterGetter() {
            properties.getSsr().getCache().setTtl("600s");
            assertThat(properties.getSsr().getCache().getTtl()).isEqualTo("600s");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Client defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Client configuration defaults")
    class ClientDefaults {

        @Test
        @DisplayName("client.outputPath defaults to '/static/js/'")
        void outputPathDefault() {
            assertThat(properties.getClient().getOutputPath()).isEqualTo("/static/js/");
        }

        @Test
        @DisplayName("client.sourceMaps defaults to false")
        void sourceMapsDefault() {
            assertThat(properties.getClient().isSourceMaps()).isFalse();
        }

        @Test
        @DisplayName("client.outputPath setter/getter round-trip")
        void outputPathSetterGetter() {
            properties.getClient().setOutputPath("/js/");
            assertThat(properties.getClient().getOutputPath()).isEqualTo("/js/");
        }

        @Test
        @DisplayName("client.sourceMaps setter/getter round-trip")
        void sourceMapsSetterGetter() {
            properties.getClient().setSourceMaps(true);
            assertThat(properties.getClient().isSourceMaps()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Resources defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Resources configuration defaults")
    class ResourcesDefaults {

        @Test
        @DisplayName("resources.versioning defaults to true")
        void versioningDefault() {
            assertThat(properties.getResources().isVersioning()).isTrue();
        }

        @Test
        @DisplayName("resources.minify defaults to true")
        void minifyDefault() {
            assertThat(properties.getResources().isMinify()).isTrue();
        }

        @Test
        @DisplayName("resources.versioning setter/getter round-trip")
        void versioningSetterGetter() {
            properties.getResources().setVersioning(false);
            assertThat(properties.getResources().isVersioning()).isFalse();
        }

        @Test
        @DisplayName("resources.minify setter/getter round-trip")
        void minifySetterGetter() {
            properties.getResources().setMinify(false);
            assertThat(properties.getResources().isMinify()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Theme defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Theme configuration defaults")
    class ThemeDefaults {

        @Test
        @DisplayName("theme.name defaults to 'default'")
        void themeNameDefault() {
            assertThat(properties.getTheme().getName()).isEqualTo("default");
        }

        @Test
        @DisplayName("theme.name setter/getter round-trip")
        void themeNameSetterGetter() {
            properties.getTheme().setName("dark");
            assertThat(properties.getTheme().getName()).isEqualTo("dark");
        }

        @Test
        @DisplayName("theme.defaultTheme defaults to 'light'")
        void themeDefaultThemeDefault() {
            assertThat(properties.getTheme().getDefaultTheme()).isEqualTo("light");
        }

        @Test
        @DisplayName("theme.availableThemes defaults to ['light', 'dark']")
        void themeAvailableThemesDefault() {
            assertThat(properties.getTheme().getAvailableThemes()).containsExactly("light", "dark");
        }

        @Test
        @DisplayName("theme.cookieName defaults to 'jux-theme'")
        void themeCookieNameDefault() {
            assertThat(properties.getTheme().getCookieName()).isEqualTo("jux-theme");
        }

        @Test
        @DisplayName("theme.cookieMaxAge defaults to 31536000 (1 year)")
        void themeCookieMaxAgeDefault() {
            assertThat(properties.getTheme().getCookieMaxAge()).isEqualTo(31536000);
        }

        @Test
        @DisplayName("theme.defaultTheme setter/getter round-trip")
        void themeDefaultThemeSetterGetter() {
            properties.getTheme().setDefaultTheme("dark");
            assertThat(properties.getTheme().getDefaultTheme()).isEqualTo("dark");
        }

        @Test
        @DisplayName("theme.availableThemes setter/getter round-trip")
        void themeAvailableThemesSetterGetter() {
            properties.getTheme().setAvailableThemes(java.util.List.of("light", "dark", "auto"));
            assertThat(properties.getTheme().getAvailableThemes()).containsExactly("light", "dark", "auto");
        }

        @Test
        @DisplayName("theme.cookieName setter/getter round-trip")
        void themeCookieNameSetterGetter() {
            properties.getTheme().setCookieName("my-theme");
            assertThat(properties.getTheme().getCookieName()).isEqualTo("my-theme");
        }

        @Test
        @DisplayName("theme.cookieMaxAge setter/getter round-trip")
        void themeCookieMaxAgeSetterGetter() {
            properties.getTheme().setCookieMaxAge(86400);
            assertThat(properties.getTheme().getCookieMaxAge()).isEqualTo(86400);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  MetaDefaults defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MetaDefaults configuration defaults")
    class MetaDefaultsDefaults {

        @Test
        @DisplayName("metaDefaults.charset defaults to 'UTF-8'")
        void charsetDefault() {
            assertThat(properties.getMetaDefaults().getCharset()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("metaDefaults.viewport defaults to 'width=device-width, initial-scale=1'")
        void viewportDefault() {
            assertThat(properties.getMetaDefaults().getViewport())
                .isEqualTo("width=device-width, initial-scale=1");
        }

        @Test
        @DisplayName("metaDefaults.charset setter/getter round-trip")
        void charsetSetterGetter() {
            properties.getMetaDefaults().setCharset("ISO-8859-1");
            assertThat(properties.getMetaDefaults().getCharset()).isEqualTo("ISO-8859-1");
        }

        @Test
        @DisplayName("metaDefaults.viewport setter/getter round-trip")
        void viewportSetterGetter() {
            properties.getMetaDefaults().setViewport("width=1024");
            assertThat(properties.getMetaDefaults().getViewport()).isEqualTo("width=1024");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  A11y defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("A11y configuration defaults")
    class A11yDefaults {

        @Test
        @DisplayName("a11y.enabled defaults to true")
        void enabledDefault() {
            assertThat(properties.getA11y().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("a11y.auditOnRender defaults to true")
        void auditOnRenderDefault() {
            assertThat(properties.getA11y().isAuditOnRender()).isTrue();
        }

        @Test
        @DisplayName("a11y.failOnError defaults to false")
        void failOnErrorDefault() {
            assertThat(properties.getA11y().isFailOnError()).isFalse();
        }

        @Test
        @DisplayName("a11y.logViolations defaults to true")
        void logViolationsDefault() {
            assertThat(properties.getA11y().isLogViolations()).isTrue();
        }

        @Test
        @DisplayName("a11y.defaultLang defaults to 'en'")
        void defaultLangDefault() {
            assertThat(properties.getA11y().getDefaultLang()).isEqualTo("en");
        }

        @Test
        @DisplayName("a11y.skipNav defaults to true")
        void skipNavDefault() {
            assertThat(properties.getA11y().isSkipNav()).isTrue();
        }

        @Test
        @DisplayName("a11y.focusVisible defaults to true")
        void focusVisibleDefault() {
            assertThat(properties.getA11y().isFocusVisible()).isTrue();
        }

        @Test
        @DisplayName("a11y.minTargetSize defaults to 24")
        void minTargetSizeDefault() {
            assertThat(properties.getA11y().getMinTargetSize()).isEqualTo(24);
        }

        @Test
        @DisplayName("a11y.contrastCheck defaults to true")
        void contrastCheckDefault() {
            assertThat(properties.getA11y().isContrastCheck()).isTrue();
        }

        @Test
        @DisplayName("a11y.autoFix defaults to true")
        void autoFixDefault() {
            assertThat(properties.getA11y().isAutoFix()).isTrue();
        }

        @Test
        @DisplayName("a11y.reportEndpoint defaults to null")
        void reportEndpointDefault() {
            assertThat(properties.getA11y().getReportEndpoint()).isNull();
        }

        @Test
        @DisplayName("a11y.enabled setter/getter round-trip")
        void enabledSetterGetter() {
            properties.getA11y().setEnabled(false);
            assertThat(properties.getA11y().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("a11y.failOnError setter/getter round-trip")
        void failOnErrorSetterGetter() {
            properties.getA11y().setFailOnError(true);
            assertThat(properties.getA11y().isFailOnError()).isTrue();
        }

        @Test
        @DisplayName("a11y.defaultLang setter/getter round-trip")
        void defaultLangSetterGetter() {
            properties.getA11y().setDefaultLang("es");
            assertThat(properties.getA11y().getDefaultLang()).isEqualTo("es");
        }

        @Test
        @DisplayName("a11y.minTargetSize setter/getter round-trip")
        void minTargetSizeSetterGetter() {
            properties.getA11y().setMinTargetSize(44);
            assertThat(properties.getA11y().getMinTargetSize()).isEqualTo(44);
        }

        @Test
        @DisplayName("a11y.reportEndpoint setter/getter round-trip")
        void reportEndpointSetterGetter() {
            properties.getA11y().setReportEndpoint("/api/a11y/audit");
            assertThat(properties.getA11y().getReportEndpoint()).isEqualTo("/api/a11y/audit");
        }

        @Test
        @DisplayName("a11y.auditOnRender setter/getter round-trip")
        void auditOnRenderSetterGetter() {
            properties.getA11y().setAuditOnRender(false);
            assertThat(properties.getA11y().isAuditOnRender()).isFalse();
        }

        @Test
        @DisplayName("a11y.skipNav setter/getter round-trip")
        void skipNavSetterGetter() {
            properties.getA11y().setSkipNav(false);
            assertThat(properties.getA11y().isSkipNav()).isFalse();
        }

        @Test
        @DisplayName("a11y.logViolations setter/getter round-trip")
        void logViolationsSetterGetter() {
            properties.getA11y().setLogViolations(false);
            assertThat(properties.getA11y().isLogViolations()).isFalse();
        }

        @Test
        @DisplayName("a11y.focusVisible setter/getter round-trip")
        void focusVisibleSetterGetter() {
            properties.getA11y().setFocusVisible(false);
            assertThat(properties.getA11y().isFocusVisible()).isFalse();
        }

        @Test
        @DisplayName("a11y.contrastCheck setter/getter round-trip")
        void contrastCheckSetterGetter() {
            properties.getA11y().setContrastCheck(false);
            assertThat(properties.getA11y().isContrastCheck()).isFalse();
        }

        @Test
        @DisplayName("a11y.autoFix setter/getter round-trip")
        void autoFixSetterGetter() {
            properties.getA11y().setAutoFix(false);
            assertThat(properties.getA11y().isAutoFix()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  I18n defaults
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("I18n configuration defaults")
    class I18nDefaults {

        @Test
        @DisplayName("i18n.enabled defaults to true")
        void enabledDefault() {
            assertThat(properties.getI18n().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("i18n.defaultLocale defaults to 'en'")
        void defaultLocaleDefault() {
            assertThat(properties.getI18n().getDefaultLocale()).isEqualTo("en");
        }

        @Test
        @DisplayName("i18n.locales defaults to ['en']")
        void localesDefault() {
            assertThat(properties.getI18n().getLocales()).containsExactly("en");
        }

        @Test
        @DisplayName("i18n.fallbackStrategy defaults to 'default'")
        void fallbackStrategyDefault() {
            assertThat(properties.getI18n().getFallbackStrategy()).isEqualTo("default");
        }

        @Test
        @DisplayName("i18n.urlStrategy defaults to 'prefix'")
        void urlStrategyDefault() {
            assertThat(properties.getI18n().getUrlStrategy()).isEqualTo("prefix");
        }

        @Test
        @DisplayName("i18n.cookieName defaults to 'jux-lang'")
        void cookieNameDefault() {
            assertThat(properties.getI18n().getCookieName()).isEqualTo("jux-lang");
        }

        @Test
        @DisplayName("i18n.cookieMaxAge defaults to 31536000 (1 year)")
        void cookieMaxAgeDefault() {
            assertThat(properties.getI18n().getCookieMaxAge()).isEqualTo(31536000);
        }

        @Test
        @DisplayName("i18n.redirectOnMissing defaults to true")
        void redirectOnMissingDefault() {
            assertThat(properties.getI18n().isRedirectOnMissing()).isTrue();
        }

        @Test
        @DisplayName("i18n.generateHreflang defaults to true")
        void generateHreflangDefault() {
            assertThat(properties.getI18n().isGenerateHreflang()).isTrue();
        }

        @Test
        @DisplayName("i18n.generateSitemapAlternates defaults to true")
        void generateSitemapAlternatesDefault() {
            assertThat(properties.getI18n().isGenerateSitemapAlternates()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Nested getters return non-null instances
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Nested configuration groups are never null")
    class NestedGroupsNotNull {

        @Test
        @DisplayName("getSsr() is never null")
        void ssrNotNull() {
            assertThat(properties.getSsr()).isNotNull();
        }

        @Test
        @DisplayName("getSsr().getCache() is never null")
        void ssrCacheNotNull() {
            assertThat(properties.getSsr().getCache()).isNotNull();
        }

        @Test
        @DisplayName("getClient() is never null")
        void clientNotNull() {
            assertThat(properties.getClient()).isNotNull();
        }

        @Test
        @DisplayName("getResources() is never null")
        void resourcesNotNull() {
            assertThat(properties.getResources()).isNotNull();
        }

        @Test
        @DisplayName("getTheme() is never null")
        void themeNotNull() {
            assertThat(properties.getTheme()).isNotNull();
        }

        @Test
        @DisplayName("getMetaDefaults() is never null")
        void metaDefaultsNotNull() {
            assertThat(properties.getMetaDefaults()).isNotNull();
        }

        @Test
        @DisplayName("getA11y() is never null")
        void a11yNotNull() {
            assertThat(properties.getA11y()).isNotNull();
        }

        @Test
        @DisplayName("getI18n() is never null")
        void i18nNotNull() {
            assertThat(properties.getI18n()).isNotNull();
        }
    }
}
