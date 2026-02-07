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

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Pricing page with tiered plan cards and an FAQ accordion,
 * built entirely in Java using JUX's Element API.
 */
@Route("/pricing")
@Title("Pricing - JUX Demo")
@Meta(name = "description", content = "JUX framework pricing plans - Starter, Professional, and Enterprise tiers")
public class PricingPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        return new PageLayout("/pricing", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
            headerSection(),
            pricingCardsSection(),
            faqSection()
        );
    }

    // ── Header ────────────────────────────────────────────────

    private Element headerSection() {
        var m = messages();
        return section().cls("bg-primary", "text-white", "py-5").children(
            div().cls("container", "py-3", "text-center").children(
                h1().cls("display-5", "fw-bold").text(m.getString("pricing.header.title")),
                p().cls("lead", "mb-0", "opacity-75").text(m.getString("pricing.header.subtitle"))
            )
        );
    }

    // ── Pricing Cards ─────────────────────────────────────────

    private Element pricingCardsSection() {
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("row", "g-4", "justify-content-center").children(
                    starterPlan(),
                    professionalPlan(),
                    enterprisePlan()
                )
            )
        );
    }

    private Element starterPlan() {
        var m = messages();
        return div().cls("col-lg-4").children(
            div().cls("card", "border-0", "shadow-sm", "h-100").children(
                div().cls("card-header", "bg-white", "text-center", "py-4", "border-bottom").children(
                    h2().cls("h4", "fw-bold", "mb-1").text(m.getString("pricing.starter.name")),
                    div().cls("mb-2").children(
                        span().cls("display-5", "fw-bold").text(m.getString("pricing.starter.price")),
                        span().cls("text-secondary").text(m.getString("pricing.per_month"))
                    ),
                    p().cls("text-secondary", "small", "mb-0").text(m.getString("pricing.starter.desc"))
                ),
                div().cls("card-body", "p-4").children(
                    ul().cls("list-group", "list-group-flush", "mb-4").children(
                        featureItem(m.getString("pricing.feature.projects3")),
                        featureItem(m.getString("pricing.feature.storage5")),
                        featureItem(m.getString("pricing.feature.support_community")),
                        featureItem(m.getString("pricing.feature.analytics_basic")),
                        featureItem(m.getString("pricing.feature.ssl"))
                    )
                ),
                div().cls("card-footer", "bg-white", "text-center", "py-4", "border-top-0").children(
                    a().cls("btn", "btn-outline-primary", "btn-lg", "w-100")
                        .attr("href", "/contact").text(m.getString("pricing.starter.cta"))
                )
            )
        );
    }

    private Element professionalPlan() {
        var m = messages();
        return div().cls("col-lg-4").children(
            div().cls("card", "border-primary", "shadow-lg", "h-100").children(
                div().cls("card-header", "bg-primary", "text-white", "text-center", "py-4").children(
                    span().cls("badge", "bg-warning", "text-dark", "mb-2").text(m.getString("pricing.pro.badge")),
                    h2().cls("h4", "fw-bold", "mb-1").text(m.getString("pricing.pro.name")),
                    div().cls("mb-2").children(
                        span().cls("display-5", "fw-bold").text(m.getString("pricing.pro.price")),
                        span().cls("opacity-75").text(m.getString("pricing.per_month"))
                    ),
                    p().cls("opacity-75", "small", "mb-0").text(m.getString("pricing.pro.desc"))
                ),
                div().cls("card-body", "p-4").children(
                    ul().cls("list-group", "list-group-flush", "mb-4").children(
                        featureItem(m.getString("pricing.feature.projects_unlimited")),
                        featureItem(m.getString("pricing.feature.storage50")),
                        featureItem(m.getString("pricing.feature.support_email")),
                        featureItem(m.getString("pricing.feature.analytics_advanced")),
                        featureItem(m.getString("pricing.feature.ssl")),
                        featureItem(m.getString("pricing.feature.domains")),
                        featureItem(m.getString("pricing.feature.team")),
                        featureItem(m.getString("pricing.feature.api"))
                    )
                ),
                div().cls("card-footer", "bg-white", "text-center", "py-4", "border-top-0").children(
                    a().cls("btn", "btn-primary", "btn-lg", "w-100")
                        .attr("href", "/contact").text(m.getString("pricing.pro.cta"))
                )
            )
        );
    }

    private Element enterprisePlan() {
        var m = messages();
        return div().cls("col-lg-4").children(
            div().cls("card", "border-0", "shadow-sm", "h-100").children(
                div().cls("card-header", "bg-white", "text-center", "py-4", "border-bottom").children(
                    h2().cls("h4", "fw-bold", "mb-1").text(m.getString("pricing.enterprise.name")),
                    div().cls("mb-2").children(
                        span().cls("display-5", "fw-bold").text(m.getString("pricing.enterprise.price")),
                        span().cls("text-secondary").text(m.getString("pricing.per_month"))
                    ),
                    p().cls("text-secondary", "small", "mb-0").text(m.getString("pricing.enterprise.desc"))
                ),
                div().cls("card-body", "p-4").children(
                    ul().cls("list-group", "list-group-flush", "mb-4").children(
                        featureItem(m.getString("pricing.feature.projects_unlimited")),
                        featureItem(m.getString("pricing.feature.storage500")),
                        featureItem(m.getString("pricing.feature.support_phone")),
                        featureItem(m.getString("pricing.feature.analytics_enterprise")),
                        featureItem(m.getString("pricing.feature.ssl")),
                        featureItem(m.getString("pricing.feature.domains")),
                        featureItem(m.getString("pricing.feature.team")),
                        featureItem(m.getString("pricing.feature.api")),
                        featureItem(m.getString("pricing.feature.sla")),
                        featureItem(m.getString("pricing.feature.manager"))
                    )
                ),
                div().cls("card-footer", "bg-white", "text-center", "py-4", "border-top-0").children(
                    a().cls("btn", "btn-outline-primary", "btn-lg", "w-100")
                        .attr("href", "/contact").text(m.getString("pricing.enterprise.cta"))
                )
            )
        );
    }

    private Element featureItem(String text) {
        return li().cls("list-group-item", "border-0", "px-0").children(
            Element.of("i").cls("bi", "bi-check-lg", "text-success", "me-2")
                .ariaHidden(true),
            span().text(text)
        );
    }

    // ── FAQ ───────────────────────────────────────────────────

    private Element faqSection() {
        var m = messages();
        return section().cls("bg-light", "py-5").children(
            div().cls("container").children(
                div().cls("col-lg-8", "mx-auto").children(
                    h2().cls("fw-bold", "mb-4", "text-center").text(m.getString("pricing.faq.title")),
                    new JuxAccordion(List.of(
                        new JuxAccordion.Section(m.getString("pricing.faq.q1"),
                            p().text(m.getString("pricing.faq.a1"))),
                        new JuxAccordion.Section(m.getString("pricing.faq.q2"),
                            p().text(m.getString("pricing.faq.a2"))),
                        new JuxAccordion.Section(m.getString("pricing.faq.q3"),
                            p().text(m.getString("pricing.faq.a3"))),
                        new JuxAccordion.Section(m.getString("pricing.faq.q4"),
                            p().text(m.getString("pricing.faq.a4")))
                    )).render()
                )
            )
        );
    }
}
