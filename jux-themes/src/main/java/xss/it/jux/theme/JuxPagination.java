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
 * Accessible pagination navigation component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a page navigation bar with previous/next buttons and numbered
 * page links. The component follows WAI-ARIA landmark and navigation
 * patterns to ensure screen reader users can understand and operate the
 * pagination controls.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>A {@code <nav>} element with {@code aria-label="Pagination"} creates
 *       a named navigation landmark</li>
 *   <li>The current page link has {@code aria-current="page"} to indicate
 *       the user's position in the page sequence</li>
 *   <li>Disabled previous/next buttons use {@code aria-disabled="true"}
 *       to inform screen readers that the control is non-interactive</li>
 *   <li>Each page link has descriptive text (e.g. "Page 3") via
 *       {@code aria-label} for clear screen reader announcements</li>
 * </ul>
 *
 * <p><b>Rendering behavior:</b></p>
 * <ul>
 *   <li>For 7 or fewer total pages, all page numbers are shown</li>
 *   <li>For more than 7 pages, an ellipsis ("...") truncation is applied
 *       to keep the control compact while always showing first, last,
 *       and pages adjacent to the current page</li>
 *   <li>Previous/Next buttons are always shown but disabled (with
 *       {@code aria-disabled}) when on the first/last page</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // Page 3 of 10, links go to /search?page=N
 * child(new JuxPagination(3, 10, "/search?page="));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/breadcrumb/">WAI-ARIA Navigation Landmark</a>
 */
@JuxComponent
public class JuxPagination extends Component {

    /**
     * The current page number (1-based).
     *
     * <p>This page is highlighted visually and marked with
     * {@code aria-current="page"} for screen readers. Must be between
     * 1 and {@link #totalPages} inclusive.</p>
     */
    private final int currentPage;

    /**
     * The total number of pages available.
     *
     * <p>Determines how many page links to render and whether
     * truncation (ellipsis) is needed. Must be at least 1.</p>
     */
    private final int totalPages;

    /**
     * The base URL for page links.
     *
     * <p>The page number is appended directly to this string to form
     * the full URL. For example, if baseUrl is {@code "/search?page="}
     * and the page number is 3, the link href becomes
     * {@code "/search?page=3"}.</p>
     *
     * <p>The component does not validate or encode the URL -- the caller
     * is responsible for providing a properly formatted base URL.</p>
     */
    private final String baseUrl;

    /**
     * Creates a new pagination navigation component.
     *
     * @param currentPage the current page number (1-based); must be between 1 and totalPages
     * @param totalPages  the total number of pages; must be at least 1
     * @param baseUrl     the URL prefix to which page numbers are appended; must not be null
     * @throws NullPointerException     if baseUrl is null
     * @throws IllegalArgumentException if totalPages is less than 1, or currentPage is
     *                                  outside the range [1, totalPages]
     */
    public JuxPagination(int currentPage, int totalPages, String baseUrl) {
        Objects.requireNonNull(baseUrl, "Base URL must not be null");
        if (totalPages < 1) {
            throw new IllegalArgumentException("Total pages must be at least 1, got: " + totalPages);
        }
        if (currentPage < 1 || currentPage > totalPages) {
            throw new IllegalArgumentException(
                    "Current page must be between 1 and " + totalPages + ", got: " + currentPage);
        }

        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.baseUrl = baseUrl;
    }

    /**
     * Builds the pagination navigation Element tree with ARIA landmarks.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <nav aria-label="Pagination" class="jux-pagination">
     *   <ul class="jux-pagination-list">
     *     <li><a href="..." aria-label="Previous page" [aria-disabled="true"]>Previous</a></li>
     *     <li><a href="..." aria-label="Page 1">1</a></li>
     *     <li><span aria-hidden="true" class="jux-pagination-ellipsis">...</span></li>
     *     <li><a href="..." aria-current="page" aria-label="Page 3, current page">3</a></li>
     *     ...
     *     <li><a href="..." aria-label="Next page" [aria-disabled="true"]>Next</a></li>
     *   </ul>
     * </nav>
     * }</pre>
     *
     * <p>Ellipsis elements use {@code aria-hidden="true"} because they are
     * purely visual indicators. Screen readers skip them entirely; the page
     * number labels provide sufficient context.</p>
     *
     * @return the pagination nav Element with page links, never null
     */
    @Override
    public Element render() {
        List<Element> items = new ArrayList<>();

        /* ── Previous button ────────────────────────────────────── */
        boolean hasPrevious = currentPage > 1;
        items.add(buildPreviousLink(hasPrevious));

        /* ── Page number links ──────────────────────────────────── */
        List<Integer> pageNumbers = computeVisiblePages();
        int lastRenderedPage = 0;

        for (int pageNum : pageNumbers) {
            /*
             * If there is a gap between the last rendered page and this one,
             * insert an ellipsis to indicate skipped pages.
             */
            if (lastRenderedPage > 0 && pageNum > lastRenderedPage + 1) {
                items.add(buildEllipsis());
            }

            items.add(buildPageLink(pageNum));
            lastRenderedPage = pageNum;
        }

        /* ── Next button ────────────────────────────────────────── */
        boolean hasNext = currentPage < totalPages;
        items.add(buildNextLink(hasNext));

        /* Wrap all items in a Bootstrap pagination list inside a nav landmark. */
        Element list = ul()
                .cls("pagination", "mb-0")
                .children(items);

        return nav()
                .aria("label", "Pagination")
                .children(list);
    }

