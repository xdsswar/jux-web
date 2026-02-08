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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import xss.it.jux.annotation.JuxComponent;

/**
 * Virtual DOM node -- the core building block of JUX's rendering model.
 *
 * <p>An Element represents a single HTML tag with its attributes, children,
 * text content, event handlers, and ARIA properties. Elements form a tree
 * that is:</p>
 *
 * <ul>
 *   <li><b>SSR:</b> serialized to an HTML string by {@code JuxRenderer}</li>
 *   <li><b>Client:</b> mapped 1:1 to {@code org.teavm.jso.dom.html.HTMLElement}
 *       via {@code JuxDomBridge}. On re-render, old and new trees are diffed
 *       and only changed nodes are patched in the real DOM.</li>
 * </ul>
 *
 * <p>Elements are created via the fluent builder pattern.
 * Use {@link Elements} static factories as the primary entry point:</p>
 * <pre>{@code
 * import static xss.it.jux.core.Elements.*;
 *
 * div().cls("card").children(
 *     h2().text("Title"),
 *     p().text("Description"),
 *     a().attr("href", "/more").text("Read more")
 * )
 * }</pre>
 *
 * <p>All builder methods return {@code this} for chaining.
 * Elements are effectively immutable after construction -- once built and
 * returned from {@link Component#render()}, they should not be mutated.
 * Builder methods mutate in place during construction for efficiency.</p>
 *
 * @see Elements
 * @see Component#render()
 */
public class Element {

    // ── Internal state ───────────────────────────────────────────

    /**
     * Global counter for generating unique {@code data-jux-id} values
     * for client-side component instances during SSR.
     */
    private static final AtomicInteger JUX_ID_COUNTER = new AtomicInteger(0);

    /** The HTML tag name for this element (e.g. "div", "section", "h1"). Never null. */
    private final String tag;

    /**
     * Arbitrary HTML attributes stored as name-value pairs in insertion order.
     * Includes id, role, lang, tabindex, and all {@code aria-*} attributes.
     * The {@code class} and {@code style} attributes are computed separately
     * from {@link #cssClasses} and {@link #styles} at read time via
     * {@link #getAttributes()}.
     */
    private final Map<String, String> attributes;

    /**
     * CSS class names accumulated via {@link #cls(String...)}. Joined with spaces
     * into the {@code class} attribute when {@link #getAttributes()} is called.
     */
    private final List<String> cssClasses;

    /**
     * Ordered child elements. Populated via {@link #children(Element...)} or
     * {@link #child(Component)}. Rendered in order during SSR serialization.
     */
    private final List<Element> children;

    /**
     * Plain text content of this element. Set via {@link #text(String)}.
     * Mutually exclusive with children in practice -- if both are set,
     * the renderer outputs text first, then children. Prefer using one
     * or the other, not both. HTML-escaped during SSR by the renderer.
     */
    private String textContent;

    /**
     * DOM event handlers keyed by event name (e.g. "click", "input").
     * Only active on client-side components compiled via TeaVM. During SSR,
     * these are serialized as {@code data-jux-event} attributes for hydration.
     */
    private final Map<String, EventHandler> eventHandlers;

    /**
     * Inline CSS style properties stored as property-value pairs in insertion order.
     * Joined into the {@code style} attribute (e.g. "color: red; padding: 1rem")
     * when {@link #getAttributes()} is called.
     */
    private final Map<String, String> styles;

    // ── Constructor (private) ────────────────────────────────────

    /**
     * Private constructor -- use {@link #of(String)} or the {@link Elements}
     * static factories to create instances.
     *
     * <p>Initializes all internal collections as empty mutable structures.
     * The element is built incrementally via fluent builder methods and
     * then treated as effectively immutable once returned from
     * {@link Component#render()}.</p>
     *
     * @param tag the HTML tag name, must not be null
     * @throws NullPointerException if tag is null
     */
    private Element(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag must not be null");
        this.attributes = new LinkedHashMap<>();
        this.cssClasses = new ArrayList<>();
        this.children = new ArrayList<>();
        this.textContent = null;
        this.eventHandlers = new LinkedHashMap<>();
        this.styles = new LinkedHashMap<>();
    }

