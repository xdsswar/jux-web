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
 * Gallery page demonstrating accessible image handling.
 *
 * <p>Showcases the difference between {@code img(src, alt)} for meaningful
 * images and {@code imgDecorative(src)} for purely decorative ones.
 * All images use {@code <figure>}/{@code <figcaption>} for proper semantics.</p>
 */
@Route("/gallery")
@Title("Gallery - JUX Demo")
@Meta(name = "description", content = "Image gallery demo - accessible images with alt text in JUX")
public class GalleryPage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create();
    }

    private record GalleryItem(String src, String alt, String caption, String category, boolean decorative) {}

    private static final List<GalleryItem> IMAGES = List.of(
        new GalleryItem("https://picsum.photos/seed/nature1/600/400",
            "Sunlight filtering through a dense forest canopy",
            "Forest Canopy", "Nature", false),
        new GalleryItem("https://picsum.photos/seed/arch1/600/400",
            "Modern glass skyscraper reflecting clouds against a blue sky",
            "Glass Tower", "Architecture", false),
        new GalleryItem("https://picsum.photos/seed/abstract1/600/400",
            "", "Abstract Texture", "Abstract", true),
        new GalleryItem("https://picsum.photos/seed/nature2/600/400",
            "Calm lake at dawn with mountains reflected in still water",
            "Mountain Lake", "Nature", false),
        new GalleryItem("https://picsum.photos/seed/people1/600/400",
            "Software developer working at a standing desk with dual monitors",
            "Developer Workspace", "People", false),
        new GalleryItem("https://picsum.photos/seed/arch2/600/400",
            "Historic stone bridge with three arches spanning a river",
            "Stone Bridge", "Architecture", false),
        new GalleryItem("https://picsum.photos/seed/abstract2/600/400",
            "", "Color Gradient", "Abstract", true),
        new GalleryItem("https://picsum.photos/seed/nature3/600/400",
            "Vibrant wildflower meadow stretching to the horizon under a clear sky",
            "Wildflower Meadow", "Nature", false),
        new GalleryItem("https://picsum.photos/seed/abstract3/600/400",
            "", "Geometric Pattern", "Abstract", true)
    );

    private static final List<String> CATEGORIES = List.of("Nature", "Architecture", "Abstract", "People");

    @Override
    public Element render() {
        return new PageLayout("/gallery", messages(), pageContent()).render();
    }

    private String activeCategory() {
        String cat = queryParam("category");
        if (cat != null && CATEGORIES.stream().anyMatch(c -> c.equalsIgnoreCase(cat))) {
            return cat.substring(0, 1).toUpperCase() + cat.substring(1).toLowerCase();
        }
        return null; // null means "All"
    }

    private Element pageContent() {
        return div().children(
            headerSection(),
            filterSection(),
            gridSection(),
            accessibilityNote()
        );
    }

    private Element headerSection() {
        var m = messages();
        return section().cls("bg-primary", "text-white", "py-5").children(
            div().cls("container", "py-3").children(
                h1().cls("display-5", "fw-bold").text(m.getString("gallery.header.title")),
                p().cls("lead", "mb-0", "opacity-75").text(m.getString("gallery.header.subtitle"))
            )
        );
    }

    private Element filterSection() {
        var m = messages();
        String active = activeCategory();
        List<Element> badges = new java.util.ArrayList<>();
        badges.add(filterBadge(m.getString("gallery.filter.all"), "/gallery", active == null));
        for (String cat : CATEGORIES) {
            badges.add(filterBadge(cat, "/gallery?category=" + cat, cat.equals(active)));
        }
        return section().cls("py-4", "border-bottom").children(
            div().cls("container").children(
                nav().aria("label", m.getString("gallery.filter.label")).children(
                    div().cls("d-flex", "flex-wrap", "gap-2", "align-items-center").children(
                        span().cls("text-secondary", "me-2", "fw-semibold", "small").text(m.getString("gallery.filter")),
                        div().cls("d-flex", "flex-wrap", "gap-2").children(badges)
                    )
                )
            )
        );
    }

    private Element filterBadge(String label, String href, boolean active) {
        String bg = active ? "bg-primary" : "bg-secondary";
        Element badge = a().attr("href", href)
            .cls("badge", "rounded-pill", bg, "px-3", "py-2", "text-decoration-none")
            .text(label);
        if (active) {
            badge = badge.ariaCurrent("true");
        }
        return badge;
    }

    private Element gridSection() {
        String active = activeCategory();
        List<GalleryItem> filtered = active == null
            ? IMAGES
            : IMAGES.stream().filter(i -> i.category().equals(active)).toList();
        return section().cls("py-5").children(
            div().cls("container").children(
                div().cls("row", "g-4").children(
                    filtered.stream().map(this::galleryCard).toList()
                )
            )
        );
    }

    private Element galleryCard(GalleryItem item) {
        var m = messages();
        Element image = item.decorative()
            ? imgDecorative(item.src()).cls("card-img-top")
            : img(item.src(), item.alt()).cls("card-img-top");

        Element badge = item.decorative()
            ? span().cls("badge", "bg-warning", "text-dark", "me-2").text(m.getString("gallery.badge.decorative"))
            : span().cls("badge", "bg-success", "me-2").text(m.getString("gallery.badge.alt"));

        return div().cls("col-md-6", "col-lg-4").children(
            div().cls("card", "mb-4", "shadow-sm", "h-100").children(
                figure().cls("mb-0").children(
                    image,
                    figcaption().cls("card-body").children(
                        div().cls("d-flex", "justify-content-between", "align-items-center", "mb-2").children(
                            h2().cls("h6", "fw-bold", "mb-0").text(item.caption()),
                            badge
                        ),
                        div().cls("d-flex", "justify-content-between", "align-items-center").children(
                            span().cls("badge", "bg-light", "text-dark", "border").text(item.category()),
                            item.decorative()
                                ? small().cls("text-muted", "fst-italic").text("alt=\"\" role=\"presentation\"")
                                : small().cls("text-muted", "fst-italic").text(
                                    item.alt().length() > 40
                                        ? "alt=\"" + item.alt().substring(0, 37) + "...\""
                                        : "alt=\"" + item.alt() + "\"")
                        )
                    )
                )
            )
        );
    }

    private Element accessibilityNote() {
        var m = messages();
        return section().cls("pb-5").children(
            div().cls("container").children(
                div().cls("alert", "alert-info", "border-0", "shadow-sm").role("alert").children(
                    div().cls("d-flex", "align-items-start").children(
                        Element.of("i").cls("bi", "bi-info-circle-fill", "me-3", "mt-1", "fs-5")
                            .ariaHidden(true),
                        div().children(
                            h2().cls("h6", "fw-bold", "mb-2").text(m.getString("gallery.a11y.title")),
                            p().cls("mb-2").text(m.getString("gallery.a11y.intro")),
                            ul().cls("mb-2").children(
                                li().children(
                                    code().text("img(src, alt)"),
                                    span().text(m.getString("gallery.a11y.img.desc"))
                                ),
                                li().children(
                                    code().text("imgDecorative(src)"),
                                    span().text(m.getString("gallery.a11y.decorative.desc"))
                                )
                            ),
                            p().cls("mb-0", "small", "text-secondary").text(m.getString("gallery.a11y.note"))
                        )
                    )
                )
            )
        );
    }
}
