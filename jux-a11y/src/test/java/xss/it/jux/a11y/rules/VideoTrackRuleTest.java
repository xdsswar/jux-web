package xss.it.jux.a11y.rules;

import org.junit.jupiter.api.Test;
import xss.it.jux.a11y.A11yRules;
import xss.it.jux.a11y.A11ySeverity;
import xss.it.jux.a11y.A11yViolation;
import xss.it.jux.core.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link A11yRules.VideoTrackRule} -- WCAG 1.2.2 (Captions).
 *
 * <p>Verifies that video elements without a {@code <track>} child are flagged
 * as WARNING violations, while videos with a track pass.</p>
 */
class VideoTrackRuleTest {

    private final A11yRules.VideoTrackRule rule = new A11yRules.VideoTrackRule();

    @Test
    void videoWithTrackChild_noViolations() {
        Element element = video().children(
            source().attr("src", "/video.mp4").attr("type", "video/mp4"),
            track().attr("kind", "captions").attr("src", "/captions.vtt").attr("srclang", "en")
        );

        List<A11yViolation> violations = rule.check(element, "video");

        assertThat(violations).isEmpty();
    }

    @Test
    void videoWithoutTrackChild_warningWithCriterion1_2_2() {
        Element element = video().children(
            source().attr("src", "/video.mp4").attr("type", "video/mp4")
        );

        List<A11yViolation> violations = rule.check(element, "video");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("1.2.2");
        assertThat(violations.get(0).rule()).isEqualTo("video-track");
    }

    @Test
    void nonVideoElement_noViolations() {
        Element element = div().text("Not a video");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}
