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
 * Defines the default message text for a method in a {@link MessageBundle} interface.
 *
 * <p>The value supports standard {@link java.text.MessageFormat} patterns, including
 * positional arguments ({@code {0}}, {@code {1}}), number formatting
 * ({@code {0,number,####}}), date formatting ({@code {0,date,short}}), and choice
 * patterns for pluralization ({@code {0,choice,0#none|1#one|1<{0} items}}).</p>
 *
 * <p>On the base {@link MessageBundle} interface, this annotation defines the default
 * (fallback) language text. On {@link MessageLocale}-annotated sub-interfaces, it
 * provides the translated text for that specific locale.</p>
 *
 * <p>The annotation processor validates all message patterns at compile time to ensure
 * they are valid {@code MessageFormat} syntax. Invalid patterns result in a compile
 * error.</p>
 *
 * <p><b>Example -- simple message:</b></p>
 * <pre>{@code
 * @Message("Welcome to our site")
 * String welcome();
 * }</pre>
 *
 * <p><b>Example -- parameterized message:</b></p>
 * <pre>{@code
 * @Message("Hello, {0}")
 * String greeting(String name);
 * }</pre>
 *
 * <p><b>Example -- pluralization with choice pattern:</b></p>
 * <pre>{@code
 * @Message("{0,choice,0#No items|1#1 item|1<{0} items}")
 * String itemCount(int count);
 * }</pre>
 *
 * <p><b>Example -- number formatting:</b></p>
 * <pre>{@code
 * @Message("Copyright (c) {0,number,####} All rights reserved.")
 * String copyright(int year);
 * }</pre>
 *
 * @see MessageBundle
 * @see MessageLocale
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

    /**
     * The message text or {@link java.text.MessageFormat} pattern.
     *
     * <p>Supports all standard {@code MessageFormat} syntax:</p>
     * <ul>
     *   <li>Positional arguments: {@code {0}}, {@code {1}}, {@code {2}}</li>
     *   <li>Number format: {@code {0,number}}, {@code {0,number,####}},
     *       {@code {0,number,currency}}</li>
     *   <li>Date format: {@code {0,date}}, {@code {0,date,short}},
     *       {@code {0,date,yyyy-MM-dd}}</li>
     *   <li>Choice (pluralization): {@code {0,choice,0#none|1#one|1<{0} many}}</li>
     * </ul>
     *
     * <p>Method parameters are mapped to format arguments by position:
     * the first parameter is {@code {0}}, the second is {@code {1}}, etc.</p>
     *
     * @return the message text or MessageFormat pattern
     */
    String value();
}
