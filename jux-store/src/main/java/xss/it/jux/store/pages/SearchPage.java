/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.store.components.*;
import xss.it.jux.store.data.StoreData;

import static xss.it.jux.core.Elements.*;

/**
 * Search page with search input and results grid.
 *
 * <p>Reads the search query from the {@code ?q=} query parameter.
 * Shows different states: empty query, no results, and results grid.</p>
 */
@Route("/search")
@Title("Search - JUX Store")
public class SearchPage extends Page {

    @Override
    public Element render() {
        var m = messages();
        String query = queryParam("q", "");

        return new StoreLayout("/search", m,
                div().children(
                        /* Search header with input */
                        section().cls("bg-gray-50", "py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-4")
                                                        .text(m.getString("search.title")),
                                                /* Search form */
                                                form().cls("max-w-xl").attr("method", "get")
                                                        .attr("action", "/search")
                                                        .children(
                                                                label().cls("jux-sr-only")
                                                                        .attr("for", "search-input")
                                                                        .text(m.getString("search.label")),
                                                                div().cls("flex", "gap-2").children(
                                                                        input().id("search-input")
                                                                                .cls("flex-1", "border",
                                                                                        "border-gray-300",
                                                                                        "rounded-lg", "px-4",
                                                                                        "py-3", "focus:ring-2",
                                                                                        "focus:ring-indigo-500",
                                                                                        "focus:border-indigo-500")
                                                                                .attr("type", "search")
                                                                                .attr("name", "q")
                                                                                .attr("value", query)
                                                                                .attr("placeholder",
                                                                                        m.getString("search.placeholder")),
                                                                        button().cls("bg-indigo-600", "text-white",
                                                                                        "px-6", "py-3", "rounded-lg",
                                                                                        "font-semibold",
                                                                                        "hover:bg-indigo-700")
                                                                                .attr("type", "submit")
                                                                                .text(m.getString("search.submit"))
                                                                )
                                                        )
                                        )
                        ),
                        /* Search results */
                        searchResults(query)
                )
        ).render();
    }

    private Element searchResults(String query) {
        var m = messages();

        /* Empty query state */
        if (query.isBlank()) {
            return section().cls("py-12").children(
                    div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                            .children(
                                    new EmptyState("\uD83D\uDD0D",
                                            m.getString("search.empty.title"),
                                            m.getString("search.empty.text"),
                                            null, null).render()
                            )
            );
        }

        var results = StoreData.search(query);

        /* No results state */
        if (results.isEmpty()) {
            return section().cls("py-12").children(
                    div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                            .children(
                                    new EmptyState("\uD83D\uDE14",
                                            m.getString("search.no_results.title"),
                                            m.getString("search.no_results.text", query),
                                            m.getString("search.no_results.cta"), "/products").render()
                            )
            );
        }

        /* Results grid */
        return section().cls("py-8").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        p().cls("text-gray-500", "mb-6")
                                .text(m.getString("search.results.found",
                                        String.valueOf(results.size()), query)),
                        new ProductGrid(results).render()
                )
        );
    }
}
