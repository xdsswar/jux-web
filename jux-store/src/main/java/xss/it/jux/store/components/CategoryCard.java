/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.store.data.Category;

import static xss.it.jux.core.Elements.*;

/**
 * Category browse card showing icon, name, description, and product count.
 *
 * <p>Used on the categories page and the home page category bar.
 * The entire card is a link to the category's product listing page.</p>
 */
public class CategoryCard extends Component {

    private final Category category;

    public CategoryCard(Category category) {
        this.category = category;
    }

    @Override
    public Element render() {
        return a().attr("href", "/categories/" + category.slug())
                .cls("block", "bg-white", "rounded-xl", "shadow-sm", "border",
                        "border-gray-200", "p-6", "hover:shadow-md",
                        "transition-shadow", "text-center", "group")
                .children(
                        /* Category icon */
                        div().cls("text-4xl", "mb-3").ariaHidden(true)
                                .text(category.icon()),
                        /* Category name */
                        h3().cls("font-semibold", "text-gray-900", "mb-1",
                                        "group-hover:text-indigo-600", "transition-colors")
                                .text(category.name()),
                        /* Short description */
                        p().cls("text-sm", "text-gray-500", "mb-2")
                                .text(category.description()),
                        /* Product count */
                        span().cls("text-xs", "text-indigo-600", "font-medium")
                                .text(category.productCount() + " products")
                );
    }
}
