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

package xss.it.jux.demo.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.demo.components.PageLayout;

import static xss.it.jux.core.Elements.*;

/**
 * Contact page demonstrating forms with full ADA compliance.
 *
 * <p>Every input has a label, required fields have aria-required,
 * and the form uses proper fieldset/legend grouping.</p>
 */
@Route(value = "/contact", methods = { HttpMethod.GET, HttpMethod.POST })
@Title("Contact - JUX Demo")
@Meta(name = "description", content = "Get in touch - JUX demo contact form")
public class ContactPage extends Page {

    @RequestContext
    private JuxRequestContext ctx;

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        boolean isPost = ctx != null && ctx.isPost();
        var content = isPost ? successContent() : formContent();
        return new PageLayout("/contact", messages(), content).render();
    }

    private Element successContent() {
        var m = messages();
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("row").children(
                    div().cls("col-lg-6", "mx-auto", "text-center", "py-5").children(
                        div().cls("mb-4").children(
                            Element.of("i").cls("bi-check-circle-fill", "text-success")
                                .style("font-size", "4rem").ariaHidden(true)
                        ),
                        h1().cls("h3", "fw-bold").text(m.getString("contact.success.title")),
                        p().cls("text-secondary", "mb-4").text(m.getString("contact.success.text")),
                        a().cls("btn", "btn-primary").attr("href", "/").text(m.getString("contact.success.btn"))
                    )
                )
            )
        );
    }

    private Element formContent() {
        var m = messages();
        return div().children(
            section().cls("bg-primary", "text-white", "py-5").children(
                div().cls("container", "py-3").children(
                    h1().cls("display-5", "fw-bold").text(m.getString("contact.header.title")),
                    p().cls("lead", "mb-0", "opacity-75").text(m.getString("contact.header.subtitle"))
                )
            ),
            section().cls("py-5").children(
                div().cls("container").children(
                    div().cls("row").children(
                        div().cls("col-lg-8").children(
                            form().attr("method", "post").attr("action", "/contact")
                                .attr("novalidate", "").children(
                                fieldset().children(
                                    legend().cls("h4", "fw-bold", "mb-4").text(m.getString("contact.form.legend")),
                                    div().cls("row", "g-3").children(
                                        div().cls("col-md-6").children(
                                            label().cls("form-label").attr("for", "name").text(m.getString("contact.form.name")),
                                            input().id("name").cls("form-control")
                                                .attr("type", "text")
                                                .attr("name", "name")
                                                .attr("placeholder", "John Doe")
                                                .attr("autocomplete", "name")
                                                .ariaRequired(true)
                                        ),
                                        div().cls("col-md-6").children(
                                            label().cls("form-label").attr("for", "email").text(m.getString("contact.form.email")),
                                            input().id("email").cls("form-control")
                                                .attr("type", "email")
                                                .attr("name", "email")
                                                .attr("placeholder", "you@example.com")
                                                .attr("autocomplete", "email")
                                                .ariaRequired(true)
                                        ),
                                        div().cls("col-12").children(
                                            label().cls("form-label").attr("for", "subject").text(m.getString("contact.form.subject")),
                                            select().id("subject").cls("form-select")
                                                .attr("name", "subject")
                                                .ariaRequired(true)
                                                .children(
                                                    option().attr("value", "").text(m.getString("contact.form.subject.choose")),
                                                    option().attr("value", "general").text(m.getString("contact.form.subject.general")),
                                                    option().attr("value", "bug").text(m.getString("contact.form.subject.bug")),
                                                    option().attr("value", "feature").text(m.getString("contact.form.subject.feature")),
                                                    option().attr("value", "other").text(m.getString("contact.form.subject.other"))
                                                )
                                        ),
                                        div().cls("col-12").children(
                                            label().cls("form-label").attr("for", "message").text(m.getString("contact.form.message")),
                                            textarea().id("message").cls("form-control")
                                                .attr("name", "message")
                                                .attr("rows", "5")
                                                .attr("placeholder", m.getString("contact.form.message.placeholder"))
                                                .ariaRequired(true),
                                            div().id("message-help").cls("form-text")
                                                .text(m.getString("contact.form.message.help"))
                                        ),
                                        div().cls("col-12").children(
                                            div().cls("form-check").children(
                                                input().id("newsletter").cls("form-check-input")
                                                    .attr("type", "checkbox")
                                                    .attr("name", "newsletter"),
                                                label().cls("form-check-label").attr("for", "newsletter")
                                                    .text(m.getString("contact.form.newsletter"))
                                            )
                                        ),
                                        div().cls("col-12").children(
                                            button().cls("btn", "btn-primary", "btn-lg")
                                                .attr("type", "submit")
                                                .text(m.getString("contact.form.submit"))
                                        )
                                    )
                                )
                            )
                        ),
                        div().cls("col-lg-4").children(
                            div().cls("card", "border-0", "bg-light").children(
                                div().cls("card-body", "p-4").children(
                                    h2().cls("h5", "fw-bold", "mb-3").text(m.getString("contact.notes.title")),
                                    p().cls("text-secondary", "small").text(m.getString("contact.notes.intro")),
                                    ul().cls("small", "text-secondary").children(
                                        li().text(m.getString("contact.notes.label")),
                                        li().text(m.getString("contact.notes.aria")),
                                        li().text(m.getString("contact.notes.fieldset")),
                                        li().text(m.getString("contact.notes.autocomplete")),
                                        li().text(m.getString("contact.notes.help"))
                                    ),
                                    p().cls("text-secondary", "small", "mb-0").text(m.getString("contact.notes.submit"))
                                )
                            )
                        )
                    )
                )
            )
        );
    }
}
