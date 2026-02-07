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

package xss.it.jux.i18n;

import xss.it.jux.annotation.Message;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link InvocationHandler} that implements {@code @MessageBundle} interfaces at
 * runtime via {@link Proxy java.lang.reflect.Proxy}.
 *
 * <p>When a component calls a method on a message bundle proxy, this handler:</p>
 * <ol>
 *   <li>Resolves the {@link Message @Message} annotation from the locale-specific
 *       interface first, falling back to the base bundle interface if no override
 *       exists.</li>
 *   <li>If the method has no parameters, the raw pattern string is returned
 *       directly (no formatting overhead).</li>
 *   <li>Otherwise, the pattern is parsed into a {@link MessageFormat} (cached per
 *       method) and the arguments are formatted according to the target
 *       {@link Locale}. This gives full access to {@code MessageFormat} features
 *       including {@code {0,choice,...}} pluralization rules.</li>
 * </ol>
 *
 * <p>The {@code MessageFormat} cache is keyed by
 * {@code "localeInterfaceName#methodName"} and is stored in a
 * {@link ConcurrentHashMap} for thread-safe access. Because
 * {@link MessageFormat} itself is <b>not</b> thread-safe, each format
 * invocation is synchronized on the format instance.</p>
 *
 * <p>If a method has no {@code @Message} annotation on either interface, the
 * handler returns a sentinel string of the form {@code "!methodName!"} to make
 * missing translations immediately visible during development.</p>
 *
 * <p><b>Standard {@code Object} methods</b> ({@code toString}, {@code hashCode},
 * {@code equals}) are intercepted and handled specially so that proxies behave
 * reasonably when used in collections or logged.</p>
 *
 * @see MessageBundleRegistry#getBundle(Class, Locale)
 * @see Message
 */
public class MessageBundleProxy implements InvocationHandler {

    /** The base bundle interface (default-language, annotated with {@code @MessageBundle}). */
    private final Class<?> bundleInterface;

    /**
     * The locale-specific sub-interface (annotated with {@code @MessageLocale}),
     * or the same as {@link #bundleInterface} when using the default language.
     */
    private final Class<?> localeInterface;

    /** The target locale used for {@link MessageFormat} formatting. */
    private final Locale locale;

    /**
     * Cache of compiled {@link MessageFormat} instances, keyed by
     * {@code "localeInterfaceName#methodName"}. Entries are created lazily
     * on first invocation of a parameterized message method.
     */
    private final Map<String, MessageFormat> formatCache = new ConcurrentHashMap<>();

    /**
     * Private constructor -- instances are created exclusively via
     * {@link #create(Class, Class, Locale)}.
     *
     * @param bundleInterface the base bundle interface class
     * @param localeInterface the locale-specific interface class (may equal {@code bundleInterface})
     * @param locale          the target locale for message formatting
     */
    private MessageBundleProxy(Class<?> bundleInterface, Class<?> localeInterface, Locale locale) {
        this.bundleInterface = bundleInterface;
        this.localeInterface = localeInterface;
        this.locale = locale;
    }

