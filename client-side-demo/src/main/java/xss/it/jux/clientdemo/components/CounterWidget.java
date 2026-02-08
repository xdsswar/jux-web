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

import static xss.it.jux.core.Elements.*;

/**
 * Interactive counter widget demonstrating reactive state management in JUX.
 *
 * <p>This component showcases the two most fundamental client-side features of the
 * JUX framework: {@code @State} for reactive field tracking and inline
 * {@code .on("click", ...)} event handlers for responding to user interactions.
 * When a {@code @State} field changes, the framework automatically re-renders the
 * component and performs a virtual DOM diff/patch, applying only the minimal set of
 * mutations to the real browser DOM.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State}</b> -- the {@code count} field is marked as reactive state.
 *       Any assignment to this field (e.g. {@code count++}, {@code count = 0}) triggers
 *       an automatic re-render cycle on the client. The framework intercepts field writes
 *       via TeaVM bytecode instrumentation.</li>
 *   <li><b>{@code .on("click", handler)}</b> -- DOM event handlers are registered inline
 *       on button elements. During SSR, these are serialized as {@code data-jux-event}
 *       attributes. During client-side hydration, they are attached as real DOM event
 *       listeners via {@code org.teavm.jso.dom.html.HTMLElement.addEventListener}.</li>
 *   <li><b>Conditional CSS classes</b> -- the display color changes dynamically based on
 *       whether the count is positive (emerald/green), negative (rose/red), or zero
 *       (white), demonstrating how state drives visual presentation.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- all controls are native {@code <button>} elements,
 *       inherently keyboard-accessible with Enter and Space activation.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- each button has a descriptive
 *       {@code aria-label} since the visible text is only a symbol (+, -, Reset).</li>
 *   <li><b>4.1.3 Status Messages</b> -- an {@code aria-live="polite"} region at the
 *       bottom announces the current count to screen readers whenever it changes,
 *       without stealing focus.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>Since the jux-client TeaVM pipeline is not yet wired up for compilation, this
 * component renders its <em>initial</em> SSR state (count = 0). The event handlers
 * are present in the element tree but will only become active once client-side
 * hydration is enabled.</p>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class CounterWidget extends Component {

    /**
     * The current count value tracked as reactive state.
     *
     * <p>On the client, every write to this field (increment, decrement, or reset)
     * triggers the JUX reactivity system to call {@link #render()} again, diff the
     * resulting element tree against the previous one, and patch only the changed
     * DOM nodes. The initial value of {@code 0} is used for SSR.</p>
     */
    @State
    private int count = 0;

    /**
     * Builds the virtual DOM tree for the counter widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A large numeric display showing the current count value, colour-coded
     *       by sign (green for positive, red for negative, white for zero).</li>
     *   <li>A row of three control buttons: decrement (&minus;), reset, and
     *       increment (+).</li>
     *   <li>A status message in an {@code aria-live} region that announces the
     *       current count to assistive technology.</li>
     * </ol>
     *
     * <p>Each button registers a {@code "click"} event handler via
     * {@link Element#on(String, xss.it.jux.core.EventHandler)}. On the client,
     * these lambdas mutate the {@code @State count} field, which automatically
     * triggers a re-render.</p>
     *
     * @return the root element of the counter widget, never null
     */
    @Override
    public Element render() {
        /*
         * Determine the text color CSS class based on the current count value.
         * Positive counts display in emerald green to indicate "above zero",
         * negative counts display in rose red to indicate "below zero", and
         * a count of exactly zero displays in plain white (the neutral state).
         */
        String countColor = count > 0
                ? "text-emerald-400"
                : count < 0
                        ? "text-rose-400"
                        : "text-white";

        /* Build and return the complete counter widget element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-8", "text-center").children(

                /* ── Large Counter Display ─────────────────────────────────────
                 * The count is shown in a very large, bold font (text-7xl) with
                 * the dynamically-computed color class. The "counter-display"
                 * class enables tabular-nums in CSS so that digits maintain
                 * consistent width as the number changes, preventing layout
                 * shifts during rapid counting.
                 */
                div().cls("mb-6").children(
                        span().cls("text-7xl", "font-bold", "counter-display", countColor)
                                .text(String.valueOf(count))
                ),

                /* ── Control Buttons Row ───────────────────────────────────────
                 * Three buttons arranged in a horizontal flex row with gap spacing:
                 * decrement (left), reset (center), increment (right).
                 */
                div().cls("flex", "items-center", "justify-center", "gap-4").children(

                        /* Decrement button: subtracts 1 from the count.
                         * Uses a rose/red color scheme to visually associate it
                         * with "decrease" or "negative direction".
                         * The Unicode minus sign (U+2212) is used instead of a
                         * hyphen for typographic correctness.
                         */
                        button().attr("type", "button")
                                .cls("w-14", "h-14", "rounded-xl",
                                        "bg-rose-500/20", "text-rose-400",
                                        "text-2xl", "font-bold",
                                        "hover:bg-rose-500/30", "transition-colors",
                                        "focus:ring-2", "focus:ring-rose-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .aria("label", "Decrease count")
                                .on("click", e -> count--)
                                .text("\u2212"),

                        /* Reset button: sets the count back to zero.
                         * Uses a neutral gray color scheme since it is neither
                         * positive nor negative in nature. The text label "Reset"
                         * is self-explanatory so the aria-label provides the
                         * additional context "Reset count to zero".
                         */
                        button().attr("type", "button")
                                .cls("px-5", "h-10", "rounded-lg",
                                        "bg-gray-700", "text-gray-300",
                                        "text-sm", "font-medium",
                                        "hover:bg-gray-600", "transition-colors",
                                        "focus:ring-2", "focus:ring-violet-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .aria("label", "Reset count to zero")
                                .on("click", e -> count = 0)
                                .text("Reset"),

                        /* Increment button: adds 1 to the count.
                         * Uses an emerald/green color scheme to visually associate
                         * it with "increase" or "positive direction".
                         */
                        button().attr("type", "button")
                                .cls("w-14", "h-14", "rounded-xl",
                                        "bg-emerald-500/20", "text-emerald-400",
                                        "text-2xl", "font-bold",
                                        "hover:bg-emerald-500/30", "transition-colors",
                                        "focus:ring-2", "focus:ring-emerald-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .aria("label", "Increase count")
                                .on("click", e -> count++)
                                .text("+")
                ),

                /* ── Status Message (ARIA Live Region) ─────────────────────────
                 * A polite live region that announces the current count value
                 * to screen readers whenever it changes. "polite" means the
                 * announcement waits until the user is idle, avoiding
                 * interruption during rapid button clicks.
                 */
                p().cls("mt-4", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text("Count is " + count)
        );
    }
}
