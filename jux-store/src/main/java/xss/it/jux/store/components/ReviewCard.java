/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.store.data.Review;

import static xss.it.jux.core.Elements.*;

/**
 * Renders a single customer review as a card.
 *
 * <p>Each review is wrapped in an {@code <article>} element since it
 * is a self-contained piece of content. The review includes a star
 * rating, author name, date, verified purchase badge, and the review text.</p>
 */
public class ReviewCard extends Component {

    private final Review review;

    public ReviewCard(Review review) {
        this.review = review;
    }

    @Override
    public Element render() {
        return article().cls("border", "border-gray-200", "rounded-lg", "p-4", "mb-4").children(
                /* Header: stars + verified badge */
                div().cls("flex", "items-center", "justify-between", "mb-2").children(
                        new RatingStars(review.rating(), 0).render(),
                        review.verified()
                                ? span().cls("text-xs", "text-green-600", "font-medium",
                                        "bg-green-50", "px-2", "py-0.5", "rounded-full")
                                    .text("Verified Purchase")
                                : span()
                ),
                /* Review text */
                p().cls("text-gray-700", "mb-3").text(review.text()),
                /* Author and date */
                div().cls("flex", "items-center", "gap-2", "text-sm", "text-gray-500").children(
                        strong().text(review.author()),
                        span().text("\u00b7"),
                        time().text(review.date())
                )
        );
    }
}
