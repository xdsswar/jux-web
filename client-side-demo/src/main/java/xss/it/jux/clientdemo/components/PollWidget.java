/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.json.JSON;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.OnMount;
import xss.it.jux.annotation.OnUnmount;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Live dashboard widget demonstrating {@code @OnMount} interval polling for API data.
 *
 * <p>This component showcases the interval-based polling pattern used to keep a dashboard
 * synchronized with a backend API. It renders a grid of six statistic cards -- Active Users,
 * API Requests/hr, Error Rate, Average Response Time, Deployments Today, and Uptime -- each
 * displaying a large numeric value with a trend indicator (up, down, or stable).</p>
 *
 * <h2>Reactive Properties Demonstrated</h2>
 * <ul>
 *   <li><b>{@link SimpleIntegerProperty}</b> -- used to model each dashboard metric as an
 *       observable integer value. The reactive property layer allows external observers
 *       (such as alert systems or chart components) to react to stat changes independently
 *       of the JUX rendering cycle. Each metric has its own named property: {@code activeUsersProp},
 *       {@code apiRequestsProp}, {@code errorRateProp}, {@code avgResponseProp}, and
 *       {@code deploymentsProp}.</li>
 *   <li><b>{@link SimpleBooleanProperty}</b> -- tracks the connection state to the backend
 *       API via {@code connectedProp}. When the connection is lost (simulated), the
 *       indicator dot turns red and the status text changes to "Disconnected". Observers
 *       on this property could trigger reconnection logic or user notifications.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @OnMount} lifecycle hook</b> -- after client-side hydration, the mount
 *       hook sets up a repeating browser interval (via {@code Window.setInterval}) that
 *       polls the {@code /api/stats} endpoint every 3 seconds. This demonstrates the
 *       standard pattern for real-time data fetching in JUX client-side components.</li>
 *   <li><b>{@code @OnUnmount} lifecycle hook</b> -- clears the polling interval when the
 *       component is removed from the DOM, preventing memory leaks and stale callbacks
 *       firing on a detached component.</li>
 *   <li><b>{@code @State} for multi-field updates</b> -- six independent stat values plus
 *       connection status and refresh count are all {@code @State} fields. When the polling
 *       callback updates them, the framework batches the changes into a single re-render
 *       cycle with efficient DOM diffing.</li>
 *   <li><b>Simulated update button</b> -- a "Simulate Update" button randomly modifies
 *       stat values to demonstrate reactive updates without requiring actual API polling.
 *       This allows testing the UI's responsiveness to data changes during SSR-only mode.</li>
 * </ul>
 *
 * <h2>API Interaction Pattern</h2>
 * <p>The {@code @OnMount} method documents the complete polling pattern:</p>
 * <ol>
 *   <li>Set up a {@code Window.setInterval} callback with a 3-second period</li>
 *   <li>Inside the callback, create an {@code XMLHttpRequest} to {@code GET /api/stats}</li>
 *   <li>Parse the JSON response and update each {@code @State} field</li>
 *   <li>Update the connection status based on request success/failure</li>
 *   <li>Increment the refresh counter for debugging visibility</li>
 * </ol>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>4.1.3 Status Messages</b> -- the stats grid is wrapped in an
 *       {@code aria-live="polite"} region so screen readers announce data updates
 *       without interrupting the user. The politeness level ensures announcements
 *       wait for idle moments.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- each stat card has a descriptive
 *       {@code aria-label} that includes both the metric name and its current value
 *       (e.g. "Active Users: 2847"). The connection indicator uses {@code role="status"}
 *       to communicate its state to assistive technology.</li>
 *   <li><b>1.4.1 Use of Color</b> -- trend arrows use both color AND directional symbols
 *       (up arrow, down arrow, horizontal line) to convey meaning, ensuring that colour-blind
 *       users can still interpret trends.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The initial server-rendered state displays all six stat cards with their default sample
 * values, a green "Connected" indicator, and a refresh count of 0. The polling interval is
 * only established after client-side hydration via the {@code @OnMount} hook.</p>
 *
 * @see SimpleIntegerProperty
 * @see SimpleBooleanProperty
 * @see xss.it.jux.annotation.OnMount
 * @see xss.it.jux.annotation.OnUnmount
 */
@JuxComponent(clientSide = true)
public class PollWidget extends Component {

