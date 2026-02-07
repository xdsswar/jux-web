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

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Blog listing page with sample posts.
 */
@Route("/blog")
@Title("Blog - JUX Demo")
@Meta(name = "description", content = "JUX framework blog posts and tutorials")
public class BlogListPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    private record BlogPost(String slug, String title, String excerpt, String date, String tag) {}

    private static final List<BlogPost> POSTS = List.of(
        new BlogPost("getting-started",
            "Getting Started with JUX",
            "Learn how to set up your first JUX project with Spring Boot and start building pages in pure Java.",
            "Feb 6, 2026", "Tutorial"),
        new BlogPost("accessibility-first",
            "Accessibility-First Development",
            "How JUX enforces WCAG 2.2 AA compliance at the framework level - and why that matters.",
            "Feb 4, 2026", "Architecture"),
        new BlogPost("ssr-performance",
            "SSR Performance Under the Hood",
            "Deep dive into how JUX achieves sub-5ms server-side rendering with zero runtime reflection.",
            "Feb 1, 2026", "Performance"),
        new BlogPost("teavm-hydration",
            "Client Hydration with TeaVM",
            "How interactive components are compiled to JavaScript and hydrated on the client.",
            "Jan 28, 2026", "Architecture"),
        new BlogPost("i18n-type-safe",
            "Type-Safe i18n with Java Interfaces",
            "Forget .properties files. JUX translations are Java interfaces with compile-time safety.",
            "Jan 25, 2026", "Tutorial"),
        new BlogPost("bootstrap-integration",
            "Using Bootstrap 5 with JUX",
            "How this demo app integrates Bootstrap 5 with JUX - using .cls() for styling, CDN for assets.",
            "Jan 22, 2026", "Tutorial")
    );

    @Override
    public Element render() {
        return new PageLayout("/blog", messages(), pageContent()).render();
    }

    private Element pageContent() {
        var m = messages();
        return div().children(
            section().cls("bg-primary", "text-white", "py-5").children(
                div().cls("container", "py-3").children(
                    h1().cls("display-5", "fw-bold").text(m.getString("blog.header.title")),
                    p().cls("lead", "mb-0", "opacity-75").text(m.getString("blog.header.subtitle"))
                )
            ),
            section().cls("py-5").children(
                div().cls("container").children(
                    div().cls("row", "g-4").children(
                        POSTS.stream().map(this::blogCard).toList()
                    )
                )
            )
        );
    }

    private Element blogCard(BlogPost post) {
        return div().cls("col-md-6", "col-lg-4").children(
            article().cls("card", "h-100", "border-0", "shadow-sm").children(
                div().cls("card-body", "d-flex", "flex-column").children(
                    div().cls("mb-2").children(
                        span().cls("badge", "bg-primary-subtle", "text-primary-emphasis")
                            .text(post.tag())
                    ),
                    h2().cls("h5", "card-title").children(
                        a().cls("text-decoration-none", "text-dark", "stretched-link")
                            .attr("href", "/blog/" + post.slug())
                            .text(post.title())
                    ),
                    p().cls("card-text", "text-secondary", "flex-grow-1").text(post.excerpt()),
                    small().cls("text-muted").children(
                        time().attr("datetime", "2026-02-06").text(post.date())
                    )
                )
            )
        );
    }
}
