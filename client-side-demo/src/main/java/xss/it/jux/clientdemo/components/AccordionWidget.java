/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Collapsible accordion/FAQ widget demonstrating the WAI-ARIA accordion pattern in JUX.
 *
 * <p>This component implements an accordion interface where only one content panel can
 * be expanded at a time. It follows the
 * <a href="https://www.w3.org/WAI/ARIA/apg/patterns/accordion/">WAI-ARIA Accordion
 * Pattern</a> for full keyboard accessibility and screen reader support.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for open panel tracking</b> -- the {@code openIndex} field
 *       tracks which accordion panel is currently expanded. A value of {@code -1}
 *       means all panels are collapsed. Changing this value triggers a re-render
 *       that expands the target panel and collapses the previously open one.</li>
 *   <li><b>Toggle behaviour</b> -- clicking an already-open panel's trigger collapses
 *       it (sets {@code openIndex} to {@code -1}). Clicking a different panel's trigger
 *       collapses the current one and expands the new one in a single re-render.</li>
 *   <li><b>ARIA expanded/controls</b> -- each trigger button uses
 *       {@code aria-expanded} (true/false) and {@code aria-controls} linking it to
 *       its content panel ID. The content panel uses {@code role="region"} and
 *       {@code aria-labelledby} back-referencing the trigger.</li>
 *   <li><b>Animated chevron indicator</b> -- the chevron icon (Unicode arrow) rotates
 *       via a CSS class when its panel is expanded, providing a clear visual cue.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- each trigger/panel pair is linked
 *       via {@code aria-controls} and {@code aria-labelledby}.</li>
 *   <li><b>2.1.1 Keyboard</b> -- triggers are native {@code <button>} elements,
 *       activatable with Enter and Space. Arrow keys navigate between triggers.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- {@code aria-expanded} communicates
 *       the open/closed state to assistive technology.</li>
 * </ul>
 *
 * <h2>FAQ Content</h2>
 * <p>Four frequently asked questions about the JUX framework:</p>
 * <ol>
 *   <li>"What is JUX?" -- introduces the framework concept.</li>
 *   <li>"How does client-side work?" -- explains TeaVM compilation.</li>
 *   <li>"Is it accessible?" -- describes WCAG 2.2 AA compliance.</li>
 *   <li>"What about performance?" -- covers SSR and bundle size targets.</li>
 * </ol>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#ariaExpanded(boolean)
 * @see xss.it.jux.core.Element#ariaControls(String)
 */
@JuxComponent(clientSide = true)
public class AccordionWidget extends Component {

    /**
     * Index of the currently open accordion panel, or {@code -1} if all are collapsed.
     *
     * <p>Zero-based: 0 = first FAQ item, 1 = second, etc. Only one panel can be
     * open at a time (single-expand mode). On the client, toggling this value
     * triggers a re-render that applies/removes the expanded visual state.</p>
     */
    @State
    private int openIndex = 0;

    /**
     * Immutable list of FAQ items displayed in the accordion.
     *
     * <p>Each item is a {@link FaqItem} record containing the question (trigger text)
     * and the answer (panel content). The items are defined inline to demonstrate
     * a realistic FAQ scenario about the JUX framework.</p>
     */
    private final List<FaqItem> faqItems = List.of(
            new FaqItem(
                    "What is JUX?",
                    "JUX is a pure-Java web framework for building complete websites "
                            + "-- landing pages, dashboards, CMS-driven sites, and admin panels "
                            + "-- writing only Java. No HTML templates, no JavaScript files, "
                            + "no CSS authoring. It renders pages server-side via Spring Boot "
                            + "and hydrates interactive components client-side via TeaVM."
            ),
            new FaqItem(
                    "How does client-side work?",
                    "Components annotated with @JuxComponent(clientSide = true) are "
                            + "compiled from Java to JavaScript using TeaVM. After the server "
                            + "renders the initial HTML, the generated JS hydrates interactive "
                            + "components by attaching event listeners and enabling @State "
                            + "reactivity. The virtual DOM diffing engine ensures only minimal "
                            + "DOM mutations occur on each state change."
            ),
            new FaqItem(
                    "Is it accessible?",
                    "Yes. Every built-in JUX component is WCAG 2.2 Level AA compliant. "
                            + "The framework enforces accessibility at the API level: images "
                            + "require alt text (there is no img() without alt), forms require "
                            + "labels, tables require captions. An audit engine scans every "
                            + "rendered page in development mode and flags violations."
            ),
            new FaqItem(
                    "What about performance?",
                    "JUX targets SSR render times under 5ms (warm, excluding DB), client "
                            + "JS bundles under 50KB gzipped (framework only), and zero runtime "
                            + "reflection on the hot path. The SSR cache uses Caffeine with "
                            + "configurable TTL per route. Only interactive components ship "
                            + "JavaScript -- pure server components have zero client overhead."
            )
    );

