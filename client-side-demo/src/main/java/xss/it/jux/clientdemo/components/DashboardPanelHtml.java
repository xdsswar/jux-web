/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.html.HtmlLoader;
import xss.it.jux.html.annotation.Html;
import xss.it.jux.html.annotation.HtmlId;
import xss.it.jux.html.annotation.Slot;

import static xss.it.jux.core.Elements.*;

/**
 * Mini dashboard panel loaded from an HTML template via the jux-html module.
 *
 * <p>This component demonstrates loading a data-dense dashboard layout from
 * an HTML template and populating it with live metric values. The template
 * defines the grid structure and styling, while Java code provides the data
 * and builds the activity feed content for the slot area.</p>
 *
 * <h2>JUX-HTML Features Demonstrated</h2>
 * <ul>
 *   <li><b>Multiple {@code @HtmlId} injections</b> — eight separate
 *       elements are injected: four metric values and four trend indicators,
 *       plus the dashboard title and status badge.</li>
 *   <li><b>{@code @Slot} for dynamic content</b> — the dashboard content
 *       area is a slot that receives a dynamically generated activity feed
 *       built with the JUX element API.</li>
 *   <li><b>Data-driven rendering</b> — metric values, trend percentages,
 *       and activity items could come from a database or API in a real
 *       application. This demo uses static sample data.</li>
 * </ul>
 *
 * @see Html
 * @see HtmlId
 * @see Slot
 * @see HtmlLoader
 */
@Html("components/dashboard-panel.html")
public class DashboardPanelHtml extends Component {

    /* ── Header elements ── */
    @HtmlId("dashboard-title") private Element dashboardTitle;
    @HtmlId("dashboard-status") private Element dashboardStatus;

    /* ── Metric values ── */
    @HtmlId("metric-1-value") private Element metric1Value;
    @HtmlId("metric-1-trend") private Element metric1Trend;
    @HtmlId("metric-2-value") private Element metric2Value;
    @HtmlId("metric-2-trend") private Element metric2Trend;
    @HtmlId("metric-3-value") private Element metric3Value;
    @HtmlId("metric-3-trend") private Element metric3Trend;
    @HtmlId("metric-4-value") private Element metric4Value;
    @HtmlId("metric-4-trend") private Element metric4Trend;

    /* ── Content slot ── */
    @Slot("dashboard-content")
    private Element dashboardContent;

    /**
     * Load the dashboard template, populate metrics with sample data,
     * and inject a recent activity feed into the content slot.
     *
     * @return the root element of the dashboard panel
     */
    @Override
    public Element render() {
        Element root = HtmlLoader.load(this);

        /* ── Header ── */
        dashboardTitle.text("JUX Framework Metrics");
        dashboardStatus.text("All Systems Go");

        /* ── Metric 1: Total Users ── */
        metric1Value.text("12,847");
        metric1Trend.text("\u2191 12.5%");

        /* ── Metric 2: Revenue ── */
        metric2Value.text("$84.2k");
        metric2Trend.text("\u2191 8.3%");

        /* ── Metric 3: Uptime ── */
        metric3Value.text("99.97%");
        metric3Trend.text("\u2191 0.02%");

        /* ── Metric 4: Latency ── */
        metric4Value.text("42ms");
        metric4Trend.text("\u2193 3.1%");

        /* ── Content Slot: Recent Activity Feed ── */
        dashboardContent.children(
                p().cls("text-sm", "font-medium", "text-gray-400", "mb-3")
                        .text("Recent Activity"),
                div().cls("space-y-3").children(
                        activityItem("\uD83D\uDE80", "Deployment completed",
                                "Production v2.4.1 deployed", "2 min ago"),
                        activityItem("\u2705", "All tests passing",
                                "847 tests \u00b7 0 failures", "15 min ago"),
                        activityItem("\uD83D\uDC64", "New team member",
                                "Alice joined the project", "1 hour ago"),
                        activityItem("\uD83D\uDCCA", "Performance improved",
                                "SSR render time down to 3.2ms", "3 hours ago")
                )
        );

        return root;
    }

    /**
     * Build a single activity feed item.
     *
     * @param icon        the emoji/icon for this activity
     * @param title       the activity title
     * @param description the activity description
     * @param time        the relative time string
     * @return the activity item element
     */
    private Element activityItem(String icon, String title, String description, String time) {
        return div().cls("flex", "items-start", "gap-3").children(
                span().cls("text-lg").ariaHidden(true).text(icon),
                div().cls("flex-1", "min-w-0").children(
                        div().cls("flex", "items-center", "justify-between").children(
                                p().cls("text-sm", "font-medium", "text-white", "truncate").text(title),
                                span().cls("text-xs", "text-gray-500", "shrink-0").text(time)
                        ),
                        p().cls("text-xs", "text-gray-500", "mt-0.5").text(description)
                )
        );
    }
}
