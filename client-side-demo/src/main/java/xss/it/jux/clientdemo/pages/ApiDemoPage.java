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
 * API Integration demo page showcasing client-side components that interact
 * with REST endpoints.
 *
 * <p>This page demonstrates six distinct API interaction patterns that
 * developers commonly encounter when building data-driven web applications
 * with JUX. Each pattern is implemented as a self-contained widget that
 * communicates with server-side REST endpoints, showing how JUX's
 * client-side model ({@code @JuxComponent(clientSide = true)},
 * {@code @State}, {@code @On}, {@code @OnMount}) can be used to build
 * rich, interactive data views without leaving Java.</p>
 *
 * <h2>Page Structure</h2>
 * <ol>
 *   <li><b>Page Header</b> -- centred title and subtitle introducing the
 *       API integration demos with a gradient background.</li>
 *   <li><b>Demo Grid</b> -- six component demo blocks, each with:
 *       <ul>
 *         <li>A descriptive left panel (category badge, title, description)</li>
 *         <li>A live interactive widget on the right</li>
 *       </ul>
 *       Widgets showcased (in order):
 *       {@link UserBrowserWidget}, {@link QuoteMachineWidget},
 *       {@link DataTableWidget}, {@link FormSubmitWidget},
 *       {@link PollWidget}, {@link ChartWidget}.
 *   </li>
 * </ol>
 *
 * <h2>API Interaction Patterns Demonstrated</h2>
 * <ul>
 *   <li><b>Data Fetching</b> -- on-demand button-triggered fetch
 *       ({@link UserBrowserWidget}), random-data fetch
 *       ({@link QuoteMachineWidget}), and paginated/sortable table
 *       ({@link DataTableWidget}).</li>
 *   <li><b>Form Submission</b> -- POST request with form data, validation,
 *       and success/error feedback ({@link FormSubmitWidget}).</li>
 *   <li><b>Real-Time</b> -- auto-refreshing dashboard that polls an
 *       endpoint at regular intervals ({@link PollWidget}).</li>
 *   <li><b>Visualization</b> -- fetching statistical data and rendering
 *       it as a chart ({@link ChartWidget}).</li>
 * </ul>
 *
 * <h2>Layout</h2>
 * <p>Each demo block uses a 5-column grid on large screens: 2 columns for
 * the info panel and 3 columns for the widget. On screens smaller than
 * {@code lg} the columns stack vertically. Alternating backgrounds
 * (transparent vs {@code bg-gray-900/50}) create visual separation
 * between adjacent demos.</p>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- each demo is a semantic
 *       {@code <section>} with a unique {@code id} anchor for deep linking
 *       and a descriptive {@code <h2>} heading.</li>
 *   <li><b>2.4.6 Headings and Labels</b> -- proper heading hierarchy is
 *       maintained: {@code <h1>} for the page header, {@code <h2>} for each
 *       component demo title.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- category badges use semantic
 *       {@code <span>} elements and do not interfere with landmark regions.</li>
 * </ul>
 *
 * <h2>i18n</h2>
 * <p>All user-visible text is resolved through the {@link JuxMessages}
 * service so the page renders correctly in every configured locale. Message
 * keys are prefixed with {@code api.} to avoid collisions with other pages.</p>
 *
 * @see ComponentsPage
 * @see HomePage
 * @see AboutPage
 * @see DemoLayout
 */
@Route("/api-demo")
@Title("API Integration \u2014 JUX Client-Side Demo")
@Meta(name = "description", content = "Client-side components that fetch data from REST endpoints: "
        + "user browser, quote machine, data table, form submission, live dashboard, and stats chart "
        + "\u2014 all built with JUX.")
public class ApiDemoPage extends Page {

    // ── Page Metadata ────────────────────────────────────────────────

