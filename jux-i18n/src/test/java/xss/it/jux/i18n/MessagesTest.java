package xss.it.jux.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.Message;
import xss.it.jux.annotation.MessageBundle;
import xss.it.jux.annotation.MessageLocale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Messages} -- the central i18n service.
 *
 * <p>Each test constructs a real {@link Messages} instance with a real
 * {@link MessageBundleRegistry} and {@link I18nProperties}, avoiding
 * mocks to test the actual integration between components.</p>
 */
class MessagesTest {

    private I18nProperties properties;
    private MessageBundleRegistry registry;
    private Messages messages;

    @BeforeEach
    void setUp() {
        properties = new I18nProperties();
        properties.setDefaultLocale("en");
        properties.setLocales(List.of("en", "es", "fr", "ar"));
        registry = new MessageBundleRegistry(properties);
        messages = new Messages(registry, properties);
    }

    @AfterEach
    void tearDown() {
        messages.clearCurrentLocale();
    }

    // ── currentLocale ────────────────────────────────────────────

    @Test
    void currentLocale_returnsDefaultLocale_whenNoThreadLocalSet() {
        Locale locale = messages.currentLocale();
        assertThat(locale.getLanguage()).isEqualTo("en");
    }

    @Test
    void currentLocale_returnsFrench_afterSetCurrentLocale() {
        messages.setCurrentLocale(Locale.FRENCH);
        assertThat(messages.currentLocale()).isEqualTo(Locale.FRENCH);
    }

    @Test
    void clearCurrentLocale_resetsToDefault() {
        messages.setCurrentLocale(Locale.FRENCH);
        assertThat(messages.currentLocale()).isEqualTo(Locale.FRENCH);

        messages.clearCurrentLocale();
        assertThat(messages.currentLocale().getLanguage()).isEqualTo("en");
    }

    // ── isRtl ────────────────────────────────────────────────────

    @Test
    void isRtl_returnsFalse_forEnglishLocale() {
        messages.setCurrentLocale(Locale.ENGLISH);
        assertThat(messages.isRtl()).isFalse();
    }

    @Test
    void isRtl_returnsTrue_forArabicLocale() {
        messages.setCurrentLocale(Locale.forLanguageTag("ar"));
        assertThat(messages.isRtl()).isTrue();
    }

    // ── availableLocales ─────────────────────────────────────────

    @Test
    void availableLocales_returnsConfiguredLocales() {
        List<Locale> locales = messages.availableLocales();
        assertThat(locales).hasSize(4);
        assertThat(locales.stream().map(Locale::getLanguage).toList())
            .containsExactly("en", "es", "fr", "ar");
    }

    // ── formatDate ───────────────────────────────────────────────

    @Test
    void formatDate_localDate_producesLocaleAwareOutput() {
        messages.setCurrentLocale(Locale.US);
        LocalDate date = LocalDate.of(2026, 2, 6);
        String formatted = messages.formatDate(date);
        // US MEDIUM format: "Feb 6, 2026"
        assertThat(formatted).contains("Feb");
        assertThat(formatted).contains("6");
        assertThat(formatted).contains("2026");
    }

    @Test
    void formatDate_withFullStyle_includesDayOfWeek() {
        messages.setCurrentLocale(Locale.US);
        LocalDate date = LocalDate.of(2026, 2, 6);
        String formatted = messages.formatDate(date, FormatStyle.FULL);
        // FULL format in US: "Friday, February 6, 2026"
        assertThat(formatted).contains("Friday");
        assertThat(formatted).contains("February");
    }

    @Test
    void formatDate_returnsEmptyString_forNullDate() {
        messages.setCurrentLocale(Locale.US);
        assertThat(messages.formatDate((LocalDate) null)).isEmpty();
    }

