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
 * Promotional banner section with gradient background and CTA.
 *
 * <p>Displays a sale or promotional message with a call-to-action
 * button. Used on the home page between content sections to
 * draw attention to current offers.</p>
 */
public class PromoBanner extends Component {

    private final JuxMessages messages;

    public PromoBanner(JuxMessages messages) {
        this.messages = messages;
    }

    @Override
    public Element render() {
        return section().cls("bg-gradient-to-r", "from-indigo-600", "to-purple-600",
                        "text-white", "py-16")
                .children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8",
                                        "text-center")
                                .children(
                                        h2().cls("text-3xl", "font-bold", "mb-3")
                                                .text(messages.getString("promo.title")),
                                        p().cls("text-lg", "text-indigo-100", "mb-6", "max-w-2xl", "mx-auto")
                                                .text(messages.getString("promo.text")),
                                        a().cls("inline-block", "bg-white", "text-indigo-600",
                                                        "px-8", "py-3", "rounded-lg", "font-semibold",
                                                        "hover:bg-indigo-50", "transition-colors",
                                                        "focus:ring-2", "focus:ring-white", "focus:ring-offset-2",
                                                        "focus:ring-offset-indigo-600")
                                                .attr("href", "/products")
                                                .text(messages.getString("promo.cta"))
                                )
                );
    }
}
