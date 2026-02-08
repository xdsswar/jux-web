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
import xss.it.jux.animations.transition.RotateTransition;
import xss.it.jux.animations.transition.ScaleTransition;
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
 * Demonstrates {@link SlideTransition}, {@link ScaleTransition}, and
 * {@link RotateTransition} each targeting a different {@link SimpleDoubleProperty}.
 *
 * <p>A square element has three properties: translateX, scale, and rotation.
 * Three buttons trigger independent animations for each transform. Each
 * animation uses 2 cycles with auto-reverse for a bounce-back effect.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>SlideTransition</b> -- animates translateX from 0 to 200 and back</li>
 *   <li><b>ScaleTransition</b> -- animates scale from 1.0 to 1.5 and back</li>
 *   <li><b>RotateTransition</b> -- animates rotation from 0 to 360 degrees</li>
 *   <li><b>Auto-reverse</b> -- each transition plays 2 cycles with auto-reverse</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>2.1.1 Keyboard</b> -- all controls are native buttons.</li>
 *   <li><b>4.1.3 Status Messages</b> -- property readout in a polite live region.</li>
 * </ul>
 */
@JuxComponent(clientSide = true)
public class TransformDemoWidget extends Component {

    /* ── Reactive animation targets ─────────────────────────────── */
    private final SimpleDoubleProperty translateX = new SimpleDoubleProperty(this, "translateX", 0.0);
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(this, "scale", 1.0);
    private final SimpleDoubleProperty rotation = new SimpleDoubleProperty(this, "rotation", 0.0);

    /* ── @State fields for rendering ────────────────────────────── */
    @State
    private double currentX = 0.0;

    @State
    private double currentScale = 1.0;

    @State
    private double currentRotation = 0.0;

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

        translateX.addListener((obs, oldVal, newVal) -> {
            currentX = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });

        scale.addListener((obs, oldVal, newVal) -> {
            currentScale = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });

        rotation.addListener((obs, oldVal, newVal) -> {
            currentRotation = newVal.doubleValue();
            ClientMain.getStateManager().notifyStateChange(this);
        });
    }

    @Override
    public void onUnmount() {
        if (activeAnimation != null) {
            activeAnimation.stop();
        }
    }

    private void slide() {
        ensureScheduler();
        stopActive();
        var anim = new SlideTransition(Duration.millis(800), translateX, null);
        anim.setFromX(0.0);
        anim.setToX(150.0);
        anim.setInterpolator(Interpolator.EASE_BOTH);
        anim.setCycleCount(2);
        anim.setAutoReverse(true);
        anim.setOnFinished(() -> {
            activeAnimation = null;
            ClientMain.getStateManager().notifyStateChange(this);
        });
        activeAnimation = anim;
        anim.play();
    }

    private void scaleUp() {
        ensureScheduler();
        stopActive();
        var anim = new ScaleTransition(Duration.millis(600), scale);
        anim.setFromValue(1.0);
        anim.setToValue(1.5);
        anim.setInterpolator(Interpolator.EASE_BOTH);
        anim.setCycleCount(2);
        anim.setAutoReverse(true);
        anim.setOnFinished(() -> {
            activeAnimation = null;
            ClientMain.getStateManager().notifyStateChange(this);
        });
        activeAnimation = anim;
        anim.play();
    }

    private void spin() {
        ensureScheduler();
        stopActive();
        var anim = new RotateTransition(Duration.seconds(1), rotation);
        anim.setFromValue(0.0);
        anim.setToValue(360.0);
        anim.setInterpolator(Interpolator.EASE_BOTH);
        anim.setCycleCount(1);
        anim.setOnFinished(() -> {
            rotation.set(0.0);
            activeAnimation = null;
            ClientMain.getStateManager().notifyStateChange(this);
        });
        activeAnimation = anim;
        anim.play();
    }

    private void stopActive() {
        if (activeAnimation != null) {
            activeAnimation.stop();
            activeAnimation = null;
        }
    }

    @Override
    public Element render() {
        /* Build the CSS transform string from current property values */
        String transform = "translateX(" + (int) currentX + "px)"
                + " scale(" + String.format("%.2f", currentScale) + ")"
                + " rotate(" + (int) currentRotation + "deg)";

        return div().children(

                /* ── Animated element ──────────────────────────────────── */
                div().cls("flex", "justify-center", "py-8", "mb-6").children(
                        div().cls("w-20", "h-20", "rounded-xl",
                                        "bg-gradient-to-br", "from-violet-600", "to-indigo-700",
                                        "shadow-2xl")
                                .style("transform", transform)
                                .ariaHidden(true)
                ),

                /* ── Property readout ──────────────────────────────────── */
                div().cls("text-center", "mb-6").children(
                        p().cls("text-xs", "font-mono", "text-gray-500")
                                .ariaLive("polite")
                                .text("X: " + (int) currentX + "px"
                                        + "  |  Scale: " + String.format("%.2f", currentScale) + "x"
                                        + "  |  Rot: " + (int) currentRotation + "\u00b0")
                ),

                /* ── Control buttons ───────────────────────────────────── */
                div().cls("flex", "flex-wrap", "justify-center", "gap-3").children(
                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800",
                                        "bg-violet-600", "hover:bg-violet-500", "focus:ring-violet-500")
                                .aria("label", "Slide element horizontally")
                                .on("click", e -> slide())
                                .text("Slide"),

                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800",
                                        "bg-emerald-600", "hover:bg-emerald-500", "focus:ring-emerald-500")
                                .aria("label", "Scale element up and back")
                                .on("click", e -> scaleUp())
                                .text("Scale"),

                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium", "text-white",
                                        "transition-colors", "focus:ring-2", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800",
                                        "bg-amber-600", "hover:bg-amber-500", "focus:ring-amber-500")
                                .aria("label", "Spin element 360 degrees")
                                .on("click", e -> spin())
                                .text("Rotate")
                )
        );
    }
}
