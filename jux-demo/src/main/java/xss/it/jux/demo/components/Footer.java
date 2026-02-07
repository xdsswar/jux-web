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

package xss.it.jux.demo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.core.JuxMessages;

import static xss.it.jux.core.Elements.*;

/**
 * Site footer with links and copyright.
 */
public class Footer extends Component {

    private final JuxMessages messages;

    public Footer(JuxMessages messages) {
        this.messages = messages;
    }

    @Override
    public Element render() {
        return footer().cls("bg-dark", "text-light", "pt-5", "pb-3", "mt-5").children(
            div().cls("container").children(
                div().cls("row").children(
                    div().cls("col-md-4", "mb-3").children(
                        p().cls("h5").text(messages.getString("footer.framework")),
                        p().cls("text-secondary").text(messages.getString("footer.description"))
                    ),
                    div().cls("col-md-4", "mb-3").children(
                        p().cls("h5").text(messages.getString("footer.links")),
                        ul().cls("list-unstyled").children(
                            li().children(a().cls("text-secondary", "text-decoration-none")
                                .attr("href", "/").text(messages.getString("nav.home"))),
                            li().children(a().cls("text-secondary", "text-decoration-none")
                                .attr("href", "/about").text(messages.getString("nav.about"))),
                            li().children(a().cls("text-secondary", "text-decoration-none")
                                .attr("href", "/blog").text(messages.getString("nav.blog"))),
                            li().children(a().cls("text-secondary", "text-decoration-none")
                                .attr("href", "/contact").text(messages.getString("nav.contact")))
                        )
                    ),
                    div().cls("col-md-4", "mb-3").children(
                        p().cls("h5").text(messages.getString("footer.built_with")),
                        ul().cls("list-unstyled", "text-secondary").children(
                            li().text("Java 25"),
                            li().text("Spring Boot 3.5"),
                            li().text("JUX Web Framework"),
                            li().text("Bootstrap 5")
                        )
                    )
                ),
                hr().cls("border-secondary"),
                p().cls("text-center", "text-secondary", "mb-0")
                    .text(messages.getString("footer.copyright"))
            )
        );
    }
}