    // ── Factory ──────────────────────────────────────────────────

    /**
     * Create an Element for the given HTML tag.
     *
     * <p>Prefer the convenience factories in {@link Elements} over this method.
     * Use this directly only for non-standard tags or web components.</p>
     *
     * @param tag the HTML tag name (e.g. "div", "section", "my-widget")
     * @return a new Element for that tag
     * @throws NullPointerException if tag is null
     */
    public static Element of(String tag) {
        return new Element(tag);
    }

    // ── Builder -- general attributes ────────────────────────────

    /**
     * Set an arbitrary HTML attribute.
     *
     * <p>Setting an attribute to {@code null} is a no-op; the attribute
     * is not added. To remove an attribute, build a new Element without it.</p>
     *
     * @param key   attribute name (e.g. "href", "data-id", "type")
     * @param value attribute value (e.g. "/about", "42", "email")
     * @return this element for chaining
     * @throws NullPointerException if key is null
     */
    public Element attr(String key, String value) {
        Objects.requireNonNull(key, "attribute key must not be null");
        if (value != null) {
            attributes.put(key, value);
        }
        return this;
    }

    /**
     * Add one or more CSS classes.
     *
     * <p>Multiple calls are additive: {@code cls("a").cls("b")} produces
     * {@code class="a b"}. Null or empty class names are silently skipped.</p>
     *
     * @param classes one or more CSS class names
     * @return this element for chaining
     */
    public Element cls(String... classes) {
        if (classes != null) {
            for (String cls : classes) {
                if (cls != null && !cls.isEmpty()) {
                    cssClasses.add(cls);
                }
            }
        }
        return this;
    }

    /**
     * Set the element's ID attribute.
     *
     * <p>IDs must be unique within a page. Used for anchor links,
     * ARIA references ({@code aria-controls}, {@code aria-labelledby}),
     * and client-side element lookup.</p>
     *
     * @param id unique identifier for this element
     * @return this element for chaining
     */
    public Element id(String id) {
        return attr("id", id);
    }

    /**
     * Set an inline CSS style property.
     *
     * <p>Prefer CSS classes via {@link #cls(String...)} for reusable styles.
     * Use inline styles only for dynamic, instance-specific values
     * (e.g. CMS-configured widget backgrounds, computed widths).</p>
     *
     * @param property CSS property name (e.g. "background-color", "padding")
     * @param value    CSS value (e.g. "#ff0000", "2rem 1rem")
     * @return this element for chaining
     * @throws NullPointerException if property is null
     */
    public Element style(String property, String value) {
        Objects.requireNonNull(property, "style property must not be null");
        if (value != null) {
            styles.put(property, value);
        }
        return this;
    }

    /**
     * Set the text content of this element.
     *
     * <p>Text is HTML-escaped during SSR (no XSS risk). The escaping is
     * performed by the renderer, not by this method.</p>
     *
     * <p>Overwrites any previous text content. Cannot be meaningfully
     * combined with {@link #children} -- use a child {@code span().text(...)}
     * instead if you need both children and text.</p>
     *
     * @param content the text to display inside this element
     * @return this element for chaining
     */
    public Element text(String content) {
        this.textContent = content;
        return this;
    }

    /**
     * Add child elements (varargs).
     *
     * <p>Children are rendered in order inside this element.
     * Null children are silently skipped. Use this for static, known
     * child lists.</p>
     *
     * @param children the child elements to nest inside this element
     * @return this element for chaining
     */
    public Element children(Element... children) {
        if (children != null) {
            for (Element child : children) {
                if (child != null) {
                    this.children.add(child);
                }
            }
        }
        return this;
    }

    /**
     * Add child elements (List).
     *
     * <p>Use this for dynamic child lists, e.g. from a stream:</p>
     * <pre>{@code
     * ul().children(
     *     items.stream().map(i -> li().text(i.name())).toList()
     * )
     * }</pre>
     *
     * <p>Null entries in the list are silently skipped.</p>
     *
     * @param children list of child elements
     * @return this element for chaining
     */
    public Element children(List<Element> children) {
        if (children != null) {
            for (Element child : children) {
                if (child != null) {
                    this.children.add(child);
                }
            }
        }
        return this;
    }

