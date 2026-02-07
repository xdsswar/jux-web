/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.theme;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible text input field component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a complete form field with a visible label, input control,
 * optional help text, and optional error message. The component follows
 * WCAG guidelines for form input accessibility (3.3.1, 3.3.2) and
 * ensures proper programmatic associations between labels, inputs,
 * descriptions, and error messages.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>A {@code <label>} element linked to the input via {@code for} attribute
 *       (WCAG 3.3.2 -- Labels or Instructions)</li>
 *   <li>{@code aria-required="true"} when the field is required (WCAG 3.3.2)</li>
 *   <li>{@code aria-invalid="true"} when the field has a validation error
 *       (WCAG 3.3.1 -- Error Identification)</li>
 *   <li>{@code aria-describedby} linking to help text and/or error message so
 *       screen readers announce contextual information after the label</li>
 * </ul>
 *
 * <p><b>Important accessibility notes:</b></p>
 * <ul>
 *   <li>The label is always visible -- never hidden or replaced by a placeholder
 *       (WCAG 3.3.2 requires persistent labels)</li>
 *   <li>Placeholder text supplements but does not replace the label</li>
 *   <li>Error messages are programmatically associated via {@code aria-describedby}
 *       so they are announced when the field is focused</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * child(new JuxTextField("email", "Email Address")
 *     .setType("email")
 *     .setRequired(true)
 *     .setPlaceholder("you@example.com")
 *     .setHelpText("We'll never share your email."));
 *
 * // With validation error:
 * child(new JuxTextField("name", "Full Name")
 *     .setRequired(true)
 *     .setInvalid(true)
 *     .setErrorMessage("Name is required."));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/tutorials/forms/">WAI Forms Tutorial</a>
 */
@JuxComponent
public class JuxTextField extends Component {

    /**
     * The {@code name} attribute for the input element.
     *
     * <p>Used as the form parameter key when the form is submitted.
     * This should be a valid, descriptive identifier (e.g. "email",
     * "firstName", "search_query").</p>
     */
    private final String name;

    /**
     * The visible label text for the input field.
     *
     * <p>Always rendered as a visible {@code <label>} element above the input.
     * Per WCAG 3.3.2, every form field must have a persistent, visible label
     * that is not replaced by placeholder text.</p>
     */
    private final String label;

    /**
     * Whether this field is required for form submission.
     *
     * <p>When true, the input receives {@code aria-required="true"} and
     * the HTML {@code required} attribute. Screen readers announce
     * "required" when the user focuses the field.</p>
     *
     * <p>Default: {@code false}</p>
     */
    private boolean required = false;

    /**
     * Whether the current field value is invalid.
     *
     * <p>When true, the input receives {@code aria-invalid="true"} and
     * a visual error styling class. If an {@link #errorMessage} is set,
     * it is displayed below the input and linked via {@code aria-describedby}
     * so screen readers announce the error when the field is focused.</p>
     *
     * <p>Default: {@code false}</p>
     */
    private boolean invalid = false;

    /**
     * The validation error message displayed when the field is invalid.
     *
     * <p>Shown below the input as a red error text and linked to the
     * input via {@code aria-describedby}. Only displayed when
     * {@link #invalid} is true and this value is non-null.</p>
     *
     * <p>Default: {@code null} (no error message)</p>
     */
    private String errorMessage;

    /**
     * Help text displayed below the input field.
     *
     * <p>Provides additional context or instructions for the field
     * (e.g. "Must be at least 8 characters", "We'll never share your email").
     * Linked to the input via {@code aria-describedby} so screen readers
     * announce it after the label and before any error message.</p>
     *
     * <p>Default: {@code null} (no help text)</p>
     */
    private String helpText;

    /**
     * The HTML input type attribute.
     *
     * <p>Common values: "text", "email", "password", "tel", "url", "number",
     * "search", "date". The type affects browser validation, mobile keyboard
     * layout, and autofill behavior. Using the correct type improves
     * accessibility by providing semantic hints to assistive technology
     * and enabling built-in browser features.</p>
     *
     * <p>Default: {@code "text"}</p>
     */
    private String type = "text";

    /**
     * Placeholder text displayed inside the input when it is empty.
     *
     * <p>Provides a hint about the expected format or content. Per WCAG
     * guidelines, the placeholder must not be the only means of labeling
     * the input -- the visible {@link #label} is always present. Placeholder
     * text disappears when the user starts typing, so it is supplementary.</p>
     *
     * <p>Default: {@code null} (no placeholder)</p>
     */
    private String placeholder;

    /**
     * Auto-generated unique ID for the input element.
     *
     * <p>Used by the {@code <label>} element's {@code for} attribute to create
     * a programmatic association between the label and the input. Also used
     * as the base for generating related IDs (help text, error message).</p>
     */
    private final String inputId;

