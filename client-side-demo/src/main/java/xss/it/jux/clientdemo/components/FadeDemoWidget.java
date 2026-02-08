/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.animations.transition.FadeTransition;
import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.clientdemo.client.BrowserFrameScheduler;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static xss.it.jux.core.Elements.*;

/**
 * Demonstrates {@link FadeTransition} with interactive controls.
 *
 * <p>A coloured card has its opacity driven by a {@link SimpleDoubleProperty}
 * which is animated by a {@code FadeTransition}. The user can play, reverse,
 * and reset the animation, switch between cycle modes (once vs infinite +
 * auto-reverse), and select different interpolators.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>FadeTransition</b> -- drives a DoubleProperty from 1.0 to 0.0</li>
 *   <li><b>Interpolator selection</b> -- switchable easing curves</li>
 *   <li><b>Cycle modes</b> -- once vs infinite with auto-reverse</li>
 *   <li><b>Reactive property listeners</b> -- property changes are mirrored
 *       to {@code @State} fields for re-rendering</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- all controls are native buttons.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- buttons have descriptive labels.</li>
 *   <li><b>4.1.3 Status Messages</b> -- opacity readout in a polite live region.</li>
 * </ul>
 */
@JuxComponent(clientSide = true)
public class FadeDemoWidget extends Component {

    /* ── Reactive animation target ──────────────────────────────── */
    private final SimpleDoubleProperty opacity = new SimpleDoubleProperty(this, "opacity", 1.0);

    /* ── @State fields for rendering ────────────────────────────── */
    @State
    private double currentOpacity = 1.0;

    @State
    private boolean playing = false;

    @State
    private int selectedInterpolator = 0;

    @State
    private boolean infiniteCycle = false;

    /* ── Animation instance ─────────────────────────────────────── */
    private FadeTransition fadeTransition;

    private static final String[] INTERPOLATOR_NAMES = {
            "EASE_BOTH", "LINEAR", "EASE_IN", "EASE_OUT", "BOUNCE"
    };

    private static final Interpolator[] INTERPOLATORS = {
            Interpolator.EASE_BOTH, Interpolator.LINEAR,
            Interpolator.EASE_IN, Interpolator.EASE_OUT, Interpolator.BOUNCE
    };

    private boolean schedulerInitialised;

    private void ensureScheduler() {
        if (!schedulerInitialised) {
            FrameScheduler.setDefault(new BrowserFrameScheduler());
            schedulerInitialised = true;
        }
    }

    @Override
    public void onMount() {
        ensureScheduler();
        buildTransition();

        opacity.addListener((obs, oldVal, newVal) -> {
            currentOpacity = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });
    }

    @Override
    public void onUnmount() {
        if (fadeTransition != null) {
            fadeTransition.stop();
        }
    }

    private void buildTransition() {
        fadeTransition = new FadeTransition(Duration.seconds(1), opacity);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setInterpolator(INTERPOLATORS[selectedInterpolator]);

        if (infiniteCycle) {
            fadeTransition.setCycleCount(Animation.INDEFINITE);
            fadeTransition.setAutoReverse(true);
        } else {
            fadeTransition.setCycleCount(1);
            fadeTransition.setAutoReverse(false);
        }

        fadeTransition.setOnFinished(() -> {
            playing = false;
            ClientMain.getStateManager().notifyStateChange(this);
        });
    }

    private void play() {
        ensureScheduler();
        if (fadeTransition != null) {
            fadeTransition.stop();
        }
        buildTransition();
        playing = true;
        fadeTransition.playFromStart();
    }

