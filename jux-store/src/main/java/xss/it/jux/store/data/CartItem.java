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

/**
 * A single line item in the shopping cart.
 *
 * <p>Pairs a product with a quantity. The subtotal is computed
 * dynamically as {@code product.price() * quantity}.</p>
 *
 * @param product  the product added to the cart
 * @param quantity the number of units (must be >= 1)
 */
public record CartItem(
        Product product,
        int quantity
) {

    /**
     * Computes the subtotal for this line item.
     *
     * @return product price multiplied by quantity
     */
    public double subtotal() {
        return product.price() * quantity;
    }
}
