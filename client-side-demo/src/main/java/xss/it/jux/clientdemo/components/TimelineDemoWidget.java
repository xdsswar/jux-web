/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import java.util.List;

import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.animations.KeyFrame;
import xss.it.jux.animations.KeyValue;
import xss.it.jux.animations.Timeline;
import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.clientdemo.client.BrowserFrameScheduler;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static xss.it.jux.core.Elements.*;

/**
 * Demonstrates the {@link Timeline} API with multiple {@link KeyFrame}s
 * driving opacity and translateX simultaneously.
 *
 * <p>A card element moves right while fading out, then moves back while
 * fading in. The timeline has three keyframes:</p>
 * <ol>
 *   <li>0 ms -- opacity = 1.0, translateX = 0</li>
 *   <li>500 ms -- opacity = 0.0, translateX = 150</li>
 *   <li>1000 ms -- opacity = 1.0, translateX = 0</li>
 * </ol>
 *
 * <p>A simple progress bar shows the timeline's current position.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>Timeline</b> -- multi-property, multi-keyframe animation</li>
 *   <li><b>KeyFrame / KeyValue</b> -- associating properties with values at specific times</li>
 *   <li><b>Mixed interpolators</b> -- each KeyValue can use a different Interpolator</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- native button controls.</li>
 *   <li><b>4.1.3 Status Messages</b> -- polite live region for timeline status.</li>
 * </ul>
 */
@JuxComponent(clientSide = true)
public class TimelineDemoWidget extends Component {

    /* ── Reactive animation targets ─────────────────────────────── */
    private final SimpleDoubleProperty opacity = new SimpleDoubleProperty(this, "opacity", 1.0);
    private final SimpleDoubleProperty translateX = new SimpleDoubleProperty(this, "translateX", 0.0);

    /* ── @State fields for rendering ────────────────────────────── */
    @State
    private double currentOpacity = 1.0;

    @State
    private double currentX = 0.0;

    @State
    private boolean playing = false;

    @State
    private double progress = 0.0;

    private Timeline timeline;
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

        opacity.addListener((obs, oldVal, newVal) -> {
            currentOpacity = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });

        translateX.addListener((obs, oldVal, newVal) -> {
            currentX = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });
    }

    @Override
    public void onUnmount() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void play() {
        ensureScheduler();

        if (timeline != null) {
            timeline.stop();
        }

        /* Reset properties */
        opacity.set(1.0);
        translateX.set(0.0);

        /* Build timeline with 3 keyframes */
        timeline = new Timeline(
                /* KF at 0ms: starting state */
                new KeyFrame(Duration.ZERO, List.of(
                        new KeyValue<>(opacity, 1.0, Interpolator.EASE_BOTH),
                        new KeyValue<>(translateX, 0.0, Interpolator.EASE_BOTH)
                )),
                /* KF at 500ms: faded out + moved right */
                new KeyFrame(Duration.millis(500), List.of(
                        new KeyValue<>(opacity, 0.0, Interpolator.EASE_IN),
                        new KeyValue<>(translateX, 150.0, Interpolator.EASE_OUT)
                )),
                /* KF at 1000ms: back to original */
                new KeyFrame(Duration.millis(1000), List.of(
                        new KeyValue<>(opacity, 1.0, Interpolator.EASE_OUT),
                        new KeyValue<>(translateX, 0.0, Interpolator.EASE_IN)
                ))
        );

        /* Track progress via currentTime property */
        timeline.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            double totalMs = 1000.0;
            progress = Math.min(1.0, newVal.doubleValue() / totalMs);
            ClientMain.getStateManager().notifyStateChange(this);
        });

        timeline.setOnFinished(() -> {
            playing = false;
            progress = 0.0;
            ClientMain.getStateManager().notifyStateChange(this);
        });

        playing = true;
        timeline.play();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        playing = false;
        progress = 0.0;
        opacity.set(1.0);
        translateX.set(0.0);
    }

    @Override
    public Element render() {
        String transform = "translateX(" + (int) currentX + "px)";
        int progressPct = (int) Math.round(progress * 100);

        return div().children(

                /* ── Animated card ─────────────────────────────────────── */
                div().cls("flex", "justify-center", "py-8", "mb-4").children(
                        div().cls("px-8", "py-6", "rounded-xl",
                                        "bg-gradient-to-r", "from-violet-600", "to-indigo-700",
                                        "shadow-2xl")
                                .style("opacity", String.valueOf(currentOpacity))
                                .style("transform", transform)
                                .children(
                                        p().cls("text-white", "font-semibold").text("Timeline Card")
                                )
                ),

                /* ── Progress bar ──────────────────────────────────────── */
                div().cls("mb-6").children(
                        div().cls("flex", "justify-between", "text-xs", "text-gray-500", "mb-1")
                                .children(
                                        span().text("0ms"),
                                        span().text("500ms"),
                                        span().text("1000ms")
                                ),
                        div().cls("h-2", "bg-gray-700", "rounded-full", "overflow-hidden")
                                .attr("role", "progressbar")
                                .aria("valuenow", String.valueOf(progressPct))
                                .aria("valuemin", "0")
                                .aria("valuemax", "100")
                                .aria("label", "Timeline progress")
                                .children(
                                        div().cls("h-full", "bg-violet-600", "rounded-full",
                                                        "transition-all")
                                                .style("width", progressPct + "%")
                                )
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
                                .aria("label", "Play timeline animation")
                                .on("click", e -> {
                                    if (!playing) play();
                                })
                                .text("Play"),

                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium",
                                        "transition-colors", "focus:ring-2", "focus:ring-gray-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800",
                                        "bg-gray-700", "text-gray-200", "hover:bg-gray-600")
                                .aria("label", "Stop and reset timeline")
                                .on("click", e -> stopTimeline())
                                .text("Stop")
                ),

                /* ── Status ────────────────────────────────────────────── */
                p().cls("mt-4", "text-sm", "text-gray-500")
                        .ariaLive("polite")
                        .text(playing ? "Playing... " + progressPct + "%" : "Ready")
        );
    }
}
