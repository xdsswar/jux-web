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
 * Contact page with form (GET + POST) and contact info sidebar.
 *
 * <p>ADA compliance:</p>
 * <ul>
 *   <li>Every input has a paired {@code <label>}</li>
 *   <li>Required fields have {@code ariaRequired(true)}</li>
 *   <li>Form grouped with {@code <fieldset>}/{@code <legend>}</li>
 *   <li>Autocomplete attributes for UX (WCAG 1.3.5)</li>
 * </ul>
 */
@Route(value = "/contact", methods = {HttpMethod.GET, HttpMethod.POST})
@Title("Contact Us - JUX Store")
@Meta(name = "description", content = "Get in touch with the JUX Store team")
public class ContactPage extends Page {

    @RequestContext
    private JuxRequestContext ctx;

    @Override
    public Element render() {
        var m = messages();
        boolean posted = ctx != null && ctx.isPost();

        if (posted) {
            return new StoreLayout("/contact", m, successContent()).render();
        }
        return new StoreLayout("/contact", m, formContent()).render();
    }

    private Element successContent() {
        var m = messages();
        return section().cls("py-16").children(
                div().cls("max-w-lg", "mx-auto", "text-center", "px-4").children(
                        div().cls("text-6xl", "mb-4").ariaHidden(true).text("\u2705"),
                        h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-3")
                                .text(m.getString("contact.success.title")),
                        p().cls("text-gray-500", "mb-6")
                                .text(m.getString("contact.success.text")),
                        a().cls("inline-block", "bg-indigo-600", "text-white", "px-6", "py-3",
                                        "rounded-lg", "font-semibold", "hover:bg-indigo-700")
                                .attr("href", "/")
                                .text(m.getString("contact.success.btn"))
                )
        );
    }

    private Element formContent() {
        var m = messages();
        return div().children(
                /* Header */
                section().cls("bg-indigo-600", "text-white", "py-12").children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                .children(
                                        h1().cls("text-3xl", "font-bold", "mb-2")
                                                .text(m.getString("contact.header.title")),
                                        p().cls("text-indigo-200")
                                                .text(m.getString("contact.header.subtitle"))
                                )
                ),
                /* Form + info sidebar */
                section().cls("py-12").children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                .children(
                                        div().cls("grid", "grid-cols-1", "lg:grid-cols-3", "gap-10")
                                                .children(
                                                        contactForm(),
                                                        contactInfo()
                                                )
                                )
                )
        );
    }

    private Element contactForm() {
        var m = messages();
        return div().cls("lg:col-span-2").children(
                form().attr("method", "post").attr("action", "/contact").children(
                        fieldset().children(
                                legend().cls("text-xl", "font-bold", "text-gray-900", "mb-6")
                                        .text(m.getString("contact.form.legend")),
                                div().cls("grid", "grid-cols-1", "sm:grid-cols-2", "gap-4").children(
                                        /* Name */
                                        div().children(
                                                label().cls("block", "text-sm", "font-medium",
                                                                "text-gray-700", "mb-1")
                                                        .attr("for", "name")
                                                        .text(m.getString("contact.form.name")),
                                                input().id("name").cls("w-full", "border",
                                                                "border-gray-300", "rounded-lg", "px-4",
                                                                "py-2", "focus:ring-2",
                                                                "focus:ring-indigo-500")
                                                        .attr("type", "text")
                                                        .attr("name", "name")
                                                        .attr("autocomplete", "name")
                                                        .ariaRequired(true)
                                        ),
                                        /* Email */
                                        div().children(
                                                label().cls("block", "text-sm", "font-medium",
                                                                "text-gray-700", "mb-1")
                                                        .attr("for", "email")
                                                        .text(m.getString("contact.form.email")),
                                                input().id("email").cls("w-full", "border",
                                                                "border-gray-300", "rounded-lg", "px-4",
                                                                "py-2", "focus:ring-2",
                                                                "focus:ring-indigo-500")
                                                        .attr("type", "email")
                                                        .attr("name", "email")
                                                        .attr("autocomplete", "email")
                                                        .ariaRequired(true)
                                        ),
                                        /* Subject */
                                        div().cls("sm:col-span-2").children(
                                                label().cls("block", "text-sm", "font-medium",
                                                                "text-gray-700", "mb-1")
                                                        .attr("for", "subject")
                                                        .text(m.getString("contact.form.subject")),
                                                input().id("subject").cls("w-full", "border",
                                                                "border-gray-300", "rounded-lg", "px-4",
                                                                "py-2", "focus:ring-2",
                                                                "focus:ring-indigo-500")
                                                        .attr("type", "text")
                                                        .attr("name", "subject")
                                                        .ariaRequired(true)
                                        ),
                                        /* Message */
                                        div().cls("sm:col-span-2").children(
                                                label().cls("block", "text-sm", "font-medium",
                                                                "text-gray-700", "mb-1")
                                                        .attr("for", "message")
                                                        .text(m.getString("contact.form.message")),
                                                textarea().id("message").cls("w-full", "border",
                                                                "border-gray-300", "rounded-lg", "px-4",
                                                                "py-2", "focus:ring-2",
                                                                "focus:ring-indigo-500")
                                                        .attr("name", "message")
                                                        .attr("rows", "5")
                                                        .ariaRequired(true)
                                        ),
                                        /* Submit */
                                        div().cls("sm:col-span-2").children(
                                                button().cls("bg-indigo-600", "text-white", "px-8",
                                                                "py-3", "rounded-lg", "font-semibold",
                                                                "hover:bg-indigo-700", "focus:ring-2",
                                                                "focus:ring-indigo-500",
                                                                "focus:ring-offset-2")
                                                        .attr("type", "submit")
                                                        .text(m.getString("contact.form.submit"))
                                        )
                                )
                        )
                )
        );
    }

    private Element contactInfo() {
        var m = messages();
        return aside().cls("space-y-6").children(
                h2().cls("text-xl", "font-bold", "text-gray-900")
                        .text(m.getString("contact.info.title")),
                infoItem("\uD83D\uDCE7", m.getString("contact.info.email"),
                        m.getString("contact.info.email.value")),
                infoItem("\uD83D\uDCDE", m.getString("contact.info.phone"),
                        m.getString("contact.info.phone.value")),
                infoItem("\uD83D\uDCCD", m.getString("contact.info.address"),
                        m.getString("contact.info.address.value")),
                infoItem("\u23F0", m.getString("contact.info.hours"),
                        m.getString("contact.info.hours.value"))
        );
    }

    private Element infoItem(String icon, String label, String value) {
        return div().cls("flex", "gap-3").children(
                span().cls("text-2xl").ariaHidden(true).text(icon),
                div().children(
                        h3().cls("font-medium", "text-gray-900").text(label),
                        p().cls("text-sm", "text-gray-500").text(value)
                )
        );
    }
}
