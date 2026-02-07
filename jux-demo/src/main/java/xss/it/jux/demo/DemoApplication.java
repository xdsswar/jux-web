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

package xss.it.jux.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xss.it.jux.annotation.Css;
import xss.it.jux.annotation.Favicon;
import xss.it.jux.annotation.Js;
import xss.it.jux.server.WebApplication;

/**
 * JUX Framework Demo Application.
 *
 * <p>Demonstrates how a consumer project uses JUX to build
 * pages entirely in Java with Bootstrap 5 styling.</p>
 *
 * <p>Global CSS and JS resources are declared here once via
 * {@code @Css}/{@code @Js} annotations. All pages inherit
 * these automatically — no need to repeat on each page class.</p>
 *
 * <p>Run with: {@code ./gradlew :jux-demo:bootRun}
 * Then open {@code http://localhost:9090}</p>
 */
@SpringBootApplication
@Css(value = "/css/bootstrap.min.css", order = 1)
@Css(value = "/css/bootstrap-icons.min.css", order = 2)
@Css(value = "/css/demo.css", order = 10)
@Js(value = "/js/bootstrap.bundle.min.js", defer = true, order = 1)
@Favicon(value = "/img/jux-logo.png", type = "image/png")
public class DemoApplication implements WebApplication {

    static void main(String... args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
