package xss.it.jux.theme;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ThemeProperties} -- theme configuration POJO.
 *
 * <p>Validates default values, getters, and setters for all
 * theme configuration properties.</p>
 */
class ThemePropertiesTest {

    @Test
    void defaults_nameIsDefault() {
        ThemeProperties props = new ThemeProperties();
        assertThat(props.getName()).isEqualTo("default");
    }

    @Test
    void defaults_darkModeIsFalse() {
        ThemeProperties props = new ThemeProperties();
        assertThat(props.isDarkMode()).isFalse();
    }

    @Test
    void defaults_primaryColorIsBlue() {
        ThemeProperties props = new ThemeProperties();
        assertThat(props.getPrimaryColor()).isEqualTo("#3b82f6");
    }

    @Test
    void defaults_fontFamilyIsSystemUi() {
        ThemeProperties props = new ThemeProperties();
        assertThat(props.getFontFamily()).isEqualTo("system-ui, sans-serif");
    }

    @Test
    void defaults_customCssPathIsNull() {
        ThemeProperties props = new ThemeProperties();
        assertThat(props.getCustomCssPath()).isNull();
    }

    @Test
    void setName_changesValue() {
        ThemeProperties props = new ThemeProperties();
        props.setName("corporate");
        assertThat(props.getName()).isEqualTo("corporate");
    }

    @Test
    void setDarkMode_changesValue() {
        ThemeProperties props = new ThemeProperties();
        props.setDarkMode(true);
        assertThat(props.isDarkMode()).isTrue();
    }

    @Test
    void setPrimaryColor_changesValue() {
        ThemeProperties props = new ThemeProperties();
        props.setPrimaryColor("#ff0000");
        assertThat(props.getPrimaryColor()).isEqualTo("#ff0000");
    }

    @Test
    void setFontFamily_changesValue() {
        ThemeProperties props = new ThemeProperties();
        props.setFontFamily("Inter, sans-serif");
        assertThat(props.getFontFamily()).isEqualTo("Inter, sans-serif");
    }

    @Test
    void setCustomCssPath_changesValue() {
        ThemeProperties props = new ThemeProperties();
        props.setCustomCssPath("css/overrides.css");
        assertThat(props.getCustomCssPath()).isEqualTo("css/overrides.css");
    }

    @Test
    void setCustomCssPath_nullClearsValue() {
        ThemeProperties props = new ThemeProperties();
        props.setCustomCssPath("css/overrides.css");
        props.setCustomCssPath(null);
        assertThat(props.getCustomCssPath()).isNull();
    }
}
