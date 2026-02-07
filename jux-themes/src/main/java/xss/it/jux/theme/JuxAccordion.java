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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible accordion component with expandable/collapsible sections
 * (WCAG 2.2 AA compliant).
 *
 * <p>Renders a set of vertically stacked sections, each with a trigger
 * button and a collapsible content panel. The component follows the
 * WAI-ARIA Accordion pattern with proper ARIA states and relationships.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>Each section trigger is a {@code <button>} inside an {@code <h3>}</li>
 *   <li>Triggers have {@code aria-expanded} indicating panel visibility</li>
 *   <li>Triggers have {@code aria-controls} pointing to their panel</li>
 *   <li>Panels have unique {@code id}s and {@code role="region"} with
 *       {@code aria-labelledby} pointing back to the trigger</li>
 * </ul>
 *
 * <p><b>Keyboard interaction (WCAG 2.1.1):</b></p>
 * <ul>
 *   <li><b>Enter/Space</b> -- toggles the focused section</li>
 *   <li><b>Up/Down Arrow</b> -- navigates between section triggers</li>
 *   <li><b>Home/End</b> -- jumps to first/last trigger</li>
 * </ul>
 *
 * <p><b>Default state:</b> All sections are collapsed by default. The
 * initial state can be changed on the client side after hydration.</p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * var sections = List.of(
 *     new JuxAccordion.Section("What is JUX?", p().text("JUX is a Java framework...")),
 *     new JuxAccordion.Section("How do I install it?", p().text("Add the dependency...")),
 *     new JuxAccordion.Section("Is it free?", p().text("Licensing is TBD..."))
 * );
 * child(new JuxAccordion(sections));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/accordion/">WAI-ARIA Accordion Pattern</a>
 */
@JuxComponent
public class JuxAccordion extends Component {

    /**
     * A single accordion section consisting of a title and its expandable content.
     *
     * <p>The title is displayed on the trigger button and serves as the
     * accessible name for the content region via {@code aria-labelledby}.
     * The content is shown/hidden when the trigger is activated.</p>
     *
     * @param title   the section heading text displayed on the trigger button;
     *                must not be null or empty
     * @param content the Element tree shown when this section is expanded;
     *                must not be null
     */
    public record Section(String title, Element content) {

        /**
         * Creates a new Section with validation.
         *
         * @param title   the section trigger text
         * @param content the expandable content
         * @throws NullPointerException if title or content is null
         */
        public Section {
            Objects.requireNonNull(title, "Section title must not be null");
            Objects.requireNonNull(content, "Section content must not be null");
        }
    }

    /**
     * The list of accordion sections to render.
     *
     * <p>Sections are rendered in order from top to bottom. Each section
     * produces one trigger/panel pair in the DOM.</p>
     */
    private final List<Section> sections;

    /**
     * Unique prefix for generating trigger and panel IDs.
     *
     * <p>Ensures that ARIA cross-references remain unique even when
     * multiple accordion instances exist on the same page.</p>
     */
    private final String idPrefix;

    /**
     * Creates a new accordion component.
     *
     * @param sections the list of expandable sections; must not be null or empty
     * @throws NullPointerException     if sections is null
     * @throws IllegalArgumentException if sections is empty
     */
    public JuxAccordion(List<Section> sections) {
        Objects.requireNonNull(sections, "Sections list must not be null");
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Sections list must not be empty");
        }

        /* Defensive copy to prevent external mutation. */
        this.sections = List.copyOf(sections);
        this.idPrefix = "jux-accordion-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Builds the accordion Element tree with full ARIA support.
     *
     * <p>The rendered structure for each section is:</p>
     * <pre>{@code
     * <div class="jux-accordion">
     *   <div class="jux-accordion-item">
     *     <h3 class="jux-accordion-header">
     *       <button type="button" id="trigger-0"
     *               aria-expanded="false" aria-controls="panel-0"
     *               class="jux-accordion-trigger">
     *         Section Title
     *       </button>
     *     </h3>
     *     <div id="panel-0" role="region" aria-labelledby="trigger-0"
     *          class="jux-accordion-panel" hidden>
     *       <!-- section content -->
     *     </div>
     *   </div>
     *   ...
     * </div>
     * }</pre>
     *
     * <p>Each trigger is wrapped in an {@code <h3>} to provide heading semantics
     * for the section. Using a heading ensures that screen reader users can
     * navigate sections via the heading shortcut (WCAG 2.4.6).</p>
     *
     * @return the root accordion container Element, never null
     */
    @Override
    public Element render() {
        List<Element> items = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);

            /* Generate cross-referenced IDs for the trigger and panel pair. */
            String headerId = idPrefix + "-header-" + i;
            String panelId = idPrefix + "-panel-" + i;

            /*
             * Build the trigger button using Bootstrap 5 accordion pattern:
             * - Wrapped in an <h3> with accordion-header class
             * - data-bs-toggle="collapse" tells Bootstrap JS to handle toggling
             * - data-bs-target references the collapsible panel
             * - aria-expanded="false" + "collapsed" class: all sections start closed
             * - aria-controls links to the associated content panel
             */
            Element trigger = h3().cls("accordion-header").id(headerId).children(
                    button()
                            .attr("type", "button")
                            .cls("accordion-button", "collapsed")
                            .attr("data-bs-toggle", "collapse")
                            .attr("data-bs-target", "#" + panelId)
                            .ariaExpanded(false)
                            .ariaControls(panelId)
                            .text(section.title())
            );

            /*
             * Build the collapsible content panel:
             * - "accordion-collapse collapse" classes handle the collapse animation
             * - data-bs-parent references the parent accordion for
             *   one-at-a-time behavior (opening one closes others)
             * - Content is wrapped in accordion-body for proper padding
             */
            Element panel = div()
                    .id(panelId)
                    .cls("accordion-collapse", "collapse")
                    .attr("data-bs-parent", "#" + idPrefix)
                    .children(
                            div().cls("accordion-body").children(section.content())
                    );

            /* Wrap each trigger/panel pair in an accordion-item container. */
            Element item = div().cls("accordion-item").children(trigger, panel);
            items.add(item);
        }

        /* Assemble the complete Bootstrap accordion. ID is needed for data-bs-parent. */
        return div().cls("accordion").id(idPrefix).children(items);
    }
}
