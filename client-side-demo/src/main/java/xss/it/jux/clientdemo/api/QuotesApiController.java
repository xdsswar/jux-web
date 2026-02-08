/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.clientdemo.data.SampleData;
import xss.it.jux.clientdemo.data.SampleData.Quote;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for inspirational and programming quote operations.
 *
 * <p>This controller exposes JSON endpoints for listing, filtering, and
 * randomly selecting quotes from the demo's in-memory collection. Quotes
 * are a lightweight, read-only dataset that is ideal for demonstrating
 * asynchronous data fetching from JUX client-side components.</p>
 *
 * <p>A common use case is the "Quote of the Day" widget on a dashboard page,
 * which calls {@code /api/quotes/random} on mount to display a fresh quote
 * each time the user visits. The category filter supports building a tabbed
 * or filtered quote browser UI.</p>
 *
 * <p>All data is sourced from {@link SampleData}, which provides a static
 * in-memory quote collection of 15 entries across 4 categories. In a
 * production application, quotes would be stored in a database with
 * user-contributed content and voting/ranking mechanisms.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/quotes} — list all quotes with optional category filter</li>
 *   <li>{@code GET /api/quotes/random} — retrieve one randomly selected quote</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/quotes
 * GET /api/quotes?category=Programming
 * GET /api/quotes?category=Motivation
 * GET /api/quotes/random
 * }</pre>
 *
 * @see SampleData
 * @see UsersApiController
 * @see NotificationsApiController
 * @see StatsApiController
 */
@RestController
@RequestMapping("/api/quotes")
public class QuotesApiController {

    /**
     * Lists all quotes, with an optional category filter.
     *
     * <p>When called without parameters, returns the complete collection of
     * 15 quotes in insertion order. The {@code category} parameter filters
     * results to quotes belonging to a specific category. The comparison
     * is case-insensitive, so "programming", "Programming", and "PROGRAMMING"
     * all match the same set of quotes.</p>
     *
     * <h4>Available categories:</h4>
     * <ul>
     *   <li>{@code "Motivation"} — 4 quotes about perseverance and ambition</li>
     *   <li>{@code "Programming"} — 5 quotes about software craftsmanship</li>
     *   <li>{@code "Design"} — 3 quotes about aesthetics and usability</li>
     *   <li>{@code "Leadership"} — 3 quotes about vision and influence</li>
     * </ul>
     *
     * <h4>Response format:</h4>
     * <pre>{@code
     * {
     *   "quotes": [
     *     { "id": 1, "text": "The only way to...", "author": "Steve Jobs", "category": "Motivation" },
     *     ...
     *   ],
     *   "total": 15
     * }
     * }</pre>
     *
     * @param category optional category name to filter by (e.g. "Programming",
     *                 "Motivation", "Design", "Leadership"). If null or blank,
     *                 all categories are included in the results.
     * @return 200 OK with a JSON object containing a {@code "quotes"} array
     *         and a {@code "total"} count of the returned quotes
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listQuotes(
            @RequestParam(required = false) String category) {

        /*
         * Determine the quote list based on the category filter. If a category
         * is specified and non-blank, delegate to SampleData.quotesByCategory()
         * which handles case-insensitive matching. Otherwise, return all quotes.
         */
        List<Quote> quotes;
        if (category != null && !category.isBlank()) {
            quotes = SampleData.quotesByCategory(category);
        } else {
            quotes = SampleData.allQuotes();
        }

        /*
         * Build the response as a LinkedHashMap to preserve key insertion order
         * in the serialized JSON output. The "quotes" array comes first, followed
         * by the "total" count for consistent, predictable API responses.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("quotes", quotes);
        response.put("total", quotes.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single randomly selected quote.
     *
     * <p>Each call to this endpoint returns a different quote (selected
     * uniformly at random from the full collection, regardless of category).
     * This is useful for "Quote of the Day" widgets, loading screens,
     * inspiration prompts, or any UI element that benefits from varied content
     * on each visit.</p>
     *
     * <p>The random selection uses {@link java.util.concurrent.ThreadLocalRandom}
     * internally, which is thread-safe and contention-free under concurrent
     * request load.</p>
     *
     * <h4>Response format:</h4>
     * <pre>{@code
     * {
     *   "quote": {
     *     "id": 7,
     *     "text": "Any fool can write code that a computer can understand...",
     *     "author": "Martin Fowler",
     *     "category": "Programming"
     *   }
     * }
     * }</pre>
     *
     * @return 200 OK with a JSON object containing a single {@code "quote"} object
     */
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> randomQuote() {

        /*
         * Delegate to SampleData.randomQuote() which selects a quote uniformly
         * at random using ThreadLocalRandom. Wrap the result in a response map
         * with a "quote" key for consistent API structure.
         */
        Quote quote = SampleData.randomQuote();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("quote", quote);

        return ResponseEntity.ok(response);
    }
}
