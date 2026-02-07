/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.store.data.Product;

import static xss.it.jux.core.Elements.*;

/**
 * Product card for grid listings.
 *
 * <p>Renders a product as a card with image, name, price, rating,
 * and a link to the product detail page. The entire card is wrapped
 * in an {@code <article>} for semantic correctness.</p>
 *
 * <p>ADA: image has alt text, the product name is a heading
 * for screen reader navigation, and the card link text is
 * descriptive (the product name itself, not "click here").</p>
 */
public class ProductCard extends Component {

    private final Product product;

    public ProductCard(Product product) {
        this.product = product;
    }

    @Override
    public Element render() {
        var card = article().cls("bg-white", "rounded-xl", "shadow-sm", "border",
                        "border-gray-200", "hover:shadow-md", "transition-shadow",
                        "overflow-hidden", "flex", "flex-col")
                .children(
                        /* Product image with link */
                        a().attr("href", "/products/" + product.slug())
                                .cls("block", "relative", "overflow-hidden")
                                .children(
                                        img(product.image(), product.imageAlt())
                                                .cls("w-full", "h-48", "object-cover"),
                                        /* Sale badge overlay */
                                        product.onSale()
                                                ? span().cls("absolute", "top-2", "left-2", "bg-red-500",
                                                                "text-white", "text-xs", "font-bold", "px-2",
                                                                "py-1", "rounded-full")
                                                    .text("-" + product.discountPercent() + "%")
                                                : span().cls("hidden"),
                                        /* Out of stock overlay */
                                        !product.inStock()
                                                ? div().cls("absolute", "inset-0", "bg-black",
                                                                "bg-opacity-40", "flex", "items-center",
                                                                "justify-center")
                                                    .children(
                                                            span().cls("text-white", "font-bold", "text-sm",
                                                                    "bg-gray-900", "px-3", "py-1", "rounded")
                                                                    .text("Out of Stock"))
                                                : span().cls("hidden")
                                ),
                        /* Card body */
                        div().cls("p-4", "flex", "flex-col", "flex-1").children(
                                /* Product name as a link (descriptive link text) */
                                h3().cls("font-semibold", "text-gray-900", "mb-1").children(
                                        a().attr("href", "/products/" + product.slug())
                                                .cls("hover:text-indigo-600", "transition-colors")
                                                .text(product.name())
                                ),
                                /* Short description */
                                p().cls("text-sm", "text-gray-500", "mb-3", "line-clamp-2", "flex-1")
                                        .text(product.description()),
                                /* Rating stars */
                                div().cls("mb-2").children(
                                        new RatingStars(product.rating(), product.reviewCount()).render()
                                ),
                                /* Price display */
                                new PriceDisplay(product).render()
                        )
                );
        return card;
    }
}
