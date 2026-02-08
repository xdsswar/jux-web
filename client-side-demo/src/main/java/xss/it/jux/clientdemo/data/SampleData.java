/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Static in-memory data provider for the JUX Client-Side Demo application.
 *
 * <p>Contains hardcoded collections of users, inspirational quotes, notifications,
 * and dashboard statistics. No database is required — all data lives in static
 * collections initialized at class-load time. This mirrors the pattern used by
 * {@code StoreData} in the jux-store demo module.</p>
 *
 * <p>In a production application, this data would be fetched from JPA repositories
 * backed by a relational database. The JUX component code that consumes this data
 * would remain identical — only the data source changes from static lists to
 * repository method calls.</p>
 *
 * <h3>Data Collections:</h3>
 * <ul>
 *   <li><b>Users</b> — 20 team members across 5 departments with realistic profiles</li>
 *   <li><b>Quotes</b> — 15 inspirational and programming quotes across 4 categories</li>
 *   <li><b>Notifications</b> — 10 system notifications of varying types and read states</li>
 *   <li><b>Stats</b> — 6 dashboard statistics with trend indicators</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <p>All data is immutable after class initialization. The static collections are
 * populated in a {@code static} block and wrapped in unmodifiable lists via
 * {@link List#copyOf(java.util.Collection)}. Multiple request threads can read
 * concurrently without synchronization.</p>
 *
 * @see xss.it.jux.clientdemo.api.UsersApiController
 * @see xss.it.jux.clientdemo.api.QuotesApiController
 * @see xss.it.jux.clientdemo.api.NotificationsApiController
 * @see xss.it.jux.clientdemo.api.StatsApiController
 */
public final class SampleData {

    /** Private constructor prevents instantiation of this utility class. */
    private SampleData() {}

    /* ═══════════════════════════════════════════════════════════════════
     *  RECORDS — Immutable data carriers for each entity type.
     * ═══════════════════════════════════════════════════════════════════ */

    /**
     * Represents a team member / application user.
     *
     * <p>Each user has a unique integer ID, personal details, organizational
     * information, and an active/inactive status flag. The avatar field stores
     * initials derived from the first letter of the first and last name (e.g.
     * "JD" for John Doe), suitable for rendering in circular avatar badges.</p>
     *
     * @param id         unique numeric identifier for this user (1-based)
     * @param name       full display name (e.g. "Alice Johnson")
     * @param email      email address in the format firstname@example.com
     * @param role       job title or role within the organization (e.g. "Admin",
     *                   "Developer", "Designer", "Manager", "QA Engineer", "DevOps")
     * @param avatar     two-character initials derived from the user's name (e.g. "AJ")
     * @param department organizational department (e.g. "Engineering", "Design",
     *                   "Marketing", "Product", "Operations")
     * @param active     whether the user account is currently active; inactive users
     *                   may be displayed with a muted visual treatment in the UI
     */
    public record User(
            int id,
            String name,
            String email,
            String role,
            String avatar,
            String department,
            boolean active
    ) {}

    /**
     * Represents an inspirational or programming-related quote.
     *
     * <p>Quotes are categorized to allow filtering in the UI. Categories include
     * Motivation, Programming, Design, and Leadership. Each quote has a unique
     * ID for API retrieval and client-side keying.</p>
     *
     * @param id       unique numeric identifier for this quote (1-based)
     * @param text     the full quote text, typically one to three sentences
     * @param author   the person who said or wrote the quote
     * @param category classification for filtering: "Motivation", "Programming",
     *                 "Design", or "Leadership"
     */
    public record Quote(
            int id,
            String text,
            String author,
            String category
    ) {}

    /**
     * Represents a system notification displayed in the notification panel.
     *
     * <p>Notifications have a type that determines their visual treatment
     * (icon, color, urgency) and a read/unread status that drives badge
     * counts in the UI header.</p>
     *
     * @param id        unique numeric identifier for this notification (1-based)
     * @param title     short summary headline (e.g. "Deployment Complete")
     * @param message   detailed notification body text explaining the event
     * @param type      notification severity/category: "info", "warning", "success",
     *                  or "error" — used to select icon and color in the UI
     * @param timestamp human-readable relative time string (e.g. "2 minutes ago",
     *                  "1 hour ago", "3 days ago")
     * @param read      whether the user has already seen/acknowledged this notification;
     *                  unread notifications contribute to the badge count
     */
    public record Notification(
            int id,
            String title,
            String message,
            String type,
            String timestamp,
            boolean read
    ) {}

    /**
     * Represents a single dashboard statistic entry.
     *
     * <p>Each stat displays a metric label, its current numeric value, an
     * optional unit suffix, and a trend direction indicating whether the
     * metric is improving, declining, or stable compared to the previous
     * measurement period.</p>
     *
     * @param label the human-readable metric name (e.g. "Active Users", "Error Rate")
     * @param value the current numeric value of the metric (integer representation;
     *              for percentage values like "0.3%", the value is stored as the
     *              significant digits and the unit carries the format)
     * @param unit  the display unit or suffix appended after the value (e.g. "%",
     *              "ms", "users", "req/s"). Empty string if no unit is needed.
     * @param trend direction indicator: "up" (metric is increasing), "down" (metric
     *              is decreasing), or "stable" (no significant change). Used to
     *              render arrow icons and color coding in the dashboard UI.
     */
    public record StatEntry(
            String label,
            int value,
            String unit,
            String trend
    ) {}

    /* ═══════════════════════════════════════════════════════════════════
     *  STATIC DATA COLLECTIONS
     *
     *  All collections are populated once at class-load time in the
     *  static initializer block below. They are stored as unmodifiable
     *  lists to prevent accidental mutation by consuming code.
     * ═══════════════════════════════════════════════════════════════════ */

    /** All registered users, indexed by insertion order. Immutable after initialization. */
    private static final List<User> USERS;

    /** All available quotes, indexed by insertion order. Immutable after initialization. */
    private static final List<Quote> QUOTES;

    /** All system notifications, indexed by insertion order. Immutable after initialization. */
    private static final List<Notification> NOTIFICATIONS;

    /** All dashboard statistics, indexed by insertion order. Immutable after initialization. */
    private static final List<StatEntry> STATS;

    static {

        /* ── 20 Users ─────────────────────────────────────────────────
         * Realistic team members spanning 6 roles across 5 departments.
         * Avatars are two-letter initials: first letter of first name +
         * first letter of last name. Emails follow the pattern
         * firstname@example.com (all lowercase).
         */
        var users = new ArrayList<User>();
        users.add(new User(1,  "Alice Johnson",     "alice@example.com",     "Admin",        "AJ", "Engineering",  true));
        users.add(new User(2,  "Bob Martinez",       "bob@example.com",       "Developer",    "BM", "Engineering",  true));
        users.add(new User(3,  "Carol Chen",         "carol@example.com",     "Designer",     "CC", "Design",       true));
        users.add(new User(4,  "David Kim",          "david@example.com",     "Manager",      "DK", "Product",      true));
        users.add(new User(5,  "Emma Wilson",        "emma@example.com",      "Developer",    "EW", "Engineering",  true));
        users.add(new User(6,  "Frank Lopez",        "frank@example.com",     "QA Engineer",  "FL", "Engineering",  true));
        users.add(new User(7,  "Grace Patel",        "grace@example.com",     "Designer",     "GP", "Design",       true));
        users.add(new User(8,  "Henry Zhang",        "henry@example.com",     "DevOps",       "HZ", "Operations",   true));
        users.add(new User(9,  "Irene Davis",        "irene@example.com",     "Developer",    "ID", "Engineering",  true));
        users.add(new User(10, "Jack Thompson",      "jack@example.com",      "Manager",      "JT", "Marketing",    true));
        users.add(new User(11, "Karen White",         "karen@example.com",     "Developer",    "KW", "Engineering",  true));
        users.add(new User(12, "Leo Ramirez",        "leo@example.com",       "QA Engineer",  "LR", "Engineering",  false));
        users.add(new User(13, "Mia Nakamura",       "mia@example.com",       "Designer",     "MN", "Design",       true));
        users.add(new User(14, "Nathan Brooks",      "nathan@example.com",    "DevOps",       "NB", "Operations",   true));
        users.add(new User(15, "Olivia Scott",       "olivia@example.com",    "Manager",      "OS", "Product",      true));
        users.add(new User(16, "Peter Andersen",     "peter@example.com",     "Developer",    "PA", "Engineering",  true));
        users.add(new User(17, "Quinn Foster",       "quinn@example.com",     "Admin",        "QF", "Operations",   false));
        users.add(new User(18, "Rachel Green",       "rachel@example.com",    "Developer",    "RG", "Engineering",  true));
        users.add(new User(19, "Sam Rivera",         "sam@example.com",       "Designer",     "SR", "Design",       true));
        users.add(new User(20, "Tina Okafor",        "tina@example.com",      "Manager",      "TO", "Marketing",    true));
        USERS = List.copyOf(users);

        /* ── 15 Quotes ────────────────────────────────────────────────
         * A mix of motivational, programming, design, and leadership
         * quotes. Each has a unique ID, the full text, the author's
         * name, and a category string used for filtering.
         */
        var quotes = new ArrayList<Quote>();
        quotes.add(new Quote(1,
                "The only way to do great work is to love what you do.",
                "Steve Jobs", "Motivation"));
        quotes.add(new Quote(2,
                "Code is like humor. When you have to explain it, it's bad.",
                "Cory House", "Programming"));
        quotes.add(new Quote(3,
                "Design is not just what it looks like and feels like. Design is how it works.",
                "Steve Jobs", "Design"));
        quotes.add(new Quote(4,
                "The best way to predict the future is to invent it.",
                "Alan Kay", "Leadership"));
        quotes.add(new Quote(5,
                "First, solve the problem. Then, write the code.",
                "John Johnson", "Programming"));
        quotes.add(new Quote(6,
                "Simplicity is the ultimate sophistication.",
                "Leonardo da Vinci", "Design"));
        quotes.add(new Quote(7,
                "Any fool can write code that a computer can understand. Good programmers write code that humans can understand.",
                "Martin Fowler", "Programming"));
        quotes.add(new Quote(8,
                "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                "Winston Churchill", "Motivation"));
        quotes.add(new Quote(9,
                "A leader is one who knows the way, goes the way, and shows the way.",
                "John C. Maxwell", "Leadership"));
        quotes.add(new Quote(10,
                "Good design is obvious. Great design is transparent.",
                "Joe Sparano", "Design"));
        quotes.add(new Quote(11,
                "Talk is cheap. Show me the code.",
                "Linus Torvalds", "Programming"));
        quotes.add(new Quote(12,
                "It does not matter how slowly you go as long as you do not stop.",
                "Confucius", "Motivation"));
        quotes.add(new Quote(13,
                "Innovation distinguishes between a leader and a follower.",
                "Steve Jobs", "Leadership"));
        quotes.add(new Quote(14,
                "The function of good software is to make the complex appear to be simple.",
                "Grady Booch", "Programming"));
        quotes.add(new Quote(15,
                "Believe you can and you're halfway there.",
                "Theodore Roosevelt", "Motivation"));
        QUOTES = List.copyOf(quotes);

        /* ── 10 Notifications ─────────────────────────────────────────
         * System notifications with varying types (info, warning, success,
         * error) and read/unread states. Timestamps are human-readable
         * relative time strings for display purposes.
         */
        var notifications = new ArrayList<Notification>();
        notifications.add(new Notification(1,
                "Deployment Successful",
                "Version 2.4.1 has been deployed to production successfully.",
                "success", "2 minutes ago", false));
        notifications.add(new Notification(2,
                "New Team Member",
                "Rachel Green has joined the Engineering department.",
                "info", "15 minutes ago", false));
        notifications.add(new Notification(3,
                "High Memory Usage",
                "Server node-03 memory usage exceeded 85% threshold.",
                "warning", "32 minutes ago", false));
        notifications.add(new Notification(4,
                "Build Failed",
                "CI pipeline for feature/auth-refactor failed at test stage.",
                "error", "1 hour ago", false));
        notifications.add(new Notification(5,
                "SSL Certificate Renewal",
                "The SSL certificate for api.example.com has been renewed automatically.",
                "success", "2 hours ago", true));
        notifications.add(new Notification(6,
                "Scheduled Maintenance",
                "Database maintenance window scheduled for Saturday 02:00-04:00 UTC.",
                "info", "5 hours ago", true));
        notifications.add(new Notification(7,
                "API Rate Limit Warning",
                "Client app-mobile-ios is approaching the hourly rate limit (90% used).",
                "warning", "8 hours ago", true));
        notifications.add(new Notification(8,
                "Security Patch Applied",
                "Critical security patch CVE-2026-1234 has been applied to all nodes.",
                "success", "1 day ago", true));
        notifications.add(new Notification(9,
                "Disk Space Alert",
                "Storage volume /data is at 92% capacity. Consider archiving old logs.",
                "error", "2 days ago", true));
        notifications.add(new Notification(10,
                "Sprint Review Reminder",
                "Sprint 24 review meeting is scheduled for Friday at 3:00 PM.",
                "info", "3 days ago", true));
        NOTIFICATIONS = List.copyOf(notifications);

        /* ── 6 Dashboard Stats ────────────────────────────────────────
         * Key performance indicators for the demo dashboard. Each stat
         * has a label, numeric value, display unit, and a trend direction
         * ("up", "down", or "stable") indicating change from the previous
         * measurement period.
         *
         * Note: values are stored as integers. For fractional metrics
         * like "0.3%", the value is 3 and the unit is "% (x0.1)" — the
         * API consumer or UI component handles the decimal formatting.
         * For this demo, we store the display-ready integer and rely on
         * the unit string to convey the format (e.g. "%" for 99.97 is
         * stored as 9997 with unit "% (x0.01)"). However, for simplicity
         * in this demo, we use representative integer values and rely on
         * the UI to format them appropriately with their unit strings.
         */
        var stats = new ArrayList<StatEntry>();
        stats.add(new StatEntry("Active Users",    2847,  "users",  "up"));
        stats.add(new StatEntry("API Requests",    14523, "req/h",  "up"));
        stats.add(new StatEntry("Error Rate",      3,     "%",      "down"));
        stats.add(new StatEntry("Avg Response",    142,   "ms",     "stable"));
        stats.add(new StatEntry("Deployments",     23,    "today",  "up"));
        stats.add(new StatEntry("Uptime",          9997,  "%",      "stable"));
        STATS = List.copyOf(stats);
    }

    /* ═══════════════════════════════════════════════════════════════════
     *  PUBLIC API — User Operations
     * ═══════════════════════════════════════════════════════════════════ */

    /**
     * Returns all users in insertion order.
     *
     * <p>The returned list is an unmodifiable copy of the internal collection.
     * Callers may freely iterate, filter, or stream the result without
     * affecting the source data.</p>
     *
     * @return an unmodifiable list containing all 20 users
     */
    public static List<User> allUsers() {
        return USERS;
    }

    /**
     * Finds a user by their unique numeric identifier.
     *
     * <p>Performs a linear scan of the user list since the collection is small
     * (20 elements). For larger datasets, an indexed lookup (e.g. a
     * {@code Map<Integer, User>}) would be more efficient.</p>
     *
     * @param id the user ID to search for (1-based)
     * @return an {@link Optional} containing the matching user, or
     *         {@link Optional#empty()} if no user has the given ID
     */
    public static Optional<User> findUser(int id) {
        /*
         * Stream through all users, filter by exact ID match, and return
         * the first (and only) match wrapped in an Optional.
         */
        return USERS.stream()
                .filter(u -> u.id() == id)
                .findFirst();
    }

    /**
     * Returns all users belonging to the specified department.
     *
     * <p>The department comparison is case-insensitive to provide a forgiving
     * API experience. For example, passing "engineering", "Engineering", or
     * "ENGINEERING" all return the same set of users.</p>
     *
     * @param department the department name to filter by (e.g. "Engineering",
     *                   "Design", "Marketing", "Product", "Operations")
     * @return an unmodifiable list of users in the specified department;
     *         returns an empty list if no users match or if the department
     *         name is {@code null}
     */
    public static List<User> usersByDepartment(String department) {
        /*
         * Guard against null input to avoid NullPointerException in the
         * equalsIgnoreCase comparison. A null department matches no users.
         */
        if (department == null) {
            return List.of();
        }

        return USERS.stream()
                .filter(u -> u.department().equalsIgnoreCase(department))
                .toList();
    }

    /**
     * Searches users by a free-text query across name, email, role, and department.
     *
     * <p>The search is case-insensitive and matches any user where the query
     * string appears as a substring of the name, email, role, or department.
     * This provides a flexible search experience suitable for autocomplete
     * and filter-as-you-type interfaces.</p>
     *
     * <p>If the query is {@code null} or blank (empty or whitespace-only),
     * an empty list is returned rather than all users. This prevents
     * accidental full-catalog dumps from empty search submissions.</p>
     *
     * @param query the search term to match against user fields
     * @return an unmodifiable list of matching users; empty if query is
     *         blank or no users match
     */
    public static List<User> searchUsers(String query) {
        /*
         * Treat null and blank queries as "no search" and return an empty
         * result set. This is intentional — the /api/users endpoint returns
         * all users when no search param is provided; the search method is
         * specifically for keyword filtering.
         */
        if (query == null || query.isBlank()) {
            return List.of();
        }

        /* Normalize the query to lowercase for case-insensitive matching. */
        String lowerQuery = query.toLowerCase(Locale.ROOT).trim();

        /*
         * Check each user's name, email, role, and department for a
         * case-insensitive substring match against the query.
         */
        return USERS.stream()
                .filter(u -> u.name().toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || u.email().toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || u.role().toLowerCase(Locale.ROOT).contains(lowerQuery)
                        || u.department().toLowerCase(Locale.ROOT).contains(lowerQuery))
                .toList();
    }

    /* ═══════════════════════════════════════════════════════════════════
     *  PUBLIC API — Quote Operations
     * ═══════════════════════════════════════════════════════════════════ */

    /**
     * Returns all quotes in insertion order.
     *
     * <p>The returned list is an unmodifiable copy. Callers may iterate,
     * filter, or stream the result without affecting the source data.</p>
     *
     * @return an unmodifiable list containing all 15 quotes
     */
    public static List<Quote> allQuotes() {
        return QUOTES;
    }

    /**
     * Returns a randomly selected quote.
     *
     * <p>Uses {@link ThreadLocalRandom} for thread-safe random number
     * generation without contention. Each call returns a single quote
     * chosen uniformly at random from the full collection.</p>
     *
     * <p>This method is used by the {@code /api/quotes/random} endpoint
     * to provide a "quote of the moment" feature in the dashboard.</p>
     *
     * @return a randomly selected quote from the collection
     */
    public static Quote randomQuote() {
        /*
         * ThreadLocalRandom is preferred over Random for concurrent access
         * because each thread has its own generator, avoiding contention on
         * a shared seed. The nextInt(bound) call returns a value in [0, size).
         */
        int index = ThreadLocalRandom.current().nextInt(QUOTES.size());
        return QUOTES.get(index);
    }

    /**
     * Returns all quotes belonging to the specified category.
     *
     * <p>The category comparison is case-insensitive. Valid categories are
     * "Motivation", "Programming", "Design", and "Leadership".</p>
     *
     * @param category the category name to filter by
     * @return an unmodifiable list of quotes in the specified category;
     *         returns an empty list if no quotes match or if the category
     *         is {@code null}
     */
    public static List<Quote> quotesByCategory(String category) {
        /*
         * Guard against null to avoid NullPointerException in the
         * equalsIgnoreCase comparison downstream.
         */
        if (category == null) {
            return List.of();
        }

        return QUOTES.stream()
                .filter(q -> q.category().equalsIgnoreCase(category))
                .toList();
    }

    /* ═══════════════════════════════════════════════════════════════════
     *  PUBLIC API — Notification Operations
     * ═══════════════════════════════════════════════════════════════════ */

    /**
     * Returns all notifications in insertion order (most recent first).
     *
     * <p>The list includes both read and unread notifications. Callers
     * can use {@link Notification#read()} to separate them in the UI.</p>
     *
     * @return an unmodifiable list containing all 10 notifications
     */
    public static List<Notification> allNotifications() {
        return NOTIFICATIONS;
    }

    /**
     * Returns only the unread notifications.
     *
     * <p>This is a convenience method primarily used by the API to report
     * the unread count in the response payload. Unread notifications are
     * those where {@link Notification#read()} returns {@code false}.</p>
     *
     * @return an unmodifiable list of notifications that have not been read
     */
    public static List<Notification> unreadNotifications() {
        return NOTIFICATIONS.stream()
                .filter(n -> !n.read())
                .toList();
    }

    /* ═══════════════════════════════════════════════════════════════════
     *  PUBLIC API — Stats Operations
     * ═══════════════════════════════════════════════════════════════════ */

    /**
     * Returns all dashboard statistics.
     *
     * <p>The returned list contains 6 key performance indicators (KPIs)
     * suitable for rendering in a dashboard grid. Each entry includes the
     * metric name, current value, display unit, and trend direction.</p>
     *
     * @return an unmodifiable list containing all 6 stat entries
     */
    public static List<StatEntry> allStats() {
        return STATS;
    }
}
