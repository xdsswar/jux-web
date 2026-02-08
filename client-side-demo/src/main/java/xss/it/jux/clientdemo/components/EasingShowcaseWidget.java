/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.animations.transition.ParallelTransition;
import xss.it.jux.animations.transition.SlideTransition;
import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.clientdemo.client.BrowserFrameScheduler;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static xss.it.jux.core.Elements.*;

/**
 * Showcases all 8 built-in {@link Interpolator} instances side by side.
 *
 * <p>Each interpolator drives a small dot sliding from left to right on a
 * horizontal track. A single "Play All" button triggers all 8 animations
 * simultaneously via a {@link ParallelTransition}.</p>
 *
 * <h2>Interpolators Shown</h2>
 * <ol>
 *   <li>{@link Interpolator#LINEAR}</li>
 *   <li>{@link Interpolator#EASE_IN}</li>
 *   <li>{@link Interpolator#EASE_OUT}</li>
 *   <li>{@link Interpolator#EASE_BOTH}</li>
 *   <li>{@link Interpolator#OVERSHOOT}</li>
 *   <li>{@link Interpolator#BOUNCE}</li>
 *   <li>{@link Interpolator#ELASTIC}</li>
 *   <li>{@link Interpolator#DISCRETE}</li>
 * </ol>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- Play All button is a native {@code <button>}.</li>
 *   <li><b>4.1.3 Status Messages</b> -- polite live region announces state.</li>
 * </ul>
 */
@JuxComponent(clientSide = true)
public class EasingShowcaseWidget extends Component {

    private static final String[] NAMES = {
            "LINEAR", "EASE_IN", "EASE_OUT", "EASE_BOTH",
            "OVERSHOOT", "BOUNCE", "ELASTIC", "DISCRETE"
    };

    private static final Interpolator[] CURVES = {
            Interpolator.LINEAR, Interpolator.EASE_IN,
            Interpolator.EASE_OUT, Interpolator.EASE_BOTH,
            Interpolator.OVERSHOOT, Interpolator.BOUNCE,
            Interpolator.ELASTIC, Interpolator.DISCRETE
    };

    /* Each dot has a translateX property (0 = left, 100 = right as %). */
    private final SimpleDoubleProperty[] positions = new SimpleDoubleProperty[8];

    @State
    private double[] currentPositions = new double[8];

    @State
    private boolean playing = false;

    private ParallelTransition parallelTransition;
    private boolean schedulerInitialised;

    public EasingShowcaseWidget() {
        for (int i = 0; i < 8; i++) {
            positions[i] = new SimpleDoubleProperty(this, "pos" + i, 0.0);
        }
    }

    private void ensureScheduler() {
        if (!schedulerInitialised) {
            FrameScheduler.setDefault(new BrowserFrameScheduler());
            schedulerInitialised = true;
        }
    }

    @Override
    public void onMount() {
        ensureScheduler();

        for (int i = 0; i < 8; i++) {
            final int idx = i;
            positions[i].addListener((obs, oldVal, newVal) -> {
                currentPositions[idx] = newVal.doubleValue();
                ClientMain.getStateManager().notifyStateChange(this);
            });
        }
    }

    @Override
    public void onUnmount() {
        if (parallelTransition != null) {
            parallelTransition.stop();
        }
    }

    private void playAll() {
        ensureScheduler();

        if (parallelTransition != null) {
            parallelTransition.stop();
        }

        /* Reset positions to 0 */
        for (int i = 0; i < 8; i++) {
            positions[i].set(0.0);
            currentPositions[i] = 0.0;
        }

        /* Create one SlideTransition per interpolator */
        SlideTransition[] slides = new SlideTransition[8];
        for (int i = 0; i < 8; i++) {
            slides[i] = new SlideTransition(Duration.seconds(1.5), positions[i], null);
            slides[i].setFromX(0.0);
            slides[i].setToX(100.0);
            slides[i].setInterpolator(CURVES[i]);
        }

        parallelTransition = new ParallelTransition(slides);
        parallelTransition.setOnFinished(() -> {
            playing = false;
            ClientMain.getStateManager().notifyStateChange(this);
        });

        playing = true;
        parallelTransition.play();
    }

    @Override
    public Element render() {
        var rows = div().cls("space-y-3", "mb-6");

        for (int i = 0; i < 8; i++) {
            rows = rows.children(easingRow(i));
        }

        return div().children(
                rows,

                /* Play All button */
                div().cls("flex", "items-center", "gap-4").children(
                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800")
                                .cls(playing
                                        ? "bg-gray-600 cursor-not-allowed"
                                        : "bg-emerald-600 hover:bg-emerald-500 focus:ring-emerald-500")
                                .aria("label", "Play all easing animations")
                                .on("click", e -> {
                                    if (!playing) playAll();
                                })
                                .text("Play All")
                ),

                p().cls("mt-3", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text(playing ? "Animating..." : "Ready")
        );
    }

    private Element easingRow(int idx) {
        double pos = currentPositions[idx];
        /* Position is 0..100 representing percentage across the track.
         * We clamp to 0..100 for display, though overshoot/elastic may
         * temporarily exceed this range. */
        double clampedPos = Math.max(0.0, Math.min(100.0, pos));

        return div().cls("flex", "items-center", "gap-3").children(
                /* Label */
                span().cls("text-xs", "font-mono", "text-gray-400", "w-24", "text-right")
                        .text(NAMES[idx]),

                /* Track */
                div().cls("flex-1", "h-6", "bg-gray-700/50", "rounded-full", "relative",
                                "overflow-hidden")
                        .children(
                                /* Dot */
                                div().cls("absolute", "top-0.5", "w-5", "h-5", "rounded-full",
                                                "bg-violet-500", "shadow-2xl")
                                        .style("left", "calc(" + clampedPos + "% - 10px)")
                                        .ariaHidden(true)
                        )
        );
    }
}