    // ─── Reactive Properties ─────────────────────────────────────────────────────
    // Observable integer properties for each dashboard metric. External observers
    // can attach ChangeListeners to react to value changes independently of the
    // rendering cycle -- useful for threshold alerts, logging, or bound charts.

    /**
     * Reactive property tracking the number of currently active users.
     *
     * <p>Initialized to 2847 as a realistic sample value. Updated by the polling
     * callback with fresh data from the backend API. Observers could trigger
     * alerts when the value crosses capacity thresholds.</p>
     */
    private final SimpleIntegerProperty activeUsersProp =
            new SimpleIntegerProperty(this, "activeUsers", 2847);

    /**
     * Reactive property tracking the number of API requests processed per hour.
     *
     * <p>Initialized to 14523. This metric represents server throughput and is
     * a key performance indicator for backend health monitoring.</p>
     */
    private final SimpleIntegerProperty apiRequestsProp =
            new SimpleIntegerProperty(this, "apiRequests", 14523);

    /**
     * Reactive property tracking the error rate as a scaled integer.
     *
     * <p>The value is stored as error rate multiplied by 10 to avoid floating-point
     * state (e.g. 3 represents 0.3%). This preserves integer-only state management
     * while still supporting one decimal place of precision in the display.</p>
     */
    private final SimpleIntegerProperty errorRateProp =
            new SimpleIntegerProperty(this, "errorRate", 3);

    /**
     * Reactive property tracking the average API response time in milliseconds.
     *
     * <p>Initialized to 142ms. This metric is critical for monitoring user-facing
     * latency. High values (above 500ms) typically indicate backend performance
     * issues that need investigation.</p>
     */
    private final SimpleIntegerProperty avgResponseProp =
            new SimpleIntegerProperty(this, "avgResponse", 142);

    /**
     * Reactive property tracking the number of deployments executed today.
     *
     * <p>Initialized to 23. This counter is useful for CI/CD monitoring dashboards
     * where deployment frequency is a key DevOps metric.</p>
     */
    private final SimpleIntegerProperty deploymentsProp =
            new SimpleIntegerProperty(this, "deployments", 23);

    /**
     * Reactive boolean property indicating whether the dashboard is connected
     * to the backend API.
     *
     * <p>When {@code true}, a green dot and "Connected" text are displayed. When
     * {@code false}, a red dot and "Disconnected" text appear. Connection state
     * changes are driven by the success or failure of polling requests.</p>
     */
    private final SimpleBooleanProperty connectedProp =
            new SimpleBooleanProperty(this, "connected", true);

    // ─── @State Fields ───────────────────────────────────────────────────────────
    // These fields drive the JUX reactivity system. Each mutation triggers a
    // virtual DOM diff/patch cycle.

    /**
     * Number of currently active users connected to the platform.
     * <p>Default: 2847. Updated by the polling callback.</p>
     */
    @State
    private int activeUsers = 2847;

    /**
     * Number of API requests processed per hour.
     * <p>Default: 14523. Updated by the polling callback.</p>
     */
    @State
    private int apiRequests = 14523;

    /**
     * Error rate as a scaled integer (actual rate = value / 10).
     * <p>Default: 3 (representing 0.3%). Updated by the polling callback.</p>
     */
    @State
    private int errorRate = 3;

    /**
     * Average API response time in milliseconds.
     * <p>Default: 142ms. Updated by the polling callback.</p>
     */
    @State
    private int avgResponse = 142;

    /**
     * Number of deployments executed today.
     * <p>Default: 23. Updated by the polling callback.</p>
     */
    @State
    private int deployments = 23;

    /**
     * Human-readable uptime percentage string.
     * <p>Default: "99.97%". Displayed as-is in the uptime stat card.</p>
     */
    @State
    private String uptime = "99.97%";

    /**
     * Whether the dashboard is currently connected to the backend API.
     * <p>Default: {@code true}. Set to {@code false} when a polling request fails.</p>
     */
    @State
    private boolean connected = true;

    /**
     * Counter tracking how many times the polling callback has executed.
     *
     * <p>Displayed in the header as a debugging aid. Incremented by 1 on each
     * successful polling cycle or simulated update.</p>
     */
    @State
    private int refreshCount = 0;

    /**
     * The browser interval timer ID returned by {@code Window.setInterval()}.
     *
     * <p>Stored as an instance field (NOT {@code @State}) so the interval can
     * be cleared in the {@code @OnUnmount} hook. Changing this value does not
     * trigger a re-render because it is internal timer lifecycle bookkeeping.</p>
     */
    private int intervalId = 0;

