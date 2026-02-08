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
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.json.JSON;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.State;
import xss.it.jux.client.ClientMain;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static xss.it.jux.core.Elements.*;

/**
 * Interactive user browsing widget demonstrating API data fetching patterns in JUX.
 *
 * <p>This component showcases how the jux-reactive module's observable collections and
 * properties integrate with JUX's {@code @State} system to build a rich, filterable
 * user directory. It simulates a common pattern seen in admin panels and dashboards
 * where a list of users is fetched from a REST API, displayed in a scrollable list,
 * filtered by a search query, and selectable for detail viewing.</p>
 *
 * <h2>Reactive Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@link ObservableList} for the user collection</b> -- the user list is backed
 *       by a jux-reactive {@code ObservableList} created via
 *       {@link JuxCollections#observableArrayList()}. Mutations to this list (add, remove,
 *       setAll) are observable by listeners, enabling future integration with data-binding
 *       pipelines that can react to collection-level changes independently of JUX's
 *       re-render cycle.</li>
 *   <li><b>{@link SimpleStringProperty} for search text</b> -- the search filter text
 *       is wrapped in a reactive string property. This allows external code or derived
 *       bindings to observe changes to the filter independently. For example, a debounced
 *       API search binding could listen to this property and fire requests after a delay.</li>
 *   <li><b>{@link SimpleBooleanProperty} for loading state</b> -- the loading flag is
 *       wrapped in a reactive boolean property. This demonstrates how loading states
 *       can be observed by parent components or analytics bindings without coupling
 *       the widget to those concerns.</li>
 *   <li><b>{@code @State} for UI-driving fields</b> -- the search query, selected user ID,
 *       and loading flag are also tracked as {@code @State} fields so that changes trigger
 *       JUX's re-render/diff/patch cycle on the client.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>Search/filter with real-time feedback</b> -- as the user types into the search
 *       input, the displayed list is filtered case-insensitively against name, email, and
 *       role fields. The result count is updated in an aria-live region.</li>
 *   <li><b>Master-detail pattern</b> -- clicking a user card in the list selects it and
 *       displays a detail panel on the right with full information. This is a common
 *       pattern in data-driven applications.</li>
 *   <li><b>Simulated API refresh</b> -- the "Refresh" button sets loading to true and
 *       simulates an API call. In a real application, this would use
 *       {@code XMLHttpRequest} via TeaVM to fetch fresh data from a REST endpoint.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the user list uses a semantic {@code <ul>}
 *       with {@code role="listbox"} and each user card has {@code role="option"} to
 *       communicate the selectable nature of the list to assistive technology.</li>
 *   <li><b>2.1.1 Keyboard</b> -- every user card has {@code tabIndex(0)} making it
 *       focusable via keyboard. Cards respond to both click and keyboard Enter events.</li>
 *   <li><b>3.3.2 Labels or Instructions</b> -- the search input has an explicit
 *       {@code aria-label} and a visible placeholder providing usage guidance.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the filtered result count is in an
 *       {@code aria-live="polite"} region so screen readers announce "X users found"
 *       as the user types, without stealing focus.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- the selected user card is indicated with
 *       {@code aria-selected="true"} and visual highlighting. The detail panel is
 *       linked to the list via {@code aria-controls}.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The component renders with 8 pre-populated sample users in its initial state,
 * providing a realistic preview during server-side rendering. All interactive features
 * (search, selection, refresh) become functional after client-side hydration.</p>
 *
 * @see xss.it.jux.reactive.collections.ObservableList
 * @see xss.it.jux.reactive.property.SimpleStringProperty
 * @see xss.it.jux.reactive.property.SimpleBooleanProperty
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class UserBrowserWidget extends Component {

    // ── Reactive Properties (jux-reactive) ────────────────────────────────────────
    //
    // These properties provide an observable layer on top of the @State fields.
    // While @State drives JUX's re-render cycle, the reactive properties enable
    // fine-grained listeners and bindings that can be composed externally.

    /**
     * Reactive string property tracking the current search/filter text.
     *
     * <p>This property is kept in sync with the {@code searchQuery} @State field.
     * External listeners can observe this property to implement debounced API
     * search, analytics tracking, or derived filter bindings without coupling
     * to the widget's internal rendering logic.</p>
     */
    private final SimpleStringProperty searchTextProperty =
            new SimpleStringProperty(this, "searchText", "");

    /**
     * Reactive boolean property tracking whether the widget is in a loading state.
     *
     * <p>When {@code true}, the UI displays a loading indicator and disables
     * interactive elements. This property can be observed by parent components
     * or global loading overlays to coordinate UI state across the application.</p>
     */
    private final SimpleBooleanProperty loadingProperty =
            new SimpleBooleanProperty(this, "loading", false);

    /**
     * Observable list of users backing the user directory.
     *
     * <p>Created via {@link JuxCollections#observableArrayList()} and initialized
     * with 8 sample users representing a typical API response. The observable
     * nature of this list means that future integrations (such as real-time
     * WebSocket updates) can push new users into the list and have listeners
     * react automatically.</p>
     */
    private final ObservableList<User> users = JuxCollections.observableArrayList(
            new User(1, "Alice Johnson", "alice@example.com", "Admin",
                    "Engineering", "AJ", true),
            new User(2, "Bob Martinez", "bob@example.com", "Developer",
                    "Engineering", "BM", true),
            new User(3, "Carol Chen", "carol@example.com", "Designer",
                    "Product", "CC", true),
            new User(4, "David Kim", "david@example.com", "Manager",
                    "Operations", "DK", true),
            new User(5, "Eva Rossi", "eva@example.com", "Developer",
                    "Engineering", "ER", false),
            new User(6, "Frank Obi", "frank@example.com", "Analyst",
                    "Finance", "FO", true),
            new User(7, "Grace Liu", "grace@example.com", "Designer",
                    "Product", "GL", true),
            new User(8, "Henry Patel", "henry@example.com", "DevOps",
                    "Infrastructure", "HP", false)
    );

    // ── @State Fields (JUX reactivity) ────────────────────────────────────────────
    //
    // These fields drive the JUX re-render cycle. When any of these change on
    // the client side, the framework calls render() again, diffs the resulting
    // element tree against the previous one, and patches only changed DOM nodes.

    /**
     * The current search query entered by the user in the filter input.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler. The value
     * is used to filter the user list case-insensitively against name, email, and
     * role fields. An empty string matches all users.</p>
     */
    @State
    private String searchQuery = "";

    /**
     * The ID of the currently selected user, or {@code -1} if no user is selected.
     *
     * <p>Updated when the user clicks or keyboard-activates a user card in the list.
     * When a valid user ID is set, the detail panel on the right displays that
     * user's full information.</p>
     */
    @State
    private int selectedUserId = -1;

    /**
     * Whether the widget is currently in a loading state (simulating an API fetch).
     *
     * <p>When {@code true}, the UI shows a loading indicator overlay and the
     * "Refresh" button displays "Loading..." text. This flag is set to {@code true}
     * when the user clicks "Refresh" and would be set back to {@code false} when
     * the simulated API response arrives.</p>
     */
    @State
    private boolean loading = false;

    /**
     * Initializes the widget after client-side hydration.
     *
     * <p>This lifecycle hook runs once after the component's server-rendered HTML
     * has been hydrated with event listeners and state on the client. In a real
     * application, this is where the initial API fetch would be triggered to load
     * the user list from a REST endpoint.</p>
     *
     * <p>The method also wires up synchronization between the {@code @State} fields
     * and their corresponding jux-reactive property counterparts, ensuring that
     * both systems stay in sync.</p>
     *
     * <p><b>Note:</b> Since TeaVM compilation is not yet wired up, the actual HTTP
     * call is described in comments. The component renders with sample data for the
     * SSR preview.</p>
     */
    @Override
    public void onMount() {
        fetchUsers();
    }

    /**
     * Performs cleanup before the component is removed from the DOM.
     *
     * <p>In a real application, this would cancel any in-flight API requests
     * and remove any reactive property listeners to prevent memory leaks.</p>
     */
    @Override
    public void onUnmount() {
        /* No interval timers to clear; XHR callbacks are harmless after unmount. */
    }

    // ── TeaVM Overlay Types for /api/users JSON response ────────────────────────

    /** Typed overlay for the {@code GET /api/users} response body. */
    interface UsersResponse extends JSObject {
        @JSProperty JSArrayReader<UserJson> getUsers();
        @JSProperty int getTotal();
    }

    /** Typed overlay for a single user object inside the response. */
    interface UserJson extends JSObject {
        @JSProperty int getId();
        @JSProperty String getName();
        @JSProperty String getEmail();
        @JSProperty String getRole();
        @JSProperty String getDepartment();
        @JSProperty String getAvatar();
        @JSProperty("active") boolean isActive();
    }

    /**
     * Fetches user data from the REST API and updates the observable list.
     *
     * <p>Performs a {@code GET /api/users} request using TeaVM's
     * {@link XMLHttpRequest}. On success, the JSON response is parsed
     * via {@link JSON#parse(String)} and accessed through the
     * {@link UsersResponse} overlay type.</p>
     */
    private void fetchUsers() {
        loading = true;
        loadingProperty.set(true);

        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open("GET", "/api/users");
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            if (xhr.getStatus() == 200) {
                UsersResponse response = (UsersResponse) JSON.parse(xhr.getResponseText());
                JSArrayReader<UserJson> arr = response.getUsers();

                List<User> fetched = new ArrayList<>(arr.getLength());
                for (int i = 0; i < arr.getLength(); i++) {
                    UserJson u = arr.get(i);
                    fetched.add(new User(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getRole(),
                            u.getDepartment(),
                            u.getAvatar(),
                            u.isActive()
                    ));
                }

                users.setAll(fetched);
            }

            loading = false;
            loadingProperty.set(false);
            ClientMain.getStateManager().notifyStateChange(this);
        });
        xhr.send();
    }

    /**
     * Builds the virtual DOM tree for the user browser widget.
     *
     * <p>The rendered structure is a two-column layout:</p>
     * <ol>
     *   <li><b>Left panel (list panel)</b> -- contains the search input, refresh button,
     *       result count indicator, and a scrollable list of user cards.</li>
     *   <li><b>Right panel (detail panel)</b> -- shows the full details of the currently
     *       selected user, or a placeholder message if no user is selected.</li>
     * </ol>
     *
     * @return the root element of the user browser widget, never null
     */
    @Override
    public Element render() {
        /*
         * Filter the user list against the current search query. The search is
         * case-insensitive and matches against name, email, and role fields.
         * This ensures a forgiving search experience where "admin" matches
         * users with "Admin" role or "admin@example.com" email.
         */
        String lowerQuery = searchQuery.toLowerCase(Locale.ROOT);
        List<User> filtered = users.stream()
                .filter(user -> lowerQuery.isEmpty()
                        || user.name().toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || user.email().toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || user.role().toLowerCase(Locale.ROOT).contains(lowerQuery))
                .toList();

        /*
         * Attempt to find the currently selected user from the unfiltered list.
         * We search the full list (not filtered) so that selecting a user and then
         * changing the search does not lose the selection unless explicitly cleared.
         */
        User selectedUser = users.stream()
                .filter(u -> u.id() == selectedUserId)
                .findFirst()
                .orElse(null);

        /* Build and return the complete two-column layout. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Header Row: Title + Refresh Button ──────────────────────────
                 * A flex row with the widget title on the left and the refresh
                 * button on the right. The refresh button simulates an API fetch.
                 */
                div().cls("flex", "items-center", "justify-between", "mb-4").children(

                        /* Widget heading. Uses h2 for proper heading hierarchy
                         * (assumes this widget is within a page with an h1). */
                        h2().cls("text-lg", "font-semibold", "text-white")
                                .text("User Directory"),

                        /* Refresh button: simulates fetching fresh data from the API.
                         * When loading, it shows "Loading..." with a muted appearance
                         * and the aria-disabled attribute to indicate non-interactivity. */
                        button().attr("type", "button")
                                .cls("px-4", "py-2", "text-sm", "font-medium", "rounded-lg",
                                        "transition-colors", "focus:ring-2", "focus:ring-violet-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .cls(loading
                                        ? "bg-gray-700 text-gray-500 cursor-not-allowed"
                                        : "bg-violet-600 text-white hover:bg-violet-500")
                                .aria("label", loading ? "Loading users" : "Refresh user list")
                                .ariaDisabled(loading)
                                .on("click", e -> {
                                    if (!loading) {
                                        fetchUsers();
                                    }
                                })
                                .text(loading ? "Loading..." : "Refresh")
                ),

                /* ── Search Input ─────────────────────────────────────────────────
                 * A text input for filtering the user list. The "input" event fires
                 * on every keystroke, updating both the @State searchQuery field
                 * and the reactive SimpleStringProperty for external observers.
                 */
                div().cls("relative", "mb-4").children(

                        /* Magnifying glass icon: a decorative Unicode character
                         * positioned inside the left side of the input field.
                         * Hidden from assistive technology since it is decorative. */
                        span().cls("absolute", "left-3", "top-1/2", "-translate-y-1/2",
                                        "text-gray-500", "text-sm")
                                .ariaHidden(true)
                                .text("\uD83D\uDD0D"),

                        /* Search text input. Updates @State and reactive property
                         * on every keystroke. The aria-controls attribute links
                         * this input to the results list for screen reader context. */
                        input()
                                .attr("type", "text")
                                .attr("placeholder", "Search by name, email, or role...")
                                .attr("value", searchQuery)
                                .aria("label", "Search users by name, email, or role")
                                .ariaControls("user-list")
                                .cls("w-full", "bg-gray-700", "text-white", "rounded-lg",
                                        "pl-10", "pr-4", "py-2.5", "text-sm",
                                        "placeholder-gray-500", "border", "border-gray-600",
                                        "focus:border-violet-500", "focus:ring-1",
                                        "focus:ring-violet-500", "focus:outline-none")
                                .on("input", e -> {
                                    /* Update the @State field to trigger re-render. */
                                    searchQuery = e.getValue();
                                    /* Keep the reactive property in sync for external observers. */
                                    searchTextProperty.set(e.getValue());
                                })
                ),

                /* ── Result Count (ARIA Live Region) ──────────────────────────────
                 * Displays the number of matching users. Placed in an aria-live
                 * region so screen readers announce updates as the user types.
                 */
                p().cls("text-xs", "text-gray-500", "mb-3")
                        .ariaLive("polite")
                        .text(filtered.size() + (filtered.size() == 1 ? " user" : " users") + " found"),

                /* ── Two-Column Layout: User List + Detail Panel ──────────────────
                 * A flex row containing the scrollable user list on the left and
                 * the detail panel on the right. On smaller screens, this could
                 * stack vertically via responsive classes.
                 */
                div().cls("flex", "gap-4").children(

                        /* ── Left Panel: Scrollable User List ─────────────────────
                         * A listbox containing user cards. Each card shows the user's
                         * initials in a coloured circle, their name, role, and department.
                         */
                        div().cls("flex-1", "min-w-0").children(
                                ul().id("user-list")
                                        .cls("space-y-2", "max-h-80", "overflow-y-auto",
                                                "pr-1")
                                        .role("listbox")
                                        .aria("label", "User list")
                                        .children(buildUserCards(filtered))
                        ),

                        /* ── Right Panel: User Detail ─────────────────────────────
                         * Shows full information for the selected user, or a
                         * placeholder prompt if no user is selected.
                         */
                        div().id("user-detail")
                                .cls("flex-1", "min-w-0")
                                .children(buildDetailPanel(selectedUser))
                )
        );
    }

    /**
     * Generates the list of user card {@code <li>} elements for the filtered user list.
     *
     * <p>Each card is rendered as a horizontal flex row containing:</p>
     * <ul>
     *   <li>An initials circle with a colour derived from the user's ID</li>
     *   <li>The user's name (bold), role, and department</li>
     *   <li>An active/inactive status dot indicator</li>
     * </ul>
     *
     * <p>The selected card receives visual highlighting (violet left border and
     * slightly brighter background) and the {@code aria-selected="true"} attribute
     * for screen reader users.</p>
     *
     * @param filtered the filtered list of users to render as cards
     * @return a list of {@code <li>} elements, one per user
     */
    private List<Element> buildUserCards(List<User> filtered) {
        /* Collect the generated <li> elements into a mutable list. */
        List<Element> cards = new ArrayList<>();

        for (User user : filtered) {
            /*
             * Determine whether this user is currently selected. The selected
             * card gets a violet left border accent and brighter background.
             */
            boolean isSelected = user.id() == selectedUserId;

            /*
             * Choose a background colour for the initials circle based on the
             * user's ID. This creates a visually distinct palette across users
             * while remaining deterministic (same user always gets same colour).
             */
            String initialsColor = getInitialsColor(user.id());

            /* Build the user card as a listbox option. */
            Element card = li()
                    .cls("flex", "items-center", "gap-3", "p-3", "rounded-lg",
                            "cursor-pointer", "transition-all", "border-l-3")
                    .cls(isSelected
                            ? "bg-gray-700 border-violet-500"
                            : "bg-gray-750 border-transparent hover:bg-gray-700 hover:border-gray-600")
                    .role("option")
                    .ariaSelected(isSelected)
                    .aria("label", user.name() + ", " + user.role() + " in " + user.department()
                            + (user.active() ? ", active" : ", inactive"))
                    .tabIndex(0)
                    .on("click", e -> {
                        /* Select this user, or deselect if already selected. */
                        selectedUserId = (selectedUserId == user.id()) ? -1 : user.id();
                    })
                    .on("keydown", e -> {
                        /* Allow keyboard activation via Enter or Space keys,
                         * matching the expected interaction pattern for listbox options. */
                        if ("Enter".equals(e.getKey()) || " ".equals(e.getKey())) {
                            e.preventDefault();
                            selectedUserId = (selectedUserId == user.id()) ? -1 : user.id();
                        }
                    })
                    .children(

                            /* ── Initials Circle ──────────────────────────────────
                             * A small coloured circle showing the user's initials.
                             * The colour is determined by the user's ID for consistency.
                             * Hidden from screen readers since the name is already
                             * conveyed by the aria-label on the card.
                             */
                            div().cls("w-10", "h-10", "rounded-full", "flex", "items-center",
                                            "justify-center", "text-sm", "font-bold",
                                            "text-white", "shrink-0")
                                    .cls(initialsColor)
                                    .ariaHidden(true)
                                    .text(user.initials()),

                            /* ── Name, Role, Department ──────────────────────────
                             * Text block showing the user's identity and organizational
                             * information. The name is bold, role and department are
                             * in smaller, muted text below.
                             */
                            div().cls("flex-1", "min-w-0").children(
                                    p().cls("text-sm", "font-medium", "text-white", "truncate")
                                            .text(user.name()),
                                    p().cls("text-xs", "text-gray-400", "truncate")
                                            .text(user.role() + " \u2022 " + user.department())
                            ),

                            /* ── Status Indicator Dot ─────────────────────────────
                             * A small coloured dot indicating whether the user is
                             * active (green) or inactive (gray). Hidden from screen
                             * readers since the status is conveyed in the aria-label.
                             */
                            div().cls("w-2.5", "h-2.5", "rounded-full", "shrink-0")
                                    .cls(user.active() ? "bg-emerald-400" : "bg-gray-600")
                                    .ariaHidden(true)
                    );

            cards.add(card);
        }

        return cards;
    }

    /**
     * Builds the detail panel content for the selected user.
     *
     * <p>If no user is selected ({@code selectedUser == null}), a placeholder message
     * is shown prompting the user to select someone from the list. When a user is
     * selected, the panel displays their full profile information including name,
     * email, role, department, and active status badge.</p>
     *
     * @param selectedUser the currently selected user, or {@code null} if none
     * @return the detail panel element
     */
    private Element buildDetailPanel(User selectedUser) {
        /*
         * If no user is selected, show a centered placeholder message guiding
         * the user to select someone from the list on the left.
         */
        if (selectedUser == null) {
            return div().cls("bg-gray-750", "rounded-xl", "p-6", "h-full",
                            "flex", "items-center", "justify-center", "min-h-48")
                    .children(
                            p().cls("text-gray-500", "text-sm", "text-center")
                                    .text("Select a user from the list to view their details.")
                    );
        }

        /* Determine the initials circle colour for the detail view. */
        String initialsColor = getInitialsColor(selectedUser.id());

        /*
         * Build the detail panel with the selected user's full information.
         * The layout is a vertical stack: large initials circle at top,
         * followed by name, email, and metadata rows.
         */
        return div().cls("bg-gray-750", "rounded-xl", "p-6").children(

                /* ── Profile Header: Initials + Name ──────────────────────────
                 * A large initials circle centered at the top, followed by
                 * the user's full name and email address.
                 */
                div().cls("text-center", "mb-5").children(

                        /* Large initials circle for the detail view. */
                        div().cls("w-16", "h-16", "rounded-full", "flex", "items-center",
                                        "justify-center", "text-xl", "font-bold",
                                        "text-white", "mx-auto", "mb-3")
                                .cls(initialsColor)
                                .ariaHidden(true)
                                .text(selectedUser.initials()),

                        /* User's full name displayed prominently. */
                        h3().cls("text-lg", "font-semibold", "text-white")
                                .text(selectedUser.name()),

                        /* User's email as a clickable mailto link. */
                        a().cls("text-sm", "text-violet-400", "hover:text-violet-300",
                                        "transition-colors")
                                .attr("href", "mailto:" + selectedUser.email())
                                .aria("label", "Send email to " + selectedUser.name())
                                .text(selectedUser.email())
                ),

                /* ── Divider ──────────────────────────────────────────────────
                 * A thin horizontal line separating the header from the detail rows.
                 */
                div().cls("border-t", "border-gray-700", "my-4")
                        .role("presentation")
                        .ariaHidden(true),

                /* ── Detail Rows ──────────────────────────────────────────────
                 * Key-value pairs showing role, department, and status.
                 * Each row uses a two-column flex layout with a muted label
                 * on the left and the value on the right.
                 */
                buildDetailRow("Role", selectedUser.role()),
                buildDetailRow("Department", selectedUser.department()),
                buildDetailRow("User ID", String.valueOf(selectedUser.id())),

                /* ── Status Badge ─────────────────────────────────────────────
                 * A coloured badge indicating active/inactive status. Active
                 * users get a green badge, inactive users get a gray badge.
                 */
                div().cls("flex", "items-center", "justify-between", "py-2").children(
                        span().cls("text-xs", "text-gray-400", "uppercase", "tracking-wide")
                                .text("Status"),
                        span().cls("px-2.5", "py-0.5", "rounded-full", "text-xs", "font-medium")
                                .cls(selectedUser.active()
                                        ? "bg-emerald-500/20 text-emerald-400"
                                        : "bg-gray-600/30 text-gray-400")
                                .text(selectedUser.active() ? "Active" : "Inactive")
                )
        );
    }

    /**
     * Builds a single key-value detail row for the user detail panel.
     *
     * <p>Each row is a horizontal flex container with the label on the left
     * (muted, uppercase, small) and the value on the right (white, normal size).
     * This creates a clean, consistent layout for displaying user metadata.</p>
     *
     * @param label the row label text (e.g. "Role", "Department")
     * @param value the row value text (e.g. "Developer", "Engineering")
     * @return the detail row element
     */
    private Element buildDetailRow(String label, String value) {
        return div().cls("flex", "items-center", "justify-between", "py-2").children(
                /* Label on the left side, styled as a muted uppercase caption. */
                span().cls("text-xs", "text-gray-400", "uppercase", "tracking-wide")
                        .text(label),
                /* Value on the right side, styled with normal white text. */
                span().cls("text-sm", "text-white")
                        .text(value)
        );
    }

    /**
     * Returns a Tailwind CSS background colour class for a user's initials circle.
     *
     * <p>The colour is deterministically derived from the user's ID using modulo
     * arithmetic, ensuring that the same user always receives the same colour across
     * renders and sessions. The palette uses vibrant yet accessible colours that
     * maintain sufficient contrast against white text.</p>
     *
     * @param userId the user's unique ID, used to select the colour
     * @return a Tailwind CSS background colour class (e.g. "bg-violet-600")
     */
    private String getInitialsColor(int userId) {
        /*
         * A curated palette of 6 background colours that provide good contrast
         * with white text (all meet WCAG 4.5:1 contrast ratio for normal text).
         * The modulo operation distributes users evenly across the palette.
         */
        String[] colors = {
                "bg-violet-600",
                "bg-blue-600",
                "bg-emerald-600",
                "bg-amber-600",
                "bg-rose-600",
                "bg-cyan-600"
        };
        return colors[Math.abs(userId) % colors.length];
    }

    /**
     * Immutable data record representing a single user in the directory.
     *
     * <p>This record models a typical user entity as might be returned from a REST API
     * endpoint such as {@code GET /api/users}. It contains identity fields (id, name,
     * email), organizational fields (role, department), display helpers (initials),
     * and status (active flag).</p>
     *
     * <p>Records are used for their immutability, which aligns with JUX's reactive
     * rendering model: changes to the user list always involve creating new record
     * instances rather than mutating existing ones, ensuring clean state transitions.</p>
     *
     * @param id         unique numeric identifier for the user
     * @param name       the user's full display name (e.g. "Alice Johnson")
     * @param email      the user's email address (e.g. "alice@example.com")
     * @param role       the user's role within the organization (e.g. "Developer", "Admin")
     * @param department the department the user belongs to (e.g. "Engineering", "Product")
     * @param initials   two-letter initials derived from the name (e.g. "AJ")
     * @param active     whether the user account is currently active
     */
    public record User(int id, String name, String email, String role,
                        String department, String initials, boolean active) {
    }
}
