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
 * Modern dark-themed navigation bar for the Client-Side Demo application.
 *
 * <p>The navbar is a sticky {@code <header>} element pinned to the top of the
 * viewport. It uses a semi-transparent dark background with a backdrop blur
 * ("frosted glass" effect) to remain readable over scrolling content.</p>
 *
 * <h2>Visual Structure</h2>
 * <pre>{@code
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  ⚡ JUX Interactive          Home  Components  About       [GH] │
 * └──────────────────────────────────────────────────────────────────┘
 * }</pre>
 *
 * <h2>Rendered HTML Outline</h2>
 * <pre>{@code
 * <header class="bg-gray-900/95 backdrop-blur ...">
 *   <div class="max-w-7xl mx-auto ...">
 *     <div class="flex items-center justify-between h-16">
 *       <a href="/">⚡ <span class="gradient-text">JUX Interactive</span></a>
 *       <nav aria-label="Main navigation">
 *         <a href="/" aria-current="page">Home</a>
 *         <a href="/components">Components</a>
 *         <a href="/about">About</a>
 *       </nav>
 *       <div>
 *         <a href="https://github.com/..." aria-label="GitHub"><!-- GH icon --></a>
 *       </div>
 *     </div>
 *   </div>
 * </header>
 * }</pre>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>4.1.2 Name, Role, Value</b> — the navigation block is wrapped in
 *       a {@code <nav>} landmark with {@code aria-label="Main navigation"} so
 *       assistive technology can announce it distinctly from other navs.</li>
 *   <li><b>2.4.8 Location</b> — the currently active link is marked with
 *       {@code aria-current="page"} and receives a distinct colour
 *       ({@code text-violet-400}) to provide both a visual and programmatic
 *       cue of the user's location within the site.</li>
 *   <li><b>2.4.4 Link Purpose</b> — the GitHub icon link includes an
 *       {@code aria-label} so screen readers announce "GitHub" rather than
 *       reading the raw SVG path data.</li>
 * </ul>
 *
 * @see DemoLayout
 */
public class DemoNavbar extends Component {

    /**
     * The URL path of the currently active page (e.g. {@code "/"} or
     * {@code "/components"}). Used by {@link #navLink(String, String)} to
     * determine which link should be highlighted and carry
     * {@code aria-current="page"}.
     */
    private final String activePath;

    /**
     * Localised message bundle for the current request locale.
     * All user-visible strings — brand name, link labels, ARIA labels —
     * are resolved through this bundle.
     */
    private final JuxMessages messages;

    /**
     * Create a new navbar component.
     *
     * @param activePath the current page's URL path (e.g. {@code "/"},
     *                   {@code "/components"}, {@code "/about"})
     * @param messages   the i18n message bundle; must not be {@code null}
     */
    public DemoNavbar(String activePath, JuxMessages messages) {
        this.activePath = activePath;
        this.messages = messages;
    }

    /**
     * Render the complete navbar element tree.
     *
     * <p>The outer {@code <header>} applies the dark glass effect via
     * Tailwind utilities: a 95 %-opaque grey-900 background, backdrop blur,
     * a subtle bottom border, sticky positioning, and a high z-index so it
     * floats above page content.</p>
     *
     * <p>Inside is a constrained-width container that holds a single flex
     * row with three sections: the brand on the left, main navigation links
     * in the centre, and the GitHub link on the right.</p>
     *
     * @return the {@code <header>} element tree
     */
    @Override
    public Element render() {
        return Element.of("header")
                .cls(
                        /* Dark glass effect: semi-transparent dark background
                         * with backdrop-blur so content scrolling beneath is
                         * slightly visible but the navbar stays readable. */
                        "bg-gray-900/95", "backdrop-blur", "border-b", "border-gray-800",
                        /* Sticky to viewport top with high z-index */
                        "sticky", "top-0", "z-50"
                )
                .children(
                        /* ── Container: centres content, provides horizontal padding ── */
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                .children(
                                        /* Single row: brand | nav links | GitHub icon */
                                        div().cls("flex", "items-center", "justify-between", "h-16")
                                                .children(
                                                        /* Left — brand / logo */
                                                        brand(),
                                                        /* Centre — main navigation links */
                                                        mainNav(),
                                                        /* Right — GitHub icon link */
                                                        rightActions()
                                                )
                                )
                );
    }

    // ──────────────────────────────────────────────────────────────────
    //  Private helper methods for each navbar section
    // ──────────────────────────────────────────────────────────────────

    /**
     * Build the brand element — a clickable link with a lightning bolt icon
     * and the "JUX Interactive" text rendered with a gradient effect.
     *
     * <p>The lightning bolt emoji is wrapped in a {@code <span>} with
     * {@code aria-hidden="true"} because it is purely decorative; the brand
     * text alone is sufficient for screen readers.</p>
     *
     * @return the brand anchor element
     */
    private Element brand() {
        return a().attr("href", "/")
                .cls("flex", "items-center", "gap-2", "font-bold", "text-xl")
                .children(
                        /* Lightning bolt icon — decorative, hidden from AT */
                        span().ariaHidden(true).text("\u26A1"),
                        /* Brand text with CSS gradient fill (see demo.css .gradient-text) */
                        span().cls("gradient-text")
                                .text(messages.getString("nav.brand"))
                );
    }

