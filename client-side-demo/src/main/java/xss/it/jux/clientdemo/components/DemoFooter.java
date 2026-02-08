/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Minimal dark-themed footer for the Client-Side Demo application.
 *
 * <p>Renders a compact footer with two centred text lines: a tagline
 * describing the demo's philosophy ("Built entirely in Java") and a
 * combined copyright / powered-by line. The footer uses the same dark
 * colour scheme as the rest of the demo.</p>
 *
 * <h2>Rendered HTML Outline</h2>
 * <pre>{@code
 * <footer class="bg-gray-900 text-gray-400 border-t border-gray-800">
 *   <div class="max-w-7xl mx-auto px-4 py-8">
 *     <div class="text-center">
 *       <p>Built entirely in Java — no JavaScript, no templates.</p>
 *       <p>© 2026 JUX Framework. All rights reserved. · Powered by JUX + TeaVM</p>
 *     </div>
 *   </div>
 * </footer>
 * }</pre>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>Landmark</b> — uses the semantic {@code <footer>} element,
 *       which assistive technology maps to the "contentinfo" ARIA role.
 *       This lets screen-reader users jump directly to the footer from
 *       a landmarks list.</li>
 *   <li><b>1.4.3 Contrast</b> — {@code text-gray-400} on
 *       {@code bg-gray-900} provides a contrast ratio above 4.5:1,
 *       meeting the AA minimum for normal text.</li>
 *   <li><b>1.4.4 Resize</b> — all font sizes are relative (Tailwind's
 *       default rem-based scale), allowing the text to resize correctly
 *       when the user adjusts browser zoom.</li>
 * </ul>
 *
 * @see DemoLayout
 */
public class DemoFooter extends Component {

    /**
     * Localised message bundle for the current request locale.
     * Provides all user-visible text in the footer: the tagline,
     * copyright notice, and "powered by" credit line.
     */
    private final JuxMessages messages;

    /**
     * Create a new footer component.
     *
     * @param messages the i18n message bundle for the current request locale;
     *                 must not be {@code null}
     */
    public DemoFooter(JuxMessages messages) {
        this.messages = messages;
    }

    /**
     * Render the footer element tree.
     *
     * <p>The {@code <footer>} element establishes the "contentinfo"
     * landmark. It uses a dark background ({@code bg-gray-900}) with a
     * top border ({@code border-t border-gray-800}) to visually separate
     * it from the main content area. Text colour is {@code text-gray-400}
     * for a subdued appearance that still meets contrast requirements.</p>
     *
     * <p>Inside is a max-width container with comfortable padding and
     * centred text. Two paragraphs are rendered:</p>
     * <ol>
     *   <li><b>Tagline</b> — a one-line summary of the demo's approach
     *       (resolved from the {@code footer.tagline} message key).</li>
     *   <li><b>Copyright + credit</b> — the copyright notice and a
     *       "Powered by" credit, separated by a middle dot. The copyright
     *       line uses a slightly muted colour ({@code text-gray-500}) and
     *       smaller text to de-emphasise it relative to the tagline.</li>
     * </ol>
     *
     * @return the {@code <footer>} element tree
     */
    @Override
    public Element render() {
        return footer().cls("bg-gray-900", "text-gray-400", "border-t", "border-gray-800")
                .children(
                        /* ── Container: constrains width, adds padding ────────── */
                        div().cls("max-w-7xl", "mx-auto", "px-4", "py-8")
                                .children(
                                        /* Centred text block */
                                        div().cls("text-center")
                                                .children(
                                                        /* Line 1: project tagline
                                                         * "Built entirely in Java — no JavaScript, no templates." */
                                                        p().cls("text-sm", "mb-2")
                                                                .text(messages.getString("footer.tagline")),

                                                        /* Line 2: copyright + powered-by credit
                                                         * Rendered in a slightly smaller, more muted style to
                                                         * create a visual hierarchy within the footer. The
                                                         * middle dot (·) serves as a lightweight separator. */
                                                        p().cls("text-xs", "text-gray-500")
                                                                .text(messages.getString("footer.copyright")
                                                                        + " \u00B7 "
                                                                        + messages.getString("footer.built_with"))
                                                )
                                )
                );
    }
}
