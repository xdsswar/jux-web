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
 * Main interactive components showcase page.
 *
 * <p>This page is the centrepiece of the Client-Side Demo application. It
 * renders every interactive widget built with JUX's client-side model --
 * {@code @JuxComponent(clientSide = true)}, {@code @State}, {@code @On},
 * {@code @OnMount}, and {@code @OnUnmount} -- so visitors can see and
 * interact with them in a single scrollable view.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Page Header</b> -- centred title and subtitle introducing the
 *       component gallery.</li>
 *   <li><b>Component Grid</b> -- eight component demo blocks, each with:
 *       <ul>
 *         <li>A descriptive left panel (category badge, title, description)</li>
 *         <li>A live interactive widget on the right</li>
 *       </ul>
 *       Components showcased (in order):
 *       {@link CounterWidget}, {@link TodoWidget}, {@link TabsWidget},
 *       {@link AccordionWidget}, {@link LiveSearchWidget},
 *       {@link StopwatchWidget}, {@link ModalWidget}, {@link ThemeToggleWidget}.
 *   </li>
 * </ol>
 *
 * <h2>Layout</h2>
 * <p>Each component demo block uses a 5-column grid on large screens:
 * 2 columns for the info panel and 3 columns for the widget. On smaller
 * screens the layout stacks vertically. Alternating backgrounds (transparent
 * vs {@code bg-gray-900/50}) create visual separation between demos.</p>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- each demo is a semantic
 *       {@code <section>} with an {@code id} anchor for deep linking and a
 *       descriptive {@code <h2>} heading.</li>
 *   <li><b>2.4.6 Headings and Labels</b> -- proper heading hierarchy is
 *       maintained: {@code <h1>} for the page header, {@code <h2>} for each
 *       component demo.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- category badges use semantic
 *       {@code <span>} elements and do not interfere with landmark regions.</li>
 * </ul>
 *
 * <h2>i18n</h2>
 * <p>All user-visible text is resolved through the {@link JuxMessages}
 * service so the page renders correctly in every configured locale.</p>
 *
 * @see HomePage
 * @see AboutPage
 * @see DemoLayout
 */
@Route("/components")
@Title("Interactive Components \u2014 JUX Client-Side Demo")
@Meta(name = "description", content = "Explore interactive Java components: counter, todo list, tabs, "
        + "accordion, live search, stopwatch, modal, and theme toggle \u2014 all built with JUX.")
public class ComponentsPage extends Page {

    // ── Page Metadata ────────────────────────────────────────────────

