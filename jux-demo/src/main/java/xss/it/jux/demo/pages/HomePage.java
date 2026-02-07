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
 * Landing page with hero section, feature cards, and CTA.
 */
@Route("/")
@Title("JUX Demo - Build Websites in Pure Java")
@Meta(name = "description", content = "Demo application showcasing the JUX web framework - build complete websites in pure Java")
public class HomePage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
            .ogTitle("JUX Demo")
            .ogDescription("Build complete websites in pure Java")
            .ogType("website");
    }

    @Override
    public Element render() {
        return new PageLayout("/", messages(), pageContent()).render();
    }

    private Element pageContent() {
        return div().children(
            heroSection(),
            featuresSection(),
            codeExampleSection(),
            ctaSection()
        );
    }

    private Element heroSection() {
        return section().cls("bg-dark", "text-white", "py-5").children(
            div().cls("container", "py-5").children(
                div().cls("row", "align-items-center").children(
                    div().cls("col-lg-7").children(
                        h1().cls("display-4", "fw-bold", "mb-3").text(messages().getString("hero.title")),
                        p().cls("lead", "mb-4", "text-secondary").text(messages().getString("hero.subtitle")),
                        div().cls("d-flex", "gap-3").children(
                            a().cls("btn", "btn-primary", "btn-lg").attr("href", "/components")
                                .text(messages().getString("hero.cta.primary")),
                            a().cls("btn", "btn-outline-light", "btn-lg").attr("href", "/about")
                                .text(messages().getString("hero.cta.secondary"))
                        )
                    ),
                    div().cls("col-lg-5", "d-none", "d-lg-block").children(
                        div().cls("bg-body-tertiary", "rounded-3", "p-4", "text-dark").children(
                            pre().cls("mb-0").children(
                                code().cls("small").text(
                                    "@Route(\"/\")\n" +
                                    "@Title(\"Home\")\n" +
                                    "public class HomePage\n" +
                                    "    extends Page {\n\n" +
                                    "  @Override\n" +
                                    "  public Element render() {\n" +
                                    "    return main_().children(\n" +
                                    "      h1().text(\"Hello JUX\")\n" +
                                    "    );\n" +
                                    "  }\n" +
                                    "}")
                            )
                        )
                    )
                )
            )
        );
    }

    private Element featuresSection() {
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("text-center", "mb-5").children(
                    h2().cls("fw-bold").text(messages().getString("features.title")),
                    p().cls("text-secondary", "lead").text(messages().getString("features.subtitle"))
                ),
                div().cls("row", "g-4").children(
                    featureCard("bi-code-slash",
                        messages().getString("features.java.title"),
                        messages().getString("features.java.desc")),
                    featureCard("bi-universal-access",
                        messages().getString("features.ada.title"),
                        messages().getString("features.ada.desc")),
                    featureCard("bi-lightning-charge",
                        messages().getString("features.ssr.title"),
                        messages().getString("features.ssr.desc")),
                    featureCard("bi-gear",
                        messages().getString("features.spring.title"),
                        messages().getString("features.spring.desc")),
                    featureCard("bi-translate",
                        messages().getString("features.i18n.title"),
                        messages().getString("features.i18n.desc")),
                    featureCard("bi-palette",
                        messages().getString("features.themes.title"),
                        messages().getString("features.themes.desc"))
                )
            )
        );
    }

    private Element featureCard(String icon, String title, String description) {
        return div().cls("col-md-6", "col-lg-4").children(
            div().cls("card", "h-100", "border-0", "shadow-sm").children(
                div().cls("card-body", "p-4").children(
                    div().cls("mb-3").children(
                        Element.of("i").cls(icon, "fs-1", "text-primary")
                            .ariaHidden(true)
                    ),
                    h3().cls("h5", "card-title").text(title),
                    p().cls("card-text", "text-secondary").text(description)
                )
            )
        );
    }

    private Element codeExampleSection() {
        return section().cls("bg-light", "py-5").children(
            div().cls("container").children(
                div().cls("row", "align-items-center").children(
                    div().cls("col-lg-6", "mb-4", "mb-lg-0").children(
                        h2().cls("fw-bold", "mb-3").text(messages().getString("code.title")),
                        p().cls("text-secondary", "mb-4").text(messages().getString("code.subtitle")),
                        ul().cls("list-unstyled").children(
                            checkItem(messages().getString("code.check1")),
                            checkItem(messages().getString("code.check2")),
                            checkItem(messages().getString("code.check3")),
                            checkItem(messages().getString("code.check4"))
                        )
                    ),
                    div().cls("col-lg-6").children(
                        div().cls("bg-dark", "text-white", "rounded-3", "p-4").children(
                            pre().cls("mb-0").children(
                                code().cls("small").text(
                                    "@Route(\"/blog/{slug}\")\n" +
                                    "public class BlogPost extends Component {\n\n" +
                                    "  @PathParam private String slug;\n" +
                                    "  @Autowired private BlogRepo repo;\n\n" +
                                    "  @Override\n" +
                                    "  public PageMeta pageMeta() {\n" +
                                    "    var post = repo.findBySlug(slug);\n" +
                                    "    return PageMeta.create()\n" +
                                    "      .title(post.getTitle())\n" +
                                    "      .ogImage(post.getCover());\n" +
                                    "  }\n\n" +
                                    "  @Override\n" +
                                    "  public Element render() {\n" +
                                    "    var post = repo.findBySlug(slug);\n" +
                                    "    return article().children(\n" +
                                    "      h1().text(post.getTitle()),\n" +
                                    "      p().text(post.getBody())\n" +
                                    "    );\n" +
                                    "  }\n" +
                                    "}")
                            )
                        )
                    )
                )
            )
        );
    }

    private Element checkItem(String text) {
        return li().cls("mb-2").children(
            Element.of("i").cls("bi-check-circle-fill", "text-success", "me-2").ariaHidden(true),
            span().text(text)
        );
    }

    private Element ctaSection() {
        return section().cls("py-5").children(
            div().cls("container", "text-center").children(
                h2().cls("fw-bold", "mb-3").text(messages().getString("cta.title")),
                p().cls("lead", "text-secondary", "mb-4").text(messages().getString("cta.subtitle")),
                div().cls("d-flex", "justify-content-center", "gap-3").children(
                    a().cls("btn", "btn-primary", "btn-lg").attr("href", "/contact")
                        .text(messages().getString("cta.primary")),
                    a().cls("btn", "btn-outline-secondary", "btn-lg").attr("href", "/blog")
                        .text(messages().getString("cta.secondary"))
                )
            )
        );
    }
}
