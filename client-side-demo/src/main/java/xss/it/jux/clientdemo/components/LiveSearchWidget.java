/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static xss.it.jux.core.Elements.*;

/**
 * Live search/filter widget demonstrating real-time reactive list filtering in JUX.
 *
 * <p>This component showcases how {@code @State} reactivity combined with the
 * {@code "input"} event handler creates a responsive filtering interface. As the
 * user types into the search input, the displayed list of programming languages
 * is filtered in real-time, with matching text portions highlighted in bold.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} for query tracking</b> -- the {@code query} string is
 *       updated on every keystroke via the {@code "input"} event. Each change
 *       triggers a re-render that recomputes the filtered list and patches the DOM.</li>
 *   <li><b>Derived state computation</b> -- the filtered results list and match count
 *       are computed fresh on every render from the source data and current query.
 *       This demonstrates the "single source of truth" pattern where all derived
 *       values flow from {@code @State} fields.</li>
 *   <li><b>Dynamic list rendering</b> -- the results list is generated dynamically
 *       using {@code stream().filter().map().toList()}, showing how Java streams
 *       integrate naturally with JUX element tree construction.</li>
 *   <li><b>Text highlighting</b> -- matching portions of each result are wrapped in
 *       {@code <strong>} elements for visual emphasis, demonstrating inline content
 *       construction with mixed text and element children.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the results use a semantic {@code <ul>}
 *       list structure. The input is linked to results via {@code aria-controls}.</li>
 *   <li><b>3.3.2 Labels or Instructions</b> -- the search input has an
 *       {@code aria-label} and a visible placeholder providing usage guidance.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the result count is in an
 *       {@code aria-live="polite"} region so screen readers announce "X results"
 *       as the user types, without stealing focus.</li>
 * </ul>
 *
 * <h2>Data</h2>
 * <p>The searchable dataset is a hardcoded list of 14 programming languages.
 * In a real application, this data could come from an API call, database query,
 * or any other data source injected via {@code @Autowired}.</p>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class LiveSearchWidget extends Component {

    /**
     * The current search query string entered by the user.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler on the
     * search text field. The value is used to filter and highlight the programming
     * language list on each re-render. An empty string matches all items.</p>
     */
    @State
    private String query = "";

    /**
     * The complete list of programming languages available for searching.
     *
     * <p>This is a static, immutable dataset used as the source of truth for
     * filtering. Each language name is compared case-insensitively against the
     * current {@code query} to produce the filtered results shown to the user.</p>
     */
    private final List<String> allLanguages = List.of(
            "Java", "JavaScript", "TypeScript", "Python", "Rust",
            "Go", "Kotlin", "Swift", "C#", "Ruby",
            "Scala", "Clojure", "Elixir", "Haskell"
    );

    /**
     * Builds the virtual DOM tree for the live search widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A search input field with a magnifying glass icon.</li>
     *   <li>A result count indicator in an aria-live region.</li>
     *   <li>A filtered list of matching programming languages, each with the
     *       matching portion highlighted in bold.</li>
     * </ol>
     *
     * @return the root element of the live search widget, never null
     */
    @Override
    public Element render() {
        /*
         * Filter the complete language list against the current query.
         * The comparison is case-insensitive to provide a forgiving search
         * experience -- typing "java" matches "Java" and "JavaScript".
         */
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<String> filtered = allLanguages.stream()
                .filter(lang -> lowerQuery.isEmpty()
                        || lang.toLowerCase(Locale.ROOT).contains(lowerQuery))
                .toList();

        /* Build and return the complete live search widget element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Search Input with Icon ────────────────────────────────────
                 * A relative-positioned container holding a magnifying glass
                 * icon (decorative) and the text input field. The icon is
                 * absolutely positioned inside the left side of the input.
                 */
                div().cls("relative", "mb-4").children(

                        /* Magnifying glass icon: a decorative Unicode character
                         * positioned inside the left side of the input field.
                         * aria-hidden="true" since it is purely decorative. */
                        span().cls("absolute", "left-3", "top-1/2", "-translate-y-1/2",
                                        "text-gray-500", "text-lg")
                                .ariaHidden(true)
                                .text("\uD83D\uDD0D"),

                        /* Search text input field. The "input" event fires on
                         * every keystroke, updating the @State query field. The
                         * aria-controls attribute links this input to the results
                         * list for screen reader context. */
                        input()
                                .attr("type", "text")
                                .attr("placeholder", "Search programming languages...")
                                .attr("value", query)
                                .aria("label", "Search programming languages")
                                .ariaControls("search-results")
                                .cls("w-full", "bg-gray-700", "text-white",
                                        "rounded-lg", "pl-10", "pr-4", "py-3",
                                        "placeholder-gray-500", "border", "border-gray-600",
                                        "focus:border-violet-500", "focus:ring-1",
                                        "focus:ring-violet-500", "focus:outline-none")
                                .on("input", e -> query = e.getValue())
                ),

                /* ── Results Count ─────────────────────────────────────────────
                 * Displays "X results" above the list. Placed in an aria-live
                 * region so screen readers announce the updated count as the
                 * user types, providing immediate feedback on filter results.
                 */
                p().cls("text-sm", "text-gray-400", "mb-3")
                        .ariaLive("polite")
                        .text(filtered.size() + (filtered.size() == 1 ? " result" : " results")),

                /* ── Filtered Results List ─────────────────────────────────────
                 * A semantic <ul> containing the filtered programming language
                 * items. Each item has the matching text portion highlighted
                 * using <strong> tags for visual emphasis.
                 */
                ul().id("search-results")
                        .cls("space-y-1", "max-h-64", "overflow-y-auto")
                        .aria("label", "Search results")
                        .children(buildResultItems(filtered, lowerQuery)),

                /* ── Empty State ───────────────────────────────────────────────
                 * When the filter produces no results, show a helpful message
                 * instead of an empty list.
                 */
                filtered.isEmpty()
                        ? p().cls("text-gray-500", "text-center", "py-4")
                                .text("No languages match your search.")
                        : null
        );
    }

    /**
     * Generates the list of {@code <li>} elements for the filtered results.
     *
     * <p>Each result item displays the language name with the matching portion
     * highlighted in bold ({@code <strong>}). The highlighting is achieved by
     * splitting the language name at the match boundaries and wrapping the
     * matched substring in a {@code <strong>} element with accent coloring.</p>
     *
     * @param filtered   the filtered list of language names to display
     * @param lowerQuery the lowercase search query for match highlighting
     * @return a list of {@code <li>} elements for the results
     */
    private List<Element> buildResultItems(List<String> filtered, String lowerQuery) {
        /* Collect the generated <li> elements into a mutable list. */
        List<Element> items = new ArrayList<>();

        for (String language : filtered) {
            /*
             * Build the list item with hover styling and a left border accent.
             * The item uses a subtle left border that becomes violet on hover,
             * providing a visual cue for the focused/hovered item.
             */
            Element item = li()
                    .cls("px-4", "py-2", "rounded-lg",
                            "hover:bg-gray-700", "transition-colors",
                            "border-l-2", "border-transparent",
                            "hover:border-violet-500",
                            "cursor-default")
                    .children(buildHighlightedText(language, lowerQuery));

            items.add(item);
        }

        return items;
    }

    /**
     * Creates an element with the matching text portion highlighted in bold.
     *
     * <p>If the query is empty or not found in the language name, the entire
     * name is returned as plain text. If a match is found, the text is split
     * into three parts: the prefix before the match, the matched portion
     * (wrapped in {@code <strong>} with violet coloring), and the suffix
     * after the match.</p>
     *
     * <p>Only the <em>first</em> occurrence is highlighted, which is sufficient
     * for short strings like programming language names.</p>
     *
     * @param text       the full text to display (e.g. "JavaScript")
     * @param lowerQuery the lowercase search query to highlight (e.g. "java")
     * @return a span element containing the text with highlighted matches
     */
    private Element buildHighlightedText(String text, String lowerQuery) {
        /*
         * If the query is empty, no highlighting is needed. Return the full
         * text as a simple span with the standard text color.
         */
        if (lowerQuery.isEmpty()) {
            return span().cls("text-gray-200").text(text);
        }

        /*
         * Find the start position of the query within the lowercase version
         * of the text. The search is case-insensitive but the original casing
         * is preserved in the display.
         */
        int matchStart = text.toLowerCase(Locale.ROOT).indexOf(lowerQuery);

        if (matchStart < 0) {
            /* No match found (should not happen since we pre-filtered, but
             * handle defensively). Return the text without highlighting. */
            return span().cls("text-gray-200").text(text);
        }

        /* Calculate the end position of the match. */
        int matchEnd = matchStart + lowerQuery.length();

        /* Split the text into prefix, matched portion, and suffix. */
        String prefix = text.substring(0, matchStart);
        String matched = text.substring(matchStart, matchEnd);
        String suffix = text.substring(matchEnd);

        /*
         * Assemble a span containing three children: the prefix text (plain),
         * the matched text (bold + violet), and the suffix text (plain).
         * Empty prefix/suffix strings produce empty spans that render nothing.
         */
        return span().children(
                span().cls("text-gray-200").text(prefix),
                strong().cls("text-violet-400", "font-semibold").text(matched),
                span().cls("text-gray-200").text(suffix)
        );
    }
}