    @Test
    void formatDate_localDateTime_producesOutput() {
        messages.setCurrentLocale(Locale.US);
        LocalDateTime dateTime = LocalDateTime.of(2026, 2, 6, 15, 45, 0);
        String formatted = messages.formatDate(dateTime);
        assertThat(formatted).contains("Feb");
        assertThat(formatted).contains("2026");
    }

    @Test
    void formatDate_localDateTime_returnsEmptyString_forNull() {
        messages.setCurrentLocale(Locale.US);
        assertThat(messages.formatDate((LocalDateTime) null)).isEmpty();
    }

    // ── formatNumber ─────────────────────────────────────────────

    @Test
    void formatNumber_usesLocaleGrouping() {
        messages.setCurrentLocale(Locale.US);
        String formatted = messages.formatNumber(1234.5);
        assertThat(formatted).isEqualTo("1,234.5");
    }

    @Test
    void formatNumber_withDecimals_formatsCorrectly() {
        messages.setCurrentLocale(Locale.US);
        String formatted = messages.formatNumber(1234.5, 2);
        assertThat(formatted).isEqualTo("1,234.50");
    }

    @Test
    void formatNumber_returnsEmptyString_forNull() {
        messages.setCurrentLocale(Locale.US);
        assertThat(messages.formatNumber(null)).isEmpty();
    }

    @Test
    void formatNumber_withDecimals_returnsEmptyString_forNull() {
        messages.setCurrentLocale(Locale.US);
        assertThat(messages.formatNumber(null, 2)).isEmpty();
    }

    // ── formatCurrency ───────────────────────────────────────────

    @Test
    void formatCurrency_containsDollarSign_forUsLocale() {
        messages.setCurrentLocale(Locale.US);
        String formatted = messages.formatCurrency(29.99, "USD");
        assertThat(formatted).contains("$");
        assertThat(formatted).contains("29.99");
    }

    @Test
    void formatCurrency_returnsEmptyString_forNullAmount() {
        messages.setCurrentLocale(Locale.US);
        assertThat(messages.formatCurrency(null, "USD")).isEmpty();
    }

    // ── formatRelative ───────────────────────────────────────────

    @Test
    void formatRelative_returnsEmptyString_forNull() {
        assertThat(messages.formatRelative(null)).isEmpty();
    }

    @Test
    void formatRelative_returnsJustNow_forRecentPast() {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        assertThat(messages.formatRelative(tenSecondsAgo)).isEqualTo("just now");
    }