    /**
     * Embed another Component as a child.
     *
     * <p>The component's {@link Component#render()} is called immediately
     * and its output Element tree is inserted as a child of this element.</p>
     *
     * <p>If the component class is annotated with
     * {@code @JuxComponent(clientSide = true)}, the root element of the
     * rendered tree is automatically tagged with {@code data-jux-id} and
     * {@code data-jux-class} attributes. These markers enable the TeaVM
     * client runtime to discover and hydrate the component after SSR.</p>
     *
     * @param component the child component to render inline
     * @return this element for chaining
     * @throws NullPointerException if component is null
     */
    public Element child(Component component) {
        Objects.requireNonNull(component, "component must not be null");
        Element rendered = component.render();
        if (rendered != null) {
            JuxComponent annotation = component.getClass().getAnnotation(JuxComponent.class);
            if (annotation != null && annotation.clientSide()) {
                String className = component.getClass().getName();
                String simpleName = component.getClass().getSimpleName();
                String instanceId = simpleName.toLowerCase() + "-"
                        + JUX_ID_COUNTER.incrementAndGet();
                rendered.attr("data-jux-id", instanceId);
                rendered.attr("data-jux-class", className);
            }
            this.children.add(rendered);
        }
        return this;
    }

    /**
     * Register a DOM event handler.
     *
     * <p>Only active on client-side components
     * ({@code @JuxComponent(clientSide = true)}). During SSR, event handlers
     * are serialized as {@code data-jux-event} attributes for hydration.</p>
     *
     * @param event   DOM event name (e.g. "click", "input", "keydown", "submit")
     * @param handler the callback to invoke when the event fires
     * @return this element for chaining
     * @throws NullPointerException if event or handler is null
     */
    public Element on(String event, EventHandler handler) {
        Objects.requireNonNull(event, "event name must not be null");
        Objects.requireNonNull(handler, "event handler must not be null");
        eventHandlers.put(event, handler);
        return this;
    }

    // ── ARIA -- accessibility attributes (first-class, not afterthoughts) ──

    /**
     * Set an arbitrary ARIA property.
     *
     * <p>Renders as {@code aria-{property}="{value}"} in the HTML output.
     * Use the typed convenience methods below when available.</p>
     *
     * @param property ARIA property name without the "aria-" prefix (e.g. "label", "live")
     * @param value    the property value
     * @return this element for chaining
     */
    public Element aria(String property, String value) {
        return attr("aria-" + property, value);
    }

    /**
     * Set the ARIA role.
     *
     * <p>Tells assistive technology what kind of widget this element represents.
     * Common roles: "button", "dialog", "alert", "tablist", "navigation", "banner".
     * Prefer semantic HTML elements (e.g. {@code nav()}, {@code button()}) over
     * generic elements with roles.</p>
     *
     * @param role the WAI-ARIA role
     * @return this element for chaining
     * @see <a href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">WAI-ARIA Roles</a>
     */
    public Element role(String role) {
        return attr("role", role);
    }

    /**
     * Set the tab order position.
     *
     * <ul>
     *   <li>{@code 0}: element is focusable in normal tab order</li>
     *   <li>{@code -1}: element is focusable programmatically but skipped in tab order</li>
     *   <li>{@code >0}: explicitly ordered (discouraged -- WCAG 2.4.3 warns about this)</li>
     * </ul>
     *
     * @param index the tabindex value
     * @return this element for chaining
     */
    public Element tabIndex(int index) {
        return attr("tabindex", String.valueOf(index));
    }

    /**
     * Set the aria-live politeness level for dynamic content regions.
     *
     * <p>Content changes inside this element will be announced by screen readers.</p>
     * <ul>
     *   <li>{@code "polite"}: announced when the user is idle (preferred)</li>
     *   <li>{@code "assertive"}: announced immediately, interrupting current speech</li>
     *   <li>{@code "off"}: not announced (default)</li>
     * </ul>
     *
     * @param mode "polite", "assertive", or "off"
     * @return this element for chaining
     * @see #ariaAtomic(boolean)
     */
    public Element ariaLive(String mode) {
        return aria("live", mode);
    }

