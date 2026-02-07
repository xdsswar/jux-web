/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Displays a star rating with review count.
 *
 * <p>Renders filled and empty star characters as an accessible
 * representation of product ratings. A screen-reader-only span
 * provides the exact numeric rating since the visual stars alone
 * are decorative.</p>
 *
 * <p>ADA: stars are marked {@code aria-hidden="true"} and an
 * sr-only span provides the text equivalent (e.g. "4.7 out of 5 stars").</p>
 */
public class RatingStars extends Component {

    private final double rating;
    private final int reviewCount;

    public RatingStars(double rating, int reviewCount) {
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    @Override
    public Element render() {
        List<Element> stars = new ArrayList<>();
        int fullStars = (int) rating;
        boolean hasHalf = (rating - fullStars) >= 0.5;

        /* Filled stars */
        for (int i = 0; i < fullStars; i++) {
            stars.add(span().cls("star-filled").ariaHidden(true).text("\u2605"));
        }
        /* Half star rendered as filled (rounding up for visual) */
        if (hasHalf) {
            stars.add(span().cls("star-filled").ariaHidden(true).text("\u2605"));
            fullStars++;
        }
        /* Empty stars to fill up to 5 */
        for (int i = fullStars; i < 5; i++) {
            stars.add(span().cls("star-empty").ariaHidden(true).text("\u2606"));
        }

        return div().cls("flex", "items-center", "gap-1").children(
                /* Visual star display (hidden from screen readers) */
                span().cls("flex").ariaHidden(true).children(stars),
                /* Screen-reader-only text with exact rating */
                srOnly(String.format("%.1f out of 5 stars", rating)),
                /* Visible review count */
                span().cls("text-sm", "text-gray-500")
                        .text("(" + reviewCount + ")")
        );
    }
}
