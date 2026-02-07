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

package xss.it.jux.a11y;

import xss.it.jux.core.Element;
import xss.it.jux.core.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains all built-in WCAG 2.2 AA audit rules as static inner classes.
 *
 * <p>Each inner class implements {@link A11yRule} and checks a specific
 * WCAG success criterion. The {@link #allRules()} factory method returns
 * instances of all rules for use by {@link JuxAccessibilityEngine}.</p>
 *
 * <p><b>Implemented rules:</b></p>
 * <ul>
 *   <li>{@link ImgAltRule} -- WCAG 1.1.1: images must have alt text</li>
 *   <li>{@link TableCaptionRule} -- WCAG 1.3.1: tables must have captions</li>
 *   <li>{@link InputLabelRule} -- WCAG 3.3.2: form inputs must have labels</li>
 *   <li>{@link FontSizeRule} -- WCAG 1.4.4: font sizes must not use px</li>
 *   <li>{@link DuplicateIdRule} -- WCAG 4.1.1: no duplicate IDs</li>
 *   <li>{@link ClickableDivRule} -- WCAG 2.1.1: clickable elements must be keyboard accessible</li>
 *   <li>{@link EmptyLinkRule} -- WCAG 2.4.4: links must have discernible text</li>
 *   <li>{@link TabIndexRule} -- WCAG 2.4.3: tabindex should not be positive</li>
 *   <li>{@link HeadingHierarchyRule} -- heading levels must not skip</li>
 *   <li>{@link VideoTrackRule} -- WCAG 1.2.x: videos must have captions track</li>
 *   <li>{@link HtmlLangRule} -- WCAG 3.1.1: html element must have lang attribute</li>
 * </ul>
 *
 * @see A11yRule
 * @see JuxAccessibilityEngine
 */
public final class A11yRules {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All access is through the static {@link #allRules()} factory method.
     */
    private A11yRules() {
        // Utility class -- no instantiation.
    }

    /**
     * Returns a list of all built-in WCAG 2.2 AA audit rule instances.
     *
     * @return a mutable list containing one instance of each built-in rule
     */
    public static List<A11yRule> allRules() {
        List<A11yRule> rules = new ArrayList<>();
        rules.add(new ImgAltRule());
        rules.add(new TableCaptionRule());
        rules.add(new InputLabelRule());
        rules.add(new FontSizeRule());
        rules.add(new DuplicateIdRule());
        rules.add(new ClickableDivRule());
        rules.add(new EmptyLinkRule());
        rules.add(new TabIndexRule());
        rules.add(new HeadingHierarchyRule());
        rules.add(new VideoTrackRule());
        rules.add(new HtmlLangRule());
        return rules;
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 1: ImgAltRule -- WCAG 1.1.1 (Non-text Content)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that {@code <img>} elements have a non-null {@code alt} attribute.
     *
     * <p>WCAG 1.1.1 requires all non-text content to have a text alternative.
     * Images without alt text are invisible to screen reader users.</p>
     *
     * <p>An empty {@code alt=""} is allowed (for decorative images) -- only
     * a completely missing alt attribute is flagged as an ERROR.</p>
     */
    public static class ImgAltRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is an {@code <img>} tag and, if so,
         * whether it has an {@code alt} attribute. A missing {@code alt} attribute
         * produces an {@link A11ySeverity#ERROR} violation. An empty {@code alt=""}
         * is considered valid (indicates a decorative image) and does not trigger
         * a violation.</p>
         *
         * @param element the element to inspect; only {@code <img>} elements are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if alt is missing, otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if ("img".equals(element.getTag())) {
                Map<String, String> attrs = element.getAttributes();
                if (!attrs.containsKey("alt")) {
                    violations.add(new A11yViolation(
                        A11ySeverity.ERROR,
                        "1.1.1",
                        "img-alt",
                        "Image element is missing alt attribute. All images must have "
                            + "alt text for screen readers, or alt=\"\" for decorative images.",
                        path,
                        "Add an alt attribute with descriptive text, or alt=\"\" if the image is purely decorative."
                    ));
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 2: TableCaptionRule -- WCAG 1.3.1 (Info and Relationships)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that {@code <table>} elements have a {@code <caption>} child.
     *
     * <p>WCAG 1.3.1 requires that tables have a visible caption describing
     * their purpose. Screen readers announce the caption when entering a
     * table, helping users understand the data.</p>
     */
    public static class TableCaptionRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is a {@code <table>} tag and, if so,
         * whether it contains at least one {@code <caption>} child element. A table
         * without a caption produces a {@link A11ySeverity#WARNING} violation.</p>
         *
         * <p>Only direct children of the {@code <table>} are inspected for the
         * caption. Nested tables are checked independently when the engine
         * recurses into them.</p>
         *
         * @param element the element to inspect; only {@code <table>} elements are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if caption is missing, otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if ("table".equals(element.getTag())) {
                boolean hasCaption = element.getChildren().stream()
                    .anyMatch(child -> "caption".equals(child.getTag()));

                if (!hasCaption) {
                    violations.add(new A11yViolation(
                        A11ySeverity.WARNING,
                        "1.3.1",
                        "table-caption",
                        "Table element is missing a <caption>. Tables should have a caption "
                            + "describing their purpose for screen reader users.",
                        path,
                        "Add a <caption> element as the first child of the table."
                    ));
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 3: InputLabelRule -- WCAG 3.3.2 (Labels or Instructions)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that form input elements have an accessible label.
     *
     * <p>WCAG 3.3.2 requires that form inputs have labels or instructions.
     * This is a tree-walking rule that performs a full scan from the root
     * element to collect all {@code <label for="...">} associations, then
     * checks each form input for a valid label. An input is considered
     * labelled if any of the following are true:</p>
     * <ul>
     *   <li>A {@code <label>} element with a matching {@code for} attribute
     *       exists anywhere in the tree</li>
     *   <li>The input has a non-blank {@code aria-label} attribute</li>
     *   <li>The input has a non-blank {@code aria-labelledby} attribute</li>
     *   <li>The input has a non-blank {@code title} attribute</li>
     *   <li>The input is a descendant of a {@code <label>} element
     *       (implicit label wrapping)</li>
     *   <li>The input's {@code type} is "hidden", "submit", "button",
     *       "reset", or "image" (exempt from labelling)</li>
     * </ul>
     */
    public static class InputLabelRule implements A11yRule {

        /**
         * HTML tag names of form elements that require an accessible label.
         */
        private static final Set<String> FORM_ELEMENTS = Set.of("input", "textarea", "select");

        /**
         * Input {@code type} attribute values that are exempt from the labelling
         * requirement.
         */
        private static final Set<String> EXEMPT_INPUT_TYPES = Set.of("hidden", "submit", "button", "reset", "image");

        /**
         * {@inheritDoc}
         *
         * <p>When invoked on the root element (detected by the absence of
         * " > " in the path), performs a full tree scan to collect all
         * {@code <label for="...">} associations and implicitly wrapped inputs,
         * then checks each form input for a valid label.</p>
         *
         * <p>When invoked on a non-root element, returns an empty list
         * immediately to avoid redundant traversals.</p>
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            // Only run the full tree scan from the root invocation.
            if (path.contains(" > ")) {
                return List.of();
            }

            // Phase 1: collect all label[for] targets and implicitly wrapped input IDs.
            Set<String> labelledIds = new java.util.HashSet<>();
            collectLabelTargets(element, labelledIds, false);

            // Phase 2: find all form inputs and check for labels.
            List<A11yViolation> violations = new ArrayList<>();
            checkInputs(element, path, labelledIds, violations);
            return violations;
        }

        /**
         * Recursively collects IDs targeted by {@code <label for="...">} elements
         * and IDs of inputs implicitly wrapped inside {@code <label>} elements.
         */
        private void collectLabelTargets(Element element, Set<String> labelledIds, boolean insideLabel) {
            String tag = element.getTag();
            Map<String, String> attrs = element.getAttributes();

            // If this is a <label> with a for attribute, record the target ID.
            if ("label".equals(tag)) {
                String forAttr = attrs.get("for");
                if (forAttr != null && !forAttr.isBlank()) {
                    labelledIds.add(forAttr);
                }
                // Recurse into label children — inputs inside are implicitly labelled.
                for (Element child : element.getChildren()) {
                    collectLabelTargets(child, labelledIds, true);
                }
                return;
            }

            // If this is a form input inside a <label>, it's implicitly labelled.
            if (insideLabel && FORM_ELEMENTS.contains(tag)) {
                String id = attrs.get("id");
                if (id != null && !id.isBlank()) {
                    labelledIds.add(id);
                } else {
                    // Mark with a synthetic key so we know it's wrapped.
                    // We'll use the element identity via name attribute as fallback.
                    String name = attrs.get("name");
                    if (name != null) {
                        labelledIds.add("__implicit__" + name);
                    }
                }
            }

            for (Element child : element.getChildren()) {
                collectLabelTargets(child, labelledIds, insideLabel);
            }
        }

        /**
         * Recursively checks all form inputs for accessible labels.
         */
        private void checkInputs(Element element, String path, Set<String> labelledIds,
                                  List<A11yViolation> violations) {
            String tag = element.getTag();

            if (FORM_ELEMENTS.contains(tag)) {
                Map<String, String> attrs = element.getAttributes();

                // Hidden inputs and submit/button types are exempt.
                String inputType = attrs.get("type");
                if (inputType != null && EXEMPT_INPUT_TYPES.contains(inputType.toLowerCase())) {
                    // Still recurse into children (unlikely but safe).
                } else {
                    boolean hasAriaLabel = attrs.containsKey("aria-label")
                        && !attrs.get("aria-label").isBlank();
                    boolean hasAriaLabelledBy = attrs.containsKey("aria-labelledby")
                        && !attrs.get("aria-labelledby").isBlank();
                    boolean hasTitle = attrs.containsKey("title")
                        && !attrs.get("title").isBlank();

                    // Check if a <label for="id"> matches this input's id.
                    String id = attrs.get("id");
                    boolean hasLabelFor = id != null && !id.isBlank() && labelledIds.contains(id);

                    // Check implicit label wrapping via name.
                    String name = attrs.get("name");
                    boolean hasImplicitLabel = name != null && labelledIds.contains("__implicit__" + name);

                    if (!hasAriaLabel && !hasAriaLabelledBy && !hasTitle && !hasLabelFor && !hasImplicitLabel) {
                        violations.add(new A11yViolation(
                            A11ySeverity.WARNING,
                            "3.3.2",
                            "input-label",
                            "Form " + tag + " element has no accessible label. "
                                + "Every form control must have a <label>, aria-label, or aria-labelledby.",
                            path,
                            "Add a <label for=\"inputId\"> element, or set aria-label or aria-labelledby on the input."
                        ));
                    }
                }
            }

            List<Element> children = element.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                String childPath = path + " > " + child.getTag() + ":" + i;
                checkInputs(child, childPath, labelledIds, violations);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 4: FontSizeRule -- WCAG 1.4.4 (Resize Text)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that inline font-size styles do not use px units.
     *
     * <p>WCAG 1.4.4 requires that text can be resized up to 200% without
     * loss of content or functionality. Using {@code px} for font sizes
     * prevents browser text zoom from working correctly. Use {@code rem}
     * or {@code em} instead.</p>
     */
    public static class FontSizeRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Inspects the element's inline styles (via {@link Element#getStyles()})
         * for a {@code font-size} property containing {@code px} units. If found,
         * produces a {@link A11ySeverity#WARNING} violation recommending the use of
         * relative units ({@code rem} or {@code em}).</p>
         *
         * <p>This rule only catches inline styles set via {@link Element#style(String, String)}.
         * External CSS files are not inspected by the audit engine.</p>
         *
         * @param element the element whose inline styles are inspected
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if px font-size is found,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            Map<String, String> styles = element.getStyles();
            String fontSize = styles.get("font-size");

            if (fontSize != null && fontSize.contains("px")) {
                violations.add(new A11yViolation(
                    A11ySeverity.WARNING,
                    "1.4.4",
                    "font-size-px",
                    "Font size uses px units (\"" + fontSize + "\"). Pixel-based font sizes "
                        + "prevent browser text zoom from working correctly.",
                    path,
                    "Use rem or em units instead of px for font-size (e.g. \"1rem\" instead of \"16px\")."
                ));
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 5: DuplicateIdRule -- WCAG 4.1.1 (Parsing / Unique IDs)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that no two elements in the tree share the same {@code id}.
     *
     * <p>WCAG 4.1.1 requires that element IDs are unique within a page.
     * Duplicate IDs break ARIA references ({@code aria-labelledby},
     * {@code aria-controls}, etc.) and cause unpredictable behavior.</p>
     *
     * <p>This is a tree-walking rule. It collects all IDs from the entire
     * subtree when invoked, so it should only be run on the root element.
     * The engine calls it on every element, but violations are only generated
     * for the second (and subsequent) occurrences of a duplicate ID.</p>
     */
    public static class DuplicateIdRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>When invoked on the root element (detected by the absence of
         * " > " in the path), performs a full depth-first traversal of the
         * entire subtree, collecting all {@code id} attributes. The first
         * occurrence of each ID is recorded; subsequent occurrences produce
         * {@link A11ySeverity#ERROR} violations referencing the first occurrence's
         * path.</p>
         *
         * <p>When invoked on a non-root element (the engine calls this rule
         * for every node), returns an empty list immediately to avoid redundant
         * traversals. The single root-level traversal covers the full tree.</p>
         *
         * @param element the element to check (full tree scan only runs at root)
         * @param path    the tree path used for violation reporting and root detection
         * @return a list of violations for each duplicate ID found, empty if all IDs are unique
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            // Only run the full tree scan from the root invocation.
            // We detect "root" by checking if the path has no " > " separator,
            // meaning it's the top-level element passed to the engine.
            if (path.contains(" > ")) {
                return List.of();
            }

            List<A11yViolation> violations = new ArrayList<>();
            Map<String, String> idToFirstPath = new HashMap<>();
            collectDuplicateIds(element, path, idToFirstPath, violations);
            return violations;
        }

        /**
         * Recursively walks the element tree collecting IDs and detecting duplicates.
         *
         * <p>For each element that has a non-blank {@code id} attribute, checks
         * whether that ID has already been seen. If so, a violation is added to
         * the violations list. Otherwise, the ID is recorded in the map for
         * future comparison.</p>
         *
         * @param element       the current element being inspected
         * @param path          the tree path of the current element for violation reporting
         * @param idToFirstPath a map from each seen ID to the path of its first occurrence
         * @param violations    the accumulator list for discovered duplicate-ID violations
         */
        private void collectDuplicateIds(Element element, String path,
                                         Map<String, String> idToFirstPath,
                                         List<A11yViolation> violations) {
            String id = element.getAttributes().get("id");
            if (id != null && !id.isBlank()) {
                if (idToFirstPath.containsKey(id)) {
                    violations.add(new A11yViolation(
                        A11ySeverity.ERROR,
                        "4.1.1",
                        "duplicate-id",
                        "Duplicate id=\"" + id + "\" found. IDs must be unique within a page. "
                            + "First occurrence at: " + idToFirstPath.get(id),
                        path,
                        "Change the id to a unique value. Duplicate IDs break ARIA references and label associations."
                    ));
                } else {
                    idToFirstPath.put(id, path);
                }
            }

            List<Element> children = element.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                String childPath = path + " > " + child.getTag() + ":" + i;
                collectDuplicateIds(child, childPath, idToFirstPath, violations);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 6: ClickableDivRule -- WCAG 2.1.1 (Keyboard)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that clickable {@code <div>} and {@code <span>} elements are
     * keyboard accessible.
     *
     * <p>WCAG 2.1.1 requires that all functionality available via mouse
     * is also available via keyboard. If a {@code <div>} or {@code <span>}
     * has a "click" event handler, it must also have:</p>
     * <ul>
     *   <li>A keyboard event handler (keydown, keypress, or keyup), OR</li>
     *   <li>A tabindex attribute (making it focusable), OR</li>
     *   <li>An ARIA role (indicating it's an interactive widget)</li>
     * </ul>
     *
     * <p>The preferred fix is to use a {@code <button>} instead of a
     * clickable div.</p>
     */
    public static class ClickableDivRule implements A11yRule {

        /**
         * HTML tag names that are not natively interactive. These elements do not
         * receive keyboard focus or fire keyboard events by default, so a click
         * handler alone is insufficient for keyboard accessibility.
         */
        private static final Set<String> NON_INTERACTIVE_TAGS = Set.of("div", "span");

        /**
         * DOM event names that indicate keyboard interaction support. If a
         * non-interactive element with a click handler also has one of these
         * keyboard event handlers registered, it is considered keyboard accessible.
         */
        private static final Set<String> KEYBOARD_EVENTS = Set.of("keydown", "keypress", "keyup");

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is a non-interactive element
         * ({@code <div>} or {@code <span>}) that has a "click" event handler
         * registered. If it does, verifies that it also has at least one of
         * the following keyboard accessibility provisions:</p>
         * <ul>
         *   <li>A keyboard event handler ({@code keydown}, {@code keypress}, or {@code keyup})</li>
         *   <li>A {@code tabindex} attribute (making the element focusable)</li>
         *   <li>An ARIA {@code role} attribute (indicating an interactive widget)</li>
         * </ul>
         *
         * <p>If none of these provisions are present, produces a
         * {@link A11ySeverity#WARNING} violation recommending the use of a native
         * {@code <button>} element or the addition of keyboard support.</p>
         *
         * @param element the element to inspect; only non-interactive elements with
         *                click handlers are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if keyboard support is missing,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if (NON_INTERACTIVE_TAGS.contains(element.getTag())) {
                Map<String, EventHandler> handlers = element.getEventHandlers();

                if (handlers.containsKey("click")) {
                    boolean hasKeyboardHandler = handlers.keySet().stream()
                        .anyMatch(KEYBOARD_EVENTS::contains);
                    boolean hasTabIndex = element.getAttributes().containsKey("tabindex");
                    boolean hasRole = element.getAttributes().containsKey("role");

                    if (!hasKeyboardHandler && !hasTabIndex && !hasRole) {
                        violations.add(new A11yViolation(
                            A11ySeverity.WARNING,
                            "2.1.1",
                            "clickable-div",
                            "Non-interactive <" + element.getTag() + "> element has a click handler "
                                + "but is not keyboard accessible. It has no keyboard event handler, "
                                + "no tabindex, and no ARIA role.",
                            path,
                            "Use a <button> element instead, or add tabindex=\"0\", role=\"button\", "
                                + "and a keydown handler for Enter/Space."
                        ));
                    }
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 7: EmptyLinkRule -- WCAG 2.4.4 (Link Purpose)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that {@code <a>} elements have discernible link text.
     *
     * <p>WCAG 2.4.4 requires that the purpose of each link can be determined
     * from the link text alone (or from the link text together with its
     * programmatically determined context). An empty link with no text,
     * no children, and no {@code aria-label} is inaccessible to screen
     * reader users.</p>
     */
    public static class EmptyLinkRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is an {@code <a>} tag and, if so,
         * whether it has any discernible text. The following sources of accessible
         * text are recognized:</p>
         * <ul>
         *   <li>Direct text content ({@link Element#getTextContent()})</li>
         *   <li>Child elements (which may themselves contain text, such as
         *       {@code <img>} with alt text or {@code <span>} with text)</li>
         *   <li>A non-blank {@code aria-label} attribute</li>
         *   <li>A non-blank {@code aria-labelledby} attribute</li>
         *   <li>A non-blank {@code title} attribute</li>
         * </ul>
         *
         * <p>If none of these text sources are present, produces a
         * {@link A11ySeverity#WARNING} violation. Screen reader users navigating
         * by links will encounter a link with no announced name, making it
         * impossible to understand the link's purpose.</p>
         *
         * @param element the element to inspect; only {@code <a>} elements are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if the link has no discernible text,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if ("a".equals(element.getTag())) {
                String text = element.getTextContent();
                boolean hasText = text != null && !text.isBlank();
                boolean hasChildren = !element.getChildren().isEmpty();
                Map<String, String> attrs = element.getAttributes();
                boolean hasAriaLabel = attrs.containsKey("aria-label")
                    && !attrs.get("aria-label").isBlank();
                boolean hasAriaLabelledBy = attrs.containsKey("aria-labelledby")
                    && !attrs.get("aria-labelledby").isBlank();
                boolean hasTitle = attrs.containsKey("title")
                    && !attrs.get("title").isBlank();

                if (!hasText && !hasChildren && !hasAriaLabel && !hasAriaLabelledBy && !hasTitle) {
                    violations.add(new A11yViolation(
                        A11ySeverity.WARNING,
                        "2.4.4",
                        "empty-link",
                        "Anchor element has no discernible link text. Links must have text content, "
                            + "child elements with text, or an aria-label for screen readers.",
                        path,
                        "Add descriptive text content to the link, or set aria-label with a descriptive value."
                    ));
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 8: TabIndexRule -- WCAG 2.4.3 (Focus Order)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Warns when {@code tabindex} has a positive value.
     *
     * <p>WCAG 2.4.3 requires a meaningful focus order. Using
     * {@code tabindex > 0} creates a custom tab order that can confuse
     * keyboard users. Only {@code tabindex="0"} (add to natural tab order)
     * and {@code tabindex="-1"} (programmatic focus only) are recommended.</p>
     */
    public static class TabIndexRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Inspects the element's {@code tabindex} attribute, if present. Parses
         * the value as an integer and checks whether it is positive (greater than
         * zero). Positive tabindex values produce a {@link A11ySeverity#WARNING}
         * violation because they create a custom tab order that overrides the
         * natural document order, which can disorient keyboard users.</p>
         *
         * <p>The recommended values are:</p>
         * <ul>
         *   <li>{@code tabindex="0"} -- adds the element to the natural tab order</li>
         *   <li>{@code tabindex="-1"} -- allows programmatic focus but excludes the
         *       element from the tab order</li>
         * </ul>
         *
         * <p>Non-numeric tabindex values are silently ignored (they are not this
         * rule's concern).</p>
         *
         * @param element the element whose tabindex attribute is inspected
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if tabindex is positive,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            String tabindex = element.getAttributes().get("tabindex");
            if (tabindex != null) {
                try {
                    int value = Integer.parseInt(tabindex.trim());
                    if (value > 0) {
                        violations.add(new A11yViolation(
                            A11ySeverity.WARNING,
                            "2.4.3",
                            "positive-tabindex",
                            "Element has tabindex=\"" + value + "\". Positive tabindex values create "
                                + "a custom tab order that can confuse keyboard users.",
                            path,
                            "Use tabindex=\"0\" to add to natural tab order, or tabindex=\"-1\" for "
                                + "programmatic focus only. Avoid positive values."
                        ));
                    }
                } catch (NumberFormatException e) {
                    // Non-numeric tabindex -- not this rule's concern.
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 9: HeadingHierarchyRule -- Heading level skip detection
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that heading levels do not skip (e.g. h1 followed by h3 without h2).
     *
     * <p>Proper heading hierarchy is essential for screen reader navigation.
     * Users rely on headings to understand page structure and jump between
     * sections. Skipping levels creates confusion about content hierarchy.</p>
     *
     * <p>This is a tree-walking rule. It collects all headings from the
     * entire subtree in document order when invoked on the root element,
     * then checks for level skips.</p>
     */
    public static class HeadingHierarchyRule implements A11yRule {

        /**
         * HTML tag names for heading elements ({@code <h1>} through {@code <h6>}).
         * Used to identify headings during the depth-first tree traversal.
         */
        private static final Set<String> HEADING_TAGS = Set.of("h1", "h2", "h3", "h4", "h5", "h6");

        /**
         * {@inheritDoc}
         *
         * <p>When invoked on the root element (detected by the absence of
         * " > " in the path), performs a full depth-first traversal to collect
         * all heading elements in document order. Then iterates through consecutive
         * heading pairs and flags any case where the heading level increases by
         * more than one (e.g. {@code <h1>} followed by {@code <h3>} with no
         * intervening {@code <h2>}).</p>
         *
         * <p>Going to a shallower or equal heading level is always valid (e.g.
         * {@code <h3>} followed by {@code <h2>} is fine). Only increases of more
         * than one level constitute a hierarchy skip.</p>
         *
         * <p>When invoked on a non-root element, returns an empty list immediately
         * to avoid redundant traversals.</p>
         *
         * @param element the element to check (full tree scan only runs at root)
         * @param path    the tree path used for violation reporting and root detection
         * @return a list of violations for each heading hierarchy skip found,
         *         empty if the heading order is correct
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            // Only run the full tree scan from the root invocation.
            if (path.contains(" > ")) {
                return List.of();
            }

            List<A11yViolation> violations = new ArrayList<>();
            List<HeadingInfo> headings = new ArrayList<>();
            collectHeadings(element, path, headings);

            // Check for level skips between consecutive headings.
            for (int i = 1; i < headings.size(); i++) {
                HeadingInfo prev = headings.get(i - 1);
                HeadingInfo curr = headings.get(i);

                // A heading can go deeper by at most 1 level.
                // Going to a shallower level (or same level) is always fine.
                if (curr.level > prev.level + 1) {
                    violations.add(new A11yViolation(
                        A11ySeverity.WARNING,
                        "1.3.1",
                        "heading-hierarchy",
                        "Heading level skipped: <" + curr.tag + "> follows <" + prev.tag + "> "
                            + "without an intervening <h" + (prev.level + 1) + ">. "
                            + "Heading levels should not skip (e.g. h1 -> h3 without h2).",
                        curr.path,
                        "Add an <h" + (prev.level + 1) + "> heading before this <" + curr.tag + ">, "
                            + "or change this heading to <h" + (prev.level + 1) + ">."
                    ));
                }
            }

            return violations;
        }

        /**
         * Recursively walks the element tree collecting heading elements in document order.
         *
         * <p>For each element whose tag is a heading ({@code h1}-{@code h6}), creates
         * a {@link HeadingInfo} record capturing the tag name, numeric level, and tree
         * path, and appends it to the headings list. Then recurses into all children
         * to continue the depth-first traversal.</p>
         *
         * @param element  the current element being inspected
         * @param path     the tree path of the current element
         * @param headings the accumulator list of heading information records, in document order
         */
        private void collectHeadings(Element element, String path, List<HeadingInfo> headings) {
            String tag = element.getTag();
            if (HEADING_TAGS.contains(tag)) {
                int level = tag.charAt(1) - '0';
                headings.add(new HeadingInfo(tag, level, path));
            }

            List<Element> children = element.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = children.get(i);
                String childPath = path + " > " + child.getTag() + ":" + i;
                collectHeadings(child, childPath, headings);
            }
        }

        /**
         * Internal record capturing information about a heading element found
         * during tree traversal.
         *
         * @param tag   the HTML tag name (e.g. "h1", "h2", "h3")
         * @param level the numeric heading level extracted from the tag (1-6)
         * @param path  the tree path where this heading was found, used in violation reports
         */
        private record HeadingInfo(String tag, int level, String path) {}
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 10: VideoTrackRule -- WCAG 1.2.2 (Captions)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that {@code <video>} elements have a {@code <track>} child
     * for captions.
     *
     * <p>WCAG 1.2.2 requires that prerecorded audio content in synchronized
     * media has captions. A {@code <video>} element without a {@code <track>}
     * element means deaf and hard-of-hearing users cannot access the audio
     * content.</p>
     */
    public static class VideoTrackRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is a {@code <video>} tag and, if so,
         * whether it contains at least one {@code <track>} child element. A video
         * without a track element produces a {@link A11ySeverity#WARNING} violation
         * because deaf and hard-of-hearing users cannot access the audio content.</p>
         *
         * <p>Only direct children of the {@code <video>} are inspected for the
         * track element. The rule does not verify the track's {@code kind},
         * {@code src}, or {@code srclang} attributes -- just that at least one
         * {@code <track>} is present.</p>
         *
         * @param element the element to inspect; only {@code <video>} elements are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if no track child is found,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if ("video".equals(element.getTag())) {
                boolean hasTrack = element.getChildren().stream()
                    .anyMatch(child -> "track".equals(child.getTag()));

                if (!hasTrack) {
                    violations.add(new A11yViolation(
                        A11ySeverity.WARNING,
                        "1.2.2",
                        "video-track",
                        "Video element is missing a <track> element for captions. "
                            + "Videos must have captions for deaf and hard-of-hearing users.",
                        path,
                        "Add a <track kind=\"captions\" src=\"captions.vtt\" srclang=\"en\" label=\"English\"> "
                            + "child element to the video."
                    ));
                }
            }

            return violations;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RULE 11: HtmlLangRule -- WCAG 3.1.1 (Language of Page)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks that the {@code <html>} root element has a {@code lang} attribute.
     *
     * <p>WCAG 3.1.1 requires that the default human language of each web page
     * can be programmatically determined. Screen readers use the {@code lang}
     * attribute to select the correct pronunciation rules.</p>
     *
     * <p>This rule only fires on elements with the "html" tag.</p>
     */
    public static class HtmlLangRule implements A11yRule {

        /**
         * {@inheritDoc}
         *
         * <p>Checks whether the given element is an {@code <html>} tag and, if so,
         * whether it has a non-blank {@code lang} attribute. A missing or empty
         * {@code lang} attribute produces an {@link A11ySeverity#ERROR} violation
         * because screen readers rely on this attribute to select the correct
         * pronunciation rules and speech synthesizer.</p>
         *
         * <p>This rule does not validate the lang value itself (e.g. whether "en"
         * is a valid BCP 47 tag). It only verifies that some non-blank value is
         * present.</p>
         *
         * @param element the element to inspect; only {@code <html>} elements are checked
         * @param path    the tree path used for violation reporting
         * @return a singleton list with one violation if lang is missing or blank,
         *         otherwise an empty list
         */
        @Override
        public List<A11yViolation> check(Element element, String path) {
            List<A11yViolation> violations = new ArrayList<>();

            if ("html".equals(element.getTag())) {
                Map<String, String> attrs = element.getAttributes();
                String lang = attrs.get("lang");

                if (lang == null || lang.isBlank()) {
                    violations.add(new A11yViolation(
                        A11ySeverity.ERROR,
                        "3.1.1",
                        "html-lang",
                        "The <html> element is missing a lang attribute. The page language must "
                            + "be programmatically determinable for screen readers to use correct "
                            + "pronunciation rules.",
                        path,
                        "Add a lang attribute to the <html> element (e.g. lang=\"en\", lang=\"es\", lang=\"ar\")."
                    ));
                }
            }

            return violations;
        }
    }
}
