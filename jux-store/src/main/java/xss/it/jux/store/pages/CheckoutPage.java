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

import static xss.it.jux.core.Elements.*;

/**
 * Checkout page with shipping and payment form (GET + POST).
 *
 * <p>Demonstrates multi-section form handling with JUX. On GET,
 * the form is displayed. On POST, a success confirmation is shown.
 * Every input has a label, required fields use ariaRequired, and
 * the form uses fieldset/legend grouping (WCAG 3.3.2).</p>
 */
@Route(value = "/checkout", methods = {HttpMethod.GET, HttpMethod.POST})
@Title("Checkout - JUX Store")
public class CheckoutPage extends Page {

    @RequestContext
    private JuxRequestContext ctx;

    @Override
    public Element render() {
        var m = messages();
        boolean posted = ctx != null && ctx.isPost();

        if (posted) {
            return new StoreLayout("/cart", m, successContent()).render();
        }
        return new StoreLayout("/cart", m, checkoutForm()).render();
    }

    /**
     * Success confirmation after form submission.
     */
    private Element successContent() {
        var m = messages();
        return section().cls("py-16").children(
                div().cls("max-w-lg", "mx-auto", "text-center", "px-4").children(
                        div().cls("text-6xl", "mb-4").ariaHidden(true).text("\u2705"),
                        h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-3")
                                .text(m.getString("checkout.success.title")),
                        p().cls("text-gray-500", "mb-6")
                                .text(m.getString("checkout.success.text")),
                        a().cls("inline-block", "bg-indigo-600", "text-white", "px-6", "py-3",
                                        "rounded-lg", "font-semibold", "hover:bg-indigo-700")
                                .attr("href", "/")
                                .text(m.getString("checkout.success.btn"))
                )
        );
    }

    /**
     * Checkout form with shipping and payment sections.
     */
    private Element checkoutForm() {
        var m = messages();
        return section().cls("py-8").children(
                div().cls("max-w-3xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-8")
                                .text(m.getString("checkout.title")),
                        form().attr("method", "post").attr("action", "/checkout").children(
                                /* Shipping section */
                                shippingFieldset(),
                                /* Payment section */
                                paymentFieldset(),
                                /* Submit button */
                                div().cls("mt-8").children(
                                        button().cls("w-full", "bg-indigo-600", "text-white",
                                                        "px-6", "py-3", "rounded-lg", "font-semibold",
                                                        "hover:bg-indigo-700", "focus:ring-2",
                                                        "focus:ring-indigo-500", "focus:ring-offset-2",
                                                        "text-lg")
                                                .attr("type", "submit")
                                                .text(m.getString("checkout.submit"))
                                )
                        )
                )
        );
    }

    private Element shippingFieldset() {
        var m = messages();
        return fieldset().cls("mb-8").children(
                legend().cls("text-xl", "font-bold", "text-gray-900", "mb-4")
                        .text(m.getString("checkout.shipping.legend")),
                div().cls("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4").children(
                        formField("first-name", m.getString("checkout.first_name"),
                                "text", "given-name", true),
                        formField("last-name", m.getString("checkout.last_name"),
                                "text", "family-name", true),
                        div().cls("sm:col-span-2").children(
                                label().cls("block", "text-sm", "font-medium", "text-gray-700", "mb-1")
                                        .attr("for", "address")
                                        .text(m.getString("checkout.address")),
                                input().id("address").cls("w-full", "border", "border-gray-300",
                                                "rounded-lg", "px-4", "py-2", "focus:ring-2",
                                                "focus:ring-indigo-500", "focus:border-indigo-500")
                                        .attr("type", "text")
                                        .attr("name", "address")
                                        .attr("autocomplete", "street-address")
                                        .ariaRequired(true)
                        ),
                        formField("city", m.getString("checkout.city"),
                                "text", "address-level2", true),
                        formField("zip", m.getString("checkout.zip"),
                                "text", "postal-code", true),
                        div().cls("sm:col-span-2").children(
                                label().cls("block", "text-sm", "font-medium", "text-gray-700", "mb-1")
                                        .attr("for", "email")
                                        .text(m.getString("checkout.email")),
                                input().id("email").cls("w-full", "border", "border-gray-300",
                                                "rounded-lg", "px-4", "py-2", "focus:ring-2",
                                                "focus:ring-indigo-500", "focus:border-indigo-500")
                                        .attr("type", "email")
                                        .attr("name", "email")
                                        .attr("autocomplete", "email")
                                        .ariaRequired(true)
                        )
                )
        );
    }

    private Element paymentFieldset() {
        var m = messages();
        return fieldset().cls("mb-8").children(
                legend().cls("text-xl", "font-bold", "text-gray-900", "mb-4")
                        .text(m.getString("checkout.payment.legend")),
                div().cls("space-y-4").children(
                        div().children(
                                label().cls("block", "text-sm", "font-medium", "text-gray-700", "mb-1")
                                        .attr("for", "card-number")
                                        .text(m.getString("checkout.card_number")),
                                input().id("card-number").cls("w-full", "border", "border-gray-300",
                                                "rounded-lg", "px-4", "py-2", "focus:ring-2",
                                                "focus:ring-indigo-500", "focus:border-indigo-500")
                                        .attr("type", "text")
                                        .attr("name", "card_number")
                                        .attr("placeholder", "1234 5678 9012 3456")
                                        .attr("autocomplete", "cc-number")
                                        .ariaRequired(true)
                        ),
                        div().cls("grid", "grid-cols-2", "gap-4").children(
                                formField("expiry", m.getString("checkout.expiry"),
                                        "text", "cc-exp", true),
                                formField("cvv", m.getString("checkout.cvv"),
                                        "text", "cc-csc", true)
                        )
                )
        );
    }

    /**
     * Reusable form field: label + input.
     */
    private Element formField(String fieldId, String labelText, String type,
                              String autocomplete, boolean required) {
        var field = div().children(
                label().cls("block", "text-sm", "font-medium", "text-gray-700", "mb-1")
                        .attr("for", fieldId)
                        .text(labelText),
                input().id(fieldId).cls("w-full", "border", "border-gray-300",
                                "rounded-lg", "px-4", "py-2", "focus:ring-2",
                                "focus:ring-indigo-500", "focus:border-indigo-500")
                        .attr("type", type)
                        .attr("name", fieldId)
                        .attr("autocomplete", autocomplete)
        );
        /* Note: ariaRequired needs to be set on the input, not the div wrapper.
         * Since we're building the input inline above, we handle it by rebuilding. */
        if (required) {
            return div().children(
                    label().cls("block", "text-sm", "font-medium", "text-gray-700", "mb-1")
                            .attr("for", fieldId)
                            .text(labelText),
                    input().id(fieldId).cls("w-full", "border", "border-gray-300",
                                    "rounded-lg", "px-4", "py-2", "focus:ring-2",
                                    "focus:ring-indigo-500", "focus:border-indigo-500")
                            .attr("type", type)
                            .attr("name", fieldId)
                            .attr("autocomplete", autocomplete)
                            .ariaRequired(true)
            );
        }
        return field;
    }
}
