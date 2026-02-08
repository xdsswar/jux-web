/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.components;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.annotation.OnMount;
import xss.it.jux.annotation.OnUnmount;
import xss.it.jux.annotation.State;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;
import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.property.SimpleIntegerProperty;
import xss.it.jux.reactive.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static xss.it.jux.core.Elements.*;

/**
 * Paginated sortable data table widget demonstrating API data fetching patterns in JUX.
 *
 * <p>This component showcases how jux-reactive's observable collections and properties
 * integrate with JUX's {@code @State} system to build a fully functional data table
 * with client-side sorting and pagination. It simulates the common pattern of fetching
 * tabular data from a REST API endpoint (e.g. {@code GET /api/users?page=0&size=5&sort=name,asc})
 * and rendering it in an accessible HTML table.</p>
 *
 * <h2>Reactive Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@link ObservableList} for data rows</b> -- the table data is backed by a
 *       jux-reactive {@code ObservableList} created via
 *       {@link JuxCollections#observableArrayList()}. This enables listeners to observe
 *       mutations to the data set independently of the JUX rendering cycle. In a real
 *       application, a WebSocket connection could push row updates directly into this
 *       list, and the list change events could be used to highlight changed rows.</li>
 *   <li><b>{@link SimpleIntegerProperty} for current page</b> -- the pagination page
 *       number is wrapped in a reactive integer property, enabling external bindings
 *       (such as a URL hash synchronizer or analytics tracker) to observe page changes.</li>
 *   <li><b>{@link SimpleStringProperty} for sort column and direction</b> -- the sort
 *       column name and direction are wrapped in reactive string properties. These can
 *       drive derived bindings such as an auto-generated API URL parameter string that
 *       updates whenever the sort configuration changes.</li>
 *   <li><b>{@link Bindings} integration</b> -- the widget is designed to work with the
 *       Bindings utility for creating computed values. For example, a "query string"
 *       binding could combine the page, sort column, and direction properties into a
 *       URL parameter string like {@code "?page=0&sort=name&dir=asc"}.</li>
 * </ul>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>Column sorting with direction toggle</b> -- clicking a column header sorts
 *       the data by that column. Clicking the same column again toggles the sort
 *       direction (ascending/descending). Sort indicators (arrow characters) show the
 *       current sort state.</li>
 *   <li><b>Client-side pagination</b> -- the data is paginated with configurable page
 *       size. Navigation controls include Previous/Next buttons and numbered page links.
 *       The current page is visually highlighted and announced to screen readers.</li>
 *   <li><b>Status badges</b> -- boolean active/inactive status is rendered as coloured
 *       badges, demonstrating conditional styling within table cells.</li>
 *   <li><b>Alternating row backgrounds</b> -- even and odd rows have subtly different
 *       background shades for improved readability.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the table uses proper semantic HTML:
 *       {@code <table>}, {@code <caption>}, {@code <thead>}, {@code <tbody>},
 *       {@code <th scope="col">}, and {@code <td>}. This ensures screen readers can
 *       navigate the table by row/column and announce header-to-cell relationships.</li>
 *   <li><b>1.3.2 Meaningful Sequence</b> -- the DOM order matches the visual reading
 *       order: caption, headers, data rows, then pagination controls.</li>
 *   <li><b>2.1.1 Keyboard</b> -- sortable column headers use {@code tabIndex(0)} and
 *       respond to both click and keyboard Enter events. Pagination buttons are native
 *       {@code <button>} elements.</li>
 *   <li><b>4.1.2 Name, Role, Value</b> -- sortable columns have {@code aria-sort}
 *       attributes indicating the current sort direction ("ascending", "descending",
 *       or "none"). Pagination buttons have descriptive {@code aria-label} attributes.
 *       The current page button has {@code aria-current="page"}.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the "Showing X-Y of Z" indicator is in an
 *       {@code aria-live="polite"} region so screen readers announce pagination changes.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The component renders with 12 pre-populated sample rows, sorted by name ascending,
 * showing the first page (rows 1-5). All sorting and pagination controls are rendered
 * in their initial state and become interactive after client-side hydration.</p>
 *
 * @see xss.it.jux.reactive.collections.ObservableList
 * @see xss.it.jux.reactive.property.SimpleIntegerProperty
 * @see xss.it.jux.reactive.property.SimpleStringProperty
 * @see xss.it.jux.reactive.binding.Bindings
 * @see xss.it.jux.annotation.State
 */
