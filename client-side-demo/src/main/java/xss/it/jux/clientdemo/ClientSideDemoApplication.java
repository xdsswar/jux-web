/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xss.it.jux.annotation.Css;
import xss.it.jux.annotation.Favicon;
import xss.it.jux.annotation.Js;
import xss.it.jux.server.WebApplication;

/**
 * JUX Client-Side Demo — Interactive components showcase.
 *
 * <p>Demonstrates how JUX builds interactive, client-side components
 * entirely in Java using {@code @JuxComponent(clientSide = true)},
 * {@code @State}, {@code @On}, {@code @OnMount}, and {@code @OnUnmount}.
 * All Java code is compiled to JavaScript via TeaVM for browser execution.</p>
 *
 * <p>This demo uses Tailwind CSS for modern, utility-first styling.
 * All CSS and JS assets are served from local resources — no CDN
 * dependencies at runtime.</p>
 *
 * <p>Run with: {@code ./gradlew :client-side-demo:bootRun}
 * Then open {@code http://localhost:9092}</p>
 */
@SpringBootApplication
@Css(value = "/css/tailwind.min.css", order = 1)
@Css(value = "/css/demo.css", order = 10)
@Js(value = "/js/jux-client.js", order = 1)
@Js(value = "/js/jux-boot.js", order = 2)
@Favicon(value = "/img/favicon.svg", type = "image/svg+xml")
public class ClientSideDemoApplication implements WebApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    static void main(String... args) {
        SpringApplication.run(ClientSideDemoApplication.class, args);
    }
}
