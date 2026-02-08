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
 * Associates a component with an HTML template file.
 *
 * <p>The template path is resolved relative to the {@code templates/} directory
 * on the classpath. During loading, the HTML is parsed into a JUX
 * {@link xss.it.jux.core.Element} tree, cached for reuse, and then wired
 * into the component via {@link HtmlId} and {@link Slot} field annotations.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @Html("pages/home.html")
 * public class HomePage extends Component {
 *     @HtmlId private Element heroSection;
 *     @Slot("sidebar") private Element sidebar;
 *
 *     @Override
 *     public Element render() {
 *         heroSection.text("Welcome!");
 *         return HtmlLoader.load(this);
 *     }
 * }
 * }</pre>
 *
 * @see HtmlId
 * @see Slot
 * @see xss.it.jux.html.HtmlLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Html {

    /**
     * Path to the HTML template file, relative to the {@code templates/} directory
     * on the classpath.
     *
     * <p>Example: {@code "pages/home.html"} resolves to
     * {@code classpath:templates/pages/home.html}.</p>
     *
     * @return the template path
     */
    String value();
}
