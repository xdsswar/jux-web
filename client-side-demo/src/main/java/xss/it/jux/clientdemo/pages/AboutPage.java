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
 * About page explaining how JUX's client-side model works.
 *
 * <p>This page provides educational content for developers who want to
 * understand the architecture behind the interactive components they see
 * in the demo. It covers the SSR-to-hydration flow, the technology stack,
 * and design decisions.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Header</b> -- centred title and subtitle introducing the page.</li>
 *   <li><b>How It Works</b> -- a styled card explaining the server-side
 *       rendering and client-side hydration pipeline in numbered steps.</li>
 *   <li><b>Technology Stack</b> -- a grid of technology cards/badges
 *       listing the core technologies: Java 25, Spring Boot, TeaVM,
 *       Tailwind CSS, and WCAG 2.2 AA.</li>
 * </ol>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- semantic {@code <section>}
 *       elements and proper heading hierarchy ({@code <h1>} for the page
 *       title, {@code <h2>} for each section, {@code <h3>} for technology
 *       card titles).</li>
 *   <li><b>2.4.6 Headings and Labels</b> -- every section has a
 *       descriptive heading that accurately describes its content.</li>
 *   <li><b>1.3.1</b> -- the numbered steps in "How It Works" use an
 *       ordered list ({@code <ol>}) for correct semantic structure.</li>
 * </ul>
 *
 * <h2>i18n</h2>
 * <p>All user-visible text is resolved through the {@link JuxMessages}
 * service.</p>
 *
 * @see HomePage
 * @see ComponentsPage
 * @see DemoLayout
 */
@Route("/about")
@Title("About \u2014 JUX Client-Side Demo")
@Meta(name = "description", content = "Learn how JUX builds interactive client-side components in pure Java "
        + "using SSR, TeaVM hydration, and WCAG 2.2 AA compliance.")
public class AboutPage extends Page {

    // ── Page Metadata ────────────────────────────────────────────────

    /**
     * Provides a minimal programmatic metadata object.
     *
     * <p>The static annotation-based title and meta description are
     * sufficient for this page, so the builder is returned empty.
     * This override exists as a hook for future locale-specific or
     * runtime-dynamic metadata.</p>
     *
     * @return a {@link PageMeta} builder (currently empty)
     */
    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    // ── Render ───────────────────────────────────────────────────────

    /**
     * Build the full page by wrapping the content inside the shared
     * {@link DemoLayout} shell.
     *
     * @return the complete page element tree
     */
    @Override
    public Element render() {
        return new DemoLayout("/about", messages(), pageContent()).render();
    }

