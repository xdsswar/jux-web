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

package xss.it.jux.server.render;

import xss.it.jux.annotation.*;
import xss.it.jux.core.CssResource;
import xss.it.jux.core.JsResource;
import xss.it.jux.core.PageMeta;
import xss.it.jux.server.WebApplication;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the final {@link PageMeta} for a page by merging annotation-declared
 * metadata with the programmatic metadata returned by
 * {@link xss.it.jux.core.Page#pageMeta()}.
 *
 * <p><b>Resolution strategy:</b></p>
 * <ol>
 *   <li>Collect application-level {@code @Css}/{@code @Js} from the
 *       {@link xss.it.jux.server.WebApplication} implementor (global defaults)</li>
 *   <li>Build a baseline {@link PageMeta} from page class-level annotations:
 *       {@code @Title}, {@code @Meta}, {@code @Canonical}, {@code @Favicon},
 *       {@code @Css}, {@code @Js} — page-level resources override application-level
 *       for the same path</li>
 *   <li>If the {@link WebApplication#defaultPageMeta()} returned a non-null value,
 *       merge it on top of the annotation baseline (app-wide programmatic defaults)</li>
 *   <li>If the page's {@code pageMeta()} returned a non-null value,
 *       merge it on top — page-level overrides everything</li>
 *   <li>Programmatic values <b>override</b> their annotation equivalents;
 *       annotation values serve as defaults for fields not set programmatically</li>
 * </ol>
 *
 * <p>This class is thread-safe. It is registered as a Spring bean by
 * {@link xss.it.jux.server.autoconfigure.JuxAutoConfiguration}.</p>
 *
 * @see PageMeta
 * @see xss.it.jux.server.WebApplication
 */
@org.springframework.stereotype.Component
public class MetadataResolver {

    /** The application class (the @SpringBootApplication class implementing WebApplication). */
    private Class<?> applicationClass;

    /** The WebApplication instance for calling {@link WebApplication#defaultPageMeta()}. */
    private WebApplication webApplication;

    /**
     * Set the application class whose {@code @Css}/{@code @Js} annotations
     * serve as global defaults for all pages.
     *
     * @param applicationClass the WebApplication implementor class, or null if not used
     */
    public void setApplicationClass(Class<?> applicationClass) {
        this.applicationClass = applicationClass;
    }

    /**
     * Set the WebApplication instance for application-wide programmatic defaults.
     *
     * <p>The instance's {@link WebApplication#defaultPageMeta()} is called during
     * metadata resolution and merged after annotation metadata but before the
     * page's own {@code pageMeta()} override.</p>
     *
     * @param webApplication the WebApplication instance, or null if not used
     */
    public void setWebApplication(WebApplication webApplication) {
        this.webApplication = webApplication;
    }

    /**
     * Resolve the final PageMeta by merging annotations and programmatic metadata.
     *
     * <p>The merge pipeline (each step overrides the previous):</p>
     * <ol>
     *   <li>Application-level {@code @Css}/{@code @Js} annotations</li>
     *   <li>Page-level annotations ({@code @Title}, {@code @Meta}, {@code @Css}, {@code @Js})</li>
     *   <li>{@link WebApplication#defaultPageMeta()} — app-wide programmatic baseline</li>
     *   <li>Page's {@code pageMeta()} — page-specific overrides (highest priority)</li>
     * </ol>
     *
     * @param componentClass the component class (for annotation scanning)
     * @param programmatic   the PageMeta returned by page.pageMeta() (may be null)
     * @return the merged PageMeta, never null
     */
    public PageMeta resolve(Class<?> componentClass, PageMeta programmatic) {
        PageMeta base = fromAnnotations(componentClass);

        // Merge application-wide programmatic defaults (between annotations and page pageMeta)
        if (webApplication != null) {
            PageMeta appDefaults = webApplication.defaultPageMeta();
            if (appDefaults != null) {
                base.merge(appDefaults);
            }
        }

        // Merge page-specific programmatic metadata (highest priority)
        if (programmatic != null) {
            base.merge(programmatic);
        }
        return base;
    }

    /**
     * Build a baseline {@link PageMeta} by scanning class-level annotations.
     *
     * <p>CSS and JS resources are collected into maps keyed by path for
     * deduplication. Application-level resources are collected first,
     * then page-hierarchy resources override same-path entries.</p>
     *
     * @param leafClass the component class to scan for metadata annotations
     * @return a new {@link PageMeta} populated from the class annotations, never null
     */
    private PageMeta fromAnnotations(Class<?> leafClass) {
        PageMeta meta = PageMeta.create();

        // Use maps for CSS/JS to deduplicate by path (last-write-wins).
        // Application-level resources go in first, page-level override same paths.
        Map<String, CssResource> cssMap = new LinkedHashMap<>();
        Map<String, JsResource> jsMap = new LinkedHashMap<>();

        // 1. Application-level annotations (global defaults, lowest priority).
        //    All metadata annotations are scanned here so that setting them on the
        //    WebApplication class acts as a site-wide default. Page-level annotations
        //    (step 2) override these via last-write-wins.
        if (applicationClass != null) {
            for (Class<?> clazz : classHierarchy(applicationClass)) {
                scanCssJs(clazz, cssMap, jsMap);
                scanMetadata(clazz, meta);
            }
        }

        // 2. Walk the page class hierarchy from superclass to subclass so that
        //    subclass annotations override superclass ones (last-write-wins).
        //    Page-level annotations override application-level ones set in step 1.
        for (Class<?> clazz : classHierarchy(leafClass)) {
            scanMetadata(clazz, meta);
            scanCssJs(clazz, cssMap, jsMap);
        }

        // 3. Add deduped resources to meta
        cssMap.values().forEach(meta::css);
        jsMap.values().forEach(meta::js);

        return meta;
    }

    /**
     * Scan a single class for metadata annotations: {@code @Title}, {@code @Meta},
     * {@code @Canonical}, and {@code @Favicon}. Values are applied to the given
     * {@link PageMeta} with last-write-wins semantics (later calls override earlier).
     */
    private void scanMetadata(Class<?> clazz, PageMeta meta) {
        Title title = clazz.getDeclaredAnnotation(Title.class);
        if (title != null) {
            meta.title(title.value());
        }

        Meta[] metas = clazz.getDeclaredAnnotationsByType(Meta.class);
        for (Meta m : metas) {
            if (!m.name().isEmpty()) {
                meta.meta(m.name(), m.content());
            }
            if (!m.property().isEmpty()) {
                meta.metaProperty(m.property(), m.content());
            }
            if (!m.httpEquiv().isEmpty()) {
                meta.httpEquiv(m.httpEquiv(), m.content());
            }
        }

        Canonical canonical = clazz.getDeclaredAnnotation(Canonical.class);
        if (canonical != null && !canonical.value().isEmpty()) {
            meta.canonical(canonical.value());
        }

        Favicon favicon = clazz.getDeclaredAnnotation(Favicon.class);
        if (favicon != null) {
            meta.favicon(favicon.value(), favicon.type(), favicon.sizes());
        }
    }

    /**
     * Scan a single class for {@code @Css} and {@code @Js} annotations and
     * add them to the dedup maps. Later calls override earlier entries with
     * the same path (last-write-wins via {@link LinkedHashMap}).
     */
    private void scanCssJs(Class<?> clazz, Map<String, CssResource> cssMap, Map<String, JsResource> jsMap) {
        Css[] cssAnnotations = clazz.getDeclaredAnnotationsByType(Css.class);
        for (Css css : cssAnnotations) {
            cssMap.put(css.value(), new CssResource(
                css.value(), css.position(), css.order(), css.media(),
                css.async(), css.integrity(), css.condition()
            ));
        }

        Js[] jsAnnotations = clazz.getDeclaredAnnotationsByType(Js.class);
        for (Js js : jsAnnotations) {
            jsMap.put(js.value(), new JsResource(
                js.value(), js.position(), js.order(), js.async(),
                js.defer(), js.module(), js.integrity(), js.condition()
            ));
        }
    }

    /**
     * Walk the class hierarchy from the topmost superclass down to the leaf class.
     * Superclass annotations are applied first, subclass annotations override.
     */
    private List<Class<?>> classHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }
        return hierarchy;
    }
}
