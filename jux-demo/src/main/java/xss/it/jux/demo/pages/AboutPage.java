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
import xss.it.jux.theme.JuxAccordion;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * About page with framework info and FAQ accordion.
 */
@Route("/about")
@Title("About - JUX Demo")
@Meta(name = "description", content = "Learn about the JUX web framework - pure Java website development")
public class AboutPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    @Override
    public Element render() {
        return new PageLayout("/about", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
            headerSection(),
            storySection(),
            architectureSection(),
            faqSection()
        );
    }

    private Element headerSection() {
        var m = messages();
        return section().cls("bg-primary", "text-white", "py-5").children(
            div().cls("container", "py-3").children(
                h1().cls("display-5", "fw-bold").text(m.getString("about.header.title")),
                p().cls("lead", "mb-0", "opacity-75").text(m.getString("about.header.subtitle"))
            )
        );
    }

    private Element storySection() {
        var m = messages();
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("row").children(
                    div().cls("col-lg-8", "mx-auto").children(
                        h2().cls("fw-bold", "mb-4").text(m.getString("about.story.title")),
                        p().text(m.getString("about.story.p1")),
                        p().text(m.getString("about.story.p2")),
                        p().cls("text-secondary").text(m.getString("about.story.p3"))
                    )
                )
            )
        );
    }

    private Element architectureSection() {
        var m = messages();
        return section().cls("bg-light", "py-5").children(
            div().cls("container").children(
                h2().cls("fw-bold", "mb-4", "text-center").text(m.getString("about.arch.title")),
                div().cls("row", "g-4").children(
                    moduleCard("jux-annotations", "about.arch.annotations"),
                    moduleCard("jux-core", "about.arch.core"),
                    moduleCard("jux-a11y", "about.arch.a11y"),
                    moduleCard("jux-i18n", "about.arch.i18n"),
                    moduleCard("jux-server", "about.arch.server"),
                    moduleCard("jux-themes", "about.arch.themes"),
                    moduleCard("jux-client", "about.arch.client"),
                    moduleCard("jux-processor", "about.arch.processor"),
                    moduleCard("jux-cms", "about.arch.cms")
                )
            )
        );
    }

    private Element moduleCard(String name, String descKey) {
        return div().cls("col-md-6", "col-lg-4").children(
            div().cls("card", "h-100", "border-0", "shadow-sm").children(
                div().cls("card-body").children(
                    h3().cls("h6", "fw-bold", "text-primary").text(name),
                    p().cls("card-text", "text-secondary", "small", "mb-0").text(messages().getString(descKey))
                )
            )
        );
    }

    private Element faqSection() {
        var m = messages();
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("col-lg-8", "mx-auto").children(
                    h2().cls("fw-bold", "mb-4", "text-center").text(m.getString("about.faq.title")),
                    new JuxAccordion(List.of(
                        new JuxAccordion.Section(m.getString("about.faq.q1"),
                            p().text(m.getString("about.faq.a1"))),
                        new JuxAccordion.Section(m.getString("about.faq.q2"),
                            p().text(m.getString("about.faq.a2"))),
                        new JuxAccordion.Section(m.getString("about.faq.q3"),
                            p().text(m.getString("about.faq.a3"))),
                        new JuxAccordion.Section(m.getString("about.faq.q4"),
                            p().text(m.getString("about.faq.a4")))
                    )).render()
                )
            )
        );
    }
}
