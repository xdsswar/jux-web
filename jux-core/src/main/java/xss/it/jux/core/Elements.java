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

package xss.it.jux.core;

/**
 * Static factory methods for creating HTML5 {@link Element} nodes.
 *
 * <p>This is the primary API for building UI in JUX. Import it statically
 * and compose element trees declaratively:</p>
 * <pre>{@code
 * import static xss.it.jux.core.Elements.*;
 *
 * main_().children(
 *     h1().text("Hello"),
 *     p().text("Built with JUX.")
 * )
 * }</pre>
 *
 * <p><b>Design principles:</b></p>
 * <ul>
 *   <li>Semantic HTML first -- use {@code section()}, {@code nav()}, {@code article()}
 *       before reaching for {@code div()}</li>
 *   <li>ADA by default -- {@code img()} requires alt text (no alt-less overload exists),
 *       skip-nav and screen-reader helpers are built in</li>
 *   <li>Every method returns a new {@link Element} for fluent chaining</li>
 * </ul>
 *
 * <p>This class cannot be instantiated. All methods are static.</p>
 *
 * @see Element
 * @see Component#render()
 */
public final class Elements {

    /** Private constructor prevents instantiation of this utility class. */
    private Elements() {}

    /*
     * ═══════════════════════════════════════════════════════════════
     *  SEMANTIC STRUCTURE
     *  Prefer these over div() whenever the content has meaning.
     *  Screen readers and search engines use these to understand
     *  page structure. (WCAG 1.3.1 -- Info and Relationships)
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <header>} element -- page or section header.
     *
     * <p>Contains heading content, logo, and navigation. Use at the page
     * level for the site header, or inside {@code article()} / {@code section()}
     * for a section-level header.</p>
     *
     * @return a new header element
     */
    public static Element header()     { return Element.of("header"); }

    /**
     * Creates a {@code <nav>} element -- navigation section.
     *
     * <p>Should contain links for navigating the site or page. Use
     * {@code .aria("label", "...")} to distinguish multiple {@code <nav>}
     * elements on the same page (e.g. "Main navigation" vs "Footer navigation").</p>
     *
     * @return a new nav element
     */
    public static Element nav()        { return Element.of("nav"); }

    /**
     * Creates a {@code <main>} element -- primary content area.
     *
     * <p>There should be only ONE {@code <main>} element per page. The
     * trailing underscore avoids a conflict with the Java keyword {@code main}.
     * This is the target for skip-navigation links (WCAG 2.4.1).</p>
     *
     * @return a new main element
     */
    public static Element main_()      { return Element.of("main"); }

    /**
     * Creates an {@code <aside>} element -- sidebar or complementary content.
     *
     * <p>Content tangentially related to the main content -- sidebars,
     * pull quotes, advertising, related links. Can be used within an
     * {@code article()} for content related to that article.</p>
     *
     * @return a new aside element
     */
    public static Element aside()      { return Element.of("aside"); }

    /**
     * Creates a {@code <footer>} element -- page or section footer.
     *
     * <p>Contains information about the nearest sectioning content:
     * copyright, contact info, links to related documents, sitemap.</p>
     *
     * @return a new footer element
     */
    public static Element footer()     { return Element.of("footer"); }

    /**
     * Creates a {@code <section>} element -- thematic grouping of content.
     *
     * <p>Usually has a heading as a child. Use when the content forms a
     * distinct part of a document. Prefer over {@code div()} when the
     * grouping has semantic meaning.</p>
     *
     * @return a new section element
     */
    public static Element section()    { return Element.of("section"); }

    /**
     * Creates an {@code <article>} element -- self-contained content.
     *
     * <p>Represents independently distributable or reusable content:
     * blog posts, news stories, comments, widgets, forum posts.</p>
     *
     * @return a new article element
     */
    public static Element article()    { return Element.of("article"); }