    /**
     * Computes which page numbers should be displayed in the pagination bar.
     *
     * <p>For 7 or fewer total pages, all pages are shown. For more pages,
     * a windowed approach is used: always show page 1 and the last page,
     * plus pages within a window of 1 around the current page. Gaps between
     * shown pages will be filled with ellipsis in the render method.</p>
     *
     * @return a sorted list of page numbers to display (1-based)
     */
    private List<Integer> computeVisiblePages() {
        /*
         * Small page count: show all pages without truncation.
         * This avoids the confusing pattern of "1 ... 3 ... 5" when
         * there are only 5 pages.
         */
        if (totalPages <= 7) {
            List<Integer> all = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                all.add(i);
            }
            return all;
        }

        /*
         * Large page count: show first page, last page, current page,
         * and one page on each side of current. This ensures the user
         * always sees: (a) where they are, (b) the bounds of the range,
         * and (c) adjacent pages for fine navigation.
         */
        List<Integer> visible = new ArrayList<>();
        visible.add(1); // Always show first page

        /* Window around the current page: [current-1, current, current+1] */
        for (int i = currentPage - 1; i <= currentPage + 1; i++) {
            if (i > 1 && i < totalPages && !visible.contains(i)) {
                visible.add(i);
            }
        }

        visible.add(totalPages); // Always show last page

        /* Sort to ensure ascending order for correct rendering. */
        visible.sort(Integer::compareTo);
        return visible;
    }

    /**
     * Builds the "Previous" navigation link.
     *
     * <p>When on the first page, the link is rendered with
     * {@code aria-disabled="true"} and no href to prevent navigation.
     * Using {@code aria-disabled} instead of removing the element ensures
     * screen reader users know the control exists but is not available.</p>
     *
     * @param enabled true if the link should be active (not on first page)
     * @return the Previous list item element
     */
    private Element buildPreviousLink(boolean enabled) {
        if (enabled) {
            return li().cls("page-item").children(
                    a().cls("page-link")
                            .attr("href", baseUrl + (currentPage - 1))
                            .aria("label", "Previous page")
                            .text("Previous")
            );
        }
        /*
         * Disabled state: use a <span> instead of <a> to avoid
         * creating a non-functional link. The "disabled" class on the
         * page-item tells Bootstrap to render it as inactive.
         */
        return li().cls("page-item", "disabled").children(
                span().cls("page-link")
                        .aria("label", "Previous page")
                        .text("Previous")
        );
    }

    /**
     * Builds the "Next" navigation link.
     *
     * <p>When on the last page, the link is rendered with
     * {@code aria-disabled="true"} and no href, following the same
     * pattern as the Previous link.</p>
     *
     * @param enabled true if the link should be active (not on last page)
     * @return the Next list item element
     */
    private Element buildNextLink(boolean enabled) {
        if (enabled) {
            return li().cls("page-item").children(
                    a().cls("page-link")
                            .attr("href", baseUrl + (currentPage + 1))
                            .aria("label", "Next page")
                            .text("Next")
            );
        }
        return li().cls("page-item", "disabled").children(
                span().cls("page-link")
                        .aria("label", "Next page")
                        .text("Next")
        );
    }

    /**
     * Builds a single page number link.
     *
     * <p>The current page is marked with {@code aria-current="page"} and
     * given a distinct CSS class for visual highlighting. Non-current pages
     * are plain links with descriptive aria-labels.</p>
     *
     * @param pageNum the page number (1-based) for this link
     * @return the page number list item element
     */
    private Element buildPageLink(int pageNum) {
        boolean isCurrent = (pageNum == currentPage);

        Element link = a()
                .cls("page-link")
                .attr("href", baseUrl + pageNum)
                .aria("label", isCurrent
                        ? "Page " + pageNum + ", current page"
                        : "Page " + pageNum)
                .text(String.valueOf(pageNum));

        /* Mark the current page for screen readers and visual styling. */
        if (isCurrent) {
            link = link.ariaCurrent("page");
        }

        Element item = li().cls("page-item");
        if (isCurrent) {
            item = item.cls("active");
        }
        return item.children(link);
    }

    /**
     * Builds an ellipsis indicator for skipped page ranges.
     *
     * <p>The ellipsis is hidden from screen readers via
     * {@code aria-hidden="true"} because it has no interactive or
     * informational value for assistive technology users. The visible
     * page numbers already communicate the available range.</p>
     *
     * @return a list item containing a decorative ellipsis span
     */
    private Element buildEllipsis() {
        return li().cls("page-item", "disabled").children(
                span().cls("page-link").ariaHidden(true)
                        .text("\u2026") // Unicode horizontal ellipsis character
        );
    }
}
