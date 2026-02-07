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
 * About page with company story, values, stats, and team.
 */
@Route("/about")
@Title("About Us - JUX Store")
@Meta(name = "description", content = "Learn about JUX Store - our story, values, and team")
public class AboutPage extends Page {

    @Override
    public Element render() {
        var m = messages();
        return new StoreLayout("/about", m,
                div().children(
                        heroSection(),
                        valuesSection(),
                        statsSection(),
                        teamSection()
                )
        ).render();
    }

    private Element heroSection() {
        var m = messages();
        return section().cls("bg-indigo-600", "text-white", "py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8",
                                "text-center")
                        .children(
                                h1().cls("text-4xl", "font-bold", "mb-4")
                                        .text(m.getString("about.hero.title")),
                                p().cls("text-xl", "text-indigo-200", "max-w-2xl", "mx-auto")
                                        .text(m.getString("about.hero.subtitle"))
                        )
        );
    }

    private Element valuesSection() {
        var m = messages();
        return section().cls("py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        h2().cls("text-3xl", "font-bold", "text-gray-900", "text-center", "mb-10")
                                .text(m.getString("about.values.title")),
                        div().cls("grid", "grid-cols-1", "md:grid-cols-3", "gap-8").children(
                                valueCard("\uD83C\uDF1F",
                                        m.getString("about.values.quality.title"),
                                        m.getString("about.values.quality.text")),
                                valueCard("\uD83E\uDD1D",
                                        m.getString("about.values.trust.title"),
                                        m.getString("about.values.trust.text")),
                                valueCard("\uD83C\uDF0D",
                                        m.getString("about.values.sustainability.title"),
                                        m.getString("about.values.sustainability.text"))
                        )
                )
        );
    }

    private Element valueCard(String icon, String title, String text) {
        return div().cls("bg-white", "rounded-xl", "shadow-sm", "border", "border-gray-200",
                        "p-6", "text-center")
                .children(
                        div().cls("text-4xl", "mb-3").ariaHidden(true).text(icon),
                        h3().cls("font-semibold", "text-gray-900", "mb-2").text(title),
                        p().cls("text-gray-500", "text-sm").text(text)
                );
    }

    private Element statsSection() {
        var m = messages();
        return section().cls("bg-gray-50", "py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        div().cls("grid", "grid-cols-2", "md:grid-cols-4", "gap-8", "text-center")
                                .children(
                                        statItem("50K+", m.getString("about.stats.customers")),
                                        statItem("20K+", m.getString("about.stats.products_sold")),
                                        statItem("4.8", m.getString("about.stats.avg_rating")),
                                        statItem("99%", m.getString("about.stats.satisfaction"))
                                )
                )
        );
    }

    private Element statItem(String value, String label) {
        return div().children(
                div().cls("text-3xl", "font-bold", "text-indigo-600", "mb-1").text(value),
                div().cls("text-sm", "text-gray-500").text(label)
        );
    }

    private Element teamSection() {
        var m = messages();
        return section().cls("py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        h2().cls("text-3xl", "font-bold", "text-gray-900", "text-center", "mb-10")
                                .text(m.getString("about.team.title")),
                        div().cls("grid", "grid-cols-1", "sm:grid-cols-2", "lg:grid-cols-4",
                                        "gap-8")
                                .children(
                                        teamCard(m.getString("about.team.ceo.name"),
                                                m.getString("about.team.ceo.role"),
                                                "\uD83D\uDC69\u200D\uD83D\uDCBC"),
                                        teamCard(m.getString("about.team.cto.name"),
                                                m.getString("about.team.cto.role"),
                                                "\uD83D\uDC68\u200D\uD83D\uDCBB"),
                                        teamCard(m.getString("about.team.design.name"),
                                                m.getString("about.team.design.role"),
                                                "\uD83D\uDC69\u200D\uD83C\uDFA8"),
                                        teamCard(m.getString("about.team.ops.name"),
                                                m.getString("about.team.ops.role"),
                                                "\uD83D\uDC68\u200D\uD83D\uDD27")
                                )
                )
        );
    }

    private Element teamCard(String name, String role, String emoji) {
        return div().cls("text-center", "p-6", "bg-white", "rounded-xl", "shadow-sm",
                        "border", "border-gray-200")
                .children(
                        div().cls("text-5xl", "mb-3").ariaHidden(true).text(emoji),
                        h3().cls("font-semibold", "text-gray-900").text(name),
                        p().cls("text-sm", "text-gray-500").text(role)
                );
    }
}
