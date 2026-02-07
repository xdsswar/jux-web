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
import xss.it.jux.theme.JuxBreadcrumb;

import java.util.List;
import java.util.Map;

import static xss.it.jux.core.Elements.*;

/**
 * Individual blog post page demonstrating @PathParam injection.
 */
@Route("/blog/{slug}")
public class BlogPostPage extends Page {

    @PathParam
    private String slug;


    private record PostData(String title, String body, String date, String author, String tag) {}

    private static final Map<String, PostData> POSTS = Map.of(
        "getting-started", new PostData(
            "Getting Started with JUX",
            "JUX lets you build complete websites in pure Java. Here's how to get started.\n\n" +
            "First, add jux-server as a dependency in your build.gradle. Spring Boot auto-configuration " +
            "picks up the framework automatically - no manual bean registration needed.\n\n" +
            "Create a class that extends Component, annotate it with @Route, and override render(). " +
            "The framework discovers it at startup, registers the URL mapping, and handles SSR rendering.\n\n" +
            "Your page can use @Title, @Css, @Js, and @Meta annotations for metadata. For dynamic " +
            "metadata (from a database, for example), override pageMeta() and return a PageMeta builder.\n\n" +
            "That's it. No controllers, no templates, no config files. Just Java classes that produce HTML.",
            "Feb 6, 2026", "JUX Team", "Tutorial"),
        "accessibility-first", new PostData(
            "Accessibility-First Development",
            "JUX doesn't treat accessibility as an afterthought - it's a core design principle.\n\n" +
            "The Element API has no img() method without an alt parameter. You literally cannot create " +
            "an image without accessibility text. For decorative images, use imgDecorative().\n\n" +
            "Every ARIA attribute has a first-class method: ariaExpanded(), ariaControls(), " +
            "ariaLabelledBy(), ariaRequired(), ariaInvalid(). These aren't afterthoughts bolted onto " +
            "a generic attr() call - they're the primary API.\n\n" +
            "The built-in a11y audit engine runs 14 WCAG rules on every render in development mode. " +
            "Missing labels, duplicate IDs, heading hierarchy violations, insufficient contrast - " +
            "all caught before the page reaches users.",
            "Feb 4, 2026", "JUX Team", "Architecture"),
        "ssr-performance", new PostData(
            "SSR Performance Under the Hood",
            "JUX achieves sub-5ms server-side rendering through careful architecture.\n\n" +
            "The Element tree is a lightweight in-memory structure. No DOM parsing, no template " +
            "compilation, no reflection on the hot path. Element.of() creates a simple Java object, " +
            "builder methods set fields, and the renderer walks the tree once to produce HTML.\n\n" +
            "The Caffeine-backed SSR cache stores rendered HTML keyed by path + query + locale. " +
            "Static pages like About or Pricing can set @Route(cacheTtl = 3600) for 1-hour caching.\n\n" +
            "Resource deduplication happens at render time. If a layout adds Bootstrap CSS and a " +
            "child component also references it, the framework emits the <link> tag once.",
            "Feb 1, 2026", "JUX Team", "Performance"),
        "teavm-hydration", new PostData(
            "Client Hydration with TeaVM",
            "JUX components marked with @JuxComponent(clientSide = true) are compiled to JavaScript " +
            "via TeaVM and hydrated in the browser.\n\n" +
            "The server renders the component to HTML normally. The client JavaScript finds elements " +
            "with data-jux-id attributes, instantiates the matching component class, and attaches " +
            "event handlers. No full page re-render - just event binding on existing DOM.\n\n" +
            "When @State fields change, the component re-renders its virtual Element tree, diffs " +
            "it against the previous tree, and applies minimal DOM mutations. This is similar to " +
            "React's reconciliation but implemented entirely in Java via TeaVM's JSO DOM API.",
            "Jan 28, 2026", "JUX Team", "Architecture"),
        "i18n-type-safe", new PostData(
            "Type-Safe i18n with Java Interfaces",
            "Forget .properties files. In JUX, translations are Java interfaces.\n\n" +
            "Define a @MessageBundle interface with methods that return strings. Each method has a " +
            "@Message annotation with the default language text. Create locale-specific implementations " +
            "with @MessageLocale. The annotation processor validates everything at compile time.\n\n" +
            "No more missing keys at runtime. No more format string mismatches. Your IDE autocompletes " +
            "translation keys. Refactoring renames propagate to all locales.",
            "Jan 25, 2026", "JUX Team", "Tutorial"),
        "bootstrap-integration", new PostData(
            "Using Bootstrap 5 with JUX",
            "This demo application uses Bootstrap 5 for styling. Here's how the integration works.\n\n" +
            "Bootstrap CSS and JS are loaded via @Css and @Js annotations pointing to the CDN. " +
            "The .cls() method on Element applies Bootstrap classes: .cls(\"btn\", \"btn-primary\"), " +
            ".cls(\"row\", \"g-4\"), .cls(\"card\", \"shadow-sm\").\n\n" +
            "JUX doesn't care which CSS framework you use. Bootstrap, Tailwind, Bulma, or your own " +
            "custom CSS - it's all just classes. The framework produces semantic HTML5, and you style " +
            "it however you want.",
            "Jan 22, 2026", "JUX Team", "Tutorial")
    );

