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
import xss.it.jux.annotation.PathParam;
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
import java.util.Optional;

import static xss.it.jux.core.Elements.*;

/**
 * Framework-provided catch-all route for CMS-managed pages.
 *
 * <p>This component resolves database-stored {@link PageDefinition} records by their
 * URL slug and renders their widget trees. It runs at the <b>lowest priority</b>
 * ({@code Integer.MAX_VALUE - 1}) so that any {@code @Route}-annotated component
 * in the consumer project takes precedence over CMS pages. This means developers
 * can always override a CMS page by creating a Java component with the same path.</p>
 *
 * <p><b>Request flow:</b></p>
 * <ol>
 *   <li>A request arrives at {@code /{slug}} (e.g. {@code /about}, {@code /services/web-design})</li>
 *   <li>The route resolver extracts the slug from the URL and injects it into
 *       the {@link #slug} field via {@code @PathParam}</li>
 *   <li>The locale is resolved from the URL prefix (e.g. {@code /es/about})
 *       and injected into the {@link #locale} field via {@code @LocaleParam}</li>
 *   <li>{@link #pageMeta()} is called first -- it loads the page from the database,
 *       checks publication status, and builds metadata (title, description, OG tags)</li>
 *   <li>{@link #render()} is called next -- it renders the page's widget list into
 *       an Element tree</li>
 *   <li>If the slug does not match any published page, a 404 response is returned
 *       with a "Page Not Found" message</li>
 * </ol>
 *
 * <p><b>Multi-language support:</b> The {@code @Localized} annotation enables
 * automatic locale prefix registration. The page's localized content is resolved
 * using the current locale with fallback to the system default locale.</p>
 *
 * <p><b>Configuration:</b> The consumer project configures CMS behavior in
 * {@code application.yml}:</p>
 * <pre>{@code
 * jux:
 *   cms:
 *     home-slug: "home"
 *     error-slug: "404"
 *     base-path: ""
 * }</pre>
 *
 * @see PageService
 * @see WidgetRenderer
 * @see PageDefinition
 * @see CmsHomeRoute
 */
@Route(value = "/{slug:regex(.+)}", priority = Integer.MAX_VALUE - 1)
@Localized
public class CmsPageRoute extends Page {

    /**
     * The URL slug extracted from the request path.
     *
     * <p>The regex pattern {@code .+} matches any path including slashes,
     * allowing nested slugs like {@code "services/web-design"}. The slug
     * is used to look up the {@link PageDefinition} in the database.</p>
     */
    @PathParam
    private String slug;

    /**
     * The resolved locale for the current request.
     *
     * <p>Determined by the i18n locale resolution chain: URL prefix, cookie,
     * session, Accept-Language header, or default. Used to select the correct
     * {@link LocalizedContent} from the page definition.</p>
     */
    @LocaleParam
    private Locale locale;

    /**
     * Service for loading CMS page definitions from the database.
     */
    @Autowired
    private PageService pageService;

    /**
     * Service for rendering individual widget instances to Element trees.
     */
    @Autowired
    private WidgetRenderer widgetRenderer;

    /**
     * Build page metadata from the database-stored page definition.
     *
     * <p>This method runs <b>before</b> {@link #render()} in the SSR lifecycle.
     * It sets the HTTP status code, page title, meta description, OpenGraph tags,
     * HTML language, and text direction.</p>
     *
     * <p>If the slug does not match any published page in the database, this method
     * returns a {@link PageMeta} with a 404 status and a "Page Not Found" title.
     * The renderer will still call {@link #render()} to produce the 404 page body.</p>
     *
     * @return page metadata built from the database content, or 404 metadata if
     *         the page is not found or not published; never null
     */
    @Override
    public PageMeta pageMeta() {
        /* Attempt to load the page by slug. */
        Optional<PageDefinition> page = pageService.findBySlug(slug);

        /*
         * Return 404 metadata if the page does not exist or is not published.
         * Draft pages are only visible in the admin panel preview, not to
         * public visitors.
         */
        if (page.isEmpty() || !page.get().published()) {
            return PageMeta.create()
                    .status(404)
                    .title("Page Not Found");
        }

        PageDefinition def = page.get();

        /*
         * Resolve the localized content for the current locale, falling back
         * to the system default locale if no content exists for the requested
         * locale. This ensures pages always render even when translations are
         * incomplete.
         */
        LocalizedContent content = def.content(locale, pageService.defaultLocale());

        /*
         * Build page metadata from the localized content fields. The htmlLang
         * and autoDir settings ensure proper language declaration and text
         * direction (LTR/RTL) on the <html> element.
         */
        PageMeta meta = PageMeta.create()
                .title(content.title())
                .description(content.description())
                .htmlLang(locale.getLanguage())
                .autoDir();

        /* Set OpenGraph tags if the content provides them. */
        if (content.ogImage() != null) {
            meta.ogImage(content.ogImage());
        }
        if (content.ogImageAlt() != null) {
            meta.ogImageAlt(content.ogImageAlt());
        }

        /* Use the title as the OG title for social media link previews. */
        meta.ogTitle(content.title());

        /* Use the description as the OG description. */
        if (content.description() != null) {
            meta.ogDescription(content.description());
        }

        return meta;
    }

    /**
     * Render the page's widget tree from the database.
     *
     * <p>Loads the page by slug, resolves the localized widget list, and renders
     * each widget instance through the {@link WidgetRenderer}. The resulting
     * elements are collected into a {@code <main>} container with
     * {@code id="main-content"} for skip-navigation anchor targeting.</p>
     *
     * <p>If the page is not found or not published, a minimal 404 error page
     * is rendered with a heading and descriptive message.</p>
     *
     * @return the root Element of the page, always a {@code <main>} element
     */
    @Override
    public Element render() {
        /* Attempt to load the page by slug. */
        Optional<PageDefinition> page = pageService.findBySlug(slug);

        /*
         * Render a 404 error page if the page does not exist or is not
         * published. This matches the 404 status set in pageMeta().
         */
        if (page.isEmpty() || !page.get().published()) {
            return main_().id("main-content").children(
                    section().cls("error-page").children(
                            h1().text("404"),
                            p().text("Page not found.")
                    )
            );
        }

        PageDefinition def = page.get();

        /* Resolve the localized content for the current locale with fallback. */
        LocalizedContent content = def.content(locale, pageService.defaultLocale());

        /*
         * Render each widget instance in order. The WidgetRenderer handles
         * type resolution, component instantiation, prop injection, styling,
         * and accessibility annotation for each widget.
         */
        List<Element> widgetElements = content.widgets().stream()
                .map(widgetRenderer::render)
                .toList();

        /* Wrap all rendered widgets in a <main> element for semantic structure. */
        return main_().id("main-content").children(widgetElements);
    }
}