    /**
     * Creates a {@code <figure>} element -- self-contained figure.
     *
     * <p>Wraps an image, diagram, code listing, or illustration with
     * an optional caption via {@link #figcaption()}. The caption should
     * be the first or last child.</p>
     *
     * @return a new figure element
     */
    public static Element figure()     { return Element.of("figure"); }

    /**
     * Creates a {@code <figcaption>} element -- caption for a figure.
     *
     * <p>Must be a child of {@link #figure()}. Should be either the first
     * or last child element.</p>
     *
     * @return a new figcaption element
     */
    public static Element figcaption() { return Element.of("figcaption"); }

    /**
     * Creates a {@code <details>} element -- disclosure widget.
     *
     * <p>An expandable/collapsible content container. Must contain a
     * {@link #summary()} child as the visible heading. Add the
     * {@code "open"} attribute to make it initially expanded.</p>
     *
     * @return a new details element
     */
    public static Element details()    { return Element.of("details"); }

    /**
     * Creates a {@code <summary>} element -- visible heading for a details element.
     *
     * <p>Must be the first child of a {@link #details()} element. Clicking
     * the summary toggles the parent details open/closed.</p>
     *
     * @return a new summary element
     */
    public static Element summary()    { return Element.of("summary"); }

    /**
     * Creates a {@code <dialog>} element -- modal or non-modal dialog box.
     *
     * <p>Use with {@code role("dialog")} and implement a focus trap for
     * modal dialogs. The {@code open} attribute controls visibility. For
     * accessible modals, use the built-in {@code JuxModal} component from
     * {@code jux-themes} instead.</p>
     *
     * @return a new dialog element
     */
    public static Element dialog()     { return Element.of("dialog"); }

