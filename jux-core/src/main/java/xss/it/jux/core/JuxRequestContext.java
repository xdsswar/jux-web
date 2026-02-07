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
import java.util.Optional;

/**
 * Unified HTTP request/response context interface for JUX components.
 *
 * <p>Provides access to the full HTTP request (method, path, headers, cookies,
 * session, form data, query parameters) and the ability to manipulate the
 * HTTP response (status code, headers, redirects). The implementation is
 * provided by the {@code jux-server} module and wraps the underlying
 * {@code HttpServletRequest} and {@code HttpServletResponse} from Spring MVC.</p>
 *
 * <p>Components receive this context via the {@code @RequestContext} annotation
 * on a field:</p>
 * <pre>{@code
 * @Route(value = "/contact", methods = { HttpMethod.GET, HttpMethod.POST })
 * public class ContactPage extends Component {
 *
 *     @RequestContext private JuxRequestContext ctx;
 *
 *     @Override
 *     public Element render() {
 *         if (ctx.isPost()) {
 *             String name = ctx.formParam("name");
 *             String email = ctx.formParam("email");
 *             // process form submission...
 *         }
 *         // render form...
 *     }
 * }
 * }</pre>
 *
 * <p><b>Thread safety:</b> Each request gets its own context instance. The context
 * is scoped to a single HTTP request and should not be stored or shared across
 * requests.</p>
 *
 * @see xss.it.jux.annotation.RequestContext
 */
public interface JuxRequestContext {

    /**
     * Returns the HTTP method of the current request.
     *
     * @return the HTTP method in uppercase (e.g. "GET", "POST", "PUT", "DELETE")
     */
    String method();

    /**
     * Convenience check for whether this is an HTTP POST request.
     *
     * <p>Equivalent to {@code "POST".equalsIgnoreCase(method())}.</p>
     *
     * @return {@code true} if the HTTP method is POST
     */
    default boolean isPost() {
        return "POST".equalsIgnoreCase(method());
    }

    /**
     * Convenience check for whether this is an HTTP GET request.
     *
     * <p>Equivalent to {@code "GET".equalsIgnoreCase(method())}.</p>
     *
     * @return {@code true} if the HTTP method is GET
     */
    default boolean isGet() {
        return "GET".equalsIgnoreCase(method());
    }

    /**
     * Returns the request URI path without the query string.
     *
     * <p>For a request to {@code /blog/hello?ref=twitter}, this returns
     * {@code "/blog/hello"}.</p>
     *
     * @return the request path, never null, always starts with "/"
     */
    String requestPath();

    /**
     * Returns the full request URL including the query string.
     *
     * <p>For a request to {@code /blog/hello?ref=twitter}, this returns
     * the full URL as seen by the server (e.g.
     * {@code "https://example.com/blog/hello?ref=twitter"}).</p>
     *
     * @return the full request URL, never null
     */
    String requestUrl();

    /**
     * Returns the value of the specified HTTP request header.
     *
     * <p>Header name matching is case-insensitive per the HTTP specification.</p>
     *
     * @param name the header name (e.g. "Accept-Language", "X-Forwarded-For", "Authorization")
     * @return the header value, or empty if the header is not present
     */
    Optional<String> header(String name);

    /**
     * Returns the value of the specified HTTP cookie.
     *
     * <p>Cookie name matching is case-sensitive.</p>
     *
     * @param name the cookie name (e.g. "session-id", "jux-lang", "theme")
     * @return the cookie value, or empty if the cookie is not present
     */
    Optional<String> cookie(String name);

    /**
     * Returns a session attribute by key.
     *
     * <p>Session attributes are stored server-side and associated with the
     * user's session (typically via a session cookie). The session is created
     * lazily on first access.</p>
     *
     * @param key the session attribute key
     * @return the attribute value, or empty if the key is not set
     */
    Optional<Object> session(String key);

    /**
     * Sets a session attribute.
     *
     * <p>The session is created if it does not already exist. The value is
     * stored server-side and persists across requests until the session
     * expires or is invalidated.</p>
     *
     * @param key   the session attribute key
     * @param value the value to store (must be serializable for distributed sessions)
     */
    void session(String key, Object value);

    /**
     * Returns a single form/POST parameter value.
     *
     * <p>For {@code application/x-www-form-urlencoded} or {@code multipart/form-data}
     * POST requests, this returns the value of the named form field. If the
     * parameter has multiple values, the first one is returned.</p>
     *
     * @param name the form parameter name
     * @return the parameter value, or null if not present
     */
    String formParam(String name);

    /**
     * Returns all form/POST parameters as a map of name to value arrays.
     *
     * <p>Each parameter name maps to an array of values, supporting multi-value
     * fields (e.g. checkboxes with the same name). For GET requests, this
     * includes query string parameters.</p>
     *
     * @return all parameters as an unmodifiable map of name to value arrays
     */
    Map<String, String[]> formParams();

    /**
     * Returns a single URL query parameter value.
     *
     * <p>For a URL like {@code /search?q=hello&page=2}, calling
     * {@code queryParam("q")} returns {@code Optional.of("hello")}.</p>
     *
     * @param name the query parameter name
     * @return the parameter value, or empty if not present in the query string
     */
    Optional<String> queryParam(String name);

    /**
     * Returns the remote IP address of the client making the request.
     *
     * <p>If the application is behind a reverse proxy, this may return the proxy's
     * address. Use {@code header("X-Forwarded-For")} for the original client IP
     * in proxied environments.</p>
     *
     * @return the remote IP address string (e.g. "192.168.1.100", "::1")
     */
    String remoteAddress();

    /**
     * Returns the resolved locale for the current request.
     *
     * <p>The locale is determined by the i18n locale resolution chain:
     * URL prefix, query param, cookie, session, Accept-Language header,
     * and finally the configured default locale.</p>
     *
     * @return the resolved locale, never null
     */
    Locale locale();

    /**
     * Sets an HTTP response header on the outgoing response.
     *
     * <p>If the header already exists, its value is replaced. Use this for
     * custom headers (e.g. {@code X-Custom-Header}) or to override framework
     * defaults. Common headers like Cache-Control can be set more conveniently
     * via {@link PageMeta#cacheControl(String)}.</p>
     *
     * @param name  the response header name
     * @param value the header value
     */
    void responseHeader(String name, String value);

    /**
     * Sets the HTTP response status code.
     *
     * <p>Common status codes: 200 (OK, default), 201 (Created), 400 (Bad Request),
     * 403 (Forbidden), 404 (Not Found), 410 (Gone), 500 (Internal Server Error).</p>
     *
     * <p>Can also be set via {@link PageMeta#status(int)} for a more declarative approach.</p>
     *
     * @param code the HTTP status code (e.g. 200, 404, 500)
     */
    void status(int code);

    /**
     * Sends an HTTP redirect response to the client.
     *
     * <p>This immediately commits the response -- no further rendering occurs
     * after this call. The client receives a redirect response with the specified
     * status code and a {@code Location} header pointing to the target URL.</p>
     *
     * @param url    the target URL to redirect to (relative or absolute)
     * @param status the HTTP redirect status code (typically 301, 302, 307, or 308)
     *
     * @see JuxRedirect
     */
    void redirect(String url, int status);
}
