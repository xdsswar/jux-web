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
import xss.it.jux.theme.*;

import static xss.it.jux.core.Elements.*;

/**
 * Login page demonstrating a centered card form layout with JuxTextField,
 * manual checkbox construction, and accessible form patterns.
 */
@Route("/login")
@Title("Login - JUX Demo")
@Meta(name = "description", content = "Login page demo - JUX framework accessible form example")
public class LoginPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        return new PageLayout("/login", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().cls("py-5", "bg-light", "min-vh-100").children(
            div().cls("container").children(
                div().cls("row", "justify-content-center").children(
                    div().cls("col-md-5").children(
                        div().cls("card", "shadow").children(
                            cardHeader(),
                            cardBody()
                        )
                    )
                )
            )
        );
    }

    private Element cardHeader() {
        return div().cls("card-header", "bg-white", "text-center", "pt-4", "pb-3", "border-0").children(
            Element.of("i").cls("bi", "bi-shield-lock", "text-primary")
                .style("font-size", "2.5rem").ariaHidden(true),
            h1().cls("h4", "fw-bold", "mt-2", "mb-0").text(messages().getString("login.title"))
        );
    }

    private Element cardBody() {
        var m = messages();
        return div().cls("card-body", "p-4").children(
            form().attr("method", "post").attr("action", "/login").children(
                div().cls("mb-3").children(
                    new JuxTextField("email", m.getString("login.email"))
                        .setType("email")
                        .setPlaceholder(m.getString("login.email.placeholder"))
                        .setRequired(true)
                        .render()
                ),
                div().cls("mb-3").children(
                    new JuxTextField("password", m.getString("login.password"))
                        .setType("password")
                        .setPlaceholder(m.getString("login.password.placeholder"))
                        .setRequired(true)
                        .render()
                ),
                div().cls("d-flex", "justify-content-between", "align-items-center", "mb-3").children(
                    div().cls("form-check").children(
                        input().cls("form-check-input").attr("type", "checkbox").id("remember"),
                        label().cls("form-check-label").attr("for", "remember").text(m.getString("login.remember"))
                    ),
                    a().attr("href", "#").cls("text-decoration-none", "small").text(m.getString("login.forgot"))
                ),
                button().cls("btn", "btn-primary", "w-100", "py-2")
                    .attr("type", "submit")
                    .text(m.getString("login.submit")),
                div().cls("position-relative", "my-4").children(
                    hr().cls("text-secondary"),
                    span().cls("position-absolute", "top-50", "start-50",
                            "translate-middle", "bg-white", "px-3", "text-secondary", "small")
                        .text(m.getString("login.or"))
                ),
                p().cls("text-center", "mb-0", "small").children(
                    span().text(m.getString("login.no_account") + " "),
                    a().attr("href", "#").cls("text-decoration-none", "fw-semibold").text(m.getString("login.signup"))
                )
            )
        );
    }
}
