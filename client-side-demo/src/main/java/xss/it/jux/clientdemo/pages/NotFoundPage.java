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
 * 404 "Page Not Found" catch-all route.
 *
 * <p>This page matches any URL that is not handled by a more specific route.
 * It uses {@code priority = Integer.MAX_VALUE} so it is always evaluated
 * <em>last</em> in the route resolution chain, acting as the final fallback.</p>
 *
 * <p>The page sets an HTTP 404 status code via {@link PageMeta#status(int)}
 * and displays a clean, modern error screen with the "404" heading, a
 * friendly message, and a "Go Home" button.</p>
 *
 * <h2>Visual Design</h2>
 * <p>The page uses a centred, minimal layout with:</p>
 * <ul>
 *   <li>A large "404" heading rendered with the {@code gradient-text} CSS
 *       class for a violet-to-purple gradient fill.</li>
 *   <li>A descriptive message explaining that the page was not found.</li>
 *   <li>A prominent "Go Home" button to navigate back to the root URL.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.4.2 Page Titled</b> -- the {@code <title>} is set to
 *       "Page Not Found" so screen readers announce the error state.</li>
 *   <li><b>2.4.4 Link Purpose</b> -- the "Go Home" button clearly
 *       communicates where it navigates.</li>
 *   <li><b>1.3.1 Info and Relationships</b> -- the page uses proper
 *       heading hierarchy ({@code <h1>} for the 404 code) and semantic
 *       {@code <section>} wrapper.</li>
 * </ul>
 *
 * <h2>i18n</h2>
 * <p>All user-visible text is resolved through the {@link JuxMessages}
 * service.</p>
 *
 * @see HomePage
 * @see DemoLayout
 */
@Route(value = "/**", priority = Integer.MAX_VALUE)
@Title("Page Not Found")
public class NotFoundPage extends Page {

    // ── Page Metadata ────────────────────────────────────────────────

    /**
     * Sets the HTTP response status to 404 (Not Found).
     *
     * <p>This ensures that search engines do not index this page and that
     * HTTP clients receive the correct status code, even though the page
     * renders a full HTML document with a friendly error message.</p>
     *
     * @return a {@link PageMeta} builder with the 404 status code
     */
    @Override
    public PageMeta pageMeta() {
        return PageMeta.create().status(404);
    }

    // ── Render ───────────────────────────────────────────────────────

    /**
     * Build the full page by wrapping the 404 content inside the shared
     * {@link DemoLayout} shell.
     *
     * <p>The active path is set to an empty string ({@code ""}) so that
     * no navigation link in the {@link DemoNavbar} is highlighted as
     * current -- this accurately reflects that the user is not on any
     * known page.</p>
     *
     * @return the complete page element tree
     */
    @Override
    public Element render() {
        return new DemoLayout("", messages(), errorContent()).render();
    }

    /**
     * Build the 404 error content -- a centred, vertically-spaced layout
     * with a large "404" heading, a message, and a navigation button.
     *
     * <p>The content is wrapped in a {@code <section>} with generous
     * vertical padding to centre it vertically within the viewport
     * (the layout's flex-1 {@code <main>} handles the remaining space).</p>
     *
     * @return the error content {@code <section>} element
     */
    private Element errorContent() {
        var m = messages();

        return section()
                /* Full-width centred container with generous vertical padding
                 * to push the content toward the visual centre of the page. */
                .cls("py-32", "px-4", "sm:px-6", "lg:px-8")
                .children(
                        div().cls("max-w-lg", "mx-auto", "text-center")
                                .children(
                                        /* ── 404 Code ─────────────────────────────────────
                                         * A large, bold heading with a gradient text fill
                                         * that visually dominates the page. The gradient
                                         * class (gradient-text, defined in demo.css) applies
                                         * a violet-to-purple gradient fill on the text. */
                                        h1().cls("text-8xl", "sm:text-9xl", "font-extrabold",
                                                        "gradient-text", "mb-6")
                                                .text(m.getString("notfound.code")),

                                        /* ── Error title ──────────────────────────────────
                                         * A secondary heading that explains the 404 code
                                         * in human-friendly terms. Uses h2 to maintain
                                         * heading hierarchy (h1 = "404" above). */
                                        h2().cls("text-2xl", "sm:text-3xl", "font-bold",
                                                        "text-white", "mb-4")
                                                .text(m.getString("notfound.title")),

                                        /* ── Description ──────────────────────────────────
                                         * A brief, friendly explanation that the requested
                                         * page does not exist, guiding the user toward
                                         * recovery (the "Go Home" button below). */
                                        p().cls("text-gray-400", "text-lg", "mb-10")
                                                .text(m.getString("notfound.text")),

                                        /* ── Go Home button ───────────────────────────────
                                         * A prominent CTA button that navigates the user
                                         * back to the home page. Styled as a filled violet
                                         * button with hover/transition effects. Meets the
                                         * 24x24 minimum target size (WCAG 2.5.8). */
                                        a().attr("href", "/")
                                                .cls("inline-flex", "items-center", "px-8", "py-3",
                                                        "rounded-lg", "bg-violet-600", "text-white",
                                                        "font-semibold", "hover:bg-violet-500",
                                                        "transition-colors", "text-sm", "sm:text-base")
                                                .text(m.getString("notfound.home"))
                                )
                );
    }
}
