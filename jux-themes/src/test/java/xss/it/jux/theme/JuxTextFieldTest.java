package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxTextField} -- accessible text input field component.
 *
 * <p>Validates the rendered label/input association, ARIA attributes for
 * required/invalid states, help text, error messages, and constructor validation.</p>
 */
class JuxTextFieldTest {

    // ── Helper methods ───────────────────────────────────────────

    private Element findByAttribute(Element root, String attr, String value) {
        if (value.equals(root.getAttributes().get(attr))) return root;
        for (Element child : root.getChildren()) {
            Element found = findByAttribute(child, attr, value);
            if (found != null) return found;
        }
        return null;
    }

    private Element findByTag(Element root, String tag) {
        if (tag.equals(root.getTag())) return root;
        for (Element child : root.getChildren()) {
            Element found = findByTag(child, tag);
            if (found != null) return found;
        }
        return null;
    }

    private List<Element> findAllByTag(Element root, String tag) {
        List<Element> results = new ArrayList<>();
        collectByTag(root, tag, results);
        return results;
    }

    private void collectByTag(Element el, String tag, List<Element> results) {
        if (tag.equals(el.getTag())) results.add(el);
        for (Element child : el.getChildren()) collectByTag(child, tag, results);
    }

    // ── Tests ────────────────────────────────────────────────────

    @Test
    void render_producesLabelAndInputStructure() {
        Element rendered = new JuxTextField("email", "Email Address").render();

        Element label = findByTag(rendered, "label");
        Element input = findByTag(rendered, "input");

        assertThat(label).isNotNull();
        assertThat(input).isNotNull();
    }

    @Test
    void render_labelForAttributeMatchesInputId() {
        Element rendered = new JuxTextField("email", "Email Address").render();

        Element label = findByTag(rendered, "label");
        Element input = findByTag(rendered, "input");

        assertThat(label).isNotNull();
        assertThat(input).isNotNull();

        String inputId = input.getAttributes().get("id");
        String labelFor = label.getAttributes().get("for");

        assertThat(labelFor).isEqualTo(inputId);
    }

    @Test
    void render_labelHasCorrectText() {
        Element rendered = new JuxTextField("email", "Email Address").render();

        Element label = findByTag(rendered, "label");
        assertThat(label).isNotNull();
        assertThat(label.getTextContent()).isEqualTo("Email Address");
    }

    @Test
    void render_defaultTypeIsText() {
        Element rendered = new JuxTextField("name", "Full Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("type")).isEqualTo("text");
    }

    @Test
    void render_setTypeChangesInputType() {
        Element rendered = new JuxTextField("email", "Email")
                .setType("email")
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("type")).isEqualTo("email");
    }

    @Test
    void render_inputHasNameAttribute() {
        Element rendered = new JuxTextField("firstName", "First Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("name")).isEqualTo("firstName");
    }

    @Test
    void render_requiredFieldHasAriaRequiredTrue() {
        Element rendered = new JuxTextField("name", "Name")
                .setRequired(true)
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("aria-required")).isEqualTo("true");
    }

