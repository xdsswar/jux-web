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

package xss.it.jux.core;

import java.util.Locale;
import java.util.Map;

/**
 * Base class for routable pages in JUX.
 *
 * <p>While {@link Component} is the building block for all UI elements
 * (widgets, cards, navbars, footers), {@code Page} is specifically for
 * top-level routable views annotated with {@code @Route}. Pages have
 * access to page metadata, request context, URL parameters, session,
 * headers, cookies, and response control.</p>
 *
 * <h2>Page vs Component</h2>
 * <ul>
 *   <li>{@link Component} -- reusable UI building blocks. Only has {@code render()}.
 *       Used for widgets, layouts, navbars, cards, modals, etc.</li>
 *   <li>{@code Page} -- routable views with metadata and request access.
 *       Has {@code pageMeta()} plus programmatic access to path params,
 *       query params, headers, cookies, session, locale, and response control.</li>
 * </ul>
 *
 * <h2>Request Data Access</h2>
 * <p>Pages can access request data in two ways:</p>
 * <ol>
 *   <li><b>Annotations</b> (declarative): {@code @PathParam}, {@code @QueryParam},
 *       {@code @HeaderParam}, {@code @CookieParam}, {@code @SessionParam},
 *       {@code @RequestContext}, {@code @LocaleParam} on fields</li>
 *   <li><b>Programmatic</b> (this class): {@code pathParam("slug")},
 *       {@code queryParam("q")}, {@code header("Accept")}, {@code cookie("theme")},
 *       {@code session("user")}, {@code locale()}, etc.</li>
 * </ol>
 * <p>Both approaches work simultaneously. Use whichever is cleaner for your case.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Route("/blog/{slug}")
 * @Title("Blog Post")
 * @Css(value = "/css/blog.css", order = 10)
 * public class BlogPostPage extends Page {
 *
 *     @Autowired private BlogRepository blogRepo;
 *
 *     @Override
 *     public PageMeta pageMeta() {
 *         var post = blogRepo.findBySlug(pathParam("slug"));
 *         if (post == null) return PageMeta.create().status(404).title("Not Found");
 *         return PageMeta.create()
 *             .title(post.getTitle())
 *             .description(post.getExcerpt())
 *             .ogImage(post.getCoverImage())
 *             .htmlLang(locale().getLanguage());
 *     }
 *
 *     @Override
 *     public Element render() {
 *         var post = blogRepo.findBySlug(pathParam("slug"));
 *         return article().children(
 *             h1().text(post.getTitle()),
 *             div().text(post.getContent())
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see Component
 * @see PageMeta
 */
public abstract class Page extends Component {

    /**
     * The HTTP request context for the current request.
     * Set by the framework before {@link #pageMeta()} and {@link #render()} are called.
     */
    private JuxRequestContext requestContext;

    /**
     * Path parameters extracted from the URL pattern (e.g. {@code {slug}} in {@code /blog/{slug}}).
     * Set by the framework before {@link #pageMeta()} and {@link #render()} are called.
     */
    private Map<String, String> pathParams = Map.of();

    /**
     * The i18n messages service for accessing translated strings and locale information.
     * Set by the framework before {@link #pageMeta()} and {@link #render()} are called.
     */
    private JuxMessages messages;

    // ── Framework Internal ──────────────────────────────────────────

    /**
     * Initialize the page with the current request context, path parameters,
     * and i18n messages service.
     *
     * <p><b>Framework internal.</b> Called by the route handler before
     * {@link #pageMeta()} and {@link #render()}. Application code should
     * not call this method directly.</p>
     *
     * @param context    the HTTP request/response context for this request
     * @param pathParams URL path variables extracted from the route pattern
     * @param messages   the i18n messages service for the current request locale
     */
    public final void initRequest(JuxRequestContext context, Map<String, String> pathParams,
                                  JuxMessages messages) {
        this.requestContext = context;
        this.pathParams = pathParams != null ? Map.copyOf(pathParams) : Map.of();
        this.messages = messages;
    }

    // ── Page Metadata ───────────────────────────────────────────────

