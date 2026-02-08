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
import org.springframework.web.bind.annotation.RestController;
import xss.it.jux.clientdemo.data.SampleData;
import xss.it.jux.clientdemo.data.SampleData.StatEntry;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for dashboard statistics.
 *
 * <p>This controller exposes a single JSON endpoint that returns the current
 * set of dashboard key performance indicators (KPIs). These statistics are
 * displayed in the demo's dashboard page as metric cards with trend arrows,
 * providing a real-time-like monitoring view.</p>
 *
 * <p>The response includes a timestamp indicating when the statistics were
 * last computed. In this demo, the timestamp is always the current server
 * time since the data is static. In a production application, this would
 * reflect the actual last-computed time from a metrics aggregation pipeline
 * (e.g. Prometheus, Datadog, or a custom aggregator).</p>
 *
 * <p>All data is sourced from {@link SampleData}, which provides 6 static
 * dashboard metrics: Active Users, API Requests, Error Rate, Average Response
 * Time, Deployments, and Uptime.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /api/stats} — retrieve all dashboard statistics with a
 *       last-updated timestamp</li>
 * </ul>
 *
 * <h3>Example request:</h3>
 * <pre>{@code
 * GET /api/stats
 * }</pre>
 *
 * <h3>Example response:</h3>
 * <pre>{@code
 * {
 *   "stats": [
 *     { "label": "Active Users", "value": 2847, "unit": "users", "trend": "up" },
 *     { "label": "API Requests", "value": 14523, "unit": "req/h", "trend": "up" },
 *     ...
 *   ],
 *   "updatedAt": "2026-02-08T14:30:00Z"
 * }
 * }</pre>
 *
 * @see SampleData
 * @see UsersApiController
 * @see QuotesApiController
 * @see NotificationsApiController
 */
@RestController
@RequestMapping("/api/stats")
public class StatsApiController {

    /**
     * Returns all dashboard statistics with a last-updated timestamp.
     *
     * <p>The statistics include 6 key performance indicators, each with a
     * human-readable label, numeric value, display unit, and trend direction.
     * The trend field indicates whether the metric is improving ("up"),
     * declining ("down"), or unchanged ("stable") compared to the previous
     * measurement period.</p>
     *
     * <p>The {@code updatedAt} field is an ISO 8601 timestamp representing
     * when the statistics were last computed. Client-side components can
     * display this as "Last updated: X minutes ago" to give users confidence
     * that the data is fresh.</p>
     *
     * <h4>Stat entries returned:</h4>
     * <ul>
     *   <li><b>Active Users</b> — 2,847 users (trending up)</li>
     *   <li><b>API Requests</b> — 14,523 requests/hour (trending up)</li>
     *   <li><b>Error Rate</b> — 0.3% (trending down — this is good)</li>
     *   <li><b>Avg Response</b> — 142ms (stable)</li>
     *   <li><b>Deployments</b> — 23 today (trending up)</li>
     *   <li><b>Uptime</b> — 99.97% (stable)</li>
     * </ul>
     *
     * <h4>Response format:</h4>
     * <pre>{@code
     * {
     *   "stats": [
     *     { "label": "Active Users", "value": 2847, "unit": "users", "trend": "up" },
     *     { "label": "API Requests", "value": 14523, "unit": "req/h", "trend": "up" },
     *     { "label": "Error Rate", "value": 3, "unit": "%", "trend": "down" },
     *     { "label": "Avg Response", "value": 142, "unit": "ms", "trend": "stable" },
     *     { "label": "Deployments", "value": 23, "unit": "today", "trend": "up" },
     *     { "label": "Uptime", "value": 9997, "unit": "%", "trend": "stable" }
     *   ],
     *   "updatedAt": "2026-02-08T14:30:00Z"
     * }
     * }</pre>
     *
     * @return 200 OK with a JSON object containing a {@code "stats"} array of
     *         metric entries and an {@code "updatedAt"} ISO 8601 timestamp
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStats() {

        /* Fetch all dashboard statistics from the static data source. */
        List<StatEntry> stats = SampleData.allStats();

        /*
         * Generate the "updatedAt" timestamp as the current server time.
         * In a production application, this would be the actual last-computed
         * timestamp from the metrics pipeline. Using Instant.now() produces
         * an ISO 8601 UTC timestamp like "2026-02-08T14:30:00.123Z" which
         * is universally parseable by JavaScript's Date constructor and
         * any ISO 8601 date library.
         */
        String updatedAt = Instant.now().toString();

        /*
         * Build the response as a LinkedHashMap to preserve key insertion order
         * in the serialized JSON output. The "stats" array comes first, followed
         * by the "updatedAt" timestamp.
         */
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("stats", stats);
        response.put("updatedAt", updatedAt);

        return ResponseEntity.ok(response);
    }
}