    /**
     * Creates a new text field component with the minimum required attributes.
     *
     * <p>The field starts in a valid, non-required state with no help text,
     * no error, and "text" as the input type. Use the fluent setters to
     * configure optional attributes.</p>
     *
     * @param name  the form parameter name for the input; must not be null
     * @param label the visible label text; must not be null
     * @throws NullPointerException if name or label is null
     */
    public JuxTextField(String name, String label) {
        this.name = Objects.requireNonNull(name, "Field name must not be null");
        this.label = Objects.requireNonNull(label, "Field label must not be null");

        /*
         * Generate a unique ID based on the field name plus a UUID fragment.
         * Using the name as a prefix makes the HTML more readable during
         * development while the UUID suffix ensures uniqueness when the
         * same field name appears multiple times on a page.
         */
        this.inputId = "jux-field-" + name + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ── Fluent setters ───────────────────────────────────────────
    // All setters return `this` to support method chaining:
    //   new JuxTextField("email", "Email").setRequired(true).setType("email")

    /**
     * Sets whether this field is required for form submission.
     *
     * @param required true to mark the field as required
     * @return this component for method chaining
     */
    public JuxTextField setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Sets whether the current field value is invalid.
     *
     * @param invalid true to mark the field as having an invalid value
     * @return this component for method chaining
     */
    public JuxTextField setInvalid(boolean invalid) {
        this.invalid = invalid;
        return this;
    }

    /**
     * Sets the error message displayed when the field is invalid.
     *
     * @param errorMessage the error text, or null to clear
     * @return this component for method chaining
     */
    public JuxTextField setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Sets help text displayed below the input.
     *
     * @param helpText the help text, or null to clear
     * @return this component for method chaining
     */
    public JuxTextField setHelpText(String helpText) {
        this.helpText = helpText;
        return this;
    }

    /**
     * Sets the HTML input type.
     *
     * @param type the input type (e.g. "text", "email", "password", "tel", "url")
     * @return this component for method chaining
     * @throws NullPointerException if type is null
     */
    public JuxTextField setType(String type) {
        this.type = Objects.requireNonNull(type, "Input type must not be null");
        return this;
    }

    /**
     * Sets the placeholder text for the input.
     *
     * @param placeholder the placeholder text, or null to clear
     * @return this component for method chaining
     */
    public JuxTextField setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    /**
     * Builds the text field Element tree with label, input, help text, and error.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <div class="jux-textfield [jux-textfield-error]">
     *   <label for="input-id" class="jux-textfield-label">Email Address</label>
     *   <input type="email" id="input-id" name="email"
     *          [placeholder="..."] [required]
     *          [aria-required="true"] [aria-invalid="true"]
     *          [aria-describedby="help-id error-id"]
     *          class="jux-textfield-input" />
     *   <span id="help-id" class="jux-textfield-help">We'll never share your email.</span>
     *   <span id="error-id" class="jux-textfield-error-message" role="alert">
     *     Email is required.
     *   </span>
     * </div>
     * }</pre>
     *
     * <p>The {@code aria-describedby} attribute is dynamically built to include
     * only the IDs of elements that are actually present (help text and/or
     * error message). This prevents broken ARIA references.</p>
     *
     * @return the text field container Element, never null
     */
    @Override
    public Element render() {
        /* IDs for the supplementary text elements. */
        String helpId = inputId + "-help";
        String errorId = inputId + "-error";

        /*
         * Build the visible label. The for attribute creates a programmatic
         * link to the input so clicking the label focuses the input and
         * screen readers announce the label when the input is focused.
         */
        Element labelElement = label()
                .attr("for", inputId)
                .cls("jux-textfield-label")
                .text(label);

        /*
         * Build the input element with all relevant attributes.
         * Start with the base attributes that are always present.
         */
        Element inputElement = input()
                .attr("type", type)
                .id(inputId)
                .attr("name", name)
                .cls("jux-textfield-input");

        /* Add optional placeholder. */
        if (placeholder != null) {
            inputElement = inputElement.attr("placeholder", placeholder);
        }

        /* Add required attributes if the field is mandatory. */
        if (required) {
            inputElement = inputElement
                    .attr("required", "required")
                    .ariaRequired(true);
        }

        /* Add invalid state if the field has a validation error. */
        if (invalid) {
            inputElement = inputElement.ariaInvalid(true);
        }

        /*
         * Build the aria-describedby value by collecting the IDs of all
         * supplementary text elements that are actually rendered. This
         * ensures screen readers announce help text and error messages
         * in the correct order when the input is focused.
         */
        List<String> describedByIds = new ArrayList<>();
        if (helpText != null) {
            describedByIds.add(helpId);
        }
        if (invalid && errorMessage != null) {
            describedByIds.add(errorId);
        }
        if (!describedByIds.isEmpty()) {
            inputElement = inputElement.ariaDescribedBy(String.join(" ", describedByIds));
        }

        /* Assemble the children: label, input, optional help, optional error. */
        List<Element> children = new ArrayList<>();
        children.add(labelElement);
        children.add(inputElement);

        /* Add help text if configured. */
        if (helpText != null) {
            children.add(
                    span()
                            .id(helpId)
                            .cls("jux-textfield-help")
                            .text(helpText)
            );
        }

        /*
         * Add error message if the field is invalid and has an error message.
         * role="alert" causes screen readers to announce the error immediately
         * when it appears, even if the element is not focused.
         */
        if (invalid && errorMessage != null) {
            children.add(
                    span()
                            .id(errorId)
                            .cls("jux-textfield-error-message")
                            .role("alert")
                            .text(errorMessage)
            );
        }

        /*
         * Wrap everything in a container div. The error modifier class
         * enables theme-level visual styling (red border, etc.) when
         * the field is in an error state.
         */
        Element container = div()
                .cls("jux-textfield")
                .children(children);

        if (invalid) {
            container = container.cls("jux-textfield-error");
        }

        return container;
    }
}
