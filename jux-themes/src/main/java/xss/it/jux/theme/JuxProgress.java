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

package xss.it.jux.theme;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.Objects;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible progress bar component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a visual progress indicator with proper ARIA attributes so
 * screen readers can announce the current progress value, range, and label.
 * The component uses a custom-styled div rather than the native
 * {@code <progress>} element to provide full visual control while maintaining
 * equivalent accessibility through explicit ARIA roles and states.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>{@code role="progressbar"} -- identifies the element as a progress indicator</li>
 *   <li>{@code aria-valuenow} -- the current progress value (0-100)</li>
 *   <li>{@code aria-valuemin="0"} -- the minimum value in the range</li>
 *   <li>{@code aria-valuemax="100"} -- the maximum value in the range</li>
 *   <li>{@code aria-label} -- a descriptive label announced by screen readers
 *       (e.g. "Upload progress", "Loading profile")</li>
 * </ul>
 *
 * <p><b>Visual design:</b></p>
 * <p>The progress bar consists of an outer track element (the full-width
 * background) and an inner fill element whose width is set as a percentage
 * of the current value. The inner fill uses the theme's primary color by default.</p>
 *
 * <p><b>Accessibility notes:</b></p>
 * <ul>
 *   <li>The label is required (not optional) because a progress bar without
 *       context is meaningless to screen reader users</li>
 *   <li>The numeric value is accessible via ARIA attributes even if it is
 *       not visually displayed, ensuring screen readers can announce "Upload
 *       progress: 75%"</li>
 *   <li>Color is not the sole indicator of progress -- the percentage width
 *       and the numeric aria-valuenow both convey the information</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * child(new JuxProgress(75, "Upload progress"));
 * child(new JuxProgress(30, "Profile completion"));
 * child(new JuxProgress(100, "Download complete"));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/meter/">WAI-ARIA Meter/Progressbar</a>
 */
@JuxComponent
public class JuxProgress extends Component {

    /**
     * The current progress value as a percentage (0 to 100).
     *
     * <p>This value determines both the visual fill width and the
     * {@code aria-valuenow} attribute announced by screen readers.
     * Values outside the 0-100 range are clamped during rendering.</p>
     */
    private final int value;

    /**
     * The accessible label describing what this progress bar represents.
     *
     * <p>Screen readers announce this label along with the current value,
     * producing announcements like "Upload progress: 75%". The label
     * should be concise but descriptive enough to identify the operation
     * being tracked when multiple progress bars may be present on a page.</p>
     */
    private final String label;

    /**
     * Creates a new progress bar component.
     *
     * @param value the current progress percentage (0-100); values outside this
     *              range are clamped to the nearest bound during rendering
     * @param label a descriptive label for screen readers (e.g. "Upload progress",
     *              "Loading data"); must not be null
     * @throws NullPointerException if label is null
     */
    public JuxProgress(int value, String label) {
        this.value = value;
        this.label = Objects.requireNonNull(label, "Progress label must not be null");
    }

    /**
     * Builds the progress bar Element tree with ARIA progressbar role.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <div role="progressbar"
     *      aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"
     *      aria-label="Upload progress"
     *      class="jux-progress">
     *   <div class="jux-progress-fill" style="width: 75%"></div>
     * </div>
     * }</pre>
     *
     * <p>The outer div acts as the progress track and carries all ARIA
     * attributes. The inner div is the visual fill indicator whose width
     * is set as a percentage. The inner div has {@code aria-hidden="true"}
     * because it is purely decorative -- the progress information is
     * communicated through the outer element's ARIA attributes.</p>
     *
     * @return the progress bar container Element, never null
     */
    @Override
    public Element render() {
        /*
         * Clamp the value to the valid 0-100 range. This prevents
         * invalid CSS percentage values and invalid aria-valuenow values.
         */
        int clampedValue = Math.max(0, Math.min(100, value));

        /*
         * Build the inner fill element. Its width represents the progress
         * visually. aria-hidden="true" prevents screen readers from
         * trying to interpret this decorative element separately.
         */
        Element fill = div()
                .cls("jux-progress-fill")
                .ariaHidden(true)
                .style("width", clampedValue + "%");

        /*
         * Build the outer progress container with all ARIA attributes:
         * - role="progressbar" identifies this as a progress indicator
         * - aria-valuenow is the current progress (clamped to range)
         * - aria-valuemin/max define the range boundaries
         * - aria-label provides the accessible name
         *
         * Screen readers combine these to announce something like:
         * "Upload progress, progress bar, 75%"
         */
        return div()
                .role("progressbar")
                .aria("valuenow", String.valueOf(clampedValue))
                .aria("valuemin", "0")
                .aria("valuemax", "100")
                .aria("label", label)
                .cls("jux-progress")
                .children(fill);
    }
}