    /**
     * Provides a programmatic meta description for this page.
     *
     * <p>While the {@code @Meta} annotation above sets the default, this
     * method allows runtime overrides if needed in the future (e.g.
     * locale-specific descriptions).</p>
     *
     * @return a {@link PageMeta} builder with the page description
     */
    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .description("Explore interactive Java components: counter, todo list, tabs, "
                        + "accordion, live search, stopwatch, modal, and theme toggle.");
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
        return new DemoLayout("/components", messages(), pageContent()).render();
    }

    /**
     * Assemble the page header and all component demo sections into a
     * single wrapper element.
     *
     * @return a {@code <div>} containing the header and component demos
     */
    private Element pageContent() {
        return div().children(
                pageHeader(),
                componentDemo(
                        /* index */ 0,
                        /* id */ "counter",
                        /* category */ messages().getString("components.category.state"),
                        /* title */ messages().getString("components.counter.title"),
                        /* description */ messages().getString("components.counter.desc"),
                        /* widget */ new CounterWidget()
                ),
                componentDemo(
                        1,
                        "todo",
                        messages().getString("components.category.state"),
                        messages().getString("components.todo.title"),
                        messages().getString("components.todo.desc"),
                        new TodoWidget()
                ),
                componentDemo(
                        2,
                        "tabs",
                        messages().getString("components.category.navigation"),
                        messages().getString("components.tabs.title"),
                        messages().getString("components.tabs.desc"),
                        new TabsWidget()
                ),
                componentDemo(
                        3,
                        "accordion",
                        messages().getString("components.category.navigation"),
                        messages().getString("components.accordion.title"),
                        messages().getString("components.accordion.desc"),
                        new AccordionWidget()
                ),
                componentDemo(
                        4,
                        "live-search",
                        messages().getString("components.category.input"),
                        messages().getString("components.search.title"),
                        messages().getString("components.search.desc"),
                        new LiveSearchWidget()
                ),
                componentDemo(
                        5,
                        "stopwatch",
                        messages().getString("components.category.lifecycle"),
                        messages().getString("components.stopwatch.title"),
                        messages().getString("components.stopwatch.desc"),
                        new StopwatchWidget()
                ),
                componentDemo(
                        6,
                        "modal",
                        messages().getString("components.category.overlay"),
                        messages().getString("components.modal.title"),
                        messages().getString("components.modal.desc"),
                        new ModalWidget()
                ),
                componentDemo(
                        7,
                        "theme-toggle",
                        messages().getString("components.category.preference"),
                        messages().getString("components.theme.title"),
                        messages().getString("components.theme.desc"),
                        new ThemeToggleWidget()
                )
        );
    }

    // ══════════════════════════════════════════════════════════════════
    //  Page Header
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build the page header section with a centred title and subtitle.
     *
     * <p>Displayed at the top of the page, below the navbar. Uses a subtle
     * gradient background that matches the overall dark theme.</p>
     *
     * @return the page header {@code <section>} element
     */
    private Element pageHeader() {
        var m = messages();

        return section()
                /* Gradient background matching the hero style but more subdued */
                .cls("bg-gradient-to-b", "from-violet-600/20", "to-transparent",
                        "py-20", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-4xl", "mx-auto", "text-center")
                                .children(
                                        /* Page title — the only h1 on this page (WCAG 2.4.2) */
                                        h1().cls("text-4xl", "sm:text-5xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("components.header.title")),

                                        /* Subtitle — describes the purpose of this page */
                                        p().cls("text-lg", "text-gray-400", "max-w-2xl", "mx-auto")
                                                .text(m.getString("components.header.subtitle"))
                                )
                );
    }

    // ══════════════════════════════════════════════════════════════════
    //  Component Demo Block
    // ══════════════════════════════════════════════════════════════════

    /**
     * Build a single component demo block with an info panel on the left
     * and the live widget on the right.
     *
     * <p>The block uses a 5-column grid on large screens (info: 2 cols,
     * widget: 3 cols). On screens smaller than {@code lg} the columns
     * stack vertically. Even-indexed blocks receive a subtle dark
     * background ({@code bg-gray-900/50}) for visual alternation.</p>
     *
     * @param index       the zero-based position of this demo in the page
     *                    (used to determine alternating background)
     * @param id          the HTML anchor id for deep linking (e.g. "counter")
     * @param category    the category badge text (e.g. "State Management")
     * @param title       the component title (e.g. "Counter")
     * @param description the component description text
     * @param widget      the widget component to render (hydration markers added automatically)
     * @return the component demo {@code <section>} element
     */
    private Element componentDemo(int index, String id, String category,
                                  String title, String description, Component widget) {

        /* Determine background class based on even/odd index for
         * visual separation between adjacent demo blocks. */
        String bgClass = (index % 2 == 0) ? "bg-transparent" : "bg-gray-900/50";

        return section()
                /* Anchor id for deep linking (e.g. /components#counter) */
                .id(id)
                .cls("py-16", "px-4", "sm:px-6", "lg:px-8", bgClass)
                .children(
                        /* Constrained-width container */
                        div().cls("max-w-7xl", "mx-auto")
                                .children(
                                        /* 5-column grid: 2 for info, 3 for widget */
                                        div().cls("grid", "lg:grid-cols-5", "gap-12", "items-start")
                                                .children(
                                                        /* ── Info panel (2 columns) ── */
                                                        infoPanel(category, title, description),

                                                        /* ── Widget panel (3 columns) ── */
                                                        widgetPanel(widget)
                                                )
                                )
                );
    }

    /**
     * Build the info panel for a component demo block.
     *
     * <p>Contains a category badge (small violet pill), the component
     * title as an {@code <h2>}, and a description paragraph.</p>
     *
     * @param category    the category badge text
     * @param title       the component title
     * @param description the component description
     * @return the info panel {@code <div>} spanning 2 grid columns
     */
    private Element infoPanel(String category, String title, String description) {
        return div().cls("lg:col-span-2")
                .children(
                        /* Category badge — small rounded pill label above the title.
                         * Uses a semi-transparent violet background to tie in with
                         * the site's accent colour. */
                        span().cls("inline-block", "px-3", "py-1", "rounded-full",
                                        "bg-violet-500/10", "text-violet-400", "text-xs",
                                        "font-medium", "mb-4")
                                .text(category),

                        /* Component title — h2 maintains heading hierarchy
                         * (h1 is the page header above). */
                        h2().cls("text-2xl", "font-bold", "text-white", "mb-3")
                                .text(title),

                        /* Description paragraph explaining what this component
                         * demonstrates and how it works. */
                        p().cls("text-gray-400", "leading-relaxed")
                                .text(description)
                );
    }

    /**
     * Build the widget panel that contains the live interactive component.
     *
     * <p>The panel spans 3 of the 5 grid columns and wraps the widget in
     * a dark card with a subtle border. This card provides consistent
     * visual framing for all widget types.</p>
     *
     * @param widget the widget component to render
     * @return the widget panel {@code <div>} spanning 3 grid columns
     */
    private Element widgetPanel(Component widget) {
        return div().cls("lg:col-span-3")
                .children(
                        /* Dark card container — matches the feature card style
                         * from the home page for visual consistency. */
                        div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                                        "border", "border-gray-700/50")
                                .child(widget)
                );
    }
}
