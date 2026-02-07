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

package xss.it.jux.cms.service;

import xss.it.jux.cms.model.PageDefinition;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing CMS page definitions.
 *
 * <p>This service is the primary interface for CRUD operations on
 * {@link PageDefinition} records. It provides methods to find pages by slug,
 * retrieve the designated home page, save new pages, list all pages, and
 * delete pages.</p>
 *
 * <p><b>Current implementation:</b> This is a structural implementation using
 * in-memory storage via a {@link ConcurrentHashMap}. In a production system,
 * this would be backed by Spring Data JPA repositories persisting to a relational
 * database (MySQL, PostgreSQL). The in-memory approach is suitable for development,
 * testing, and prototyping.</p>
 *
 * <p><b>Thread safety:</b> The underlying {@code ConcurrentHashMap} provides
 * thread-safe reads and writes without external synchronization, making this
 * service safe for concurrent access from multiple request threads.</p>
 *
 * <p><b>Configuration:</b> The home page slug and default locale are configured
 * in {@code application.yml}:</p>
 * <pre>{@code
 * jux:
 *   cms:
 *     home-slug: "home"
 *   i18n:
 *     default-locale: "en"
 * }</pre>
 *
 * @see PageDefinition
 * @see xss.it.jux.cms.routing.CmsPageRoute
 * @see xss.it.jux.cms.routing.CmsHomeRoute
 */
@Service
public class PageService {

    /**
     * In-memory page storage keyed by slug.
     *
     * <p>Uses {@link ConcurrentHashMap} for thread-safe concurrent access
     * from multiple request-handling threads. In production, this would be
     * replaced by a JPA repository.</p>
     */
    private final Map<String, PageDefinition> pages = new ConcurrentHashMap<>();

    /**
     * The slug of the page designated as the home page.
     *
     * <p>Configured via {@code jux.cms.home-slug} in {@code application.yml}.
     * Defaults to {@code "home"} if not configured. The
     * {@link xss.it.jux.cms.routing.CmsHomeRoute} uses this to determine
     * which page to serve at the root URL ({@code /}).</p>
     */
    private String homeSlug = "home";

    /**
     * The default locale used for content fallback.
     *
     * <p>When a page does not have content for the requested locale, the
     * system falls back to this locale. Defaults to {@code Locale.ENGLISH}.</p>
     */
    private Locale defaultLocaleValue = Locale.ENGLISH;

    /**
     * Find a page by its URL slug.
     *
     * <p>Performs a case-sensitive lookup of the slug in the page store.
     * Returns an empty Optional if no page with the given slug exists.</p>
     *
     * @param slug the URL slug to search for (e.g. {@code "about"},
     *             {@code "services/web-design"})
     * @return the matching page definition, or empty if not found
     */
    public Optional<PageDefinition> findBySlug(String slug) {
        return Optional.ofNullable(pages.get(slug));
    }

    /**
     * Find the designated home page, or throw if it does not exist.
     *
     * <p>Looks up the page whose slug matches the configured home slug
     * (default: {@code "home"}). This method is used by
     * {@link xss.it.jux.cms.routing.CmsHomeRoute} to serve the root URL.</p>
     *
     * @return the home page definition, never null
     * @throws NoSuchElementException if no page with the configured home slug exists
     */
    public PageDefinition findHomePageOrThrow() {
        /*
         * Look up the home page by its configured slug. If no page is found,
         * throw an informative exception indicating which slug was expected.
         */
        PageDefinition home = pages.get(homeSlug);
        if (home == null) {
            throw new NoSuchElementException(
                    "CMS home page not found. Expected a page with slug '" + homeSlug
                    + "'. Configure a different slug via jux.cms.home-slug or create "
                    + "a page with slug '" + homeSlug + "'.");
        }
        return home;
    }

    /**
     * Returns the default locale for content fallback.
     *
     * <p>Used by route components when calling
     * {@link PageDefinition#content(Locale, Locale)} to provide the fallback
     * locale parameter.</p>
     *
     * @return the default locale, never null
     */
    public Locale defaultLocale() {
        return defaultLocaleValue;
    }

    /**
     * Save a page definition to the store.
     *
     * <p>If a page with the same slug already exists, it is replaced.
     * This method is used by the admin panel to create new pages and
     * update existing ones.</p>
     *
     * @param page the page definition to save; the {@link PageDefinition#slug()}
     *             is used as the storage key
     * @throws NullPointerException if page or page.slug() is null
     */
    public void save(PageDefinition page) {
        pages.put(page.slug(), page);
    }

    /**
     * Retrieve all page definitions.
     *
     * <p>Returns an unmodifiable list of all pages in the store, in no
     * guaranteed order. Used by the admin panel to display the page list.</p>
     *
     * @return an unmodifiable list of all page definitions; empty if no pages exist
     */
    public List<PageDefinition> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(pages.values()));
    }

    /**
     * Delete a page by its slug.
     *
     * <p>Removes the page with the given slug from the store. If no page
     * with that slug exists, this method is a no-op.</p>
     *
     * @param slug the slug of the page to delete
     */
    public void delete(String slug) {
        pages.remove(slug);
    }

    /**
     * Set the home page slug.
     *
     * <p>Called during configuration to set which page slug serves as the
     * home page at the root URL ({@code /}). Typically set from the
     * {@code jux.cms.home-slug} configuration property.</p>
     *
     * @param homeSlug the slug of the home page
     */
    public void setHomeSlug(String homeSlug) {
        this.homeSlug = homeSlug;
    }

    /**
     * Set the default locale for content fallback.
     *
     * <p>Called during configuration to set the fallback locale. Typically
     * set from the {@code jux.i18n.default-locale} configuration property.</p>
     *
     * @param locale the default locale
     */
    public void setDefaultLocale(Locale locale) {
        this.defaultLocaleValue = locale;
    }
}
