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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.clientdemo.data.SampleData;
import xss.it.jux.clientdemo.data.SampleData.Notification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for system notification operations.
 *
 * <p>This controller exposes JSON endpoints for listing notifications and
 * marking individual notifications as read. It demonstrates a typical
 * notification panel pattern where the client fetches the full notification
 * list on load, renders a badge with the unread count, and calls the
 * "mark as read" endpoint when the user clicks on a notification.</p>
 *
 * <p>In this demo, the "mark as read" operation is simulated — it always
 * returns success without persisting the state change. In a real application,
 * the POST endpoint would update a database record and the unread count
 * would reflect the actual persisted state.</p>
 *
 * <p>All data is sourced from {@link SampleData}, which provides a static
 * in-memory notification collection of 10 entries with mixed read/unread
 * states and notification types (info, warning, success, error).</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/notifications} — list all notifications with unread count</li>
 *   <li>{@code POST /api/notifications/{id}/read} — mark a notification as read</li>
 * </ul>
 *
 * <h3>Example requests:</h3>
 * <pre>{@code
 * GET /api/notifications
 * POST /api/notifications/3/read
 * }</pre>
 *
 * @see SampleData
 * @see UsersApiController
 * @see QuotesApiController
 * @see StatsApiController
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationsApiController {

    /**
     * Lists all system notifications with read/unread counts.
     *
     * <p>Returns the complete notification list in insertion order (most recent
     * first), along with the total count and the number of unread notifications.
     * The unread count is particularly useful for rendering notification badges
     * in the UI header — a common pattern where the badge shows a number like
     * "4" to indicate unread items.</p>
     *
     * <p>Each notification in the response includes its type (info, warning,
     * success, error) which the client uses to select the appropriate icon
     * and color styling.</p>
     *
     * <h4>Response format:</h4>
     * <pre>{@code
     * {
     *   "notifications": [
     *     {
     *       "id": 1,
     *       "title": "Deployment Successful",
     *       "message": "Version 2.4.1 has been deployed to production.",
     *       "type": "success",
     *       "timestamp": "2 minutes ago",
     *       "read": false
     *     },
     *     ...
     *   ],
     *   "unread": 4,
     *   "total": 10
     * }
     * }</pre>
     *
     * @return 200 OK with a JSON object containing a {@code "notifications"} array,
     *         an {@code "unread"} count of unread notifications, and a {@code "total"}
     *         count of all notifications
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listNotifications() {

        /* Fetch all notifications from the static data source. */
        List<Notification> all = SampleData.allNotifications();

        /*
         * Calculate the unread count by fetching the unread subset.
         * This is done as a separate call rather than filtering the `all` list
         * because SampleData.unreadNotifications() provides a clean, dedicated
         * method for this common operation.
         */
        List<Notification> unread = SampleData.unreadNotifications();

        /*
         * Build the response as a LinkedHashMap to preserve key insertion order
         * in the serialized JSON output. The response includes the full notification
         * list, the unread count (for badge rendering), and the total count.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("notifications", all);
        response.put("unread", unread.size());
        response.put("total", all.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Marks a notification as read.
     *
     * <p>This endpoint simulates the "mark as read" action that would
     * normally persist a state change to the database. In this demo, it
     * always returns a success response without modifying the in-memory
     * data, since the sample data is static and immutable.</p>
     *
     * <p>The endpoint uses POST (not PUT or PATCH) because it represents
     * an action ("mark this notification as read") rather than a full
     * resource replacement or partial update. This aligns with REST
     * conventions for action-oriented endpoints.</p>
     *
     * <h4>Success response (200 OK):</h4>
     * <pre>{@code
     * {
     *   "success": true,
     *   "id": 3,
     *   "message": "Notification marked as read"
     * }
     * }</pre>
     *
     * @param id the notification ID to mark as read, extracted from the URL
     *           path (e.g. "/api/notifications/3/read"). Must be a positive integer.
     * @return 200 OK with a JSON success confirmation including the notification ID
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable int id) {

        /*
         * In a production application, this method would:
         * 1. Look up the notification by ID from the database
         * 2. Verify the notification exists (return 404 if not)
         * 3. Verify the notification belongs to the current user (auth check)
         * 4. Set the "read" flag to true and persist the change
         * 5. Return the updated notification or a success confirmation
         *
         * For this demo, we skip all of that and simply return a success
         * response. The client-side component updates its local state
         * optimistically — it marks the notification as read in the UI
         * immediately upon clicking, without waiting for the server response.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("id", id);
        response.put("message", "Notification marked as read");

        return ResponseEntity.ok(response);
    }
}
