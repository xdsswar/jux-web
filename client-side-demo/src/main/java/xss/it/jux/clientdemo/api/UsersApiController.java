/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.clientdemo.data.SampleData;
import xss.it.jux.clientdemo.data.SampleData.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for user management operations.
 *
 * <p>This controller exposes JSON endpoints for listing, filtering, searching,
 * and retrieving individual users from the demo's in-memory dataset. It
 * demonstrates how standard Spring {@link RestController} endpoints coexist
 * alongside JUX server-rendered pages within the same application.</p>
 *
 * <p>Client-side JUX components (compiled to JavaScript via TeaVM) call these
 * endpoints via {@code fetch()} to load user data dynamically. The JSON
 * responses use a consistent structure with a data wrapper key and a total
 * count, making them easy to consume from any client.</p>
 *
 * <p>All data is sourced from {@link SampleData}, which provides a static
 * in-memory user collection. In a production application, these methods
 * would delegate to a Spring Data JPA repository backed by a database.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/users} — list all users with optional department and
 *       search query filters</li>
 *   <li>{@code GET /api/users/{id}} — retrieve a single user by numeric ID</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/users
 * GET /api/users?department=Engineering
 * GET /api/users?search=alice
 * GET /api/users?department=Design&search=chen
 * GET /api/users/3
 * }</pre>
 *
 * @see SampleData
 * @see QuotesApiController
 * @see NotificationsApiController
 * @see StatsApiController
 */
@RestController
@RequestMapping("/api/users")
public class UsersApiController {

    /**
     * Lists all users, with optional department filtering and search.
     *
     * <p>When called without parameters, returns the complete user list in
     * insertion order. The {@code department} parameter filters results to
     * users belonging to a specific organizational department (matched
     * case-insensitively). The {@code search} parameter performs a free-text
     * search across user name, email, role, and department fields.</p>
     *
     * <p>When both parameters are provided, the department filter is applied
     * first, then the search filter is applied to the already-filtered result
     * set. This allows queries like "find all developers in Engineering whose
     * name contains 'kim'".</p>
     *
     * <h4>Response format:</h4>
     * <pre>{@code
     * {
     *   "users": [
     *     { "id": 1, "name": "Alice Johnson", "email": "alice@example.com", ... },
     *     ...
     *   ],
     *   "total": 20
     * }
     * }</pre>
     *
     * @param department optional department name to filter by (e.g. "Engineering",
     *                   "Design", "Marketing", "Product", "Operations"). If null
     *                   or blank, all departments are included in the results.
     * @param search     optional free-text query to match against user name, email,
     *                   role, and department. If null or blank, no text filtering
     *                   is applied. The search is case-insensitive.
     * @return 200 OK with a JSON object containing a {@code "users"} array and
     *         a {@code "total"} count of the returned users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search) {

        /*
         * Start with the full user collection. We apply filters progressively:
         * first by department (if specified), then by search query (if specified).
         * Each filter narrows the result set further.
         */
        List<User> users;

        /*
         * Department filter: if the department parameter is present and non-blank,
         * restrict the user list to only those in the specified department.
         * SampleData.usersByDepartment() handles the case-insensitive comparison.
         */
        if (department != null && !department.isBlank()) {
            users = SampleData.usersByDepartment(department);
        } else {
            users = SampleData.allUsers();
        }

        /*
         * Search filter: if a search query is provided, further narrow the
         * results by matching against user name, email, role, and department.
         * When combined with a department filter, this searches within the
         * already-filtered department subset.
         *
         * Note: When search is provided without department, we still search
         * against all users (not just the department-filtered subset), because
         * SampleData.searchUsers() operates on the full collection. To combine
         * filters correctly, we apply the search as a stream filter on the
         * current result set instead of calling searchUsers() directly.
         */
        if (search != null && !search.isBlank()) {
            /* Normalize the search query to lowercase for comparison. */
            String lowerSearch = search.toLowerCase(java.util.Locale.ROOT).trim();

            /*
             * Apply the search filter to the current result set (which may
             * already be filtered by department). We match against name, email,
             * role, and department fields, mirroring SampleData.searchUsers().
             */
            users = users.stream()
                    .filter(u -> u.name().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch)
                            || u.email().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch)
                            || u.role().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch)
                            || u.department().toLowerCase(java.util.Locale.ROOT).contains(lowerSearch))
                    .toList();
        }

        /*
         * Build the response as a LinkedHashMap to preserve key insertion order
         * in the serialized JSON output. This makes the API response predictable
         * and easier to read when inspecting with curl, Postman, or browser DevTools.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("users", users);
        response.put("total", users.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single user by their unique numeric identifier.
     *
     * <p>This endpoint is typically called by client-side components to fetch
     * user details for profile cards, modal popups, or user management panels
     * without a full page navigation.</p>
     *
     * <h4>Success response (200 OK):</h4>
     * <pre>{@code
     * {
     *   "user": {
     *     "id": 3,
     *     "name": "Carol Chen",
     *     "email": "carol@example.com",
     *     "role": "Designer",
     *     "avatar": "CC",
     *     "department": "Design",
     *     "active": true
     *   }
     * }
     * }</pre>
     *
     * <h4>Error response (404 Not Found):</h4>
     * <pre>{@code
     * {
     *   "error": "User not found",
     *   "id": 999
     * }
     * }</pre>
     *
     * @param id the user's unique numeric identifier, extracted from the URL
     *           path (e.g. "/api/users/3"). Must be a positive integer.
     * @return 200 OK with the user JSON if found, or 404 Not Found with an
     *         error message if no user exists with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {

        /*
         * Look up the user by ID using Optional. If the user exists, wrap it
         * in a response map with a "user" key. If not, return a 404 status
         * with a descriptive error message that includes the requested ID
         * for debugging purposes.
         */
        return SampleData.findUser(id)
                .map(user -> {
                    /* User found — return it with a 200 OK status. */
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    /*
                     * No user with this ID exists. Return a 404 with a JSON
                     * error body so API consumers can distinguish between
                     * "not found" and other error types programmatically.
                     * The ID is echoed back for client-side debugging.
                     */
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("error", "User not found");
                    error.put("id", id);
                    return ResponseEntity.status(404).body(error);
                });
    }
}
