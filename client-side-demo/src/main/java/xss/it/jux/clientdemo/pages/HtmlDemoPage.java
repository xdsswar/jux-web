/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.clientdemo.components.*;

import static xss.it.jux.core.Elements.*;

/**
 * HTML template demo page showcasing components loaded via the jux-html module.
 *
 * <p>This page demonstrates how JUX components can load their visual layout
 * from external HTML template files rather than building the entire element
 * tree programmatically. The jux-html module provides {@code @Html},
 * {@code @HtmlId}, and {@code @Slot} annotations that bridge HTML templates
 * with Java component logic.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Page Header</b> — centred title and subtitle introducing the
 *       HTML template integration demos with a gradient background.</li>
 *   <li><b>Demo Grid</b> — four component demo blocks, each with:
 *       <ul>
 *         <li>A descriptive left panel (category badge, title, description)</li>
 *         <li>A live rendered component on the right</li>
 *       </ul>
 *       Components showcased (in order):
 *       {@link ProfileCardHtml}, {@link PricingTableHtml},
 *       {@link TimelineHtml}, {@link DashboardPanelHtml}.
 *   </li>
 * </ol>
 *
 * <h2>JUX-HTML Patterns Demonstrated</h2>
 * <ul>
 *   <li><b>Template Loading</b> — {@code @Html} + {@code @HtmlId} for basic
 *       template loading and element injection ({@link ProfileCardHtml}).</li>
 *   <li><b>Slot Injection</b> — {@code @Slot} for injecting dynamically
 *       generated content into template placeholders ({@link PricingTableHtml}).</li>
 *   <li><b>Initializable Lifecycle</b> — the {@code Initializable} interface
 *       for post-injection setup ({@link TimelineHtml}).</li>
 *   <li><b>Data Population</b> — multiple {@code @HtmlId} injections combined
 *       with {@code @Slot} content for data-dense layouts ({@link DashboardPanelHtml}).</li>
 * </ul>
 *
 * @see ProfileCardHtml
 * @see PricingTableHtml
 * @see TimelineHtml
 * @see DashboardPanelHtml
 * @see DemoLayout
 */
@Route("/html-demo")
@Title("HTML Templates \u2014 JUX Client-Side Demo")
@Meta(name = "description", content = "Components loaded from HTML templates via the jux-html module: "
        + "profile card, pricing table, project timeline, and dashboard panel "
        + "\u2014 demonstrating @Html, @HtmlId, @Slot, and Initializable.")
public class HtmlDemoPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .description("Components loaded from HTML templates via the jux-html module: "
                        + "profile card, pricing table, project timeline, and dashboard panel.");
    }

    @Override
    public Element render() {
        return new DemoLayout("/html-demo", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
                pageHeader(),

                componentDemo(
                        0,
                        "profile-card",
                        messages().getString("html.category.template"),
                        messages().getString("html.profile.title"),
                        messages().getString("html.profile.desc"),
                        new ProfileCardHtml().render()
                ),

                componentDemo(
                        1,
                        "pricing-table",
                        messages().getString("html.category.slot"),
                        messages().getString("html.pricing.title"),
                        messages().getString("html.pricing.desc"),
                        new PricingTableHtml().render()
                ),

                componentDemo(
                        2,
                        "project-timeline",
                        messages().getString("html.category.lifecycle"),
                        messages().getString("html.timeline.title"),
                        messages().getString("html.timeline.desc"),
                        new TimelineHtml().render()
                ),

                componentDemo(
                        3,
                        "dashboard-panel",
                        messages().getString("html.category.data"),
                        messages().getString("html.dashboard.title"),
                        messages().getString("html.dashboard.desc"),
                        new DashboardPanelHtml().render()
                )
        );
    }

    private Element pageHeader() {
        var m = messages();
        return section()
                .cls("bg-gradient-to-b", "from-violet-600/20", "to-transparent",
                        "py-20", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-4xl", "mx-auto", "text-center")
                                .children(
                                        h1().cls("text-4xl", "sm:text-5xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("html.header.title")),
                                        p().cls("text-lg", "text-gray-400", "max-w-2xl", "mx-auto")
                                                .text(m.getString("html.header.subtitle"))
                                )
                );
    }

    private Element componentDemo(int index, String id, String category,
                                  String title, String description, Element widget) {
        String bgClass = (index % 2 == 0) ? "bg-transparent" : "bg-gray-900/50";

        return section()
                .id(id)
                .cls("py-16", "px-4", "sm:px-6", "lg:px-8", bgClass)
                .children(
                        div().cls("max-w-7xl", "mx-auto")
                                .children(
                                        div().cls("grid", "lg:grid-cols-5", "gap-12", "items-start")
                                                .children(
                                                        infoPanel(category, title, description),
                                                        widgetPanel(widget)
                                                )
                                )
                );
    }

    private Element infoPanel(String category, String title, String description) {
        return div().cls("lg:col-span-2")
                .children(
                        span().cls("inline-block", "px-3", "py-1", "rounded-full",
                                        "bg-violet-500/10", "text-violet-400", "text-xs",
                                        "font-medium", "mb-4")
                                .text(category),
                        h2().cls("text-2xl", "font-bold", "text-white", "mb-3")
                                .text(title),
                        p().cls("text-gray-400", "leading-relaxed")
                                .text(description)
                );
    }

    private Element widgetPanel(Element widget) {
        return div().cls("lg:col-span-3")
                .children(
                        div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                                        "border", "border-gray-700/50")
                                .children(widget)
                );
    }
}