@JuxComponent(clientSide = true)
public class DataTableWidget extends Component {

    // ── Column Definitions ────────────────────────────────────────────────────────
    //
    // The table columns are defined as a static list of column descriptors. Each
    // descriptor contains the internal field key (used for sorting), the display
    // header label, and whether the column supports sorting.

    /**
     * Column descriptors defining the table structure.
     *
     * <p>Each column has a field key (matching the property name used for sort
     * comparisons), a display label (shown in the {@code <th>} header), and a
     * sortable flag indicating whether clicking the header triggers sorting.</p>
     *
     * <p>The "status" column uses a special rendering path that displays a
     * coloured badge instead of raw text.</p>
     */
    private static final List<Column> COLUMNS = List.of(
            new Column("id", "ID", true),
            new Column("name", "Name", true),
            new Column("email", "Email", true),
            new Column("role", "Role", true),
            new Column("department", "Department", true),
            new Column("active", "Status", true)
    );

    // ── Reactive Properties (jux-reactive) ────────────────────────────────────────

    /**
     * Reactive integer property tracking the current page number (zero-based).
     *
     * <p>Kept in sync with the {@code currentPage} @State field. External observers
     * can listen to this property for URL hash synchronization (e.g. updating
     * {@code #page=2} in the browser address bar) or analytics tracking of
     * pagination behaviour.</p>
     */
    private final SimpleIntegerProperty pageProperty =
            new SimpleIntegerProperty(this, "currentPage", 0);

    /**
     * Reactive string property tracking the current sort column field key.
     *
     * <p>Kept in sync with the {@code sortColumn} @State field. Together with
     * {@link #sortDirectionProperty}, this enables derived bindings such as an
     * auto-generated API sort parameter string.</p>
     */
    private final SimpleStringProperty sortColumnProperty =
            new SimpleStringProperty(this, "sortColumn", "name");

    /**
     * Reactive string property tracking the current sort direction.
     *
     * <p>Contains "asc" for ascending or "desc" for descending. Kept in sync
     * with the {@code sortAsc} @State field (translated between boolean and string
     * representations). Observable by external bindings for URL parameter generation.</p>
     */
    private final SimpleStringProperty sortDirectionProperty =
            new SimpleStringProperty(this, "sortDirection", "asc");

    /**
     * Observable list of table rows backing the data table.
     *
     * <p>Created via {@link JuxCollections#observableArrayList()} and initialized
     * with 12 sample rows representing a typical API response payload. The observable
     * nature of this list means that real-time data updates (e.g. from a WebSocket)
     * could push new or modified rows into the list and have listeners react to
     * the specific change events.</p>
     */
    private final ObservableList<TableRow> rows = JuxCollections.observableArrayList(
            new TableRow(1, "Alice Johnson", "alice@example.com",
                    "Admin", "Engineering", true),
            new TableRow(2, "Bob Martinez", "bob@example.com",
                    "Developer", "Engineering", true),
            new TableRow(3, "Carol Chen", "carol@example.com",
                    "Designer", "Product", true),
            new TableRow(4, "David Kim", "david@example.com",
                    "Manager", "Operations", true),
            new TableRow(5, "Eva Rossi", "eva@example.com",
                    "Developer", "Engineering", false),
            new TableRow(6, "Frank Obi", "frank@example.com",
                    "Analyst", "Finance", true),
            new TableRow(7, "Grace Liu", "grace@example.com",
                    "Designer", "Product", true),
            new TableRow(8, "Henry Patel", "henry@example.com",
                    "DevOps", "Infrastructure", false),
            new TableRow(9, "Ingrid Berg", "ingrid@example.com",
                    "Developer", "Engineering", true),
            new TableRow(10, "Jack O'Brien", "jack@example.com",
                    "Manager", "Sales", true),
            new TableRow(11, "Keiko Tanaka", "keiko@example.com",
                    "Analyst", "Finance", false),
            new TableRow(12, "Leo Fernandez", "leo@example.com",
                    "Developer", "Engineering", true)
    );

