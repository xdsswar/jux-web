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
 * Injects an {@link xss.it.jux.core.Element} from the parsed HTML template
 * into a component field by matching the element's {@code id} attribute.
 *
 * <p>If {@link #value()} is empty (the default), the field name is used as
 * the element id to look up. The injected Element is a live reference into the
 * cloned template tree -- mutations to it affect the rendered output.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * // Template: <div id="title-bar">...</div>
 *
 * @HtmlId private Element titleBar;          // looks for id="titleBar"
 * @HtmlId("title-bar") private Element bar;  // looks for id="title-bar"
 * }</pre>
 *
 * @see Html
 * @see xss.it.jux.html.HtmlLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface HtmlId {

    /**
     * The HTML element id to inject.
     *
     * <p>If empty (default), the Java field name is used as the id to look up
     * in the parsed template.</p>
     *
     * @return the element id, or empty to use the field name
     */
    String value() default "";
}