    /**
     * Optional programmatic page metadata.
     *
     * <p>Override this method to set page title, meta tags, CSS/JS resources,
     * OpenGraph data, HTTP status, and other metadata <b>dynamically</b> at
     * render time -- useful when metadata depends on database content, user
     * preferences, or runtime conditions.</p>
     *
     * <p><b>Annotation vs. Programmatic:</b></p>
     * <ul>
     *   <li>Annotations ({@code @Title}, {@code @Css}, {@code @Meta}) are the baseline</li>
     *   <li>{@code pageMeta()} values <b>override</b> their annotation equivalents</li>
     *   <li>Return {@code null} (default) to use annotations only</li>
     * </ul>
     *
     * @return page metadata builder, or {@code null} if annotations are sufficient
     * @see PageMeta
     */
    public PageMeta pageMeta() {
        return null;
    }

    // ── Request Context ─────────────────────────────────────────────

    /**
     * Returns the full HTTP request context.
     *
     * <p>Use this when the convenience methods below don't cover your needs.
     * Provides access to all request data, response manipulation, and
     * advanced features like form params, custom headers, and redirects.</p>
     *
     * @return the request context, or {@code null} if not in a request scope
     */
    protected JuxRequestContext context() {
        return requestContext;
    }

    // ── Path Parameters ─────────────────────────────────────────────

    /**
     * Get a URL path parameter by name.
     *
     * <p>Path parameters are extracted from the route pattern. For example,
     * with {@code @Route("/blog/{slug}")}, calling {@code pathParam("slug")}
     * returns the matched value from the URL.</p>
     *
     * @param name the parameter name (must match a {@code {name}} in the route pattern)
     * @return the parameter value, or {@code null} if not present
     */
    protected String pathParam(String name) {
        return pathParams.get(name);
    }

    /**
     * Get a URL path parameter with a default fallback.
     *
     * @param name         the parameter name
     * @param defaultValue value to return if the parameter is absent
     * @return the parameter value, or {@code defaultValue} if not present
     */
    protected String pathParam(String name, String defaultValue) {
        return pathParams.getOrDefault(name, defaultValue);
    }

    // ── Query Parameters ────────────────────────────────────────────

    /**
     * Get a URL query parameter by name.
     *
     * <p>For a URL like {@code /search?q=hello&page=2}, calling
     * {@code queryParam("q")} returns {@code "hello"}.</p>
     *
     * @param name the query parameter name
     * @return the parameter value, or {@code null} if not present
     */
    protected String queryParam(String name) {
        return requestContext != null ? requestContext.queryParam(name).orElse(null) : null;
    }

    /**
     * Get a URL query parameter with a default fallback.
     *
     * @param name         the query parameter name
     * @param defaultValue value to return if the parameter is absent
     * @return the parameter value, or {@code defaultValue} if not present
     */
    protected String queryParam(String name, String defaultValue) {
        return requestContext != null ? requestContext.queryParam(name).orElse(defaultValue) : defaultValue;
    }

    // ── Headers ─────────────────────────────────────────────────────

    /**
     * Get an HTTP request header by name (case-insensitive).
     *
     * @param name the header name (e.g. "Accept-Language", "X-Forwarded-For")
     * @return the header value, or {@code null} if not present
     */
    protected String header(String name) {
        return requestContext != null ? requestContext.header(name).orElse(null) : null;
    }

    // ── Cookies ─────────────────────────────────────────────────────

    /**
     * Get an HTTP cookie value by name (case-sensitive).
     *
     * @param name the cookie name (e.g. "session-id", "theme")
     * @return the cookie value, or {@code null} if not present
     */
    protected String cookie(String name) {
        return requestContext != null ? requestContext.cookie(name).orElse(null) : null;
    }

    // ── Session ─────────────────────────────────────────────────────

    /**
     * Get a server-side session attribute.
     *
     * @param name the session attribute key
     * @param <T>  the expected value type
     * @return the attribute value, or {@code null} if not set
     */
    @SuppressWarnings("unchecked")
    protected <T> T session(String name) {
        return requestContext != null ? (T) requestContext.session(name).orElse(null) : null;
    }