    /**
     * Build the main navigation link group.
     *
     * <p>Wrapped in a {@code <nav>} landmark with an {@code aria-label}
     * so assistive technology can distinguish it from any other navigation
     * regions on the page. Links are hidden on small screens
     * ({@code hidden md:flex}) — a mobile hamburger menu could be added
     * in a future iteration.</p>
     *
     * @return the {@code <nav>} element containing Home, Components, and
     *         About links
     */
    private Element mainNav() {
        return nav().aria("label", messages.getString("nav.main.label"))
                .cls("hidden", "md:flex", "items-center", "gap-6")
                .children(
                        navLink("/", messages.getString("nav.home")),
                        navLink("/components", messages.getString("nav.components")),
                        navLink("/api-demo", messages.getString("nav.api")),
                        navLink("/html-demo", messages.getString("nav.html")),
                        navLink("/about", messages.getString("nav.about"))
                );
    }

    /**
     * Build a single navigation link with active-state detection.
     *
     * <p>A link is considered "active" when either:</p>
     * <ul>
     *   <li>The {@code activePath} exactly matches the link's {@code href}
     *       (e.g. both are {@code "/"}).</li>
     *   <li>The link's {@code href} is longer than one character and the
     *       {@code activePath} starts with it (e.g. active path
     *       {@code "/components/counter"} matches link {@code "/components"}).</li>
     * </ul>
     *
     * <p>Active links receive the {@code text-violet-400} colour and
     * {@code aria-current="page"}. Inactive links use {@code text-gray-400}
     * with a hover transition to white.</p>
     *
     * @param href  the target URL path
     * @param label the visible link text (already localised)
     * @return the anchor element with appropriate styling and ARIA state
     */
    private Element navLink(String href, String label) {
        /* Determine whether this link matches the current page path */
        boolean isActive = activePath.equals(href)
                || (href.length() > 1 && activePath.startsWith(href));

        /* Base styling shared by both active and inactive states */
        var link = a().attr("href", href)
                .cls("text-sm", "font-medium", "transition-colors");

        if (isActive) {
            /* Active state: violet accent colour + ARIA current marker */
            link = link.cls("text-violet-400")
                    .ariaCurrent("page");
        } else {
            /* Inactive state: muted grey, brightens on hover */
            link = link.cls("text-gray-400", "hover:text-white");
        }

        return link.text(label);
    }

    /**
     * Build the right-side action area containing a GitHub icon link.
     *
     * <p>The GitHub icon is rendered as an inline SVG-style representation
     * using a simple text glyph. The link opens the JUX framework's GitHub
     * repository and includes an {@code aria-label} so screen readers
     * announce the destination meaningfully.</p>
     *
     * <p>The {@code target="_blank"} attribute opens the link in a new tab,
     * and {@code rel="noopener noreferrer"} prevents the opened page from
     * accessing the opener's {@code window} object (security best practice).</p>
     *
     * @return the wrapper {@code <div>} containing the GitHub link
     */
    private Element rightActions() {
        return div().cls("flex", "items-center", "gap-4")
                .children(
                        /* GitHub repository link */
                        a().attr("href", "https://github.com/xdsswar/jux-web")
                                .attr("target", "_blank")
                                .attr("rel", "noopener noreferrer")
                                .cls("text-gray-400", "hover:text-white", "transition-colors")
                                .aria("label", messages.getString("nav.github"))
                                .children(
                                        /* GitHub "octocat" represented as a simple SVG icon.
                                         * Using an inline SVG ensures no external font/image
                                         * dependency. The SVG is aria-hidden because the
                                         * parent <a> already has an aria-label. */
                                        Element.of("svg")
                                                .attr("xmlns", "http://www.w3.org/2000/svg")
                                                .attr("width", "20")
                                                .attr("height", "20")
                                                .attr("viewBox", "0 0 24 24")
                                                .attr("fill", "currentColor")
                                                .ariaHidden(true)
                                                .children(
                                                        Element.of("path")
                                                                .attr("d", "M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 "
                                                                        + "9.795 8.205 11.385.6.105.825-.255.825-.57 "
                                                                        + "0-.285-.015-1.04-.015-2.04-3.338.724-4.042"
                                                                        + "-1.61-4.042-1.61-.546-1.385-1.335-1.755-1."
                                                                        + "335-1.755-1.087-.744.084-.729.084-.729 1.205"
                                                                        + ".084 1.838 1.236 1.838 1.236 1.07 1.835 "
                                                                        + "2.809 1.305 3.495.998.108-.776.417-1.305"
                                                                        + ".76-1.605-2.665-.3-5.466-1.332-5.466-5.93"
                                                                        + " 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-"
                                                                        + "1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96"
                                                                        + "-.267 1.98-.399 3-.405 1.02.006 2.04.138 "
                                                                        + "3 .405 2.28-1.552 3.285-1.23 3.285-1.23"
                                                                        + ".645 1.653.24 2.873.12 3.176.765.84 1.23"
                                                                        + " 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475"
                                                                        + " 5.92.42.36.81 1.096.81 2.22 0 1.606-.015"
                                                                        + " 2.896-.015 3.286 0 .315.21.69.825.57C20"
                                                                        + ".565 21.795 24 17.31 24 12c0-6.63-5.37-12"
                                                                        + "-12-12z")
                                                )
                                )
                );
    }
}
