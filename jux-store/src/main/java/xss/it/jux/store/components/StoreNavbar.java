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

import java.util.Locale;

import static xss.it.jux.core.Elements.*;

/**
 * Top navigation bar for the JUX Store.
 *
 * <p>Contains the store logo/brand, main navigation links,
 * a search shortcut, cart link, and a language switcher.</p>
 *
 * <p>ADA compliance:</p>
 * <ul>
 *   <li>The nav is wrapped in a {@code <nav>} landmark with aria-label</li>
 *   <li>The current page link uses {@code aria-current="page"}</li>
 *   <li>The language switcher is in its own {@code <nav>} with a distinct aria-label</li>
 * </ul>
 */
public class StoreNavbar extends Component {

    private final String activePath;
    private final JuxMessages messages;

    public StoreNavbar(String activePath, JuxMessages messages) {
        this.activePath = activePath;
        this.messages = messages;
    }

    @Override
    public Element render() {
        return Element.of("header").cls("bg-white", "border-b", "border-gray-200",
                        "sticky", "top-0", "z-50")
                .children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                .children(
                                        div().cls("flex", "items-center", "justify-between", "h-16")
                                                .children(
                                                        /* Logo / Brand */
                                                        a().attr("href", "/")
                                                                .cls("flex", "items-center", "gap-2",
                                                                        "font-bold", "text-xl",
                                                                        "text-indigo-600")
                                                                .children(
                                                                        span().ariaHidden(true).text("\uD83D\uDED2"),
                                                                        span().text(messages.getString("nav.brand"))
                                                                ),
                                                        /* Main navigation links */
                                                        mainNav(),
                                                        /* Right side: search + cart + lang */
                                                        rightActions()
                                                )
                                )
                );
    }

    /**
     * Main navigation links: Home, Products, Categories, About, Contact.
     */
    private Element mainNav() {
        return nav().aria("label", messages.getString("nav.main.label"))
                .cls("hidden", "md:flex", "items-center", "gap-6")
                .children(
                        navLink("/", messages.getString("nav.home")),
                        navLink("/products", messages.getString("nav.products")),
                        navLink("/categories", messages.getString("nav.categories")),
                        navLink("/about", messages.getString("nav.about")),
                        navLink("/contact", messages.getString("nav.contact"))
                );
    }

    /**
     * Builds a single nav link with active state detection.
     */
    private Element navLink(String href, String label) {
        boolean isActive = activePath.equals(href)
                || (href.length() > 1 && activePath.startsWith(href));

        var link = a().attr("href", href)
                .cls("text-sm", "font-medium", "transition-colors");

        if (isActive) {
            link = link.cls("text-indigo-600").ariaCurrent("page");
        } else {
            link = link.cls("text-gray-600", "hover:text-indigo-600");
        }
        return link.text(label);
    }

    /**
     * Right side actions: search link, cart link, and language switcher.
     */
    private Element rightActions() {
        return div().cls("flex", "items-center", "gap-4").children(
                /* Search link */
                a().attr("href", "/search")
                        .cls("text-gray-500", "hover:text-indigo-600", "transition-colors")
                        .aria("label", messages.getString("nav.search"))
                        .text("\uD83D\uDD0D"),
                /* Cart link */
                a().attr("href", "/cart")
                        .cls("text-gray-500", "hover:text-indigo-600", "transition-colors")
                        .aria("label", messages.getString("nav.cart"))
                        .text("\uD83D\uDED2"),
                /* Language switcher */
                languageSwitcher()
        );
    }

    /**
     * Language switcher showing available locales.
     *
     * <p>Each locale link includes a {@code hreflang} attribute and
     * displays the language name in its own language (e.g. "Espa\u00f1ol").</p>
     */
    private Element languageSwitcher() {
        var locales = messages.availableLocales();
        var current = messages.currentLocale();

        var links = locales.stream()
                .map(locale -> {
                    var link = a().attr("href", "?lang=" + locale.getLanguage())
                            .attr("hreflang", locale.getLanguage())
                            .cls("text-xs", "px-2", "py-1", "rounded", "transition-colors");
                    if (locale.equals(current)) {
                        link = link.cls("bg-indigo-100", "text-indigo-700", "font-medium")
                                .ariaCurrent("true");
                    } else {
                        link = link.cls("text-gray-500", "hover:text-indigo-600");
                    }
                    return link.text(locale.getLanguage().toUpperCase(Locale.ROOT));
                })
                .toList();

        return nav().aria("label", messages.getString("nav.language"))
                .cls("flex", "items-center", "gap-1")
                .children(links);
    }
}
