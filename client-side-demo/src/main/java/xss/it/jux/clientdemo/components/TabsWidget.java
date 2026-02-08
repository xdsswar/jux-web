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

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible tabbed interface widget demonstrating ARIA tab pattern in JUX.
 *
 * <p>This component implements the
 * <a href="https://www.w3.org/WAI/ARIA/apg/patterns/tabs/">WAI-ARIA Tabs Pattern</a>,
 * providing a fully accessible tabbed content interface built entirely in Java.
 * It showcases how JUX's {@code @State} reactivity combined with proper ARIA
 * roles and keyboard event handling creates a rich, accessible interactive widget.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for active tab tracking</b> -- the {@code activeTab} index
 *       determines which tab is selected and which panel is visible. Changing this
 *       value triggers a complete re-render with DOM diffing.</li>
 *   <li><b>Keyboard navigation</b> -- the {@code "keydown"} event handler on the
 *       tab list processes ArrowLeft/ArrowRight to move between tabs, and Home/End
 *       to jump to the first/last tab, following the WAI-ARIA tabs pattern.</li>
 *   <li><b>ARIA roles and states</b> -- full implementation of {@code role="tablist"},
 *       {@code role="tab"}, {@code role="tabpanel"}, {@code aria-selected},
 *       {@code aria-controls}, and {@code aria-labelledby} relationships.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- tabs and panels are linked via
 *       ARIA attributes ({@code aria-controls}/{@code aria-labelledby}).</li>
 *   <li><b>2.1.1 Keyboard</b> -- all tabs are reachable via arrow keys within the
 *       tablist. Tab key moves focus into the active panel content.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- each tab has {@code role="tab"} with
 *       {@code aria-selected} reflecting its state; panels have {@code role="tabpanel"}.</li>
 * </ul>
 *
 * <h2>Tab Content</h2>
 * <p>Three tabs demonstrate different content types:</p>
 * <ul>
 *   <li><b>Overview</b> -- descriptive paragraph text about JUX components.</li>
 *   <li><b>Features</b> -- a bulleted feature list using semantic {@code <ul>}.</li>
 *   <li><b>Code</b> -- a code block example using {@code <pre>}/{@code <code>}.</li>
 * </ul>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class TabsWidget extends Component {

    /**
     * Index of the currently active (selected) tab.
     *
     * <p>Zero-based: 0 = "Overview", 1 = "Features", 2 = "Code". On the client,
     * changing this value via click or keyboard navigation triggers a re-render
     * that shows the corresponding panel and updates ARIA selected states.</p>
     */
    @State
    private int activeTab = 0;

    /**
     * Tab label definitions.
     *
     * <p>An ordered list of the tab labels displayed in the tab strip. The index
     * of each label corresponds to the panel content rendered by
     * {@link #buildPanelContent(int)}. This list is immutable and used for both
     * rendering the tab buttons and calculating keyboard navigation bounds.</p>
     */
    private final List<String> tabLabels = List.of("Overview", "Features", "Code");

    /**
     * Builds the virtual DOM tree for the tabbed interface widget.
     *
     * <p>The rendered structure follows the WAI-ARIA tabs pattern:</p>
     * <pre>{@code
     * <div>
     *   <div role="tablist">
     *     <button role="tab" aria-selected="true" ...>Overview</button>
     *     <button role="tab" aria-selected="false" ...>Features</button>
     *     <button role="tab" aria-selected="false" ...>Code</button>
     *   </div>
     *   <div role="tabpanel" aria-labelledby="tab-0">
     *     <!-- active panel content -->
     *   </div>
     * </div>
     * }</pre>
     *
     * @return the root element of the tabs widget, never null
     */
    @Override
    public Element render() {
        /* Build and return the complete tabbed interface element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "overflow-hidden").children(

                /* ── Tab Strip (tablist) ───────────────────────────────────────
                 * A horizontal row of tab buttons with role="tablist". The tablist
                 * handles keyboard navigation via the "keydown" event: ArrowLeft
                 * and ArrowRight cycle through tabs, Home jumps to first, End
                 * jumps to last.
                 */
                div().role("tablist")
                        .aria("label", "Component information tabs")
                        .cls("flex", "border-b", "border-gray-700")
                        .on("keydown", e -> {
                            /* Handle keyboard navigation within the tab strip.
                             * ArrowRight advances to the next tab (wrapping to first),
                             * ArrowLeft goes to the previous tab (wrapping to last),
                             * Home jumps to the first tab, End jumps to the last. */
                            switch (e.getKey()) {
                                case "ArrowRight" -> {
                                    activeTab = (activeTab + 1) % tabLabels.size();
                                    e.preventDefault();
                                }
                                case "ArrowLeft" -> {
                                    activeTab = (activeTab - 1 + tabLabels.size()) % tabLabels.size();
                                    e.preventDefault();
                                }
                                case "Home" -> {
                                    activeTab = 0;
                                    e.preventDefault();
                                }
                                case "End" -> {
                                    activeTab = tabLabels.size() - 1;
                                    e.preventDefault();
                                }
                            }
                        })
                        .children(buildTabButtons()),

                /* ── Tab Panel ─────────────────────────────────────────────────
                 * Only the active panel is rendered. The panel has role="tabpanel"
                 * and aria-labelledby linking it back to the corresponding tab
                 * button for screen reader context.
                 */
                div().role("tabpanel")
                        .id("tabpanel-" + activeTab)
                        .aria("labelledby", "tab-" + activeTab)
                        .tabIndex(0)
                        .cls("p-6")
                        .children(buildPanelContent(activeTab))
        );
    }

    /**
     * Generates the tab button elements for the tab strip.
     *
     * <p>Each button has:</p>
     * <ul>
     *   <li>{@code role="tab"} identifying it as a tab to assistive technology.</li>
     *   <li>{@code aria-selected} reflecting whether it is the active tab.</li>
     *   <li>{@code aria-controls} pointing to the corresponding panel ID.</li>
     *   <li>{@code tabindex} set to 0 for the active tab and -1 for inactive tabs,
     *       following the "roving tabindex" pattern so that only one tab is in the
     *       tab order at a time.</li>
     *   <li>A bottom border highlight (violet) on the active tab for visual indication.</li>
     * </ul>
     *
     * @return an array of tab button elements
     */
    private Element[] buildTabButtons() {
        /* Allocate an array to hold one button element per tab label. */
        Element[] buttons = new Element[tabLabels.size()];

        for (int i = 0; i < tabLabels.size(); i++) {
            /* Capture the index for lambda closure. */
            final int tabIndex = i;
            boolean isActive = (i == activeTab);

            /*
             * Build the CSS class string for this tab button. The active tab gets
             * a violet bottom border and brighter text; inactive tabs have muted
             * text and a transparent bottom border that transitions on hover.
             */
            String activeClasses = isActive
                    ? "border-violet-500 text-white"
                    : "border-transparent text-gray-400 hover:text-gray-200 hover:border-gray-500";

            buttons[i] = button().attr("type", "button")
                    .role("tab")
                    .id("tab-" + i)
                    .ariaSelected(isActive)
                    .ariaControls("tabpanel-" + i)
                    .tabIndex(isActive ? 0 : -1)
                    .cls("px-6", "py-3", "text-sm", "font-medium",
                            "border-b-2", "transition-colors",
                            "focus:outline-none", "focus:ring-2",
                            "focus:ring-violet-500", "focus:ring-inset",
                            activeClasses)
                    .on("click", e -> activeTab = tabIndex)
                    .text(tabLabels.get(i));
        }

        return buttons;
    }

    /**
     * Generates the content element for the specified tab panel index.
     *
     * <p>Each tab has distinct content to demonstrate different rendering patterns:</p>
     * <ul>
     *   <li><b>Tab 0 (Overview)</b> -- paragraph text describing JUX component architecture.</li>
     *   <li><b>Tab 1 (Features)</b> -- a semantic unordered list of framework features.</li>
     *   <li><b>Tab 2 (Code)</b> -- a preformatted code block showing a JUX component example.</li>
     * </ul>
     *
     * @param index the zero-based tab index to generate content for
     * @return the content element for the requested tab panel
     */
    private Element buildPanelContent(int index) {
        return switch (index) {
            /*
             * Overview panel: descriptive text about the JUX component model.
             * Uses semantic <p> tags for paragraph content.
             */
            case 0 -> div().children(
                    h3().cls("text-lg", "font-semibold", "text-white", "mb-3")
                            .text("JUX Component Model"),
                    p().cls("text-gray-300", "leading-relaxed", "mb-3")
                            .text("JUX components are pure Java classes that produce virtual DOM "
                                    + "element trees. Each component extends the base Component class "
                                    + "and implements a render() method that returns an Element tree."),
                    p().cls("text-gray-300", "leading-relaxed")
                            .text("Components marked with @JuxComponent(clientSide = true) are "
                                    + "compiled to JavaScript via TeaVM and hydrated on the client "
                                    + "after server-side rendering, enabling full interactivity "
                                    + "without writing a single line of JavaScript.")
            );

            /*
             * Features panel: a bulleted list of framework capabilities.
             * Uses semantic <ul>/<li> for list structure.
             */
            case 1 -> div().children(
                    h3().cls("text-lg", "font-semibold", "text-white", "mb-3")
                            .text("Key Features"),
                    ul().cls("space-y-2", "text-gray-300").children(
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("Reactive @State fields with automatic DOM diffing")
                            ),
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("Server-side rendering with client-side hydration")
                            ),
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("WCAG 2.2 AA accessibility built into every component")
                            ),
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("Lifecycle hooks: @OnMount and @OnUnmount")
                            ),
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("Event handling via .on() and @On annotations")
                            ),
                            li().cls("flex", "items-start", "gap-2").children(
                                    span().cls("text-violet-400", "mt-1").text("\u2022"),
                                    span().text("Zero JavaScript authoring -- 100% Java via TeaVM")
                            )
                    )
            );

            /*
             * Code panel: a preformatted code example showing a minimal JUX component.
             * Uses <pre><code> for code display with a monospace font.
             */
            case 2 -> div().children(
                    h3().cls("text-lg", "font-semibold", "text-white", "mb-3")
                            .text("Example Component"),
                    pre().cls("bg-gray-900", "rounded-lg", "p-4", "overflow-x-auto").children(
                            code().cls("text-sm", "text-gray-300", "font-mono")
                                    .text("@JuxComponent(clientSide = true)\n"
                                            + "public class Counter extends Component {\n"
                                            + "    @State private int count = 0;\n"
                                            + "\n"
                                            + "    @Override\n"
                                            + "    public Element render() {\n"
                                            + "        return div().children(\n"
                                            + "            span().text(\"Count: \" + count),\n"
                                            + "            button().text(\"+\")\n"
                                            + "                .on(\"click\", e -> count++)\n"
                                            + "        );\n"
                                            + "    }\n"
                                            + "}")
                    )
            );

            /*
             * Fallback for any unexpected index value. This should never be reached
             * since activeTab is constrained to 0..2, but defensive programming
             * demands a default branch.
             */
            default -> p().cls("text-gray-400").text("No content available.");
        };
    }
}
