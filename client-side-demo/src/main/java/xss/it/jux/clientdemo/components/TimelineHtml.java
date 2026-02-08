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
import xss.it.jux.reactive.Initializable;

import static xss.it.jux.core.Elements.*;

/**
 * Project timeline component loaded from an HTML template via the jux-html module.
 *
 * <p>This component demonstrates the {@link Initializable} lifecycle callback
 * in combination with {@link Html @Html} template loading. After the template
 * is parsed and all {@code @HtmlId}/{@code @Slot} fields are injected, the
 * {@link HtmlLoader} detects that this component implements {@code Initializable}
 * and calls {@link #initialize()} automatically.</p>
 *
 * <h2>JUX-HTML Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code Initializable} lifecycle</b> — the {@code initialize()} method
 *       runs after template parsing and field injection, before the element tree
 *       is returned. This is the correct place for post-injection setup that
 *       depends on injected fields being populated.</li>
 *   <li><b>{@code @Slot} for footer content</b> — the timeline footer area
 *       is a slot that receives dynamically generated content showing a
 *       progress summary.</li>
 *   <li><b>Multiple {@code @HtmlId} injections</b> — four timeline steps
 *       each have date, title, and description elements injected and
 *       populated with milestone data.</li>
 * </ul>
 *
 * @see Html
 * @see HtmlId
 * @see Slot
 * @see Initializable
 * @see HtmlLoader
 */
@Html("components/timeline.html")
public class TimelineHtml extends Component implements Initializable {

    /* ── Step 1 ── */
    @HtmlId("step-1-date") private Element step1Date;
    @HtmlId("step-1-title") private Element step1Title;
    @HtmlId("step-1-desc") private Element step1Desc;

    /* ── Step 2 ── */
    @HtmlId("step-2-date") private Element step2Date;
    @HtmlId("step-2-title") private Element step2Title;
    @HtmlId("step-2-desc") private Element step2Desc;

    /* ── Step 3 ── */
    @HtmlId("step-3-date") private Element step3Date;
    @HtmlId("step-3-title") private Element step3Title;
    @HtmlId("step-3-desc") private Element step3Desc;

    /* ── Step 4 ── */
    @HtmlId("step-4-date") private Element step4Date;
    @HtmlId("step-4-title") private Element step4Title;
    @HtmlId("step-4-desc") private Element step4Desc;

    /* ── Footer slot ── */
    @Slot("timeline-footer")
    private Element timelineFooter;

    /**
     * Called automatically by {@link HtmlLoader} after all {@code @HtmlId} and
     * {@code @Slot} fields have been injected. Populates the timeline entries
     * with JUX framework milestone data.
     *
     * <p>This method demonstrates the {@link Initializable} pattern: rather
     * than doing setup in {@code render()}, the initialization logic runs
     * during the template loading pipeline, keeping {@code render()} clean.</p>
     */
    @Override
    public void initialize() {
        /* ── Step 1: Project Kickoff ── */
        step1Date.text("January 2026");
        step1Title.text("Project Kickoff");
        step1Desc.text("Framework architecture designed. Core module structure "
                + "established with jux-annotations, jux-core, and jux-server.");

        /* ── Step 2: Reactive & HTML Modules ── */
        step2Date.text("March 2026");
        step2Title.text("Reactive & HTML Modules");
        step2Desc.text("jux-reactive module completed with observable properties, "
                + "collections, and bindings. jux-html template loader implemented "
                + "with parser, caching, and expression resolution.");

        /* ── Step 3: Client-Side Integration ── */
        step3Date.text("June 2026");
        step3Title.text("Client-Side Integration");
        step3Desc.text("TeaVM compilation pipeline established. Client-side hydration, "
                + "@State reactivity, and DOM diffing implemented via jux-client.");

        /* ── Step 4: 1.0 Release ── */
        step4Date.text("September 2026");
        step4Title.text("1.0 Stable Release");
        step4Desc.text("Production-ready release with full WCAG 2.2 AA compliance, "
                + "CMS module, theme system, and comprehensive documentation.");

        /* ── Footer: progress summary ── */
        timelineFooter.children(
                div().cls("flex", "items-center", "justify-between").children(
                        div().children(
                                p().cls("text-sm", "text-gray-400").text("Overall Progress"),
                                p().cls("text-lg", "font-bold", "text-white").text("Phase 2 of 4")
                        ),
                        div().cls("flex", "items-center", "gap-2").children(
                                /* Progress bar track */
                                div().cls("w-32", "h-2", "bg-gray-700", "rounded-full", "overflow-hidden")
                                        .children(
                                                div().cls("h-full", "bg-violet-500", "rounded-full")
                                                        .style("width", "50%")
                                        ),
                                span().cls("text-sm", "text-violet-400", "font-medium").text("50%")
                        )
                )
        );
    }

    /**
     * Load the HTML template and return the fully populated element tree.
     *
     * <p>Since this component implements {@link Initializable}, the
     * {@code initialize()} method is called automatically during
     * {@link HtmlLoader#load(Object)}, so the returned tree is already
     * populated with all timeline data.</p>
     *
     * @return the root element of the timeline
     */
    @Override
    public Element render() {
        return HtmlLoader.load(this);
    }
}
