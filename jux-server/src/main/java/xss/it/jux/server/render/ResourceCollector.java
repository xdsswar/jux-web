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

import xss.it.jux.annotation.Css;
import xss.it.jux.annotation.CssPosition;
import xss.it.jux.annotation.Js;
import xss.it.jux.annotation.JsPosition;
import xss.it.jux.core.CssResource;
import xss.it.jux.core.JsResource;
import xss.it.jux.core.PageMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gathers CSS and JavaScript resources from annotation declarations and
 * programmatic {@link PageMeta}, deduplicates them by path, and sorts
 * them by their declared order.
 *
 * <p>This collector implements the JUX resource pipeline:</p>
 * <ol>
 *   <li>Collect {@code @Css}/{@code @Js} annotations from the component class
 *       (and potentially its layout and child components)</li>
 *   <li>Add resources declared programmatically via {@link PageMeta#css(String)}
 *       and {@link PageMeta#js(String)}</li>
 *   <li>Remove resources excluded via {@link PageMeta#removeCss(String)}
 *       and {@link PageMeta#removeJs(String)}</li>
 *   <li>Deduplicate by resource path (later declarations override earlier ones)</li>
 *   <li>Sort by declared order (ascending) within each position partition</li>
 * </ol>
 *
 * <p>Resources are keyed by their path, so a {@code PageMeta} resource with the
 * same path as an annotation-declared resource will replace the annotation version.
 * This allows programmatic overrides of annotation-level resource configuration.</p>
 *
 * @see CssResource
 * @see JsResource
 * @see PageMeta
 */
@org.springframework.stereotype.Component
public class ResourceCollector {

    /** The application class (the @SpringBootApplication class implementing WebApplication). */
    private Class<?> applicationClass;

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
     * Collect all CSS resources for a page component class, merged with
     * any programmatic CSS from the component's {@link PageMeta}.
     *
     * <p>The collection process:</p>
     * <ol>
     *   <li>Scan the application class for {@code @Css} annotations (global defaults)</li>
     *   <li>Scan the component class for {@code @Css} annotations (repeatable)</li>
     *   <li>Add any CSS resources declared in the {@code PageMeta}</li>
     *   <li>Remove any CSS paths listed in {@code PageMeta.removeCss()}</li>
     *   <li>Deduplicate by path (last-write-wins via {@link LinkedHashMap})</li>
     *   <li>Sort the final list by {@link CssResource#order()} ascending</li>
     * </ol>
     *
     * @param componentClass the {@code @Route}-annotated component class to scan for {@code @Css} annotations
     * @param meta           the programmatic {@link PageMeta} from the component's {@code pageMeta()} method;
     *                       may be {@code null} if the component does not provide programmatic metadata
     * @return an unmodifiable list of CSS resources sorted by order, never null
     */
    public List<CssResource> collectCss(Class<?> componentClass, PageMeta meta) {
        Map<String, CssResource> resources = new LinkedHashMap<>();

        // 0. Application-level annotations (global defaults, lowest priority)
        if (applicationClass != null) {
            for (Class<?> cls : classHierarchy(applicationClass)) {
                Css[] cssAnnotations = cls.getDeclaredAnnotationsByType(Css.class);
                for (Css css : cssAnnotations) {
                    resources.put(css.value(), new CssResource(
                        css.value(), css.position(), css.order(), css.media(),
                        css.async(), css.integrity(), css.condition()
                    ));
                }
            }
        }

        // 1. Collect from annotations (walk class hierarchy: superclass first, subclass overrides)
        for (Class<?> cls : classHierarchy(componentClass)) {
            Css[] cssAnnotations = cls.getDeclaredAnnotationsByType(Css.class);
            for (Css css : cssAnnotations) {
                resources.put(css.value(), new CssResource(
                    css.value(), css.position(), css.order(), css.media(),
                    css.async(), css.integrity(), css.condition()
                ));
            }
        }

        // 2. Add from PageMeta
        if (meta != null) {
            for (CssResource res : meta.getCssResources()) {
                resources.put(res.path(), res);
            }
            // 3. Remove excluded
            for (String removed : meta.getRemovedCss()) {
                resources.remove(removed);
            }
        }

        // 4. Sort by order
        return resources.values().stream()
            .sorted(Comparator.comparingInt(CssResource::order))
            .collect(Collectors.toList());
    }

    /**
     * Collect all JavaScript resources for a page component class, merged with
     * any programmatic JS from the component's {@link PageMeta}.
     *
     * <p>The collection process mirrors {@link #collectCss(Class, PageMeta)}:</p>
     * <ol>
     *   <li>Scan the component class for {@code @Js} annotations (repeatable)</li>
     *   <li>Add any JS resources declared in the {@code PageMeta}</li>
     *   <li>Remove any JS paths listed in {@code PageMeta.removeJs()}</li>
     *   <li>Deduplicate by path (last-write-wins via {@link LinkedHashMap})</li>
     *   <li>Sort the final list by {@link JsResource#order()} ascending</li>
     * </ol>
     *
     * @param componentClass the {@code @Route}-annotated component class to scan for {@code @Js} annotations
     * @param meta           the programmatic {@link PageMeta} from the component's {@code pageMeta()} method;
     *                       may be {@code null} if the component does not provide programmatic metadata
     * @return an unmodifiable list of JS resources sorted by order, never null
     */
    public List<JsResource> collectJs(Class<?> componentClass, PageMeta meta) {
        Map<String, JsResource> resources = new LinkedHashMap<>();

        // 0. Application-level annotations (global defaults, lowest priority)
        if (applicationClass != null) {
            for (Class<?> cls : classHierarchy(applicationClass)) {
                Js[] jsAnnotations = cls.getDeclaredAnnotationsByType(Js.class);
                for (Js js : jsAnnotations) {
                    resources.put(js.value(), new JsResource(
                        js.value(), js.position(), js.order(), js.async(),
                        js.defer(), js.module(), js.integrity(), js.condition()
                    ));
                }
            }
        }

        // 1. Collect from annotations (walk class hierarchy: superclass first, subclass overrides)
        for (Class<?> cls : classHierarchy(componentClass)) {
            Js[] jsAnnotations = cls.getDeclaredAnnotationsByType(Js.class);
            for (Js js : jsAnnotations) {
                resources.put(js.value(), new JsResource(
                    js.value(), js.position(), js.order(), js.async(),
                    js.defer(), js.module(), js.integrity(), js.condition()
                ));
            }
        }

        // 2. Add from PageMeta
        if (meta != null) {
            for (JsResource res : meta.getJsResources()) {
                resources.put(res.path(), res);
            }
            // 3. Remove excluded
            for (String removed : meta.getRemovedJs()) {
                resources.remove(removed);
            }
        }

        // 4. Sort by order
        return resources.values().stream()
            .sorted(Comparator.comparingInt(JsResource::order))
            .collect(Collectors.toList());
    }

    /**
     * Partition a list of CSS resources by their injection position.
     *
     * <p>Separates resources into those that should be injected in the
     * {@code <head>} section ({@link CssPosition#HEAD}) and those that
     * should be injected at the end of {@code <body>}
     * ({@link CssPosition#BODY_END}).</p>
     *
     * @param resources the collected CSS resources to partition
     * @return a map from {@link CssPosition} to the list of resources at that position
     */
    public Map<CssPosition, List<CssResource>> partitionCss(List<CssResource> resources) {
        return resources.stream().collect(Collectors.groupingBy(CssResource::position));
    }

    /**
     * Partition a list of JavaScript resources by their injection position.
     *
     * <p>Separates resources into those that should be injected in the
     * {@code <head>} section ({@link JsPosition#HEAD}) and those that
     * should be injected at the end of {@code <body>}
     * ({@link JsPosition#BODY_END}, the default and recommended position).</p>
     *
     * @param resources the collected JS resources to partition
     * @return a map from {@link JsPosition} to the list of resources at that position
     */
    public Map<JsPosition, List<JsResource>> partitionJs(List<JsResource> resources) {
        return resources.stream().collect(Collectors.groupingBy(JsResource::position));
    }

    /**
     * Walk the class hierarchy from the topmost superclass down to the given class,
     * collecting only classes that are subclasses of {@link xss.it.jux.core.Component}.
     * Superclass annotations are collected first so subclass annotations can override
     * (same path = last-write-wins via LinkedHashMap).
     *
     * @param clazz the leaf class to start from
     * @return a list of classes ordered from topmost ancestor to the leaf class
     */
    private List<Class<?>> classHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current); // prepend so superclass comes first
            current = current.getSuperclass();
        }
        return hierarchy;
    }
}