    /**
     * Provides a programmatic meta description for this page.
     *
     * <p>While the {@code @Meta} annotation above sets the default, this
     * method allows runtime overrides if needed in the future (e.g.
     * locale-specific descriptions or dynamic content based on which
     * API endpoints are currently available).</p>
     *
     * @return a {@link PageMeta} builder with the page description
     */
    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .description("Client-side components that fetch data from REST endpoints: "
                        + "user browser, quote machine, data table, form submission, "
                        + "live dashboard, and stats chart.");
    }

    // ── Render ───────────────────────────────────────────────────────

    /**
     * Build the full page by wrapping the content inside the shared
     * {@link DemoLayout} shell.
     *
     * <p>The layout receives the active path {@code "/api-demo"} so the
     * navbar can highlight the correct navigation link, the current
     * locale's message bundle for i18n, and the assembled page content
     * as the main body.</p>
     *
     * @return the complete page element tree including the layout chrome
     *         (navbar, footer) and the API demo content
     */
    @Override
    public Element render() {
        return new DemoLayout("/api-demo", messages(), pageContent()).render();
    }

    /**
     * Assemble the page header and all six API demo sections into a
     * single wrapper element.
     *
     * <p>The demos are ordered to progressively introduce more complex
     * API interaction patterns: simple data fetching first, then form
     * submission, real-time polling, and finally data visualization.</p>
     *
     * @return a {@code <div>} containing the header and all component demos
     */
    private Element pageContent() {
        return div().children(
                /* ── Page header with gradient background ── */
                pageHeader(),

                /* ── Demo 1: User Browser — on-demand data fetching ── */
                componentDemo(
                        /* index */ 0,
                        /* id */ "user-browser",
                        /* category */ messages().getString("api.category.fetch"),
                        /* title */ messages().getString("api.users.title"),
                        /* description */ messages().getString("api.users.desc"),
                        /* widget */ new UserBrowserWidget()
                ),

                /* ── Demo 2: Quote Machine — random data fetching ── */
                componentDemo(
                        1,
                        "quote-machine",
                        messages().getString("api.category.fetch"),
                        messages().getString("api.quotes.title"),
                        messages().getString("api.quotes.desc"),
                        new QuoteMachineWidget()
                ),

                /* ── Demo 3: Data Table — paginated/sortable fetch ── */
                componentDemo(
                        2,
                        "data-table",
                        messages().getString("api.category.fetch"),
                        messages().getString("api.table.title"),
                        messages().getString("api.table.desc"),
                        new DataTableWidget()
                ),

                /* ── Demo 4: Form Submission — POST with validation ── */
                componentDemo(
                        3,
                        "form-submit",
                        messages().getString("api.category.form"),
                        messages().getString("api.form.title"),
                        messages().getString("api.form.desc"),
                        new FormSubmitWidget()
                ),

                /* ── Demo 5: Live Dashboard — auto-refreshing poll ── */
                componentDemo(
                        4,
                        "live-dashboard",
                        messages().getString("api.category.realtime"),
                        messages().getString("api.poll.title"),
                        messages().getString("api.poll.desc"),
                        new PollWidget()
                ),

                /* ── Demo 6: Stats Chart — data visualization ── */
                componentDemo(
                        5,
                        "stats-chart",
                        messages().getString("api.category.visualization"),
                        messages().getString("api.chart.title"),
                        messages().getString("api.chart.desc"),
                        new ChartWidget()
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
     * violet gradient background that fades to transparent, matching the
     * overall dark theme established by the other demo pages. The gradient
     * provides a visual cue that a new content area has begun.</p>
     *
     * <p>The header contains exactly one {@code <h1>} element (WCAG 2.4.2
     * requires every page to have a title), followed by a descriptive
     * subtitle paragraph explaining the purpose of this page.</p>
     *
     * @return the page header {@code <section>} element
     */
    private Element pageHeader() {
        /* Retrieve the i18n message bundle for the current locale */
        var m = messages();

        return section()
                /* Gradient background matching the hero style but more subdued.
                 * The gradient starts with a semi-transparent violet at the top
                 * and fades to fully transparent at the bottom, creating a
                 * smooth transition into the first demo section. */
                .cls("bg-gradient-to-b", "from-violet-600/20", "to-transparent",
                        "py-20", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        /* Constrained-width container centres the text and
                         * prevents overly wide lines on large screens. */
                        div().cls("max-w-4xl", "mx-auto", "text-center")
                                .children(
                                        /* Page title — the only h1 on this page.
                                         * WCAG 2.4.2 requires at least one page title,
                                         * and WCAG 2.4.6 requires a proper heading
                                         * hierarchy (h1 > h2 > h3...). */
                                        h1().cls("text-4xl", "sm:text-5xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("api.header.title")),

                                        /* Subtitle — describes what visitors will find
                                         * on this page. Uses a muted grey colour to
                                         * establish visual hierarchy below the title. */
                                        p().cls("text-lg", "text-gray-400", "max-w-2xl", "mx-auto")
                                                .text(m.getString("api.header.subtitle"))
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
     * stack vertically. Odd-indexed blocks receive a subtle dark
     * background ({@code bg-gray-900/50}) for visual alternation, while
     * even-indexed blocks remain transparent.</p>
     *
     * <p>Each demo block is wrapped in a semantic {@code <section>} element
     * with a unique {@code id} attribute, enabling deep linking via URL
     * fragments (e.g. {@code /api-demo#user-browser}).</p>
     *
     * @param index       the zero-based position of this demo in the page;
     *                    used to determine the alternating background colour
     *                    (even = transparent, odd = dark overlay)
     * @param id          the HTML anchor id for deep linking (e.g.
     *                    {@code "user-browser"}, {@code "quote-machine"})
     * @param category    the category badge text (e.g. "Data Fetching",
     *                    "Real-Time"), resolved from the i18n message bundle
     * @param title       the component title (e.g. "User Browser"),
     *                    rendered as an {@code <h2>} to maintain heading
     *                    hierarchy below the page's {@code <h1>}
     * @param description the component description text explaining what
     *                    this particular demo showcases
     * @param widget      the widget component to render (hydration markers added automatically)
     * @return the component demo {@code <section>} element
     */
    private Element componentDemo(int index, String id, String category,
                                  String title, String description, Component widget) {

        /* Determine background class based on even/odd index for
         * visual separation between adjacent demo blocks. Even indices
         * get a transparent background, odd indices get a subtle
         * semi-transparent dark overlay. */
        String bgClass = (index % 2 == 0) ? "bg-transparent" : "bg-gray-900/50";

        return section()
                /* Anchor id for deep linking (e.g. /api-demo#user-browser) */
                .id(id)
                /* Vertical and horizontal padding with the alternating
                 * background class applied for visual rhythm. */
                .cls("py-16", "px-4", "sm:px-6", "lg:px-8", bgClass)
                .children(
                        /* Constrained-width container centres the grid
                         * within the full-width section background. */
                        div().cls("max-w-7xl", "mx-auto")
                                .children(
                                        /* 5-column grid: 2 columns for info panel,
                                         * 3 columns for the widget panel. The items-start
                                         * alignment ensures the info panel stays at the
                                         * top when the widget is taller. */
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
     * <p>Contains three elements stacked vertically:</p>
     * <ol>
     *   <li>A <b>category badge</b> -- a small violet pill label that
     *       groups related demos (e.g. "Data Fetching", "Form Submission").
     *       Uses a semi-transparent violet background to tie in with
     *       the site's accent colour.</li>
     *   <li>A <b>title</b> -- rendered as an {@code <h2>} to maintain
     *       proper heading hierarchy below the page-level {@code <h1>}.
     *       The title identifies the specific widget being demonstrated.</li>
     *   <li>A <b>description</b> -- a paragraph of muted grey text
     *       explaining the API interaction pattern and what the widget
     *       does when the user interacts with it.</li>
     * </ol>
     *
     * @param category    the category badge text (already localised)
     * @param title       the component title (already localised)
     * @param description the component description (already localised)
     * @return the info panel {@code <div>} spanning 2 grid columns
     */
    private Element infoPanel(String category, String title, String description) {
        return div().cls("lg:col-span-2")
                .children(
                        /* Category badge — small rounded pill label above the title.
                         * Uses a semi-transparent violet background to tie in with
                         * the site's accent colour. The inline-block display ensures
                         * the pill only takes as much width as its content needs. */
                        span().cls("inline-block", "px-3", "py-1", "rounded-full",
                                        "bg-violet-500/10", "text-violet-400", "text-xs",
                                        "font-medium", "mb-4")
                                .text(category),

                        /* Component title — h2 maintains heading hierarchy
                         * (h1 is the page header above). Bold white text at
                         * 2xl size creates clear visual prominence. */
                        h2().cls("text-2xl", "font-bold", "text-white", "mb-3")
                                .text(title),

                        /* Description paragraph explaining what this component
                         * demonstrates and how it interacts with the API.
                         * Uses a relaxed line height for readability of
                         * longer description text. */
                        p().cls("text-gray-400", "leading-relaxed")
                                .text(description)
                );
    }

    /**
     * Build the widget panel that contains the live interactive component.
     *
     * <p>The panel spans 3 of the 5 grid columns and wraps the widget in
     * a dark card with a subtle border. This card provides consistent
     * visual framing for all widget types, regardless of their internal
     * layout or complexity.</p>
     *
     * <p>The card uses a semi-transparent dark background
     * ({@code bg-gray-800/50}) with large border radius ({@code rounded-2xl})
     * and a subtle border ({@code border-gray-700/50}) to create a
     * contained, elevated appearance without harsh edges.</p>
     *
     * @param widget the widget component to render
     * @return the widget panel {@code <div>} spanning 3 grid columns
     */
    private Element widgetPanel(Component widget) {
        return div().cls("lg:col-span-3")
                .children(
                        /* Dark card container — matches the feature card style
                         * from the home page and components page for visual
                         * consistency across the entire demo application. */
                        div().cls("bg-gray-800/50", "rounded-2xl", "p-6",
                                        "border", "border-gray-700/50")
                                .child(widget)
                );
    }
}
