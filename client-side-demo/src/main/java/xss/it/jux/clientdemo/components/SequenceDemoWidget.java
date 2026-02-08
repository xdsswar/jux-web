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
import xss.it.jux.animations.transition.ParallelTransition;
import xss.it.jux.animations.transition.SequentialTransition;
import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.clientdemo.client.BrowserFrameScheduler;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static xss.it.jux.core.Elements.*;

/**
 * Demonstrates the difference between {@link SequentialTransition} and
 * {@link ParallelTransition}.
 *
 * <p>Three coloured dots (red, green, blue) each have their own opacity
 * property. The user toggles between two modes:</p>
 * <ul>
 *   <li><b>Sequential</b> -- dots fade in one after another</li>
 *   <li><b>Parallel</b> -- all dots fade in simultaneously</li>
 * </ul>
 *
 * <p>The visual contrast clearly shows the difference between sequential
 * and parallel composition.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>SequentialTransition</b> -- chaining animations end-to-end</li>
 *   <li><b>ParallelTransition</b> -- running animations concurrently</li>
 *   <li><b>FadeTransition</b> -- reused for each dot</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- all controls are native buttons.</li>
 *   <li><b>4.1.3 Status Messages</b> -- polite live region for animation state.</li>
 * </ul>
 */
@JuxComponent(clientSide = true)
public class SequenceDemoWidget extends Component {

    /* ── Reactive animation targets ─────────────────────────────── */
    private final SimpleDoubleProperty redOpacity = new SimpleDoubleProperty(this, "red", 0.0);
    private final SimpleDoubleProperty greenOpacity = new SimpleDoubleProperty(this, "green", 0.0);
    private final SimpleDoubleProperty blueOpacity = new SimpleDoubleProperty(this, "blue", 0.0);

    /* ── @State fields for rendering ────────────────────────────── */
    @State
    private double currentRed = 0.0;

    @State
    private double currentGreen = 0.0;

    @State
    private double currentBlue = 0.0;

    @State
    private boolean sequential = true;

    @State
    private boolean playing = false;

    private Animation activeAnimation;
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

        redOpacity.addListener((obs, oldVal, newVal) -> {
            currentRed = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });

        greenOpacity.addListener((obs, oldVal, newVal) -> {
            currentGreen = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });

        blueOpacity.addListener((obs, oldVal, newVal) -> {
            currentBlue = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });
    }

    @Override
    public void onUnmount() {
        if (activeAnimation != null) {
            activeAnimation.stop();
        }
    }

    private void play() {
        ensureScheduler();

        if (activeAnimation != null) {
            activeAnimation.stop();
        }

        /* Reset all opacities to 0 */
        redOpacity.set(0.0);
        greenOpacity.set(0.0);
        blueOpacity.set(0.0);

        /* Create a fade-in for each dot */
        var fadeRed = new FadeTransition(Duration.millis(500), redOpacity);
        fadeRed.setFromValue(0.0);
        fadeRed.setToValue(1.0);
        fadeRed.setInterpolator(Interpolator.EASE_OUT);

        var fadeGreen = new FadeTransition(Duration.millis(500), greenOpacity);
        fadeGreen.setFromValue(0.0);
        fadeGreen.setToValue(1.0);
        fadeGreen.setInterpolator(Interpolator.EASE_OUT);

        var fadeBlue = new FadeTransition(Duration.millis(500), blueOpacity);
        fadeBlue.setFromValue(0.0);
        fadeBlue.setToValue(1.0);
        fadeBlue.setInterpolator(Interpolator.EASE_OUT);

        Animation composite;
        if (sequential) {
            composite = new SequentialTransition(fadeRed, fadeGreen, fadeBlue);
        } else {
            composite = new ParallelTransition(fadeRed, fadeGreen, fadeBlue);
        }

        composite.setOnFinished(() -> {
            playing = false;
            activeAnimation = null;
            ClientMain.getStateManager().notifyStateChange(this);
        });

        activeAnimation = composite;
        playing = true;
        composite.play();
    }

    @Override
    public Element render() {
        return div().children(

                /* ── Dot display ───────────────────────────────────────── */
                div().cls("flex", "justify-center", "items-center", "gap-8", "py-10", "mb-6")
                        .children(
                                dot("bg-rose-500", "shadow-rose-500/40", currentRed),
                                dot("bg-emerald-500", "shadow-emerald-500/40", currentGreen),
                                dot("bg-blue-500", "shadow-blue-500/40", currentBlue)
                        ),

                /* ── Mode toggle ───────────────────────────────────────── */
                div().cls("flex", "justify-center", "gap-2", "mb-6").children(
                        button().attr("type", "button")
                                .cls("px-4", "py-2", "rounded-lg", "font-medium", "text-sm",
                                        "transition-colors")
                                .cls(sequential
                                        ? "bg-violet-600 text-white"
                                        : "bg-gray-700 text-gray-400 hover:text-white")
                                .aria("label", "Switch to sequential animation mode")
                                .on("click", e -> {
                                    sequential = true;
                                })
                                .text("Sequential"),

                        button().attr("type", "button")
                                .cls("px-4", "py-2", "rounded-lg", "font-medium", "text-sm",
                                        "transition-colors")
                                .cls(!sequential
                                        ? "bg-violet-600 text-white"
                                        : "bg-gray-700 text-gray-400 hover:text-white")
                                .aria("label", "Switch to parallel animation mode")
                                .on("click", e -> {
                                    sequential = false;
                                })
                                .text("Parallel")
                ),

                /* ── Play button ───────────────────────────────────────── */
                div().cls("flex", "justify-center").children(
                        button().attr("type", "button")
                                .cls("px-6", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800")
                                .cls(playing
                                        ? "bg-gray-600 cursor-not-allowed"
                                        : "bg-emerald-600 hover:bg-emerald-500 focus:ring-emerald-500")
                                .aria("label", "Play " + (sequential ? "sequential" : "parallel") + " animation")
                                .on("click", e -> {
                                    if (!playing) play();
                                })
                                .text("Play")
                ),

                /* ── Status ────────────────────────────────────────────── */
                p().cls("mt-4", "text-center", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text(playing
                                ? "Playing " + (sequential ? "sequential" : "parallel") + "..."
                                : "Mode: " + (sequential ? "Sequential" : "Parallel"))
        );
    }

    private Element dot(String colorClass, String shadowClass, double opacityVal) {
        return div().cls("w-16", "h-16", "rounded-full", colorClass, "shadow-2xl")
                .style("opacity", String.valueOf(opacityVal))
                .ariaHidden(true);
    }
}