    /**
     * Creates a {@code <search>} element -- search section.
     *
     * <p>Semantically identifies a search form area. Contains forms or
     * content related to search or filtering functionality.</p>
     *
     * @return a new search element
     */
    public static Element search()     { return Element.of("search"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  HEADINGS
     *  Always maintain proper heading hierarchy:
     *  h1 -> h2 -> h3. Never skip levels (e.g. h1 -> h3).
     *  One h1 per page (WCAG 2.4.6 -- Headings and Labels).
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates an {@code <h1>} element -- level 1 heading.
     *
     * <p>Should appear exactly once per page. Represents the top-level
     * heading of the page content. The a11y audit flags pages with
     * zero or multiple {@code <h1>} elements.</p>
     *
     * @return a new h1 element
     */
    public static Element h1() { return Element.of("h1"); }

    /**
     * Creates an {@code <h2>} element -- level 2 heading.
     *
     * <p>Major sections under the h1. Must not appear before an h1 in the
     * document heading hierarchy.</p>
     *
     * @return a new h2 element
     */
    public static Element h2() { return Element.of("h2"); }

    /**
     * Creates an {@code <h3>} element -- level 3 heading.
     *
     * <p>Subsections under an h2. Must not skip heading levels
     * (e.g. h1 directly to h3 without an intervening h2).</p>
     *
     * @return a new h3 element
     */
    public static Element h3() { return Element.of("h3"); }

    /**
     * Creates an {@code <h4>} element -- level 4 heading.
     *
     * @return a new h4 element
     */
    public static Element h4() { return Element.of("h4"); }

    /**
     * Creates an {@code <h5>} element -- level 5 heading.
     *
     * @return a new h5 element
     */
    public static Element h5() { return Element.of("h5"); }

    /**
     * Creates an {@code <h6>} element -- level 6 heading.
     *
     * @return a new h6 element
     */
    public static Element h6() { return Element.of("h6"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  GENERIC CONTAINERS
     *  Use only when no semantic element fits. Prefer section(),
     *  article(), nav(), aside(), etc. for meaningful grouping.
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <div>} element -- generic block container.
     *
     * <p>Has no semantic meaning. Use sparingly -- prefer semantic elements
     * like {@link #section()}, {@link #article()}, {@link #nav()}, or
     * {@link #aside()} when the content has meaning.</p>
     *
     * @return a new div element
     */
    public static Element div()  { return Element.of("div"); }

    /**
     * Creates a {@code <span>} element -- generic inline container.
     *
     * <p>Has no semantic meaning. Use sparingly -- prefer semantic inline
     * elements like {@link #strong()}, {@link #em()}, {@link #mark()},
     * or {@link #code()} when the content has meaning.</p>
     *
     * @return a new span element
     */
    public static Element span() { return Element.of("span"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  TEXT & INLINE ELEMENTS
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <p>} element -- paragraph of text.
     *
     * @return a new p element
     */
    public static Element p()          { return Element.of("p"); }

    /**
     * Creates an {@code <a>} element -- hyperlink.
     *
     * <p>Always set {@code attr("href", url)}. Use descriptive link text --
     * not "click here" (WCAG 2.4.4 -- Link Purpose). For external links,
     * consider adding {@code attr("target", "_blank")} and
     * {@code attr("rel", "noopener noreferrer")}.</p>
     *
     * @return a new anchor element
     */
    public static Element a()          { return Element.of("a"); }

    /**
     * Creates a {@code <strong>} element -- strong importance (typically bold).
     *
     * <p>Semantically meaningful -- screen readers may use a different voice
     * or emphasis. For purely visual bold, use CSS instead.</p>
     *
     * @return a new strong element
     */
    public static Element strong()     { return Element.of("strong"); }

    /**
     * Creates an {@code <em>} element -- emphasis (typically italic).
     *
     * <p>Semantically meaningful -- screen readers may use a different voice
     * or emphasis. For purely visual italic, use CSS instead.</p>
     *
     * @return a new em element
     */
    public static Element em()         { return Element.of("em"); }

    /**
     * Creates a {@code <small>} element -- side comment, fine print.
     *
     * <p>Used for copyright notices, legal text, caveats, and other
     * supplementary information.</p>
     *
     * @return a new small element
     */
    public static Element small()      { return Element.of("small"); }

    /**
     * Creates a {@code <mark>} element -- highlighted/marked text.
     *
     * <p>Indicates text that is marked or highlighted for reference purposes,
     * such as search result highlighting or marking relevant text.</p>
     *
     * @return a new mark element
     */
    public static Element mark()       { return Element.of("mark"); }

    /**
     * Creates a {@code <code>} element -- inline code fragment.
     *
     * <p>For multi-line code blocks, wrap inside a {@link #pre()} element:
     * {@code pre().children(code().text(sourceCode))}.</p>
     *
     * @return a new code element
     */
    public static Element code()       { return Element.of("code"); }

    /**
     * Creates a {@code <pre>} element -- preformatted text.
     *
     * <p>Preserves whitespace and line breaks. Typically used to wrap
     * {@link #code()} elements for multi-line code blocks.</p>
     *
     * @return a new pre element
     */
    public static Element pre()        { return Element.of("pre"); }

    /**
     * Creates a {@code <blockquote>} element -- extended quotation.
     *
     * <p>For quotes from another source. Add {@code attr("cite", url)} for
     * the source attribution URL.</p>
     *
     * @return a new blockquote element
     */
    public static Element blockquote() { return Element.of("blockquote"); }

    /**
     * Creates a {@code <time>} element -- date/time representation.
     *
     * <p>Set {@code attr("datetime", "2026-02-06")} for the machine-readable
     * value, and use {@code .text("February 6, 2026")} for the human-readable
     * display.</p>
     *
     * @return a new time element
     */
    public static Element time()       { return Element.of("time"); }

    /**
     * Creates an {@code <abbr>} element -- abbreviation.
     *
     * <p>Set {@code attr("title", "full term")} to provide the full expansion
     * on hover and for screen readers.</p>
     *
     * @return a new abbr element
     */
    public static Element abbr()       { return Element.of("abbr"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  LISTS
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <ul>} element -- unordered (bulleted) list.
     *
     * <p>Children must be {@link #li()} elements.</p>
     *
     * @return a new ul element
     */
    public static Element ul() { return Element.of("ul"); }

    /**
     * Creates an {@code <ol>} element -- ordered (numbered) list.
     *
     * <p>Children must be {@link #li()} elements. Use {@code attr("start", "5")}
     * to change the starting number.</p>
     *
     * @return a new ol element
     */
    public static Element ol() { return Element.of("ol"); }

    /**
     * Creates a {@code <li>} element -- list item.
     *
     * <p>Must be a child of {@link #ul()} or {@link #ol()}.</p>
     *
     * @return a new li element
     */
    public static Element li() { return Element.of("li"); }

    /**
     * Creates a {@code <dl>} element -- description list.
     *
     * <p>Used for key-value pairs, glossaries, metadata. Contains
     * alternating {@link #dt()} (term) and {@link #dd()} (description) children.</p>
     *
     * @return a new dl element
     */
    public static Element dl() { return Element.of("dl"); }

    /**
     * Creates a {@code <dt>} element -- description term (the key in a dl).
     *
     * <p>Must be a child of {@link #dl()}.</p>
     *
     * @return a new dt element
     */
    public static Element dt() { return Element.of("dt"); }

    /**
     * Creates a {@code <dd>} element -- description details (the value in a dl).
     *
     * <p>Must be a child of {@link #dl()}, following a {@link #dt()} element.</p>
     *
     * @return a new dd element
     */
    public static Element dd() { return Element.of("dd"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  TABLES
     *  ADA: tables MUST have a <caption> and <th scope="col|row">
     *  for screen reader navigation. The a11y audit enforces this.
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <table>} element -- data table.
     *
     * <p>Must include {@link #caption()}, {@link #thead()}, and {@link #tbody()}.
     * ADA requires a visible caption and {@code <th>} cells with
     * {@code attr("scope", "col")} or {@code attr("scope", "row")} for
     * screen reader navigation (WCAG 1.3.1).</p>
     *
     * @return a new table element
     */
    public static Element table()   { return Element.of("table"); }

    /**
     * Creates a {@code <thead>} element -- table head section.
     *
     * <p>Contains header rows with {@link #th()} cells.</p>
     *
     * @return a new thead element
     */
    public static Element thead()   { return Element.of("thead"); }

    /**
     * Creates a {@code <tbody>} element -- table body section.
     *
     * <p>Contains data rows with {@link #td()} cells.</p>
     *
     * @return a new tbody element
     */
    public static Element tbody()   { return Element.of("tbody"); }

    /**
     * Creates a {@code <tfoot>} element -- table footer section.
     *
     * <p>Contains summary rows (e.g. totals, averages).</p>
     *
     * @return a new tfoot element
     */
    public static Element tfoot()   { return Element.of("tfoot"); }

    /**
     * Creates a {@code <tr>} element -- table row.
     *
     * @return a new tr element
     */
    public static Element tr()      { return Element.of("tr"); }

    /**
     * Creates a {@code <th>} element -- table header cell.
     *
     * <p>ADA: always set {@code attr("scope", "col")} for column headers or
     * {@code attr("scope", "row")} for row headers to enable screen reader
     * navigation (WCAG 1.3.1).</p>
     *
     * @return a new th element
     */
    public static Element th()      { return Element.of("th"); }

    /**
     * Creates a {@code <td>} element -- table data cell.
     *
     * @return a new td element
     */
    public static Element td()      { return Element.of("td"); }

    /**
     * Creates a {@code <caption>} element -- table caption.
     *
     * <p>Provides a visible description of the table's purpose. Required by
     * WCAG 1.3.1. Should be the first child of {@link #table()}.</p>
     *
     * @return a new caption element
     */
    public static Element caption() { return Element.of("caption"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  FORMS
     *  ADA: every input MUST have a <label> (WCAG 3.3.2).
     *  Use label().attr("for", id) paired with input().id(id).
     *  Required fields need ariaRequired(true).
     *  Invalid fields need ariaInvalid(true) + ariaDescribedBy(errorId).
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <form>} element -- form container.
     *
     * <p>Set {@code attr("method", "post")} and {@code attr("action", url)} for
     * form submission. For JUX form handling, use
     * {@code @Route(methods = {HttpMethod.GET, HttpMethod.POST})}.</p>
     *
     * @return a new form element
     */
    public static Element form()     { return Element.of("form"); }

    /**
     * Creates an {@code <input>} element -- text/email/number/etc. input.
     *
     * <p>ADA: requires a paired {@link #label()} element with
     * {@code attr("for", inputId)}. Set the input type via
     * {@code attr("type", "text"|"email"|"password"|"number"|"date"|...)}.</p>
     *
     * @return a new input element
     */
    public static Element input()    { return Element.of("input"); }

    /**
     * Creates a {@code <textarea>} element -- multi-line text input.
     *
     * <p>ADA: requires a paired {@link #label()} element.</p>
     *
     * @return a new textarea element
     */
    public static Element textarea() { return Element.of("textarea"); }

    /**
     * Creates a {@code <select>} element -- dropdown select.
     *
     * <p>ADA: requires a paired {@link #label()} element. Contains
     * {@link #option()} or {@link #optgroup()} children.</p>
     *
     * @return a new select element
     */
    public static Element select()   { return Element.of("select"); }

    /**
     * Creates an {@code <option>} element -- option within a select.
     *
     * <p>Set {@code attr("value", val)} for the form submission value
     * and {@code .text("Display Text")} for the visible label.</p>
     *
     * @return a new option element
     */
    public static Element option()   { return Element.of("option"); }

    /**
     * Creates an {@code <optgroup>} element -- group of options within a select.
     *
     * <p>Set {@code attr("label", "Group Name")} to label the group.</p>
     *
     * @return a new optgroup element
     */
    public static Element optgroup() { return Element.of("optgroup"); }

    /**
     * Creates a {@code <button>} element -- clickable button.
     *
     * <p>Always set {@code attr("type", "button"|"submit"|"reset")} to avoid
     * unexpected form submission behavior (buttons default to type="submit"
     * inside forms).</p>
     *
     * @return a new button element
     */
    public static Element button()   { return Element.of("button"); }

    /**
     * Creates a {@code <label>} element -- label for a form control.
     *
     * <p>Link to its input via {@code attr("for", inputId)}. The {@code for}
     * attribute value must match the target input's {@code id}. ADA requires
     * every form input to have a label (WCAG 3.3.2).</p>
     *
     * @return a new label element
     */
    public static Element label()    { return Element.of("label"); }

    /**
     * Creates a {@code <fieldset>} element -- groups related form controls.
     *
     * <p>Must contain a {@link #legend()} as its first child to label
     * the group. Use for radio button groups, checkbox groups, and
     * related form sections.</p>
     *
     * @return a new fieldset element
     */
    public static Element fieldset() { return Element.of("fieldset"); }

    /**
     * Creates a {@code <legend>} element -- caption for a fieldset.
     *
     * <p>Must be the first child of a {@link #fieldset()} element.</p>
     *
     * @return a new legend element
     */
    public static Element legend()   { return Element.of("legend"); }

    /**
     * Creates an {@code <output>} element -- result of a calculation or user action.
     *
     * <p>Use {@code attr("for", "input1 input2")} to reference the inputs
     * that contribute to the output.</p>
     *
     * @return a new output element
     */
    public static Element output()   { return Element.of("output"); }

    /**
     * Creates a {@code <progress>} element -- progress bar.
     *
     * <p>Set {@code attr("value", n)} for the current value and
     * {@code attr("max", total)} for the maximum. Omit {@code value} for
     * an indeterminate progress bar.</p>
     *
     * @return a new progress element
     */
    public static Element progress() { return Element.of("progress"); }

    /**
     * Creates a {@code <meter>} element -- scalar measurement within a known range.
     *
     * <p>Set {@code attr("min", lo)}, {@code attr("max", hi)}, and
     * {@code attr("value", current)}. Use for gauges, not for progress --
     * use {@link #progress()} instead.</p>
     *
     * @return a new meter element
     */
    public static Element meter()    { return Element.of("meter"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  MEDIA
     *  ADA: images need alt text, videos need captions,
     *  iframes need titles.
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a {@code <picture>} element -- container for responsive image sources.
     *
     * <p>Contains {@link #source()} children for different media conditions
     * and a fallback {@code img()} as the last child.</p>
     *
     * @return a new picture element
     */
    public static Element picture() { return Element.of("picture"); }

    /**
     * Creates a {@code <source>} element -- media source.
     *
     * <p>Used inside {@link #picture()}, {@link #video()}, or {@link #audio()}.
     * Set {@code attr("srcset", url)} and {@code attr("media", query)} for
     * responsive images, or {@code attr("src", url)} and {@code attr("type", mime)}
     * for audio/video sources.</p>
     *
     * @return a new source element
     */
    public static Element source()  { return Element.of("source"); }

    /**
     * Creates a {@code <video>} element -- video player.
     *
     * <p>ADA: include a {@link #track()} child for captions (WCAG 1.2.2).
     * Set {@code attr("controls", "true")} to show playback controls.</p>
     *
     * @return a new video element
     */
    public static Element video()   { return Element.of("video"); }

    /**
     * Creates an {@code <audio>} element -- audio player.
     *
     * <p>ADA: provide a text transcript nearby for accessibility.
     * Set {@code attr("controls", "true")} to show playback controls.</p>
     *
     * @return a new audio element
     */
    public static Element audio()   { return Element.of("audio"); }

    /**
     * Creates a {@code <track>} element -- text track for video/audio.
     *
     * <p>Provides captions, subtitles, or descriptions. Set
     * {@code attr("kind", "captions")}, {@code attr("src", vttUrl)},
     * {@code attr("srclang", "en")}, and {@code attr("label", "English")}.</p>
     *
     * @return a new track element
     */
    public static Element track()   { return Element.of("track"); }

    /**
     * Creates a {@code <canvas>} element -- drawing surface.
     *
     * <p>ADA: set {@code aria("label", "description")} to describe the
     * canvas content for screen readers. Canvas content is not accessible
     * by default -- provide a fallback description or use ARIA properties.</p>
     *
     * @return a new canvas element
     */
    public static Element canvas()  { return Element.of("canvas"); }

    /**
     * Creates an {@code <svg>} element -- inline SVG container.
     *
     * <p>For inline SVG graphics. Add {@code aria("label", "...")} or
     * a {@code <title>} child element for accessibility.</p>
     *
     * @return a new svg element
     */
    public static Element svg()     { return Element.of("svg"); }

    /**
     * Creates an {@code <iframe>} element -- inline frame.
     *
     * <p>ADA: must have {@code attr("title", "descriptive title")} to
     * describe the iframe content for screen readers (WCAG 4.1.2).</p>
     *
     * @return a new iframe element
     */
    public static Element iframe()  { return Element.of("iframe"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  MISC
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates an {@code <hr>} element -- thematic break / horizontal rule.
     *
     * <p>Represents a semantic section separator. Use between distinct
     * sections of content. For decorative lines, use CSS borders instead.</p>
     *
     * @return a new hr element
     */
    public static Element hr()  { return Element.of("hr"); }

    /**
     * Creates a {@code <br>} element -- line break.
     *
     * <p>Use sparingly -- prefer CSS margins and padding for spacing.
     * Appropriate for addresses, poetry, or other content where line
     * breaks are part of the content structure.</p>
     *
     * @return a new br element
     */
    public static Element br()  { return Element.of("br"); }

    /**
     * Creates a {@code <wbr>} element -- word break opportunity.
     *
     * <p>Hints to the browser where a long word or URL can be broken
     * across lines if necessary. Does not force a break.</p>
     *
     * @return a new wbr element
     */
    public static Element wbr() { return Element.of("wbr"); }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  ADA-ENFORCED IMAGES
     *
     *  There is intentionally NO img() method without alt text.
     *  This is a compile-time guarantee: you cannot create an image
     *  in JUX without providing accessibility text.
     *  (WCAG 1.1.1 -- Non-text Content)
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates an {@code <img>} element with REQUIRED alt text.
     *
     * <p>There is intentionally no {@code img(src)} overload -- alt text is
     * mandatory in JUX. This is a compile-time guarantee that you cannot
     * create an image without providing accessibility text (WCAG 1.1.1 --
     * Non-text Content).</p>
     *
     * <p>For decorative images that add no information, use
     * {@link #imgDecorative(String)} instead.</p>
     *
     * @param src the image URL (relative path or absolute URL)
     * @param alt description of the image for screen readers
     *            (e.g. "Team photo in the office", "Bar chart showing Q3 revenue")
     * @return an img element with src and alt attributes set
     */
    public static Element img(String src, String alt) {
        return Element.of("img").attr("src", src).attr("alt", alt);
    }

    /**
     * Creates a decorative {@code <img>} element hidden from assistive technology.
     *
     * <p>Sets {@code alt=""} and {@code role="presentation"} to hide the image
     * from screen readers. Use for visual flourishes, background textures,
     * icons that duplicate adjacent text, and other images that convey no
     * information.</p>
     *
     * <p>If the image conveys any meaning at all, use {@link #img(String, String)}
     * with descriptive alt text instead.</p>
     *
     * @param src the image URL
     * @return an img element hidden from assistive technology
     */
    public static Element imgDecorative(String src) {
        return Element.of("img").attr("src", src).attr("alt", "").attr("role", "presentation");
    }

    /*
     * ═══════════════════════════════════════════════════════════════
     *  ACCESSIBILITY HELPERS
     *  Pre-built patterns for common ADA requirements.
     * ═══════════════════════════════════════════════════════════════
     */

    /**
     * Creates a skip-navigation link for keyboard users (WCAG 2.4.1 -- Bypass Blocks).
     *
     * <p>Allows keyboard users to jump past repeated navigation directly to
     * the main content. The link is visually hidden (via the {@code jux-skip-nav}
     * CSS class) until it receives keyboard focus. {@code DefaultLayout}
     * auto-injects this.</p>
     *
     * @param targetId the {@code id} of the main content element to skip to
     *                 (e.g. "main-content")
     * @param text     link text announced by screen readers
     *                 (e.g. "Skip to main content")
     * @return a visually-hidden anchor that appears on keyboard focus
     */
    public static Element skipNav(String targetId, String text) {
        return a().cls("jux-skip-nav").attr("href", "#" + targetId).text(text);
    }

    /**
     * Creates a screen-reader-only text span -- visually hidden but announced
     * by assistive technology.
     *
     * <p>Uses the {@code jux-sr-only} CSS class to position the text off-screen.
     * The text is not visible to sighted users but is read aloud by screen
     * readers. Use for additional context that is visually obvious but not
     * programmatically apparent (e.g. "Opens in new window", "External link").</p>
     *
     * @param text the text to announce to screen readers
     * @return a visually-hidden span with the given text
     */
    public static Element srOnly(String text) {
        return span().cls("jux-sr-only").text(text);
    }

    /**
     * Creates an ARIA live region for announcing dynamic content updates
     * to screen readers.
     *
     * <p>Content inserted or changed inside this element will be automatically
     * spoken by screen readers. Use for toast messages, loading indicators,
     * live search results, chat messages, and real-time data updates.</p>
     *
     * <p>The element is configured with {@code aria-atomic="true"} so the
     * entire region content is announced on any change.</p>
     *
     * @param politeness the announcement politeness level:
     *                   {@code "polite"} (wait for user idle, preferred) or
     *                   {@code "assertive"} (interrupt current speech immediately)
     * @return a div configured as a live region with the {@code jux-live-region} CSS class
     */
    public static Element liveRegion(String politeness) {
        return div().ariaLive(politeness).ariaAtomic(true).cls("jux-live-region");
    }
}
