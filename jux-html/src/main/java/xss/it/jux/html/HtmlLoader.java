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

package xss.it.jux.html;

import xss.it.jux.core.Element;
import xss.it.jux.html.annotation.Html;
import xss.it.jux.html.annotation.HtmlId;
import xss.it.jux.html.annotation.Slot;
import xss.it.jux.reactive.Initializable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrator that loads, parses, caches, and wires HTML templates into JUX components.
 *
 * <p>The loading pipeline for a component annotated with {@link Html @Html}:</p>
 * <ol>
 *   <li>Read the {@code @Html} annotation to determine the template path</li>
 *   <li>Check the cache for a previously parsed prototype</li>
 *   <li>If not cached: load raw HTML from the classpath ({@code templates/} directory)</li>
 *   <li>Parse the HTML into an {@link Element} tree via {@link HtmlParser}</li>
 *   <li>Cache the prototype for future requests</li>
 *   <li>Deep-clone the prototype (each caller gets an independent copy)</li>
 *   <li>Build an index of all elements by their {@code id} attribute</li>
 *   <li>Inject {@link HtmlId @HtmlId}-annotated fields with matching elements</li>
 *   <li>Inject {@link Slot @Slot}-annotated fields with matching elements</li>
 *   <li>If the component implements {@link Initializable}, call {@code initialize()}</li>
 *   <li>Return the root Element</li>
 * </ol>
 *
 * <p><b>Example usage in a component:</b></p>
 * <pre>{@code
 * @Html("pages/dashboard.html")
 * public class DashboardPage extends Component {
 *     @HtmlId private Element userCount;
 *     @HtmlId("recent-activity") private Element activity;
 *     @Slot("sidebar") private Element sidebar;
 *
 *     @Override
 *     public Element render() {
 *         Element root = HtmlLoader.load(this);
 *         userCount.text("42 users online");
 *         return root;
 *     }
 * }
 * }</pre>
 *
 * @see Html
 * @see HtmlId
 * @see Slot
 * @see HtmlParser
 * @see TemplateCache
 */
public final class HtmlLoader {

    /** Shared template cache instance. */
    private static final TemplateCache CACHE = new TemplateCache();

    /** Classpath directory prefix where templates are loaded from. */
    private static final String TEMPLATES_DIR = "templates/";

    private HtmlLoader() {
        /* Utility class -- not instantiable. */
    }

    // ── Public API ────────────────────────────────────────────────

    /**
     * Load and process an HTML template for the given component.
     *
     * <p>The component's class must be annotated with {@link Html @Html} to specify
     * which template to load. Fields annotated with {@link HtmlId @HtmlId} and
     * {@link Slot @Slot} are injected with matching elements from the parsed tree.
     * If the component implements {@link Initializable}, its {@code initialize()}
     * method is called after all injection is complete.</p>
     *
     * @param component the component instance to load the template for
     * @return the root Element of the processed template tree
     * @throws TemplateException if the template cannot be found, parsed, or wired
     * @throws NullPointerException if component is null
     */
    public static Element load(Object component) {
        if (component == null) {
            throw new NullPointerException("component must not be null");
        }

        /* 1. Read @Html annotation to get template path. */
        Class<?> componentClass = component.getClass();
        Html htmlAnnotation = componentClass.getAnnotation(Html.class);
        if (htmlAnnotation == null) {
            throw new TemplateException(
                    "Component " + componentClass.getName() + " is not annotated with @Html",
                    null, -1);
        }

        String templatePath = htmlAnnotation.value();
        if (templatePath == null || templatePath.isBlank()) {
            throw new TemplateException(
                    "Empty template path in @Html annotation on " + componentClass.getName(),
                    null, -1);
        }

        /* 2. Check cache for previously parsed prototype. */
        Element root = CACHE.get(templatePath);

        if (root == null) {
            /* 3. Load raw HTML from classpath. */
            String rawHtml = loadTemplate(templatePath);

            /* 4. Parse HTML into Element tree. */
            Element prototype = HtmlParser.parse(rawHtml, templatePath);

            /* 5. Cache the prototype. */
            CACHE.put(templatePath, prototype);

            /* 6. Clone the prototype. */
            root = TemplateCache.deepClone(prototype);
        }
        /* If cache hit, root is already a deep clone from CACHE.get(). */

        /* 7. Index all elements by id. */
        Map<String, Element> idIndex = indexById(root);

        /* 8. Inject @HtmlId fields. */
        injectHtmlIds(component, idIndex);

        /* 9. Inject @Slot fields. */
        injectSlots(component, idIndex);

        /* 10. Call Initializable.initialize() if applicable. */
        if (component instanceof Initializable initializable) {
            initializable.initialize();
        }

        /* 11. Return root Element. */
        return root;
    }

