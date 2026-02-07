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

/**
 * HTTP methods supported by the {@link Route} annotation.
 *
 * <p>These constants map to the standard HTTP/1.1 request methods defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc9110#section-9">RFC 9110 Section 9</a>.
 * They are used in the {@link Route#methods()} element to declare which HTTP methods
 * a routed component responds to.</p>
 *
 * <p>By default, routes only respond to {@link #GET} requests. To handle form
 * submissions, add {@link #POST}:</p>
 * <pre>{@code
 * @Route(value = "/contact", methods = {HttpMethod.GET, HttpMethod.POST})
 * public class ContactPage extends Component { ... }
 * }</pre>
 *
 * @see Route#methods()
 */
public enum HttpMethod {

    /**
     * HTTP GET -- retrieve a resource.
     *
     * <p>The default and most common method for page routes. GET requests are
     * idempotent and safe (they do not modify server state). All JUX page routes
     * respond to GET by default.</p>
     */
    GET,

    /**
     * HTTP POST -- submit data for processing.
     *
     * <p>Used for form submissions, file uploads, and any request that creates
     * or modifies server-side resources. Add alongside GET for routes that both
     * display and process forms.</p>
     */
    POST,

    /**
     * HTTP PUT -- replace a resource entirely.
     *
     * <p>Typically used in RESTful API-style routes to update an existing resource
     * by replacing it with the request payload. Idempotent -- repeated identical
     * requests produce the same result.</p>
     */
    PUT,

    /**
     * HTTP DELETE -- remove a resource.
     *
     * <p>Used to request deletion of a server-side resource. Idempotent -- deleting
     * an already-deleted resource should not produce an error.</p>
     */
    DELETE,

    /**
     * HTTP PATCH -- partially update a resource.
     *
     * <p>Similar to PUT but applies a partial modification rather than a full
     * replacement. Not necessarily idempotent.</p>
     */
    PATCH,

    /**
     * HTTP HEAD -- retrieve response headers only, without a body.
     *
     * <p>Identical to GET but the server must not return a message body. Useful
     * for checking resource existence, content length, or cache validity without
     * transferring the full response.</p>
     */
    HEAD,

    /**
     * HTTP OPTIONS -- describe communication options for the target resource.
     *
     * <p>Used by browsers for CORS preflight requests and by clients to discover
     * which HTTP methods and headers a resource supports. The response typically
     * includes {@code Allow} and CORS headers.</p>
     */
    OPTIONS
}
