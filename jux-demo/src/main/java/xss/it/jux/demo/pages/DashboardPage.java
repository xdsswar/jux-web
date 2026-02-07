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
 * Dashboard demo page showcasing stats cards, data tables,
 * progress bars, and activity feeds built entirely in Java.
 */
@Route("/dashboard")
@Title("Dashboard - JUX Demo")
@Meta(name = "description", content = "JUX framework dashboard demo - stats, tables, and progress bars in pure Java")
public class DashboardPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        return new PageLayout("/dashboard", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
            headerSection(),
            statsSection(),
            contentSection(),
            activitySection()
        );
    }

    // ── Header ────────────────────────────────────────────────

    private Element headerSection() {
        var m = messages();
        return section().cls("bg-primary", "text-white", "py-5").children(
            div().cls("container", "py-3").children(
                h1().cls("display-5", "fw-bold").text(m.getString("dashboard.header.title")),
                p().cls("lead", "mb-0", "opacity-75").text(m.getString("dashboard.header.subtitle"))
            )
        );
    }

    // ── Stats Cards ───────────────────────────────────────────

    private Element statsSection() {
        var m = messages();
        return section().cls("py-4").children(
            div().cls("container").children(
                div().cls("row", "g-4").children(
                    statCard("bi-people", "text-primary", "24,500", m.getString("dashboard.stats.users"),
                        "+12.5%", true),
                    statCard("bi-currency-dollar", "text-success", "$48,200", m.getString("dashboard.stats.revenue"),
                        "+8.2%", true),
                    statCard("bi-cart", "text-info", "1,840", m.getString("dashboard.stats.orders"),
                        "+23.1%", true),
                    statCard("bi-graph-up", "text-warning", "3.6%", m.getString("dashboard.stats.conversion"),
                        "-0.4%", false)
                )
            )
        );
    }

    private Element statCard(String icon, String iconColor, String value,
                             String label, String trend, boolean trendPositive) {
        String badgeCls = trendPositive
            ? "badge bg-success-subtle text-success"
            : "badge bg-danger-subtle text-danger";
        String trendIcon = trendPositive ? "bi-arrow-up" : "bi-arrow-down";

        return div().cls("col-sm-6", "col-xl-3").children(
            div().cls("card", "border-0", "shadow-sm", "h-100").children(
                div().cls("card-body", "p-4").children(
                    div().cls("d-flex", "justify-content-between", "align-items-start", "mb-3").children(
                        Element.of("i").cls("bi", icon, "fs-2", iconColor)
                            .ariaHidden(true),
                        span().cls(badgeCls).children(
                            Element.of("i").cls("bi", trendIcon, "me-1")
                                .ariaHidden(true),
                            span().text(trend)
                        )
                    ),
                    h2().cls("h3", "fw-bold", "mb-1").text(value),
                    p().cls("text-secondary", "mb-0", "small").text(label)
                )
            )
        );
    }

    // ── Content: Orders Table + Progress ──────────────────────

    private Element contentSection() {
        return section().cls("py-4").children(
            div().cls("container").children(
                div().cls("row", "g-4").children(
                    div().cls("col-lg-8").children(ordersTable()),
                    div().cls("col-lg-4").children(projectProgress())
                )
            )
        );
    }

    private Element ordersTable() {
        var m = messages();
        return div().cls("card", "border-0", "shadow-sm").children(
            div().cls("card-header", "bg-white", "border-bottom").children(
                h3().cls("h5", "fw-bold", "mb-0", "py-2").text(m.getString("dashboard.orders.title"))
            ),
            div().cls("card-body", "p-0").children(
                div().cls("table-responsive").children(
                    table().cls("table", "table-hover", "mb-0").children(
                        caption().cls("visually-hidden").text(m.getString("dashboard.orders.caption")),
                        thead().cls("table-light").children(
                            tr().children(
                                th().attr("scope", "col").cls("ps-4").text(m.getString("dashboard.orders.col.order")),
                                th().attr("scope", "col").text(m.getString("dashboard.orders.col.customer")),
                                th().attr("scope", "col").text(m.getString("dashboard.orders.col.product")),
                                th().attr("scope", "col").text(m.getString("dashboard.orders.col.amount")),
                                th().attr("scope", "col").text(m.getString("dashboard.orders.col.status"))
                            )
                        ),
                        Element.of("tbody").children(
                            orderRow("#3210", "Sarah Johnson", "Pro License", "$299.00",
                                m.getString("dashboard.status.completed"), "bg-success-subtle text-success"),
                            orderRow("#3209", "Michael Chen", "Team Plan", "$799.00",
                                m.getString("dashboard.status.processing"), "bg-warning-subtle text-warning"),
                            orderRow("#3208", "Emily Davis", "Starter Kit", "$49.00",
                                m.getString("dashboard.status.completed"), "bg-success-subtle text-success"),
                            orderRow("#3207", "James Wilson", "Enterprise", "$1,299.00",
                                m.getString("dashboard.status.pending"), "bg-info-subtle text-info"),
                            orderRow("#3206", "Maria Garcia", "Pro License", "$299.00",
                                m.getString("dashboard.status.cancelled"), "bg-danger-subtle text-danger")
                        )
                    )
                )
            )
        );
    }

    private Element orderRow(String order, String customer, String product,
                             String amount, String status, String statusCls) {
        return tr().children(
            th().attr("scope", "row").cls("ps-4").children(
                code().text(order)
            ),
            td().text(customer),
            td().text(product),
            td().children(strong().text(amount)),
            td().children(
                span().cls("badge", statusCls).text(status)
            )
        );
    }

    private Element projectProgress() {
        var m = messages();
        return div().cls("card", "border-0", "shadow-sm", "h-100").children(
            div().cls("card-header", "bg-white", "border-bottom").children(
                h3().cls("h5", "fw-bold", "mb-0", "py-2").text(m.getString("dashboard.projects.title"))
            ),
            div().cls("card-body").children(
                projectItem("Project Alpha", 85, "text-success"),
                hr().cls("my-3"),
                projectItem("Project Beta", 62, "text-primary"),
                hr().cls("my-3"),
                projectItem("Project Gamma", 38, "text-warning")
            )
        );
    }

    private Element projectItem(String name, int percent, String colorCls) {
        return div().children(
            div().cls("d-flex", "justify-content-between", "mb-2").children(
                span().cls("fw-semibold", "small").text(name),
                span().cls("small", colorCls).text(percent + "%")
            ),
            new JuxProgress(percent, name).render()
        );
    }

    // ── Activity Feed ─────────────────────────────────────────

    private Element activitySection() {
        var m = messages();
        return section().cls("py-4", "pb-5").children(
            div().cls("container").children(
                div().cls("card", "border-0", "shadow-sm").children(
                    div().cls("card-header", "bg-white", "border-bottom").children(
                        h3().cls("h5", "fw-bold", "mb-0", "py-2").text(m.getString("dashboard.activity.title"))
                    ),
                    div().cls("card-body", "p-0").children(
                        ul().cls("list-group", "list-group-flush").children(
                            activityItem("bi-person-plus-fill", "text-primary",
                                m.getString("dashboard.activity.user.title"),
                                m.getString("dashboard.activity.user.desc"),
                                m.getString("dashboard.activity.user.time")),
                            activityItem("bi-cart-check-fill", "text-success",
                                m.getString("dashboard.activity.order.title"),
                                m.getString("dashboard.activity.order.desc"),
                                m.getString("dashboard.activity.order.time")),
                            activityItem("bi-arrow-up-circle-fill", "text-info",
                                m.getString("dashboard.activity.update.title"),
                                m.getString("dashboard.activity.update.desc"),
                                m.getString("dashboard.activity.update.time")),
                            activityItem("bi-exclamation-triangle-fill", "text-warning",
                                m.getString("dashboard.activity.storage.title"),
                                m.getString("dashboard.activity.storage.desc"),
                                m.getString("dashboard.activity.storage.time")),
                            activityItem("bi-shield-check", "text-success",
                                m.getString("dashboard.activity.security.title"),
                                m.getString("dashboard.activity.security.desc"),
                                m.getString("dashboard.activity.security.time"))
                        )
                    )
                )
            )
        );
    }

    private Element activityItem(String icon, String iconColor, String title,
                                 String description, String time) {
        return li().cls("list-group-item", "px-4", "py-3").children(
            div().cls("d-flex", "align-items-start").children(
                div().cls("me-3", "mt-1").children(
                    Element.of("i").cls("bi", icon, "fs-5", iconColor)
                        .ariaHidden(true)
                ),
                div().cls("flex-grow-1").children(
                    div().cls("d-flex", "justify-content-between", "align-items-center").children(
                        strong().cls("small").text(title),
                        small().cls("text-secondary").text(time)
                    ),
                    p().cls("mb-0", "small", "text-secondary").text(description)
                )
            )
        );
    }
}
