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
 * Four-column store footer with shop links, company info, and copyright.
 *
 * <p>The footer is wrapped in a {@code <footer>} landmark element.
 * Navigation links within are grouped in a {@code <nav>} with a
 * descriptive aria-label to distinguish it from the header nav.</p>
 */
public class StoreFooter extends Component {

    private final JuxMessages messages;

    public StoreFooter(JuxMessages messages) {
        this.messages = messages;
    }

    @Override
    public Element render() {
        return footer().cls("bg-gray-900", "text-gray-300").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8",
                                "py-12")
                        .children(
                                /* 4-column grid */
                                div().cls("grid", "grid-cols-1", "md:grid-cols-4", "gap-8")
                                        .children(
                                                brandColumn(),
                                                shopColumn(),
                                                companyColumn(),
                                                supportColumn()
                                        )
                        ),
                /* Bottom bar with copyright */
                div().cls("border-t", "border-gray-700").children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8",
                                        "py-4", "text-sm", "text-gray-500", "text-center")
                                .children(
                                        p().text(messages.getString("footer.copyright"))
                                )
                )
        );
    }

    private Element brandColumn() {
        return div().children(
                h3().cls("text-white", "font-bold", "text-lg", "mb-3")
                        .text(messages.getString("nav.brand")),
                p().cls("text-sm", "text-gray-400", "mb-4")
                        .text(messages.getString("footer.description")),
                p().cls("text-sm", "text-gray-500")
                        .text(messages.getString("footer.built_with"))
        );
    }

    private Element shopColumn() {
        return nav().aria("label", messages.getString("footer.shop")).children(
                h3().cls("text-white", "font-semibold", "mb-3")
                        .text(messages.getString("footer.shop")),
                ul().cls("space-y-2", "text-sm").children(
                        footerLink("/products", messages.getString("nav.products")),
                        footerLink("/categories", messages.getString("nav.categories")),
                        footerLink("/products?sort=rating", messages.getString("footer.top_rated")),
                        footerLink("/products?sort=newest", messages.getString("footer.new_arrivals"))
                )
        );
    }

    private Element companyColumn() {
        return nav().aria("label", messages.getString("footer.company")).children(
                h3().cls("text-white", "font-semibold", "mb-3")
                        .text(messages.getString("footer.company")),
                ul().cls("space-y-2", "text-sm").children(
                        footerLink("/about", messages.getString("nav.about")),
                        footerLink("/contact", messages.getString("nav.contact")),
                        footerLink("/about", messages.getString("footer.careers")),
                        footerLink("/about", messages.getString("footer.press"))
                )
        );
    }

    private Element supportColumn() {
        return nav().aria("label", messages.getString("footer.support")).children(
                h3().cls("text-white", "font-semibold", "mb-3")
                        .text(messages.getString("footer.support")),
                ul().cls("space-y-2", "text-sm").children(
                        footerLink("/contact", messages.getString("footer.help")),
                        footerLink("/contact", messages.getString("footer.shipping")),
                        footerLink("/contact", messages.getString("footer.returns")),
                        footerLink("/contact", messages.getString("footer.faq"))
                )
        );
    }

    private Element footerLink(String href, String label) {
        return li().children(
                a().attr("href", href)
                        .cls("text-gray-400", "hover:text-white", "transition-colors")
                        .text(label)
        );
    }
}
