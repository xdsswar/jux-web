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

package xss.it.jux.demo.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.demo.components.PageLayout;
import xss.it.jux.theme.*;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Showcase page for JUX theme components and Element API features.
 */
@Route("/components")
@Title("Components - JUX Demo")
@Meta(name = "description", content = "JUX framework component showcase - tabs, accordion, breadcrumbs, forms, and more")
public class ComponentsPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        return new PageLayout("/components", messages(), pageContent()).render();
    }

    private Element pageContent() {
        var m = messages();
        return div().children(
            section().cls("bg-primary", "text-white", "py-5").children(
                div().cls("container", "py-3").children(
                    h1().cls("display-5", "fw-bold").text(m.getString("components.header.title")),
                    p().cls("lead", "mb-0", "opacity-75").text(m.getString("components.header.subtitle"))
                )
            ),
            section().cls("py-5").children(
                div().cls("container").children(
                    div().cls("row").children(
                        div().cls("col-lg-3", "d-none", "d-lg-block").children(sideNav()),
                        div().cls("col-lg-9").children(
                            breadcrumbDemo(),
                            tabsDemo(),
                            accordionDemo(),
                            formFieldsDemo(),
                            progressDemo(),
                            toastDemo(),
                            modalDemo(),
                            dropdownDemo(),
                            paginationDemo(),
                            elementsDemo(),
                            tableDemo()
                        )
                    )
                )
            )
        );
    }

    private Element sideNav() {
        return nav().cls("sticky-top").style("top", "1rem").aria("label", "Component navigation").children(
            div().cls("list-group", "list-group-flush").children(
                sideLink("#breadcrumbs", "Breadcrumbs"),
                sideLink("#tabs", "Tabs"),
                sideLink("#accordion", "Accordion"),
                sideLink("#form-fields", "Form Fields"),
                sideLink("#progress", "Progress"),
                sideLink("#toasts", "Toasts"),
                sideLink("#modal", "Modal"),
                sideLink("#dropdown", "Dropdown"),
                sideLink("#pagination", "Pagination"),
                sideLink("#elements", "Element API"),
                sideLink("#table", "Data Table")
            )
        );
    }

    private Element sideLink(String href, String text) {
        return a().cls("list-group-item", "list-group-item-action", "border-0")
            .attr("href", href).text(text);
    }

    // ── Breadcrumb ──────────────────────────────────────────

    private Element breadcrumbDemo() {
        var m = messages();
        return componentSection("breadcrumbs", m.getString("components.breadcrumbs.title"),
            m.getString("components.breadcrumbs.desc"),
            new JuxBreadcrumb(List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("Components", "/components"),
                new JuxBreadcrumb.Crumb("Breadcrumbs", null)
            )).render()
        );
    }

    // ── Tabs ────────────────────────────────────────────────

    private Element tabsDemo() {
        var m = messages();
        return componentSection("tabs", m.getString("components.tabs.title"),
            m.getString("components.tabs.desc"),
            new JuxTabs(List.of(
                new JuxTabs.Tab(m.getString("components.tabs.overview"),
                    div().cls("p-3").children(
                        p().text(m.getString("components.tabs.overview.text"))
                    )),
                new JuxTabs.Tab(m.getString("components.tabs.usage"),
                    div().cls("p-3").children(
                        pre().cls("bg-light", "p-3", "rounded").children(
                            code().text(
                                "new JuxTabs(List.of(\n" +
                                "  new JuxTabs.Tab(\"Tab 1\", content1),\n" +
                                "  new JuxTabs.Tab(\"Tab 2\", content2)\n" +
                                "))")
                        )
                    )),
                new JuxTabs.Tab(m.getString("components.tabs.accessibility"),
                    div().cls("p-3").children(
                        ul().children(
                            li().text(m.getString("components.tabs.a11y.tablist")),
                            li().text(m.getString("components.tabs.a11y.tab")),
                            li().text(m.getString("components.tabs.a11y.panel")),
                            li().text(m.getString("components.tabs.a11y.keys"))
                        )
                    ))
            )).render()
        );
    }

    // ── Accordion ───────────────────────────────────────────

    private Element accordionDemo() {
        var m = messages();
        return componentSection("accordion", m.getString("components.accordion.title"),
            m.getString("components.accordion.desc"),
            new JuxAccordion(List.of(
                new JuxAccordion.Section(m.getString("components.accordion.q1"),
                    p().text(m.getString("components.accordion.a1"))),
                new JuxAccordion.Section(m.getString("components.accordion.q2"),
                    p().text(m.getString("components.accordion.a2"))),
                new JuxAccordion.Section(m.getString("components.accordion.q3"),
                    p().text(m.getString("components.accordion.a3")))
            )).render()
        );
    }

    // ── Form Fields ─────────────────────────────────────────

    private Element formFieldsDemo() {
        var m = messages();
        return componentSection("form-fields", m.getString("components.form.title"),
            m.getString("components.form.desc"),
            div().cls("row", "g-3").children(
                div().cls("col-md-6").children(
                    new JuxTextField("username", "Username")
                        .setPlaceholder("Enter your username")
                        .setRequired(true)
                        .setHelpText("Choose a unique username")
                        .render()
                ),
                div().cls("col-md-6").children(
                    new JuxTextField("email", "Email Address")
                        .setType("email")
                        .setPlaceholder("you@example.com")
                        .setRequired(true)
                        .render()
                ),
                div().cls("col-md-6").children(
                    new JuxTextField("password", "Password")
                        .setType("password")
                        .setPlaceholder("Min. 8 characters")
                        .setHelpText("Use letters, numbers, and symbols")
                        .render()
                ),
                div().cls("col-md-6").children(
                    new JuxTextField("website", "Website")
                        .setType("url")
                        .setPlaceholder("https://example.com")
                        .setInvalid(true)
                        .setErrorMessage("Please enter a valid URL")
                        .render()
                )
            )
        );
    }

    // ── Progress ────────────────────────────────────────────

    private Element progressDemo() {
        var m = messages();
        return componentSection("progress", m.getString("components.progress.title"),
            m.getString("components.progress.desc"),
            div().cls("d-flex", "flex-column", "gap-3").children(
                new JuxProgress(25, "Upload progress").render(),
                new JuxProgress(60, "Build progress").render(),
                new JuxProgress(100, "Complete").render()
            )
        );
    }

    // ── Toast ───────────────────────────────────────────────

    private Element toastDemo() {
        var m = messages();
        return componentSection("toasts", m.getString("components.toasts.title"),
            m.getString("components.toasts.desc"),
            div().cls("d-flex", "flex-column", "gap-2").children(
                new JuxToast("Changes saved successfully!", JuxToast.ToastType.SUCCESS).render(),
                new JuxToast("New update available.", JuxToast.ToastType.INFO).render(),
                new JuxToast("Storage almost full.", JuxToast.ToastType.WARNING).render(),
                new JuxToast("Failed to connect to server.", JuxToast.ToastType.ERROR).render()
            )
        );
    }

    // ── Modal ──────────────────────────────────────────────

    private Element modalDemo() {
        var m = messages();
        var modal = new JuxModal("Example Modal",
            div().children(
                p().text(m.getString("components.modal.body.p1")),
                p().text(m.getString("components.modal.body.p2"))
            )
        );
        return componentSection("modal", m.getString("components.modal.title"),
            m.getString("components.modal.desc"),
            div().children(
                button().attr("type", "button")
                    .cls("btn", "btn-primary", "mb-3")
                    .attr("data-bs-toggle", "modal")
                    .attr("data-bs-target", "#" + modal.getDialogId())
                    .text(m.getString("components.modal.open")),
                modal.render()
            )
        );
    }

    // ── Dropdown ───────────────────────────────────────────

    private Element dropdownDemo() {
        var m = messages();
        return componentSection("dropdown", m.getString("components.dropdown.title"),
            m.getString("components.dropdown.desc"),
            div().cls("d-flex", "gap-3", "flex-wrap").children(
                new JuxDropdown("Actions", List.of(
                    new JuxDropdown.DropdownItem("Edit Profile", "/edit"),
                    new JuxDropdown.DropdownItem("Settings", "/settings"),
                    new JuxDropdown.DropdownItem("Help Center", "/help"),
                    new JuxDropdown.DropdownItem("Sign Out", "/logout")
                )).render(),
                new JuxDropdown("Sort By", List.of(
                    new JuxDropdown.DropdownItem("Newest First", "?sort=newest"),
                    new JuxDropdown.DropdownItem("Oldest First", "?sort=oldest"),
                    new JuxDropdown.DropdownItem("Most Popular", "?sort=popular")
                )).render()
            )
        );
    }

    // ── Pagination ─────────────────────────────────────────

    private Element paginationDemo() {
        var m = messages();
        return componentSection("pagination", m.getString("components.pagination.title"),
            m.getString("components.pagination.desc"),
            div().cls("d-flex", "flex-column", "gap-4").children(
                div().children(
                    h3().cls("h6", "fw-bold", "mb-2").text(m.getString("components.pagination.page", 3, 10)),
                    new JuxPagination(3, 10, "/blog").render()
                ),
                div().children(
                    h3().cls("h6", "fw-bold", "mb-2").text(m.getString("components.pagination.first")),
                    new JuxPagination(1, 5, "/gallery").render()
                ),
                div().children(
                    h3().cls("h6", "fw-bold", "mb-2").text(m.getString("components.pagination.last")),
                    new JuxPagination(8, 8, "/search").render()
                )
            )
        );
    }

    // ── Element API Demo ────────────────────────────────────

    private Element elementsDemo() {
        var m = messages();
        return componentSection("elements", m.getString("components.elements.title"),
            m.getString("components.elements.desc"),
            div().children(
                h3().cls("h6", "fw-bold", "mb-3").text(m.getString("components.elements.semantic")),
                div().cls("bg-light", "p-3", "rounded", "mb-4").children(
                    nav().aria("label", "Example navigation").children(
                        ul().cls("list-inline", "mb-0").children(
                            li().cls("list-inline-item").children(
                                a().attr("href", "#").text("Docs")),
                            li().cls("list-inline-item").children(
                                a().attr("href", "#").text("API")),
                            li().cls("list-inline-item").children(
                                a().attr("href", "#").text("Examples"))
                        )
                    )
                ),
                h3().cls("h6", "fw-bold", "mb-3").text(m.getString("components.elements.images")),
                div().cls("row", "g-3", "mb-4").children(
                    div().cls("col-auto").children(
                        img("https://picsum.photos/seed/jux1/200/150", "Mountain landscape at sunset")
                            .cls("rounded", "shadow-sm")
                    ),
                    div().cls("col-auto").children(
                        imgDecorative("https://picsum.photos/seed/jux2/200/150")
                            .cls("rounded", "shadow-sm")
                    )
                ),
                p().cls("text-secondary", "small").text(m.getString("components.elements.images.note")),
                h3().cls("h6", "fw-bold", "mb-3").text(m.getString("components.elements.a11y")),
                div().cls("bg-light", "p-3", "rounded").children(
                    srOnly("This text is only visible to screen readers"),
                    liveRegion("polite").children(
                        p().cls("mb-0").text(m.getString("components.elements.live"))
                    )
                )
            )
        );
    }

    // ── Table ───────────────────────────────────────────────

    private Element tableDemo() {
        var m = messages();
        return componentSection("table", m.getString("components.table.title"),
            m.getString("components.table.desc"),
            table().cls("table", "table-striped", "table-hover").children(
                caption().text(m.getString("components.table.caption")),
                thead().children(
                    tr().children(
                        th().attr("scope", "col").text(m.getString("components.table.col.module")),
                        th().attr("scope", "col").text(m.getString("components.table.col.package")),
                        th().attr("scope", "col").text(m.getString("components.table.col.depends")),
                        th().attr("scope", "col").text(m.getString("components.table.col.status"))
                    )
                ),
                Element.of("tbody").children(
                    tableRow("jux-annotations", "xss.it.jux.annotation", "none", "Stable"),
                    tableRow("jux-core", "xss.it.jux.core", "annotations", "Stable"),
                    tableRow("jux-a11y", "xss.it.jux.a11y", "core", "Stable"),
                    tableRow("jux-i18n", "xss.it.jux.i18n", "core", "Stable"),
                    tableRow("jux-server", "xss.it.jux.server", "core, a11y, i18n", "Stable"),
                    tableRow("jux-themes", "xss.it.jux.theme", "core, a11y", "Stable"),
                    tableRow("jux-client", "xss.it.jux.client", "core, a11y", "Structural"),
                    tableRow("jux-cms", "xss.it.jux.cms", "server, a11y, i18n", "Structural")
                )
            )
        );
    }

    private Element tableRow(String module, String pkg, String deps, String status) {
        String badgeCls = "Stable".equals(status) ? "bg-success" : "bg-warning text-dark";
        return tr().children(
            th().attr("scope", "row").children(code().text(module)),
            td().children(code().cls("text-secondary").text(pkg)),
            td().text(deps),
            td().children(span().cls("badge", badgeCls).text(status))
        );
    }

    // ── Shared Section Wrapper ──────────────────────────────

    private Element componentSection(String id, String title, String description, Element content) {
        return section().id(id).cls("mb-5", "pb-4", "border-bottom").children(
            h2().cls("h4", "fw-bold", "mb-2").text(title),
            p().cls("text-secondary", "mb-4").text(description),
            content
        );
    }
}
