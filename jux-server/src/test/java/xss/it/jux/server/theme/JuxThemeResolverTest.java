package xss.it.jux.server.theme;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import xss.it.jux.server.autoconfigure.JuxProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JuxThemeResolver}.
 *
 * <p>Verifies cookie-based theme resolution, validation of theme names
 * against the configured available themes, and proper delegation to
 * {@link JuxProperties.Theme} for configuration values.</p>
 */
@DisplayName("JuxThemeResolver")
class JuxThemeResolverTest {

    private JuxProperties.Theme themeProperties;
    private JuxThemeResolver resolver;

    @BeforeEach
    void setUp() {
        themeProperties = new JuxProperties.Theme();
        themeProperties.setName("default");
        themeProperties.setDefaultTheme("light");
        themeProperties.setAvailableThemes(List.of("light", "dark"));
        themeProperties.setCookieName("jux-theme");
        themeProperties.setCookieMaxAge(31536000);
        resolver = new JuxThemeResolver(themeProperties);
    }

    @Nested
    @DisplayName("resolve(HttpServletRequest)")
    class Resolve {

        @Test
        @DisplayName("returns default theme when request has no cookies")
        void returnsDefaultThemeWhenNoCookies() {
            var request = new MockHttpServletRequest();

            String result = resolver.resolve(request);

            assertThat(result).isEqualTo("light");
        }

        @Test
        @DisplayName("returns default theme when request cookies array is null")
        void returnsDefaultThemeWhenCookiesNull() {
            var request = new MockHttpServletRequest();
            // MockHttpServletRequest.getCookies() returns null when no cookies are set

            String result = resolver.resolve(request);

            assertThat(result).isEqualTo("light");
        }

        @Test
        @DisplayName("returns cookie value when valid theme cookie is present")
        void returnsCookieValueWhenValidThemeCookie() {
            var request = new MockHttpServletRequest();
            request.setCookies(new Cookie("jux-theme", "dark"));

            String result = resolver.resolve(request);

            assertThat(result).isEqualTo("dark");
        }

        @Test
        @DisplayName("returns default theme when cookie has invalid theme value")
        void returnsDefaultThemeWhenCookieHasInvalidValue() {
            var request = new MockHttpServletRequest();
            request.setCookies(new Cookie("jux-theme", "ocean"));

            String result = resolver.resolve(request);

            assertThat(result).isEqualTo("light");
        }

        @Test
        @DisplayName("ignores cookies with wrong name")
        void ignoresCookiesWithWrongName() {
            var request = new MockHttpServletRequest();
            request.setCookies(
                new Cookie("other-cookie", "dark"),
                new Cookie("theme", "dark")
            );

            String result = resolver.resolve(request);

            assertThat(result).isEqualTo("light");
        }
    }

    @Nested
    @DisplayName("isValidTheme(String)")
    class IsValidTheme {

        @Test
        @DisplayName("returns true for 'light' theme")
        void returnsTrueForLight() {
            assertThat(resolver.isValidTheme("light")).isTrue();
        }

        @Test
        @DisplayName("returns true for 'dark' theme")
        void returnsTrueForDark() {
            assertThat(resolver.isValidTheme("dark")).isTrue();
        }

        @Test
        @DisplayName("returns false for unregistered theme")
        void returnsFalseForUnregisteredTheme() {
            assertThat(resolver.isValidTheme("ocean")).isFalse();
        }

        @Test
        @DisplayName("returns false for null")
        void returnsFalseForNull() {
            assertThat(resolver.isValidTheme(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("delegation to ThemeProperties")
    class Delegation {

        @Test
        @DisplayName("getDefaultTheme delegates to themeProperties")
        void getDefaultThemeDelegates() {
            assertThat(resolver.getDefaultTheme()).isEqualTo("light");
        }

        @Test
        @DisplayName("getCookieName delegates to themeProperties")
        void getCookieNameDelegates() {
            assertThat(resolver.getCookieName()).isEqualTo("jux-theme");
        }

        @Test
        @DisplayName("getCookieMaxAge delegates to themeProperties")
        void getCookieMaxAgeDelegates() {
            assertThat(resolver.getCookieMaxAge()).isEqualTo(31536000);
        }
    }
}
