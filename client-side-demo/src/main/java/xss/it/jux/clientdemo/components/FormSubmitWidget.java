/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.json.JSON;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleStringProperty;

import static xss.it.jux.core.Elements.*;

/**
 * Interactive form submission widget demonstrating API interaction patterns in JUX.
 *
 * <p>This component builds a fully functional contact form with client-side validation,
 * loading states, and success feedback. It showcases how reactive properties from the
 * {@code jux-reactive} module can be used alongside {@code @State} fields to model
 * complex form interactions entirely in Java -- no JavaScript, no HTML templates.</p>
 *
 * <h2>Reactive Properties Demonstrated</h2>
 * <ul>
 *   <li><b>{@link SimpleStringProperty}</b> -- used to model each form field's current
 *       text value and its associated validation error message. The reactive property
 *       system allows listeners to observe changes, enabling future features like
 *       real-time validation as the user types.</li>
 *   <li><b>{@link SimpleBooleanProperty}</b> -- tracks the binary submission lifecycle
 *       states: whether the form is currently being submitted ({@code submittingProp})
 *       and whether submission has completed successfully ({@code submittedProp}).
 *       Boolean properties integrate with conditional rendering to swap between the
 *       form view and the success confirmation view.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for form fields</b> -- each form input is backed by a reactive
 *       {@code @State} field that updates on every keystroke via the {@code "input"} event.
 *       Changes trigger re-renders that reflect the current value in the input element.</li>
 *   <li><b>Client-side validation</b> -- the {@link #validateForm()} method checks all
 *       fields against their validation rules (minimum length, email format) and populates
 *       error state fields. Invalid fields are visually marked with red borders and
 *       error messages are displayed below each input.</li>
 *   <li><b>Loading state management</b> -- the submit button transitions between "Send Message"
 *       and "Sending..." states, with the button disabled during submission to prevent
 *       duplicate requests.</li>
 *   <li><b>Success state transition</b> -- after successful submission, the entire form is
 *       replaced with a success confirmation card featuring a green checkmark, a thank-you
 *       message, and a "Send Another" button that resets all state.</li>
 * </ul>
 *
 * <h2>API Interaction Pattern</h2>
 * <p>The submit handler includes detailed comments describing how the form data would be
 * sent to a REST endpoint ({@code POST /api/contact}) using {@code XMLHttpRequest} once
 * TeaVM client-side compilation is wired up. The pattern demonstrates:</p>
 * <ul>
 *   <li>Constructing a JSON payload from form field values</li>
 *   <li>Setting appropriate HTTP headers ({@code Content-Type: application/json})</li>
 *   <li>Handling success (200 response) and error (non-200) responses</li>
 *   <li>Managing loading state transitions around the async operation</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- every {@code <input>} and {@code <textarea>}
 *       has an associated {@code <label>} element linked via {@code for}/{@code id} pairing.
 *       This ensures screen readers announce the field purpose when focused.</li>
 *   <li><b>3.3.1 Error Identification</b> -- validation errors are displayed as text below
 *       each invalid field, with {@code aria-invalid="true"} on the field and
 *       {@code aria-describedby} pointing to the error message element.</li>
 *   <li><b>3.3.2 Labels or Instructions</b> -- all fields have visible labels, and required
 *       fields are marked with {@code aria-required="true"} to communicate mandatory status
 *       to assistive technology.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the success confirmation is announced via an
 *       {@code aria-live="polite"} region, ensuring screen reader users are informed of
 *       the submission result without focus being moved.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The initial server-rendered state displays the empty form with all fields in their
 * default (empty) state, no validation errors, and the "Send Message" button enabled.
 * Event handlers are serialized as {@code data-jux-event} attributes and will become
 * active once client-side hydration via TeaVM is enabled.</p>
 *
 * @see SimpleStringProperty
 * @see SimpleBooleanProperty
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class FormSubmitWidget extends Component {

    // ─── Reactive Properties ─────────────────────────────────────────────────────
    // These properties provide observable semantics from the jux-reactive module.
    // Listeners can be attached to react to value changes, enabling features like
    // real-time validation or binding form values to external state managers.

    /**
     * Reactive property tracking the current value of the "Name" input field.
     *
     * <p>This {@link SimpleStringProperty} wraps the name text so that external
     * observers (validators, analytics, or bound UI elements) can react to changes.
     * The property is initialized with an empty string representing the blank state
     * of a fresh form.</p>
     */
    private final SimpleStringProperty nameProp = new SimpleStringProperty(this, "name", "");

    /**
     * Reactive property tracking the current value of the "Email" input field.
     *
     * <p>The email property uses {@link SimpleStringProperty} to allow observation
     * of the email field content. In a production application, a change listener
     * could trigger real-time email format validation as the user types.</p>
     */
    private final SimpleStringProperty emailProp = new SimpleStringProperty(this, "email", "");

    /**
     * Reactive property tracking the current value of the "Message" textarea field.
     *
     * <p>Like the other field properties, this enables reactive observation of
     * the message content. The property fires change events on every keystroke
     * captured by the {@code "input"} event handler.</p>
     */
    private final SimpleStringProperty messageProp = new SimpleStringProperty(this, "message", "");

    /**
     * Reactive boolean property indicating whether a form submission is in progress.
     *
     * <p>When {@code true}, the submit button displays "Sending..." and is disabled
     * to prevent duplicate submissions. Transitions from {@code false} to {@code true}
     * when the user clicks submit, and back to {@code false} when the API call
     * completes (or after a simulated delay in the demo).</p>
     */
    private final SimpleBooleanProperty submittingProp = new SimpleBooleanProperty(this, "submitting", false);

    /**
     * Reactive boolean property indicating whether the form was successfully submitted.
     *
     * <p>When {@code true}, the form is replaced with a success confirmation card.
     * This property is set to {@code true} after the simulated API call completes
     * successfully. The "Send Another" button resets it to {@code false}.</p>
     */
    private final SimpleBooleanProperty submittedProp = new SimpleBooleanProperty(this, "submitted", false);

    // ─── @State Fields ───────────────────────────────────────────────────────────
    // These fields drive the JUX reactivity system: any mutation triggers a
    // re-render with virtual DOM diffing and patching.

    /**
     * Current text content of the "Name" input field.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler.
     * Validated on submit: must be at least 2 characters long.</p>
     */
    @State
    private String nameValue = "";

    /**
     * Current text content of the "Email" input field.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler.
     * Validated on submit: must contain an {@code @} character to pass
     * basic email format validation.</p>
     */
    @State
    private String emailValue = "";

    /**
     * Current text content of the "Message" textarea field.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler.
     * Validated on submit: must be at least 10 characters long to encourage
     * meaningful messages.</p>
     */
    @State
    private String messageValue = "";

    /**
     * Whether the form is currently being submitted (API call in flight).
     *
     * <p>When {@code true}, the submit button shows a loading indicator text
     * and is visually and programmatically disabled. This prevents the user
     * from triggering multiple simultaneous submissions.</p>
     */
    @State
    private boolean submitting = false;

    /**
     * Whether the form submission completed successfully.
     *
     * <p>When {@code true}, the form card is replaced with a success confirmation
     * card showing a green checkmark and a thank-you message. The user can click
     * "Send Another" to reset this flag and return to the empty form.</p>
     */
    @State
    private boolean submitted = false;

    /**
     * Validation error message for the "Name" field.
     *
     * <p>An empty string indicates no error. When non-empty, the error text is
     * displayed below the name input in red, and the input receives
     * {@code aria-invalid="true"} and a red border to indicate the problem.</p>
     */
    @State
    private String nameError = "";

    /**
     * Validation error message for the "Email" field.
     *
     * <p>An empty string indicates no error. When non-empty, the error text is
     * displayed below the email input in red, and the input receives
     * {@code aria-invalid="true"} and a red border.</p>
     */
    @State
    private String emailError = "";

    /**
     * Validation error message for the "Message" field.
     *
     * <p>An empty string indicates no error. When non-empty, the error text is
     * displayed below the message textarea in red, and the textarea receives
     * {@code aria-invalid="true"} and a red border.</p>
     */
    @State
    private String messageError = "";

    /**
     * Builds the virtual DOM tree for the form submission widget.
     *
     * <p>This method returns one of two views depending on the current state:</p>
     * <ol>
     *   <li><b>Form view</b> (when {@code submitted == false}): a styled card containing
     *       the contact form with name, email, and message fields, validation error
     *       displays, and a submit button with loading state.</li>
     *   <li><b>Success view</b> (when {@code submitted == true}): a confirmation card
     *       with a green checkmark icon, success heading, descriptive text, and a
     *       "Send Another" button to reset the form.</li>
     * </ol>
     *
     * @return the root element of the form widget, never null
     */
    @Override
    public Element render() {
        /*
         * Branch on the submitted state to determine which view to render.
         * When the form has been successfully submitted, show the success
         * confirmation; otherwise, show the interactive form.
         */
        if (submitted) {
            return renderSuccessView();
        }
        return renderFormView();
    }

    /**
     * Renders the success confirmation view shown after a successful form submission.
     *
     * <p>This view replaces the form and consists of:</p>
     * <ul>
     *   <li>A large green checkmark icon centered at the top</li>
     *   <li>A "Message sent!" heading</li>
     *   <li>Supporting text: "We'll get back to you soon."</li>
     *   <li>A "Send Another" button that resets all form state</li>
     * </ul>
     *
     * <p>The entire success view is wrapped in an {@code aria-live="polite"} region
     * so that screen readers automatically announce the confirmation when it appears,
     * without requiring the user to navigate to it.</p>
     *
     * @return the success confirmation element tree
     */
    private Element renderSuccessView() {
        return div().cls("bg-gray-800", "rounded-2xl", "p-8", "text-center")
                .ariaLive("polite")
                .children(

                        /* ── Green Checkmark Icon ────────────────────────────────────
                         * A large green circle with a checkmark character, serving as
                         * a visual success indicator. Hidden from assistive technology
                         * because the heading text below conveys the same meaning.
                         */
                        div().cls("mb-6").children(
                                div().cls("w-20", "h-20", "rounded-full", "bg-emerald-500/20",
                                                "flex", "items-center", "justify-center", "mx-auto")
                                        .ariaHidden(true)
                                        .children(
                                                span().cls("text-4xl", "text-emerald-400").text("\u2713")
                                        )
                        ),

                        /* ── Success Heading ─────────────────────────────────────────
                         * A bold heading announcing the successful submission. Uses h2
                         * to maintain proper heading hierarchy within the page.
                         */
                        h2().cls("text-2xl", "font-bold", "text-white", "mb-2")
                                .text("Message sent!"),

                        /* ── Supporting Text ─────────────────────────────────────────
                         * A brief follow-up message reassuring the user that their
                         * submission was received and will be addressed.
                         */
                        p().cls("text-gray-400", "mb-8")
                                .text("We'll get back to you soon."),

                        /* ── Send Another Button ─────────────────────────────────────
                         * Resets all form state fields to their initial values, clearing
                         * the submitted flag to return to the form view. This allows
                         * the user to submit another message without reloading the page.
                         */
                        button().attr("type", "button")
                                .cls("px-6", "py-3", "bg-violet-600", "text-white",
                                        "rounded-lg", "font-medium",
                                        "hover:bg-violet-500", "transition-colors",
                                        "focus:ring-2", "focus:ring-violet-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .aria("label", "Send another message")
                                .on("click", e -> resetForm())
                                .text("Send Another")
                );
    }

    /**
     * Renders the main form view with all input fields, validation feedback, and submit button.
     *
     * <p>The form is structured as a styled card with:</p>
     * <ul>
     *   <li>A heading and description at the top</li>
     *   <li>Three form fields (name, email, message) each with label, input, and error display</li>
     *   <li>A submit button with loading state</li>
     * </ul>
     *
     * <p>The form uses {@code role="form"} and {@code aria-label} to identify itself as
     * a contact form to assistive technology. Each field group uses proper label-input
     * pairing via {@code for}/{@code id} attributes.</p>
     *
     * @return the form view element tree
     */
    private Element renderFormView() {
        return div().cls("bg-gray-800", "rounded-2xl", "p-8").children(

                /* ── Form Header ─────────────────────────────────────────────
                 * A heading and brief description explaining the form's purpose.
                 */
                div().cls("mb-6").children(
                        h2().cls("text-xl", "font-bold", "text-white", "mb-1")
                                .text("Contact Us"),
                        p().cls("text-sm", "text-gray-400")
                                .text("Fill out the form below and we'll get back to you.")
                ),

                /* ── Form Element ────────────────────────────────────────────
                 * The semantic <form> container. The role and aria-label provide
                 * additional context for screen readers. The form does not have
                 * a traditional action/method because submission is handled via
                 * JavaScript (XMLHttpRequest) on the client side.
                 */
                form().role("form")
                        .aria("label", "Contact form")
                        .cls("space-y-5")
                        .on("submit", e -> {
                            /* Prevent the default browser form submission behaviour.
                             * The form is submitted programmatically via the click
                             * handler on the submit button. */
                            e.preventDefault();
                        })
                        .children(

                                /* ── Name Field Group ────────────────────────────────
                                 * Label, text input, and conditional error message for
                                 * the user's name. Required field with 2-char minimum.
                                 */
                                buildFieldGroup(
                                        "form-name",
                                        "Name",
                                        "text",
                                        "Your full name",
                                        nameValue,
                                        nameError,
                                        "form-name-error",
                                        true
                                ),

                                /* ── Email Field Group ───────────────────────────────
                                 * Label, email input, and conditional error message for
                                 * the user's email address. Required field that must
                                 * contain an @ character.
                                 */
                                buildFieldGroup(
                                        "form-email",
                                        "Email",
                                        "email",
                                        "you@example.com",
                                        emailValue,
                                        emailError,
                                        "form-email-error",
                                        true
                                ),

                                /* ── Message Field Group ─────────────────────────────
                                 * Label, textarea, and conditional error message for
                                 * the user's message. Required with 10-char minimum.
                                 */
                                buildMessageFieldGroup(),

                                /* ── Submit Button ───────────────────────────────────
                                 * The submit button changes its text and style based on
                                 * the submitting state. When submitting, it shows
                                 * "Sending..." in a muted style and is disabled.
                                 */
                                button().attr("type", "button")
                                        .cls("w-full", "py-3", "rounded-lg", "font-medium",
                                                "transition-colors",
                                                "focus:ring-2", "focus:ring-offset-2",
                                                "focus:ring-offset-gray-800")
                                        .cls(submitting
                                                ? "bg-gray-600 text-gray-400 cursor-not-allowed focus:ring-gray-500"
                                                : "bg-violet-600 text-white hover:bg-violet-500 focus:ring-violet-500")
                                        .attr("aria-disabled", submitting ? "true" : "false")
                                        .aria("label", submitting ? "Sending message" : "Send message")
                                        .on("click", e -> handleSubmit())
                                        .text(submitting ? "Sending..." : "Send Message")
                        )
        );
    }

    /**
     * Builds a form field group consisting of a label, text/email input, and error display.
     *
     * <p>This method generates the standard field layout used for the name and email fields.
     * Each group contains:</p>
     * <ul>
     *   <li>A {@code <label>} element linked to the input via {@code for}/{@code id}</li>
     *   <li>An {@code <input>} element with appropriate type, placeholder, and ARIA attributes</li>
     *   <li>A conditional error message {@code <span>} that appears only when the error
     *       string is non-empty, with {@code role="alert"} for screen reader announcement</li>
     * </ul>
     *
     * @param fieldId     the HTML id for the input element, also used in the label's {@code for} attribute
     * @param labelText   the visible label text displayed above the input
     * @param inputType   the HTML input type attribute (e.g. "text", "email")
     * @param placeholder the placeholder hint text displayed inside the empty input
     * @param value       the current field value from the corresponding {@code @State} field
     * @param error       the current error message (empty string means no error)
     * @param errorId     the HTML id for the error message element, used in {@code aria-describedby}
     * @param required    whether this field is required for form submission
     * @return the complete field group element tree
     */
    private Element buildFieldGroup(String fieldId, String labelText, String inputType,
                                    String placeholder, String value, String error,
                                    String errorId, boolean required) {
        /*
         * Determine whether the field is currently in an error state.
         * This controls the border color (red for error, gray for normal)
         * and the presence of aria-invalid on the input.
         */
        boolean hasError = !error.isEmpty();

        return div().children(

                /* ── Label ───────────────────────────────────────────────────
                 * A visible label linked to the input via the for attribute.
                 * Required fields get an asterisk after the label text.
                 */
                label().attr("for", fieldId)
                        .cls("block", "text-sm", "font-medium", "text-gray-300", "mb-1")
                        .text(labelText + (required ? " *" : "")),

                /* ── Input ───────────────────────────────────────────────────
                 * The text/email input field with dynamic styling based on
                 * the error state. When an error is present, the border turns
                 * red and aria-invalid is set to true. The aria-describedby
                 * attribute points to the error message element when visible.
                 */
                input()
                        .id(fieldId)
                        .attr("type", inputType)
                        .attr("placeholder", placeholder)
                        .attr("value", value)
                        .ariaRequired(required)
                        .ariaInvalid(hasError)
                        .ariaDescribedBy(hasError ? errorId : "")
                        .cls("w-full", "bg-gray-700", "text-white",
                                "rounded-lg", "px-4", "py-2.5",
                                "placeholder-gray-500", "border",
                                "focus:ring-1", "focus:outline-none",
                                "transition-colors")
                        .cls(hasError
                                ? "border-rose-500 focus:border-rose-500 focus:ring-rose-500"
                                : "border-gray-600 focus:border-violet-500 focus:ring-violet-500")
                        .on("input", e -> {
                            /* Update the corresponding @State field based on the input id.
                             * Each keystroke updates the field value and syncs the reactive
                             * property. The field-specific routing is done by matching the
                             * fieldId parameter captured in this lambda's closure. */
                            String newValue = e.getValue();
                            if ("form-name".equals(fieldId)) {
                                nameValue = newValue;
                                nameProp.set(newValue);
                                /* Clear the error when the user starts typing again,
                                 * providing immediate feedback that their correction
                                 * is acknowledged. */
                                if (!nameError.isEmpty()) {
                                    nameError = "";
                                }
                            } else if ("form-email".equals(fieldId)) {
                                emailValue = newValue;
                                emailProp.set(newValue);
                                if (!emailError.isEmpty()) {
                                    emailError = "";
                                }
                            }
                        }),

                /* ── Error Message ────────────────────────────────────────────
                 * Conditionally rendered error text below the input. Only visible
                 * when the error string is non-empty. Uses role="alert" so screen
                 * readers immediately announce the error when it appears.
                 */
                hasError
                        ? span().id(errorId)
                                .cls("block", "text-sm", "text-rose-400", "mt-1")
                                .role("alert")
                                .text(error)
                        : span().id(errorId).ariaHidden(true)
        );
    }

    /**
     * Builds the message textarea field group with label, textarea, and error display.
     *
     * <p>This is separated from {@link #buildFieldGroup} because the message field uses
     * a {@code <textarea>} element instead of an {@code <input>}, requiring slightly
     * different element construction. The layout and ARIA patterns are identical.</p>
     *
     * @return the message field group element tree
     */
    private Element buildMessageFieldGroup() {
        /*
         * Determine the error state for the message field to control
         * border color and aria-invalid attribute.
         */
        boolean hasError = !messageError.isEmpty();

        return div().children(

                /* ── Label for the message textarea ──────────────────────────
                 * Linked to the textarea via for/id. The asterisk indicates
                 * this is a required field.
                 */
                label().attr("for", "form-message")
                        .cls("block", "text-sm", "font-medium", "text-gray-300", "mb-1")
                        .text("Message *"),

                /* ── Textarea ────────────────────────────────────────────────
                 * A multi-line text input for the user's message content.
                 * The rows attribute sets the initial visible height to 4 lines.
                 * Styling and ARIA attributes follow the same pattern as the
                 * text/email inputs above.
                 */
                textarea()
                        .id("form-message")
                        .attr("placeholder", "Your message (at least 10 characters)...")
                        .attr("rows", "4")
                        .ariaRequired(true)
                        .ariaInvalid(hasError)
                        .ariaDescribedBy(hasError ? "form-message-error" : "")
                        .cls("w-full", "bg-gray-700", "text-white",
                                "rounded-lg", "px-4", "py-2.5",
                                "placeholder-gray-500", "border",
                                "focus:ring-1", "focus:outline-none",
                                "transition-colors", "resize-y")
                        .cls(hasError
                                ? "border-rose-500 focus:border-rose-500 focus:ring-rose-500"
                                : "border-gray-600 focus:border-violet-500 focus:ring-violet-500")
                        .text(messageValue)
                        .on("input", e -> {
                            /* Update the message state and reactive property on every
                             * keystroke. Clear any existing validation error to provide
                             * immediate positive feedback as the user corrects the issue. */
                            messageValue = e.getValue();
                            messageProp.set(e.getValue());
                            if (!messageError.isEmpty()) {
                                messageError = "";
                            }
                        }),

                /* ── Error Message for message field ─────────────────────────
                 * Conditionally rendered error text. Same pattern as the other
                 * field groups: visible when error is non-empty, with role="alert"
                 * for screen reader announcement.
                 */
                hasError
                        ? span().id("form-message-error")
                                .cls("block", "text-sm", "text-rose-400", "mt-1")
                                .role("alert")
                                .text(messageError)
                        : span().id("form-message-error").ariaHidden(true)
        );
    }

    /**
     * Validates all form fields and populates error state fields.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li><b>Name:</b> required, minimum 2 characters</li>
     *   <li><b>Email:</b> required, must contain an {@code @} character</li>
     *   <li><b>Message:</b> required, minimum 10 characters</li>
     * </ul>
     *
     * <p>Each invalid field gets its corresponding error state field set to a
     * human-readable error message. Valid fields have their error state cleared.
     * The method returns {@code true} only if all fields pass validation.</p>
     *
     * @return {@code true} if all fields are valid, {@code false} if any field has errors
     */
    private boolean validateForm() {
        /* Assume validity; each failing check sets this to false. */
        boolean valid = true;

        /* ── Name Validation ─────────────────────────────────────────────────
         * The name must be at least 2 characters long. This prevents single-
         * character submissions that are likely accidental while allowing
         * short names (e.g. "Jo", "Li") from various cultures.
         */
        if (nameValue.trim().length() < 2) {
            nameError = "Name must be at least 2 characters.";
            valid = false;
        } else {
            nameError = "";
        }

        /* ── Email Validation ────────────────────────────────────────────────
         * A simple check for the presence of an @ character. This is not a
         * full RFC 5322 email validation but catches the most common mistake
         * of entering a non-email string. A more robust check would use a
         * regex pattern, but for a demo, the @ check is sufficient.
         */
        if (!emailValue.contains("@")) {
            emailError = "Please enter a valid email address.";
            valid = false;
        } else {
            emailError = "";
        }

        /* ── Message Validation ──────────────────────────────────────────────
         * The message must be at least 10 characters long to encourage
         * meaningful communication and discourage empty or trivial submissions
         * like "hi" or "test".
         */
        if (messageValue.trim().length() < 10) {
            messageError = "Message must be at least 10 characters.";
            valid = false;
        } else {
            messageError = "";
        }

        return valid;
    }

    /**
     * Handles the form submission process: validates, sets loading state, and simulates an API call.
     *
     * <p>The submission flow is:</p>
     * <ol>
     *   <li>Skip if already submitting (prevents duplicate submissions)</li>
     *   <li>Run client-side validation via {@link #validateForm()}</li>
     *   <li>If valid: set {@code submitting = true} to show loading state</li>
     *   <li>Simulate an API call (or make a real one once TeaVM is wired up)</li>
     *   <li>On success: set {@code submitted = true} to show the success view</li>
     *   <li>On completion: set {@code submitting = false} to re-enable the button</li>
     * </ol>
     */
    private void handleSubmit() {
        /* Guard: prevent duplicate submissions while one is in flight. */
        if (submitting) {
            return;
        }

        /* Run validation on all fields. If any field fails, the error
         * state is populated and the form re-renders with error messages
         * visible. Submission is aborted. */
        if (!validateForm()) {
            return;
        }

        /* Set the loading state. This triggers a re-render that changes
         * the submit button text to "Sending..." and disables it. */
        submitting = true;
        submittingProp.set(true);

        /* Build JSON payload and POST to /api/contact via XMLHttpRequest. */
        String payload = buildContactPayload(nameValue, emailValue, messageValue);

        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open("POST", "/api/contact");
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            if (xhr.getStatus() == 200) {
                /* Success: transition to the success confirmation view. */
                submitted = true;
                submittedProp.set(true);
            } else if (xhr.getStatus() == 400) {
                /* Validation error: parse server-side error messages. */
                ContactResponse resp =
                        (ContactResponse) JSON.parse(xhr.getResponseText());
                ContactErrors errors = resp.getErrors();
                if (errors != null) {
                    String sName = nullToEmpty(errors.getName());
                    String sEmail = nullToEmpty(errors.getEmail());
                    String sMessage = nullToEmpty(errors.getMessage());
                    if (!sName.isEmpty()) nameError = sName;
                    if (!sEmail.isEmpty()) emailError = sEmail;
                    if (!sMessage.isEmpty()) messageError = sMessage;
                }
            } else {
                messageError = "Failed to send. Please try again.";
            }

            submitting = false;
            submittingProp.set(false);
            ClientMain.getStateManager().notifyStateChange(this);
        });
        xhr.send(payload);
    }

    // ── TeaVM Overlay Types for /api/contact JSON response ─────────────────────

    /** Typed overlay for the {@code POST /api/contact} response body. */
    interface ContactResponse extends JSObject {
        @JSProperty("success") boolean isSuccess();
        @JSProperty String getMessage();
        @JSProperty ContactErrors getErrors();
    }

    /** Typed overlay for validation error messages in a 400 response. */
    interface ContactErrors extends JSObject {
        @JSProperty String getName();
        @JSProperty String getEmail();
        @JSProperty String getMessage();
    }

    /**
     * Builds a JSON payload string for the contact form submission.
     *
     * <p>Uses {@code @JSBody} to delegate to the browser's {@code JSON.stringify},
     * which handles all escaping correctly.</p>
     */
    @JSBody(params = {"name", "email", "message"}, script =
            "return JSON.stringify({name: name, email: email, message: message});")
    private static native String buildContactPayload(String name, String email, String message);

    /** Returns the string itself if non-null, otherwise empty string. */
    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    /**
     * Resets all form state fields to their initial empty/default values.
     *
     * <p>Called when the user clicks "Send Another" from the success view.
     * All text fields are cleared, all error messages are removed, and the
     * submitted/submitting flags are reset. The reactive properties are also
     * synchronized to maintain consistency between the {@code @State} fields
     * and the observable property layer.</p>
     */
    private void resetForm() {
        /* Clear all text field values. */
        nameValue = "";
        emailValue = "";
        messageValue = "";

        /* Clear all validation error messages. */
        nameError = "";
        emailError = "";
        messageError = "";

        /* Reset submission state flags. */
        submitting = false;
        submitted = false;

        /* Synchronize the reactive properties with the cleared state. */
        nameProp.set("");
        emailProp.set("");
        messageProp.set("");
        submittingProp.set(false);
        submittedProp.set(false);
    }
}
