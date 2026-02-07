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

package xss.it.jux.demo.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.demo.components.PageLayout;

import static xss.it.jux.core.Elements.*;

/**
 * 404 catch-all page. Matches any unmatched path at lowest priority.
 */
@Route(value = "/**", priority = Integer.MAX_VALUE)
@Title("Page Not Found - JUX Demo")
public class NotFoundPage extends Page {

    @RequestContext
    private JuxRequestContext ctx;

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create().status(404);
    }

    @Override
    public Element render() {
        return new PageLayout("", messages(), errorContent()).render();
    }

    private Element errorContent() {
        var m = messages();
        return section().cls("py-5").children(
            div().cls("container", "text-center", "py-5").children(
                h1().cls("display-1", "fw-bold", "text-primary").text(m.getString("notfound.code")),
                p().cls("h4", "mb-3").text(m.getString("notfound.title")),
                p().cls("text-secondary", "mb-4").text(m.getString("notfound.text")),
                div().cls("d-flex", "justify-content-center", "gap-3").children(
                    a().cls("btn", "btn-primary").attr("href", "/").text(m.getString("notfound.home")),
                    a().cls("btn", "btn-outline-secondary").attr("href", "/blog").text(m.getString("notfound.blog"))
                )
            )
        );
    }
}
