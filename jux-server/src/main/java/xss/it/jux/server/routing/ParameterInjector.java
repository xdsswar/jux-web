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

package xss.it.jux.server.routing;

import xss.it.jux.annotation.*;
import xss.it.jux.core.JuxRequestContext;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

/**
 * Reflection-based parameter injector that populates annotated fields on
 * component instances with values extracted from the HTTP request.
 *
 * <p>This injector scans the component class's declared fields for the
 * following annotations and injects the corresponding values:</p>
 *
 * <table>
 *   <tr><th>Annotation</th><th>Source</th><th>Example</th></tr>
 *   <tr><td>{@code @PathParam}</td><td>URL path variables</td><td>{@code /blog/{slug}}</td></tr>
 *   <tr><td>{@code @QueryParam}</td><td>URL query parameters</td><td>{@code ?page=2}</td></tr>
 *   <tr><td>{@code @HeaderParam}</td><td>HTTP request headers</td><td>{@code Accept-Language}</td></tr>
 *   <tr><td>{@code @CookieParam}</td><td>HTTP cookies</td><td>{@code session-id}</td></tr>
 *   <tr><td>{@code @SessionParam}</td><td>HTTP session attributes</td><td>{@code cart}</td></tr>
 *   <tr><td>{@code @RequestContext}</td><td>Full request context</td><td>{@code JuxRequestContext}</td></tr>
 *   <tr><td>{@code @LocaleParam}</td><td>Resolved locale</td><td>{@code Locale("es")}</td></tr>
 * </table>
 *
 * <p>String values from path, query, header, and cookie sources are automatically
 * coerced to the field's declared type using {@link TypeCoercer}. Session attributes
 * are injected as-is (no type coercion).</p>
 *
 * <p><b>Default values:</b> When a value is missing and the annotation specifies
 * a non-empty {@code defaultValue()}, the default is used. Required parameters
 * that are missing throw an {@link IllegalArgumentException}.</p>
 *
 * <p><b>Thread safety:</b> This class is stateless and thread-safe.</p>
 *
 * @see TypeCoercer
 * @see xss.it.jux.annotation.PathParam
 * @see xss.it.jux.annotation.QueryParam
 * @see xss.it.jux.annotation.HeaderParam
 * @see xss.it.jux.annotation.CookieParam
 * @see xss.it.jux.annotation.SessionParam
 * @see xss.it.jux.annotation.RequestContext
 * @see xss.it.jux.annotation.LocaleParam
 */
public class ParameterInjector {

