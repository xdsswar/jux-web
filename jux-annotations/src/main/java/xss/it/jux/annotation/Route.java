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
 * Maps a {@code Component} to a URL path, turning it into a routable page.
 *
 * <p>Any Component annotated with {@code @Route} becomes a routable page in the
 * JUX framework. The framework auto-discovers all {@code @Route}-annotated classes
 * at startup, registers them with Spring's {@code DispatcherServlet}, and handles
 * server-side rendering (SSR), parameter injection, security checks, and response
 * caching automatically.</p>
 *
 * <h2>Path Patterns</h2>
 * <ul>
 *   <li>Static: {@code "/about"}</li>
 *   <li>Path parameters: {@code "/blog/{slug}"}</li>
 *   <li>Typed parameters: {@code "/users/{id:long}"}, {@code "/items/{id:uuid}"}</li>
 *   <li>Regex captures: {@code "/files/{path:regex(.+)}"}</li>
 *   <li>Wildcards: {@code "/docs/**"} -- matches any depth</li>
 * </ul>
 *
 * <h2>Request Dispatch Flow</h2>
 * <ol>
 *   <li>Spring's {@code DispatcherServlet} delegates to {@code JuxRouteHandlerMapping}</li>
 *   <li>Locale is resolved (URL prefix, cookie, session, Accept-Language header)</li>
 *   <li>Path and query parameters are extracted and type-coerced</li>
 *   <li>Security roles are checked (if configured)</li>
 *   <li>SSR cache is consulted (if {@code cacheTtl > 0})</li>
 *   <li>The Component is instantiated via Spring DI, and parameters are injected</li>
 *   <li>{@code pageMeta()} and {@code render()} are called to produce the HTML</li>
 *   <li>The accessibility audit runs (in development mode)</li>
 *   <li>The HTML response is returned (and cached if applicable)</li>
 * </ol>
 *
 * <p><b>Example -- basic page:</b></p>
 * <pre>{@code
 * @Route("/")
 * @Title("Home")
 * @Css(value = "themes/default.css", order = 1)
 * public class HomePage extends Component {
 *     @Override
 *     public Element render() {
 *         return main_().children(
 *             h1().text("Welcome")
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example -- path parameters with caching:</b></p>
 * <pre>{@code
 * @Route(value = "/blog/{slug}", cacheTtl = 3600)
 * @Title("Blog Post")
 * public class BlogPostPage extends Component {
 *     @PathParam private String slug;
 *     // ...
 * }
 * }</pre>
 *
 * <p><b>Example -- secured admin route:</b></p>
 * <pre>{@code
 * @Route(value = "/admin/dashboard", roles = {"ROLE_ADMIN"})
 * @Layout(AdminLayout.class)
 * @Title("Admin Dashboard")
 * public class AdminDashboardPage extends Component { ... }
 * }</pre>
 *
 * <p><b>Example -- 404 catch-all with lowest priority:</b></p>
 * <pre>{@code
 * @Route(value = "/**", priority = Integer.MAX_VALUE)
 * @Title("Page Not Found")
 * public class NotFoundPage extends Component { ... }
 * }</pre>
 *
 * @see PathParam
 * @see QueryParam
 * @see Localized
 * @see Layout
 * @see Title
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Route {

    /**
     * URL path pattern for this route.
     *
     * <p>Supports several pattern syntaxes:</p>
     * <ul>
     *   <li>Static paths: {@code "/about"}, {@code "/contact"}</li>
     *   <li>Path variables: {@code "/blog/{slug}"} -- extracted via {@link PathParam}</li>
     *   <li>Typed variables: {@code "/users/{id:long}"}, {@code "/items/{id:uuid}"},
     *       {@code "/events/{date:date}"} -- auto-coerced to the target field type</li>
     *   <li>Regex captures: {@code "/files/{path:regex(.+)}"} -- matches complex patterns</li>
     *   <li>Wildcards: {@code "/docs/**"} -- matches any number of path segments</li>
     * </ul>
     *
     * @return the URL path pattern
     */
    String value();

    /**
     * HTTP methods this route responds to.
     *
     * <p>By default, only GET requests are handled. Add POST for form handling
     * or other methods as needed:</p>
     * <pre>{@code
     * @Route(value = "/contact", methods = {HttpMethod.GET, HttpMethod.POST})
     * }</pre>
     *
     * @return the allowed HTTP methods; defaults to {@code {HttpMethod.GET}}
     */
    HttpMethod[] methods() default {HttpMethod.GET};

    /**
     * Named identifier for this route, used in reverse URL generation.
     *
     * <p>If empty (the default), the simple class name is used as the route name
     * (e.g., {@code "BlogPostPage"}). The name is used with
     * {@code JuxRouter.url("blog-post", params)} to generate URLs programmatically.</p>
     *
     * @return the route name, or empty string to use the class simple name
     */
    String name() default "";

    /**
     * Layout component class that wraps this page.
     *
     * <p>The layout's {@code render()} method is called with this page's content
     * injected as a child element, providing shared chrome such as headers, footers,
     * and navigation bars. Set to {@code Void.class} (the default) to indicate no
     * layout wrapping, or rely on the default layout configured via
     * {@code jux.cms.default-layout}.</p>
     *
     * <p>This can also be set via the separate {@link Layout} annotation.</p>
     *
     * @return the layout Component class, or {@code Void.class} for no layout
     * @see Layout
     */
    Class<?> layout() default Void.class;

    /**
     * The Content-Type header sent with the HTTP response.
     *
     * <p>Defaults to {@code "text/html; charset=UTF-8"} which is appropriate for
     * standard HTML page responses. Override for special cases such as XML sitemaps
     * or JSON API responses rendered through the component system.</p>
     *
     * @return the Content-Type header value
     */
    String produces() default "text/html; charset=UTF-8";

    /**
     * Server-side rendering (SSR) cache time-to-live in seconds.
     *
     * <p>When greater than zero, the rendered HTML output is cached in an in-memory
     * Caffeine cache for the specified duration. The cache key includes the full path,
     * query parameters, and resolved locale, ensuring different variants are cached
     * independently.</p>
     *
     * <p>Use for pages with content that rarely changes (e.g., about pages, pricing
     * pages). Set to {@code 0} (the default) to disable caching for dynamic pages.</p>
     *
     * @return cache TTL in seconds; {@code 0} disables caching
     */
    int cacheTtl() default 0;

    /**
     * Spring Security roles required to access this route.
     *
     * <p>When non-empty, the framework enforces authentication and verifies that the
     * current user has at least one of the specified roles before rendering the page.
     * Unauthenticated users are redirected to the login page; authenticated users
     * without the required roles receive a 403 Forbidden response.</p>
     *
     * <p>An empty array (the default) means the route is public -- no authentication
     * required.</p>
     *
     * <pre>{@code
     * @Route(value = "/admin/users", roles = {"ROLE_ADMIN"})
     * }</pre>
     *
     * @return the required role names; empty for public access
     */
    String[] roles() default {};

    /**
     * Route matching priority -- lower numbers indicate higher priority.
     *
     * <p>When multiple routes could match a given request path, the route with the
     * lowest priority number wins. This is particularly important for wildcard and
     * catch-all routes.</p>
     *
     * <p>Default is {@code 100}. Use {@code Integer.MAX_VALUE} for fallback/catch-all
     * routes (e.g., a custom 404 page) to ensure they only match when no other
     * route does.</p>
     *
     * @return the priority value; lower numbers match first; defaults to {@code 100}
     */
    int priority() default 100;

    /**
     * Spring profiles that must be active for this route to be registered.
     *
     * <p>When non-empty, the route is only registered if at least one of the specified
     * Spring profiles is active. This is useful for debug and diagnostic pages that
     * should only be available in development or staging environments.</p>
     *
     * <p>An empty array (the default) means the route is active in all profiles.</p>
     *
     * <pre>{@code
     * @Route(value = "/debug/routes", profiles = {"dev", "staging"})
     * }</pre>
     *
     * @return the required Spring profile names; empty for all profiles
     */
    String[] profiles() default {};
}
