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

package xss.it.jux.demo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Main page layout wrapping content with navbar and footer.
 *
 * <p>Usage: compose in any page's render() method:
 * <pre>{@code
 * new PageLayout("/about", messages,
 *     section().cls("container").children(...)
 * )
 * }</pre>
 */
public class PageLayout extends Component {

    private final String activePath;
    private final JuxMessages messages;
    private final Element content;

    public PageLayout(String activePath, JuxMessages messages, Element content) {
        this.activePath = activePath;
        this.messages = messages;
        this.content = content;
    }

    @Override
    public Element render() {
        return div().cls("d-flex", "flex-column", "min-vh-100").children(
            skipNav("main-content", "Skip to main content"),
            new Navbar(activePath, messages).render(),
            main_().id("main-content").cls("flex-grow-1").children(content),
            new Footer(messages).render()
        );
    }
}
