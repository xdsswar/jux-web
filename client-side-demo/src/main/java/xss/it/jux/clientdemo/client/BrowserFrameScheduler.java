/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.client;

import org.teavm.jso.browser.Window;
import xss.it.jux.animations.FrameScheduler;

/**
 * Browser-based {@link FrameScheduler} implementation that delegates to
 * {@code requestAnimationFrame} via TeaVM's {@link Window} JSO binding.
 *
 * <p>This scheduler drives the JUX animation system at the browser's native
 * refresh rate (typically 60 fps). Each call to {@link #requestFrame(FrameCallback)}
 * schedules a single {@code requestAnimationFrame} callback; the animation's
 * {@code tick()} method is responsible for requesting the next frame if it
 * needs to continue.</p>
 *
 * <p><b>Usage:</b> Call {@code FrameScheduler.setDefault(new BrowserFrameScheduler())}
 * once during client-side initialisation (e.g. in {@code onMount()}) before
 * creating any animations.</p>
 *
 * @see FrameScheduler
 */
public final class BrowserFrameScheduler implements FrameScheduler {

    /** The ID returned by the most recent {@code requestAnimationFrame} call, or 0. */
    private int frameId;

    @Override
    public void requestFrame(FrameCallback callback) {
        frameId = Window.requestAnimationFrame(timestamp -> callback.onFrame(timestamp));
    }

    @Override
    public void cancelFrame() {
        if (frameId != 0) {
            Window.cancelAnimationFrame(frameId);
            frameId = 0;
        }
    }
}
