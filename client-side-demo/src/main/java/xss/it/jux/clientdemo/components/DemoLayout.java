/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Top-level page layout for every page in the Client-Side Demo application.
 *
 * <p>{@code DemoLayout} provides a consistent structural shell that wraps
 * page-specific content with shared chrome: a skip-navigation link for
 * keyboard/screen-reader users, the dark-themed {@link DemoNavbar}, the
 * main content area, and the {@link DemoFooter}.</p>
 *
 * <h2>Rendered Structure</h2>
 * <pre>{@code
 * <div class="flex flex-col min-h-screen bg-gray-950 text-white">
 *   <a class="jux-skip-nav" href="#main-content">Skip to main content</a>
 *   <header><!-- DemoNavbar --></header>
 *   <main id="main-content" class="flex-1">
 *     <!-- page content injected here -->
 *   </main>
 *   <footer><!-- DemoFooter --></footer>
 * </div>
 * }</pre>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.4.1 Bypass Blocks</b> — the skip-nav link lets keyboard users
 *       jump past the navbar directly to {@code #main-content}.</li>
 *   <li><b>Landmark regions</b> — {@code <header>}, {@code <main>}, and
 *       {@code <footer>} are semantic landmarks recognised by assistive
 *       technology, giving users an instant page outline.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @Override
 * public Element render() {
 *     return new DemoLayout("/", messages(),
 *         section().cls("py-16").children(
 *             h1().text("Hello World")
 *         )
 *     ).render();
 * }
 * }</pre>
 *
 * @see DemoNavbar
 * @see DemoFooter
 */
public class DemoLayout extends Component {

    /**
     * The URL path of the currently active page (e.g. {@code "/"} or
     * {@code "/components"}). Forwarded to {@link DemoNavbar} so it can
     * highlight the active navigation link and set {@code aria-current="page"}.
     */
    private final String activePath;

    /**
     * Localised message bundle for the current request locale.
     * Passed through to both the navbar and footer so they can resolve
     * all user-visible text via {@link JuxMessages#getString(String)}.
     */
    private final JuxMessages messages;

    /**
     * The page-specific content element tree to render inside the
     * {@code <main>} region. This is whatever the individual page
     * builds in its own {@code render()} method.
     */
    private final Element content;

    /**
     * Create a new layout wrapper.
     *
     * @param activePath the current page's URL path, used to highlight the
     *                   active navbar link (e.g. {@code "/"}, {@code "/components"})
     * @param messages   the i18n message bundle for the current request locale;
     *                   must not be {@code null}
     * @param content    the page body element tree to inject into the
     *                   {@code <main>} region; must not be {@code null}
     */
    public DemoLayout(String activePath, JuxMessages messages, Element content) {
        this.activePath = activePath;
        this.messages = messages;
        this.content = content;
    }

    /**
     * Build the full-page element tree.
     *
     * <p>The outermost {@code <div>} uses a flexbox column layout with
     * {@code min-h-screen} to ensure the footer is always pushed to the
     * bottom of the viewport, even on short pages. The dark background
     * ({@code bg-gray-950}) and white text ({@code text-white}) establish
     * the demo's dark colour scheme.</p>
     *
     * @return the root element containing skip-nav, navbar, main content,
     *         and footer
     */
    @Override
    public Element render() {
        return div().cls("flex", "flex-col", "min-h-screen", "bg-gray-950", "text-white")
                .children(

                        /* ── Skip Navigation (WCAG 2.4.1 — Bypass Blocks) ──────────
                         * A visually-hidden link that becomes visible on keyboard
                         * focus, allowing users to jump past the navbar directly
                         * to the main content area. The target ID "main-content"
                         * matches the id on the <main> element below.
                         */
                        skipNav("main-content", "Skip to main content"),

                        /* ── Navbar ─────────────────────────────────────────────────
                         * Dark-themed sticky header with brand, navigation links,
                         * and a GitHub link. The activePath tells the navbar which
                         * link to mark as current.
                         */
                        new DemoNavbar(activePath, messages).render(),

                        /* ── Main Content Area ──────────────────────────────────────
                         * The <main> landmark wraps the page-specific content.
                         * id="main-content" is the skip-nav anchor target.
                         * "flex-1" makes this region grow to fill available
                         * vertical space, pushing the footer to the bottom.
                         */
                        main_().id("main-content").cls("flex-1").children(content),

                        /* ── Footer ─────────────────────────────────────────────────
                         * Minimal dark footer with a tagline and copyright line.
                         */
                        new DemoFooter(messages).render()
                );
    }
}
