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

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Responsive grid of product cards.
 *
 * <p>Renders a CSS Grid that adapts from 1 column on mobile to
 * 4 columns on extra-large screens. Each product is rendered
 * as a {@link ProductCard}.</p>
 */
public class ProductGrid extends Component {

    private final List<Product> products;

    public ProductGrid(List<Product> products) {
        this.products = products;
    }

    @Override
    public Element render() {
        var cards = products.stream()
                .map(p -> new ProductCard(p).render())
                .toList();

        return div().cls("grid", "grid-cols-1", "sm:grid-cols-2", "lg:grid-cols-3",
                        "xl:grid-cols-4", "gap-6")
                .children(cards);
    }
}
