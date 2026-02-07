/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.core;

/**
 * DOM event wrapper carrying event data from the client to JUX event handlers.
 *
 * <p>This class encapsulates the properties of a browser DOM event in a
 * platform-independent form. On the client side, the TeaVM hydration layer
 * ({@code JuxDomBridge}) translates native {@code org.teavm.jso.dom.events.Event}
 * objects into {@code DomEvent} instances before dispatching them to
 * {@link EventHandler} callbacks.</p>
 *
 * <p>The class carries common event properties applicable across event types:</p>
 * <ul>
 *   <li><b>type</b> -- the DOM event name (e.g. "click", "input", "keydown")</li>
 *   <li><b>targetId</b> -- the {@code id} attribute of the element that fired the event</li>
 *   <li><b>value</b> -- the current value of the target element (useful for form inputs)</li>
 *   <li><b>key</b> -- the keyboard key for key events (e.g. "Enter", "Escape", "a")</li>
 *   <li><b>modifier keys</b> -- Shift, Ctrl, Alt, and Meta (Cmd on macOS) states</li>
 *   <li><b>mouse coordinates</b> -- clientX/clientY for mouse and pointer events</li>
 * </ul>
 *
 * <p>Event propagation can be controlled via {@link #preventDefault()} and
 * {@link #stopPropagation()}, mirroring the standard DOM event API.</p>
 *
 * @see EventHandler
 * @see Element#on(String, EventHandler)
 */
public class DomEvent {

    /** The DOM event type name (e.g. "click", "input", "keydown", "submit"). */
    private final String type;

    /** The {@code id} attribute of the DOM element that dispatched this event. May be empty. */
    private final String targetId;

    /**
     * The current value of the event target element. For {@code <input>} and
     * {@code <textarea>} elements, this is the text content. For {@code <select>},
     * this is the selected option value. May be empty for non-form elements.
     */
    private final String value;

    /**
     * The keyboard key identifier for key events. Uses the standard
     * {@code KeyboardEvent.key} values (e.g. "Enter", "Escape", "ArrowDown", "a").
     * Empty string for non-keyboard events.
     */
    private final String key;

    /** Whether the Shift key was held when the event fired. */
    private final boolean shiftKey;

    /** Whether the Ctrl key (Control on macOS) was held when the event fired. */
    private final boolean ctrlKey;

    /** Whether the Alt key (Option on macOS) was held when the event fired. */
    private final boolean altKey;

    /** Whether the Meta key (Cmd on macOS, Windows key on Windows) was held when the event fired. */
    private final boolean metaKey;

    /** The horizontal mouse/pointer coordinate relative to the browser viewport. */
    private final double clientX;

    /** The vertical mouse/pointer coordinate relative to the browser viewport. */
    private final double clientY;

    /**
     * Flag indicating whether {@link #preventDefault()} has been called.
     * When {@code true}, the client-side bridge calls {@code event.preventDefault()}
     * on the native DOM event to suppress default browser behavior (e.g. form
     * submission, link navigation).
     */
    private boolean defaultPrevented;

    /**
     * Flag indicating whether {@link #stopPropagation()} has been called.
     * When {@code true}, the client-side bridge calls {@code event.stopPropagation()}
     * on the native DOM event to prevent it from bubbling up the DOM tree.
     */
    private boolean propagationStopped;

    /**
     * Constructs a fully specified DOM event with all properties.
     *
     * <p>Typically not called directly by application code. The client-side
     * hydration bridge creates instances from native browser events. For
     * testing, use {@link #simple(String, String)} as a convenience factory.</p>
     *
     * @param type     the DOM event type name (e.g. "click", "input")
     * @param targetId the id attribute of the target element, or empty string
     * @param value    the current value of the target element, or empty string
     * @param key      the keyboard key for key events, or empty string
     * @param shiftKey whether Shift was held
     * @param ctrlKey  whether Ctrl was held
     * @param altKey   whether Alt was held
     * @param metaKey  whether Meta/Cmd was held
     * @param clientX  horizontal mouse coordinate relative to viewport
     * @param clientY  vertical mouse coordinate relative to viewport
     */
    public DomEvent(String type, String targetId, String value, String key,
                    boolean shiftKey, boolean ctrlKey, boolean altKey, boolean metaKey,
                    double clientX, double clientY) {
        this.type = type;
        this.targetId = targetId;
        this.value = value;
        this.key = key;
        this.shiftKey = shiftKey;
        this.ctrlKey = ctrlKey;
        this.altKey = altKey;
        this.metaKey = metaKey;
        this.clientX = clientX;
        this.clientY = clientY;
    }

