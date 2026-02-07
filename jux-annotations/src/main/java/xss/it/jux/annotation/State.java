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
 * Marks a field as reactive client-side state.
 *
 * <p>When a {@code @State} field's value changes on the client, the component
 * automatically re-renders and the framework performs a virtual DOM diff/patch,
 * applying only the minimal set of mutations to the real DOM. This provides a
 * reactive programming model similar to React's {@code useState} or Vue's
 * reactive data, but implemented entirely in Java via TeaVM.</p>
 *
 * <p>State is local to the component instance -- it is not shared across
 * components or persisted between page loads. For shared state, use Spring
 * services injected via {@code @Autowired} or browser storage accessed
 * through the TeaVM JSO APIs.</p>
 *
 * <p>This annotation is only meaningful on client-side components
 * ({@code @JuxComponent(clientSide = true)}). On the server, state fields
 * are treated as regular instance fields initialized to their default values.</p>
 *
 * <p><b>Example -- reactive counter:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class Counter extends Component {
 *     @State private int count = 0;
 *
 *     @Override
 *     public Element render() {
 *         return div().children(
 *             span().text("Count: " + count),
 *             button().text("+").on("click", e -> count++),
 *             button().text("-").on("click", e -> count--)
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example -- search autocomplete with reactive suggestions:</b></p>
 * <pre>{@code
 * @JuxComponent(clientSide = true)
 * public class SearchAutocomplete extends Component {
 *     @State private String query = "";
 *     @State private List<String> suggestions = List.of();
 *
 *     @Override
 *     public Element render() {
 *         return div().cls("autocomplete").children(
 *             input().id("search").attr("type", "text")
 *                 .on("input", e -> setQuery(e.getValue())),
 *             ul().children(suggestions.stream().map(s ->
 *                 li().text(s)
 *             ).toList())
 *         );
 *     }
 * }
 * }</pre>
 *
 * @see JuxComponent
 * @see On
 * @see OnMount
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface State {
}
