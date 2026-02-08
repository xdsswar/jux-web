/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Stats visualization widget demonstrating data-driven chart rendering in JUX.
 *
 * <p>This component renders an interactive horizontal bar chart built entirely from
 * {@code <div>} elements -- no Canvas, no SVG, no JavaScript charting libraries. The
 * chart displays weekly data (Monday through Sunday) for one of three selectable
 * metrics: Requests, Errors, or Latency. Users switch between metrics via a button
 * group, and the chart re-renders with the corresponding dataset.</p>
 *
 * <h2>Reactive Collections Demonstrated</h2>
 * <ul>
 *   <li><b>{@link ObservableList}</b> -- the chart data is stored in an
 *       {@link ObservableList} of {@link ChartBar} records, created via
 *       {@link JuxCollections#observableArrayList()}. This reactive collection
 *       allows external listeners to observe when the chart data changes (e.g.
 *       when the user switches metrics), enabling features like data export,
 *       analytics tracking, or synchronized secondary visualizations.</li>
 *   <li><b>{@link JuxCollections}</b> -- the factory methods from this utility class
 *       are used to create observable collections that integrate with the JUX
 *       reactive system. The {@code observableArrayList()} factory produces a list
 *       backed by an {@link java.util.ArrayList} with change notification support.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for metric selection</b> -- the {@code selectedMetric} field
 *       tracks which metric dataset is currently displayed. Clicking a metric selector
 *       button updates this field, triggering a re-render that swaps the chart data
 *       and recalculates bar widths.</li>
 *   <li><b>Proportional rendering</b> -- bar widths are calculated as a percentage of
 *       the maximum value in the current dataset. This ensures the longest bar always
 *       fills the available width, providing proper visual scaling regardless of the
 *       absolute data values.</li>
 *   <li><b>Derived summary statistics</b> -- below the chart, a summary section displays
 *       Total, Average, Peak, and Trend computed from the current dataset. These values
 *       are recalculated on every render, demonstrating the "derived state" pattern.</li>
 * </ul>
 *
 * <h2>Data Structure</h2>
 * <p>Each metric has 7 data points representing Monday through Sunday:</p>
 * <ul>
 *   <li><b>Requests:</b> 1200, 1800, 2100, 1900, 2400, 800, 600</li>
 *   <li><b>Errors:</b> 12, 8, 15, 7, 20, 5, 3</li>
 *   <li><b>Latency:</b> 145, 132, 158, 141, 167, 120, 115 (in milliseconds)</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.1.1 Non-text Content</b> -- the chart container has {@code role="img"} with
 *       an {@code aria-label} that describes the chart content (e.g. "Horizontal bar chart
 *       showing Requests by day of the week"). This provides a text alternative for the
 *       visual chart for screen reader users.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- each individual bar has an {@code aria-label}
 *       containing the day name and its value (e.g. "Monday: 1200 requests"). The metric
 *       selector buttons use {@code aria-pressed} to indicate the currently active metric.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the summary statistics section uses
 *       {@code aria-live="polite"} so screen readers announce the updated statistics
 *       when the user switches metrics.</li>
 * </ul>
 *
 * <h2>Color Scheme</h2>
 * <ul>
 *   <li><b>Requests:</b> violet gradient ({@code bg-violet-500})</li>
 *   <li><b>Errors:</b> rose/red gradient ({@code bg-rose-500})</li>
 *   <li><b>Latency:</b> amber/yellow gradient ({@code bg-amber-500})</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The initial server-rendered state shows the "Requests" metric selected, with all
 * seven bars rendered at their correct proportional widths. The metric selector buttons
 * and hover effects become interactive after client-side hydration.</p>
 *
 * @see ObservableList
 * @see JuxCollections
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class ChartWidget extends Component {

    // ─── Reactive Collections ────────────────────────────────────────────────────
    // The chart data is stored in an ObservableList, enabling external listeners
    // to react to dataset changes (e.g. when the user switches metrics).

    /**
     * Observable list of chart bar entries for the currently selected metric.
     *
     * <p>This list is populated in {@link #getDataForMetric(String)} based on the
     * selected metric. The {@link ObservableList} wrapper from {@link JuxCollections}
     * fires change events whenever the list contents are replaced via {@code setAll()},
     * allowing external observers to track data changes.</p>
     *
     * <p>The list is initialized with the default "requests" dataset so that the
     * chart renders correctly during SSR without requiring any client-side initialization.</p>
     */
    private final ObservableList<ChartBar> chartData = JuxCollections.observableArrayList(
            new ChartBar("Mon", 1200, "bg-violet-500"),
            new ChartBar("Tue", 1800, "bg-violet-500"),
            new ChartBar("Wed", 2100, "bg-violet-500"),
            new ChartBar("Thu", 1900, "bg-violet-500"),
            new ChartBar("Fri", 2400, "bg-violet-500"),
            new ChartBar("Sat", 800, "bg-violet-500"),
            new ChartBar("Sun", 600, "bg-violet-500")
    );

    // ─── @State Fields ───────────────────────────────────────────────────────────

    /**
     * The currently selected metric determining which dataset is displayed.
     *
     * <p>Valid values: {@code "requests"}, {@code "errors"}, {@code "latency"}.
     * Defaults to {@code "requests"} for the initial render. Clicking a metric
     * selector button updates this field, which triggers a re-render that
     * recalculates bar data and summary statistics.</p>
     */
    @State
    private String selectedMetric = "requests";

    /**
     * Whether the chart is currently playing an animation transition.
     *
     * <p>Set to {@code true} when the user switches metrics, triggering a brief
     * visual transition effect. In the current SSR-only demo, animation is not
     * active, but the flag is included to demonstrate the pattern for future
     * client-side animation support via TeaVM and CSS transitions.</p>
     */
    @State
    private boolean animating = false;

    /**
     * Builds the virtual DOM tree for the chart visualization widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A metric selector button group (Requests, Errors, Latency)</li>
     *   <li>The horizontal bar chart with 7 bars (Mon-Sun)</li>
     *   <li>Summary statistics (Total, Average, Peak, Trend)</li>
     * </ol>
     *
     * @return the root element of the chart widget, never null
     */
    @Override
    public Element render() {
        /* Load the data for the currently selected metric. This populates
         * the chartData observable list and returns the list of ChartBar
         * records for rendering. */
        List<ChartBar> data = getDataForMetric(selectedMetric);

        /* Calculate the maximum value across all bars. This is used to compute
         * proportional bar widths: each bar's width = (value / maxValue) * 100%. */
        int maxValue = data.stream()
                .mapToInt(ChartBar::value)
                .max()
                .orElse(1); /* Default to 1 to avoid division by zero. */

        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Metric Selector Button Group ────────────────────────────
                 * Three buttons allowing the user to switch between Requests,
                 * Errors, and Latency datasets. The selected button is visually
                 * highlighted and has aria-pressed="true".
                 */
                buildMetricSelector(),

                /* ── Bar Chart Container ─────────────────────────────────────
                 * The chart is built entirely from div elements, with each bar
                 * being a flex row containing the day label, the proportional
                 * bar, and the numeric value. The container has role="img" with
                 * a descriptive aria-label for screen readers.
                 */
                div().cls("mt-6", "space-y-3")
                        .role("img")
                        .aria("label", "Horizontal bar chart showing "
                                + getMetricLabel(selectedMetric)
                                + " by day of the week")
                        .children(buildBars(data, maxValue)),

                /* ── Summary Statistics ───────────────────────────────────────
                 * A row of four computed statistics below the chart: Total,
                 * Average, Peak, and Trend. Updated whenever the metric changes.
                 */
                buildSummary(data)
        );
    }

    /**
     * Builds the metric selector button group.
     *
     * <p>Three buttons are rendered in a horizontal row with pill-shaped styling.
     * The currently selected metric's button is highlighted with a violet background
     * and has {@code aria-pressed="true"} to communicate its active state to assistive
     * technology. Non-selected buttons use a transparent background with hover effects.</p>
     *
     * @return the metric selector element
     */
    private Element buildMetricSelector() {
        return div().cls("flex", "gap-2")
                .role("group")
                .aria("label", "Chart metric selector")
                .children(
                        /* Requests button */
                        buildMetricButton("requests", "Requests"),
                        /* Errors button */
                        buildMetricButton("errors", "Errors"),
                        /* Latency button */
                        buildMetricButton("latency", "Latency")
                );
    }

    /**
     * Builds a single metric selector button.
     *
     * <p>The button toggles the chart to display the specified metric's dataset.
     * When the button's metric matches {@code selectedMetric}, the button is styled
     * with a solid violet background and {@code aria-pressed="true"}. Otherwise, it
     * uses a transparent background with a hover effect.</p>
     *
     * @param metric the metric key (e.g. "requests", "errors", "latency")
     * @param label  the visible button label text
     * @return the metric selector button element
     */
    private Element buildMetricButton(String metric, String label) {
        /* Determine whether this button's metric is currently selected. */
        boolean isSelected = selectedMetric.equals(metric);

        return button().attr("type", "button")
                .cls("px-4", "py-2", "rounded-lg", "text-sm", "font-medium",
                        "transition-colors",
                        "focus:ring-2", "focus:ring-violet-500",
                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                .cls(isSelected
                        ? "bg-violet-600 text-white"
                        : "bg-gray-700/50 text-gray-400 hover:bg-gray-700 hover:text-gray-200")
                .attr("aria-pressed", isSelected ? "true" : "false")
                .aria("label", "Show " + label.toLowerCase() + " chart")
                .on("click", e -> {
                    /* Switch to the clicked metric's dataset. Setting the @State
                     * selectedMetric field triggers a re-render that recomputes
                     * bar data and summary statistics. */
                    selectedMetric = metric;

                    /* Update the observable list with the new metric's data.
                     * This fires change events for any external observers. */
                    List<ChartBar> newData = getDataForMetric(metric);
                    chartData.setAll(newData);

                    /* Set animating flag for transition effects (future use). */
                    animating = true;
                })
                .text(label);
    }

    /**
     * Builds the list of bar elements for the chart.
     *
     * <p>Each bar is a horizontal flex row containing:</p>
     * <ul>
     *   <li>The day label (Mon-Sun) in a fixed-width column on the left</li>
     *   <li>A coloured bar div whose width is proportional to the value</li>
     *   <li>The numeric value on the right</li>
     * </ul>
     *
     * <p>Bar widths are calculated as {@code (value / maxValue) * 100}%, ensuring
     * the largest bar fills the available width and all other bars are proportionally
     * smaller.</p>
     *
     * @param data     the chart data entries for the currently selected metric
     * @param maxValue the maximum value across all bars, used for proportional scaling
     * @return a list of bar row elements
     */
    private List<Element> buildBars(List<ChartBar> data, int maxValue) {
        /* Collect the generated bar row elements into a list. */
        List<Element> bars = new ArrayList<>();

        for (ChartBar bar : data) {
            /* Calculate the bar width as a percentage of the maximum value.
             * This proportional approach ensures the longest bar always extends
             * to ~100% width, providing proper visual scaling. */
            int widthPercent = (int) ((double) bar.value() / maxValue * 100);

            /* Determine the unit suffix for the aria-label based on the
             * current metric. This provides context for screen reader users. */
            String unitLabel = getUnitLabel(selectedMetric);

            /* Build the individual bar row: day label, bar visualization, value. */
            Element barRow = div().cls("flex", "items-center", "gap-3", "group")
                    .aria("label", bar.label() + ": " + bar.value() + " " + unitLabel)
                    .children(

                            /* ── Day Label ────────────────────────────────────────
                             * A fixed-width column showing the abbreviated day name.
                             * The width is set to 2.5rem to ensure consistent alignment
                             * across all seven rows.
                             */
                            span().cls("text-sm", "text-gray-400", "w-10", "text-right",
                                            "shrink-0")
                                    .text(bar.label()),

                            /* ── Bar Container ───────────────────────────────────
                             * A flex-grow container that holds the coloured bar.
                             * The background is dark gray to show the "track" behind
                             * the filled portion.
                             */
                            div().cls("flex-1", "bg-gray-700/50", "rounded-full", "h-8",
                                            "overflow-hidden")
                                    .children(
                                            /* The coloured bar itself. Its width is set as an
                                             * inline style percentage. The bar has rounded ends
                                             * and a hover effect that increases brightness. */
                                            div().cls("h-full", "rounded-full",
                                                            "transition-all", "duration-300",
                                                            bar.color(),
                                                            "group-hover:brightness-110")
                                                    .style("width", widthPercent + "%")
                                                    .ariaHidden(true)
                                    ),

                            /* ── Value Label ──────────────────────────────────────
                             * The numeric value displayed to the right of the bar.
                             * Uses tabular-nums for consistent digit width alignment.
                             */
                            span().cls("text-sm", "font-medium", "text-gray-300",
                                            "w-16", "text-right", "shrink-0",
                                            "tabular-nums")
                                    .text(formatValue(bar.value(), selectedMetric))
                    );

            bars.add(barRow);
        }

        return bars;
    }

    /**
     * Builds the summary statistics section displayed below the chart.
     *
     * <p>The summary contains four computed metrics presented in a horizontal row:</p>
     * <ul>
     *   <li><b>Total</b> -- the sum of all 7 data points</li>
     *   <li><b>Average</b> -- the arithmetic mean of all data points</li>
     *   <li><b>Peak</b> -- the highest value among all data points</li>
     *   <li><b>Trend</b> -- a simple comparison of the last value vs the first value,
     *       displayed as "Up", "Down", or "Stable" with a coloured arrow</li>
     * </ul>
     *
     * <p>The section uses {@code aria-live="polite"} so screen readers announce the
     * updated statistics when the user switches metrics.</p>
     *
     * @param data the chart data for the current metric
     * @return the summary statistics element
     */
    private Element buildSummary(List<ChartBar> data) {
        /* Calculate the four summary statistics from the current dataset. */

        /* Total: sum of all 7 data points. */
        int total = data.stream().mapToInt(ChartBar::value).sum();

        /* Average: arithmetic mean, rounded to the nearest integer. */
        int average = data.isEmpty() ? 0 : total / data.size();

        /* Peak: the maximum value across all data points. */
        int peak = data.stream().mapToInt(ChartBar::value).max().orElse(0);

        /* Trend: compare the last data point (Sunday) against the first (Monday).
         * This gives a simple week-over-week direction indicator. */
        String trend;
        String trendColor;
        String trendArrow;
        if (data.size() >= 2) {
            int first = data.getFirst().value();
            int last = data.getLast().value();
            if (last > first) {
                trend = "Up";
                trendColor = "text-emerald-400";
                trendArrow = "\u2191"; /* ↑ */
            } else if (last < first) {
                trend = "Down";
                trendColor = "text-rose-400";
                trendArrow = "\u2193"; /* ↓ */
            } else {
                trend = "Stable";
                trendColor = "text-gray-400";
                trendArrow = "\u2192"; /* → */
            }
        } else {
            trend = "N/A";
            trendColor = "text-gray-500";
            trendArrow = "";
        }

        /* Get the unit label for formatting (e.g. "requests", "errors", "ms"). */
        String unit = getUnitLabel(selectedMetric);

        return div().cls("mt-6", "pt-4", "border-t", "border-gray-700",
                        "grid", "grid-cols-4", "gap-4")
                .ariaLive("polite")
                .children(

                        /* ── Total ────────────────────────────────────────────────
                         * Sum of all 7 days' data points.
                         */
                        div().cls("text-center").children(
                                span().cls("block", "text-xs", "text-gray-500", "mb-1")
                                        .text("Total"),
                                span().cls("block", "text-lg", "font-bold", "text-white",
                                                "tabular-nums")
                                        .text(formatLargeNumber(total))
                        ),

                        /* ── Average ──────────────────────────────────────────────
                         * Arithmetic mean of the data points.
                         */
                        div().cls("text-center").children(
                                span().cls("block", "text-xs", "text-gray-500", "mb-1")
                                        .text("Average"),
                                span().cls("block", "text-lg", "font-bold", "text-white",
                                                "tabular-nums")
                                        .text(formatValue(average, selectedMetric))
                        ),

                        /* ── Peak ─────────────────────────────────────────────────
                         * The highest value in the dataset.
                         */
                        div().cls("text-center").children(
                                span().cls("block", "text-xs", "text-gray-500", "mb-1")
                                        .text("Peak"),
                                span().cls("block", "text-lg", "font-bold", "text-white",
                                                "tabular-nums")
                                        .text(formatValue(peak, selectedMetric))
                        ),

                        /* ── Trend ────────────────────────────────────────────────
                         * Week direction: comparing Sunday (last) to Monday (first).
                         */
                        div().cls("text-center").children(
                                span().cls("block", "text-xs", "text-gray-500", "mb-1")
                                        .text("Trend"),
                                span().cls("block", "text-lg", "font-bold", trendColor)
                                        .text(trendArrow + " " + trend)
                        )
                );
    }

    /**
     * Returns the chart data for the specified metric.
     *
     * <p>Each metric has 7 data points representing Monday through Sunday. The data
     * values are hardcoded as realistic sample data. In a production application,
     * this data would be fetched from an API endpoint.</p>
     *
     * <p>The returned list also updates the {@link #chartData} observable list via
     * {@code setAll()} to keep the reactive collection synchronized.</p>
     *
     * @param metric the metric key: "requests", "errors", or "latency"
     * @return the list of ChartBar entries for the specified metric
     */
    private List<ChartBar> getDataForMetric(String metric) {
        /* Determine the bar colour class based on the metric type.
         * Each metric has a distinct colour for visual differentiation:
         * - Requests: violet (brand colour, positive connotation)
         * - Errors: rose/red (warning connotation)
         * - Latency: amber/yellow (caution connotation)
         */
        List<ChartBar> data = switch (metric) {
            case "errors" -> List.of(
                    /* Error counts per day. Friday has the highest error count (20),
                     * likely correlating with the highest request volume on that day.
                     * Weekend error counts are low due to reduced traffic. */
                    new ChartBar("Mon", 12, "bg-rose-500"),
                    new ChartBar("Tue", 8, "bg-rose-500"),
                    new ChartBar("Wed", 15, "bg-rose-500"),
                    new ChartBar("Thu", 7, "bg-rose-500"),
                    new ChartBar("Fri", 20, "bg-rose-500"),
                    new ChartBar("Sat", 5, "bg-rose-500"),
                    new ChartBar("Sun", 3, "bg-rose-500")
            );
            case "latency" -> List.of(
                    /* Average response latency in milliseconds per day. Friday has
                     * the highest latency (167ms), correlating with peak traffic.
                     * Sunday has the lowest (115ms) due to minimal load. */
                    new ChartBar("Mon", 145, "bg-amber-500"),
                    new ChartBar("Tue", 132, "bg-amber-500"),
                    new ChartBar("Wed", 158, "bg-amber-500"),
                    new ChartBar("Thu", 141, "bg-amber-500"),
                    new ChartBar("Fri", 167, "bg-amber-500"),
                    new ChartBar("Sat", 120, "bg-amber-500"),
                    new ChartBar("Sun", 115, "bg-amber-500")
            );
            default -> List.of(
                    /* Request counts per day. The distribution shows a typical
                     * business-week pattern: ramp up Mon-Fri with Friday peak,
                     * then significant drop on the weekend. */
                    new ChartBar("Mon", 1200, "bg-violet-500"),
                    new ChartBar("Tue", 1800, "bg-violet-500"),
                    new ChartBar("Wed", 2100, "bg-violet-500"),
                    new ChartBar("Thu", 1900, "bg-violet-500"),
                    new ChartBar("Fri", 2400, "bg-violet-500"),
                    new ChartBar("Sat", 800, "bg-violet-500"),
                    new ChartBar("Sun", 600, "bg-violet-500")
            );
        };

        /* Synchronize the observable list with the new data. The setAll()
         * method replaces all existing entries and fires a single change
         * notification to any attached ListChangeListeners. */
        chartData.setAll(data);

        return data;
    }

    /**
     * Returns a human-readable label for the specified metric.
     *
     * <p>Used in ARIA labels and chart descriptions to provide context
     * about what the chart is currently displaying.</p>
     *
     * @param metric the metric key
     * @return the human-readable metric label
     */
    private String getMetricLabel(String metric) {
        return switch (metric) {
            case "errors" -> "Errors";
            case "latency" -> "Latency";
            default -> "Requests";
        };
    }

    /**
     * Returns the unit label for the specified metric.
     *
     * <p>Used in ARIA labels for individual bars and summary statistics to
     * provide measurement context (e.g. "1200 requests", "145 ms").</p>
     *
     * @param metric the metric key
     * @return the unit label string
     */
    private String getUnitLabel(String metric) {
        return switch (metric) {
            case "errors" -> "errors";
            case "latency" -> "ms";
            default -> "requests";
        };
    }

    /**
     * Formats a numeric value with the appropriate unit suffix for display.
     *
     * <p>The formatting depends on the metric type:</p>
     * <ul>
     *   <li><b>latency:</b> appends "ms" suffix (e.g. "145ms")</li>
     *   <li><b>requests/errors:</b> displays the number with comma separators
     *       for values over 1000 (e.g. "1,200")</li>
     * </ul>
     *
     * @param value  the numeric value to format
     * @param metric the metric key determining the format
     * @return the formatted value string
     */
    private String formatValue(int value, String metric) {
        if ("latency".equals(metric)) {
            /* Latency values are displayed with a "ms" (milliseconds) suffix. */
            return value + "ms";
        }
        /* For requests and errors, format with comma thousands separators
         * if the value is 1000 or greater. */
        if (value >= 1000) {
            return formatLargeNumber(value);
        }
        return String.valueOf(value);
    }

    /**
     * Formats a large number with comma thousands separators.
     *
     * <p>This method manually formats the number to avoid locale-dependent
     * behaviour of {@code NumberFormat} in a client-side (TeaVM) context.
     * The algorithm iterates the digits from right to left, inserting commas
     * every three digits.</p>
     *
     * <p>Examples: 1200 = "1,200", 14523 = "14,523", 100000 = "100,000".</p>
     *
     * @param value the integer value to format, must be non-negative
     * @return the formatted string with comma separators
     */
    private String formatLargeNumber(int value) {
        /* Convert the integer to a string for digit-by-digit processing. */
        String raw = String.valueOf(value);

        /* If the number has 3 or fewer digits, no separator is needed. */
        if (raw.length() <= 3) {
            return raw;
        }

        /* Build the formatted string by inserting commas from right to left.
         * We process the digits in reverse order, inserting a comma every
         * 3 digits, then reverse the result. */
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (int i = raw.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                result.append(',');
            }
            result.append(raw.charAt(i));
            count++;
        }

        /* Reverse the built string since we processed digits right-to-left. */
        return result.reverse().toString();
    }

    /**
     * Immutable data record representing a single bar in the horizontal bar chart.
     *
     * <p>Each bar corresponds to one day of the week and contains:</p>
     * <ul>
     *   <li>A label (the abbreviated day name, e.g. "Mon", "Tue")</li>
     *   <li>A numeric value (the metric measurement for that day)</li>
     *   <li>A CSS colour class (e.g. "bg-violet-500", "bg-rose-500")</li>
     * </ul>
     *
     * <p>Records are used instead of mutable classes to align with JUX's preference
     * for immutable state. When the metric changes, an entirely new list of
     * {@code ChartBar} records is created rather than mutating existing ones.</p>
     *
     * @param label the abbreviated day-of-week name (e.g. "Mon", "Tue", "Wed")
     * @param value the numeric metric value for this day
     * @param color the Tailwind CSS background colour class for the bar
     */
    public record ChartBar(String label, int value, String color) {
    }
}
