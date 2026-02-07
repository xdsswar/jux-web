/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.store.data.Product;
import xss.it.jux.store.data.StoreData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for product catalog operations.
 *
 * <p>This controller demonstrates how standard Spring {@link RestController}
 * endpoints coexist seamlessly alongside JUX server-rendered pages within
 * the same application. While JUX handles the HTML page rendering via
 * {@code Component} classes and the {@code @Route} annotation, REST
 * controllers handle data-only JSON endpoints for AJAX calls, mobile
 * clients, or third-party integrations.</p>
 *
 * <p>All data is sourced from {@link StoreData}, which provides a static
 * in-memory product catalog. In a production application, these methods
 * would delegate to a Spring Data JPA repository instead.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/products} — list all products, with optional
 *       category filtering and sort ordering</li>
 *   <li>{@code GET /api/products/{slug}} — retrieve a single product
 *       by its URL-friendly slug identifier</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/products
 * GET /api/products?category=electronics
 * GET /api/products?sort=price-asc
 * GET /api/products?category=clothing&sort=rating
 * GET /api/products/wireless-headphones
 * }</pre>
 *
 * @see StoreData
 * @see CartApiController
 * @see SearchApiController
 */
@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    /**
     * Lists all products in the catalog, with optional filtering and sorting.
     *
     * <p>When called without parameters, returns the complete product catalog
     * in the default insertion order. The {@code category} parameter filters
     * results to products belonging to a specific category (matched by slug).
     * The {@code sort} parameter reorders the results according to the
     * specified strategy.</p>
     *
     * <p>Filtering and sorting can be combined: a request to
     * {@code /api/products?category=electronics&sort=price-desc} returns
     * only electronics products sorted from most to least expensive.</p>
     *
     * <h4>Sort options:</h4>
     * <ul>
     *   <li>{@code "price-asc"} — cheapest first</li>
     *   <li>{@code "price-desc"} — most expensive first</li>
     *   <li>{@code "rating"} — highest-rated first</li>
     *   <li>{@code "name"} — alphabetical by product name</li>
     * </ul>
     *
     * @param category optional category slug to filter by (e.g. "electronics",
     *                 "clothing"). If null or blank, all categories are included.
     * @param sort     optional sort strategy (e.g. "price-asc", "rating").
     *                 If null or blank, products are returned in default order.
     * @return 200 OK with a JSON object containing a {@code "products"} array
     *         and a {@code "total"} count of the returned products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort) {

        /*
         * Start with the full catalog. If a category filter is specified,
         * narrow the list to only products in that category. StoreData
         * handles the slug-based lookup internally.
         */
        List<Product> products;
        if (category != null && !category.isBlank()) {
            products = StoreData.productsByCategory(category);
        } else {
            products = StoreData.allProducts();
        }

        /*
         * Apply sorting if a sort parameter was provided. StoreData.sort()
         * accepts the sort key strings directly ("price-asc", "price-desc",
         * "rating", "name") and returns a new sorted list, leaving the
         * original unchanged.
         */
        if (sort != null && !sort.isBlank()) {
            products = StoreData.sort(products, sort);
        }

        /*
         * Build the response as a LinkedHashMap to preserve key insertion
         * order in the JSON output. This makes the API response predictable
         * and easier to read when inspecting with tools like curl or Postman.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("products", products);
        response.put("total", products.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single product by its slug.
     *
     * <p>The slug is the URL-friendly identifier for a product (e.g.
     * "wireless-headphones", "organic-cotton-tee"). This endpoint is
     * typically called by client-side JavaScript to fetch product details
     * for modal popups, quick-view panels, or cart operations without
     * a full page navigation.</p>
     *
     * @param slug the product's URL-friendly identifier, extracted from
     *             the path (e.g. "/api/products/wireless-headphones")
     * @return 200 OK with the product JSON if found, or 404 Not Found
     *         with an error message if no product matches the slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable String slug) {

        /*
         * Look up the product by slug using Optional. If the product exists,
         * wrap it in a response map with a "product" key. If not, return a
         * 404 status with a descriptive error message.
         */
        return StoreData.findProduct(slug)
                .map(product -> {
                    /* Product found — return it with a 200 OK status. */
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("product", product);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    /*
                     * No product with this slug exists. Return a 404 with a
                     * JSON error body so API consumers can distinguish between
                     * "not found" and other error types programmatically.
                     */
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "Product not found");
                    error.put("slug", slug);
                    return ResponseEntity.status(404).body(error);
                });
    }
}
