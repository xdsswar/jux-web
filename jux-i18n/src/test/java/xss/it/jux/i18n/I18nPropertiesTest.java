package xss.it.jux.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link I18nProperties} -- the configuration POJO for the i18n subsystem.
 *
 * <p>Verifies default values for all properties and that getters/setters
 * work correctly, including the derived helper methods
 * {@link I18nProperties#getDefaultLocaleObj()} and
 * {@link I18nProperties#getLocaleObjects()}.</p>
 */
class I18nPropertiesTest {

    private I18nProperties props;

    @BeforeEach
    void setUp() {
        props = new I18nProperties();
    }

    // ── Default values ───────────────────────────────────────────

    @Test
    void defaultEnabled_isTrue() {
        assertThat(props.isEnabled()).isTrue();
    }

    @Test
    void defaultDefaultLocale_isEn() {
        assertThat(props.getDefaultLocale()).isEqualTo("en");
    }

    @Test
    void defaultLocales_containsOnlyEn() {
        assertThat(props.getLocales()).containsExactly("en");
    }

    @Test
    void defaultFallbackStrategy_isDefault() {
        assertThat(props.getFallbackStrategy()).isEqualTo("default");
    }

    @Test
    void defaultUrlStrategy_isPrefix() {
        assertThat(props.getUrlStrategy()).isEqualTo("prefix");
    }

    @Test
    void defaultCookieName_isJuxLang() {
        assertThat(props.getCookieName()).isEqualTo("jux-lang");
    }

    @Test
    void defaultCookieMaxAge_isOneYear() {
        assertThat(props.getCookieMaxAge()).isEqualTo(31536000);
    }

    @Test
    void defaultRedirectOnMissing_isTrue() {
        assertThat(props.isRedirectOnMissing()).isTrue();
    }

    @Test
    void defaultGenerateHreflang_isTrue() {
        assertThat(props.isGenerateHreflang()).isTrue();
    }

    @Test
    void defaultGenerateSitemapAlternates_isTrue() {
        assertThat(props.isGenerateSitemapAlternates()).isTrue();
    }

    @Test
    void defaultBaseName_isEmpty() {
        assertThat(props.getBaseName()).isEmpty();
    }

    // ── getDefaultLocaleObj ──────────────────────────────────────

    @Test
    void getDefaultLocaleObj_returnsEnLocaleByDefault() {
        Locale locale = props.getDefaultLocaleObj();
        assertThat(locale.getLanguage()).isEqualTo("en");
    }

    @Test
    void getDefaultLocaleObj_reflectsSetDefaultLocale() {
        props.setDefaultLocale("es");
        Locale locale = props.getDefaultLocaleObj();
        assertThat(locale.getLanguage()).isEqualTo("es");
    }

    @Test
    void getDefaultLocaleObj_handlesBcp47Tag() {
        props.setDefaultLocale("pt-BR");
        Locale locale = props.getDefaultLocaleObj();
        assertThat(locale.getLanguage()).isEqualTo("pt");
        assertThat(locale.getCountry()).isEqualTo("BR");
    }

    // ── getLocaleObjects ─────────────────────────────────────────

    @Test
    void getLocaleObjects_returnsOneLocaleByDefault() {
        List<Locale> locales = props.getLocaleObjects();
        assertThat(locales).hasSize(1);
        assertThat(locales.getFirst().getLanguage()).isEqualTo("en");
    }

    @Test
    void getLocaleObjects_returnsThreeLocales_afterSetLocales() {
        props.setLocales(List.of("en", "es", "fr"));
        List<Locale> locales = props.getLocaleObjects();
        assertThat(locales).hasSize(3);
        assertThat(locales.stream().map(Locale::getLanguage).toList())
            .containsExactly("en", "es", "fr");
    }

    @Test
    void getLocaleObjects_preservesConfiguredOrder() {
        props.setLocales(List.of("fr", "de", "en", "es"));
        List<Locale> locales = props.getLocaleObjects();
        assertThat(locales.stream().map(Locale::getLanguage).toList())
            .containsExactly("fr", "de", "en", "es");
    }

    // ── Setters ──────────────────────────────────────────────────

    @Test
    void setEnabled_false_disablesI18n() {
        props.setEnabled(false);
        assertThat(props.isEnabled()).isFalse();
    }

    @Test
    void setFallbackStrategy_chain() {
        props.setFallbackStrategy("chain");
        assertThat(props.getFallbackStrategy()).isEqualTo("chain");
    }

    @Test
    void setUrlStrategy_subdomain() {
        props.setUrlStrategy("subdomain");
        assertThat(props.getUrlStrategy()).isEqualTo("subdomain");
    }

    @Test
    void setCookieName_custom() {
        props.setCookieName("my-locale");
        assertThat(props.getCookieName()).isEqualTo("my-locale");
    }

    @Test
    void setCookieMaxAge_zero() {
        props.setCookieMaxAge(0);
        assertThat(props.getCookieMaxAge()).isZero();
    }

    @Test
    void setRedirectOnMissing_false() {
        props.setRedirectOnMissing(false);
        assertThat(props.isRedirectOnMissing()).isFalse();
    }

    @Test
    void setGenerateHreflang_false() {
        props.setGenerateHreflang(false);
        assertThat(props.isGenerateHreflang()).isFalse();
    }

    @Test
    void setGenerateSitemapAlternates_false() {
        props.setGenerateSitemapAlternates(false);
        assertThat(props.isGenerateSitemapAlternates()).isFalse();
    }

    @Test
    void setBaseName_messages() {
        props.setBaseName("messages");
        assertThat(props.getBaseName()).isEqualTo("messages");
    }
}