    // ── @State Fields (JUX reactivity) ────────────────────────────────────────────

    /**
     * The current page number (zero-based) for pagination.
     *
     * <p>Page 0 shows the first {@link #pageSize} rows, page 1 shows the next
     * {@code pageSize} rows, and so on. Updated when the user clicks pagination
     * controls or when sorting resets the page to 0.</p>
     */
    @State
    private int currentPage = 0;

    /**
     * The number of rows displayed per page.
     *
     * <p>Set to 5 for the demo to create multiple pages from the 12-row dataset.
     * In a real application, this might be configurable via a dropdown or
     * application setting.</p>
     */
    @State
    private int pageSize = 5;

    /**
     * The field key of the column currently used for sorting.
     *
     * <p>Must match one of the {@code key} values from the {@link #COLUMNS}
     * definitions. The default is "name", sorting users alphabetically by name.</p>
     */
    @State
    private String sortColumn = "name";

    /**
     * Whether the current sort direction is ascending ({@code true}) or
     * descending ({@code false}).
     *
     * <p>Toggled when the user clicks the same column header again. When
     * switching to a different column, the direction resets to ascending.</p>
     */
    @State
    private boolean sortAsc = true;

    /**
     * Initializes the widget after client-side hydration.
     *
     * <p>In a real application, this is where the initial data fetch would occur,
     * loading table rows from a paginated API endpoint. The sort and pagination
     * parameters would be sent as query parameters to the backend.</p>
     *
     * <p><b>Note:</b> Since TeaVM compilation is not yet wired up, the actual HTTP
     * call is described in comments. The component renders with sample data for the
     * SSR preview.</p>
     */
    @OnMount
    public void onMount() {
        /*
         * In a TeaVM-compiled environment, this would fetch the initial page of data:
         *
         *   String url = "/api/users?page=" + currentPage
         *              + "&size=" + pageSize
         *              + "&sort=" + sortColumn
         *              + "&dir=" + (sortAsc ? "asc" : "desc");
         *
         *   XMLHttpRequest xhr = XMLHttpRequest.create();
         *   xhr.open("GET", url);
         *   xhr.setOnReadyStateChange(() -> {
         *       if (xhr.getReadyState() == XMLHttpRequest.DONE && xhr.getStatus() == 200) {
         *           String json = xhr.getResponseText();
         *           PagedResponse<TableRow> response = parseResponse(json);
         *           rows.setAll(response.content());
         *           // Total count would come from response.totalElements()
         *       }
         *   });
         *   xhr.send();
         *
         * Additionally, a derived binding for the API URL could be set up:
         *
         *   StringBinding apiUrl = Bindings.createStringBinding(
         *       () -> "/api/users?page=" + pageProperty.get()
         *           + "&sort=" + sortColumnProperty.get()
         *           + "," + sortDirectionProperty.get(),
         *       pageProperty, sortColumnProperty, sortDirectionProperty
         *   );
         */
    }

    /**
     * Performs cleanup before the component is removed from the DOM.
     *
     * <p>Cancels any in-flight API requests, disposes of computed bindings,
     * and clears the observable list to release row references.</p>
     */
    @OnUnmount
    public void onUnmount() {
        /*
         * Cleanup tasks for a real implementation:
         *   - Cancel any pending XMLHttpRequest via xhr.abort()
         *   - Dispose of any Bindings created in onMount (e.g. apiUrl.dispose())
         *   - Remove listeners from pageProperty, sortColumnProperty, sortDirectionProperty
         */
    }

