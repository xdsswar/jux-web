package xss.it.jux.theme;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DesignTokens} -- CSS custom property reference and generation.
 *
 * <p>Validates that token reference methods return correct var() strings,
 * spacing and shadow range validation, and CSS variable generation.</p>
 */
class DesignTokensTest {

    // ── Token reference methods ──────────────────────────────────

    @Test
    void primaryColor_returnsCorrectVarReference() {
        assertThat(DesignTokens.primaryColor()).isEqualTo("var(--jux-primary)");
    }

    @Test
    void secondaryColor_returnsCorrectVarReference() {
        assertThat(DesignTokens.secondaryColor()).isEqualTo("var(--jux-secondary)");
    }

    @Test
    void backgroundColor_returnsCorrectVarReference() {
        assertThat(DesignTokens.backgroundColor()).isEqualTo("var(--jux-bg)");
    }

    @Test
    void textColor_returnsCorrectVarReference() {
        assertThat(DesignTokens.textColor()).isEqualTo("var(--jux-text)");
    }

    @Test
    void fontFamily_returnsCorrectVarReference() {
        assertThat(DesignTokens.fontFamily()).isEqualTo("var(--jux-font-family)");
    }

    @Test
    void borderRadius_returnsCorrectVarReference() {
        assertThat(DesignTokens.borderRadius()).isEqualTo("var(--jux-radius)");
    }

    @Test
    void focusRing_returnsCorrectVarReference() {
        assertThat(DesignTokens.focusRing()).isEqualTo("var(--jux-focus-ring)");
    }

    // ── Spacing scale ────────────────────────────────────────────

