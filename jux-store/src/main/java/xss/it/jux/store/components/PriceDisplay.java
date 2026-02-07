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
import xss.it.jux.store.data.StoreData;

import static xss.it.jux.core.Elements.*;

/**
 * Displays a product price with optional sale styling.
 *
 * <p>When the product is on sale, the original price is shown with
 * a strikethrough, the current price is shown in a sale color,
 * and a discount badge is displayed.</p>
 *
 * <p>Accessibility: uses {@code <del>} for the original price
 * (screen readers announce "deleted text") and sr-only text to
 * clarify the pricing context.</p>
 */
public class PriceDisplay extends Component {

    private final Product product;

    public PriceDisplay(Product product) {
        this.product = product;
    }

    @Override
    public Element render() {
        if (product.onSale()) {
            /* Sale price layout: strikethrough original + highlighted current + badge */
            return div().cls("flex", "items-center", "gap-2").children(
                    /* Original price with <del> for semantic strikethrough (ADA: announced as deleted) */
                    Element.of("del").cls("text-gray-400", "text-sm")
                            .text(StoreData.formatPrice(product.originalPrice())),
                    /* Current sale price in red for visual emphasis */
                    span().cls("text-red-600", "font-bold", "text-lg")
                            .text(StoreData.formatPrice(product.price())),
                    /* Discount percentage badge */
                    span().cls("bg-red-100", "text-red-700", "text-xs", "font-semibold",
                                    "px-2", "py-0.5", "rounded-full")
                            .text("-" + product.discountPercent() + "%")
            );
        }
        /* Standard price display */
        return span().cls("text-gray-900", "font-bold", "text-lg")
                .text(StoreData.formatPrice(product.price()));
    }
}