    @Test
    void formatRelative_returnsMinutesAgo_forPastMinutes() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        String result = messages.formatRelative(fiveMinutesAgo);
        assertThat(result).isEqualTo("5 minutes ago");
    }

    @Test
    void formatRelative_returnsHoursAgo_forPastHours() {
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
        String result = messages.formatRelative(threeHoursAgo);
        assertThat(result).isEqualTo("3 hours ago");
    }

    @Test
    void formatRelative_returnsDaysAgo_forPastDays() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        String result = messages.formatRelative(twoDaysAgo);
        assertThat(result).isEqualTo("2 days ago");
    }

    @Test
    void formatRelative_returnsInAMoment_forNearFuture() {
        LocalDateTime tenSecondsFromNow = LocalDateTime.now().plusSeconds(10);
        assertThat(messages.formatRelative(tenSecondsFromNow)).isEqualTo("in a moment");
    }

    @Test
    void formatRelative_returnsInMinutes_forFutureMinutes() {
        LocalDateTime fiveMinutesFromNow = LocalDateTime.now().plusMinutes(5);
        String result = messages.formatRelative(fiveMinutesFromNow);
        assertThat(result).isEqualTo("in 5 minutes");
    }

    // ── Thread-local isolation ───────────────────────────────────

    @Test
    void threadLocalIsolation_settingLocaleInOneThread_doesNotAffectAnother() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Locale> otherThreadLocale = new AtomicReference<>();

        messages.setCurrentLocale(Locale.FRENCH);

        Thread other = new Thread(() -> {
            // This thread has no locale set, should get the default
            otherThreadLocale.set(messages.currentLocale());
            latch.countDown();
        });
        other.start();
        latch.await();

        assertThat(messages.currentLocale()).isEqualTo(Locale.FRENCH);
        assertThat(otherThreadLocale.get().getLanguage()).isEqualTo("en");
    }

    // ── getString (ResourceBundle-based) ─────────────────────────

    @Test
    void getString_returnsKeyItself_whenBaseNameNotConfigured() {
        // baseName defaults to "" which means no ResourceBundle lookup
        String result = messages.getString("some.unknown.key");
        assertThat(result).isEqualTo("some.unknown.key");
    }

    @Test
    void getString_returnsKeyItself_whenKeyNotFoundInBundle() {
        properties.setBaseName("lang");
        // Even with baseName set, if no resource bundle file exists,
        // MissingResourceException is caught and the key is returned
        String result = messages.getString("nonexistent.key");
        assertThat(result).isEqualTo("nonexistent.key");
    }

    @Test
    void getString_withArgs_returnsKey_whenPatternNotFound() {
        // baseName is empty, so getString(key) returns the key itself
        // Since pattern.equals(key), getString(key, args) also returns the key
        String result = messages.getString("missing.key", "arg1", "arg2");
        assertThat(result).isEqualTo("missing.key");
    }

    // ── Typed message bundles via get(Class) ─────────────────────

    @MessageBundle
    interface TestMessages {
        @Message("Hello World")
        String hello();

        @Message("Goodbye, {0}")
        String goodbye(String name);
    }

    @MessageBundle
    @MessageLocale("es")
    interface TestMessagesEs extends TestMessages {
        @Override
        @Message("Hola Mundo")
        String hello();

        @Override
        @Message("Adios, {0}")
        String goodbye(String name);
    }

    @Test
    void get_returnsDefaultMessages_whenLocaleIsDefault() {
        registry.registerBundle(TestMessages.class);
        registry.registerLocaleBundle(TestMessagesEs.class);

        messages.setCurrentLocale(Locale.ENGLISH);
        TestMessages t = messages.get(TestMessages.class);
        assertThat(t.hello()).isEqualTo("Hello World");
    }

    @Test
    void get_returnsSpanishMessages_whenLocaleIsSpanish() {
        registry.registerBundle(TestMessages.class);
        registry.registerLocaleBundle(TestMessagesEs.class);

        messages.setCurrentLocale(Locale.forLanguageTag("es"));
        TestMessages t = messages.get(TestMessages.class);
        assertThat(t.hello()).isEqualTo("Hola Mundo");
    }

    @Test
    void get_withExplicitLocale_returnsCorrectTranslation() {
        registry.registerBundle(TestMessages.class);
        registry.registerLocaleBundle(TestMessagesEs.class);

        messages.setCurrentLocale(Locale.ENGLISH);
        // Request Spanish explicitly even though thread locale is English
        TestMessages t = messages.get(TestMessages.class, Locale.forLanguageTag("es"));
        assertThat(t.hello()).isEqualTo("Hola Mundo");
    }

    @Test
    void get_formatsArguments_withMessageFormat() {
        registry.registerBundle(TestMessages.class);
        registry.registerLocaleBundle(TestMessagesEs.class);

        messages.setCurrentLocale(Locale.forLanguageTag("es"));
        TestMessages t = messages.get(TestMessages.class);
        assertThat(t.goodbye("Maria")).isEqualTo("Adios, Maria");
    }

    @Test
    void get_fallsBackToDefault_whenLocaleNotRegistered() {
        registry.registerBundle(TestMessages.class);
        // No French locale registered

        messages.setCurrentLocale(Locale.FRENCH);
        TestMessages t = messages.get(TestMessages.class);
        // Falls back to the base bundle (English)
        assertThat(t.hello()).isEqualTo("Hello World");
    }
}
