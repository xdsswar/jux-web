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

import java.util.Objects;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible toast notification component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a non-modal notification message that is automatically announced
 * by screen readers via an ARIA live region. Toasts are used for brief,
 * time-sensitive feedback: form submission confirmations, error alerts,
 * status updates, and informational messages.</p>
 *
 * <p><b>ARIA semantics:</b></p>
 * <ul>
 *   <li>{@code role="alert"} -- causes immediate announcement by screen readers
 *       regardless of current focus position (equivalent to {@code aria-live="assertive"})</li>
 *   <li>{@code aria-live="assertive"} -- explicitly set for assistive technology
 *       that may not fully support the alert role</li>
 *   <li>{@code aria-atomic="true"} -- ensures the entire toast message is
 *       re-announced when content changes, not just the delta</li>
 * </ul>
 *
 * <p><b>Toast types and their visual/semantic meaning:</b></p>
 * <ul>
 *   <li>{@code SUCCESS} -- operation completed successfully (green styling)</li>
 *   <li>{@code ERROR} -- an error occurred that needs attention (red styling)</li>
 *   <li>{@code WARNING} -- a cautionary message (amber/yellow styling)</li>
 *   <li>{@code INFO} -- neutral informational message (blue styling)</li>
 * </ul>
 *
 * <p><b>Accessibility considerations:</b></p>
 * <ul>
 *   <li>The assertive live region ensures screen reader users are immediately
 *       notified of the toast, even if focus is elsewhere on the page</li>
 *   <li>Toast content should be concise (one sentence) for quick comprehension</li>
 *   <li>Color alone does not convey meaning -- the toast type is also indicated
 *       by a CSS class and can be extended with icons that have alt text</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * child(new JuxToast("Settings saved successfully.", JuxToast.ToastType.SUCCESS));
 * child(new JuxToast("Unable to connect to server.", JuxToast.ToastType.ERROR));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/alert/">WAI-ARIA Alert Pattern</a>
 */
@JuxComponent
public class JuxToast extends Component {

    /**
     * Enumeration of toast notification types.
     *
     * <p>Each type maps to a distinct visual treatment (color, icon) and
     * conveys a specific semantic meaning to the user. The type is rendered
     * as a CSS class modifier on the toast element (e.g. {@code jux-toast-success})
     * so themes can style each type differently.</p>
     */
    public enum ToastType {

        /**
         * Success notification -- indicates an operation completed without errors.
         *
         * <p>Visual treatment: typically green background or border with a
         * checkmark icon. Example: "Your changes have been saved."</p>
         */
        SUCCESS("success"),

        /**
         * Error notification -- indicates a failure that requires user attention.
         *
         * <p>Visual treatment: typically red background or border with an
         * exclamation/error icon. Example: "Unable to process payment."</p>
         */
        ERROR("error"),

        /**
         * Warning notification -- indicates a condition that may need attention.
         *
         * <p>Visual treatment: typically amber/yellow background or border
         * with a warning icon. Example: "Your session will expire in 5 minutes."</p>
         */
        WARNING("warning"),

        /**
         * Informational notification -- neutral message for the user's awareness.
         *
         * <p>Visual treatment: typically blue background or border with an
         * info icon. Example: "A new version is available."</p>
         */
        INFO("info");

        /**
         * The CSS class suffix used to style this toast type.
         *
         * <p>Appended to the base class to form the full modifier class:
         * {@code "jux-toast-" + cssClass} (e.g. "jux-toast-success").</p>
         */
        private final String cssClass;

        /**
         * Creates a ToastType with its associated CSS class suffix.
         *
         * @param cssClass the CSS class suffix for styling
         */
        ToastType(String cssClass) {
            this.cssClass = cssClass;
        }

        /**
         * Returns the CSS class suffix for this toast type.
         *
         * @return the CSS class suffix (e.g. "success", "error")
         */
        public String cssClass() {
            return cssClass;
        }
    }

    /**
     * The notification message displayed in the toast.
     *
     * <p>Should be concise and actionable. Screen readers will announce
     * this text immediately due to the alert role and assertive live region.</p>
     */
    private final String message;

    /**
     * The type of toast notification.
     *
     * <p>Determines the visual styling (color, icon) and the semantic
     * meaning conveyed to the user.</p>
     */
    private final ToastType type;

    /**
     * Creates a new toast notification component.
     *
     * @param message the notification message to display; must not be null
     * @param type    the type of notification (SUCCESS, ERROR, WARNING, INFO);
     *                must not be null
     * @throws NullPointerException if message or type is null
     */
    public JuxToast(String message, ToastType type) {
        this.message = Objects.requireNonNull(message, "Toast message must not be null");
        this.type = Objects.requireNonNull(type, "Toast type must not be null");
    }

    /**
     * Builds the toast notification Element tree with ARIA live region support.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <div role="alert" aria-live="assertive" aria-atomic="true"
     *      class="jux-toast jux-toast-success">
     *   <p class="jux-toast-message">Settings saved successfully.</p>
     * </div>
     * }</pre>
     *
     * <p>The combination of {@code role="alert"} and {@code aria-live="assertive"}
     * provides maximum compatibility across screen readers. Some older
     * assistive technologies respond to one but not the other, so both
     * are included as a belt-and-suspenders approach.</p>
     *
     * <p>The message is wrapped in a {@code <p>} element for proper
     * text semantics rather than placed directly as text content on the
     * container div.</p>
     *
     * @return the toast container Element with alert semantics, never null
     */
    @Override
    public Element render() {
        /*
         * Build the message paragraph. Using a <p> provides proper text
         * semantics and allows future extension with additional content
         * (e.g. an action link) as siblings.
         */
        Element messageElement = p()
                .cls("jux-toast-message")
                .text(message);

        /*
         * Build the toast container with alert semantics:
         * - role="alert" triggers immediate screen reader announcement
         * - aria-live="assertive" is a fallback for assistive tech that
         *   doesn't fully support the alert role
         * - aria-atomic="true" ensures the full message is announced,
         *   not just changed parts (important if the toast is reused
         *   with updated messages)
         * - Type-specific CSS class enables visual differentiation by theme
         */
        return div()
                .role("alert")
                .ariaLive("assertive")
                .ariaAtomic(true)
                .cls("jux-toast", "jux-toast-" + type.cssClass())
                .children(messageElement);
    }
}
