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
 * Conditionally injects a {@code <meta>} tag based on a server-side Spring Expression
 * Language (SpEL) condition.
 *
 * <p>This annotation is repeatable and is specifically designed for environment-dependent
 * meta tags that should vary between deployment environments (e.g., development, staging,
 * production). Unlike {@link Meta#condition()}, which provides optional conditional
 * behavior on a general-purpose meta annotation, {@code @ServerMeta} makes the condition
 * a required element, emphasizing its environment-gating purpose.</p>
 *
 * <p>A common use case is controlling search engine indexing per environment: staging
 * sites should have {@code noindex,nofollow} to prevent indexing, while production
 * sites should have {@code index,follow}.</p>
 *
 * <p><b>Example -- environment-specific robots directives:</b></p>
 * <pre>{@code
 * @Route("/about")
 * @ServerMeta(condition = "#{env == 'staging'}", name = "robots", content = "noindex,nofollow")
 * @ServerMeta(condition = "#{env == 'prod'}", name = "robots", content = "index,follow")
 * public class AboutPage extends Component { ... }
 * }</pre>
 *
 * @see Meta
 * @see Title
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ServerMeta.List.class)
@Documented
public @interface ServerMeta {

    /**
     * SpEL condition that must evaluate to {@code true} for this meta tag to be rendered.
     *
     * <p>The expression is evaluated at render time against the Spring application context.
     * If the condition evaluates to {@code false}, the meta tag is omitted from the output.
     * Examples: {@code "#{env == 'staging'}"}, {@code "#{profile == 'prod'}"}</p>
     *
     * @return the SpEL condition expression
     */
    String condition();

    /**
     * The {@code name} attribute of the {@code <meta>} tag.
     *
     * <p>Common values: {@code "robots"}, {@code "googlebot"}, {@code "description"}.</p>
     *
     * @return the meta name attribute value
     */
    String name();

    /**
     * The {@code content} attribute value of the {@code <meta>} tag.
     *
     * <p>Examples: {@code "noindex,nofollow"}, {@code "index,follow"},
     * {@code "max-image-preview:large"}.</p>
     *
     * @return the meta content value
     */
    String content();

    /**
     * Container annotation for repeatable {@code @ServerMeta} annotations.
     *
     * <p>This is used internally by the Java compiler to hold multiple {@code @ServerMeta}
     * annotations on a single type. Developers should not use this directly -- simply
     * apply multiple {@code @ServerMeta} annotations to the same class.</p>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * The array of {@link ServerMeta} annotations applied to the annotated type.
         *
         * @return the ServerMeta annotations
         */
        ServerMeta[] value();
    }
}