    /**
     * Assemble all three content sections into a single wrapper element.
     *
     * @return a {@code <div>} containing the header, how-it-works, and
     *         technology stack sections
     */
    private Element pageContent() {
        return div().children(
                headerSection(),
                howItWorksSection(),
                techStackSection()
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 1 — Header
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the page header section with a centred title and subtitle.
     *
     * <p>Uses the same gradient-to-transparent style as the components
     * page header for visual consistency across the site.</p>
     *
     * @return the header {@code <section>} element
     */
    private Element headerSection() {
        var m = messages();

        return section()
                .cls("bg-gradient-to-b", "from-violet-600/20", "to-transparent",
                        "py-20", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-4xl", "mx-auto", "text-center")
                                .children(
                                        /* Page title — the only h1 on this page (WCAG 2.4.2) */
                                        h1().cls("text-4xl", "sm:text-5xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("about.header.title")),

                                        /* Subtitle — brief description of the page's purpose */
                                        p().cls("text-lg", "text-gray-400", "max-w-2xl", "mx-auto")
                                                .text(m.getString("about.header.subtitle"))
                                )
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 2 — How It Works
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the "How It Works" section explaining the SSR + hydration pipeline.
     *
     * <p>This section uses a dark card ({@code bg-gray-800/50}) to visually
     * group the explanation. Inside, a brief introduction is followed by an
     * ordered list of numbered steps that walk the reader through the
     * lifecycle of a JUX client-side component from server render to
     * browser interaction.</p>
     *
     * @return the how-it-works {@code <section>} element
     */
    private Element howItWorksSection() {
        var m = messages();

        return section()
                .cls("py-20", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-4xl", "mx-auto")
                                .children(
                                        /* Section heading */
                                        h2().cls("text-3xl", "font-bold", "text-white", "mb-8",
                                                        "text-center")
                                                .text(m.getString("about.howitworks.title")),

                                        /* Explanation card — dark card with border */
                                        div().cls("bg-gray-800/50", "rounded-2xl", "p-8",
                                                        "border", "border-gray-700/50")
                                                .children(
                                                        /* Introduction paragraph */
                                                        p().cls("text-gray-300", "text-lg", "mb-8",
                                                                        "leading-relaxed")
                                                                .text(m.getString("about.howitworks.intro")),

                                                        /* Ordered list of pipeline steps.
                                                         * Using <ol> for semantic correctness — screen readers
                                                         * announce "list, 5 items" and number each step. */
                                                        ol().cls("space-y-6")
                                                                .children(
                                                                        pipelineStep("1",
                                                                                m.getString("about.howitworks.step1.title"),
                                                                                m.getString("about.howitworks.step1.desc")),
                                                                        pipelineStep("2",
                                                                                m.getString("about.howitworks.step2.title"),
                                                                                m.getString("about.howitworks.step2.desc")),
                                                                        pipelineStep("3",
                                                                                m.getString("about.howitworks.step3.title"),
                                                                                m.getString("about.howitworks.step3.desc")),
                                                                        pipelineStep("4",
                                                                                m.getString("about.howitworks.step4.title"),
                                                                                m.getString("about.howitworks.step4.desc")),
                                                                        pipelineStep("5",
                                                                                m.getString("about.howitworks.step5.title"),
                                                                                m.getString("about.howitworks.step5.desc"))
                                                                )
                                                )
                                )
                );
    }

    /**
     * Build a single numbered step in the "How It Works" pipeline.
     *
     * <p>Each step is rendered as an {@code <li>} containing a row with a
     * numbered badge on the left and the step title + description on the
     * right. The badge uses a violet gradient circle to visually anchor
     * the step number.</p>
     *
     * @param number      the step number (e.g. "1", "2")
     * @param title       the step title (already localised)
     * @param description the step description (already localised)
     * @return an {@code <li>} element representing one pipeline step
     */
    private Element pipelineStep(String number, String title, String description) {
        return li().cls("flex", "gap-4", "items-start")
                .children(
                        /* Numbered badge — a circular violet gradient indicator.
                         * The number is purely visual (aria-hidden) because the
                         * <ol> already provides implicit numbering for AT. */
                        span().cls("flex-shrink-0", "w-10", "h-10", "rounded-full",
                                        "bg-violet-600", "flex", "items-center", "justify-center",
                                        "text-white", "font-bold", "text-sm")
                                .ariaHidden(true)
                                .text(number),

                        /* Step content — title and description */
                        div().children(
                                /* Step title */
                                strong().cls("text-white", "text-lg", "block", "mb-1")
                                        .text(title),
                                /* Step description */
                                p().cls("text-gray-400", "leading-relaxed")
                                        .text(description)
                        )
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 3 — Technology Stack
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the technology stack section displaying the core technologies
     * used by JUX as styled cards.
     *
     * <p>Technologies are shown in a responsive grid: 1 column on mobile,
     * 2 on medium, 3 on large screens. Each card has an icon (decorative),
     * the technology name, and a brief description of its role.</p>
     *
     * @return the tech stack {@code <section>} element
     */
    private Element techStackSection() {
        var m = messages();

        return section()
                .cls("py-20", "px-4", "sm:px-6", "lg:px-8", "bg-gray-900/50")
                .children(
                        div().cls("max-w-5xl", "mx-auto")
                                .children(
                                        /* Section heading */
                                        h2().cls("text-3xl", "font-bold", "text-white", "mb-4",
                                                        "text-center")
                                                .text(m.getString("about.tech.title")),

                                        /* Section subtitle */
                                        p().cls("text-gray-400", "text-center", "mb-12", "max-w-2xl",
                                                        "mx-auto")
                                                .text(m.getString("about.tech.subtitle")),

                                        /* Technology cards grid */
                                        div().cls("grid", "md:grid-cols-2", "lg:grid-cols-3", "gap-6")
                                                .children(
                                                        techCard("\u2615",
                                                                m.getString("about.tech.java.title"),
                                                                m.getString("about.tech.java.desc")),
                                                        techCard("\uD83C\uDF31",
                                                                m.getString("about.tech.spring.title"),
                                                                m.getString("about.tech.spring.desc")),
                                                        techCard("\uD83D\uDD27",
                                                                m.getString("about.tech.teavm.title"),
                                                                m.getString("about.tech.teavm.desc")),
                                                        techCard("\uD83C\uDFA8",
                                                                m.getString("about.tech.tailwind.title"),
                                                                m.getString("about.tech.tailwind.desc")),
                                                        techCard("\u267F",
                                                                m.getString("about.tech.wcag.title"),
                                                                m.getString("about.tech.wcag.desc")),
                                                        techCard("\uD83C\uDF10",
                                                                m.getString("about.tech.i18n.title"),
                                                                m.getString("about.tech.i18n.desc"))
                                                )
                                )
                );
    }

    /**
     * Build a single technology card with an icon, title, and description.
     *
     * <p>Each card uses the same dark semi-transparent style as the feature
     * cards on the home page for visual consistency. The hover border
     * transition to violet provides a polished interactive feel.</p>
     *
     * @param icon        the emoji character to display (decorative)
     * @param title       the technology name (already localised)
     * @param description the technology description (already localised)
     * @return a technology card {@code <div>} element
     */
    private Element techCard(String icon, String title, String description) {
        return div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                        "border", "border-gray-700/50", "hover:border-violet-500/50",
                        "transition")
                .children(
                        /* Decorative emoji icon — hidden from screen readers
                         * since the title alone communicates the technology. */
                        span().cls("text-3xl", "mb-4", "block").ariaHidden(true).text(icon),

                        /* Technology name */
                        h3().cls("text-lg", "font-semibold", "text-white", "mb-2").text(title),

                        /* Technology role description */
                        p().cls("text-gray-400", "text-sm", "leading-relaxed").text(description)
                );
    }
}
