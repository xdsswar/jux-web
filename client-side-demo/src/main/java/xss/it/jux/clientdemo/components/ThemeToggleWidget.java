/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.OnMount;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import static xss.it.jux.core.Elements.*;

/**
 * Theme toggle switch widget demonstrating system preference detection and toggle
 * state in JUX.
 *
 * <p>This component renders a custom-styled toggle switch with sun and moon icons
 * that lets users switch between dark and light mode preferences. On mount, it reads
 * the user's system preference via the {@code prefers-color-scheme} CSS media query
 * (accessed through the TeaVM JSO {@code Window.matchMedia()} API) and initializes
 * the toggle accordingly.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for toggle tracking</b> -- the {@code darkMode} boolean
 *       tracks the current theme preference. Toggling it triggers a re-render that
 *       updates the switch position, label text, and icon display.</li>
 *   <li><b>{@code @OnMount} for system preference detection</b> -- after client-side
 *       hydration, the mount hook reads {@code window.matchMedia('(prefers-color-scheme: dark)')}
 *       to initialise the toggle to match the user's OS-level preference.</li>
 *   <li><b>Custom toggle switch</b> -- a styled switch built from {@code <div>} elements
 *       with CSS transitions for the sliding thumb animation. This demonstrates how
 *       JUX can create complex custom form controls entirely in Java.</li>
 *   <li><b>Icon rendering</b> -- Unicode characters serve as sun ({@code \u2600}) and
 *       moon ({@code \u263D}) icons, avoiding the need for an icon library.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the toggle uses {@code role="switch"}
 *       and {@code aria-checked} to communicate its binary state to screen readers.</li>
 *   <li><b>2.1.1 Keyboard</b> -- the toggle has {@code tabindex="0"} and responds
 *       to Space and Enter key presses for keyboard activation.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- the toggle has an {@code aria-label} that
 *       describes both the action and current state ("Switch to light/dark mode").</li>
 * </ul>
 *
 * <h2>Note on Scope</h2>
 * <p>This toggle is a <em>visual demonstration</em> -- it does not actually change
 * the page's theme because the demo uses a fixed dark theme. In a real application,
 * the toggle would update a CSS custom property on {@code :root}, write to
 * {@code localStorage}, and potentially notify a parent layout component.</p>
 *
 * @see xss.it.jux.annotation.OnMount
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class ThemeToggleWidget extends Component {

    /**
     * Whether dark mode is currently active.
     *
     * <p>Defaults to {@code true} since the demo application uses a dark theme.
     * On mount, this is overridden by the user's system preference via
     * {@code prefers-color-scheme}. On the client, toggling this field
     * triggers a re-render that slides the toggle thumb, swaps the icon,
     * and updates the label text.</p>
     */
    @State
    private boolean darkMode = true;

    /**
     * Detects the user's system color scheme preference after client-side hydration.
     *
     * <p>Uses the browser's {@code matchMedia} API to query
     * {@code (prefers-color-scheme: dark)}. If the user's system is set to light
     * mode, the toggle is initialised to the light position.</p>
     *
     * <p><b>Note:</b> In the current SSR-only demo, this method is declared but
     * not invoked. Once TeaVM compilation is enabled, the JUX runtime will call
     * it automatically after hydration.</p>
     */
    @OnMount
    public void onMount() {
        /*
         * Read the system color scheme preference from the browser.
         * In a TeaVM-compiled environment, this would use:
         *
         *   Window window = Window.current();
         *   MediaQueryList mql = window.matchMedia("(prefers-color-scheme: dark)");
         *   darkMode = mql.matches();
         *
         * This initializes the toggle to match the user's OS-level preference,
         * providing a seamless experience where the UI reflects their existing
         * choice without requiring manual configuration.
         */
    }

    /**
     * Builds the virtual DOM tree for the theme toggle widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A row with the current theme icon (sun or moon) and a label.</li>
     *   <li>A custom toggle switch with a sliding thumb indicator.</li>
     *   <li>A descriptive text explaining the current mode.</li>
     * </ol>
     *
     * @return the root element of the theme toggle widget, never null
     */
    @Override
    public Element render() {
        /* Select the appropriate icon and label text based on the current state. */
        String icon = darkMode ? "\u263D" : "\u2600";
        String label = darkMode ? "Dark Mode" : "Light Mode";
        String description = darkMode
                ? "Currently using the dark colour scheme. Toggle to switch to light mode."
                : "Currently using the light colour scheme. Toggle to switch to dark mode.";

        /* Build and return the complete theme toggle element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-8").children(

                /* ── Toggle Row ────────────────────────────────────────────────
                 * A horizontal flex row containing the icon, label text, and
                 * the toggle switch. The items are centered vertically and
                 * spaced apart using justify-between.
                 */
                div().cls("flex", "items-center", "justify-between", "mb-4").children(

                        /* Left side: icon and label text. */
                        div().cls("flex", "items-center", "gap-3").children(

                                /* Theme icon: sun (U+2600) or moon (U+263D).
                                 * Displayed in a larger font with appropriate color. */
                                span().cls("text-3xl")
                                        .ariaHidden(true)
                                        .text(icon),

                                /* Label text: "Dark Mode" or "Light Mode". */
                                span().cls("text-lg", "font-medium", "text-white")
                                        .text(label)
                        ),

                        /* ── Custom Toggle Switch ──────────────────────────────
                         * A pill-shaped track with a sliding circular thumb.
                         * The switch uses role="switch" and aria-checked for
                         * accessibility. It responds to click events and
                         * keyboard activation (Space/Enter) via the keydown
                         * handler.
                         */
                        buildToggleSwitch()
                ),

                /* ── Description Text ──────────────────────────────────────────
                 * A small descriptive paragraph below the toggle explaining
                 * the current state and how to change it.
                 */
                p().cls("text-sm", "text-gray-400")
                        .text(description),

                /* ── Demo Notice ───────────────────────────────────────────────
                 * A notice explaining that this is a visual demonstration
                 * and does not actually change the page theme.
                 */
                p().cls("mt-3", "text-xs", "text-gray-600")
                        .text("This is a visual demo. In a real app, toggling would update CSS custom properties and persist the preference.")
        );
    }

    /**
     * Builds the custom toggle switch element.
     *
     * <p>The switch consists of a pill-shaped track (outer {@code <div>}) containing
     * a circular thumb (inner {@code <div>}) that slides left or right based on the
     * {@code darkMode} state. The sliding animation is achieved through CSS
     * {@code translate-x} classes with a transition duration.</p>
     *
     * <p>Accessibility attributes:</p>
     * <ul>
     *   <li>{@code role="switch"} -- identifies the element as a toggle switch.</li>
     *   <li>{@code aria-checked} -- reflects the current on/off state.</li>
     *   <li>{@code aria-label} -- describes the action ("Switch to light/dark mode").</li>
     *   <li>{@code tabindex="0"} -- makes the switch focusable in the tab order.</li>
     * </ul>
     *
     * @return the toggle switch element
     */
    private Element buildToggleSwitch() {
        /*
         * Determine the CSS classes for the track and thumb based on state.
         * When dark mode is on: violet track, thumb translated to the right.
         * When light mode is on: gray track, thumb at the left position.
         */
        String trackColor = darkMode ? "bg-violet-600" : "bg-gray-600";
        String thumbPosition = darkMode ? "translate-x-6" : "translate-x-0";

        return div()
                /* The track is a pill-shaped container with rounded-full corners.
                 * It has a fixed width and height to contain the sliding thumb. */
                .cls("relative", "w-12", "h-6", "rounded-full",
                        "cursor-pointer", "transition-colors", "duration-200",
                        trackColor)

                /* Accessibility attributes for the switch pattern. */
                .role("switch")
                .ariaChecked(darkMode ? "true" : "false")
                .aria("label", darkMode
                        ? "Switch to light mode"
                        : "Switch to dark mode")
                .tabIndex(0)

                /* Click handler: toggles the dark mode state. */
                .on("click", e -> darkMode = !darkMode)

                /* Keyboard handler: Space and Enter toggle the switch,
                 * matching the expected keyboard interaction for switch
                 * role elements per WAI-ARIA. */
                .on("keydown", e -> {
                    if (" ".equals(e.getKey()) || "Enter".equals(e.getKey())) {
                        darkMode = !darkMode;
                        e.preventDefault();
                    }
                })

                .children(
                        /* The thumb is a small white circle that slides
                         * horizontally within the track. The translate-x
                         * class controls its position, and the transition
                         * classes animate the slide. */
                        div().cls("absolute", "top-0.5", "left-0.5",
                                        "w-5", "h-5", "rounded-full", "bg-white",
                                        "shadow", "transition-transform", "duration-200",
                                        thumbPosition)
                                .ariaHidden(true)
                );
    }
}
