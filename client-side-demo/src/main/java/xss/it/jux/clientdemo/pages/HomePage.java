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
 * Landing page for the JUX Client-Side Demo application.
 *
 * <p>This is the first page visitors see when navigating to the root URL
 * ({@code /}). It serves as both an introduction to JUX's client-side
 * capabilities and a live showcase of interactive components running
 * entirely in the browser -- all written in pure Java.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Hero Section</b> -- a full-width gradient banner with a tagline,
 *       two CTA buttons ("Explore Components" and "View Source"), and a dark
 *       code snippet card showing a minimal {@code @State} counter example.</li>
 *   <li><b>Features Grid</b> -- six feature cards arranged in a 3-column
 *       responsive grid (two rows), each highlighting a key aspect of the
 *       JUX client-side model: reactive state, event handling, lifecycle
 *       hooks, SSR + hydration, WCAG 2.2 AA compliance, and TeaVM
 *       compilation.</li>
 *   <li><b>Live Demo Preview</b> -- an embedded {@link CounterWidget}
 *       rendered inline so visitors can interact with a real JUX component
 *       immediately, without navigating away.</li>
 * </ol>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the page uses semantic
 *       {@code <section>} elements for each logical block, with proper
 *       heading hierarchy ({@code <h1>} in the hero, {@code <h2>} for
 *       subsequent sections).</li>
 *   <li><b>2.4.6 Headings and Labels</b> -- every section has a clear,
 *       descriptive heading.</li>
 *   <li><b>1.1.1 Non-text Content</b> -- decorative emoji icons are hidden
 *       from assistive technology via {@code aria-hidden="true"}.</li>
 *   <li><b>2.4.4 Link Purpose</b> -- CTA buttons have descriptive text
 *       that communicates their destination.</li>
 * </ul>
 *
 * <h2>i18n</h2>
 * <p>All user-visible text is resolved through the {@link JuxMessages}
 * service so the page renders correctly in every configured locale.</p>
 *
 * @see ComponentsPage
 * @see DemoLayout
 * @see CounterWidget
 */
@Route("/")
@Title("JUX Interactive \u2014 Client-Side Components in Pure Java")
@Meta(name = "description", content = "Explore interactive, client-side Java components built with JUX. "
        + "Reactive state, event handling, SSR hydration, and WCAG 2.2 AA \u2014 all in pure Java.")
public class HomePage extends Page {

    // ── Page Metadata ────────────────────────────────────────────────

