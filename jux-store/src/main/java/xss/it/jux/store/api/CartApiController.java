/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.store.data.CartItem;
import xss.it.jux.store.data.StoreData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for shopping cart operations.
 *
 * <p>This controller provides JSON endpoints for cart management: viewing
 * the current cart contents, adding products, and removing products. It
 * demonstrates how POST-based mutation endpoints work alongside JUX
 * server-rendered pages.</p>
 *
 * <p><b>Demo note:</b> This is a stateless demo. There is no persistent
 * cart storage. The {@code GET} endpoint returns a static sample cart from
 * {@link StoreData#sampleCart()}, and the {@code POST} endpoints echo back
 * acknowledgments without actually modifying any state. In a real
 * application, these endpoints would interact with a session-backed or
 * database-backed cart service.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/cart} -- view the current cart with items,
 *       subtotals, and grand total</li>
 *   <li>{@code POST /api/cart/add} -- add a product to the cart
 *       (accepts JSON body with slug and quantity)</li>
 *   <li>{@code POST /api/cart/remove} -- remove a product from the cart
 *       (accepts JSON body with slug)</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/cart
 *
 * POST /api/cart/add
 * Content-Type: application/json
 * {"slug": "wireless-headphones", "quantity": 2}
 *
 * POST /api/cart/remove
 * Content-Type: application/json
 * {"slug": "wireless-headphones"}
 * }</pre>
 *
 * @see StoreData#sampleCart()
 * @see ProductApiController
 * @see SearchApiController
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    /**
     * Returns the current shopping cart contents.
     *
     * <p>The response includes each cart line item (product details and
     * quantity), the per-item subtotal, the number of distinct items,
     * the total quantity of all items, and the grand total price formatted
     * as a currency string.</p>
     *
     * <p>In this demo, the cart is always the static sample from
     * {@link StoreData#sampleCart()}. A real implementation would load the
     * cart from the user session or a database table keyed by user/session ID.</p>
     *
     * @return 200 OK with a JSON object containing {@code "items"} (array
     *         of cart items with subtotals), {@code "itemCount"} (distinct
     *         products), {@code "totalQuantity"} (sum of all quantities),
     *         and {@code "total"} (formatted grand total string)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {

        /* Retrieve the demo cart. Each CartItem pairs a Product with a quantity. */
        List<CartItem> items = StoreData.sampleCart();

        /*
         * Transform each CartItem into a serialization-friendly map.
         * We include the full product details, the quantity, and the
         * computed subtotal so the client does not need to recalculate.
         * LinkedHashMap preserves the key order for clean JSON output.
         */
        List<Map<String, Object>> itemMaps = items.stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("product", item.product());
                    map.put("quantity", item.quantity());
                    map.put("subtotal", StoreData.formatPrice(item.subtotal()));
                    return map;
                })
                .toList();

        /*
         * Calculate the grand total by summing all line item subtotals.
         * This mirrors what a real cart service would compute, but here
         * we derive it directly from the static sample data.
         */
        double total = items.stream()
                .mapToDouble(CartItem::subtotal)
                .sum();

        /*
         * Calculate total quantity across all items. A cart with 2 headphones
         * and 1 shirt has itemCount=2 (distinct products) but totalQuantity=3.
         */
        int totalQuantity = items.stream()
                .mapToInt(CartItem::quantity)
                .sum();

        /* Assemble the response with all cart summary fields. */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", itemMaps);
        response.put("itemCount", items.size());
        response.put("totalQuantity", totalQuantity);
        response.put("total", StoreData.formatPrice(total));

        return ResponseEntity.ok(response);
    }

    /**
     * Adds a product to the shopping cart.
     *
     * <p>Accepts a JSON request body with a {@code "slug"} field identifying
     * the product and an optional {@code "quantity"} field (defaults to 1).
     * The endpoint validates that the product exists in the catalog before
     * acknowledging the add operation.</p>
     *
     * <p><b>Demo behavior:</b> Since this demo has no persistent cart, the
     * endpoint validates the input and echoes back a success confirmation
     * without actually modifying any state. A real implementation would
     * update the session or database cart.</p>
     *
     * @param body a JSON object with {@code "slug"} (required, String) and
     *             {@code "quantity"} (optional, Integer, defaults to 1)
     * @return 200 OK with a success message and the added product details,
     *         or 400 Bad Request if the slug is missing,
     *         or 404 Not Found if the product slug does not exist
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> body) {

        /*
         * Extract the product slug from the request body. This is the
         * only required field -- it identifies which product to add.
         */
        String slug = (String) body.get("slug");
        if (slug == null || slug.isBlank()) {
            /*
             * No slug provided -- the client must specify which product
             * to add. Return a 400 Bad Request with a clear error message.
             */
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required field: slug"));
        }

        /*
         * Extract the quantity, defaulting to 1 if not provided.
         * The body value could be an Integer or a Number depending on
         * how Jackson deserializes the JSON, so we use Number for safety.
         */
        int quantity = 1;
        Object quantityValue = body.get("quantity");
        if (quantityValue instanceof Number number) {
            quantity = number.intValue();
        }

        /*
         * Validate that the product actually exists in the catalog.
         * Allowing adds for nonexistent products would create ghost
         * items that cannot be displayed or checked out.
         */
        int finalQuantity = quantity;
        return StoreData.findProduct(slug)
                .map(product -> {
                    /*
                     * Product found -- acknowledge the add operation.
                     * In a real app, this is where we would call
                     * cartService.addItem(userId, product, quantity).
                     */
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("message", "Product added to cart");
                    response.put("slug", product.slug());
                    response.put("name", product.name());
                    response.put("quantity", finalQuantity);
                    response.put("unitPrice", StoreData.formatPrice(product.price()));
                    response.put("lineTotal", StoreData.formatPrice(product.price() * finalQuantity));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    /*
                     * No product matches the given slug. Return a 404 so the
                     * client can distinguish "product not found" from other
                     * error conditions.
                     */
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "Product not found");
                    error.put("slug", slug);
                    return ResponseEntity.status(404).body(error);
                });
    }

    /**
     * Removes a product from the shopping cart.
     *
     * <p>Accepts a JSON request body with a {@code "slug"} field identifying
     * the product to remove. Removes all units of that product from the cart
     * (not just one).</p>
     *
     * <p><b>Demo behavior:</b> Since this demo has no persistent cart, the
     * endpoint validates the input and echoes back a success confirmation.
     * A real implementation would remove the item from the session or
     * database cart and return the updated cart state.</p>
     *
     * @param body a JSON object with {@code "slug"} (required, String)
     * @return 200 OK with a success message confirming removal,
     *         or 400 Bad Request if the slug is missing,
     *         or 404 Not Found if the product slug does not exist
     */
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestBody Map<String, Object> body) {

        /*
         * Extract and validate the product slug. Without it, we do not
         * know which item the client wants to remove.
         */
        String slug = (String) body.get("slug");
        if (slug == null || slug.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required field: slug"));
        }

        /*
         * Verify the product exists. Even though we are not modifying a real
         * cart, validating the slug ensures the API behaves consistently --
         * the client will not get a success response for a nonexistent product.
         */
        return StoreData.findProduct(slug)
                .map(product -> {
                    /*
                     * Product found -- acknowledge the removal.
                     * In a real app: cartService.removeItem(userId, product.slug()).
                     */
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("message", "Product removed from cart");
                    response.put("slug", product.slug());
                    response.put("name", product.name());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    /*
                     * Product not found -- return 404. The client might be
                     * trying to remove an item with a stale or mistyped slug.
                     */
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "Product not found");
                    error.put("slug", slug);
                    return ResponseEntity.status(404).body(error);
                });
    }
}
