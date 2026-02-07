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
import xss.it.jux.store.data.CartItem;
import xss.it.jux.store.data.StoreData;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Shopping cart page showing current cart items and order summary.
 *
 * <p>Uses a sample cart from StoreData for the demo. In a real
 * application, cart data would come from a session or database.</p>
 */
@Route("/cart")
@Title("Shopping Cart - JUX Store")
public class CartPage extends Page {

    @Override
    public Element render() {
        var m = messages();
        var cart = StoreData.sampleCart();

        if (cart.isEmpty()) {
            return new StoreLayout("/cart", m,
                    new EmptyState("\uD83D\uDED2", m.getString("cart.empty.title"),
                            m.getString("cart.empty.text"),
                            m.getString("cart.empty.cta"), "/products").render()
            ).render();
        }

        double subtotal = cart.stream().mapToDouble(CartItem::subtotal).sum();
        double shipping = subtotal > 100 ? 0 : 9.99;
        double tax = subtotal * 0.08;
        double total = subtotal + shipping + tax;

        return new StoreLayout("/cart", m,
                div().children(
                        /* Page header */
                        section().cls("bg-gray-50", "py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                h1().cls("text-3xl", "font-bold", "text-gray-900")
                                                        .text(m.getString("cart.title"))
                                        )
                        ),
                        /* Cart content */
                        section().cls("py-8").children(
                                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                        .children(
                                                div().cls("grid", "grid-cols-1", "lg:grid-cols-3", "gap-8")
                                                        .children(
                                                                /* Cart items (2/3 width) */
                                                                div().cls("lg:col-span-2").children(
                                                                        cartItems(cart)
                                                                ),
                                                                /* Order summary sidebar (1/3 width) */
                                                                div().children(
                                                                        orderSummary(subtotal, shipping, tax, total)
                                                                )
                                                        )
                                        )
                        )
                )
        ).render();
    }

    private Element cartItems(List<CartItem> cart) {
        var rows = cart.stream()
                .map(item -> new CartItemRow(item).render())
                .toList();
        return div().children(rows);
    }

    private Element orderSummary(double subtotal, double shipping, double tax, double total) {
        var m = messages();
        return div().cls("bg-gray-50", "rounded-xl", "p-6", "sticky", "top-24").children(
                h2().cls("text-lg", "font-bold", "text-gray-900", "mb-4")
                        .text(m.getString("cart.summary.title")),
                dl().cls("space-y-3").children(
                        summaryRow(m.getString("cart.summary.subtotal"),
                                StoreData.formatPrice(subtotal)),
                        summaryRow(m.getString("cart.summary.shipping"),
                                shipping == 0 ? m.getString("cart.summary.free") :
                                        StoreData.formatPrice(shipping)),
                        summaryRow(m.getString("cart.summary.tax"),
                                StoreData.formatPrice(tax)),
                        /* Total with emphasis */
                        div().cls("flex", "justify-between", "pt-3", "border-t",
                                        "border-gray-300")
                                .children(
                                        dt().cls("font-bold", "text-gray-900")
                                                .text(m.getString("cart.summary.total")),
                                        dd().cls("font-bold", "text-gray-900", "text-lg")
                                                .text(StoreData.formatPrice(total))
                                )
                ),
                /* Checkout button */
                a().cls("block", "w-full", "bg-indigo-600", "text-white", "text-center",
                                "px-6", "py-3", "rounded-lg", "font-semibold", "mt-6",
                                "hover:bg-indigo-700", "focus:ring-2", "focus:ring-indigo-500",
                                "focus:ring-offset-2", "transition-colors")
                        .attr("href", "/checkout")
                        .text(m.getString("cart.checkout")),
                /* Continue shopping link */
                a().cls("block", "text-center", "text-indigo-600", "text-sm",
                                "font-medium", "mt-3", "hover:text-indigo-700")
                        .attr("href", "/products")
                        .text(m.getString("cart.continue_shopping"))
        );
    }

    private Element summaryRow(String label, String value) {
        return div().cls("flex", "justify-between").children(
                dt().cls("text-gray-500").text(label),
                dd().cls("text-gray-900", "font-medium").text(value)
        );
    }
}
