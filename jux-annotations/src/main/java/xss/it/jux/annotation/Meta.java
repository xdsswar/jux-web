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
 * Adds an HTML {@code <meta>} tag to the page {@code <head>} section.
 *
 * <p>This annotation is repeatable -- use multiple {@code @Meta} annotations on a single
 * component to declare several meta tags. The framework renders each annotation as a
 * {@code <meta>} element with the specified attributes.</p>
 *
 * <h2>Meta Tag Variants</h2>
 * <p>Each {@code @Meta} annotation produces one of three {@code <meta>} tag forms,
 * depending on which attribute is set:</p>
 * <ul>
 *   <li>{@code <meta name="..." content="...">} -- standard meta tags for SEO,
 *       description, keywords, robots directives, etc.</li>
 *   <li>{@code <meta property="..." content="...">} -- OpenGraph protocol tags
 *       for social media link previews (Facebook, LinkedIn, WhatsApp)</li>
 *   <li>{@code <meta http-equiv="..." content="...">} -- HTTP-equivalent headers
 *       for Content-Type, refresh, X-UA-Compatible, etc.</li>
 * </ul>
 *
 * <p>The {@link #content()} value supports i18n message keys using the {@code #{key}}
 * syntax, which are resolved against the current locale's message bundle at render time.</p>
 *
 * <p>Meta tags declared via this annotation can be overridden or supplemented
 * programmatically using {@code PageMeta.meta()} in the component's {@code pageMeta()} method.</p>
 *
 * <p><b>Example -- SEO and OpenGraph tags:</b></p>
 * <pre>{@code
 * @Route("/about")
 * @Title("About Us")
 * @Meta(name = "description", content = "Learn about our company and mission")
 * @Meta(name = "keywords", content = "company, about, mission, team")
 * @Meta(property = "og:type", content = "website")
 * @Meta(property = "og:image", content = "https://example.com/og-image.jpg")
 * @Meta(name = "robots", content = "noindex", condition = "#{profile == 'staging'}")
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * @see Title
 * @see ServerMeta
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Meta.List.class)
@Documented
public @interface Meta {

    /**
     * The {@code name} attribute of the {@code <meta>} tag.
     *
     * <p>Used for standard HTML meta tags such as {@code "description"},
     * {@code "keywords"}, {@code "robots"}, {@code "author"}, or
     * {@code "viewport"}. Mutually exclusive with {@link #property()} and
     * {@link #httpEquiv()} -- only one should be set per annotation.</p>
     *
     * @return the meta name, or empty if using {@link #property()} or {@link #httpEquiv()}
     */
    String name() default "";

    /**
     * The {@code property} attribute of the {@code <meta>} tag, used for OpenGraph tags.
     *
     * <p>OpenGraph tags control how the page appears when shared on social media
     * platforms. Common properties: {@code "og:title"}, {@code "og:description"},
     * {@code "og:image"}, {@code "og:type"}, {@code "og:url"},
     * {@code "og:site_name"}.</p>
     *
     * @return the OpenGraph property name, or empty if using {@link #name()} or {@link #httpEquiv()}
     */
    String property() default "";

    /**
     * The {@code content} attribute value of the {@code <meta>} tag.
     *
     * <p>This is the actual value of the meta tag. Supports i18n message keys
     * using the {@code "#{key}"} prefix syntax, which are resolved against the
     * current locale's message bundle at render time.</p>
     *
     * <p>Examples: {@code "About our company"}, {@code "noindex,nofollow"},
     * {@code "#{meta.description}"}.</p>
     *
     * @return the meta content value or i18n message key
     */
    String content();

    /**
     * The {@code http-equiv} attribute of the {@code <meta>} tag.
     *
     * <p>Used for HTTP header equivalents that the browser should respect.
     * Common values: {@code "Content-Type"}, {@code "refresh"},
     * {@code "X-UA-Compatible"}, {@code "Content-Security-Policy"}.</p>
     *
     * @return the HTTP-equiv directive name, or empty if using {@link #name()} or {@link #property()}
     */
    String httpEquiv() default "";

    /**
     * Spring Expression Language (SpEL) condition for conditional inclusion.
     *
     * <p>When set, the meta tag is only rendered if this SpEL expression evaluates
     * to {@code true}. This enables environment-specific meta tags, such as
     * {@code "noindex"} on staging environments.</p>
     *
     * <p>An empty string (the default) means the meta tag is always included.</p>
     *
     * @return the SpEL condition expression, or empty for unconditional inclusion
     */
    String condition() default "";

    /**
     * Container annotation for repeatable {@code @Meta} annotations.
     *
     * <p>This is used internally by the Java compiler to hold multiple {@code @Meta}
     * annotations on a single type. Developers should not use this directly -- simply
     * apply multiple {@code @Meta} annotations to the same class.</p>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * The array of {@link Meta} annotations applied to the annotated type.
         *
         * @return the Meta annotations
         */
        Meta[] value();
    }
}