    @Override
    public PageMeta pageMeta() {
        var post = POSTS.get(slug);
        if (post == null) {
            return PageMeta.create().status(404).title("Not Found - JUX Demo");
        }
        return PageMeta.create()
            .title(post.title() + " - JUX Blog")
            .description(post.body().substring(0, Math.min(160, post.body().length())))
            .ogTitle(post.title())
            .ogDescription(post.body().substring(0, Math.min(160, post.body().length())))
            .ogType("article");
    }

    @Override
    public Element render() {
        var post = POSTS.get(slug);
        if (post == null) {
            return new PageLayout("/blog", messages(), notFound()).render();
        }
        return new PageLayout("/blog", messages(), postContent(post)).render();
    }

    private Element postContent(PostData post) {
        var m = messages();
        return div().children(
            section().cls("bg-primary", "text-white", "py-4").children(
                div().cls("container").children(
                    new JuxBreadcrumb(List.of(
                        new JuxBreadcrumb.Crumb(m.getString("nav.home"), "/"),
                        new JuxBreadcrumb.Crumb(m.getString("nav.blog"), "/blog"),
                        new JuxBreadcrumb.Crumb(post.title(), null)
                    )).render()
                )
            ),
            section().cls("py-5").children(
                div().cls("container").children(
                    div().cls("row").children(
                        div().cls("col-lg-8", "mx-auto").children(
                            article().children(
                                div().cls("mb-4").children(
                                    span().cls("badge", "bg-primary-subtle", "text-primary-emphasis", "me-2")
                                        .text(post.tag()),
                                    small().cls("text-muted").children(
                                        time().attr("datetime", "2026-02-06").text(post.date()),
                                        span().cls("mx-2").text(m.getString("blog.post.by")),
                                        strong().text(post.author()),
                                        span().cls("mx-2").text(" \u00b7 "),
                                        span().cls("text-secondary").children(
                                            Element.of("i").cls("bi", "bi-clock", "me-1"),
                                            span().text(estimateReadingTime(post.body()) + m.getString("blog.post.min_read"))
                                        )
                                    )
                                ),
                                h1().cls("display-6", "fw-bold", "mb-4").text(post.title()),
                                renderBody(post.body()),
                                hr().cls("my-5"),
                                div().cls("d-flex", "justify-content-between").children(
                                    a().cls("btn", "btn-outline-primary").attr("href", "/blog")
                                        .children(
                                            Element.of("i").cls("bi-arrow-left", "me-2").ariaHidden(true),
                                            span().text(m.getString("blog.post.back"))
                                        )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    private Element renderBody(String body) {
        var paragraphs = body.split("\n\n");
        var elements = new java.util.ArrayList<Element>();
        for (var para : paragraphs) {
            if (!para.isBlank()) {
                elements.add(p().cls("mb-3", "text-secondary").text(para.trim()));
            }
        }
        return div().cls("blog-content").children(elements);
    }

    private int estimateReadingTime(String text) {
        if (text == null) return 1;
        int words = text.split("\\s+").length;
        return Math.max(1, words / 200);
    }

    private Element notFound() {
        var m = messages();
        return section().cls("py-5").children(
            div().cls("container", "text-center", "py-5").children(
                h1().cls("display-1", "fw-bold", "text-muted").text(m.getString("blog.post.notfound.code")),
                p().cls("lead", "mb-4").text(m.getString("blog.post.notfound.text")),
                a().cls("btn", "btn-primary").attr("href", "/blog").text(m.getString("blog.post.notfound.btn"))
            )
        );
    }
}
