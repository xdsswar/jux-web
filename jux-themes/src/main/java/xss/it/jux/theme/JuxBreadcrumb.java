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

package xss.it.jux.theme;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible breadcrumb navigation component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a breadcrumb trail that shows the user's current location within
 * a site hierarchy. The component follows the WAI-ARIA Breadcrumb pattern
 * with proper navigation semantics and current-page indication.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>A {@code <nav>} element with {@code aria-label="Breadcrumb"} provides
 *       a named navigation landmark for screen reader users</li>
 *   <li>An {@code <ol>} (ordered list) conveys the hierarchical sequence</li>
 *   <li>The last crumb has {@code aria-current="page"} to indicate the
 *       current page (WCAG 2.4.8 -- Location)</li>
 *   <li>Visual separators between crumbs are injected via CSS pseudo-elements
 *       (not in the DOM) to avoid screen reader announcement of decorative characters</li>
 * </ul>
 *
 * <p><b>Accessibility rationale:</b></p>
 * <ul>
 *   <li>Using {@code <nav>} creates a navigation landmark that screen reader
 *       users can jump to directly from a landmark list</li>
 *   <li>Using {@code <ol>} instead of {@code <ul>} conveys that the items
 *       form an ordered sequence (parent to child)</li>
 *   <li>{@code aria-current="page"} on the last item tells screen readers
 *       "this is where you are" without relying on visual styling alone</li>
 *   <li>The last crumb is rendered as a {@code <span>} (not a link) because
 *       linking to the current page is redundant and confusing</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * var crumbs = List.of(
 *     new JuxBreadcrumb.Crumb("Home", "/"),
 *     new JuxBreadcrumb.Crumb("Products", "/products"),
 *     new JuxBreadcrumb.Crumb("Widget Pro", "/products/widget-pro")
 * );
 * child(new JuxBreadcrumb(crumbs));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/breadcrumb/">WAI-ARIA Breadcrumb Pattern</a>
 */
@JuxComponent
public class JuxBreadcrumb extends Component {

    /**
     * A single crumb in the breadcrumb trail.
     *
     * <p>Each crumb represents one level in the site hierarchy. The label
     * is the visible and accessible text; the href is the navigation
     * destination. The last crumb in the trail represents the current page
     * and is rendered differently (no link, {@code aria-current="page"}).</p>
     *
     * @param label the visible text for this breadcrumb level; must not be null
     * @param href  the URL for this level; may be null for the last crumb
     *              (current page) since it is rendered as a span, not a link
     */
    public record Crumb(String label, String href) {

        /**
         * Creates a new breadcrumb with validation.
         *
         * @param label the crumb text
         * @param href  the navigation URL (null allowed for the current page crumb)
         * @throws NullPointerException if label is null
         */
        public Crumb {
            Objects.requireNonNull(label, "Crumb label must not be null");
        }
    }

    /**
     * The ordered list of breadcrumb levels from root to current page.
     *
     * <p>The first crumb is typically "Home" or the site root. The last
     * crumb represents the current page. Intermediate crumbs represent
     * parent sections/categories in the hierarchy.</p>
     */
    private final List<Crumb> crumbs;

    /**
     * Creates a new breadcrumb navigation component.
     *
     * @param crumbs the ordered list of breadcrumb levels, from root to current page;
     *               must not be null or empty
     * @throws NullPointerException     if crumbs is null
     * @throws IllegalArgumentException if crumbs is empty
     */
    public JuxBreadcrumb(List<Crumb> crumbs) {
        Objects.requireNonNull(crumbs, "Crumbs list must not be null");
        if (crumbs.isEmpty()) {
            throw new IllegalArgumentException("Crumbs list must not be empty");
        }

        /* Defensive copy to prevent external mutation. */
        this.crumbs = List.copyOf(crumbs);
    }

    /**
     * Builds the breadcrumb navigation Element tree with ARIA landmark semantics.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <nav aria-label="Breadcrumb" class="jux-breadcrumb">
     *   <ol class="jux-breadcrumb-list">
     *     <li class="jux-breadcrumb-item">
     *       <a href="/">Home</a>
     *     </li>
     *     <li class="jux-breadcrumb-item">
     *       <a href="/products">Products</a>
     *     </li>
     *     <li class="jux-breadcrumb-item">
     *       <span aria-current="page">Widget Pro</span>
     *     </li>
     *   </ol>
     * </nav>
     * }</pre>
     *
     * <p>Visual separators (e.g. "/" or ">") between crumbs are not included
     * in the DOM. They should be added via CSS {@code ::before} pseudo-elements
     * on the {@code .jux-breadcrumb-item} class (except the first). This
     * approach prevents screen readers from announcing separator characters.</p>
     *
     * @return the breadcrumb nav Element with ordered list, never null
     */
    @Override
    public Element render() {
        List<Element> items = new ArrayList<>();
        int lastIndex = crumbs.size() - 1;

        for (int i = 0; i < crumbs.size(); i++) {
            Crumb crumb = crumbs.get(i);
            boolean isCurrentPage = (i == lastIndex);

            Element itemContent;
            if (isCurrentPage) {
                /*
                 * The last crumb is the current page. Render as a <span>
                 * (not a link) with aria-current="page". Linking to the
                 * current page would be redundant and confusing for
                 * screen reader users who would hear "link" but get no
                 * navigation benefit.
                 */
                itemContent = span()
                        .ariaCurrent("page")
                        .text(crumb.label());
            } else {
                /*
                 * Intermediate crumbs are rendered as links to their
                 * respective pages. The link text is descriptive
                 * (the actual section name, not "click here").
                 */
                itemContent = a()
                        .attr("href", crumb.href())
                        .text(crumb.label());
            }

            Element listItem = li()
                    .cls("jux-breadcrumb-item")
                    .children(itemContent);

            items.add(listItem);
        }

        /*
         * Use <ol> (ordered list) rather than <ul> because breadcrumbs
         * represent a sequential hierarchy from root to current page.
         * The order is semantically meaningful.
         */
        Element orderedList = ol()
                .cls("jux-breadcrumb-list")
                .children(items);

        /*
         * Wrap in a <nav> landmark with aria-label. Using "Breadcrumb"
         * as the label is a widely recognized convention that screen
         * reader users understand. If a page has multiple <nav> elements,
         * each should have a distinct aria-label to differentiate them
         * in the landmarks list.
         */
        return nav()
                .aria("label", "Breadcrumb")
                .cls("jux-breadcrumb")
                .children(orderedList);
    }
}
