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

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Search page demonstrating {@code @QueryParam} injection and dynamic content.
 *
 * <p>The page reads the {@code q} query parameter from the URL, filters
 * a static dataset, and displays results or a helpful empty state.
 * This showcases JUX's parameter binding and conditional rendering.</p>
 */
@Route("/search")
@Title("Search - JUX Demo")
@Meta(name = "description", content = "Search the JUX framework documentation, modules, and blog posts")
public class SearchPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @QueryParam
    private String q;

    private record SearchItem(String title, String description, String category, String url) {}

    private static final List<SearchItem> ITEMS = List.of(
        new SearchItem("jux-core Module",
            "Component, Element, PageMeta, and the Elements factory class. The foundation of every JUX application.",
            "Module", "/about"),
        new SearchItem("jux-annotations Module",
            "Zero-dependency annotation library: @Route, @Title, @Css, @Js, @Meta, @QueryParam, and more.",
            "Module", "/about"),
        new SearchItem("jux-a11y Module",
            "WCAG 2.2 AA audit engine with 14+ built-in rules. Automatic accessibility validation on every render.",
            "Module", "/about"),
        new SearchItem("jux-i18n Module",
            "Multi-language support with type-safe message bundles, locale-aware routing, and RTL layout.",
            "Module", "/about"),
        new SearchItem("jux-server Module",
            "Spring Boot auto-configuration, SSR renderer, route engine, and Caffeine-backed HTML cache.",
            "Module", "/about"),
        new SearchItem("jux-themes Module",
            "Accessible UI components: Tabs, Accordion, Breadcrumb, Modal, Toast, TextField, Progress.",
            "Module", "/components"),
        new SearchItem("jux-cms Module",
            "Database-driven page builder with widget registry, drag-and-drop admin panel, and dynamic routing.",
            "Module", "/about"),
        new SearchItem("Getting Started with JUX",
            "Learn how to set up your first JUX project with Spring Boot and start building pages in pure Java.",
            "Blog", "/blog/getting-started"),
        new SearchItem("Accessibility-First Development",
            "How JUX enforces WCAG 2.2 AA compliance at the framework level and why that matters for your users.",
            "Blog", "/blog/accessibility-first"),
        new SearchItem("SSR Performance Under the Hood",
            "Deep dive into how JUX achieves sub-5ms server-side rendering with zero runtime reflection.",
            "Blog", "/blog/ssr-performance"),
        new SearchItem("Bootstrap 5 Integration",
            "Using Bootstrap CSS classes with JUX's .cls() method for rapid UI development without writing CSS.",
            "Blog", "/blog/bootstrap-integration"),
        new SearchItem("Element API Reference",
            "Complete reference for div(), section(), h1(), img(), form(), table() and all HTML5 element factories.",
            "Feature", "/components")
    );

    @Override
    public Element render() {
        return new PageLayout("/search", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
            headerSection(),
            searchSection()
        );
    }

    private Element headerSection() {
        var m = messages();
        return section().cls("bg-primary", "text-white", "py-5").children(
            div().cls("container", "py-3").children(
                h1().cls("display-5", "fw-bold").text(m.getString("search.header.title")),
                p().cls("lead", "mb-0", "opacity-75").text(m.getString("search.header.subtitle"))
            )
        );
    }

    private Element searchSection() {
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("row", "justify-content-center").children(
                    div().cls("col-lg-8").children(
                        searchForm(),
                        resultsArea()
                    )
                )
            )
        );
    }

    private Element searchForm() {
        var m = messages();
        return form().attr("method", "get").attr("action", "/search").cls("mb-4").children(
            div().cls("input-group", "input-group-lg").children(
                input().cls("form-control").attr("type", "search").attr("name", "q")
                    .attr("placeholder", m.getString("search.placeholder"))
                    .attr("value", q != null ? q : "")
                    .attr("autocomplete", "off")
                    .aria("label", "Search"),
                button().cls("btn", "btn-primary").attr("type", "submit").children(
                    Element.of("i").cls("bi", "bi-search").ariaHidden(true),
                    srOnly("Search")
                )
            )
        );
    }

    private Element resultsArea() {
        if (q == null || q.isBlank()) {
            return emptyState();
        }

        List<SearchItem> results = ITEMS.stream()
            .filter(item -> item.title().toLowerCase().contains(q.toLowerCase())
                || item.description().toLowerCase().contains(q.toLowerCase()))
            .toList();

        if (results.isEmpty()) {
            return noResults();
        }

        var m = messages();
        return div().children(
            p().cls("text-secondary", "mb-3").children(
                span().text(m.getString("search.results.found") + " "),
                strong().text(String.valueOf(results.size())),
                span().text(results.size() == 1 ? m.getString("search.results.single") : m.getString("search.results.multiple")),
                strong().text("\"" + q + "\"")
            ),
            div().cls("list-group").aria("label", "Search results").children(
                results.stream().map(this::resultItem).toList()
            )
        );
    }

    private Element resultItem(SearchItem item) {
        String badgeClass = switch (item.category()) {
            case "Module" -> "bg-primary";
            case "Blog" -> "bg-success";
            case "Feature" -> "bg-info text-dark";
            default -> "bg-secondary";
        };

        return a().cls("list-group-item", "list-group-item-action", "py-3")
            .attr("href", item.url())
            .children(
                div().cls("d-flex", "justify-content-between", "align-items-start", "mb-1").children(
                    h2().cls("h6", "fw-bold", "mb-0").text(item.title()),
                    span().cls("badge", badgeClass, "ms-2").text(item.category())
                ),
                p().cls("mb-0", "text-secondary", "small").text(item.description())
            );
    }

    private Element emptyState() {
        var m = messages();
        return div().cls("text-center", "py-5").children(
            Element.of("i").cls("bi", "bi-search", "text-secondary")
                .style("font-size", "3rem").ariaHidden(true),
            h2().cls("h5", "mt-3", "text-secondary").text(m.getString("search.empty.title")),
            p().cls("text-muted", "mb-4").text(m.getString("search.empty.text")),
            div().cls("d-flex", "flex-wrap", "justify-content-center", "gap-2").children(
                suggestionBadge("accessibility"),
                suggestionBadge("SSR"),
                suggestionBadge("i18n"),
                suggestionBadge("components"),
                suggestionBadge("routing"),
                suggestionBadge("Bootstrap")
            )
        );
    }

    private Element suggestionBadge(String term) {
        return a().cls("badge", "bg-light", "text-dark", "border", "text-decoration-none", "px-3", "py-2")
            .attr("href", "/search?q=" + term)
            .text(term);
    }

    private Element noResults() {
        var m = messages();
        return div().cls("text-center", "py-5").children(
            Element.of("i").cls("bi", "bi-emoji-frown", "text-secondary")
                .style("font-size", "3rem").ariaHidden(true),
            h2().cls("h5", "mt-3", "text-secondary").text(m.getString("search.no_results.title")),
            p().cls("text-muted", "mb-3").children(
                span().text(m.getString("search.no_results.pre") + " "),
                strong().text("\"" + q + "\""),
                span().text(m.getString("search.no_results.post"))
            ),
            div().cls("d-flex", "flex-wrap", "justify-content-center", "gap-2").children(
                suggestionBadge("core"),
                suggestionBadge("themes"),
                suggestionBadge("Java"),
                suggestionBadge("Spring Boot")
            )
        );
    }
}
