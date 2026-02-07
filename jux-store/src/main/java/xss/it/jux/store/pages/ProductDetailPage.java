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
import xss.it.jux.store.data.Product;
import xss.it.jux.store.data.StoreData;
import xss.it.jux.theme.JuxBreadcrumb;
import xss.it.jux.theme.JuxTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static xss.it.jux.core.Elements.*;

/**
 * Product detail page showing full product information.
 *
 * <p>Demonstrates @PathParam injection, dynamic PageMeta from data,
 * JuxBreadcrumb for navigation context, and JuxTabs for organized content.</p>
 */
@Route("/products/{slug}")
public class ProductDetailPage extends Page {

    @PathParam
    private String slug;

    @Override
    public PageMeta pageMeta() {
        var product = StoreData.findProduct(slug).orElse(null);
        if (product == null) {
            return PageMeta.create().status(404).title("Product Not Found - JUX Store");
        }
        return PageMeta.create()
                .title(product.name() + " - JUX Store")
                .description(product.description())
                .ogTitle(product.name())
                .ogDescription(product.description())
                .ogImage(product.image())
                .ogImageAlt(product.imageAlt())
                .ogType("product");
    }

    @Override
    public Element render() {
        var m = messages();
        var product = StoreData.findProduct(slug).orElse(null);

        if (product == null) {
            return new StoreLayout("/products", m,
                    new EmptyState("\uD83D\uDD0D", m.getString("product.notfound.title"),
                            m.getString("product.notfound.text"),
                            m.getString("product.notfound.btn"), "/products").render()
            ).render();
        }

        return new StoreLayout("/products", m,
                div().children(
                        breadcrumbSection(product),
                        productSection(product),
                        tabsSection(product),
                        relatedSection(product)
                )
        ).render();
    }

    private Element breadcrumbSection(Product product) {
        var m = messages();
        var categoryName = StoreData.findCategory(product.category())
                .map(c -> c.name()).orElse(product.category());

        return section().cls("bg-gray-50", "py-3").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        new JuxBreadcrumb(List.of(
                                new JuxBreadcrumb.Crumb(m.getString("nav.home"), "/"),
                                new JuxBreadcrumb.Crumb(m.getString("nav.products"), "/products"),
                                new JuxBreadcrumb.Crumb(categoryName, "/categories/" + product.category()),
                                new JuxBreadcrumb.Crumb(product.name(), null)
                        )).render()
                )
        );
    }

    /**
     * Main product section: image + product info side by side.
     */
    private Element productSection(Product product) {
        var m = messages();
        return section().cls("py-8").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        div().cls("grid", "grid-cols-1", "lg:grid-cols-2", "gap-10").children(
                                /* Product image */
                                div().children(
                                        img(product.image(), product.imageAlt())
                                                .cls("w-full", "rounded-xl", "shadow-sm")
                                ),
                                /* Product info */
                                div().children(
                                        h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-3")
                                                .text(product.name()),
                                        div().cls("mb-4").children(
                                                new RatingStars(product.rating(), product.reviewCount()).render()
                                        ),
                                        div().cls("mb-4").children(
                                                new PriceDisplay(product).render()
                                        ),
                                        p().cls("text-gray-600", "mb-6").text(product.description()),
                                        /* Stock status */
                                        div().cls("mb-6").children(
                                                product.inStock()
                                                        ? span().cls("text-green-600", "font-medium")
                                                            .text("\u2713 " + m.getString("product.in_stock"))
                                                        : span().cls("text-red-600", "font-medium")
                                                            .text("\u2717 " + m.getString("product.out_of_stock"))
                                        ),
                                        /* Add to cart button */
                                        a().cls("inline-block", "bg-indigo-600", "text-white",
                                                        "px-8", "py-3", "rounded-lg", "font-semibold",
                                                        "hover:bg-indigo-700", "focus:ring-2",
                                                        "focus:ring-indigo-500", "focus:ring-offset-2",
                                                        "transition-colors")
                                                .attr("href", "/cart")
                                                .text(m.getString("product.add_to_cart")),
                                        /* SKU */
                                        p().cls("text-sm", "text-gray-400", "mt-4")
                                                .text("SKU: " + product.sku())
                                )
                        )
                )
        );
    }

    /**
     * Tabbed content: Description, Specifications, Reviews.
     */
    private Element tabsSection(Product product) {
        var m = messages();
        var reviews = StoreData.reviewsFor(product.slug());

        /* Build specs table */
        var specRows = product.specs().entrySet().stream()
                .map(e -> tr().children(
                        td().cls("py-2", "pr-8", "font-medium", "text-gray-700").text(e.getKey()),
                        td().cls("py-2", "text-gray-500").text(e.getValue())
                ))
                .toList();

        var specsContent = table().cls("w-full").children(
                caption().cls("jux-sr-only").text(m.getString("product.specs.caption")),
                thead().children(
                        tr().children(
                                th().attr("scope", "col").cls("jux-sr-only").text(m.getString("product.specs.feature")),
                                th().attr("scope", "col").cls("jux-sr-only").text(m.getString("product.specs.value"))
                        )
                ),
                Element.of("tbody").children(specRows)
        );

        /* Build reviews content */
        Element reviewsContent;
        if (reviews.isEmpty()) {
            reviewsContent = p().cls("text-gray-500").text(m.getString("product.no_reviews"));
        } else {
            var reviewCards = reviews.stream()
                    .map(r -> new ReviewCard(r).render())
                    .toList();
            reviewsContent = div().children(reviewCards);
        }

        var tabs = List.of(
                new JuxTabs.Tab(m.getString("product.tab.description"),
                        div().cls("prose", "max-w-none").children(
                                p().cls("text-gray-600", "leading-relaxed").text(product.longDescription())
                        )),
                new JuxTabs.Tab(m.getString("product.tab.specs"), specsContent),
                new JuxTabs.Tab(m.getString("product.tab.reviews") + " (" + reviews.size() + ")",
                        reviewsContent)
        );

        return section().cls("py-8", "border-t", "border-gray-200").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        new JuxTabs(tabs).render()
                )
        );
    }

    /**
     * Related products section (same category, excluding current).
     */
    private Element relatedSection(Product product) {
        var m = messages();
        var related = StoreData.relatedProducts(product.slug(), 4);
        if (related.isEmpty()) return span();

        return section().cls("bg-gray-50", "py-12").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        h2().cls("text-2xl", "font-bold", "text-gray-900", "mb-6")
                                .text(m.getString("product.related")),
                        new ProductGrid(related).render()
                )
        );
    }
}