    /**
     * Creates a dynamic proxy that implements the given bundle interface.
     *
     * <p>The returned proxy dispatches every method call to a
     * {@link MessageBundleProxy} handler that looks up the {@code @Message}
     * pattern on the locale-specific interface (or the base interface as
     * fallback), formats it via {@link MessageFormat}, and returns the result.</p>
     *
     * <p>The proxy is loaded by the same {@link ClassLoader} that loaded
     * {@code bundleType}, which is normally the application class loader.</p>
     *
     * @param bundleType the base bundle interface class (annotated with
     *                   {@code @MessageBundle})
     * @param localeType the locale-specific sub-interface (annotated with
     *                   {@code @MessageLocale}), or the same as {@code bundleType}
     *                   when the default language should be used
     * @param locale     the target locale for {@link MessageFormat} formatting
     * @param <T>        the bundle interface type
     * @return a proxy instance of type {@code T}; never {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> bundleType, Class<?> localeType, Locale locale) {
        MessageBundleProxy handler = new MessageBundleProxy(bundleType, localeType, locale);
        return (T) Proxy.newProxyInstance(
            bundleType.getClassLoader(),
            new Class<?>[]{bundleType},
            handler
        );
    }

    /**
     * Intercepts every method call on the proxy and returns the localized,
     * formatted message string.
     *
     * <p><b>Dispatch logic:</b></p>
     * <ul>
     *   <li>{@code Object} methods ({@code toString}, {@code hashCode},
     *       {@code equals}) are handled directly without consulting message
     *       annotations.</li>
     *   <li>Bundle methods are resolved via {@link #resolvePattern(Method)}.
     *       If no {@code @Message} annotation is found, a sentinel string
     *       {@code "!methodName!"} is returned to flag missing translations
     *       during development.</li>
     *   <li>Zero-argument methods return the raw pattern string (no
     *       {@link MessageFormat} overhead).</li>
     *   <li>Methods with arguments delegate to a cached {@link MessageFormat}
     *       instance for locale-aware formatting and pluralization.</li>
     * </ul>
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@link Method} corresponding to the interface method invoked
     * @param args   the arguments passed to the method, or {@code null} if none
     * @return the formatted message string
     * @throws Throwable if the underlying {@code Object} method invocation fails
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Intercept standard Object methods so proxies behave correctly in
        // logging, collections, and debugging contexts.
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "toString" -> bundleInterface.getSimpleName() + "[" + locale.toLanguageTag() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> method.invoke(this, args);
            };
        }

        // Look up the @Message pattern from the locale or base interface
        String pattern = resolvePattern(method);
        if (pattern == null) {
            // Sentinel that makes missing translations obvious in rendered output
            return "!" + method.getName() + "!";
        }

        // Fast path: no arguments means no MessageFormat parsing is needed
        if (args == null || args.length == 0) {
            return pattern;
        }

        // Compile (or retrieve) a MessageFormat and format the arguments.
        // The cache key includes the locale interface name to prevent collisions
        // when the same method name exists on different locale interfaces.
        String cacheKey = localeInterface.getName() + "#" + method.getName();
        MessageFormat format = formatCache.computeIfAbsent(cacheKey,
            k -> new MessageFormat(pattern, locale));

        // MessageFormat is not thread-safe, so synchronize on the instance
        synchronized (format) {
            return format.format(args);
        }
    }

    /**
     * Resolves the {@link MessageFormat} pattern string for the given method
     * by searching for a {@link Message @Message} annotation.
     *
     * <p>The search order ensures that locale-specific translations override
     * the default-language text:</p>
     * <ol>
     *   <li>The matching method on the <b>locale-specific</b> interface
     *       ({@link #localeInterface}). This is skipped if the locale interface
     *       is the same as the base bundle (default language).</li>
     *   <li>The {@code @Message} annotation directly on the {@code method}
     *       argument (which comes from the proxy invocation and may already
     *       carry the annotation from the base interface).</li>
     *   <li>The method looked up explicitly on the <b>base bundle</b> interface
     *       ({@link #bundleInterface}). This covers edge cases where the proxy
     *       method reference does not carry the annotation.</li>
     * </ol>
     *
     * @param method the interface method whose {@code @Message} pattern is needed
     * @return the {@link MessageFormat} pattern string, or {@code null} if no
     *         {@code @Message} annotation is found on any level
     */
    private String resolvePattern(Method method) {
        // 1. Try the locale-specific interface first (translated text)
        if (localeInterface != null && localeInterface != bundleInterface) {
            try {
                Method localeMethod = localeInterface.getMethod(method.getName(), method.getParameterTypes());
                Message msg = localeMethod.getAnnotation(Message.class);
                if (msg != null) return msg.value();
            } catch (NoSuchMethodException ignored) {
                // Method not overridden in the locale interface -- fall through to base
            }
        }

        // 2. Try the annotation on the method reference handed to us by the proxy
        Message msg = method.getAnnotation(Message.class);
        if (msg != null) return msg.value();

        // 3. Explicitly look up the method on the base bundle interface as a last resort
        try {
            Method baseMethod = bundleInterface.getMethod(method.getName(), method.getParameterTypes());
            msg = baseMethod.getAnnotation(Message.class);
            if (msg != null) return msg.value();
        } catch (NoSuchMethodException ignored) {
            // Method does not exist on the base interface either
        }

        return null;
    }
}
