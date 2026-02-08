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

package xss.it.jux.html.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a component field to a named content slot in the HTML template.
 *
 * <p>A slot is an element identified by its {@code id} attribute in the template.
 * The slot name maps to the element id. A value of {@code "default"} maps to the
 * element with {@code id="default"} in the template.</p>
 *
 * <p>Slots allow components to inject dynamic content into predefined locations
 * within a template. The injected {@link xss.it.jux.core.Element} is a live
 * reference into the cloned template tree.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * // Template:
 * // <main>
 * //   <aside id="sidebar">...</aside>
 * //   <div id="default">...</div>
 * // </main>
 *
 * @Slot private Element defaultSlot;           // maps to id="default"
 * @Slot("sidebar") private Element sidebar;    // maps to id="sidebar"
 * }</pre>
 *
 * @see Html
 * @see xss.it.jux.html.HtmlLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Slot {

    /**
     * The named content slot in the template.
     *
     * <p>Maps to the {@code id} attribute of an element in the HTML template.
     * Default value is {@code "default"}, meaning it looks for an element
     * with {@code id="default"}.</p>
     *
     * @return the slot name (element id in the template)
     */
    String value() default "default";
}
