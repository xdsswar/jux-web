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
import xss.it.jux.theme.JuxBreadcrumb;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Filtered product listing for a single category.
 *
 * <p>Demonstrates @PathParam with a category slug, breadcrumb navigation,
 * and 404 handling when the category doesn't exist.</p>
 */
@Route("/categories/{slug}")
public class CategoryPage extends Page {

    @PathParam
    private String slug;

    @Override
    public PageMeta pageMeta() {
        var category = StoreData.findCategory(slug).orElse(null);
        if (category == null) {
            return PageMeta.create().status(404).title("Category Not Found - JUX Store");
        }
        return PageMeta.create()
                .title(category.name() + " - JUX Store")
                .description(category.description());
    }

    @Override
    public Element render() {
        var m = messages();
        var category = StoreData.findCategory(slug).orElse(null);

        if (category == null) {
            return new StoreLayout("/categories", m,
                    new EmptyState("\uD83D\uDCC2", m.getString("category.notfound.title"),
                            m.getString("category.notfound.text"),
                            m.getString("category.notfound.btn"), "/categories").render()
            ).render();
        }

        var products = StoreData.productsByCategory(slug);

        return new StoreLayout("/categories", m,
                div().children(
                        /* Breadcrumb */
                        section().cls("bg-gray-50", "py-3").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                new JuxBreadcrumb(List.of(
                                                        new JuxBreadcrumb.Crumb(m.getString("nav.home"), "/"),
                                                        new JuxBreadcrumb.Crumb(m.getString("nav.categories"), "/categories"),
                                                        new JuxBreadcrumb.Crumb(category.name(), null)
                                                )).render()
                                        )
                        ),
                        /* Category header */
                        section().cls("bg-gray-50", "pb-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                div().cls("flex", "items-center", "gap-3").children(
                                                        span().cls("text-4xl").ariaHidden(true)
                                                                .text(category.icon()),
                                                        div().children(
                                                                h1().cls("text-3xl", "font-bold",
                                                                                "text-gray-900")
                                                                        .text(category.name()),
                                                                p().cls("text-gray-500")
                                                                        .text(category.description())
                                                        )
                                                )
                                        )
                        ),
                        /* Product grid */
                        section().cls("py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                products.isEmpty()
                                                        ? new EmptyState("\uD83D\uDCE6",
                                                                m.getString("category.empty.title"),
                                                                m.getString("category.empty.text"),
                                                                null, null).render()
                                                        : new ProductGrid(products).render()
                                        )
                        )
                )
        ).render();
    }
}
