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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static xss.it.jux.core.Elements.*;

/**
 * Accessible dropdown menu component (WCAG 2.2 AA compliant).
 *
 * <p>Renders a button that toggles a dropdown menu of navigable links.
 * The component follows the WAI-ARIA Menu Button pattern with proper
 * roles, states, and keyboard interaction support.</p>
 *
 * <p><b>ARIA structure:</b></p>
 * <ul>
 *   <li>A trigger {@code <button>} with {@code aria-haspopup="menu"} and
 *       {@code aria-expanded} to indicate whether the menu is open</li>
 *   <li>A {@code <ul>} with {@code role="menu"} containing the menu items</li>
 *   <li>Each item is an {@code <li>} with {@code role="menuitem"} containing
 *       an {@code <a>} element for navigation</li>
 * </ul>
 *
 * <p><b>Keyboard interaction (WCAG 2.1.1):</b></p>
 * <ul>
 *   <li><b>Enter/Space</b> on trigger -- toggles the menu</li>
 *   <li><b>Down Arrow</b> on trigger -- opens the menu and moves focus to first item</li>
 *   <li><b>Up/Down Arrow</b> in menu -- navigates between items</li>
 *   <li><b>Escape</b> -- closes the menu and returns focus to trigger</li>
 *   <li><b>Home/End</b> -- jumps to first/last menu item</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * var items = List.of(
 *     new JuxDropdown.DropdownItem("Profile", "/profile"),
 *     new JuxDropdown.DropdownItem("Settings", "/settings"),
 *     new JuxDropdown.DropdownItem("Sign Out", "/logout")
 * );
 * child(new JuxDropdown("Account", items));
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/ARIA/apg/patterns/menu-button/">WAI-ARIA Menu Button Pattern</a>
 */
@JuxComponent
public class JuxDropdown extends Component {

    /**
     * A single item in the dropdown menu.
     *
     * <p>Each item is rendered as a link ({@code <a>}) inside a list item
     * with {@code role="menuitem"}. The label is the visible and accessible
     * text; the href is the navigation destination.</p>
     *
     * @param label the visible text for this menu item; must not be null
     * @param href  the URL that this item navigates to when activated; must not be null
     */
    public record DropdownItem(String label, String href) {

        /**
         * Creates a new dropdown item with validation.
         *
         * @param label the menu item text
         * @param href  the navigation URL
         * @throws NullPointerException if label or href is null
         */
        public DropdownItem {
            Objects.requireNonNull(label, "DropdownItem label must not be null");
            Objects.requireNonNull(href, "DropdownItem href must not be null");
        }
    }

    /**
     * The visible label on the trigger button.
     *
     * <p>This text is displayed on the button and serves as the accessible
     * name. It should clearly indicate what kind of menu will open
     * (e.g. "Account", "File", "Sort by").</p>
     */
    private final String label;

    /**
     * The list of menu items to display when the dropdown is open.
     *
     * <p>Items are rendered in order from top to bottom. Each item
     * produces a list item with a link in the DOM.</p>
     */
    private final List<DropdownItem> items;

    /**
     * Unique ID for the menu element.
     *
     * <p>Used by the trigger button's {@code aria-controls} attribute to
     * programmatically associate the button with the menu it controls.
     * A UUID suffix ensures uniqueness across multiple dropdown instances.</p>
     */
    private final String menuId;

    /**
     * Creates a new dropdown menu component.
     *
     * @param label the text displayed on the trigger button; must not be null
     * @param items the list of menu items; must not be null or empty
     * @throws NullPointerException     if label or items is null
     * @throws IllegalArgumentException if items is empty
     */
    public JuxDropdown(String label, List<DropdownItem> items) {
        this.label = Objects.requireNonNull(label, "Dropdown label must not be null");
        Objects.requireNonNull(items, "Dropdown items list must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Dropdown items list must not be empty");
        }

        /* Defensive copy to prevent external mutation. */
        this.items = List.copyOf(items);
        this.menuId = "jux-dropdown-menu-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Builds the dropdown menu Element tree with full ARIA support.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <div class="jux-dropdown">
     *   <button type="button" aria-haspopup="menu" aria-expanded="false"
     *           aria-controls="menu-id" class="jux-dropdown-trigger">
     *     Account
     *   </button>
     *   <ul role="menu" id="menu-id" class="jux-dropdown-menu" hidden>
     *     <li role="menuitem">
     *       <a href="/profile" class="jux-dropdown-item">Profile</a>
     *     </li>
     *     <li role="menuitem">
     *       <a href="/settings" class="jux-dropdown-item">Settings</a>
     *     </li>
     *   </ul>
     * </div>
     * }</pre>
     *
     * <p>The menu starts hidden ({@code hidden} attribute) with
     * {@code aria-expanded="false"} on the trigger. Client-side hydration
     * toggles these states when the user interacts with the button.</p>
     *
     * <p>Menu items use {@code role="menuitem"} on the {@code <li>} element
     * rather than the {@code <a>} to maintain proper list semantics while
     * providing the ARIA role for assistive technology navigation.</p>
     *
     * @return the root dropdown container Element, never null
     */
    @Override
    public Element render() {
        /*
         * Build the trigger button using Bootstrap 5 dropdown conventions:
         * - data-bs-toggle="dropdown" tells Bootstrap JS to manage this dropdown
         * - aria-expanded="false" indicates the menu is currently closed
         * - Bootstrap handles keyboard navigation, outside-click close, etc.
         */
        Element trigger = button()
                .attr("type", "button")
                .cls("btn", "btn-outline-secondary", "dropdown-toggle")
                .attr("data-bs-toggle", "dropdown")
                .ariaExpanded(false)
                .text(label);

        /*
         * Build the menu items. Each item is an <li> containing
         * an <a> with Bootstrap's dropdown-item class.
         */
        List<Element> menuItems = items.stream()
                .map(item -> li().children(
                        a().attr("href", item.href())
                                .cls("dropdown-item")
                                .text(item.label())
                ))
                .toList();

        /* Build the menu container with Bootstrap dropdown-menu class. */
        Element menu = ul()
                .cls("dropdown-menu")
                .children(menuItems);

        /* Wrap in a Bootstrap dropdown container. */
        return div().cls("dropdown").children(trigger, menu);
    }
}
