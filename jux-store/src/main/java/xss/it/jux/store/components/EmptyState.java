/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import static xss.it.jux.core.Elements.*;

/**
 * Reusable empty state component for when there is no content to display.
 *
 * <p>Shows an emoji icon, a heading, a description message, and an
 * optional call-to-action link. Used for empty carts, no search results,
 * and other empty views.</p>
 */
public class EmptyState extends Component {

    private final String icon;
    private final String title;
    private final String message;
    private final String ctaText;
    private final String ctaUrl;

    /**
     * Creates an empty state with all elements.
     *
     * @param icon    emoji or text icon displayed large
     * @param title   heading text
     * @param message descriptive paragraph text
     * @param ctaText button label (null to hide button)
     * @param ctaUrl  button destination URL (null to hide button)
     */
    public EmptyState(String icon, String title, String message, String ctaText, String ctaUrl) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.ctaText = ctaText;
        this.ctaUrl = ctaUrl;
    }

    @Override
    public Element render() {
        var content = div().cls("text-center", "py-16").children(
                /* Large icon */
                div().cls("text-6xl", "mb-4").ariaHidden(true).text(icon),
                /* Heading */
                h2().cls("text-2xl", "font-bold", "text-gray-900", "mb-2").text(title),
                /* Description */
                p().cls("text-gray-500", "mb-6", "max-w-md", "mx-auto").text(message)
        );

        /* Optional CTA button */
        if (ctaText != null && ctaUrl != null) {
            content = content.children(
                    a().cls("inline-block", "bg-indigo-600", "text-white", "px-6", "py-3",
                                    "rounded-lg", "font-semibold", "hover:bg-indigo-700",
                                    "focus:ring-2", "focus:ring-indigo-500", "focus:ring-offset-2")
                            .attr("href", ctaUrl)
                            .text(ctaText)
            );
        }
        return content;
    }
}