    /**
     * Returns the shared template cache.
     *
     * <p>Use this to configure caching behavior (enable/disable, clear).</p>
     *
     * @return the template cache instance
     */
    public static TemplateCache getCache() {
        return CACHE;
    }

    // ── Internal helpers ──────────────────────────────────────────

    /**
     * Load the raw HTML content of a template from the classpath.
     *
     * <p>Templates are loaded from the {@code templates/} directory on the classpath.
     * The entire file is read as UTF-8 text.</p>
     *
     * @param path the template path relative to templates/ (e.g. "pages/home.html")
     * @return the raw HTML content
     * @throws TemplateException if the template file cannot be found or read
     */
    static String loadTemplate(String path) {
        String resourcePath = TEMPLATES_DIR + path;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = HtmlLoader.class.getClassLoader();
        }

        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new TemplateException(
                        "Template not found on classpath: " + resourcePath,
                        path, -1);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new TemplateException(
                    "Failed to read template: " + resourcePath,
                    path, -1, e);
        }
    }

    /**
     * Build a map of element id to Element by walking the entire tree.
     *
     * <p>Uses a breadth-first traversal. If multiple elements share the same id
     * (invalid HTML but possible in practice), the first one encountered wins.</p>
     *
     * @param root the root element of the tree
     * @return map of id attribute value to Element
     */
    static Map<String, Element> indexById(Element root) {
        Map<String, Element> index = new LinkedHashMap<>();
        if (root == null) {
            return index;
        }

        Deque<Element> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Element current = queue.poll();

            /* Check for id attribute. */
            String id = current.getAttributes().get("id");
            if (id != null && !id.isEmpty()) {
                index.putIfAbsent(id, current);
            }

            /* Enqueue children. */
            for (Element child : current.getChildren()) {
                queue.add(child);
            }
        }

        return index;
    }

    /**
     * Inject elements into fields annotated with {@link HtmlId @HtmlId}.
     *
     * <p>Walks the component class hierarchy (including superclasses) looking for
     * {@code @HtmlId} fields. For each field, determines the target element id
     * (from the annotation value or the field name) and injects the corresponding
     * element from the id index.</p>
     *
     * @param component the component instance
     * @param idIndex   map of element id to Element
     * @throws TemplateException if a required element id is not found in the template
     */
    static void injectHtmlIds(Object component, Map<String, Element> idIndex) {
        Class<?> clazz = component.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                HtmlId annotation = field.getAnnotation(HtmlId.class);
                if (annotation == null) {
                    continue;
                }

                /* Determine the element id to look up. */
                String targetId = annotation.value();
                if (targetId == null || targetId.isEmpty()) {
                    targetId = field.getName();
                }

                Element element = idIndex.get(targetId);
                if (element == null) {
                    throw new TemplateException(
                            "No element with id=\"" + targetId + "\" found in template for @HtmlId field '"
                                    + field.getName() + "' in " + component.getClass().getName(),
                            getTemplatePath(component), -1);
                }

                injectField(component, field, element);
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Inject elements into fields annotated with {@link Slot @Slot}.
     *
     * <p>Walks the component class hierarchy looking for {@code @Slot} fields.
     * The slot value maps to an element id in the template.</p>
     *
     * @param component the component instance
     * @param idIndex   map of element id to Element
     * @throws TemplateException if a required slot element is not found in the template
     */
    static void injectSlots(Object component, Map<String, Element> idIndex) {
        Class<?> clazz = component.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                Slot annotation = field.getAnnotation(Slot.class);
                if (annotation == null) {
                    continue;
                }

                String slotName = annotation.value();
                Element element = idIndex.get(slotName);

                if (element == null) {
                    throw new TemplateException(
                            "No element with id=\"" + slotName + "\" found in template for @Slot field '"
                                    + field.getName() + "' in " + component.getClass().getName(),
                            getTemplatePath(component), -1);
                }

                injectField(component, field, element);
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Reflectively set a field value on the component instance.
     *
     * @param component the target object
     * @param field     the field to set
     * @param value     the value to inject
     * @throws TemplateException if the field cannot be set
     */
    private static void injectField(Object component, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(component, value);
        } catch (IllegalAccessException e) {
            throw new TemplateException(
                    "Cannot inject field '" + field.getName() + "' in "
                            + component.getClass().getName() + ": " + e.getMessage(),
                    getTemplatePath(component), -1, e);
        }
    }

    /**
     * Extract the template path from the component's {@code @Html} annotation.
     * Returns null if the annotation is not present.
     */
    private static String getTemplatePath(Object component) {
        Html html = component.getClass().getAnnotation(Html.class);
        return html != null ? html.value() : null;
    }
}
