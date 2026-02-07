package xss.it.jux.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link RtlDetector} -- a static utility that determines whether
 * a locale uses a right-to-left script.
 */
class RtlDetectorTest {

    // ── isRtl: RTL languages ─────────────────────────────────────

    @Test
    void isRtl_returnsTrue_forArabic() {
        assertThat(RtlDetector.isRtl(Locale.of("ar"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forHebrew() {
        assertThat(RtlDetector.isRtl(Locale.of("he"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forFarsi() {
        assertThat(RtlDetector.isRtl(Locale.of("fa"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forUrdu() {
        assertThat(RtlDetector.isRtl(Locale.of("ur"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forPashto() {
        assertThat(RtlDetector.isRtl(Locale.of("ps"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forSindhi() {
        assertThat(RtlDetector.isRtl(Locale.of("sd"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forYiddish() {
        assertThat(RtlDetector.isRtl(Locale.of("yi"))).isTrue();
    }

    // ── isRtl: LTR languages ────────────────────────────────────

    @Test
    void isRtl_returnsFalse_forEnglish() {
        assertThat(RtlDetector.isRtl(Locale.ENGLISH)).isFalse();
    }

    @Test
    void isRtl_returnsFalse_forSpanish() {
        assertThat(RtlDetector.isRtl(Locale.of("es"))).isFalse();
    }

    @Test
    void isRtl_returnsFalse_forFrench() {
        assertThat(RtlDetector.isRtl(Locale.of("fr"))).isFalse();
    }

    @Test
    void isRtl_returnsFalse_forGerman() {
        assertThat(RtlDetector.isRtl(Locale.of("de"))).isFalse();
    }

    // ── isRtl: null handling ─────────────────────────────────────

    @Test
    void isRtl_returnsFalse_forNull() {
        assertThat(RtlDetector.isRtl(null)).isFalse();
    }

    // ── isRtl: region subtags are ignored ────────────────────────

    @Test
    void isRtl_returnsTrue_forArabicWithRegion() {
        // ar-EG (Arabic, Egypt) should still be RTL
        assertThat(RtlDetector.isRtl(Locale.forLanguageTag("ar-EG"))).isTrue();
    }

    @Test
    void isRtl_returnsFalse_forEnglishWithRegion() {
        // en-US should still be LTR
        assertThat(RtlDetector.isRtl(Locale.forLanguageTag("en-US"))).isFalse();
    }

    // ── direction ────────────────────────────────────────────────

    @Test
    void direction_returnsRtl_forArabic() {
        assertThat(RtlDetector.direction(Locale.of("ar"))).isEqualTo("rtl");
    }

    @Test
    void direction_returnsLtr_forEnglish() {
        assertThat(RtlDetector.direction(Locale.ENGLISH)).isEqualTo("ltr");
    }

    @Test
    void direction_returnsRtl_forHebrew() {
        assertThat(RtlDetector.direction(Locale.of("he"))).isEqualTo("rtl");
    }

    @Test
    void direction_returnsLtr_forFrench() {
        assertThat(RtlDetector.direction(Locale.of("fr"))).isEqualTo("ltr");
    }

    @Test
    void direction_returnsLtr_forNull() {
        assertThat(RtlDetector.direction(null)).isEqualTo("ltr");
    }

    // ── Additional RTL languages in the set ──────────────────────

    @Test
    void isRtl_returnsTrue_forKurdish() {
        assertThat(RtlDetector.isRtl(Locale.of("ku"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forUyghur() {
        assertThat(RtlDetector.isRtl(Locale.of("ug"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forDhivehi() {
        assertThat(RtlDetector.isRtl(Locale.of("dv"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forKashmiri() {
        assertThat(RtlDetector.isRtl(Locale.of("ks"))).isTrue();
    }

    @Test
    void isRtl_returnsTrue_forHausa() {
        assertThat(RtlDetector.isRtl(Locale.of("ha"))).isTrue();
    }
}