    /**
     * Builds the virtual DOM tree for the data table widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>An HTML {@code <table>} with a descriptive {@code <caption>}</li>
     *   <li>A {@code <thead>} with sortable column headers showing sort indicators</li>
     *   <li>A {@code <tbody>} with paginated, sorted data rows</li>
     *   <li>A pagination footer with Previous/Next buttons and page numbers</li>
     * </ol>
     *
     * @return the root element of the data table widget, never null
     */
    @Override
    public Element render() {
        /*
         * Sort the full data set according to the current sort column and direction.
         * This produces a new sorted list without mutating the underlying observable
         * list, preserving the original data order for re-sorting.
         */
        List<TableRow> sorted = sortRows(new ArrayList<>(rows));

        /*
         * Calculate pagination boundaries. The total number of pages is derived
         * from the data size and page size, rounding up to include a partial
         * final page.
         */
        int totalRows = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / pageSize));

        /* Clamp the current page to valid bounds in case the data shrinks. */
        int safePage = Math.min(currentPage, totalPages - 1);

        /* Extract the slice of rows for the current page. */
        int startIndex = safePage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRows);
        List<TableRow> pageRows = sorted.subList(startIndex, endIndex);

        /* Calculate 1-based display numbers for the "Showing X-Y of Z" indicator. */
        int displayStart = totalRows > 0 ? startIndex + 1 : 0;
        int displayEnd = endIndex;

        /* Build and return the complete data table element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Table Container with Horizontal Scroll ───────────────────────
                 * A wrapper div that provides horizontal scrolling on narrow screens
                 * to prevent the table from overflowing its container.
                 */
                div().cls("overflow-x-auto", "rounded-lg", "border",
                                "border-gray-700")
                        .children(

                                /* ── The Semantic HTML Table ──────────────────────
                                 * A proper HTML table with caption, thead, and tbody.
                                 * The caption provides a programmatic description of
                                 * the table's purpose for screen readers.
                                 */
                                table().cls("w-full", "text-sm", "text-left").children(

                                        /* Table caption: a programmatic description that
                                         * is announced by screen readers when the table
                                         * receives focus. Visually styled as a small
                                         * header above the table. */
                                        caption().cls("px-4", "py-3", "text-left",
                                                        "text-sm", "font-medium",
                                                        "text-gray-400",
                                                        "bg-gray-800")
                                                .text("User directory — sortable and paginated"),

                                        /* Table header row with sortable column headers. */
                                        thead().cls("text-xs", "text-gray-400",
                                                        "uppercase", "tracking-wider",
                                                        "bg-gray-750")
                                                .children(
                                                        tr().children(buildHeaderCells())
                                                ),

                                        /* Table body with the current page of data rows. */
                                        tbody().children(buildDataRows(pageRows, startIndex))
                                )
                        ),

                /* ── Pagination Footer ────────────────────────────────────────────
                 * A flex row containing the "Showing X-Y of Z" indicator on the
                 * left and pagination controls on the right.
                 */
                div().cls("flex", "items-center", "justify-between",
                                "mt-4", "px-1")
                        .children(

                                /* "Showing X-Y of Z" indicator in an aria-live region.
                                 * Screen readers announce this when the page changes,
                                 * providing context about the current position in
                                 * the data set. */
                                p().cls("text-sm", "text-gray-400")
                                        .ariaLive("polite")
                                        .text("Showing " + displayStart + "-" + displayEnd
                                                + " of " + totalRows),

                                /* Pagination navigation: Previous, page numbers, Next. */
                                nav().aria("label", "Table pagination")
                                        .children(
                                                div().cls("flex", "items-center", "gap-1")
                                                        .children(buildPaginationControls(
                                                                safePage, totalPages))
                                        )
                        )
        );
    }

    /**
     * Builds the header cells ({@code <th>}) for the table header row.
     *
     * <p>Each header cell is rendered from the {@link #COLUMNS} definitions. Sortable
     * columns include a sort indicator arrow and respond to click and keyboard events.
     * The {@code aria-sort} attribute indicates the current sort state for each column:
     * "ascending", "descending", or "none".</p>
     *
     * @return a list of {@code <th>} elements for the table header
     */
    private List<Element> buildHeaderCells() {
        /* Collect the generated <th> elements into a mutable list. */
        List<Element> cells = new ArrayList<>();

        for (Column column : COLUMNS) {
            /*
             * Determine the aria-sort value for this column. Only the currently
             * sorted column has a direction; all others are "none".
             */
            String ariaSort;
            if (column.key().equals(sortColumn)) {
                ariaSort = sortAsc ? "ascending" : "descending";
            } else {
                ariaSort = "none";
            }

            /*
             * Build the sort indicator text. The active sort column shows an
             * upward or downward arrow; other columns show a neutral double arrow.
             */
            String sortIndicator;
            if (column.key().equals(sortColumn)) {
                sortIndicator = sortAsc ? " \u2191" : " \u2193";
            } else {
                sortIndicator = " \u2195";
            }

            /* Create a final local variable for lambda capture. */
            final String colKey = column.key();

            /* Build the <th> element with sort interaction. */
            Element th = th().cls("px-4", "py-3", "font-medium")
                    .attr("scope", "col")
                    .aria("sort", column.sortable() ? ariaSort : null);

            if (column.sortable()) {
                /*
                 * Sortable column: wrap the header label and sort indicator in
                 * an interactive container that responds to click and keyboard.
                 * The cursor changes to pointer to indicate interactivity.
                 */
                th = th.cls("cursor-pointer", "hover:text-white",
                                "transition-colors", "select-none")
                        .tabIndex(0)
                        .aria("label", "Sort by " + column.label()
                                + (colKey.equals(sortColumn)
                                ? (sortAsc ? ", currently ascending" : ", currently descending")
                                : ""))
                        .on("click", e -> handleSort(colKey))
                        .on("keydown", e -> {
                            /* Allow keyboard activation via Enter or Space keys. */
                            if ("Enter".equals(e.getKey()) || " ".equals(e.getKey())) {
                                e.preventDefault();
                                handleSort(colKey);
                            }
                        })
                        .children(
                                /* Column label text. */
                                span().text(column.label()),
                                /* Sort direction indicator arrow. Uses a lighter color
                                 * for the active column and a very muted color for
                                 * inactive columns. */
                                span().cls("ml-1")
                                        .cls(colKey.equals(sortColumn)
                                                ? "text-violet-400"
                                                : "text-gray-600")
                                        .ariaHidden(true)
                                        .text(sortIndicator)
                        );
            } else {
                /* Non-sortable column: just display the label text. */
                th = th.text(column.label());
            }

            cells.add(th);
        }

        return cells;
    }

    /**
     * Builds the data rows ({@code <tr>}) for the current page of table data.
     *
     * <p>Each row contains cells matching the column definitions. The "active" column
     * is rendered as a coloured status badge instead of raw text. Alternating rows
     * have subtly different background colours for improved readability.</p>
     *
     * @param pageRows   the sorted, paginated rows to display
     * @param startIndex the zero-based index of the first row on this page (used
     *                   for alternating row background calculation)
     * @return a list of {@code <tr>} elements for the table body
     */
    private List<Element> buildDataRows(List<TableRow> pageRows, int startIndex) {
        /* Collect the generated <tr> elements into a mutable list. */
        List<Element> tableRows = new ArrayList<>();

        for (int i = 0; i < pageRows.size(); i++) {
            TableRow row = pageRows.get(i);

            /*
             * Determine whether this is an even or odd row for alternating
             * background colours. The calculation uses the absolute row index
             * (not the page-relative index) for consistent colouring across pages.
             */
            boolean isEvenRow = (startIndex + i) % 2 == 0;

            /* Build the <tr> with alternating background and hover effect. */
            Element tr = tr()
                    .cls("border-t", "border-gray-700/50", "transition-colors",
                            "hover:bg-gray-700/50")
                    .cls(isEvenRow ? "bg-gray-800" : "bg-gray-750")
                    .children(

                            /* ID column: displayed in a muted monospace style to
                             * visually distinguish numeric identifiers from text. */
                            td().cls("px-4", "py-3", "text-gray-500", "font-mono",
                                            "text-xs")
                                    .text(String.valueOf(row.id())),

                            /* Name column: the primary identifying text, displayed
                             * in white with medium font weight for emphasis. */
                            td().cls("px-4", "py-3", "text-white", "font-medium")
                                    .text(row.name()),

                            /* Email column: displayed as a subtle link-styled text.
                             * In a real application, this could be a clickable mailto link. */
                            td().cls("px-4", "py-3", "text-gray-400")
                                    .text(row.email()),

                            /* Role column: displayed in standard gray text. */
                            td().cls("px-4", "py-3", "text-gray-300")
                                    .text(row.role()),

                            /* Department column: displayed in muted gray text. */
                            td().cls("px-4", "py-3", "text-gray-400")
                                    .text(row.department()),

                            /* Status column: rendered as a coloured badge instead of
                             * raw boolean text. Active users get a green badge,
                             * inactive users get a gray badge. */
                            td().cls("px-4", "py-3").children(
                                    span().cls("px-2.5", "py-0.5", "rounded-full",
                                                    "text-xs", "font-medium")
                                            .cls(row.active()
                                                    ? "bg-emerald-500/20 text-emerald-400"
                                                    : "bg-gray-600/30 text-gray-500")
                                            .text(row.active() ? "Active" : "Inactive")
                            )
                    );

            tableRows.add(tr);
        }

        /* Handle empty state: if there are no rows, show a full-width message. */
        if (tableRows.isEmpty()) {
            tableRows.add(
                    tr().children(
                            td().cls("px-4", "py-8", "text-center", "text-gray-500")
                                    .attr("colspan", String.valueOf(COLUMNS.size()))
                                    .text("No data available.")
                    )
            );
        }

        return tableRows;
    }

    /**
     * Builds the pagination control elements (Previous, page numbers, Next).
     *
     * <p>The pagination control renders:</p>
     * <ul>
     *   <li>A "Previous" button, disabled on the first page</li>
     *   <li>Numbered page buttons (1-based display), with the current page
     *       highlighted and marked with {@code aria-current="page"}</li>
     *   <li>A "Next" button, disabled on the last page</li>
     * </ul>
     *
     * <p>Disabled buttons use {@code aria-disabled="true"} and a muted visual
     * style to indicate that they cannot be activated.</p>
     *
     * @param safePage   the current page number (zero-based, clamped to valid bounds)
     * @param totalPages the total number of pages
     * @return a list of button elements for the pagination controls
     */
    private List<Element> buildPaginationControls(int safePage, int totalPages) {
        /* Collect the generated pagination elements into a mutable list. */
        List<Element> controls = new ArrayList<>();

        /*
         * Determine whether the Previous and Next buttons should be disabled.
         * Previous is disabled on the first page, Next on the last.
         */
        boolean hasPrevious = safePage > 0;
        boolean hasNext = safePage < totalPages - 1;

        /* ── Previous Button ──────────────────────────────────────────────────
         * Navigates to the previous page. Disabled on page 0. Uses a left
         * chevron character as the label with descriptive aria-label.
         */
        controls.add(
                button().attr("type", "button")
                        .cls("px-3", "py-1.5", "rounded-lg", "text-sm",
                                "transition-colors", "focus:ring-2",
                                "focus:ring-violet-500", "focus:ring-offset-1",
                                "focus:ring-offset-gray-800")
                        .cls(hasPrevious
                                ? "text-gray-300 hover:bg-gray-700 hover:text-white"
                                : "text-gray-600 cursor-not-allowed")
                        .aria("label", "Go to previous page")
                        .ariaDisabled(!hasPrevious)
                        .on("click", e -> {
                            /* Navigate to the previous page if available. */
                            if (hasPrevious) {
                                currentPage = safePage - 1;
                                pageProperty.set(currentPage);
                            }
                        })
                        .text("\u2039 Prev")
        );

        /* ── Numbered Page Buttons ────────────────────────────────────────────
         * One button per page. The current page is highlighted with a violet
         * background and aria-current="page". Other pages have a subtle gray
         * background that brightens on hover.
         */
        for (int i = 0; i < totalPages; i++) {
            /* Capture the page index in a final local variable for lambda use. */
            final int pageNum = i;
            boolean isCurrent = i == safePage;

            Element pageBtn = button().attr("type", "button")
                    .cls("w-8", "h-8", "rounded-lg", "text-sm", "font-medium",
                            "transition-colors", "focus:ring-2",
                            "focus:ring-violet-500", "focus:ring-offset-1",
                            "focus:ring-offset-gray-800")
                    .cls(isCurrent
                            ? "bg-violet-600 text-white"
                            : "text-gray-400 hover:bg-gray-700 hover:text-white")
                    .aria("label", "Go to page " + (i + 1))
                    /* Mark the current page for screen readers. */
                    .ariaCurrent(isCurrent ? "page" : null)
                    .on("click", e -> {
                        /* Navigate to the clicked page. */
                        currentPage = pageNum;
                        pageProperty.set(pageNum);
                    })
                    /* Display 1-based page numbers for human readability. */
                    .text(String.valueOf(i + 1));

            controls.add(pageBtn);
        }

        /* ── Next Button ──────────────────────────────────────────────────────
         * Navigates to the next page. Disabled on the last page. Uses a right
         * chevron character as the label.
         */
        controls.add(
                button().attr("type", "button")
                        .cls("px-3", "py-1.5", "rounded-lg", "text-sm",
                                "transition-colors", "focus:ring-2",
                                "focus:ring-violet-500", "focus:ring-offset-1",
                                "focus:ring-offset-gray-800")
                        .cls(hasNext
                                ? "text-gray-300 hover:bg-gray-700 hover:text-white"
                                : "text-gray-600 cursor-not-allowed")
                        .aria("label", "Go to next page")
                        .ariaDisabled(!hasNext)
                        .on("click", e -> {
                            /* Navigate to the next page if available. */
                            if (hasNext) {
                                currentPage = safePage + 1;
                                pageProperty.set(currentPage);
                            }
                        })
                        .text("Next \u203A")
        );

        return controls;
    }

    /**
     * Handles a sort column header click.
     *
     * <p>If the clicked column is already the active sort column, the direction is
     * toggled (ascending becomes descending and vice versa). If a different column
     * is clicked, it becomes the new sort column with ascending direction. In both
     * cases, the page resets to 0 to show the beginning of the newly sorted data.</p>
     *
     * <p>This method updates both the {@code @State} fields (for JUX re-rendering)
     * and the jux-reactive properties (for external observers) in a single operation.</p>
     *
     * @param columnKey the field key of the column that was clicked (e.g. "name", "email")
     */
    private void handleSort(String columnKey) {
        if (columnKey.equals(sortColumn)) {
            /*
             * Same column clicked: toggle the sort direction. This provides the
             * standard ascending/descending toggle interaction expected from
             * sortable table headers.
             */
            sortAsc = !sortAsc;
        } else {
            /*
             * Different column clicked: switch to that column with ascending
             * direction as the default starting sort direction.
             */
            sortColumn = columnKey;
            sortAsc = true;
        }

        /*
         * Reset to the first page after any sort change. Users expect to see
         * the top results after re-sorting, not remain on an arbitrary page
         * that may now contain different data.
         */
        currentPage = 0;

        /*
         * Keep the reactive properties in sync with the @State fields.
         * This enables external observers to react to sort changes independently.
         */
        sortColumnProperty.set(sortColumn);
        sortDirectionProperty.set(sortAsc ? "asc" : "desc");
        pageProperty.set(0);
    }

    /**
     * Sorts the given list of rows according to the current sort column and direction.
     *
     * <p>This method creates a comparator based on the {@link #sortColumn} field key
     * and applies it to the list. The comparator handles all column types: numeric
     * comparison for "id", boolean comparison for "active", and case-insensitive
     * string comparison for all text columns.</p>
     *
     * <p>The sort is performed on a copy of the data (passed as a mutable list),
     * preserving the original order in the observable list.</p>
     *
     * @param data a mutable list of rows to sort in place
     * @return the same list, sorted according to the current sort configuration
     */
    private List<TableRow> sortRows(List<TableRow> data) {
        /*
         * Build a comparator based on the current sort column. Each column
         * type may require a different comparison strategy (numeric, boolean,
         * or string).
         */
        Comparator<TableRow> comparator = switch (sortColumn) {
            case "id" ->
                    /* Numeric comparison for the ID column. */
                    Comparator.comparingInt(TableRow::id);

            case "name" ->
                    /* Case-insensitive string comparison for the name column. */
                    Comparator.comparing(TableRow::name, String.CASE_INSENSITIVE_ORDER);

            case "email" ->
                    /* Case-insensitive string comparison for the email column. */
                    Comparator.comparing(TableRow::email, String.CASE_INSENSITIVE_ORDER);

            case "role" ->
                    /* Case-insensitive string comparison for the role column. */
                    Comparator.comparing(TableRow::role, String.CASE_INSENSITIVE_ORDER);

            case "department" ->
                    /* Case-insensitive string comparison for the department column. */
                    Comparator.comparing(TableRow::department, String.CASE_INSENSITIVE_ORDER);

            case "active" ->
                    /* Boolean comparison for the status column.
                     * Active (true) sorts before inactive (false) in ascending order. */
                    Comparator.comparing(TableRow::active);

            default ->
                    /* Fallback: sort by name if an unknown column key is provided.
                     * This should not happen with our defined COLUMNS but provides
                     * a safe default. */
                    Comparator.comparing(TableRow::name, String.CASE_INSENSITIVE_ORDER);
        };

        /*
         * Reverse the comparator if the sort direction is descending.
         * This avoids duplicating the comparator logic for each direction.
         */
        if (!sortAsc) {
            comparator = comparator.reversed();
        }

        /* Sort the list in place and return it. */
        data.sort(comparator);
        return data;
    }

    /**
     * Immutable data record representing a single row in the data table.
     *
     * <p>This record models a typical entity as might be returned from a paginated
     * REST API endpoint such as {@code GET /api/users?page=0&size=5&sort=name,asc}.
     * It contains identity fields (id, name, email), organizational fields (role,
     * department), and a status flag (active).</p>
     *
     * <p>Records are used for their immutability, which ensures thread-safe access
     * during SSR and aligns with JUX's reactive rendering model where state changes
     * involve creating new instances rather than mutating existing ones.</p>
     *
     * @param id         unique numeric identifier for the row
     * @param name       the user's full display name (e.g. "Alice Johnson")
     * @param email      the user's email address (e.g. "alice@example.com")
     * @param role       the user's role within the organization (e.g. "Developer")
     * @param department the department the user belongs to (e.g. "Engineering")
     * @param active     whether the user account is currently active
     */
    public record TableRow(int id, String name, String email, String role,
                            String department, boolean active) {
    }

    /**
     * Immutable data record describing a table column.
     *
     * <p>Each column has a field key used for sort comparisons, a human-readable
     * display label for the header, and a flag indicating whether the column
     * supports interactive sorting.</p>
     *
     * @param key      the internal field key, matching a {@link TableRow} property
     *                 (e.g. "name", "email", "active")
     * @param label    the display text shown in the column header (e.g. "Name", "Email")
     * @param sortable whether clicking this column header triggers sort interactions
     */
    public record Column(String key, String label, boolean sortable) {
    }
}
