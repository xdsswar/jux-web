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
 * Browse all categories page.
 *
 * <p>Displays a grid of category cards, each linking to the
 * filtered product listing for that category.</p>
 */
@Route("/categories")
@Title("Shop by Category - JUX Store")
@Meta(name = "description", content = "Browse products by category")
public class CategoriesPage extends Page {

    @Override
    public Element render() {
        var m = messages();
        var categories = StoreData.allCategories();

        var cards = categories.stream()
                .map(c -> new CategoryCard(c).render())
                .toList();

        return new StoreLayout("/categories", m,
                div().children(
                        /* Page header */
                        section().cls("bg-gray-50", "py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                h1().cls("text-3xl", "font-bold", "text-gray-900")
                                                        .text(m.getString("categories.title")),
                                                p().cls("text-gray-500", "mt-1")
                                                        .text(m.getString("categories.subtitle"))
                                        )
                        ),
                        /* Category cards grid */
                        section().cls("py-12").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                div().cls("grid", "grid-cols-1", "sm:grid-cols-2",
                                                                "lg:grid-cols-3", "gap-8")
                                                        .children(cards)
                                        )
                        )
                )
        ).render();
    }
}