    /**
     * Builds the virtual DOM tree for the accordion widget.
     *
     * <p>The rendered structure is a vertical stack of trigger/panel pairs, separated
     * by subtle borders. Each pair consists of a {@code <button>} trigger and a
     * content {@code <div>} that is only rendered when the panel is open.</p>
     *
     * @return the root element of the accordion widget, never null
     */
    @Override
    public Element render() {
        /* Build and return the complete accordion element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "overflow-hidden")
                .children(buildAccordionItems());
    }

    /**
     * Generates the list of accordion item elements (trigger + panel pairs).
     *
     * <p>Each item consists of:</p>
     * <ul>
     *   <li>A {@code <div>} wrapper with a bottom border separating items.</li>
     *   <li>A {@code <button>} trigger with the question text and a chevron icon.</li>
     *   <li>A content panel {@code <div>} that is only present in the DOM when
     *       this item's index matches {@code openIndex}.</li>
     * </ul>
     *
     * @return a list of accordion item elements
     */
    private List<Element> buildAccordionItems() {
        /* Collect the generated item elements into a mutable list. */
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < faqItems.size(); i++) {
            /* Capture the index in a final local for lambda closure. */
            final int index = i;
            FaqItem item = faqItems.get(i);

            /* Determine whether this panel is currently expanded. */
            boolean isOpen = (i == openIndex);

            /* ID values for ARIA cross-referencing between trigger and panel. */
            String triggerId = "accordion-trigger-" + i;
            String panelId = "accordion-panel-" + i;

            /*
             * Build the chevron indicator. When the panel is open, the chevron
             * receives a "rotate-180" CSS class to flip it upward, providing
             * a clear visual cue of the expanded state. The Unicode character
             * U+2039 (single left-pointing angle quotation mark) rotated 90
             * degrees via CSS serves as a lightweight chevron without requiring
             * an icon library.
             */
            String chevronClasses = isOpen
                    ? "transform rotate-180 transition-transform duration-200"
                    : "transition-transform duration-200";

            /*
             * Build the trigger button for this accordion item. It uses
             * aria-expanded to communicate its state and aria-controls to
             * link to the panel it manages. Clicking toggles between open
             * and closed: if this item is already open, clicking collapses
             * it (sets openIndex to -1); otherwise, it opens this item.
             */
            Element trigger = button().attr("type", "button")
                    .id(triggerId)
                    .ariaExpanded(isOpen)
                    .ariaControls(panelId)
                    .cls("w-full", "flex", "items-center", "justify-between",
                            "px-6", "py-4", "text-left",
                            "hover:bg-gray-750", "transition-colors",
                            "focus:outline-none", "focus:ring-2",
                            "focus:ring-violet-500", "focus:ring-inset")
                    .on("click", e -> {
                        /* Toggle logic: if this item is already open, close it;
                         * otherwise, open it (which implicitly closes the
                         * previously open item since only openIndex is tracked). */
                        openIndex = (openIndex == index) ? -1 : index;
                    })
                    .children(
                            /* Question text displayed as the trigger label. */
                            span().cls("text-white", "font-medium").text(item.question()),

                            /* Chevron icon that rotates when the panel is open.
                             * Uses a downward-pointing Unicode arrow character. */
                            span().cls("text-gray-400", "text-lg", chevronClasses)
                                    .ariaHidden(true)
                                    .text("\u25BE")
                    );

            /* Start building the wrapper div for this accordion item.
             * A bottom border separates each item, except the last one. */
            Element itemWrapper = div();
            if (i < faqItems.size() - 1) {
                itemWrapper = itemWrapper.cls("border-b", "border-gray-700");
            }

            /*
             * Only render the content panel if this item is open. When closed,
             * the panel is completely absent from the DOM (not just hidden),
             * which is a common accordion pattern that reduces DOM size.
             */
            if (isOpen) {
                Element panel = div()
                        .id(panelId)
                        .role("region")
                        .ariaLabelledBy(triggerId)
                        .cls("px-6", "pb-4")
                        .children(
                                p().cls("text-gray-300", "leading-relaxed")
                                        .text(item.answer())
                        );

                itemWrapper = itemWrapper.children(trigger, panel);
            } else {
                /* When collapsed, only the trigger is rendered. The panel
                 * content is omitted entirely for efficiency. */
                itemWrapper = itemWrapper.children(trigger);
            }

            elements.add(itemWrapper);
        }

        return elements;
    }

    /**
     * Immutable data record representing a single FAQ item in the accordion.
     *
     * @param question the question text displayed on the accordion trigger button
     * @param answer   the answer text displayed in the expandable content panel
     */
    public record FaqItem(String question, String answer) {
    }
}
