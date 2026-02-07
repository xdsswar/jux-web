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

package xss.it.jux.store.data;

import java.util.List;
import java.util.Map;

/**
 * A product in the store catalog.
 *
 * <p>This record holds all information needed to display a product on
 * listing pages, detail pages, and in cart/checkout views. Products are
 * organized by category and can be marked as featured for homepage display.</p>
 *
 * @param id              unique numeric identifier
 * @param slug            URL-friendly identifier (e.g. "wireless-headphones")
 * @param name            display name
 * @param description     short description for card views (1-2 sentences)
 * @param longDescription detailed description for the product detail page
 * @param price           current selling price in USD
 * @param originalPrice   original price before discount (0.0 if no discount)
 * @param image           placeholder image URL path
 * @param imageAlt        alt text describing the product image (ADA required)
 * @param category        category slug this product belongs to
 * @param rating          average customer rating (1.0 to 5.0)
 * @param reviewCount     total number of customer reviews
 * @param inStock         whether the product is currently available
 * @param featured        whether to show on the homepage featured section
 * @param tags            searchable keyword tags
 * @param sku             stock keeping unit identifier
 * @param specs           technical specifications as key-value pairs
 */
public record Product(
        long id,
        String slug,
        String name,
        String description,
        String longDescription,
        double price,
        double originalPrice,
        String image,
        String imageAlt,
        String category,
        double rating,
        int reviewCount,
        boolean inStock,
        boolean featured,
        List<String> tags,
        String sku,
        Map<String, String> specs
) {

    /**
     * Returns true if this product is on sale (has an original price
     * higher than the current price).
     */
    public boolean onSale() {
        return originalPrice > 0 && originalPrice > price;
    }

    /**
     * Calculates the discount percentage if the product is on sale.
     *
     * @return discount percentage (0-100), or 0 if not on sale
     */
    public int discountPercent() {
        if (!onSale()) return 0;
        return (int) Math.round((1.0 - price / originalPrice) * 100);
    }
}
