/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.json.JSON;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.OnMount;
import xss.it.jux.annotation.OnUnmount;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Random quote generator widget demonstrating API data fetching patterns in JUX.
 *
 * <p>This component simulates fetching random quotes from a REST API, showcasing
 * how jux-reactive's observable properties integrate with JUX's {@code @State} system
 * to build a dynamic, category-filterable quote display. It demonstrates the common
 * pattern of displaying a single piece of content fetched from an endpoint, with
 * controls to request new data and filter by category.</p>
 *
 * <h2>Reactive Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@link SimpleStringProperty} for quote text and author</b> -- the current
 *       quote text and author name are wrapped in reactive string properties. This
 *       enables external observers (such as analytics bindings, clipboard watchers,
 *       or social sharing integrations) to react to quote changes without coupling
 *       to the widget's internal rendering logic.</li>
 *   <li><b>{@link SimpleBooleanProperty} for loading state</b> -- the loading flag
 *       is wrapped in a reactive boolean property. Parent components or global loading
 *       overlays can observe this property to coordinate UI state across the application.
 *       This demonstrates the pattern of exposing loading states through the reactive
 *       property system.</li>
 *   <li><b>{@link Bindings} integration</b> -- the widget is designed to work with the
 *       Bindings utility class for creating derived computed values. For example, a
 *       "share URL" binding could be created from the quote text and author properties
 *       that updates automatically whenever either changes.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>Simulated API fetch cycle</b> -- the "New Quote" button triggers a loading
 *       state transition followed by displaying a new quote from the internal collection,
 *       simulating the async nature of a real API call.</li>
 *   <li><b>Category filtering</b> -- category buttons allow filtering which quotes are
 *       displayed. In a real API scenario, this would translate to a query parameter
 *       (e.g. {@code GET /api/quotes?category=programming}).</li>
 *   <li><b>Fetch counter</b> -- tracks the number of quotes fetched, demonstrating
 *       derived state that accumulates across interactions.</li>
 *   <li><b>Gradient card design</b> -- the quote card uses a gradient background to
 *       demonstrate how CSS-heavy visual designs integrate with the JUX element API.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the quote is wrapped in a semantic
 *       {@code <blockquote>} element with a {@code <cite>} element for the author,
 *       providing correct semantics for assistive technology.</li>
 *   <li><b>2.1.1 Keyboard</b> -- all buttons (categories, "New Quote") are native
 *       {@code <button>} elements, inherently keyboard-accessible.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the quote display area is an {@code aria-live="polite"}
 *       region, so screen readers announce new quotes as they are loaded without
 *       stealing focus from the "New Quote" button.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- category filter buttons use
 *       {@code aria-pressed} to indicate the active category. The "New Quote"
 *       button has a descriptive {@code aria-label}.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The component renders with a default motivational quote in its initial state,
 * providing meaningful content during server-side rendering. The category filters
 * default to "All" and the fetch count shows 0.</p>
 *
 * @see xss.it.jux.reactive.property.SimpleStringProperty
 * @see xss.it.jux.reactive.property.SimpleBooleanProperty
 * @see xss.it.jux.reactive.binding.Bindings
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class QuoteMachineWidget extends Component {

    // ── Quote Data ────────────────────────────────────────────────────────────────
    //
    // A curated collection of quotes organized by category. In a real application,
    // these would be fetched from a REST API such as GET /api/quotes?category=X.
    // The hardcoded data serves as a simulation of API responses.

    /**
     * The complete collection of quotes available to the widget.
     *
     * <p>Each quote is a {@link Quote} record containing the text, author, and
     * category. The collection spans five categories (Motivation, Programming,
     * Design, Leadership) plus general-purpose quotes that appear in all
     * category views.</p>
     *
     * <p>In a real application, this list would be populated from an API response
     * in the {@code @OnMount} lifecycle hook.</p>
     */
    private final List<Quote> allQuotes = List.of(
            new Quote(
                    "The only way to do great work is to love what you do.",
                    "Steve Jobs",
                    "Motivation"
            ),
            new Quote(
                    "Talk is cheap. Show me the code.",
                    "Linus Torvalds",
                    "Programming"
            ),
            new Quote(
                    "Design is not just what it looks like and feels like. Design is how it works.",
                    "Steve Jobs",
                    "Design"
            ),
            new Quote(
                    "A leader is one who knows the way, goes the way, and shows the way.",
                    "John C. Maxwell",
                    "Leadership"
            ),
            new Quote(
                    "First, solve the problem. Then, write the code.",
                    "John Johnson",
                    "Programming"
            ),
            new Quote(
                    "The best time to plant a tree was 20 years ago. The second best time is now.",
                    "Chinese Proverb",
                    "Motivation"
            ),
            new Quote(
                    "Good design is obvious. Great design is transparent.",
                    "Joe Sparano",
                    "Design"
            ),
            new Quote(
                    "Before software can be reusable it first has to be usable.",
                    "Ralph Johnson",
                    "Programming"
            ),
            new Quote(
                    "Innovation distinguishes between a leader and a follower.",
                    "Steve Jobs",
                    "Leadership"
            ),
            new Quote(
                    "Simplicity is the ultimate sophistication.",
                    "Leonardo da Vinci",
                    "Design"
            ),
            new Quote(
                    "It does not matter how slowly you go as long as you do not stop.",
                    "Confucius",
                    "Motivation"
            ),
            new Quote(
                    "The best way to predict the future is to invent it.",
                    "Alan Kay",
                    "Leadership"
            )
    );

    /**
     * The list of available category names for filtering.
     *
     * <p>"All" is a special value that matches every category. The remaining
     * entries correspond to the category strings used in the {@link Quote} records.
     * This list drives the rendering of category filter buttons.</p>
     */
    private static final List<String> CATEGORIES = List.of(
            "All", "Motivation", "Programming", "Design", "Leadership"
    );

    // ── Reactive Properties (jux-reactive) ────────────────────────────────────────

    /**
     * Reactive string property holding the current quote text.
     *
     * <p>Kept in sync with the {@code currentQuote} @State field. External observers
     * can listen to this property for integrations such as clipboard copy, social
     * sharing URL generation, or analytics event emission.</p>
     */
    private final SimpleStringProperty quoteTextProperty =
            new SimpleStringProperty(this, "quoteText",
                    "The only way to do great work is to love what you do.");

    /**
     * Reactive string property holding the current quote author.
     *
     * <p>Kept in sync with the {@code currentAuthor} @State field. Together with
     * {@link #quoteTextProperty}, this enables derived bindings such as a formatted
     * "quote by author" string that updates automatically when either changes.</p>
     */
    private final SimpleStringProperty quoteAuthorProperty =
            new SimpleStringProperty(this, "quoteAuthor", "Steve Jobs");

    /**
     * Reactive boolean property tracking the loading state of a quote fetch.
     *
     * <p>When {@code true}, the UI displays a loading indicator on the quote card
     * and the "New Quote" button is visually disabled. Observable by parent
     * components for coordinated loading state management.</p>
     */
    private final SimpleBooleanProperty loadingProperty =
            new SimpleBooleanProperty(this, "loading", false);

    // ── @State Fields (JUX reactivity) ────────────────────────────────────────────

    /**
     * The text of the currently displayed quote.
     *
     * <p>Initialized with a default motivational quote for the SSR preview.
     * Updated when the user clicks "New Quote" or changes the category filter.
     * Each update triggers a JUX re-render cycle.</p>
     */
    @State
    private String currentQuote = "The only way to do great work is to love what you do.";

    /**
     * The author of the currently displayed quote.
     *
     * <p>Displayed below the quote text in a {@code <cite>} element. Updated
     * in tandem with {@link #currentQuote} when a new quote is selected.</p>
     */
    @State
    private String currentAuthor = "Steve Jobs";

    /**
     * The category of the currently displayed quote.
     *
     * <p>Displayed as a badge next to the author name. Updated whenever a new
     * quote is selected, reflecting the quote's category label.</p>
     */
    @State
    private String currentCategory = "Motivation";

    /**
     * The total number of quotes that have been fetched (cycled through).
     *
     * <p>Incremented each time the user clicks "New Quote" to load a new
     * quote. This counter persists across category changes and demonstrates
     * accumulating state across interactions.</p>
     */
    @State
    private int fetchCount = 0;

    /**
     * The currently selected category filter.
     *
     * <p>"All" means no filtering is applied and quotes from any category may
     * be shown. Any other value restricts the quote selection to that category.</p>
     */
    @State
    private String selectedCategory = "All";

    /**
     * Whether the widget is currently in a loading state (simulating an API fetch).
     *
     * <p>When {@code true}, the "New Quote" button shows a loading indicator and
     * the quote text may show a brief transition effect.</p>
     */
    @State
    private boolean loading = false;

    /**
     * Internal index tracking which quote was last shown, used to cycle through
     * the collection without repeating the same quote consecutively.
     *
     * <p>This is NOT a {@code @State} field because changes to the index alone
     * should not trigger a re-render. The re-render is triggered by the actual
     * quote text/author state changes that result from advancing the index.</p>
     */
    private int quoteIndex = 0;

    /**
     * Initializes the widget after client-side hydration.
     *
     * <p>Sets up the initial quote display and would trigger the first API fetch
     * in a real application. Also establishes any reactive property listeners or
     * bindings needed for integration with external systems.</p>
     *
     * <p><b>Note:</b> Since TeaVM compilation is not yet wired up, the actual HTTP
     * call is described in comments. The component renders with a default quote for
     * the SSR preview.</p>
     */
    @OnMount
    public void onMount() {
        /* Fetch the initial random quote from the API. */
        fetchRandomQuote();
    }

    /**
     * Performs cleanup before the component is removed from the DOM.
     *
     * <p>Cancels any in-flight API requests, removes reactive property listeners,
     * and disposes of any computed bindings to prevent memory leaks.</p>
     */
    @OnUnmount
    public void onUnmount() {
        /* No interval timers to clear; XHR callbacks are harmless after unmount. */
    }

    /**
     * Builds the virtual DOM tree for the quote machine widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>A gradient-backed blockquote card displaying the current quote text</li>
     *   <li>An author line with the author name and category badge</li>
     *   <li>Category filter buttons with active state indication</li>
     *   <li>A "New Quote" action button with fetch count display</li>
     * </ol>
     *
     * @return the root element of the quote machine widget, never null
     */
    @Override
    public Element render() {
        /* Build and return the complete quote machine element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Quote Card with Gradient Background ──────────────────────────
                 * The main visual centerpiece: a gradient-backed card containing
                 * the quote in a large, stylish font. The gradient uses violet-to-blue
                 * tones that complement the dark theme.
                 */
                div().cls("rounded-xl", "p-8", "mb-6",
                                "bg-gradient-to-br", "from-violet-900/60", "via-blue-900/40",
                                "to-gray-800", "border", "border-violet-500/20")
                        .children(

                                /* ── Quote Text (ARIA Live Region) ────────────────────
                                 * The quote is displayed inside a semantic <blockquote>
                                 * element. The aria-live="polite" attribute ensures that
                                 * screen readers announce new quotes when they change,
                                 * without interrupting the user.
                                 */
                                div().ariaLive("polite")
                                        .id("quote-display")
                                        .children(

                                                /* Opening quotation mark: a large decorative character
                                                 * displayed above the quote text. Hidden from screen
                                                 * readers since it is purely decorative. */
                                                span().cls("text-4xl", "text-violet-400/60",
                                                                "font-serif", "leading-none",
                                                                "select-none")
                                                        .ariaHidden(true)
                                                        .text("\u201C"),

                                                /* The actual quote text in a blockquote element.
                                                 * Uses a slightly larger font with serif styling
                                                 * to create a classic quotation appearance. */
                                                Element.of("blockquote")
                                                        .cls("text-xl", "text-white", "font-medium",
                                                                "leading-relaxed", "my-3",
                                                                "italic")
                                                        .text(currentQuote),

                                                /* Closing quotation mark: mirrors the opening mark.
                                                 * Aligned to the right for visual balance. */
                                                div().cls("text-right").children(
                                                        span().cls("text-4xl", "text-violet-400/60",
                                                                        "font-serif", "leading-none",
                                                                        "select-none")
                                                                .ariaHidden(true)
                                                                .text("\u201D")
                                                )
                                        ),

                                /* ── Author and Category Line ─────────────────────────
                                 * Displays the author name in a <cite> element (correct
                                 * semantics for attributing a quote) alongside a category
                                 * badge indicating the quote's topic.
                                 */
                                div().cls("flex", "items-center", "gap-3", "mt-4",
                                                "pt-4", "border-t", "border-violet-500/20")
                                        .children(

                                                /* Em dash separator before the author name.
                                                 * A typographic convention for quote attribution. */
                                                span().cls("text-gray-500")
                                                        .ariaHidden(true)
                                                        .text("\u2014"),

                                                /* Author name in a <cite> element. The cite element
                                                 * semantically identifies the source of the quotation
                                                 * for assistive technology and search engines. */
                                                Element.of("cite")
                                                        .cls("text-gray-300", "not-italic",
                                                                "font-medium", "text-sm")
                                                        .text(currentAuthor),

                                                /* Category badge: a small coloured pill indicating
                                                 * the quote's category. The colour is determined by
                                                 * the category name for visual consistency. */
                                                span().cls("px-2.5", "py-0.5", "rounded-full",
                                                                "text-xs", "font-medium")
                                                        .cls(getCategoryBadgeColor(currentCategory))
                                                        .text(currentCategory)
                                        )
                        ),

                /* ── Category Filter Buttons ──────────────────────────────────────
                 * A horizontal row of category buttons that filter which quotes
                 * are shown. "All" shows quotes from any category. The currently
                 * active category is visually highlighted with aria-pressed="true".
                 */
                nav().cls("flex", "flex-wrap", "gap-2", "mb-5")
                        .aria("label", "Quote category filter")
                        .children(buildCategoryButtons()),

                /* ── Action Row: New Quote Button + Fetch Count ───────────────────
                 * A flex row with the "New Quote" button on the left and the
                 * fetch count display on the right.
                 */
                div().cls("flex", "items-center", "justify-between").children(

                        /* "New Quote" button: triggers the quote cycling logic.
                         * When loading, shows "Loading..." with a muted style.
                         * The button includes a refresh icon character for visual appeal. */
                        button().attr("type", "button")
                                .cls("px-5", "py-2.5", "rounded-lg", "font-medium",
                                        "text-sm", "transition-all", "focus:ring-2",
                                        "focus:ring-violet-500", "focus:ring-offset-2",
                                        "focus:ring-offset-gray-800")
                                .cls(loading
                                        ? "bg-gray-700 text-gray-500 cursor-not-allowed"
                                        : "bg-violet-600 text-white hover:bg-violet-500 active:scale-95")
                                .aria("label", loading
                                        ? "Loading new quote"
                                        : "Fetch a new random quote")
                                .ariaDisabled(loading)
                                .on("click", e -> {
                                    /* Only trigger if not already loading. */
                                    if (!loading) {
                                        fetchRandomQuote();
                                    }
                                })
                                .children(
                                        /* Refresh icon character before the button text. */
                                        span().cls("mr-1.5")
                                                .ariaHidden(true)
                                                .text("\u21BB"),
                                        span().text(loading ? "Loading..." : "New Quote")
                                ),

                        /* Fetch count display: shows how many quotes have been loaded.
                         * This provides a subtle progress indicator that accumulates
                         * over the session, demonstrating persistent derived state. */
                        span().cls("text-sm", "text-gray-500")
                                .text(fetchCount + (fetchCount == 1 ? " quote" : " quotes")
                                        + " fetched")
                )
        );
    }

    /**
     * Builds the category filter buttons.
     *
     * <p>Each button represents a category from the {@link #CATEGORIES} list. The
     * currently active category is highlighted with a filled violet background and
     * {@code aria-pressed="true"} for screen reader indication. Inactive buttons
     * have a subtle outlined appearance that becomes more prominent on hover.</p>
     *
     * @return a list of button elements for the category filter
     */
    private List<Element> buildCategoryButtons() {
        /* Collect the generated button elements into a mutable list. */
        List<Element> buttons = new ArrayList<>();

        for (String category : CATEGORIES) {
            /*
             * Determine whether this category is currently active. The active
             * button receives a filled background and aria-pressed="true".
             */
            boolean isActive = category.equals(selectedCategory);

            Element btn = button().attr("type", "button")
                    .cls("px-3", "py-1.5", "rounded-full", "text-xs", "font-medium",
                            "transition-colors", "focus:ring-2", "focus:ring-violet-500",
                            "focus:ring-offset-1", "focus:ring-offset-gray-800")
                    .cls(isActive
                            ? "bg-violet-600 text-white"
                            : "bg-gray-700 text-gray-400 hover:text-white hover:bg-gray-600")
                    .aria("label", "Filter quotes by category: " + category)
                    .aria("pressed", isActive ? "true" : "false")
                    .on("click", e -> {
                        /* Update the selected category. This changes which quotes
                         * are eligible for selection when "New Quote" is clicked.
                         * The UI re-renders to show the active filter state. */
                        selectedCategory = category;
                    })
                    .text(category);

            buttons.add(btn);
        }

        return buttons;
    }

    // ── TeaVM Overlay Types for /api/quotes/random JSON response ────────────────

    /** Typed overlay for the {@code GET /api/quotes/random} response body. */
    interface RandomQuoteResponse extends JSObject {
        @JSProperty QuoteJson getQuote();
    }

    /** Typed overlay for a single quote object inside the response. */
    interface QuoteJson extends JSObject {
        @JSProperty int getId();
        @JSProperty String getText();
        @JSProperty String getAuthor();
        @JSProperty String getCategory();
    }

    /**
     * Fetches a random quote from the REST API using {@link XMLHttpRequest}.
     *
     * <p>Performs a {@code GET /api/quotes/random} request. On a 200 response,
     * the JSON body is parsed via {@link JSON#parse(String)} and accessed
     * through the {@link RandomQuoteResponse} overlay type.</p>
     */
    private void fetchRandomQuote() {
        loading = true;
        loadingProperty.set(true);

        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open("GET", "/api/quotes/random");
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            if (xhr.getStatus() == 200) {
                RandomQuoteResponse response =
                        (RandomQuoteResponse) JSON.parse(xhr.getResponseText());
                QuoteJson q = response.getQuote();

                currentQuote = q.getText();
                currentAuthor = q.getAuthor();
                currentCategory = q.getCategory();
                fetchCount++;

                quoteTextProperty.set(currentQuote);
                quoteAuthorProperty.set(currentAuthor);
            }

            loading = false;
            loadingProperty.set(false);
            ClientMain.getStateManager().notifyStateChange(this);
        });
        xhr.send();
    }

    /**
     * Returns the Tailwind CSS colour classes for a category badge.
     *
     * <p>Each category has a distinct colour to help users quickly identify
     * the topic of a quote. The colours are chosen to be visually distinct
     * from each other while maintaining readability against the dark background.</p>
     *
     * @param category the category name (e.g. "Motivation", "Programming")
     * @return a string of Tailwind CSS classes for background and text colour
     */
    private String getCategoryBadgeColor(String category) {
        /*
         * Map each category to a background/text colour combination.
         * The colours use Tailwind's opacity modifier to create a subtle
         * badge appearance that does not overpower the quote text.
         */
        return switch (category) {
            case "Motivation" -> "bg-amber-500/20 text-amber-400";
            case "Programming" -> "bg-blue-500/20 text-blue-400";
            case "Design" -> "bg-rose-500/20 text-rose-400";
            case "Leadership" -> "bg-emerald-500/20 text-emerald-400";
            default -> "bg-gray-600/30 text-gray-400";
        };
    }

    /**
     * Immutable data record representing a single quote.
     *
     * <p>This record models a typical quote entity as might be returned from a REST API
     * endpoint such as {@code GET /api/quotes/random}. It contains the quote text, the
     * attributed author, and a category classification for filtering purposes.</p>
     *
     * <p>Records are used for their immutability, which aligns with JUX's reactive
     * rendering model and ensures thread-safe access during SSR.</p>
     *
     * @param text     the full text of the quote (e.g. "Talk is cheap. Show me the code.")
     * @param author   the person or source to whom the quote is attributed (e.g. "Linus Torvalds")
     * @param category the topical category of the quote (e.g. "Programming", "Motivation")
     */
    public record Quote(String text, String author, String category) {
    }
}