    /**
     * Initializes the polling interval after client-side hydration.
     *
     * <p>This method is called once by the JUX runtime after the server-rendered
     * HTML has been hydrated with event listeners and state on the client. It sets
     * up a repeating interval that polls the backend API every 3 seconds to fetch
     * fresh dashboard metrics.</p>
     *
     * <p><b>Note:</b> In the current SSR-only demo, this method is declared but
     * not invoked because client-side hydration via TeaVM is not yet wired up.
     * Once TeaVM compilation is enabled, the JUX runtime will call this method
     * automatically after hydration.</p>
     */
    // ── TeaVM Overlay Types for /api/stats JSON response ────────────────────────

    /** Typed overlay for the {@code GET /api/stats} response body. */
    interface StatsResponse extends JSObject {
        @JSProperty JSArrayReader<StatJson> getStats();
        @JSProperty String getUpdatedAt();
    }

    /** Typed overlay for a single stat entry inside the response array. */
    interface StatJson extends JSObject {
        @JSProperty String getLabel();
        @JSProperty int getValue();
        @JSProperty String getUnit();
        @JSProperty String getTrend();
    }

    @OnMount
    public void onMount() {
        /* Poll the /api/stats endpoint every 5 seconds using a browser interval. */
        intervalId = Window.current().setInterval(() -> {
            XMLHttpRequest xhr = XMLHttpRequest.create();
            xhr.open("GET", "/api/stats");
            xhr.setOnReadyStateChange(() -> {
                if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                    return;
                }

                if (xhr.getStatus() == 200) {
                    StatsResponse response =
                            (StatsResponse) JSON.parse(xhr.getResponseText());
                    JSArrayReader<StatJson> arr = response.getStats();

                    for (int i = 0; i < arr.getLength(); i++) {
                        StatJson stat = arr.get(i);
                        String label = stat.getLabel();
                        int value = stat.getValue();

                        switch (label) {
                            case "Active Users" -> {
                                activeUsers = value;
                                activeUsersProp.set(value);
                            }
                            case "API Requests" -> {
                                apiRequests = value;
                                apiRequestsProp.set(value);
                            }
                            case "Error Rate" -> {
                                errorRate = value;
                                errorRateProp.set(value);
                            }
                            case "Avg Response" -> {
                                avgResponse = value;
                                avgResponseProp.set(value);
                            }
                            case "Deployments" -> {
                                deployments = value;
                                deploymentsProp.set(value);
                            }
                            case "Uptime" -> {
                                int whole = value / 100;
                                int fraction = value % 100;
                                uptime = whole + "." + (fraction < 10 ? "0" : "") + fraction + "%";
                            }
                            default -> { /* ignore unknown labels */ }
                        }
                    }

                    connected = true;
                    connectedProp.set(true);
                    refreshCount++;
                } else {
                    connected = false;
                    connectedProp.set(false);
                }

                ClientMain.getStateManager().notifyStateChange(this);
            });
            xhr.send();
        }, 5000);
    }

    /**
     * Clears the polling interval before the component is removed from the DOM.
     *
     * <p>This cleanup hook prevents the interval callback from continuing to fire
     * after the component's DOM nodes are removed. Without this cleanup, the
     * callback would attempt to update state on a detached component, causing
     * errors and memory leaks.</p>
     *
     * <p><b>Note:</b> As with {@code onMount()}, this method is declared but
     * not invoked in the current SSR-only demo.</p>
     */
    @OnUnmount
    public void onUnmount() {
        if (intervalId != 0) {
            Window.current().clearInterval(intervalId);
            intervalId = 0;
        }
    }

    /**
     * Builds the virtual DOM tree for the live dashboard widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A header row with the dashboard title, connection status indicator,
     *       and refresh count badge.</li>
     *   <li>A 3-column, 2-row grid of stat cards, each displaying a metric icon,
     *       label, current value, unit, and trend arrow.</li>
     *   <li>A footer with a "Simulate Update" button and a last-updated timestamp.</li>
     * </ol>
     *
     * @return the root element of the dashboard widget, never null
     */
    @Override
    public Element render() {
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Dashboard Header ────────────────────────────────────────
                 * Contains the dashboard title on the left, and on the right
                 * a connection status indicator (colored dot + text) and the
                 * refresh count badge.
                 */
                div().cls("flex", "items-center", "justify-between", "mb-6").children(

                        /* Dashboard title */
                        h2().cls("text-xl", "font-bold", "text-white")
                                .text("Live Dashboard"),

                        /* Right-side status indicators */
                        div().cls("flex", "items-center", "gap-4").children(

                                /* ── Connection Status Indicator ─────────────────
                                 * A small colored dot (green = connected, red =
                                 * disconnected) followed by the status text.
                                 * Uses role="status" so screen readers announce
                                 * connection state changes.
                                 */
                                div().cls("flex", "items-center", "gap-2")
                                        .role("status")
                                        .aria("label", connected ? "Connected to server" : "Disconnected from server")
                                        .children(
                                                /* Colored dot indicator. The dot uses a CSS
                                                 * animation class (animate-pulse) when connected
                                                 * to subtly draw attention to the live state. */
                                                span().cls("w-2.5", "h-2.5", "rounded-full")
                                                        .cls(connected
                                                                ? "bg-emerald-400 animate-pulse"
                                                                : "bg-rose-400")
                                                        .ariaHidden(true),
                                                span().cls("text-sm")
                                                        .cls(connected ? "text-emerald-400" : "text-rose-400")
                                                        .text(connected ? "Connected" : "Disconnected")
                                        ),

                                /* ── Refresh Count Badge ─────────────────────────
                                 * A small pill-shaped badge showing how many polling
                                 * cycles have completed. Useful for debugging and
                                 * verifying that polling is active.
                                 */
                                span().cls("text-xs", "bg-gray-700", "text-gray-400",
                                                "px-2", "py-1", "rounded-full")
                                        .aria("label", "Refresh count: " + refreshCount)
                                        .text("Refreshes: " + refreshCount)
                        )
                ),

                /* ── Stats Grid ──────────────────────────────────────────────
                 * A responsive 3-column grid containing 6 stat cards. The grid
                 * uses aria-live="polite" so that screen readers announce updated
                 * values when the polling callback refreshes the data.
                 */
                div().cls("grid", "grid-cols-3", "gap-4", "mb-6")
                        .ariaLive("polite")
                        .children(buildStatCards()),

                /* ── Dashboard Footer ────────────────────────────────────────
                 * Contains a "Simulate Update" button for demo purposes and a
                 * "Last updated" timestamp placeholder.
                 */
                div().cls("flex", "items-center", "justify-between",
                                "pt-4", "border-t", "border-gray-700")
                        .children(

                                /* Simulate Update button: randomly adjusts stat
                                 * values to demonstrate reactive re-rendering without
                                 * requiring actual API polling. */
                                button().attr("type", "button")
                                        .cls("px-4", "py-2", "bg-violet-600/20", "text-violet-400",
                                                "rounded-lg", "text-sm", "font-medium",
                                                "hover:bg-violet-600/30", "transition-colors",
                                                "focus:ring-2", "focus:ring-violet-500",
                                                "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                        .aria("label", "Simulate a data update from the server")
                                        .on("click", e -> simulateUpdate())
                                        .text("Simulate Update"),

                                /* Last updated timestamp. In a real application, this
                                 * would display the actual time of the last successful
                                 * API response. For the demo, it shows a static value. */
                                span().cls("text-xs", "text-gray-500")
                                        .text("Last updated: just now")
                        )
        );
    }

    /**
     * Generates the list of six stat card elements for the dashboard grid.
     *
     * <p>Each card is built via {@link #buildStatCard} with the appropriate icon,
     * label, current value, unit string, and trend direction. The cards are
     * returned in a fixed order:</p>
     * <ol>
     *   <li>Active Users (people icon, upward trend)</li>
     *   <li>API Requests/hr (chart icon, upward trend)</li>
     *   <li>Error Rate (warning icon, downward trend = good)</li>
     *   <li>Avg Response (lightning icon, stable trend)</li>
     *   <li>Deployments (rocket icon, upward trend)</li>
     *   <li>Uptime (checkmark icon, stable trend)</li>
     * </ol>
     *
     * @return a list of six stat card elements
     */
    private List<Element> buildStatCards() {
        /* Build each card and collect into a mutable list. The order here
         * determines the visual layout in the 3-column grid: cards are
         * placed left-to-right, top-to-bottom. */
        List<Element> cards = new ArrayList<>();

        /* Card 1: Active Users -- the number of users currently online. */
        cards.add(buildStatCard(
                "\uD83D\uDC65",       /* 👥 People icon */
                "Active Users",
                String.valueOf(activeUsers),
                "",                     /* No unit suffix */
                "up"                    /* Trend: increasing */
        ));

        /* Card 2: API Requests per hour -- server throughput metric. */
        cards.add(buildStatCard(
                "\uD83D\uDCCA",       /* 📊 Chart icon */
                "API Requests/hr",
                String.valueOf(apiRequests),
                "",
                "up"
        ));

        /* Card 3: Error Rate -- displayed as a percentage with one decimal.
         * The errorRate state is stored as rate * 10 (integer), so we format
         * it here by dividing: e.g. 3 becomes "0.3%". A downward trend on
         * error rate is positive (fewer errors). */
        cards.add(buildStatCard(
                "\u26A0\uFE0F",       /* ⚠️ Warning icon */
                "Error Rate",
                (errorRate / 10) + "." + (errorRate % 10) + "%",
                "",
                "down"                  /* Trend: decreasing (good for errors) */
        ));

        /* Card 4: Average Response Time -- displayed in milliseconds. */
        cards.add(buildStatCard(
                "\u26A1",             /* ⚡ Lightning icon */
                "Avg Response",
                String.valueOf(avgResponse),
                "ms",                   /* Unit: milliseconds */
                "stable"                /* Trend: stable */
        ));

        /* Card 5: Deployments today -- CI/CD deployment counter. */
        cards.add(buildStatCard(
                "\uD83D\uDE80",       /* 🚀 Rocket icon */
                "Deployments",
                String.valueOf(deployments),
                "today",
                "up"
        ));

        /* Card 6: Uptime -- displayed as a percentage string. */
        cards.add(buildStatCard(
                "\u2705",             /* ✅ Checkmark icon */
                "Uptime",
                uptime,
                "",
                "stable"
        ));

        return cards;
    }

    /**
     * Builds a single stat card element for the dashboard grid.
     *
     * <p>Each card has a consistent layout:</p>
     * <ul>
     *   <li>Top row: icon (left) and trend arrow (right)</li>
     *   <li>Middle: the metric value in large bold text</li>
     *   <li>Bottom row: the metric label and optional unit</li>
     * </ul>
     *
     * <p>The trend arrow uses both colour and directional symbols to convey meaning:</p>
     * <ul>
     *   <li><b>"up"</b>: green upward arrow ({@code \u2191})</li>
     *   <li><b>"down"</b>: rose/red downward arrow ({@code \u2193})</li>
     *   <li><b>"stable"</b>: gray horizontal line ({@code \u2192})</li>
     * </ul>
     *
     * @param icon      a Unicode emoji or symbol representing the metric
     * @param labelText the human-readable metric name
     * @param value     the current metric value as a formatted string
     * @param unit      the unit suffix (e.g. "ms", "today") or empty string
     * @param trend     the trend direction: "up", "down", or "stable"
     * @return the stat card element
     */
    private Element buildStatCard(String icon, String labelText, String value,
                                  String unit, String trend) {
        /*
         * Determine the trend arrow character and its CSS color class.
         * The arrow provides a visual indicator of the metric's direction
         * since the last update cycle.
         */
        String trendArrow;
        String trendColor;
        switch (trend) {
            case "up" -> {
                trendArrow = "\u2191"; /* ↑ Upward arrow */
                trendColor = "text-emerald-400";
            }
            case "down" -> {
                trendArrow = "\u2193"; /* ↓ Downward arrow */
                trendColor = "text-rose-400";
            }
            default -> {
                trendArrow = "\u2192"; /* → Rightward arrow (stable) */
                trendColor = "text-gray-500";
            }
        }

        /* Construct the full accessible label for the card, including the
         * value, unit, and trend direction. This is announced by screen
         * readers when the card receives focus or when the live region updates. */
        String fullLabel = labelText + ": " + value + (unit.isEmpty() ? "" : " " + unit)
                + ", trend " + trend;

        return div().cls("bg-gray-750", "rounded-xl", "p-4", "hover:bg-gray-700",
                        "transition-colors")
                .aria("label", fullLabel)
                .children(

                        /* ── Top Row: Icon + Trend Arrow ─────────────────────────
                         * The icon identifies the metric type visually, and the
                         * trend arrow shows the direction of change.
                         */
                        div().cls("flex", "items-center", "justify-between", "mb-3").children(
                                /* Metric icon -- decorative, hidden from screen readers
                                 * because the aria-label on the card provides full context. */
                                span().cls("text-2xl").ariaHidden(true).text(icon),
                                /* Trend arrow with colour coding. Also hidden from screen
                                 * readers because the trend is included in the card's
                                 * aria-label. */
                                span().cls("text-sm", "font-bold", trendColor)
                                        .ariaHidden(true)
                                        .text(trendArrow)
                        ),

                        /* ── Value Display ────────────────────────────────────────
                         * The metric value in large, bold white text. This is the
                         * primary visual element of each card.
                         */
                        div().cls("mb-1").children(
                                span().cls("text-2xl", "font-bold", "text-white")
                                        .text(value),
                                /* Unit suffix displayed in smaller, muted text after
                                 * the value (e.g. "ms" after "142"). */
                                unit.isEmpty()
                                        ? span()
                                        : span().cls("text-sm", "text-gray-400", "ml-1").text(unit)
                        ),

                        /* ── Metric Label ─────────────────────────────────────────
                         * The metric name in small, muted text below the value.
                         */
                        span().cls("text-xs", "text-gray-500").text(labelText)
                );
    }

    /**
     * Simulates a data update from the backend API by randomly adjusting stat values.
     *
     * <p>This method is invoked by the "Simulate Update" button to demonstrate how
     * the dashboard UI reacts to data changes without requiring actual API polling.
     * Each metric is randomly adjusted within a realistic range:</p>
     * <ul>
     *   <li>Active Users: +/- 50 from current value</li>
     *   <li>API Requests: +/- 200 from current value</li>
     *   <li>Error Rate: +/- 2 from current value (clamped to 0-50)</li>
     *   <li>Avg Response: +/- 20 from current value (clamped to 50-500)</li>
     *   <li>Deployments: +0 or +1 (deployments only go up)</li>
     * </ul>
     *
     * <p>After updating all {@code @State} fields, the reactive properties are
     * synchronized to maintain consistency. The refresh counter is incremented
     * to show that an update cycle occurred.</p>
     */
    private void simulateUpdate() {
        /*
         * Generate pseudo-random adjustments for each metric. In a real
         * application, these values would come from the parsed JSON response
         * of the /api/stats endpoint. Here we use simple arithmetic to
         * produce realistic-looking fluctuations.
         *
         * The randomization uses a hash-based approach seeded from the
         * current refresh count to produce deterministic but varied results
         * across consecutive simulations.
         */

        /* Seed value derived from the refresh count. Each simulation produces
         * a different adjustment pattern. The multiplication by a prime number
         * spreads the values to avoid obvious patterns. */
        int seed = (refreshCount + 1) * 7;

        /* Active Users: fluctuate by -50 to +50 around the current value.
         * The modulo arithmetic produces values in the range [-50, +49]. */
        int userDelta = (seed % 101) - 50;
        activeUsers = Math.max(0, activeUsers + userDelta);
        activeUsersProp.set(activeUsers);

        /* API Requests: fluctuate by -200 to +200. */
        int requestDelta = ((seed * 3) % 401) - 200;
        apiRequests = Math.max(0, apiRequests + requestDelta);
        apiRequestsProp.set(apiRequests);

        /* Error Rate: fluctuate by -2 to +2, clamped to [0, 50] range.
         * This represents 0.0% to 5.0% error rate. */
        int errorDelta = ((seed * 5) % 5) - 2;
        errorRate = Math.max(0, Math.min(50, errorRate + errorDelta));
        errorRateProp.set(errorRate);

        /* Avg Response: fluctuate by -20 to +20ms, clamped to [50, 500] range.
         * Values below 50ms are unrealistic, and above 500ms indicate problems. */
        int responseDelta = ((seed * 11) % 41) - 20;
        avgResponse = Math.max(50, Math.min(500, avgResponse + responseDelta));
        avgResponseProp.set(avgResponse);

        /* Deployments: can only increase (you can't un-deploy). Add 0 or 1. */
        int deployDelta = (seed % 3 == 0) ? 1 : 0;
        deployments = deployments + deployDelta;
        deploymentsProp.set(deployments);

        /* Increment the refresh counter to reflect that an update occurred. */
        refreshCount++;
    }
}
