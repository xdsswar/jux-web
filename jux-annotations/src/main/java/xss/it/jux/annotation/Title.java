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

package xss.it.jux.annotation;

import java.lang.annotation.*;

/**
 * Sets the page {@code <title>} tag in the HTML {@code <head>}.
 *
 * <p>The title is rendered inside the {@code <title>} element and is visible in browser
 * tabs, bookmarks, and search engine results. It is also required for WCAG 2.2 AA
 * compliance (Success Criterion 2.4.2 -- Page Titled).</p>
 *
 * <h2>Static vs. Dynamic Titles</h2>
 * <ul>
 *   <li><b>Static:</b> Set a literal string: {@code @Title("About Us")}</li>
 *   <li><b>i18n:</b> Use a message key prefix: {@code @Title("#{about.title}")} --
 *       the framework resolves the key against the current locale's message bundle</li>
 *   <li><b>Dynamic:</b> Override programmatically via {@code PageMeta.title()} in
 *       the component's {@code pageMeta()} method. Programmatic values override
 *       this annotation.</li>
 * </ul>
 *
 * <p><b>Example -- static title:</b></p>
 * <pre>{@code
 * @Route("/about")
 * @Title("About Us")
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- i18n title:</b></p>
 * <pre>{@code
 * @Route("/about")
 * @Title("#{about.page.title}")
 * @Localized
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * @see Route
 * @see Meta
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Title {

    /**
     * The page title text, or an i18n message key.
     *
     * <p>Literal strings are used as-is. Strings prefixed with {@code "#{"}
     * and suffixed with {@code "}"} are treated as message bundle keys and
     * resolved against the current request's locale at render time.</p>
     *
     * <p>This value can be overridden at runtime by returning a non-null
     * {@code PageMeta} from the component's {@code pageMeta()} method with
     * {@code .title(...)} set.</p>
     *
     * @return the page title text or i18n message key
     */
    String value();
}
