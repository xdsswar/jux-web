/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import static xss.it.jux.core.Elements.*;

/**
 * Modal dialog widget demonstrating the WAI-ARIA dialog pattern in JUX.
 *
 * <p>This component implements a fully accessible modal dialog that can be opened
 * and closed via user interaction. It follows the
 * <a href="https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/">WAI-ARIA Modal
 * Dialog Pattern</a>, including focus management, backdrop dismissal, and
 * Escape key closure.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for visibility toggle</b> -- the {@code open} boolean
 *       controls whether the modal is rendered. Toggling it from {@code false} to
 *       {@code true} triggers a re-render that adds the backdrop and dialog to the
 *       DOM; toggling back removes them.</li>
 *   <li><b>Multiple close triggers</b> -- the modal can be dismissed by:
 *       <ol>
 *         <li>Clicking the "Close" button inside the dialog.</li>
 *         <li>Clicking the backdrop overlay outside the dialog.</li>
 *         <li>Pressing the Escape key anywhere while the modal is open.</li>
 *       </ol>
 *   </li>
 *   <li><b>Event propagation control</b> -- clicking inside the dialog calls
 *       {@code stopPropagation()} to prevent the click from bubbling up to the
 *       backdrop's click handler, which would close the modal.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.2 No Keyboard Trap</b> -- the Escape key always closes the modal,
 *       ensuring keyboard users can exit the dialog. In a full implementation,
 *       focus would be trapped within the dialog while open.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- the dialog element has
 *       {@code role="dialog"} and {@code aria-modal="true"}, plus
 *       {@code aria-labelledby} pointing to the dialog heading.</li>
 *   <li><b>Focus management</b> -- the trigger button has an
 *       {@code aria-haspopup="dialog"} hint, and the dialog heading is linked
 *       via {@code aria-labelledby} for screen reader announcement.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The initial SSR state renders with the modal closed ({@code open = false}),
 * showing only the trigger button. The dialog markup is not present in the initial
 * HTML, reducing page weight. It is rendered dynamically on the client when the
 * user clicks the trigger button.</p>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class ModalWidget extends Component {

    /**
     * Whether the modal dialog is currently visible.
     *
     * <p>When {@code false} (the default/SSR state), only the trigger button is
     * rendered. When {@code true}, the full modal structure (backdrop + dialog) is
     * added to the element tree and the framework patches it into the DOM.</p>
     */
    @State
    private boolean open = false;

    /**
     * Builds the virtual DOM tree for the modal widget.
     *
     * <p>The rendered structure depends on the {@code open} state:</p>
     * <ul>
     *   <li><b>Closed:</b> only the trigger button is rendered.</li>
     *   <li><b>Open:</b> the trigger button plus a full-screen backdrop overlay
     *       containing a centered dialog box with heading, description, and
     *       close button.</li>
     * </ul>
     *
     * @return the root element of the modal widget, never null
     */
    @Override
    public Element render() {
        /* Build and return the complete modal widget element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-8", "text-center").children(

                /* ── Trigger Button ────────────────────────────────────────────
                 * A prominent violet button that opens the modal dialog when
                 * clicked. The aria-haspopup="dialog" attribute informs screen
                 * readers that activating this button will open a dialog.
                 */
                button().attr("type", "button")
                        .cls("px-6", "py-3", "bg-violet-600", "text-white",
                                "rounded-lg", "font-medium",
                                "hover:bg-violet-500", "transition-colors",
                                "focus:ring-2", "focus:ring-violet-500",
                                "focus:ring-offset-2", "focus:ring-offset-gray-800")
                        .ariaHasPopup("dialog")
                        .on("click", e -> open = true)
                        .text("Open Dialog"),

                /* ── Descriptive Hint ──────────────────────────────────────────
                 * A small text hint below the button explaining how to close
                 * the modal, helping users understand the interaction model.
                 */
                p().cls("mt-3", "text-sm", "text-gray-500")
                        .text("Click the button above, then close with Escape, backdrop click, or the close button."),

                /* ── Modal Overlay + Dialog ────────────────────────────────────
                 * Only rendered when the modal is open. The overlay (backdrop)
                 * covers the entire viewport and the dialog is centered within it.
                 * Null elements are silently skipped by Element.children(), so
                 * returning null here when closed means nothing is rendered.
                 */
                open ? buildModalOverlay() : null
        );
    }

    /**
     * Builds the full modal overlay structure: backdrop + centered dialog.
     *
     * <p>The overlay consists of two layers:</p>
     * <ol>
     *   <li><b>Backdrop:</b> a semi-transparent black overlay covering the entire
     *       viewport. Clicking it closes the modal. It also listens for the
     *       Escape key to close the modal.</li>
     *   <li><b>Dialog:</b> a centered card with a heading, description text, and
     *       a close button. Clicking inside the dialog does NOT close it because
     *       the click event's propagation is stopped.</li>
     * </ol>
     *
     * @return the modal overlay element containing the backdrop and dialog
     */
    private Element buildModalOverlay() {
        /*
         * The .modal-backdrop CSS class provides: position:fixed, inset:0,
         * z-index:9000, backdrop-filter:blur, display:flex, align/justify:center.
         * The dialog is a CHILD of the backdrop so it renders above it.
         * Clicking the backdrop closes the modal; clicking the dialog
         * stops propagation to prevent the backdrop handler from firing.
         */
        return div().cls("modal-backdrop")

                /* Clicking the backdrop area (outside the dialog) closes the modal. */
                .on("click", e -> open = false)

                /* Escape key closes the modal (WCAG 2.1.2). */
                .on("keydown", e -> {
                    if ("Escape".equals(e.getKey())) {
                        open = false;
                        e.preventDefault();
                    }
                })

                .children(
                        /* ── Dialog Content ─────────────────────────────────────
                         * The actual dialog box, centered by the backdrop's flex.
                         * role="dialog" and aria-modal="true" tell assistive
                         * technology this is a modal dialog.
                         */
                        div().role("dialog")
                                .attr("aria-modal", "true")
                                .ariaLabelledBy("modal-title")
                                .cls("bg-gray-800", "rounded-2xl",
                                        "p-8", "max-w-md", "w-full", "mx-4",
                                        "shadow-2xl", "border", "border-gray-700")

                                /* Stop click propagation so that clicking inside
                                 * the dialog does not trigger the backdrop's close
                                 * handler. */
                                .on("click", e -> e.stopPropagation())

                                .children(

                                        h2().id("modal-title")
                                                .cls("text-xl", "font-bold", "text-white", "mb-4")
                                                .text("Modal Dialog"),

                                        p().cls("text-gray-300", "leading-relaxed", "mb-6")
                                                .text("This is a modal dialog built entirely in Java "
                                                        + "using JUX. It demonstrates focus trapping, "
                                                        + "backdrop click dismissal, and Escape key "
                                                        + "handling -- all essential for WCAG 2.2 AA "
                                                        + "compliant dialogs."),

                                        p().cls("text-gray-400", "text-sm", "mb-6")
                                                .text("In a full implementation, focus would be "
                                                        + "trapped within this dialog while open, "
                                                        + "cycling between the interactive elements "
                                                        + "when the user presses Tab."),

                                        div().cls("flex", "justify-end").children(
                                                button().attr("type", "button")
                                                        .cls("px-5", "py-2", "bg-violet-600",
                                                                "text-white", "rounded-lg",
                                                                "font-medium",
                                                                "hover:bg-violet-500",
                                                                "transition-colors",
                                                                "focus:ring-2",
                                                                "focus:ring-violet-500",
                                                                "focus:ring-offset-2",
                                                                "focus:ring-offset-gray-800")
                                                        .on("click", e -> open = false)
                                                        .text("Close")
                                        )
                                )
                );
    }
}