    /**
     * Create a simple event with only a type and value, with all other
     * properties set to their defaults (no modifiers, no coordinates).
     *
     * <p>Useful for testing and for programmatically simulating input events:</p>
     * <pre>{@code
     * var event = DomEvent.simple("input", "search query");
     * handler.handle(event);
     * }</pre>
     *
     * @param type  the DOM event type name (e.g. "input", "click")
     * @param value the target element value (e.g. user-entered text)
     * @return a new DomEvent with default modifier keys and zero coordinates
     */
    public static DomEvent simple(String type, String value) {
        return new DomEvent(type, "", value, "", false, false, false, false, 0, 0);
    }

    /**
     * Returns the DOM event type name.
     *
     * @return the event type (e.g. "click", "input", "keydown"), never null
     */
    public String getType() { return type; }

    /**
     * Returns the {@code id} attribute of the element that dispatched this event.
     *
     * @return the target element's id, or empty string if the element has no id
     */
    public String getTargetId() { return targetId; }

    /**
     * Returns the current value of the event target element.
     *
     * <p>For form inputs ({@code <input>}, {@code <textarea>}, {@code <select>}),
     * this returns the element's current value at the time the event fired.
     * For non-form elements, this is typically an empty string.</p>
     *
     * @return the target element value, or empty string
     */
    public String getValue() { return value; }

    /**
     * Returns the keyboard key identifier for key events.
     *
     * <p>Uses the standard {@code KeyboardEvent.key} naming convention:
     * "Enter", "Escape", "ArrowUp", "Tab", "a", "A", " " (space), etc.</p>
     *
     * @return the key name, or empty string for non-keyboard events
     */
    public String getKey() { return key; }

    /**
     * Returns whether the Shift modifier key was active when the event fired.
     *
     * @return {@code true} if Shift was held down
     */
    public boolean isShiftKey() { return shiftKey; }

    /**
     * Returns whether the Ctrl modifier key was active when the event fired.
     *
     * @return {@code true} if Ctrl (Control on macOS) was held down
     */
    public boolean isCtrlKey() { return ctrlKey; }

    /**
     * Returns whether the Alt modifier key was active when the event fired.
     *
     * @return {@code true} if Alt (Option on macOS) was held down
     */
    public boolean isAltKey() { return altKey; }

    /**
     * Returns whether the Meta modifier key was active when the event fired.
     *
     * @return {@code true} if Meta (Cmd on macOS, Windows key) was held down
     */
    public boolean isMetaKey() { return metaKey; }

    /**
     * Returns the horizontal mouse/pointer coordinate relative to the browser viewport.
     *
     * @return the X coordinate in CSS pixels, or 0.0 for non-mouse events
     */
    public double getClientX() { return clientX; }

    /**
     * Returns the vertical mouse/pointer coordinate relative to the browser viewport.
     *
     * @return the Y coordinate in CSS pixels, or 0.0 for non-mouse events
     */
    public double getClientY() { return clientY; }

    /**
     * Suppress the browser's default action for this event.
     *
     * <p>Common uses: preventing form submission on "submit" events,
     * preventing navigation on link "click" events, or preventing
     * character insertion on "keydown" events.</p>
     *
     * <p>The client-side bridge reads this flag after the handler returns
     * and calls {@code event.preventDefault()} on the native DOM event
     * if it is set.</p>
     */
    public void preventDefault() { this.defaultPrevented = true; }

    /**
     * Returns whether {@link #preventDefault()} has been called on this event.
     *
     * @return {@code true} if the default action should be suppressed
     */
    public boolean isDefaultPrevented() { return defaultPrevented; }

    /**
     * Stop the event from propagating (bubbling) up the DOM tree.
     *
     * <p>Prevents parent elements from receiving this event. Useful when
     * a child handler fully processes the event and parent handlers should
     * not react to it.</p>
     *
     * <p>The client-side bridge reads this flag after the handler returns
     * and calls {@code event.stopPropagation()} on the native DOM event
     * if it is set.</p>
     */
    public void stopPropagation() { this.propagationStopped = true; }

    /**
     * Returns whether {@link #stopPropagation()} has been called on this event.
     *
     * @return {@code true} if event propagation should be stopped
     */
    public boolean isPropagationStopped() { return propagationStopped; }
}
