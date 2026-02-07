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
import xss.it.jux.store.data.StoreData;

import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Store home page with hero, featured products, categories, promo, and newsletter.
 *
 * <p>This is the landing page of the JUX Store demo. It showcases
 * multiple content sections composed as reusable components — all
 * built in pure Java with Tailwind CSS styling.</p>
 */
@Route("/")
@Title("JUX Store - Shop the Best Products Online")
@Meta(name = "description", content = "JUX Store demo - a complete e-commerce website built entirely in Java with the JUX framework and Tailwind CSS")
public class HomePage extends Page {

    @Override
    public PageMeta pageMeta() {
        return PageMeta.create()
                .ogTitle("JUX Store")
                .ogDescription("A complete e-commerce demo built in pure Java")
                .ogType("website");
    }

    @Override
    public Element render() {
        return new StoreLayout("/", messages(),
                div().children(
                        heroSection(),
                        featuredProductsSection(),
                        categoriesSection(),
                        new PromoBanner(messages()).render(),
                        whyShopSection(),
                        new NewsletterSection(messages()).render()
                )
        ).render();
    }

    /**
     * Hero section with large heading, subheading, and CTA buttons.
     */
    private Element heroSection() {
        var m = messages();
        return section().cls("bg-gradient-to-br", "from-indigo-600", "to-indigo-800",
                        "text-white", "py-20")
                .children(
                        div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8")
                                .children(
                                        div().cls("max-w-3xl").children(
                                                h1().cls("text-4xl", "sm:text-5xl", "font-bold",
                                                                "mb-4", "leading-tight")
                                                        .text(m.getString("home.hero.title")),
                                                p().cls("text-xl", "text-indigo-200", "mb-8")
                                                        .text(m.getString("home.hero.subtitle")),
                                                div().cls("flex", "flex-wrap", "gap-4").children(
                                                        a().cls("bg-white", "text-indigo-600", "px-8",
                                                                        "py-3", "rounded-lg", "font-semibold",
                                                                        "hover:bg-indigo-50", "transition-colors",
                                                                        "focus:ring-2", "focus:ring-white",
                                                                        "focus:ring-offset-2",
                                                                        "focus:ring-offset-indigo-600")
                                                                .attr("href", "/products")
                                                                .text(m.getString("home.hero.cta.primary")),
                                                        a().cls("border-2", "border-white", "text-white",
                                                                        "px-8", "py-3", "rounded-lg",
                                                                        "font-semibold", "hover:bg-white",
                                                                        "hover:text-indigo-600", "transition-colors",
                                                                        "focus:ring-2", "focus:ring-white",
                                                                        "focus:ring-offset-2",
                                                                        "focus:ring-offset-indigo-600")
                                                                .attr("href", "/categories")
                                                                .text(m.getString("home.hero.cta.secondary"))
                                                )
                                        )
                                )
                );
    }

    /**
     * Featured products section showing top-picked items.
     */
    private Element featuredProductsSection() {
        var m = messages();
        var featured = StoreData.featuredProducts();

        return section().cls("py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        div().cls("text-center", "mb-10").children(
                                h2().cls("text-3xl", "font-bold", "text-gray-900", "mb-2")
                                        .text(m.getString("home.featured.title")),
                                p().cls("text-gray-500", "text-lg")
                                        .text(m.getString("home.featured.subtitle"))
                        ),
                        new ProductGrid(featured).render(),
                        div().cls("text-center", "mt-8").children(
                                a().cls("text-indigo-600", "font-semibold", "hover:text-indigo-700")
                                        .attr("href", "/products")
                                        .text(m.getString("home.featured.view_all") + " \u2192")
                        )
                )
        );
    }

    /**
     * Category browsing section with cards.
     */
    private Element categoriesSection() {
        var m = messages();
        var categories = StoreData.allCategories();

        var cards = categories.stream()
                .map(c -> new CategoryCard(c).render())
                .toList();

        return section().cls("bg-gray-50", "py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        div().cls("text-center", "mb-10").children(
                                h2().cls("text-3xl", "font-bold", "text-gray-900", "mb-2")
                                        .text(m.getString("home.categories.title")),
                                p().cls("text-gray-500", "text-lg")
                                        .text(m.getString("home.categories.subtitle"))
                        ),
                        div().cls("grid", "grid-cols-1", "sm:grid-cols-2", "lg:grid-cols-5", "gap-6")
                                .children(cards)
                )
        );
    }

    /**
     * "Why Shop With Us" section with value proposition cards.
     */
    private Element whyShopSection() {
        var m = messages();
        return section().cls("py-16").children(
                div().cls("max-w-7xl", "mx-auto", "px-4", "sm:px-6", "lg:px-8").children(
                        div().cls("text-center", "mb-10").children(
                                h2().cls("text-3xl", "font-bold", "text-gray-900", "mb-2")
                                        .text(m.getString("home.why.title"))
                        ),
                        div().cls("grid", "grid-cols-1", "md:grid-cols-3", "gap-8").children(
                                valueCard("\uD83D\uDE9A",
                                        m.getString("home.why.shipping.title"),
                                        m.getString("home.why.shipping.text")),
                                valueCard("\uD83D\uDD12",
                                        m.getString("home.why.secure.title"),
                                        m.getString("home.why.secure.text")),
                                valueCard("\uD83D\uDD04",
                                        m.getString("home.why.returns.title"),
                                        m.getString("home.why.returns.text"))
                        )
                )
        );
    }

    private Element valueCard(String icon, String title, String text) {
        return div().cls("text-center", "p-6").children(
                div().cls("text-4xl", "mb-3").ariaHidden(true).text(icon),
                h3().cls("font-semibold", "text-gray-900", "mb-2").text(title),
                p().cls("text-gray-500", "text-sm").text(text)
        );
    }
}