    /**
     * Set a server-side session attribute.
     *
     * @param name  the session attribute key
     * @param value the value to store
     */
    protected void session(String name, Object value) {
        if (requestContext != null) {
            requestContext.session(name, value);
        }
    }

    // ── i18n Messages ────────────────────────────────────────────────

    /**
     * Returns the i18n messages service for accessing translated strings
     * and locale information.
     *
     * <p>Use this to look up translated strings from properties files,
     * check the current locale, list available locales, and detect RTL:</p>
     * <pre>{@code
     * messages().getString("hero.title")              // translated string
     * messages().getString("greeting", "World")       // formatted: "Hello, World!"
     * messages().currentLocale().getLanguage()         // "en", "es", etc.
     * messages().availableLocales()                    // [en, es, fr, ...]
     * messages().isRtl()                               // true for Arabic, Hebrew, etc.
     * }</pre>
     *
     * @return the messages service, or {@code null} if i18n is not configured
     * @see JuxMessages
     */
    protected JuxMessages messages() {
        return messages;
    }

    // ── Locale ──────────────────────────────────────────────────────

    /**
     * Get the resolved locale for the current request.
     *
     * <p>Determined by the i18n locale resolution chain:
     * URL prefix, query param, cookie, session, Accept-Language header,
     * and finally the configured default locale.</p>
     *
     * @return the resolved locale, never {@code null}
     */
    protected Locale locale() {
        return requestContext != null ? requestContext.locale() : Locale.getDefault();
    }

    // ── Request Info ────────────────────────────────────────────────

    /**
     * Get the request URI path without the query string.
     *
     * @return the request path (e.g. "/blog/hello")
     */
    protected String requestPath() {
        return requestContext != null ? requestContext.requestPath() : "/";
    }

    /**
     * Get the full request URL including the query string.
     *
     * @return the full request URL
     */
    protected String requestUrl() {
        return requestContext != null ? requestContext.requestUrl() : "";
    }

    /**
     * Check if the current request is an HTTP POST.
     *
     * @return {@code true} if the request method is POST
     */
    protected boolean isPost() {
        return requestContext != null && requestContext.isPost();
    }

    /**
     * Check if the current request is an HTTP GET.
     *
     * @return {@code true} if the request method is GET
     */
    protected boolean isGet() {
        return requestContext != null && requestContext.isGet();
    }

    /**
     * Get the HTTP method of the current request.
     *
     * @return the HTTP method in uppercase (e.g. "GET", "POST")
     */
    protected String method() {
        return requestContext != null ? requestContext.method() : "GET";
    }

    /**
     * Get the remote IP address of the client.
     *
     * @return the remote IP address
     */
    protected String remoteAddress() {
        return requestContext != null ? requestContext.remoteAddress() : null;
    }

    // ── Form Data ───────────────────────────────────────────────────

    /**
     * Get a form/POST parameter value.
     *
     * @param name the form parameter name
     * @return the parameter value, or {@code null} if not present
     */
    protected String formParam(String name) {
        return requestContext != null ? requestContext.formParam(name) : null;
    }

    // ── Response Control ────────────────────────────────────────────

    /**
     * Set the HTTP response status code.
     *
     * @param code the HTTP status code (e.g. 200, 404, 500)
     */
    protected void status(int code) {
        if (requestContext != null) {
            requestContext.status(code);
        }
    }

    /**
     * Set an HTTP response header.
     *
     * @param name  the header name
     * @param value the header value
     */
    protected void responseHeader(String name, String value) {
        if (requestContext != null) {
            requestContext.responseHeader(name, value);
        }
    }

    /**
     * Send a 302 redirect to the given URL.
     *
     * @param url the target URL
     */
    protected void redirect(String url) {
        if (requestContext != null) {
            requestContext.redirect(url, 302);
        }
    }

    /**
     * Send a redirect with a specific status code.
     *
     * @param url        the target URL
     * @param statusCode the redirect status (301, 302, 307, 308)
     */
    protected void redirect(String url, int statusCode) {
        if (requestContext != null) {
            requestContext.redirect(url, statusCode);
        }
    }
}
