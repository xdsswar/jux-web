/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.store.pages;

import xss.it.jux.annotation.*;
import xss.it.jux.core.*;
import xss.it.jux.store.components.*;

import static xss.it.jux.core.Elements.*;

/**
 * 404 catch-all page for unmatched routes.
 *
 * <p>Uses {@code priority = Integer.MAX_VALUE} so all other routes
 * take precedence. Sets HTTP 404 status via pageMeta().</p>
 */
@Route(value = "/**", priority = Integer.MAX_VALUE)
@Title("Page Not Found - JUX Store")
public class NotFoundPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create().status(404);
    }

    @Override
    public Element render() {
        var m = messages();
        return new StoreLayout("/", m,
                section().cls("py-20").children(
                        div().cls("max-w-lg", "mx-auto", "text-center", "px-4").children(
                                div().cls("text-8xl", "font-bold", "text-gray-200", "mb-4")
                                        .ariaHidden(true).text("404"),
                                h1().cls("text-3xl", "font-bold", "text-gray-900", "mb-3")
                                        .text(m.getString("notfound.title")),
                                p().cls("text-gray-500", "mb-8")
                                        .text(m.getString("notfound.text")),
                                div().cls("flex", "flex-wrap", "justify-center", "gap-4").children(
                                        a().cls("bg-indigo-600", "text-white", "px-6", "py-3",
                                                        "rounded-lg", "font-semibold",
                                                        "hover:bg-indigo-700")
                                                .attr("href", "/")
                                                .text(m.getString("notfound.home")),
                                        a().cls("border", "border-gray-300", "text-gray-700",
                                                        "px-6", "py-3", "rounded-lg",
                                                        "font-semibold", "hover:bg-gray-50")
                                                .attr("href", "/products")
                                                .text(m.getString("notfound.products"))
                                )
                        )
                )
        ).render();
    }
}
