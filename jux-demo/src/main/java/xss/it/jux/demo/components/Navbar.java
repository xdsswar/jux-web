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

package xss.it.jux.demo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import java.util.Locale;

import static xss.it.jux.core.Elements.*;

/**
 * Bootstrap 5 navigation bar with brand, page links, and language switcher.
 */
public class Navbar extends Component {

    private final String activePath;
    private final JuxMessages messages;

    public Navbar(String activePath, JuxMessages messages) {
        this.activePath = activePath;
        this.messages = messages;
    }

    @Override
    public Element render() {
        return nav().cls("navbar", "navbar-expand-lg", "navbar-dark", "bg-dark", "shadow-sm").children(
            div().cls("container").children(
                a().cls("navbar-brand", "fw-bold").attr("href", "/").text(messages.getString("nav.brand")),
                button().cls("navbar-toggler")
                    .attr("type", "button")
                    .attr("data-bs-toggle", "collapse")
                    .attr("data-bs-target", "#navbarNav")
                    .aria("controls", "navbarNav")
                    .ariaExpanded(false)
                    .aria("label", messages.getString("nav.toggle"))
                    .children(span().cls("navbar-toggler-icon")),
                div().cls("collapse", "navbar-collapse").id("navbarNav").children(
                    ul().cls("navbar-nav", "ms-auto").children(
                        navItem(messages.getString("nav.home"), "/"),
                        navItem(messages.getString("nav.about"), "/about"),
                        navItem(messages.getString("nav.blog"), "/blog"),
                        navItem(messages.getString("nav.components"), "/components"),
                        navItem(messages.getString("nav.dashboard"), "/dashboard"),
                        navItem(messages.getString("nav.pricing"), "/pricing"),
                        navItem(messages.getString("nav.gallery"), "/gallery"),
                        navItem(messages.getString("nav.contact"), "/contact")
                    ),
                    div().cls("d-flex", "align-items-center", "gap-2").children(
                        a().cls("btn", "btn-outline-light", "btn-sm").attr("href", "/search").children(
                            Element.of("i").cls("bi", "bi-search", "me-1"),
                            span().text(messages.getString("nav.search"))
                        ),
                        a().cls("btn", "btn-light", "btn-sm").attr("href", "/login").children(
                            Element.of("i").cls("bi", "bi-person", "me-1"),
                            span().text(messages.getString("nav.login"))
                        ),
                        languageSwitcher()
                    )
                )
            )
        );
    }

    private Element navItem(String label, String href) {
        boolean active = href.equals(activePath);
        var link = a().cls("nav-link").attr("href", href).text(label);
        if (active) {
            link = link.cls("active").ariaCurrent("page");
        }
        return li().cls("nav-item").children(link);
    }

    private Element languageSwitcher() {
        Locale current = messages.currentLocale();
        String currentLabel = current.getLanguage().toUpperCase();

        return div().cls("dropdown").children(
            button().cls("btn", "btn-outline-light", "btn-sm", "dropdown-toggle")
                .attr("type", "button")
                .attr("data-bs-toggle", "dropdown")
                .ariaExpanded(false)
                .aria("label", messages.getString("nav.language"))
                .children(
                    Element.of("i").cls("bi", "bi-translate", "me-1").ariaHidden(true),
                    span().text(currentLabel)
                ),
            ul().cls("dropdown-menu", "dropdown-menu-end").children(
                messages.availableLocales().stream().map(locale -> {
                    boolean isActive = locale.getLanguage().equals(current.getLanguage());
                    String label = locale.getDisplayLanguage(locale);
                    // Capitalize first letter
                    label = label.substring(0, 1).toUpperCase() + label.substring(1);
                    var link = a().cls("dropdown-item")
                        .attr("href", activePath + "?lang=" + locale.getLanguage())
                        .text(label);
                    if (isActive) {
                        link = link.cls("active").ariaCurrent("true");
                    }
                    return li().children(link);
                }).toList()
            )
        );
    }
}
