package xss.it.jux.server.theme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link JuxThemeController}.
 *
 * <p>Uses standalone {@link MockMvc} setup to test the controller in isolation
 * with a mocked {@link JuxThemeResolver} dependency. Verifies HTTP responses,
 * cookie setting, and input validation for the theme switching endpoint.</p>
 */
@DisplayName("JuxThemeController")
class JuxThemeControllerTest {

    private MockMvc mockMvc;
    private JuxThemeResolver themeResolver;

    @BeforeEach
    void setUp() {
        themeResolver = mock(JuxThemeResolver.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new JuxThemeController(themeResolver)).build();
    }

    @Nested
    @DisplayName("POST /api/theme")
    class SetTheme {

        @Test
        @DisplayName("returns 200 with theme name when value is valid")
        void returns200WithThemeNameWhenValid() throws Exception {
            when(themeResolver.isValidTheme("dark")).thenReturn(true);
            when(themeResolver.getCookieName()).thenReturn("jux-theme");
            when(themeResolver.getCookieMaxAge()).thenReturn(31536000);

            mockMvc.perform(post("/api/theme").param("value", "dark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));
        }

        @Test
        @DisplayName("sets theme cookie in response when value is valid")
        void setsCookieInResponseWhenValid() throws Exception {
            when(themeResolver.isValidTheme("dark")).thenReturn(true);
            when(themeResolver.getCookieName()).thenReturn("jux-theme");
            when(themeResolver.getCookieMaxAge()).thenReturn(31536000);

            mockMvc.perform(post("/api/theme").param("value", "dark"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("jux-theme", "dark"))
                .andExpect(cookie().maxAge("jux-theme", 31536000))
                .andExpect(cookie().path("jux-theme", "/"));
        }

        @Test
        @DisplayName("returns 400 with error message when value is invalid")
        void returns400WhenInvalidTheme() throws Exception {
            when(themeResolver.isValidTheme("invalid")).thenReturn(false);

            mockMvc.perform(post("/api/theme").param("value", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid theme: invalid"));
        }

        @Test
        @DisplayName("returns 400 when value parameter is missing")
        void returns400WhenValueMissing() throws Exception {
            mockMvc.perform(post("/api/theme"))
                .andExpect(status().isBadRequest());
        }
    }
}
