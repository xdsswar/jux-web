/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import org.teavm.jso.browser.Window;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import static xss.it.jux.core.Elements.*;

/**
 * Stopwatch timer widget demonstrating lifecycle hooks in JUX.
 *
 * <p>This component showcases the {@code @OnMount} and {@code @OnUnmount} lifecycle
 * annotations, which allow client-side components to perform initialization after
 * DOM hydration and cleanup before removal. The stopwatch uses a browser interval
 * timer (via {@code Window.setInterval}) to update elapsed time, and properly clears
 * the interval in the unmount hook to prevent memory leaks.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for time tracking</b> -- two reactive fields track the
 *       elapsed time ({@code elapsedMs}) and running state ({@code running}). The
 *       interval callback updates {@code elapsedMs} every 10ms, triggering re-renders
 *       that update the displayed time.</li>
 *   <li><b>{@code @OnMount}</b> -- after client-side hydration, the mount hook sets up
 *       the browser interval timer using {@code Window.current().setInterval()}.
 *       This demonstrates post-hydration initialization that requires browser APIs.</li>
 *   <li><b>{@code @OnUnmount}</b> -- before the component is removed from the DOM,
 *       the unmount hook clears the interval timer via {@code Window.current().clearInterval()},
 *       preventing the callback from firing on a detached component.</li>
 *   <li><b>Conditional button rendering</b> -- the Start/Pause button label and style
 *       change based on the {@code running} state, demonstrating dynamic UI that
 *       reflects component state.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- all controls are native {@code <button>} elements,
 *       inherently keyboard-accessible.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- each button has a descriptive
 *       {@code aria-label} that reflects its current action.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the time display is in an
 *       {@code aria-live="off"} region to avoid overwhelming screen readers with
 *       rapid updates. A separate polite live region announces state changes
 *       (started/paused/reset).</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The initial server-rendered state shows "00:00.00" with a "Start" button and a
 * disabled-looking "Reset" button. The interval timer is only set up after client-side
 * hydration via the {@code @OnMount} hook.</p>
 *
 * <h2>Time Format</h2>
 * <p>The display format is {@code MM:SS.cc} where:</p>
 * <ul>
 *   <li>{@code MM} = minutes (zero-padded to 2 digits)</li>
 *   <li>{@code SS} = seconds (zero-padded to 2 digits)</li>
 *   <li>{@code cc} = centiseconds / hundredths of a second (zero-padded to 2 digits)</li>
 * </ul>
 *
 * @see xss.it.jux.annotation.OnMount
 * @see xss.it.jux.annotation.OnUnmount
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class StopwatchWidget extends Component {

    /**
     * Elapsed time in milliseconds since the stopwatch was started.
     *
     * <p>Updated every 10ms by the interval callback when the stopwatch is running.
     * Each update triggers a re-render that recomputes the formatted time display.
     * The initial value of 0 produces the display "00:00.00".</p>
     */
    @State
    private long elapsedMs = 0;

    /**
     * Whether the stopwatch is currently running (counting up).
     *
     * <p>When {@code true}, the interval callback increments {@code elapsedMs} on
     * each tick. When {@code false}, the callback is a no-op, effectively pausing
     * the timer without clearing the interval itself.</p>
     */
    @State
    private boolean running = false;

    /**
     * The browser interval timer ID returned by {@code Window.setInterval()}.
     *
     * <p>Stored so the interval can be cleared in the {@code @OnUnmount} hook.
     * This is NOT a {@code @State} field because changing it should not trigger
     * a re-render -- it is internal bookkeeping for the timer lifecycle.</p>
     */
    private int intervalId = 0;

    /**
     * Initializes the browser interval timer after client-side hydration.
     *
     * <p>This method is called once by the JUX runtime after the component's
     * server-rendered HTML has been hydrated with event listeners and state.
     * It sets up a repeating interval that fires every 10 milliseconds. On each
     * tick, if the stopwatch is running, the elapsed time is incremented.</p>
     *
     * <p>The interval is always active (even when paused) to simplify the
     * start/pause logic. When paused, the callback simply returns without
     * modifying state. This avoids the complexity of creating/clearing intervals
     * on every start/pause toggle.</p>
     *
     * <p><b>Note:</b> In the current SSR-only demo, this method is declared but
     * not invoked because client-side hydration via TeaVM is not yet wired up.
     * Once TeaVM compilation is enabled, the JUX runtime will call this method
     * automatically after hydration.</p>
     */
    @Override
    public void onMount() {
        intervalId = Window.current().setInterval(() -> {
            if (running) {
                elapsedMs += 10;
                ClientMain.getStateManager().notifyStateChange(this);
            }
        }, 10);
    }

    /**
     * Clears the browser interval timer before the component is removed from the DOM.
     *
     * <p>This cleanup hook prevents the interval callback from continuing to fire
     * after the component's DOM nodes are removed, which would cause errors and
     * memory leaks. It is the lifecycle counterpart to the interval setup in
     * {@link #onMount()}.</p>
     *
     * <p><b>Note:</b> As with {@code onMount()}, this method is declared but
     * not invoked in the current SSR-only demo.</p>
     */
    @Override
    public void onUnmount() {
        if (intervalId != 0) {
            Window.current().clearInterval(intervalId);
            intervalId = 0;
        }
    }

    /**
     * Builds the virtual DOM tree for the stopwatch widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A large monospace time display showing MM:SS.cc format.</li>
     *   <li>A subtle progress/running indicator bar that shows a violet gradient
     *       when the stopwatch is running.</li>
     *   <li>Control buttons: Start/Pause toggle and Reset.</li>
     *   <li>A status message in an aria-live region announcing state changes.</li>
     * </ol>
     *
     * @return the root element of the stopwatch widget, never null
     */
    @Override
    public Element render() {
        /* Format the elapsed time into MM:SS.cc display format. */
        String formattedTime = formatTime(elapsedMs);

        /* Determine the status text for the screen reader announcement. */
        String statusText = running
                ? "Stopwatch running"
                : elapsedMs > 0
                        ? "Stopwatch paused at " + formattedTime
                        : "Stopwatch ready";

        /* Build and return the complete stopwatch element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-8", "text-center").children(

                /* ── Time Display ──────────────────────────────────────────────
                 * A large monospace font display showing the elapsed time in
                 * MM:SS.cc format. The aria-live is set to "off" to prevent
                 * screen readers from announcing rapid 10ms updates. The
                 * status message below handles screen reader announcements
                 * at a more reasonable pace.
                 */
                div().cls("mb-6").children(
                        span().cls("text-6xl", "font-mono", "font-bold", "text-white",
                                        "tracking-wider")
                                .ariaLive("off")
                                .attr("aria-label", "Elapsed time: " + formattedTime)
                                .text(formattedTime)
                ),

                /* ── Running Indicator Bar ─────────────────────────────────────
                 * A thin horizontal bar below the time display. When the
                 * stopwatch is running, it shows a violet gradient that
                 * pulses via CSS animation. When paused or stopped, it
                 * displays as a dim gray line.
                 */
                div().cls("h-1", "rounded-full", "mb-6", "mx-auto", "max-w-xs")
                        .cls(running
                                ? "bg-gradient-to-r from-violet-600 via-violet-400 to-violet-600"
                                : "bg-gray-700")
                        .ariaHidden(true),

                /* ── Control Buttons ───────────────────────────────────────────
                 * Two buttons: Start/Pause toggle and Reset. The Start/Pause
                 * button changes its label and colour based on the running state.
                 */
                div().cls("flex", "items-center", "justify-center", "gap-4").children(

                        /* Start/Pause toggle button.
                         * When running: shows "Pause" in amber/yellow styling.
                         * When stopped: shows "Start" in emerald/green styling. */
                        button().attr("type", "button")
                                .cls("px-6", "py-3", "rounded-lg", "font-medium",
                                        "text-white", "transition-colors",
                                        "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800")
                                .cls(running
                                        ? "bg-amber-600 hover:bg-amber-500 focus:ring-amber-500"
                                        : "bg-emerald-600 hover:bg-emerald-500 focus:ring-emerald-500")
                                .aria("label", running ? "Pause stopwatch" : "Start stopwatch")
                                .on("click", e -> running = !running)
                                .text(running ? "Pause" : "Start"),

                        /* Reset button: stops the timer and resets elapsed time to zero.
                         * Uses a neutral gray style. Slightly muted when the elapsed
                         * time is already zero to indicate there is nothing to reset. */
                        button().attr("type", "button")
                                .cls("px-6", "py-3", "rounded-lg", "font-medium",
                                        "transition-colors",
                                        "focus:ring-2", "focus:ring-gray-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .cls(elapsedMs > 0
                                        ? "bg-gray-700 text-gray-200 hover:bg-gray-600"
                                        : "bg-gray-700/50 text-gray-500")
                                .aria("label", "Reset stopwatch to zero")
                                .on("click", e -> {
                                    /* Stop the timer and reset the elapsed time. Both
                                     * state changes happen in the same event handler,
                                     * resulting in a single batched re-render. */
                                    running = false;
                                    elapsedMs = 0;
                                })
                                .text("Reset")
                ),

                /* ── Status Message (ARIA Live Region) ─────────────────────────
                 * A polite live region that announces the stopwatch state to
                 * screen readers when it changes (started, paused, or reset).
                 * This provides accessibility without the noise of announcing
                 * every 10ms time update.
                 */
                p().cls("mt-4", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text(statusText)
        );
    }

    /**
     * Formats milliseconds into a human-readable {@code MM:SS.cc} time string.
     *
     * <p>The format breaks down as:</p>
     * <ul>
     *   <li><b>MM</b> -- minutes, zero-padded to 2 digits (00-99)</li>
     *   <li><b>SS</b> -- seconds within the current minute, zero-padded (00-59)</li>
     *   <li><b>cc</b> -- centiseconds (hundredths of a second), zero-padded (00-99)</li>
     * </ul>
     *
     * <p>Examples: 0ms = "00:00.00", 61230ms = "01:01.23", 3723400ms = "62:03.40".</p>
     *
     * @param ms the elapsed time in milliseconds, must be non-negative
     * @return the formatted time string in MM:SS.cc format
     */
    private String formatTime(long ms) {
        /* Extract centiseconds (hundredths) from the millisecond remainder. */
        long centiseconds = (ms / 10) % 100;

        /* Extract whole seconds from the total, then take modulo 60 for display. */
        long totalSeconds = ms / 1000;
        long seconds = totalSeconds % 60;

        /* Extract whole minutes from the total seconds. */
        long minutes = totalSeconds / 60;

        /* Format each component with zero-padding to ensure consistent width.
         * String.format is used for clarity; in performance-critical client code,
         * manual formatting could avoid the overhead. */
        return String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
    }
}