    /**
     * Provides dynamic OpenGraph metadata for social sharing previews.
     *
     * <p>Sets the OG title, description, and type so that when this page
     * is shared on social media platforms, the link preview displays
     * meaningful information about the JUX client-side demo.</p>
     *
     * @return a {@link PageMeta} builder with OpenGraph properties configured
     */
    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .ogTitle("JUX Interactive \u2014 Client-Side Components in Pure Java")
                .ogDescription("Explore interactive, client-side Java components built with JUX. "
                        + "Reactive state, event handling, SSR hydration, and WCAG 2.2 AA.")
                .ogType("website");
    }

    // ── Render ───────────────────────────────────────────────────────

    /**
     * Build the full page by wrapping the content inside the shared
     * {@link DemoLayout} shell (navbar, skip-nav, footer).
     *
     * @return the complete page element tree
     */
    @Override
    public Element render() {
        return new DemoLayout("/", messages(), pageContent()).render();
    }

    /**
     * Assemble all three content sections into a single wrapper element.
     *
     * <p>The sections are stacked vertically in reading order: hero first,
     * features grid second, and the live demo preview third.</p>
     *
     * @return a {@code <div>} containing the hero, features, and live demo
     */
    private Element pageContent() {
        return div().children(
                heroSection(),
                featuresSection(),
                liveDemoSection()
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 1 — Hero
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the hero section -- a full-width gradient banner that introduces
     * the demo application with a heading, subtitle, CTA buttons, and a
     * decorative code snippet card.
     *
     * <p>Layout is a two-column grid on large screens: the left column holds
     * the badge, heading, subtitle, and CTAs; the right column holds a dark
     * code card showing a minimal {@code @State} counter example. On mobile
     * the columns stack vertically.</p>
     *
     * @return the hero {@code <section>} element
     */
    private Element heroSection() {
        /* Retrieve the i18n messages service once for reuse */
        var m = messages();

        return section()
                /* Full-width gradient background: violet -> purple -> indigo */
                .cls("bg-gradient-to-br", "from-violet-600", "via-purple-600", "to-indigo-700",
                        "py-24", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        /* Constrained-width container centred on the page */
                        div().cls("max-w-7xl", "mx-auto")
                                .children(
                                        /* Two-column grid: text left, code card right */
                                        div().cls("grid", "lg:grid-cols-2", "gap-12", "items-center")
                                                .children(
                                                        /* ── Left column: text content + CTAs ── */
                                                        heroTextColumn(m),
                                                        /* ── Right column: code snippet card ── */
                                                        heroCodeCard()
                                                )
                                )
                );
    }

    /**
     * Build the left side of the hero -- badge, heading, subtitle, and
     * two call-to-action buttons.
     *
     * @param m the messages service for i18n string lookup
     * @return a {@code <div>} containing the hero text and CTAs
     */
    private Element heroTextColumn(JuxMessages m) {
        return div().children(
                /* ── Badge pill ──────────────────────────────────────────
                 * A small rounded pill above the heading that reads
                 * "Pure Java -> Browser". The arrow character is part
                 * of the message string for easy localisation.
                 */
                span().cls("inline-block", "px-4", "py-1.5", "rounded-full",
                                "bg-white/10", "text-violet-200", "text-sm", "font-medium", "mb-6")
                        .text(m.getString("home.hero.badge")),

                /* ── Main heading ────────────────────────────────────────
                 * The h1 uses the "gradient-text" CSS class (defined in
                 * demo.css) to apply a gradient fill on the text.
                 * This is the only h1 on the page (WCAG 2.4.2).
                 */
                h1().cls("text-4xl", "sm:text-5xl", "lg:text-6xl", "font-bold",
                                "tracking-tight", "gradient-text", "mb-6")
                        .text(m.getString("home.hero.title")),

                /* ── Subtitle ────────────────────────────────────────────
                 * A brief supporting paragraph that elaborates on the h1.
                 */
                p().cls("text-lg", "sm:text-xl", "text-violet-100", "mb-8", "max-w-xl")
                        .text(m.getString("home.hero.subtitle")),

                /* ── CTA button row ──────────────────────────────────────
                 * Two buttons side by side: a primary filled button that
                 * navigates to /components, and a secondary outline button
                 * for viewing the source code. Both have generous padding
                 * and meet the 24x24 minimum target size (WCAG 2.5.8).
                 */
                div().cls("flex", "flex-wrap", "gap-4")
                        .children(
                                /* Primary CTA — filled violet button */
                                a().attr("href", "/components")
                                        .cls("inline-flex", "items-center", "px-8", "py-3",
                                                "rounded-lg", "bg-white", "text-violet-700",
                                                "font-semibold", "hover:bg-violet-50",
                                                "transition-colors", "text-sm", "sm:text-base")
                                        .text(m.getString("home.hero.cta.explore")),

                                /* Secondary CTA — outline ghost button */
                                a().attr("href", "https://github.com/xdsswar/jux-web")
                                        .attr("target", "_blank")
                                        .attr("rel", "noopener noreferrer")
                                        .cls("inline-flex", "items-center", "px-8", "py-3",
                                                "rounded-lg", "border", "border-white/30",
                                                "text-white", "font-semibold", "hover:bg-white/10",
                                                "transition-colors", "text-sm", "sm:text-base")
                                        .text(m.getString("home.hero.cta.source"))
                        )
        );
    }

    /**
     * Build the hero's right-side code snippet card.
     *
     * <p>Displays a dark-themed code block showing a minimal JUX counter
     * component that uses {@code @State} and {@code @On("click")}. This
     * gives visitors an immediate visual of what JUX code looks like,
     * reinforcing the "pure Java" message.</p>
     *
     * <p>The card is hidden on screens smaller than {@code lg} to avoid
     * cramped layouts on mobile devices.</p>
     *
     * @return a {@code <div>} containing the styled code block
     */
    private Element heroCodeCard() {
        return div().cls("hidden", "lg:block")
                .children(
                        /* Dark card with subtle border and rounded corners */
                        div().cls("bg-gray-900/80", "backdrop-blur", "rounded-2xl",
                                        "border", "border-gray-700/50", "p-6", "shadow-2xl")
                                .children(
                                        /* File name header — decorative dot indicators
                                         * mimicking a macOS title bar, followed by the
                                         * file name label. */
                                        div().cls("flex", "items-center", "gap-2", "mb-4")
                                                .children(
                                                        /* Red/yellow/green dots (purely decorative) */
                                                        span().cls("w-3", "h-3", "rounded-full", "bg-red-500")
                                                                .ariaHidden(true),
                                                        span().cls("w-3", "h-3", "rounded-full", "bg-yellow-500")
                                                                .ariaHidden(true),
                                                        span().cls("w-3", "h-3", "rounded-full", "bg-green-500")
                                                                .ariaHidden(true),
                                                        /* File name label */
                                                        span().cls("text-gray-400", "text-sm", "ml-2")
                                                                .text("Counter.java")
                                                ),

                                        /* Code listing — a <pre>/<code> block with the
                                         * counter component source. Uses a smaller text
                                         * size and a monospace font for readability. */
                                        pre().cls("text-sm", "leading-relaxed", "overflow-x-auto")
                                                .children(
                                                        code().cls("text-gray-300")
                                                                .text("@JuxComponent(clientSide = true)\n"
                                                                        + "public class Counter extends Component {\n\n"
                                                                        + "    @State private int count = 0;\n\n"
                                                                        + "    @Override\n"
                                                                        + "    public Element render() {\n"
                                                                        + "        return div().children(\n"
                                                                        + "            span().text(\"Count: \" + count),\n"
                                                                        + "            button().text(\"+\")\n"
                                                                        + "                .on(\"click\", e -> count++)\n"
                                                                        + "        );\n"
                                                                        + "    }\n"
                                                                        + "}")
                                                )
                                )
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 2 — Features Grid
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the features section -- a 3-column responsive grid showcasing
     * six key capabilities of the JUX client-side model.
     *
     * <p>Each card contains a decorative emoji icon (aria-hidden), a title,
     * and a description. All text is pulled from the messages bundle.</p>
     *
     * @return the features {@code <section>} element
     */
    private Element featuresSection() {
        var m = messages();

        return section().cls("py-24", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-7xl", "mx-auto")
                                .children(
                                        /* Section heading — centred above the grid */
                                        div().cls("text-center", "mb-16")
                                                .children(
                                                        h2().cls("text-3xl", "sm:text-4xl", "font-bold",
                                                                        "text-white", "mb-4")
                                                                .text(m.getString("home.features.title")),
                                                        p().cls("text-gray-400", "text-lg", "max-w-2xl", "mx-auto")
                                                                .text(m.getString("home.features.subtitle"))
                                                ),

                                        /* 3-column responsive grid — stacks to 1 on mobile,
                                         * 2 on medium, 3 on large screens. */
                                        div().cls("grid", "md:grid-cols-2", "lg:grid-cols-3", "gap-6")
                                                .children(
                                                        /* Row 1 — Reactive State, Event Handling, Lifecycle Hooks */
                                                        featureCard("\uD83D\uDD04",
                                                                m.getString("home.features.state.title"),
                                                                m.getString("home.features.state.desc")),
                                                        featureCard("\uD83D\uDD25",
                                                                m.getString("home.features.events.title"),
                                                                m.getString("home.features.events.desc")),
                                                        featureCard("\u267B\uFE0F",
                                                                m.getString("home.features.lifecycle.title"),
                                                                m.getString("home.features.lifecycle.desc")),

                                                        /* Row 2 — SSR + Hydration, WCAG 2.2 AA, TeaVM Compiled */
                                                        featureCard("\u26A1",
                                                                m.getString("home.features.ssr.title"),
                                                                m.getString("home.features.ssr.desc")),
                                                        featureCard("\u2705",
                                                                m.getString("home.features.a11y.title"),
                                                                m.getString("home.features.a11y.desc")),
                                                        featureCard("\uD83D\uDCE6",
                                                                m.getString("home.features.teavm.title"),
                                                                m.getString("home.features.teavm.desc"))
                                                )
                                )
                );
    }

    /**
     * Build a single feature card with an icon, title, and description.
     *
     * <p>The card uses a dark semi-transparent background with a subtle
     * border that transitions to a violet accent on hover, providing a
     * polished interactive feel. The icon emoji is wrapped in a {@code <span>}
     * with {@code aria-hidden="true"} because it is purely decorative.</p>
     *
     * @param icon        the emoji character to display (decorative)
     * @param title       the feature card title (already localised)
     * @param description the feature card description (already localised)
     * @return a feature card {@code <div>} element
     */
    private Element featureCard(String icon, String title, String description) {
        return div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                        "border", "border-gray-700/50", "hover:border-violet-500/50",
                        "transition", "group")
                .children(
                        /* Decorative emoji icon — hidden from screen readers
                         * since the title alone communicates the feature. */
                        span().cls("text-3xl", "mb-4", "block").ariaHidden(true).text(icon),

                        /* Feature title */
                        h3().cls("text-lg", "font-semibold", "text-white", "mb-2").text(title),

                        /* Feature description */
                        p().cls("text-gray-400", "text-sm", "leading-relaxed").text(description)
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  SECTION 3 — Live Demo Preview
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the live demo section -- an embedded {@link CounterWidget}
     * that visitors can interact with directly on the landing page.
     *
     * <p>This section sits on a subtle dark background to visually
     * distinguish it from the features grid above. A heading introduces
     * the demo, a short paragraph explains what it is, and the counter
     * widget is rendered in a styled container below.</p>
     *
     * @return the live demo {@code <section>} element
     */
    private Element liveDemoSection() {
        var m = messages();

        return section()
                .cls("py-24", "px-4", "sm:px-6", "lg:px-8", "bg-gray-900/50")
                .children(
                        div().cls("max-w-3xl", "mx-auto", "text-center")
                                .children(
                                        /* Section heading */
                                        h2().cls("text-3xl", "sm:text-4xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("home.demo.title")),

                                        /* Brief explanation of the live demo */
                                        p().cls("text-gray-400", "text-lg", "mb-12")
                                                .text(m.getString("home.demo.description")),

                                        /* Counter widget container — dark card wrapping the
                                         * actual interactive component. The component is
                                         * rendered server-side and hydrated on the client. */
                                        div().cls("bg-gray-800/50", "rounded-2xl", "p-8",
                                                        "border", "border-gray-700/50")
                                                .child(new CounterWidget())
                                )
                );
    }
}
