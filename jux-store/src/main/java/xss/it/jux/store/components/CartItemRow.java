/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.store.data.CartItem;
import xss.it.jux.store.data.StoreData;

import static xss.it.jux.core.Elements.*;

/**
 * Renders a single cart line item row.
 *
 * <p>Shows the product image, name, price, quantity display,
 * and subtotal. Each row is an {@code <article>} for semantic
 * grouping. The product name links to its detail page.</p>
 */
public class CartItemRow extends Component {

    private final CartItem item;

    public CartItemRow(CartItem item) {
        this.item = item;
    }

    @Override
    public Element render() {
        var product = item.product();

        return article().cls("flex", "items-center", "gap-4", "py-4", "border-b",
                        "border-gray-200")
                .children(
                        /* Product thumbnail */
                        a().attr("href", "/products/" + product.slug())
                                .cls("flex-shrink-0")
                                .children(
                                        img(product.image(), product.imageAlt())
                                                .cls("w-20", "h-20", "object-cover", "rounded-lg")
                                ),
                        /* Product info */
                        div().cls("flex-1", "min-w-0").children(
                                h3().cls("font-medium", "text-gray-900", "truncate").children(
                                        a().attr("href", "/products/" + product.slug())
                                                .cls("hover:text-indigo-600")
                                                .text(product.name())
                                ),
                                p().cls("text-sm", "text-gray-500").text("SKU: " + product.sku()),
                                span().cls("text-sm", "font-medium", "text-gray-700")
                                        .text(StoreData.formatPrice(product.price()) + " each")
                        ),
                        /* Quantity display */
                        div().cls("flex", "items-center", "gap-2").children(
                                span().cls("text-sm", "text-gray-500").text("Qty:"),
                                span().cls("font-medium", "text-gray-900")
                                        .text(String.valueOf(item.quantity()))
                        ),
                        /* Subtotal */
                        div().cls("text-right").children(
                                span().cls("font-bold", "text-gray-900")
                                        .text(StoreData.formatPrice(item.subtotal()))
                        )
                );
    }
}
