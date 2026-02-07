/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.store.data.Product;
import xss.it.jux.store.data.StoreData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for product search.
 *
 * <p>This controller provides a JSON search endpoint that queries the
 * product catalog by keyword. It demonstrates a typical AJAX-driven
 * search pattern: the client-side search bar (rendered by a JUX component)
 * calls this endpoint as the user types, and updates the results list
 * dynamically without a full page reload.</p>
 *
 * <p>The search implementation in {@link StoreData#search(String)} performs
 * a case-insensitive match against product names, descriptions, tags, and
 * category names. In a production application, this would delegate to a
 * full-text search engine (Elasticsearch, Lucene) or a database
 * {@code LIKE} / {@code FULLTEXT} query.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/search?q=query} -- search products by keyword</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/search?q=headphones
 * GET /api/search?q=organic+cotton
 * GET /api/search?q=
 * }</pre>
 *
 * @see StoreData#search(String)
 * @see ProductApiController
 * @see CartApiController
 */
@RestController
@RequestMapping("/api/search")
public class SearchApiController {

    /**
     * Searches the product catalog by keyword query.
     *
     * <p>Performs a case-insensitive search across product names, descriptions,
     * tags, and categories. Returns all matching products along with a result
     * count and the original query string (useful for the client to display
     * "Showing results for: ...").</p>
     *
     * <p>If the query is blank or not provided, the endpoint returns an empty
     * result set with a count of zero rather than returning all products.
     * This prevents accidental full-catalog dumps from empty search submissions
     * and provides a clean baseline for the client-side search UI.</p>
     *
     * <p>The response always includes the original query string, even when
     * empty, so the client can render contextual messages like "Enter a
     * search term" versus "No results for X".</p>
     *
     * @param q the search query string. May be blank or null, in which case
     *          an empty result set is returned. Typical values: "headphones",
     *          "organic cotton", "laptop".
     * @return 200 OK with a JSON object containing {@code "query"} (the
     *         original search string), {@code "results"} (array of matching
     *         products), and {@code "count"} (number of matches)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(value = "q", required = false, defaultValue = "") String q) {

        /*
         * Build the response map. We use LinkedHashMap to guarantee a
         * predictable key order in the JSON output: query first, then
         * results, then count. This makes the API output easier to read
         * in debugging tools and more consistent for client parsers that
         * might rely on key ordering (though they should not).
         */
        Map<String, Object> response = new LinkedHashMap<>();

        /* Always echo back the query so the client can display it. */
        response.put("query", q);

        /*
         * If the query is blank, return an empty result set immediately.
         * We intentionally do not return all products for an empty query
         * because:
         *   1. It would be a wasteful full-catalog response on every
         *      page load that includes a search bar.
         *   2. The product listing endpoint (GET /api/products) already
         *      serves that purpose.
         *   3. It provides a clean "zero state" for the search UI.
         */
        if (q.isBlank()) {
            response.put("results", List.of());
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        /*
         * Delegate to StoreData.search() which performs case-insensitive
         * matching across product name, description, tags, and category.
         * The returned list is already filtered and ready to serialize.
         */
        List<Product> results = StoreData.search(q);

        response.put("results", results);
        response.put("count", results.size());

        return ResponseEntity.ok(response);
    }
}
