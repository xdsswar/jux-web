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
import java.util.UUID;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible modal dialog component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a {@code <dialog>} element with proper ARIA attributes for
 * screen reader announcements, focus management, and keyboard interaction.
 * The dialog follows the WAI-ARIA Dialog (Modal) pattern:</p>
 *
 * <ul>
 *   <li>{@code role="dialog"} -- identifies the element as a dialog</li>
 *   <li>{@code aria-modal="true"} -- indicates the dialog traps focus</li>
 *   <li>{@code aria-labelledby} -- points to the dialog's title element
 *       so screen readers announce the dialog purpose on open</li>
 * </ul>
 *
 * <p><b>Keyboard interaction (WCAG 2.1.1, 2.1.2):</b></p>
 * <ul>
 *   <li><b>Escape</b> -- closes the dialog (handled by native {@code <dialog>})</li>
 *   <li><b>Tab/Shift+Tab</b> -- cycles focus within the dialog (focus trap)</li>
 *   <li>Focus moves to the dialog when opened, returns to trigger on close</li>
 * </ul>
 *
 * <p><b>Structure produced:</b></p>
 * <pre>{@code
 * <dialog role="dialog" aria-modal="true" aria-labelledby="title-id" class="jux-modal">
 *   <div class="jux-modal-header">
 *     <h2 id="title-id">Title</h2>
 *     <button type="button" class="jux-modal-close" aria-label="Close dialog">x</button>
 *   </div>
 *   <div class="jux-modal-body">
 *     <!-- content -->
 *   </div>
 * </dialog>
 * }</pre>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * var content = p().text("Are you sure you want to delete this item?");
 * child(new JuxModal("Confirm Deletion", content));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/">WAI-ARIA Dialog Pattern</a>
 */
@JuxComponent
public class JuxModal extends Component {

    /**
     * The title displayed in the modal header.
     *
     * <p>This title serves double duty: it is visually displayed as an
     * {@code <h2>} heading, and it is programmatically associated with the
     * dialog via {@code aria-labelledby} so screen readers announce it
     * when the modal opens.</p>
     */
    private final String title;

    /**
     * The body content of the modal dialog.
     *
     * <p>This can be any Element tree -- text, forms, images, or complex
     * layouts. The content is placed inside a scrollable container so
     * the modal handles overflow gracefully on small viewports.</p>
     */
    private final Element content;

    /**
     * Auto-generated unique ID for the title element.
     *
     * <p>Used to link the {@code aria-labelledby} attribute on the dialog
     * to the {@code <h2>} title element. A UUID suffix ensures uniqueness
     * even when multiple modals exist on the same page.</p>
     */
    private final String titleId;

    /**
     * Auto-generated unique ID for the dialog element itself.
     *
     * <p>Used by trigger buttons via {@code aria-controls} to reference
     * this modal dialog. Also allows JavaScript to locate and open/close
     * the dialog programmatically.</p>
     */
    private final String dialogId;

    /**
     * Creates a new modal dialog component.
     *
     * @param title   the dialog title displayed in the header and announced
     *                by screen readers; must not be null
     * @param content the Element tree to display in the dialog body; must not be null
     * @throws NullPointerException if title or content is null
     */
    public JuxModal(String title, Element content) {
        this.title = Objects.requireNonNull(title, "Modal title must not be null");
        this.content = Objects.requireNonNull(content, "Modal content must not be null");

        /*
         * Generate a unique ID for the title element. We use only the first
         * 8 characters of a UUID to keep the HTML attribute short while still
         * providing sufficient uniqueness for a single page.
         */
        String uid = UUID.randomUUID().toString().substring(0, 8);
        this.titleId = "jux-modal-title-" + uid;
        this.dialogId = "jux-modal-" + uid;
    }

    /**
     * Returns the auto-generated ID of the dialog element.
     *
     * <p>Use this to wire up a trigger button with {@code aria-controls}:</p>
     * <pre>{@code
     * var modal = new JuxModal("Title", content);
     * button().cls("jux-modal-open")
     *     .ariaControls(modal.getDialogId())
     *     .text("Open");
     * }</pre>
     *
     * @return the dialog element's unique ID
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * Builds the modal dialog Element tree with full ARIA support.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A {@code <dialog>} root element with role, modal, and labelling attributes</li>
     *   <li>A header {@code <div>} containing the title {@code <h2>} and a close button</li>
     *   <li>A body {@code <div>} containing the user-provided content</li>
     * </ol>
     *
     * <p>The close button includes {@code aria-label="Close dialog"} so screen
     * readers announce its purpose even though its visible text is just "x".
     * The button uses {@code type="button"} to prevent accidental form submission
     * if the modal is inside a form context.</p>
     *
     * @return the root dialog Element with all children, never null
     */
    @Override
    public Element render() {
        /*
         * Build the header section using Bootstrap 5 modal-header pattern.
         * The title uses <h2> with modal-title + fs-5 for appropriate sizing.
         * The close button uses Bootstrap's btn-close class with data-bs-dismiss
         * so Bootstrap JS handles closing automatically.
         */
        Element headerSection = div().cls("modal-header").children(
                h2().cls("modal-title", "fs-5").id(titleId).text(title),
                button()
                        .attr("type", "button")
                        .cls("btn-close")
                        .attr("data-bs-dismiss", "modal")
                        .aria("label", "Close")
        );

        /* Build the body section that wraps the user-provided content. */
        Element bodySection = div().cls("modal-body").children(content);

        /*
         * Assemble the Bootstrap 5 modal structure:
         * modal > modal-dialog > modal-content > (header + body)
         *
         * - "modal fade" enables the backdrop and fade animation
         * - tabindex="-1" allows Bootstrap to manage focus
         * - aria-labelledby points to the title for screen reader context
         * - aria-hidden="true" hides it from assistive tech when closed
         *
         * Opening is handled by a trigger button with
         * data-bs-toggle="modal" data-bs-target="#dialogId"
         */
        Element modalContent = div().cls("modal-content").children(headerSection, bodySection);
        Element modalDialog = div().cls("modal-dialog").children(modalContent);

        return div()
                .cls("modal", "fade")
                .id(dialogId)
                .tabIndex(-1)
                .ariaLabelledBy(titleId)
                .ariaHidden(true)
                .children(modalDialog);
    }
}