    private void reverse() {
        ensureScheduler();
        if (fadeTransition != null) {
            fadeTransition.stop();
        }
        /* Build a transition that goes from current opacity back to 1.0 */
        fadeTransition = new FadeTransition(Duration.seconds(1), opacity);
        fadeTransition.setFromValue(currentOpacity);
        fadeTransition.setToValue(1.0);
        fadeTransition.setInterpolator(INTERPOLATORS[selectedInterpolator]);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);
        fadeTransition.setOnFinished(() -> {
            playing = false;
            ClientMain.getStateManager().notifyStateChange(this);
        });
        playing = true;
        fadeTransition.play();
    }

    private void reset() {
        if (fadeTransition != null) {
            fadeTransition.stop();
        }
        opacity.set(1.0);
        playing = false;
    }

    @Override
    public Element render() {
        int pct = (int) Math.round(currentOpacity * 100);

        return div().children(

                /* ── Animated card ─────────────────────────────────────── */
                div().cls("rounded-xl", "p-8", "mb-6", "text-center",
                                "bg-gradient-to-r", "from-violet-600", "to-indigo-700")
                        .style("opacity", String.valueOf(currentOpacity))
                        .children(
                                p().cls("text-white", "text-lg", "font-semibold")
                                        .text("Opacity: " + pct + "%")
                        ),

                /* ── Interpolator selector ─────────────────────────────── */
                div().cls("flex", "flex-wrap", "gap-2", "mb-4").children(
                        interpolatorButton(0),
                        interpolatorButton(1),
                        interpolatorButton(2),
                        interpolatorButton(3),
                        interpolatorButton(4)
                ),

                /* ── Cycle toggle ──────────────────────────────────────── */
                div().cls("flex", "gap-2", "mb-4").children(
                        button().attr("type", "button")
                                .cls("px-3", "py-1.5", "rounded-lg", "text-xs", "font-medium",
                                        "transition-colors")
                                .cls(!infiniteCycle
                                        ? "bg-violet-600 text-white"
                                        : "bg-gray-700 text-gray-400 hover:text-white")
                                .on("click", e -> {
                                    infiniteCycle = false;
                                })
                                .text("Once"),
                        button().attr("type", "button")
                                .cls("px-3", "py-1.5", "rounded-lg", "text-xs", "font-medium",
                                        "transition-colors")
                                .cls(infiniteCycle
                                        ? "bg-violet-600 text-white"
                                        : "bg-gray-700 text-gray-400 hover:text-white")
                                .on("click", e -> {
                                    infiniteCycle = true;
                                })
                                .text("Infinite + AutoReverse")
                ),

                /* ── Control buttons ───────────────────────────────────── */
                div().cls("flex", "items-center", "gap-3").children(
                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800")
                                .cls(playing
                                        ? "bg-gray-600 cursor-not-allowed"
                                        : "bg-emerald-600 hover:bg-emerald-500 focus:ring-emerald-500")
                                .aria("label", "Play fade animation")
                                .on("click", e -> {
                                    if (!playing) play();
                                })
                                .text("Play"),

                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800",
                                        "bg-amber-600", "hover:bg-amber-500", "focus:ring-amber-500")
                                .aria("label", "Reverse fade animation")
                                .on("click", e -> reverse())
                                .text("Reverse"),

                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium",
                                        "transition-colors", "focus:ring-2", "focus:ring-gray-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800",
                                        "bg-gray-700", "text-gray-200", "hover:bg-gray-600")
                                .aria("label", "Reset opacity to 100%")
                                .on("click", e -> reset())
                                .text("Reset")
                ),

                /* ── Status ────────────────────────────────────────────── */
                p().cls("mt-4", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text(playing ? "Playing..." : "Opacity: " + pct + "%")
        );
    }

    private Element interpolatorButton(int idx) {
        boolean active = (selectedInterpolator == idx);
        return button().attr("type", "button")
                .cls("px-3", "py-1.5", "rounded-lg", "text-xs", "font-medium",
                        "transition-colors")
                .cls(active
                        ? "bg-violet-600 text-white"
                        : "bg-gray-700 text-gray-400 hover:text-white")
                .aria("label", "Use " + INTERPOLATOR_NAMES[idx] + " interpolator")
                .on("click", e -> {
                    selectedInterpolator = idx;
                })
                .text(INTERPOLATOR_NAMES[idx]);
    }
}