    @Test
    void spacing_level1_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(1)).isEqualTo("var(--jux-space-1)");
    }

    @Test
    void spacing_level2_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(2)).isEqualTo("var(--jux-space-2)");
    }

    @Test
    void spacing_level3_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(3)).isEqualTo("var(--jux-space-3)");
    }

    @Test
    void spacing_level4_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(4)).isEqualTo("var(--jux-space-4)");
    }

    @Test
    void spacing_level5_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(5)).isEqualTo("var(--jux-space-5)");
    }

    @Test
    void spacing_level6_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(6)).isEqualTo("var(--jux-space-6)");
    }

    @Test
    void spacing_level7_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(7)).isEqualTo("var(--jux-space-7)");
    }

    @Test
    void spacing_level8_returnsCorrectVarReference() {
        assertThat(DesignTokens.spacing(8)).isEqualTo("var(--jux-space-8)");
    }

    @Test
    void spacing_level0_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.spacing(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void spacing_level9_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.spacing(9))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void spacing_negativeLevel_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.spacing(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Shadow scale ─────────────────────────────────────────────

    @Test
    void shadow_sm_returnsCorrectVarReference() {
        assertThat(DesignTokens.shadow("sm")).isEqualTo("var(--jux-shadow-sm)");
    }

    @Test
    void shadow_md_returnsCorrectVarReference() {
        assertThat(DesignTokens.shadow("md")).isEqualTo("var(--jux-shadow-md)");
    }

    @Test
    void shadow_lg_returnsCorrectVarReference() {
        assertThat(DesignTokens.shadow("lg")).isEqualTo("var(--jux-shadow-lg)");
    }

    @Test
    void shadow_xl_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.shadow("xl"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shadow_invalidSize_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.shadow("tiny"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shadow_emptyString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> DesignTokens.shadow(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shadow_null_throwsNullPointerException() {
        assertThatThrownBy(() -> DesignTokens.shadow(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── CSS variable generation ──────────────────────────────────

    @Test
    void generateCssVariables_containsRootBlock() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains(":root {");
        assertThat(css).contains("}");
    }

    @Test
    void generateCssVariables_containsPrimaryColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-primary: #3b82f6");
    }

    @Test
    void generateCssVariables_containsSecondaryColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-secondary:");
    }

    @Test
    void generateCssVariables_containsBackgroundColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-bg:");
    }

    @Test
    void generateCssVariables_containsTextColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-text:");
    }

    @Test
    void generateCssVariables_containsFontFamily() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-font-family: system-ui, sans-serif");
    }

    @Test
    void generateCssVariables_containsSpacingScale() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        for (int i = 1; i <= 8; i++) {
            assertThat(css).contains("--jux-space-" + i + ":");
        }
    }

    @Test
    void generateCssVariables_containsShadowScale() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-shadow-sm:");
        assertThat(css).contains("--jux-shadow-md:");
        assertThat(css).contains("--jux-shadow-lg:");
    }

    @Test
    void generateCssVariables_containsFocusRing() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-focus-ring:");
    }

    @Test
    void generateCssVariables_containsBorderRadius() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-radius:");
    }

    @Test
    void generateCssVariables_containsTransition() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-transition:");
    }

    @Test
    void generateCssVariables_customPrimaryColor() {
        ThemeProperties props = new ThemeProperties();
        props.setPrimaryColor("#ff0000");
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-primary: #ff0000");
    }

    @Test
    void generateCssVariables_customFontFamily() {
        ThemeProperties props = new ThemeProperties();
        props.setFontFamily("Inter, sans-serif");
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-font-family: Inter, sans-serif");
    }

    @Test
    void generateCssVariables_lightMode_whiteBackground() {
        ThemeProperties props = new ThemeProperties();
        props.setDarkMode(false);
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-bg: #ffffff");
    }

    @Test
    void generateCssVariables_darkMode_darkBackground() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        // The [data-theme="dark"] block must contain the dark background color
        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-bg: #0f172a");
    }

    @Test
    void generateCssVariables_lightMode_darkText() {
        ThemeProperties props = new ThemeProperties();
        props.setDarkMode(false);
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-text: #0f172a");
    }

    @Test
    void generateCssVariables_darkMode_lightText() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        // The [data-theme="dark"] block must contain the light text color
        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-text: #f1f5f9");
    }

    @Test
    void generateCssVariables_darkMode_differentShadows() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        // :root block contains light shadows (0.05 opacity)
        String rootBlock = css.substring(0, css.indexOf("[data-theme=\"dark\"]"));
        assertThat(rootBlock).contains("rgba(0, 0, 0, 0.05)");

        // [data-theme="dark"] block contains dark shadows (0.3 opacity)
        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("rgba(0, 0, 0, 0.3)");
    }

    @Test
    void generateCssVariables_focusRingUsesPrimaryColor() {
        ThemeProperties props = new ThemeProperties();
        props.setPrimaryColor("#22c55e");
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-focus-ring: 2px solid #22c55e");
    }

    @Test
    void generateCssVariables_nullPropsThrowsNullPointerException() {
        assertThatThrownBy(() -> DesignTokens.generateCssVariables(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void generateCssVariables_containsSurfaceColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-surface:");
    }

    @Test
    void generateCssVariables_containsBorderColor() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-border:");
    }

    @Test
    void generateCssVariables_containsFontSizes() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("--jux-font-size-base:");
        assertThat(css).contains("--jux-font-size-sm:");
        assertThat(css).contains("--jux-font-size-lg:");
        assertThat(css).contains("--jux-font-size-xl:");
    }

    // ── Dual-mode (light + dark) generation tests ──────────────

    @Test
    void generateCssVariables_containsDataThemeDarkBlock() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        assertThat(css).contains("[data-theme=\"dark\"] {");
    }

    @Test
    void generateCssVariables_darkBlockOverridesBackground() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-bg: #0f172a");
    }

    @Test
    void generateCssVariables_darkBlockOverridesText() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-text: #f1f5f9");
    }

    @Test
    void generateCssVariables_darkBlockOverridesSurface() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-surface: #1e293b");
    }

    @Test
    void generateCssVariables_darkBlockOverridesBorder() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).contains("--jux-border: #334155");
    }

    @Test
    void generateCssVariables_darkBlockDoesNotContainTypography() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).doesNotContain("--jux-font-family");
    }

    @Test
    void generateCssVariables_darkBlockDoesNotContainSpacing() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariables(props);

        String darkBlock = css.substring(css.indexOf("[data-theme=\"dark\"]"));
        assertThat(darkBlock).doesNotContain("--jux-space-1");
    }

    // ── Single-mode generation tests ───────────────────────────

    @Test
    void generateCssVariablesSingleMode_lightMode_whiteBackground() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariablesSingleMode(props, false);

        assertThat(css).contains("--jux-bg: #ffffff");
    }

    @Test
    void generateCssVariablesSingleMode_darkMode_darkBackground() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariablesSingleMode(props, true);

        assertThat(css).contains("--jux-bg: #0f172a");
    }

    @Test
    void generateCssVariablesSingleMode_containsOnlySingleRootBlock() {
        ThemeProperties props = new ThemeProperties();
        String css = DesignTokens.generateCssVariablesSingleMode(props, false);

        // Should contain exactly one :root { block
        int rootCount = 0;
        int idx = 0;
        while ((idx = css.indexOf(":root {", idx)) != -1) {
            rootCount++;
            idx += 7;
        }
        assertThat(rootCount).isEqualTo(1);

        // Should not contain any [data-theme block
        assertThat(css).doesNotContain("[data-theme");
    }
}
