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
 * Animation API demo page showcasing the {@code jux-animations} module.
 *
 * <p>This page presents five interactive animation widgets arranged in the
 * same 5-column grid layout used by {@link ComponentsPage}. Each widget
 * demonstrates a different part of the animation API: fade transitions,
 * easing interpolators, transform transitions, keyframe timelines, and
 * sequential vs parallel composition.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Page Header</b> -- centred title and subtitle introducing the
 *       animation demos.</li>
 *   <li><b>Animation Grid</b> -- five demo blocks, each with:
 *       <ul>
 *         <li>A descriptive left panel (category badge, title, description)</li>
 *         <li>A live interactive widget on the right</li>
 *       </ul>
 *       Widgets showcased (in order):
 *       {@link FadeDemoWidget}, {@link EasingShowcaseWidget},
 *       {@link TransformDemoWidget}, {@link TimelineDemoWidget},
 *       {@link SequenceDemoWidget}.
 *   </li>
 * </ol>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- each demo is a semantic
 *       {@code <section>} with a descriptive {@code <h2>} heading.</li>
 *   <li><b>2.4.6 Headings and Labels</b> -- proper heading hierarchy:
 *       {@code <h1>} for page header, {@code <h2>} for each demo.</li>
 * </ul>
 *
 * @see ComponentsPage
 * @see DemoLayout
 */
@Route("/animations")
@Title("Animations \u2014 JUX Client-Side Demo")
@Meta(name = "description", content = "Interactive demos of the jux-animations module: "
        + "fade, easing, transforms, timelines, and composite animations \u2014 all in Java.")
public class AnimationsPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .description("Interactive demos of the jux-animations module: "
                        + "fade, easing, transforms, timelines, and composite animations.");
    }

    @Override
    public Element render() {
        return new DemoLayout("/animations", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
                pageHeader(),
                componentDemo(
                        0,
                        "fade",
                        messages().getString("animations.category.transitions"),
                        messages().getString("animations.fade.title"),
                        messages().getString("animations.fade.desc"),
                        new FadeDemoWidget()
                ),
                componentDemo(
                        1,
                        "easing",
                        messages().getString("animations.category.easing"),
                        messages().getString("animations.easing.title"),
                        messages().getString("animations.easing.desc"),
                        new EasingShowcaseWidget()
                ),
                componentDemo(
                        2,
                        "transform",
                        messages().getString("animations.category.transitions"),
                        messages().getString("animations.transform.title"),
                        messages().getString("animations.transform.desc"),
                        new TransformDemoWidget()
                ),
                componentDemo(
                        3,
                        "timeline",
                        messages().getString("animations.category.timeline"),
                        messages().getString("animations.timeline.title"),
                        messages().getString("animations.timeline.desc"),
                        new TimelineDemoWidget()
                ),
                componentDemo(
                        4,
                        "sequence",
                        messages().getString("animations.category.composition"),
                        messages().getString("animations.sequence.title"),
                        messages().getString("animations.sequence.desc"),
                        new SequenceDemoWidget()
                )
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  Page Header
    // ══════════════════════════════════════════════════════════════════

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
                                                .text(m.getString("animations.header.title")),

                                        p().cls("text-lg", "text-gray-400", "max-w-2xl", "mx-auto")
                                                .text(m.getString("animations.header.subtitle"))
                                )
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  Component Demo Block (same pattern as ComponentsPage)
    // ══════════════════════════════════════════════════════════════════

    private Element componentDemo(int index, String id, String category,
                                  String title, String description, Component widget) {

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

    private Element widgetPanel(Component widget) {
        return div().cls("lg:col-span-3")
                .children(
                        div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                                        "border", "border-gray-700/50")
                                .child(widget)
                );
    }
}
