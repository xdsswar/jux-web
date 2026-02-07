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

package xss.it.jux.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xss.it.jux.annotation.Css;
import xss.it.jux.annotation.Favicon;
import xss.it.jux.server.WebApplication;

/**
 * JUX Store — E-commerce demo application.
 *
 * <p>Demonstrates how a consumer project uses JUX to build a complete
 * e-commerce website entirely in Java with Tailwind CSS styling.
 * This proves JUX is CSS-framework-agnostic — jux-demo uses Bootstrap,
 * jux-store uses Tailwind.</p>
 *
 * <p>Global CSS resources are declared here once. All pages inherit
 * these automatically — no need to repeat on each page class.</p>
 *
 * <p>Run with: {@code ./gradlew :jux-store:bootRun}
 * Then open {@code http://localhost:9091}</p>
 */
@SpringBootApplication
@Css(value = "/css/tailwind.min.css", order = 1)
@Css(value = "/css/store.css", order = 10)
@Favicon(value = "/img/store-logo.png", type = "image/png")
public class StoreApplication implements WebApplication {

    public static void main(String... args) {
        SpringApplication.run(StoreApplication.class, args);
    }
}