    @Test
    void render_requiredFieldHasHtmlRequiredAttribute() {
        Element rendered = new JuxTextField("name", "Name")
                .setRequired(true)
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("required")).isEqualTo("required");
    }

    @Test
    void render_nonRequiredFieldDoesNotHaveAriaRequired() {
        Element rendered = new JuxTextField("name", "Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes()).doesNotContainKey("aria-required");
    }

    @Test
    void render_helpTextCreatesDescribedByLink() {
        Element rendered = new JuxTextField("email", "Email")
                .setHelpText("We'll never share your email.")
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes()).containsKey("aria-describedby");

        String describedBy = input.getAttributes().get("aria-describedby");
        // The help element with this ID should exist
        Element helpElement = findByAttribute(rendered, "id", describedBy);
        assertThat(helpElement).isNotNull();
        assertThat(helpElement.getTextContent()).isEqualTo("We'll never share your email.");
    }

    @Test
    void render_helpTextHasCorrectCssClass() {
        Element rendered = new JuxTextField("email", "Email")
                .setHelpText("Help text here")
                .render();

        List<Element> spans = findAllByTag(rendered, "span");
        Element helpSpan = spans.stream()
                .filter(s -> s.getCssClasses().contains("jux-textfield-help"))
                .findFirst()
                .orElse(null);

        assertThat(helpSpan).isNotNull();
        assertThat(helpSpan.getTextContent()).isEqualTo("Help text here");
    }

    @Test
    void render_invalidFieldHasAriaInvalidTrue() {
        Element rendered = new JuxTextField("name", "Name")
                .setInvalid(true)
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("aria-invalid")).isEqualTo("true");
    }

    @Test
    void render_invalidWithErrorMessageHasDescribedByPointingToError() {
        Element rendered = new JuxTextField("name", "Name")
                .setInvalid(true)
                .setErrorMessage("Name is required.")
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();

        String describedBy = input.getAttributes().get("aria-describedby");
        assertThat(describedBy).isNotNull();

        // The error element with this ID should exist
        Element errorElement = findByAttribute(rendered, "id", describedBy);
        assertThat(errorElement).isNotNull();
        assertThat(errorElement.getTextContent()).isEqualTo("Name is required.");
    }

    @Test
    void render_errorMessageHasRoleAlert() {
        Element rendered = new JuxTextField("name", "Name")
                .setInvalid(true)
                .setErrorMessage("Name is required.")
                .render();

        List<Element> spans = findAllByTag(rendered, "span");
        Element errorSpan = spans.stream()
                .filter(s -> s.getCssClasses().contains("jux-textfield-error-message"))
                .findFirst()
                .orElse(null);

        assertThat(errorSpan).isNotNull();
        assertThat(errorSpan.getAttributes().get("role")).isEqualTo("alert");
    }

    @Test
    void render_invalidWithBothHelpAndErrorHasCorrectDescribedBy() {
        Element rendered = new JuxTextField("email", "Email")
                .setHelpText("Enter your email")
                .setInvalid(true)
                .setErrorMessage("Invalid email format")
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();

        String describedBy = input.getAttributes().get("aria-describedby");
        assertThat(describedBy).isNotNull();

        // Should contain both help ID and error ID (space-separated)
        String[] ids = describedBy.split(" ");
        assertThat(ids).hasSize(2);

        // Both referenced elements should exist in the tree
        for (String id : ids) {
            Element ref = findByAttribute(rendered, "id", id);
            assertThat(ref).as("Element with id '%s' should exist", id).isNotNull();
        }
    }

    @Test
    void render_noHelpAndNotInvalid_noAriaDescribedBy() {
        Element rendered = new JuxTextField("name", "Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes()).doesNotContainKey("aria-describedby");
    }

    @Test
    void render_errorMessageNotShownWhenNotInvalid() {
        Element rendered = new JuxTextField("name", "Name")
                .setErrorMessage("This is an error")
                .render();

        // Error message should not be rendered if invalid is false
        List<Element> spans = findAllByTag(rendered, "span");
        boolean hasErrorSpan = spans.stream()
                .anyMatch(s -> s.getCssClasses().contains("jux-textfield-error-message"));
        assertThat(hasErrorSpan).isFalse();
    }

    @Test
    void render_invalidContainerHasErrorClass() {
        Element rendered = new JuxTextField("name", "Name")
                .setInvalid(true)
                .render();

        assertThat(rendered.getCssClasses()).contains("jux-textfield-error");
    }

    @Test
    void render_validContainerDoesNotHaveErrorClass() {
        Element rendered = new JuxTextField("name", "Name").render();

        assertThat(rendered.getCssClasses()).doesNotContain("jux-textfield-error");
    }

    @Test
    void render_placeholderIsSetWhenConfigured() {
        Element rendered = new JuxTextField("email", "Email")
                .setPlaceholder("you@example.com")
                .render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("placeholder")).isEqualTo("you@example.com");
    }

    @Test
    void render_noPlaceholderByDefault() {
        Element rendered = new JuxTextField("name", "Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes()).doesNotContainKey("placeholder");
    }

    @Test
    void render_containerHasTextfieldCssClass() {
        Element rendered = new JuxTextField("name", "Name").render();

        assertThat(rendered.getTag()).isEqualTo("div");
        assertThat(rendered.getCssClasses()).contains("jux-textfield");
    }

    @Test
    void render_inputHasTextfieldInputCssClass() {
        Element rendered = new JuxTextField("name", "Name").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getCssClasses()).contains("jux-textfield-input");
    }

    @Test
    void render_labelHasTextfieldLabelCssClass() {
        Element rendered = new JuxTextField("name", "Name").render();

        Element label = findByTag(rendered, "label");
        assertThat(label).isNotNull();
        assertThat(label.getCssClasses()).contains("jux-textfield-label");
    }

    @Test
    void render_inputIdContainsFieldName() {
        Element rendered = new JuxTextField("email", "Email").render();

        Element input = findByTag(rendered, "input");
        assertThat(input).isNotNull();
        assertThat(input.getAttributes().get("id")).contains("email");
    }

    @Test
    void constructor_nullNameThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxTextField(null, "Label"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullLabelThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxTextField("name", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void setType_nullThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxTextField("name", "Name").setType(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_fluentSettersReturnSameInstance() {
        JuxTextField field = new JuxTextField("name", "Name");

        JuxTextField result = field
                .setRequired(true)
                .setInvalid(false)
                .setType("text")
                .setPlaceholder("placeholder")
                .setHelpText("help")
                .setErrorMessage("error");

        assertThat(result).isSameAs(field);
    }
}