    /**
     * Set whether the entire live region should be announced on changes.
     *
     * <p>When {@code true}, any change within an {@code aria-live} region causes
     * the entire region to be re-announced. When {@code false} (default),
     * only the changed nodes are announced.</p>
     *
     * @param atomic true to announce the full region, false for changed parts only
     * @return this element for chaining
     */
    public Element ariaAtomic(boolean atomic) {
        return aria("atomic", String.valueOf(atomic));
    }

    /**
     * Hide this element from the accessibility tree.
     *
     * <p>The element is still visible on screen but invisible to screen readers.
     * Use for decorative content that adds no information.
     * <b>Never hide interactive or meaningful content.</b></p>
     *
     * @param hidden true to hide from assistive technology
     * @return this element for chaining
     */
    public Element ariaHidden(boolean hidden) {
        return aria("hidden", String.valueOf(hidden));
    }

    /**
     * Indicate whether a collapsible section is expanded or collapsed.
     *
     * <p>Used on accordion triggers, dropdown buttons, tree nodes.
     * The controlled content should be referenced via {@link #ariaControls(String)}.</p>
     *
     * @param expanded true if the controlled content is currently visible
     * @return this element for chaining
     */
    public Element ariaExpanded(boolean expanded) {
        return aria("expanded", String.valueOf(expanded));
    }

    /**
     * Reference the element that this element controls.
     *
     * <p>Tells assistive technology which element's visibility or content
     * changes when this element is activated. The target ID must match
     * an element's {@code id} attribute in the same page.</p>
     *
     * @param id the ID of the controlled element
     * @return this element for chaining
     */
    public Element ariaControls(String id) {
        return aria("controls", id);
    }

    /**
     * Reference the element(s) that label this element.
     *
     * <p>Alternative to a visible {@code <label>} when the labelling element
     * isn't a {@code <label>} tag. Multiple IDs can be space-separated.</p>
     *
     * @param id one or more IDs of the labelling elements (space-separated)
     * @return this element for chaining
     */
    public Element ariaLabelledBy(String id) {
        return aria("labelledby", id);
    }

    /**
     * Reference the element that describes this element in detail.
     *
     * <p>Points to a longer description -- like help text below a form field
     * or an error message. The target element must have the referenced {@code id}.</p>
     *
     * @param id the ID of the describing element
     * @return this element for chaining
     */
    public Element ariaDescribedBy(String id) {
        return aria("describedby", id);
    }

    /**
     * Mark a form field as required.
     *
     * <p>Screen readers announce "required" when the user focuses the field.
     * Also consider using the HTML {@code required} attribute on form inputs.</p>
     *
     * @param required true if the field must be filled before submission
     * @return this element for chaining
     */
    public Element ariaRequired(boolean required) {
        return aria("required", String.valueOf(required));
    }

    /**
     * Mark a form field as having an invalid value.
     *
     * <p>Screen readers announce "invalid entry" when focused.
     * Combine with {@link #ariaDescribedBy(String)} pointing to the error message.</p>
     *
     * @param invalid true if the field's current value fails validation
     * @return this element for chaining
     */
    public Element ariaInvalid(boolean invalid) {
        return aria("invalid", String.valueOf(invalid));
    }

    /**
     * Indicate the current item within a set.
     *
     * <p>Used in breadcrumbs, pagination, navigation to mark the active item.</p>
     * <ul>
     *   <li>{@code "page"}: current page in pagination</li>
     *   <li>{@code "step"}: current step in a wizard</li>
     *   <li>{@code "location"}: current breadcrumb</li>
     *   <li>{@code "true"}: generic current item</li>
     *   <li>{@code "false"}: not current (default)</li>
     * </ul>
     *
     * @param value the type of "current" indicator
     * @return this element for chaining
     */
    public Element ariaCurrent(String value) {
        return aria("current", value);
    }

    /**
     * Indicate that this element is disabled.
     *
     * <p>Unlike the HTML {@code disabled} attribute, this is for custom widgets
     * that don't use native form elements. The element remains in the DOM
     * but is presented as non-interactive to assistive technology.</p>
     *
     * @param disabled true if the element is not interactive
     * @return this element for chaining
     */
    public Element ariaDisabled(boolean disabled) {
        return aria("disabled", String.valueOf(disabled));
    }

