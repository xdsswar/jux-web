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
 * Accessible tabbed content component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a tab interface following the WAI-ARIA Tabs pattern, where a set
 * of tab buttons controls which content panel is visible. The component
 * produces correct ARIA roles, states, and relationships so screen readers
 * can announce tab counts, selected state, and panel associations.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>Container {@code <div>} with {@code role="tablist"} holding all tab buttons</li>
 *   <li>Each tab is a {@code <button>} with {@code role="tab"}, {@code aria-selected},
 *       and {@code aria-controls} pointing to its panel</li>
 *   <li>Each panel is a {@code <div>} with {@code role="tabpanel"} and
 *       {@code aria-labelledby} pointing back to its tab</li>
 * </ul>
 *
 * <p><b>Keyboard interaction (WCAG 2.1.1):</b></p>
 * <ul>
 *   <li><b>Left/Right Arrow</b> -- move between tabs</li>
 *   <li><b>Home/End</b> -- jump to first/last tab</li>
 *   <li><b>Tab key</b> -- moves focus into the active panel content</li>
 *   <li><b>Enter/Space</b> -- activates the focused tab</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * var tabs = List.of(
 *     new JuxTabs.Tab("Overview", p().text("Product overview...")),
 *     new JuxTabs.Tab("Specs",    table()...),
 *     new JuxTabs.Tab("Reviews",  div()...)
 * );
 * child(new JuxTabs(tabs));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/tabs/">WAI-ARIA Tabs Pattern</a>
 */
@JuxComponent
public class JuxTabs extends Component {

    /**
     * A single tab definition consisting of a label and its associated content panel.
     *
     * <p>Instances of this record are immutable and define the data model for
     * one tab in the tabbed interface. The label is displayed on the tab button
     * and used as the accessible name for the panel via {@code aria-labelledby}.</p>
     *
     * @param label   the visible text on the tab button; must not be null or empty
     * @param content the Element tree displayed when this tab is active; must not be null
     */
    public record Tab(String label, Element content) {

        /**
         * Creates a new Tab with validation.
         *
         * @param label   the tab button label
         * @param content the tab panel content
         * @throws NullPointerException if label or content is null
         */
        public Tab {
            Objects.requireNonNull(label, "Tab label must not be null");
            Objects.requireNonNull(content, "Tab content must not be null");
        }
    }

    /**
     * The list of tabs to render.
     *
     * <p>Order matters: tabs are rendered left-to-right (or right-to-left
     * in RTL layouts) in the order they appear in this list. The first
     * tab is selected by default.</p>
     */
    private final List<Tab> tabs;

    /**
     * Unique prefix for generating tab and panel IDs.
     *
     * <p>Ensures that ARIA cross-references ({@code aria-controls},
     * {@code aria-labelledby}) remain unique even when multiple
     * {@code JuxTabs} instances exist on the same page.</p>
     */
    private final String idPrefix;

    /**
     * Creates a new tabbed content component.
     *
     * @param tabs the list of tabs to display; must not be null or empty
     * @throws NullPointerException     if tabs is null
     * @throws IllegalArgumentException if tabs is empty
     */
    public JuxTabs(List<Tab> tabs) {
        Objects.requireNonNull(tabs, "Tabs list must not be null");
        if (tabs.isEmpty()) {
            throw new IllegalArgumentException("Tabs list must not be empty");
        }

        /* Defensive copy to prevent external mutation after construction. */
        this.tabs = List.copyOf(tabs);
        this.idPrefix = "jux-tabs-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Builds the tabbed interface Element tree with full ARIA support.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <div class="jux-tabs">
     *   <div role="tablist" aria-label="Tabs">
     *     <button role="tab" id="tab-0" aria-selected="true"
     *             aria-controls="panel-0" tabindex="0">Tab 1</button>
     *     <button role="tab" id="tab-1" aria-selected="false"
     *             aria-controls="panel-1" tabindex="-1">Tab 2</button>
     *   </div>
     *   <div role="tabpanel" id="panel-0" aria-labelledby="tab-0"
     *        tabindex="0">
     *     <!-- Tab 1 content -->
     *   </div>
     *   <div role="tabpanel" id="panel-1" aria-labelledby="tab-1"
     *        tabindex="0" hidden>
     *     <!-- Tab 2 content (hidden by default) -->
     *   </div>
     * </div>
     * }</pre>
     *
     * <p>The first tab is selected by default. Inactive panels have the
     * {@code hidden} attribute set. Each panel has {@code tabindex="0"}
     * so it can receive focus when the user presses Tab from the tab bar.</p>
     *
     * @return the root container Element with tablist and panels, never null
     */
    @Override
    public Element render() {
        /* Build the tab button list using Bootstrap 5 nav-tabs pattern. */
        List<Element> tabItems = new ArrayList<>();
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);

            /* Generate unique IDs for cross-referencing between tab and panel. */
            String tabId = idPrefix + "-tab-" + i;
            String panelId = idPrefix + "-panel-" + i;

            /* First tab (index 0) is selected by default. */
            boolean isSelected = (i == 0);

            /*
             * Build the tab button with Bootstrap data attributes:
             * - data-bs-toggle="tab" tells Bootstrap JS to handle tab switching
             * - data-bs-target references the panel to show
             * - role="tab" + aria-selected for screen readers
             * - aria-controls links to the associated panel by ID
             */
            Element tabButton = button()
                    .attr("type", "button")
                    .cls("nav-link")
                    .id(tabId)
                    .attr("data-bs-toggle", "tab")
                    .attr("data-bs-target", "#" + panelId)
                    .role("tab")
                    .ariaControls(panelId)
                    .ariaSelected(isSelected);

            if (isSelected) {
                tabButton = tabButton.cls("active");
            }
            tabButton = tabButton.text(tab.label());

            tabItems.add(li().cls("nav-item").role("presentation").children(tabButton));
        }

        /*
         * Build the tablist using Bootstrap nav-tabs classes.
         * role="tablist" is added explicitly for screen readers.
         */
        Element tabList = ul()
                .cls("nav", "nav-tabs")
                .role("tablist")
                .children(tabItems);

        /* Build the tab panels inside a Bootstrap tab-content container. */
        List<Element> panelElements = new ArrayList<>();
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            String tabId = idPrefix + "-tab-" + i;
            String panelId = idPrefix + "-panel-" + i;
            boolean isSelected = (i == 0);

            /*
             * Build the tab panel using Bootstrap tab-pane pattern:
             * - "fade" enables CSS transition animation
             * - "show active" on the selected panel makes it visible
             * - role="tabpanel" + aria-labelledby for screen readers
             * - tabindex="0" makes the panel focusable via Tab key
             */
            Element panel = div()
                    .cls("tab-pane", "fade")
                    .id(panelId)
                    .role("tabpanel")
                    .ariaLabelledBy(tabId)
                    .tabIndex(0)
                    .children(tab.content());

            if (isSelected) {
                panel = panel.cls("show", "active");
            }

            panelElements.add(panel);
        }

        Element tabContent = div().cls("tab-content").children(panelElements);

        /* Assemble: tab navigation + content panels. */
        return div().children(tabList, tabContent);
    }
}
