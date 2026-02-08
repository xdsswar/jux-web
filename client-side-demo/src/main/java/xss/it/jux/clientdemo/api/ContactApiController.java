/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * REST API controller for contact form submissions.
 *
 * <p>This controller handles the submission of contact form data, performing
 * server-side validation on the submitted fields and returning structured
 * success or error responses. It demonstrates a typical form-handling pattern
 * where a JUX client-side component collects user input, serializes it as
 * JSON, and POSTs it to this endpoint via {@code fetch()}.</p>
 *
 * <p>In this demo, the form submission is simulated — validated data is
 * acknowledged with a success message but not persisted to a database or
 * sent as an email. In a production application, the validated data would
 * be saved to a database, forwarded to an email service (SendGrid, SES),
 * or pushed to a CRM system (Salesforce, HubSpot).</p>
 *
 * <h3>Validation Rules:</h3>
 * <ul>
 *   <li><b>name</b> — required, must not be blank after trimming</li>
 *   <li><b>email</b> — required, must match a basic email format pattern</li>
 *   <li><b>message</b> — required, must be at least 10 characters long</li>
 * </ul>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code POST /api/contact} — submit a contact form</li>
 * </ul>
 *
 * <h3>Example request:</h3>
 * <pre>{@code
 * POST /api/contact
 * Content-Type: application/json
 *
 * {
 *   "name": "Jane Doe",
 *   "email": "jane@example.com",
 *   "message": "I'd like to learn more about your services."
 * }
 * }</pre>
 *
 * @see UsersApiController
 * @see QuotesApiController
 * @see NotificationsApiController
 * @see StatsApiController
 */
@RestController
@RequestMapping("/api/contact")
public class ContactApiController {

    /**
     * Compiled regex pattern for basic email address validation.
     *
     * <p>This pattern validates that the email string contains:</p>
     * <ol>
     *   <li>One or more characters before the {@code @} symbol (local part)</li>
     *   <li>An {@code @} symbol</li>
     *   <li>One or more characters after {@code @} containing at least one dot (domain part)</li>
     * </ol>
     *
     * <p>This is intentionally a simple pattern suitable for UI-level validation.
     * It does not attempt to fully validate RFC 5322 compliance, as the only
     * reliable way to verify an email address is to send a confirmation message
     * to it. The pattern catches obvious formatting errors like missing {@code @},
     * missing domain, or missing TLD.</p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Minimum required length for the message field, in characters.
     *
     * <p>Messages shorter than this threshold are rejected to prevent
     * trivially short or accidental submissions. The value of 10 allows
     * short but meaningful messages like "Need help!" while rejecting
     * single-word or blank submissions.</p>
     */
    private static final int MIN_MESSAGE_LENGTH = 10;

    /**
     * Processes a contact form submission.
     *
     * <p>Accepts a JSON body containing {@code name}, {@code email}, and
     * {@code message} fields. Each field is validated according to the rules
     * described below. If all validations pass, a success response is returned.
     * If one or more fields fail validation, an error response is returned
     * with a map of field-specific error messages.</p>
     *
     * <h4>Validation rules:</h4>
     * <ul>
     *   <li><b>name</b> — must not be null, empty, or blank after trimming.
     *       Error: "Name is required"</li>
     *   <li><b>email</b> — must not be null or blank, and must match the basic
     *       email format pattern ({@code user@domain.tld}).
     *       Errors: "Email is required" or "Invalid email format"</li>
     *   <li><b>message</b> — must not be null or blank, and must be at least
     *       10 characters long after trimming.
     *       Errors: "Message is required" or "Message must be at least 10 characters"</li>
     * </ul>
     *
     * <h4>Success response (200 OK):</h4>
     * <pre>{@code
     * {
     *   "success": true,
     *   "message": "Thank you for your message! We'll get back to you soon."
     * }
     * }</pre>
     *
     * <h4>Validation error response (400 Bad Request):</h4>
     * <pre>{@code
     * {
     *   "success": false,
     *   "errors": {
     *     "name": "Name is required",
     *     "email": "Invalid email format",
     *     "message": "Message must be at least 10 characters"
     *   }
     * }
     * }</pre>
     *
     * @param body the request body as a map of string key-value pairs,
     *             deserialized from the JSON payload. Expected keys:
     *             "name", "email", "message". Additional keys are ignored.
     * @return 200 OK with a success confirmation if all fields are valid, or
     *         400 Bad Request with a field-level error map if validation fails
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContact(
            @RequestBody Map<String, String> body) {

        /*
         * Extract form fields from the request body. Using Map.getOrDefault()
         * ensures we get an empty string rather than null when a key is missing,
         * which simplifies the validation logic below (no null checks needed
         * on the extracted values themselves — we just check for blank/empty).
         */
        String name = body.getOrDefault("name", "");
        String email = body.getOrDefault("email", "");
        String message = body.getOrDefault("message", "");

        /*
         * Collect validation errors into a LinkedHashMap. We use LinkedHashMap
         * (not HashMap) to preserve insertion order so that errors appear in
         * field order (name → email → message) in the JSON response, which
         * makes client-side rendering of errors predictable and consistent.
         */
        Map<String, String> errors = new LinkedHashMap<>();

        /* ── Validate name ────────────────────────────────────────────────
         * The name field must not be blank (empty or whitespace-only).
         * We trim before checking to handle cases where the user typed
         * only spaces.
         */
        if (name.isBlank()) {
            errors.put("name", "Name is required");
        }

        /* ── Validate email ───────────────────────────────────────────────
         * The email field has a two-stage validation:
         * 1. It must not be blank (presence check)
         * 2. It must match the basic email pattern (format check)
         *
         * We only check the format if the field is non-blank, to avoid
         * showing both "required" and "invalid format" errors simultaneously.
         */
        if (email.isBlank()) {
            errors.put("email", "Email is required");
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.put("email", "Invalid email format");
        }

        /* ── Validate message ─────────────────────────────────────────────
         * The message field has a two-stage validation:
         * 1. It must not be blank (presence check)
         * 2. It must be at least MIN_MESSAGE_LENGTH characters (length check)
         *
         * The length check uses the trimmed value to ignore leading/trailing
         * whitespace that the user may have inadvertently entered.
         */
        if (message.isBlank()) {
            errors.put("message", "Message is required");
        } else if (message.trim().length() < MIN_MESSAGE_LENGTH) {
            errors.put("message", "Message must be at least " + MIN_MESSAGE_LENGTH + " characters");
        }

        /* ── Return error response if any validations failed ──────────────
         * If the errors map is non-empty, at least one field failed validation.
         * Return a 400 Bad Request status with the error details so the client
         * can display field-level error messages to the user.
         */
        if (!errors.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        /* ── All validations passed — process the submission ──────────────
         *
         * In a production application, this is where we would:
         * 1. Sanitize the input (strip HTML tags, normalize whitespace)
         * 2. Save the contact request to a database table
         * 3. Send a confirmation email to the user
         * 4. Forward the message to the support team (email, Slack, CRM)
         * 5. Log the submission for auditing purposes
         *
         * For this demo, we simply acknowledge the submission with a
         * friendly success message. The client-side component can use
         * the "success: true" flag to show a thank-you state and clear
         * the form fields.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Thank you for your message! We'll get back to you soon.");

        return ResponseEntity.ok(response);
    }
}
