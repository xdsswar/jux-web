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
import xss.it.jux.theme.JuxPagination;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Product catalog page with grid, category filter pills, sort dropdown, and pagination.
 *
 * <p>Supports query parameters for filtering and sorting:</p>
 * <ul>
 *   <li>{@code ?category=electronics} — filter by category</li>
 *   <li>{@code ?sort=price-asc} — sort by price ascending</li>
 *   <li>{@code ?page=2} — pagination</li>
 * </ul>
 */
@Route("/products")
@Title("All Products - JUX Store")
@Meta(name = "description", content = "Browse our full product catalog")
public class ProductCatalogPage extends Page {

    private static final int PAGE_SIZE = 8;

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .ogTitle("All Products - JUX Store")
                .ogType("website");
    }

    @Override
    public Element render() {
        var m = messages();

        /* Read query parameters with defaults */
        String category = queryParam("category", "");
        String sort = queryParam("sort", "newest");
        int page = parseIntOrDefault(queryParam("page", "1"), 1);

        /* Filter, sort, and paginate products */
        var allProducts = category.isEmpty()
                ? StoreData.allProducts()
                : StoreData.productsByCategory(category);
        var sorted = StoreData.sort(allProducts, sort);
        int totalPages = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));
        page = Math.min(page, totalPages);
        var pageProducts = StoreData.paginate(sorted, page, PAGE_SIZE);

        /* Build base URL for pagination links */
        var baseUrl = "/products?sort=" + sort
                + (category.isEmpty() ? "" : "&category=" + category)
                + "&page=";

        return new StoreLayout("/products", m,
                div().children(
                        /* Page header */
                        section().cls("bg-gray-50", "py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                h1().cls("text-3xl", "font-bold", "text-gray-900")
                                                        .text(m.getString("catalog.title")),
                                                p().cls("text-gray-500", "mt-1")
                                                        .text(m.getString("catalog.subtitle",
                                                                String.valueOf(allProducts.size())))
                                        )
                        ),
                        /* Filters and grid */
                        section().cls("py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                /* Category filter pills + sort dropdown */
                                                filtersBar(category, sort),
                                                /* Product grid */
                                                div().cls("mt-6").children(
                                                        new ProductGrid(pageProducts).render()
                                                ),
                                                /* Pagination */
                                                totalPages > 1
                                                        ? div().cls("mt-8", "flex", "justify-center")
                                                            .children(new JuxPagination(page, totalPages, baseUrl).render())
                                                        : span()
                                        )
                        )
                )
        ).render();
    }

    /**
     * Category filter pills and sort dropdown.
     */
    private Element filtersBar(String activeCategory, String activeSort) {
        var m = messages();
        var categories = StoreData.allCategories();

        /* Category pills */
        var pills = new java.util.ArrayList<Element>();
        pills.add(filterPill("", m.getString("catalog.filter.all"), activeCategory));
        for (var cat : categories) {
            pills.add(filterPill(cat.slug(), cat.name(), activeCategory));
        }

        return div().cls("flex", "flex-col", "sm:flex-row", "sm:items-center",
                        "sm:justify-between", "gap-4")
                .children(
                        /* Category pills */
                        div().cls("flex", "flex-wrap", "gap-2").role("group")
                                .aria("label", m.getString("catalog.filter.label"))
                                .children(pills),
                        /* Sort dropdown */
                        div().cls("flex", "items-center", "gap-2").children(
                                label().cls("text-sm", "text-gray-500")
                                        .attr("for", "sort-select")
                                        .text(m.getString("catalog.sort.label")),
                                select().id("sort-select")
                                        .cls("border", "border-gray-300", "rounded-lg", "px-3",
                                                "py-2", "text-sm", "focus:ring-2",
                                                "focus:ring-indigo-500")
                                        .attr("name", "sort")
                                        .attr("onchange", "window.location.href='/products?sort='+this.value"
                                                + (activeCategory.isEmpty() ? "" : "+'&category=" + activeCategory + "'"))
                                        .children(
                                                sortOption("newest", m.getString("catalog.sort.newest"), activeSort),
                                                sortOption("price-asc", m.getString("catalog.sort.price_asc"), activeSort),
                                                sortOption("price-desc", m.getString("catalog.sort.price_desc"), activeSort),
                                                sortOption("rating", m.getString("catalog.sort.rating"), activeSort),
                                                sortOption("name", m.getString("catalog.sort.name"), activeSort)
                                        )
                        )
                );
    }

    private Element filterPill(String categorySlug, String label, String activeCategory) {
        boolean isActive = categorySlug.equals(activeCategory);
        var link = a().attr("href", "/products" + (categorySlug.isEmpty() ? "" : "?category=" + categorySlug))
                .cls("px-4", "py-2", "rounded-full", "text-sm", "font-medium", "transition-colors");
        if (isActive) {
            link = link.cls("bg-indigo-600", "text-white");
        } else {
            link = link.cls("bg-gray-100", "text-gray-700", "hover:bg-gray-200");
        }
        return link.text(label);
    }

    private Element sortOption(String value, String label, String activeSort) {
        var opt = option().attr("value", value).text(label);
        if (value.equals(activeSort)) {
            opt = opt.attr("selected", "selected");
        }
        return opt;
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
