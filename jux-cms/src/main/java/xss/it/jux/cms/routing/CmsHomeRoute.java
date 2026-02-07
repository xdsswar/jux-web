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

package xss.it.jux.cms.routing;

import xss.it.jux.annotation.Localized;
import xss.it.jux.annotation.LocaleParam;
import xss.it.jux.annotation.Route;
import xss.it.jux.cms.model.LocalizedContent;
import xss.it.jux.cms.model.PageDefinition;
import xss.it.jux.cms.service.PageService;
import xss.it.jux.cms.service.WidgetRenderer;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.Page;
import xss.it.jux.core.PageMeta;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import static xss.it.jux.core.Elements.*;

/**
 * Home page route -- serves the CMS page designated as the home page at {@code /}.
 *
 * <p>This component always serves the root URL ({@code /}) with the page whose slug
 * matches the configured {@code jux.cms.home-slug} property (default: {@code "home"}).
 * It has <b>high priority</b> ({@code 1}) to ensure it always wins the route matching
 * for the root path, even when other catch-all routes are registered.</p>
 *
 * <p><b>Relationship to CmsPageRoute:</b> While {@link CmsPageRoute} handles all
 * slug-based CMS pages at {@code /{slug}}, this route specifically handles the root
 * URL. This separation is needed because the home page slug (e.g. {@code "home"})
 * should not appear in the URL -- visitors should see {@code /} not {@code /home}.</p>
 *
 * <p><b>Request flow:</b></p>
 * <ol>
 *   <li>A request arrives at {@code /} (or {@code /es/}, {@code /fr/} for localized variants)</li>
 *   <li>The locale is resolved and injected via {@code @LocaleParam}</li>
 *   <li>{@link #pageMeta()} loads the home page from the database and builds metadata</li>
 *   <li>{@link #render()} renders the home page's widget tree</li>
 *   <li>If no home page is configured, an error is thrown with a descriptive message</li>
 * </ol>
 *
 * <p><b>Multi-language support:</b> The {@code @Localized} annotation enables
 * automatic locale prefix registration. The route becomes available at {@code /},
 * {@code /es/}, {@code /fr/}, etc., depending on the configured locales.</p>
 *
 * <p><b>Configuration:</b></p>
 * <pre>{@code
 * jux:
 *   cms:
 *     home-slug: "home"      # Which PageDefinition slug serves "/"
 * }</pre>
 *
 * @see CmsPageRoute
 * @see PageService
 * @see WidgetRenderer
 * @see PageDefinition
 */
@Route(value = "/", priority = 1)
@Localized
public class CmsHomeRoute extends Page {

    /**
     * The resolved locale for the current request.
     *
     * <p>Determined by the i18n locale resolution chain. Used to select
     * the correct {@link LocalizedContent} from the home page definition.</p>
     */
    @LocaleParam
    private Locale locale;

    /**
     * Service for loading CMS page definitions from the database.
     *
     * <p>Used to retrieve the designated home page via
     * {@link PageService#findHomePageOrThrow()}.</p>
     */
    @Autowired
    private PageService pageService;

    /**
     * Service for rendering individual widget instances to Element trees.
     */
    @Autowired
    private WidgetRenderer widgetRenderer;

    /**
     * Build page metadata from the home page's database content.
     *
     * <p>Loads the designated home page via {@link PageService#findHomePageOrThrow()},
     * resolves the localized content for the current request locale, and builds
     * a {@link PageMeta} with the page title, description, language, and text
     * direction.</p>
     *
     * <p>If no home page is configured in the database, the
     * {@link PageService#findHomePageOrThrow()} call throws a
     * {@link NoSuchElementException}. In production, this would result in
     * a 500 error page -- the developer must create a page with the configured
     * home slug.</p>
     *
     * @return page metadata built from the home page's localized content; never null
     * @throws NoSuchElementException if no page with the configured home slug exists
     */
    @Override
    public PageMeta pageMeta() {
        /* Load the home page or throw if it doesn't exist. */
        PageDefinition home = pageService.findHomePageOrThrow();

        /*
         * Resolve the localized content for the current locale.
         * Falls back to the default locale if no content exists for
         * the requested locale.
         */
        LocalizedContent content = home.content(locale, pageService.defaultLocale());

        /*
         * Build metadata from the localized content. The home page typically
         * has a welcoming title and a site-level description.
         */
        return PageMeta.create()
                .title(content.title())
                .description(content.description())
                .htmlLang(locale.getLanguage())
                .autoDir();
    }

    /**
     * Render the home page's widget tree.
     *
     * <p>Loads the home page, resolves the localized widget list for the current
     * locale, and renders each widget instance through the {@link WidgetRenderer}.
     * The resulting elements are collected into a {@code <main>} container.</p>
     *
     * @return the root Element of the home page, always a {@code <main>} element
     *         with {@code id="main-content"} for skip-navigation targeting
     * @throws NoSuchElementException if no page with the configured home slug exists
     */
    @Override
    public Element render() {
        /* Load the home page. */
        PageDefinition home = pageService.findHomePageOrThrow();

        /* Resolve localized content with fallback to the default locale. */
        LocalizedContent content = home.content(locale, pageService.defaultLocale());

        /*
         * Render each widget instance in the page's widget list. The widgets
         * are rendered in their stored order, which determines the visual
         * top-to-bottom flow of the page.
         */
        List<Element> widgetElements = content.widgets().stream()
                .map(widgetRenderer::render)
                .toList();

        /* Wrap all rendered widgets in a semantic <main> element. */
        return main_().id("main-content").children(widgetElements);
    }
}