    /**
     * Indicate whether this element is selected within a set.
     *
     * <p>Used for tabs, list items in a listbox, grid cells.</p>
     *
     * @param selected true if this item is currently selected
     * @return this element for chaining
     */
    public Element ariaSelected(boolean selected) {
        return aria("selected", String.valueOf(selected));
    }

    /**
     * Indicate the checked state of a checkbox or toggle.
     *
     * <ul>
     *   <li>{@code "true"}: checked</li>
     *   <li>{@code "false"}: unchecked</li>
     *   <li>{@code "mixed"}: indeterminate/partial</li>
     * </ul>
     *
     * @param value "true", "false", or "mixed"
     * @return this element for chaining
     */
    public Element ariaChecked(String value) {
        return aria("checked", value);
    }

    /**
     * Indicate that this element triggers a popup.
     *
     * <ul>
     *   <li>{@code "menu"}: opens a menu</li>
     *   <li>{@code "listbox"}: opens a listbox</li>
     *   <li>{@code "dialog"}: opens a dialog</li>
     *   <li>{@code "tree"}: opens a tree view</li>
     *   <li>{@code "true"}: generic popup</li>
     *   <li>{@code "false"}: no popup (default)</li>
     * </ul>
     *
     * @param value the type of popup this element triggers
     * @return this element for chaining
     */
    public Element ariaHasPopup(String value) {
        return aria("haspopup", value);
    }

    // ── Language ─────────────────────────────────────────────────

    /**
     * Set the language of this element's content.
     *
     * <p>Rendered as the {@code lang} attribute. Required by WCAG 3.1.2
     * when a section of content is in a different language than the page.
     * Example: a Spanish quote on an English page: {@code .lang("es")}.</p>
     *
     * @param language BCP 47 language tag (e.g. "en", "es", "fr", "ar", "zh-CN")
     * @return this element for chaining
     */
    public Element lang(String language) {
        return attr("lang", language);
    }

    // ── Getters (used by JuxRenderer for SSR and JuxDomBridge for client) ──

    /**
     * Returns the HTML tag name.
     *
     * @return the HTML tag name (e.g. "div", "section", "h1"), never null
     */
    public String getTag() {
        return tag;
    }

    /**
     * Returns all attributes as an unmodifiable merged map.
     *
     * <p>The returned map combines:</p>
     * <ul>
     *   <li>Base attributes set via {@link #attr(String, String)} (includes id, role,
     *       lang, tabindex, and all aria-* attributes)</li>
     *   <li>The {@code class} attribute computed by space-joining all CSS classes
     *       added via {@link #cls(String...)}</li>
     *   <li>The {@code style} attribute computed by joining all inline style
     *       properties set via {@link #style(String, String)} into
     *       {@code "property: value; "} pairs</li>
     * </ul>
     *
     * @return all attributes as an unmodifiable map of name to value
     */
    public Map<String, String> getAttributes() {
        Map<String, String> merged = new LinkedHashMap<>(attributes);

        if (!cssClasses.isEmpty()) {
            merged.put("class", String.join(" ", cssClasses));
        }

        if (!styles.isEmpty()) {
            String styleValue = styles.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining("; "));
            merged.put("style", styleValue);
        }

        return Collections.unmodifiableMap(merged);
    }

    /**
     * Returns child elements in render order.
     *
     * @return child elements as an unmodifiable list
     */
    public List<Element> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns the text content.
     *
     * @return text content, or null if this element has children instead
     */
    public String getTextContent() {
        return textContent;
    }

    /**
     * Returns event handlers keyed by event name.
     *
     * @return event handlers as an unmodifiable map (e.g. "click" to handler)
     */
    public Map<String, EventHandler> getEventHandlers() {
        return Collections.unmodifiableMap(eventHandlers);
    }

    /**
     * Returns the CSS classes added to this element.
     *
     * @return CSS class names as an unmodifiable list
     */
    public List<String> getCssClasses() {
        return Collections.unmodifiableList(cssClasses);
    }

    /**
     * Returns the inline styles set on this element.
     *
     * @return inline styles as an unmodifiable map of CSS property to value
     */
    public Map<String, String> getStyles() {
        return Collections.unmodifiableMap(styles);
    }
}
