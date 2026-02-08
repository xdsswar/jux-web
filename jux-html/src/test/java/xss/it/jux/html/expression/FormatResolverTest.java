package xss.it.jux.html.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link FormatResolver}.
 *
 * <p>Covers all built-in directives (dir, lang, locale, country,
 * displayLang, displayCountry, currency, date, number), custom
 * directive registration and overriding, resolveAll with mixed
 * text, locale variations, and error handling.</p>
 */
@DisplayName("FormatResolver")
class FormatResolverTest {

    // ── Built-in simple directives ──────────────────────────────

    @Nested
    @DisplayName("built-in directive: dir")
    class DirDirectiveTests {

        @Test
        @DisplayName("English locale returns 'ltr'")
        void englishLocale_returnsLtr() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("dir")).isEqualTo("ltr");
        }

        @Test
        @DisplayName("French locale returns 'ltr'")
        void frenchLocale_returnsLtr() {
            var resolver = new FormatResolver(Locale.FRANCE);
            assertThat(resolver.resolve("dir")).isEqualTo("ltr");
        }

        @Test
        @DisplayName("Arabic locale returns 'rtl'")
        void arabicLocale_returnsRtl() {
            var resolver = new FormatResolver(Locale.of("ar"));
            assertThat(resolver.resolve("dir")).isEqualTo("rtl");
        }

        @Test
        @DisplayName("Hebrew locale returns 'rtl'")
        void hebrewLocale_returnsRtl() {
            var resolver = new FormatResolver(Locale.of("he"));
            assertThat(resolver.resolve("dir")).isEqualTo("rtl");
        }

        @Test
        @DisplayName("Farsi locale returns 'rtl'")
        void farsiLocale_returnsRtl() {
            var resolver = new FormatResolver(Locale.of("fa"));
            assertThat(resolver.resolve("dir")).isEqualTo("rtl");
        }

        @Test
        @DisplayName("Urdu locale returns 'rtl'")
        void urduLocale_returnsRtl() {
            var resolver = new FormatResolver(Locale.of("ur"));
            assertThat(resolver.resolve("dir")).isEqualTo("rtl");
        }
    }

    @Nested
    @DisplayName("built-in directive: lang")
    class LangDirectiveTests {

        @Test
        @DisplayName("returns language code for US locale")
        void usLocale_returnsEn() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("lang")).isEqualTo("en");
        }

        @Test
        @DisplayName("returns language code for Japanese locale")
        void japaneseLocale_returnsJa() {
            var resolver = new FormatResolver(Locale.JAPAN);
            assertThat(resolver.resolve("lang")).isEqualTo("ja");
        }

        @Test
        @DisplayName("returns language code for German locale")
        void germanLocale_returnsDe() {
            var resolver = new FormatResolver(Locale.GERMANY);
            assertThat(resolver.resolve("lang")).isEqualTo("de");
        }
    }

    @Nested
    @DisplayName("built-in directive: locale")
    class LocaleDirectiveTests {

        @Test
        @DisplayName("returns full BCP 47 tag for en-US")
        void usLocale_returnsFullTag() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("locale")).isEqualTo("en-US");
        }

        @Test
        @DisplayName("returns language-only tag when no country")
        void languageOnlyLocale_returnsLanguageTag() {
            var resolver = new FormatResolver(Locale.of("fr"));
            assertThat(resolver.resolve("locale")).isEqualTo("fr");
        }
    }

    @Nested
    @DisplayName("built-in directive: country")
    class CountryDirectiveTests {

        @Test
        @DisplayName("returns country code for en-US")
        void usLocale_returnsUS() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("country")).isEqualTo("US");
        }

        @Test
        @DisplayName("returns empty string when no country set")
        void noCountry_returnsEmpty() {
            var resolver = new FormatResolver(Locale.of("en"));
            assertThat(resolver.resolve("country")).isEmpty();
        }

        @Test
        @DisplayName("returns DE for German locale")
        void germanLocale_returnsDE() {
            var resolver = new FormatResolver(Locale.GERMANY);
            assertThat(resolver.resolve("country")).isEqualTo("DE");
        }
    }

    @Nested
    @DisplayName("built-in directive: displayLang")
    class DisplayLangDirectiveTests {

        @Test
        @DisplayName("returns localized language name for English in English")
        void englishInEnglish_returnsEnglish() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("displayLang")).isEqualTo("English");
        }

        @Test
        @DisplayName("returns localized language name for French in French")
        void frenchInFrench_returnsFrancais() {
            var resolver = new FormatResolver(Locale.FRANCE);
            String result = resolver.resolve("displayLang");
            /* French displays its own language as "fran\u00e7ais" */
            assertThat(result).isEqualToIgnoringCase("fran\u00e7ais");
        }
    }

    @Nested
    @DisplayName("built-in directive: displayCountry")
    class DisplayCountryDirectiveTests {

        @Test
        @DisplayName("returns localized country name for US in English")
        void usInEnglish_returnsUnitedStates() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("displayCountry")).isEqualTo("United States");
        }

        @Test
        @DisplayName("returns localized country name for Germany in German")
        void germanyInGerman_returnsDeutschland() {
            var resolver = new FormatResolver(Locale.GERMANY);
            assertThat(resolver.resolve("displayCountry")).isEqualTo("Deutschland");
        }
    }

    // ── Built-in directives with arguments ──────────────────────

    @Nested
    @DisplayName("built-in directive: currency")
    class CurrencyDirectiveTests {

        @Test
        @DisplayName("formats amount with US locale currency symbol")
        void usLocale_formatsWithDollarSign() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("currency(29.99)");
            assertThat(result).isEqualTo("$29.99");
        }

        @Test
        @DisplayName("formats amount with German locale currency symbol")
        void germanLocale_formatsWithEuroSign() {
            var resolver = new FormatResolver(Locale.GERMANY);
            String result = resolver.resolve("currency(29.99)");
            /* German formats as "29,99 \u20ac" */
            assertThat(result).contains("29,99").contains("\u20ac");
        }

        @Test
        @DisplayName("formats whole number amount correctly")
        void wholeNumber_formatsCorrectly() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("currency(1500)");
            assertThat(result).isEqualTo("$1,500.00");
        }

        @Test
        @DisplayName("invalid number in currency throws IllegalArgumentException")
        void invalidNumber_throwsIllegalArgumentException() {
            var resolver = new FormatResolver(Locale.US);
            assertThatThrownBy(() -> resolver.resolve("currency(abc)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid number");
        }
    }

    @Nested
    @DisplayName("built-in directive: date")
    class DateDirectiveTests {

        @Test
        @DisplayName("formats with yyyy-MM-dd pattern to current date")
        void isoPattern_formatsToCurrentDate() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("date(yyyy-MM-dd)");
            /* Verify the result matches the expected format, not exact date */
            assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}");
            /* Verify it actually matches today */
            String expected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("formats with localized month name in English")
        void localizedPatternEnglish_usesEnglishMonthName() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("date(MMMM yyyy)");
            /* Should contain the full English month name */
            String expectedMonth = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("MMMM", Locale.US));
            assertThat(result).startsWith(expectedMonth);
        }

        @Test
        @DisplayName("empty pattern throws IllegalArgumentException")
        void emptyPattern_throwsIllegalArgumentException() {
            var resolver = new FormatResolver(Locale.US);
            assertThatThrownBy(() -> resolver.resolve("date()"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Empty pattern");
        }

        @Test
        @DisplayName("invalid pattern throws IllegalArgumentException")
        void invalidPattern_throwsIllegalArgumentException() {
            var resolver = new FormatResolver(Locale.US);
            assertThatThrownBy(() -> resolver.resolve("date(ZZZ$$$INVALID)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid date pattern");
        }
    }

    @Nested
    @DisplayName("built-in directive: number")
    class NumberDirectiveTests {

        @Test
        @DisplayName("formats number with specified decimal places for US locale")
        void usLocaleWithDecimals_formatsCorrectly() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("number(1250.5, 2)");
            assertThat(result).isEqualTo("1,250.50");
        }

        @Test
        @DisplayName("formats number with specified decimal places for German locale")
        void germanLocaleWithDecimals_formatsCorrectly() {
            var resolver = new FormatResolver(Locale.GERMANY);
            String result = resolver.resolve("number(1250.5, 2)");
            assertThat(result).isEqualTo("1.250,50");
        }

        @Test
        @DisplayName("formats number with zero decimals rounds correctly")
        void zeroDecimals_roundsCorrectly() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("number(1250.5, 0)");
            /* NumberFormat with 0 decimal places rounds 1250.5 */
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
            assertThat(result).isEqualTo(nf.format(1250.5));
        }

        @Test
        @DisplayName("formats number without decimals argument defaults to zero decimals")
        void noDecimalsArg_defaultsToZeroDecimals() {
            var resolver = new FormatResolver(Locale.US);
            String result = resolver.resolve("number(42)");
            assertThat(result).isEqualTo("42");
        }

        @Test
        @DisplayName("formats large number with French locale grouping")
        void frenchLocale_usesSpaceGrouping() {
            var resolver = new FormatResolver(Locale.FRANCE);
            String result = resolver.resolve("number(1000000, 0)");
            /*
             * French locale uses narrow no-break space (U+202F) or non-breaking
             * space (U+00A0) as grouping separator depending on JVM version.
             * Strip all non-digit characters and verify the digits are correct.
             */
            assertThat(result.replaceAll("[^0-9]", "")).isEqualTo("1000000");
        }

        @Test
        @DisplayName("invalid number throws IllegalArgumentException")
        void invalidNumber_throwsIllegalArgumentException() {
            var resolver = new FormatResolver(Locale.US);
            assertThatThrownBy(() -> resolver.resolve("number(xyz, 2)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid number");
        }

        @Test
        @DisplayName("missing value throws IllegalArgumentException")
        void missingValue_throwsIllegalArgumentException() {
            var resolver = new FormatResolver(Locale.US);
            assertThatThrownBy(() -> resolver.resolve("number()"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── resolveAll ──────────────────────────────────────────────

    @Nested
    @DisplayName("resolveAll()")
    class ResolveAllTests {

        private FormatResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new FormatResolver(Locale.US);
        }

        @Test
        @DisplayName("replaces single @{...} expression in text")
        void singleExpression_replacedInText() {
            String result = resolver.resolveAll("Direction: @{dir}");
            assertThat(result).isEqualTo("Direction: ltr");
        }

        @Test
        @DisplayName("replaces multiple @{...} expressions in same string")
        void multipleExpressions_allReplaced() {
            String result = resolver.resolveAll("Lang: @{lang}, Dir: @{dir}");
            assertThat(result).isEqualTo("Lang: en, Dir: ltr");
        }

        @Test
        @DisplayName("text without expressions returned unchanged")
        void noExpressions_returnedUnchanged() {
            String input = "Just regular text.";
            assertThat(resolver.resolveAll(input)).isEqualTo(input);
        }

        @Test
        @DisplayName("null text returns null")
        void nullText_returnsNull() {
            assertThat(resolver.resolveAll(null)).isNull();
        }

        @Test
        @DisplayName("empty text returns empty string")
        void emptyText_returnsEmptyString() {
            assertThat(resolver.resolveAll("")).isEmpty();
        }

        @Test
        @DisplayName("mixed text and directive-with-args expressions")
        void mixedTextAndDirectiveWithArgs_resolvedCorrectly() {
            String result = resolver.resolveAll("Price: @{currency(49.99)}, Lang: @{lang}");
            assertThat(result).isEqualTo("Price: $49.99, Lang: en");
        }
    }

    // ── Unknown directives ──────────────────────────────────────

    @Nested
    @DisplayName("unknown directives")
    class UnknownDirectiveTests {

        private FormatResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new FormatResolver(Locale.US);
        }

        @Test
        @DisplayName("unknown simple directive throws IllegalArgumentException")
        void unknownSimpleDirective_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> resolver.resolve("bogus"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown format directive")
                    .hasMessageContaining("bogus");
        }

        @Test
        @DisplayName("unknown directive with args throws IllegalArgumentException")
        void unknownDirectiveWithArgs_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> resolver.resolve("bogus(123)"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown format directive")
                    .hasMessageContaining("bogus");
        }
    }

    // ── Custom directives ───────────────────────────────────────

    @Nested
    @DisplayName("custom directives")
    class CustomDirectiveTests {

        @Test
        @DisplayName("custom directive is registered and resolvable")
        void customDirective_registeredAndResolvable() {
            FormatDirective custom = FormatDirective.simple("appVersion", locale -> "3.2.1");
            var resolver = new FormatResolver(Locale.US, List.of(custom));

            assertThat(resolver.resolve("appVersion")).isEqualTo("3.2.1");
        }

        @Test
        @DisplayName("custom directive with args is resolvable")
        void customDirectiveWithArgs_resolvable() {
            FormatDirective custom = FormatDirective.of("repeat", (args, locale) -> {
                String[] parts = args.split(",");
                String text = parts[0].trim();
                int times = Integer.parseInt(parts[1].trim());
                return text.repeat(times);
            });
            var resolver = new FormatResolver(Locale.US, List.of(custom));

            assertThat(resolver.resolve("repeat(ha, 3)")).isEqualTo("hahaha");
        }

        @Test
        @DisplayName("custom directive overrides built-in with same name")
        void customDirective_overridesBuiltIn() {
            FormatDirective custom = FormatDirective.simple("dir", locale -> "custom-dir");
            var resolver = new FormatResolver(Locale.US, List.of(custom));

            assertThat(resolver.resolve("dir")).isEqualTo("custom-dir");
        }

        @Test
        @DisplayName("getCustomDirectives returns registered custom directives")
        void getCustomDirectives_returnsRegisteredDirectives() {
            FormatDirective d1 = FormatDirective.simple("alpha", locale -> "a");
            FormatDirective d2 = FormatDirective.simple("beta", locale -> "b");
            var resolver = new FormatResolver(Locale.US, List.of(d1, d2));

            assertThat(resolver.getCustomDirectives())
                    .containsKey("alpha")
                    .containsKey("beta")
                    .hasSize(2);
        }

        @Test
        @DisplayName("getCustomDirectives returns empty map when none registered")
        void getCustomDirectives_emptyWhenNoneRegistered() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.getCustomDirectives()).isEmpty();
        }

        @Test
        @DisplayName("custom directives map is unmodifiable")
        void customDirectivesMap_isUnmodifiable() {
            FormatDirective custom = FormatDirective.simple("x", locale -> "x");
            var resolver = new FormatResolver(Locale.US, List.of(custom));

            assertThatThrownBy(() ->
                    resolver.getCustomDirectives().put("hack", custom)
            ).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ── FormatDirective factories ───────────────────────────────

    @Nested
    @DisplayName("FormatDirective factory methods")
    class FormatDirectiveFactoryTests {

        @Test
        @DisplayName("FormatDirective.of creates directive with correct name")
        void ofFactory_createsDirectiveWithCorrectName() {
            FormatDirective d = FormatDirective.of("myDir", (args, locale) -> "result");
            assertThat(d.name()).isEqualTo("myDir");
        }

        @Test
        @DisplayName("FormatDirective.of resolve receives args and locale")
        void ofFactory_resolveReceivesArgsAndLocale() {
            FormatDirective d = FormatDirective.of("echo", (args, locale) ->
                    args + "-" + locale.getLanguage());

            assertThat(d.resolve("hello", Locale.FRANCE)).isEqualTo("hello-fr");
        }

        @Test
        @DisplayName("FormatDirective.simple creates no-arg directive with correct name")
        void simpleFactory_createsDirectiveWithCorrectName() {
            FormatDirective d = FormatDirective.simple("version", locale -> "1.0");
            assertThat(d.name()).isEqualTo("version");
        }

        @Test
        @DisplayName("FormatDirective.simple resolve uses locale")
        void simpleFactory_resolveUsesLocale() {
            FormatDirective d = FormatDirective.simple("langName",
                    locale -> locale.getDisplayLanguage(Locale.ENGLISH));

            assertThat(d.resolve(null, Locale.GERMANY)).isEqualTo("German");
        }

        @Test
        @DisplayName("FormatDirective.of with null name throws NullPointerException")
        void ofFactoryNullName_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> FormatDirective.of(null, (a, l) -> "x"));
        }

        @Test
        @DisplayName("FormatDirective.of with null resolver throws NullPointerException")
        void ofFactoryNullResolver_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> FormatDirective.of("x", null));
        }

        @Test
        @DisplayName("FormatDirective.simple with null name throws NullPointerException")
        void simpleFactoryNullName_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> FormatDirective.simple(null, l -> "x"));
        }

        @Test
        @DisplayName("FormatDirective.simple with null resolver throws NullPointerException")
        void simpleFactoryNullResolver_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> FormatDirective.simple("x", null));
        }
    }

    // ── Constructor validation ───────────────────────────────────

    @Nested
    @DisplayName("constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("null locale throws NullPointerException")
        void nullLocale_throwsNullPointerException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FormatResolver(null))
                    .withMessage("locale must not be null");
        }

        @Test
        @DisplayName("null customDirectives collection throws NullPointerException")
        void nullCustomDirectives_throwsNullPointerException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new FormatResolver(Locale.US, null))
                    .withMessage("customDirectives must not be null");
        }

        @Test
        @DisplayName("getLocale returns the configured locale")
        void getLocale_returnsConfiguredLocale() {
            var resolver = new FormatResolver(Locale.JAPAN);
            assertThat(resolver.getLocale()).isEqualTo(Locale.JAPAN);
        }
    }

    // ── Edge cases and null/empty ────────────────────────────────

    @Nested
    @DisplayName("null and empty expression handling")
    class NullAndEmptyTests {

        private FormatResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new FormatResolver(Locale.US);
        }

        @Test
        @DisplayName("null expression returns empty string")
        void nullExpression_returnsEmptyString() {
            assertThat(resolver.resolve(null)).isEmpty();
        }

        @Test
        @DisplayName("empty expression returns empty string")
        void emptyExpression_returnsEmptyString() {
            assertThat(resolver.resolve("")).isEmpty();
        }
    }

    // ── Various locales for formatting differences ──────────────

    @Nested
    @DisplayName("locale-specific formatting variations")
    class LocaleVariationTests {

        @Test
        @DisplayName("Japanese locale currency formats with yen symbol")
        void japaneseLocale_currencyFormatsWithYen() {
            var resolver = new FormatResolver(Locale.JAPAN);
            String result = resolver.resolve("currency(1500)");
            /* Japanese currency should contain yen symbol and no decimals */
            assertThat(result).contains("\uFFE5").doesNotContain(".");
        }

        @Test
        @DisplayName("US locale number uses comma grouping separator")
        void usLocale_numberUsesCommaGrouping() {
            var resolver = new FormatResolver(Locale.US);
            assertThat(resolver.resolve("number(1000000, 0)")).isEqualTo("1,000,000");
        }

        @Test
        @DisplayName("German locale number uses period grouping and comma decimal")
        void germanLocale_numberUsesPeriodGroupingAndCommaDecimal() {
            var resolver = new FormatResolver(Locale.GERMANY);
            assertThat(resolver.resolve("number(1000.5, 1)")).isEqualTo("1.000,5");
        }

        @Test
        @DisplayName("dir for multiple RTL languages all return rtl")
        void multipleRtlLanguages_allReturnRtl() {
            for (String lang : List.of("ar", "he", "fa", "ur", "ps", "sd", "yi", "ku", "ug", "dv")) {
                var resolver = new FormatResolver(Locale.of(lang));
                assertThat(resolver.resolve("dir"))
                        .as("dir for language '%s'", lang)
                        .isEqualTo("rtl");
            }
        }

        @Test
        @DisplayName("dir for multiple LTR languages all return ltr")
        void multipleLtrLanguages_allReturnLtr() {
            for (String lang : List.of("en", "fr", "de", "es", "pt", "it", "ja", "zh", "ko", "ru")) {
                var resolver = new FormatResolver(Locale.of(lang));
                assertThat(resolver.resolve("dir"))
                        .as("dir for language '%s'", lang)
                        .isEqualTo("ltr");
            }
        }
    }
}
