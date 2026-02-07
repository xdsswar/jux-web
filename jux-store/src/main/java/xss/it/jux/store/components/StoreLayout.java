/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Main page layout wrapping content with skip-nav, navbar, main area, and footer.
 *
 * <p>Every page in the store uses this layout. It provides:</p>
 * <ul>
 *   <li>Skip navigation link (WCAG 2.4.1 — Bypass Blocks)</li>
 *   <li>Store navbar with navigation, search, cart, and language switcher</li>
 *   <li>Main content area with {@code id="main-content"} as the skip-nav target</li>
 *   <li>Store footer with site links and copyright</li>
 * </ul>
 *
 * <p>Usage: compose in any page's render() method:</p>
 * <pre>{@code
 * new StoreLayout("/products", messages(),
 *     section().cls("py-12").children(...)
 * )
 * }</pre>
 */
public class StoreLayout extends Component {

    private final String activePath;
    private final JuxMessages messages;
    private final Element content;

    public StoreLayout(String activePath, JuxMessages messages, Element content) {
        this.activePath = activePath;
        this.messages = messages;
        this.content = content;
    }

    @Override
    public Element render() {
        return div().cls("flex", "flex-col", "min-h-screen").children(
                /* Skip navigation for keyboard users (WCAG 2.4.1) */
                skipNav("main-content", "Skip to main content"),
                /* Store header with navigation */
                new StoreNavbar(activePath, messages).render(),
                /* Main content area — skip-nav target */
                main_().id("main-content").cls("flex-1").children(content),
                /* Store footer */
                new StoreFooter(messages).render()
        );
    }
}
