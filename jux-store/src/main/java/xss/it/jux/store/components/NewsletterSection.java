/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Newsletter signup section with email input and submit button.
 *
 * <p>ADA compliance:</p>
 * <ul>
 *   <li>Email input has a visible {@code <label>} (not placeholder-only)</li>
 *   <li>The label is linked to the input via {@code for}/id pairing</li>
 *   <li>The input has {@code ariaRequired(true)} to indicate it's required</li>
 *   <li>The input has {@code autocomplete="email"} (WCAG 1.3.5)</li>
 * </ul>
 */
public class NewsletterSection extends Component {

    private final JuxMessages messages;

    public NewsletterSection(JuxMessages messages) {
        this.messages = messages;
    }

    @Override
    public Element render() {
        return section().cls("bg-gray-50", "py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8", "text-center")
                        .children(
                                h2().cls("text-2xl", "font-bold", "text-gray-900", "mb-2")
                                        .text(messages.getString("newsletter.title")),
                                p().cls("text-gray-500", "mb-6")
                                        .text(messages.getString("newsletter.text")),
                                /* Inline form: label + email input + button */
                                form().cls("flex", "flex-col", "sm:flex-row", "gap-3",
                                                "max-w-lg", "mx-auto")
                                        .attr("method", "post")
                                        .attr("action", "/newsletter")
                                        .children(
                                                /* Visible label linked to input (ADA: WCAG 3.3.2) */
                                                label().cls("jux-sr-only")
                                                        .attr("for", "newsletter-email")
                                                        .text(messages.getString("newsletter.label")),
                                                input().id("newsletter-email")
                                                        .cls("flex-1", "px-4", "py-3", "border",
                                                                "border-gray-300", "rounded-lg",
                                                                "focus:ring-2", "focus:ring-indigo-500",
                                                                "focus:border-indigo-500")
                                                        .attr("type", "email")
                                                        .attr("name", "email")
                                                        .attr("placeholder",
                                                                messages.getString("newsletter.placeholder"))
                                                        .attr("autocomplete", "email")
                                                        .ariaRequired(true),
                                                button().cls("bg-indigo-600", "text-white", "px-6", "py-3",
                                                                "rounded-lg", "font-semibold",
                                                                "hover:bg-indigo-700", "focus:ring-2",
                                                                "focus:ring-indigo-500", "focus:ring-offset-2")
                                                        .attr("type", "submit")
                                                        .text(messages.getString("newsletter.submit"))
                                        )
                        )
        );
    }
}