    /**
     * Inject all annotated parameters into a component instance.
     *
     * @param component     the component instance
     * @param pathVariables resolved path variables from the route pattern
     * @param ctx           the request context
     * @param locale        the resolved locale
     */
    public void inject(Object component, Map<String, String> pathVariables,
                       JuxRequestContext ctx, Locale locale) {
        Class<?> clazz = component.getClass();

        // Iterate over all declared fields (not inherited ones) to find
        // injection annotations. Each field can have at most one injection
        // annotation — the if/else chain ensures only the first match is processed.
        for (Field field : clazz.getDeclaredFields()) {
            // Make private fields accessible for reflective injection.
            // This is necessary because component fields are typically private.
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PathParam.class)) {
                    injectPathParam(component, field, pathVariables);
                } else if (field.isAnnotationPresent(QueryParam.class)) {
                    injectQueryParam(component, field, ctx);
                } else if (field.isAnnotationPresent(HeaderParam.class)) {
                    injectHeaderParam(component, field, ctx);
                } else if (field.isAnnotationPresent(CookieParam.class)) {
                    injectCookieParam(component, field, ctx);
                } else if (field.isAnnotationPresent(SessionParam.class)) {
                    injectSessionParam(component, field, ctx);
                } else if (field.isAnnotationPresent(RequestContext.class)) {
                    // Direct injection of the full request context object
                    field.set(component, ctx);
                } else if (field.isAnnotationPresent(LocaleParam.class)) {
                    // Direct injection of the resolved Locale object
                    field.set(component, locale);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to inject parameter into field: " + field.getName(), e);
            }
        }
    }

    /**
     * Inject a path variable value into a {@code @PathParam}-annotated field.
     *
     * <p>The variable name is determined from the annotation's {@code value()} attribute,
     * falling back to the field name if the annotation value is empty. The raw string
     * is coerced to the field's type via {@link TypeCoercer}.</p>
     *
     * @param component     the component instance to inject into
     * @param field         the field annotated with {@code @PathParam}
     * @param pathVariables the map of path variable names to their captured string values
     * @throws IllegalAccessException   if the field cannot be set
     * @throws IllegalArgumentException if a required path variable is missing
     */
    private void injectPathParam(Object component, Field field, Map<String, String> pathVariables) throws IllegalAccessException {
        PathParam ann = field.getAnnotation(PathParam.class);
        String name = ann.value().isEmpty() ? field.getName() : ann.value();
        String value = pathVariables.get(name);

        if (value == null && !ann.defaultValue().isEmpty()) {
            value = ann.defaultValue();
        }
        if (value == null && ann.required()) {
            throw new IllegalArgumentException("Required path parameter missing: " + name);
        }
        if (value != null) {
            field.set(component, TypeCoercer.coerce(value, field.getType()));
        }
    }

    /**
     * Inject a query parameter value into a {@code @QueryParam}-annotated field.
     *
     * <p>The parameter name is determined from the annotation's {@code value()} attribute,
     * falling back to the field name if empty. Missing optional query params use the
     * annotation's default value. Missing required params throw an exception.</p>
     *
     * @param component the component instance to inject into
     * @param field     the field annotated with {@code @QueryParam}
     * @param ctx       the request context providing access to query parameters
     * @throws IllegalAccessException   if the field cannot be set
     * @throws IllegalArgumentException if a required query parameter is missing
     */
    private void injectQueryParam(Object component, Field field, JuxRequestContext ctx) throws IllegalAccessException {
        QueryParam ann = field.getAnnotation(QueryParam.class);
        String name = ann.value().isEmpty() ? field.getName() : ann.value();
        String value = ctx.queryParam(name).orElse(null);

        if (value == null && !ann.defaultValue().isEmpty()) {
            value = ann.defaultValue();
        }
        if (value == null && ann.required()) {
            throw new IllegalArgumentException("Required query parameter missing: " + name);
        }
        if (value != null) {
            field.set(component, TypeCoercer.coerce(value, field.getType()));
        }
    }

    /**
     * Inject an HTTP header value into a {@code @HeaderParam}-annotated field.
     *
     * <p>The header name is taken from the annotation's {@code value()} attribute.
     * Header lookup is case-insensitive per the HTTP specification. If the header
     * is absent and a default value is specified, the default is used.</p>
     *
     * @param component the component instance to inject into
     * @param field     the field annotated with {@code @HeaderParam}
     * @param ctx       the request context providing access to HTTP headers
     * @throws IllegalAccessException if the field cannot be set
     */
    private void injectHeaderParam(Object component, Field field, JuxRequestContext ctx) throws IllegalAccessException {
        HeaderParam ann = field.getAnnotation(HeaderParam.class);
        String value = ctx.header(ann.value()).orElse(null);
        if (value == null && !ann.defaultValue().isEmpty()) {
            value = ann.defaultValue();
        }
        if (value != null) {
            field.set(component, TypeCoercer.coerce(value, field.getType()));
        }
    }

    /**
     * Inject a cookie value into a {@code @CookieParam}-annotated field.
     *
     * <p>The cookie name is taken from the annotation's {@code value()} attribute.
     * Cookie name matching is case-sensitive. If the cookie is absent and a
     * default value is specified, the default is used.</p>
     *
     * @param component the component instance to inject into
     * @param field     the field annotated with {@code @CookieParam}
     * @param ctx       the request context providing access to HTTP cookies
     * @throws IllegalAccessException if the field cannot be set
     */
    private void injectCookieParam(Object component, Field field, JuxRequestContext ctx) throws IllegalAccessException {
        CookieParam ann = field.getAnnotation(CookieParam.class);
        String value = ctx.cookie(ann.value()).orElse(null);
        if (value == null && !ann.defaultValue().isEmpty()) {
            value = ann.defaultValue();
        }
        if (value != null) {
            field.set(component, TypeCoercer.coerce(value, field.getType()));
        }
    }

    /**
     * Inject an HTTP session attribute into a {@code @SessionParam}-annotated field.
     *
     * <p>The session attribute key is taken from the annotation's {@code value()} attribute.
     * Unlike other parameter types, session values are injected as-is without type
     * coercion, since they are stored as typed Java objects in the HTTP session.</p>
     *
     * @param component the component instance to inject into
     * @param field     the field annotated with {@code @SessionParam}
     * @param ctx       the request context providing access to the HTTP session
     * @throws IllegalAccessException if the field cannot be set
     */
    private void injectSessionParam(Object component, Field field, JuxRequestContext ctx) throws IllegalAccessException {
        SessionParam ann = field.getAnnotation(SessionParam.class);
        Object value = ctx.session(ann.value()).orElse(null);
        if (value != null) {
            field.set(component, value);
        }
    }
}
