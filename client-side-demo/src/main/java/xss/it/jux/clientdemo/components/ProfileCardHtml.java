/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.html.HtmlLoader;
import xss.it.jux.html.annotation.Html;
import xss.it.jux.html.annotation.HtmlId;

/**
 * User profile card component loaded from an HTML template via the jux-html module.
 *
 * <p>This component demonstrates the {@link Html @Html} and {@link HtmlId @HtmlId}
 * annotations for template-driven rendering. Instead of building the entire
 * element tree programmatically in Java, the visual layout is defined in an
 * HTML file ({@code templates/components/profile-card.html}) and specific
 * elements are injected into Java fields by their {@code id} attribute.</p>
 *
 * <h2>JUX-HTML Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @Html} template loading</b> — the component's layout is
 *       defined in a separate HTML file, parsed by {@link xss.it.jux.html.HtmlParser},
 *       and cached by {@link xss.it.jux.html.TemplateCache}.</li>
 *   <li><b>{@code @HtmlId} element injection</b> — fields annotated with
 *       {@code @HtmlId} are automatically populated with the matching
 *       {@code id=""} element from the parsed template tree.</li>
 *   <li><b>Programmatic mutation</b> — after injection, the component
 *       modifies text content and attributes of the injected elements
 *       to populate them with dynamic data.</li>
 * </ul>
 *
 * <h2>Template Elements</h2>
 * <ul>
 *   <li>{@code id="avatar"} — the avatar circle with initials</li>
 *   <li>{@code id="name"} — the user's full name heading</li>
 *   <li>{@code id="role"} — the user's job title</li>
 *   <li>{@code id="bio"} — a short biography paragraph</li>
 *   <li>{@code id="stat-projects"} — project count stat</li>
 *   <li>{@code id="stat-followers"} — follower count stat</li>
 *   <li>{@code id="stat-rating"} — rating stat</li>
 *   <li>{@code id="profile-link"} — the "View Profile" link</li>
 * </ul>
 *
 * @see Html
 * @see HtmlId
 * @see HtmlLoader
 */
@Html("components/profile-card.html")
public class ProfileCardHtml extends Component {

    /** The avatar circle element — text is set to the user's initials. */
    @HtmlId
    private Element avatar;

    /** The name heading — populated with the user's full name. */
    @HtmlId
    private Element name;

    /** The role/title line below the name. */
    @HtmlId
    private Element role;

    /** The biography paragraph. */
    @HtmlId
    private Element bio;

    /** Number of projects statistic. */
    @HtmlId("stat-projects")
    private Element statProjects;

    /** Number of followers statistic. */
    @HtmlId("stat-followers")
    private Element statFollowers;

    /** Rating statistic. */
    @HtmlId("stat-rating")
    private Element statRating;

    /** The "View Profile" action link. */
    @HtmlId("profile-link")
    private Element profileLink;

    /**
     * Load the HTML template, inject elements by ID, populate with
     * sample profile data, and return the root element tree.
     *
     * @return the root element of the profile card
     */
    @Override
    public Element render() {
        /* Load and parse the HTML template, inject @HtmlId fields. */
        Element root = HtmlLoader.load(this);

        /* Populate the injected elements with sample user data. */
        avatar.text("AJ");
        name.text("Alice Johnson");
        role.text("Senior Java Architect");
        bio.text("Passionate about building accessible, high-performance web "
                + "applications using pure Java. Core contributor to the JUX "
                + "framework and advocate for server-side rendering.");

        statProjects.text("47");
        statFollowers.text("1.2k");
        statRating.text("4.9");

        profileLink.attr("href", "/users/alice-johnson");

        return root;
    }
}
