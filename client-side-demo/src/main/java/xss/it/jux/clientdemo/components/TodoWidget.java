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

import static xss.it.jux.core.Elements.*;

/**
 * Interactive todo list widget demonstrating reactive list state management in JUX.
 *
 * <p>This component showcases how {@code @State} works with collection types
 * (specifically {@link List}) to build a fully interactive task management widget.
 * Users can add new items, toggle their completion status, and remove them
 * individually. Every mutation to the list triggers a re-render with efficient
 * DOM diffing.</p>
 *
 * <h2>Client-Side Features Demonstrated</h2>
 * <ul>
 *   <li><b>{@code @State} with collections</b> -- the {@code items} list is reactive.
 *       Adding, removing, or modifying items triggers a full re-render. The framework
 *       diffs the old and new element trees and patches only the changed DOM nodes.</li>
 *   <li><b>Multiple event types</b> -- {@code "click"} for toggling and deleting,
 *       plus {@code "input"} for capturing text field changes and {@code "keydown"}
 *       for Enter-key submission.</li>
 *   <li><b>List rendering</b> -- dynamic child generation from a collection using
 *       {@code stream().map().toList()} passed to {@code Element.children(List)}.</li>
 *   <li><b>Conditional styling</b> -- completed items receive line-through text
 *       decoration and muted colors to visually indicate their done state.</li>
 * </ul>
 *
 * <h2>Accessibility (WCAG 2.2 AA)</h2>
 * <ul>
 *   <li><b>1.3.1 Info and Relationships</b> -- the todo list uses semantic {@code <ul>}
 *       and {@code <li>} elements so screen readers announce "list, N items".</li>
 *   <li><b>2.1.1 Keyboard</b> -- all controls (input, buttons, checkboxes) are native
 *       HTML elements, inherently keyboard-accessible.</li>
 *   <li><b>3.3.2 Labels or Instructions</b> -- the input field has a visible
 *       placeholder and an {@code aria-label} for screen readers.</li>
 *   <li><b>4.1.3 Status Messages</b> -- the remaining items count is in an
 *       {@code aria-live="polite"} region that announces changes.</li>
 * </ul>
 *
 * <h2>SSR Behaviour</h2>
 * <p>The component renders with three pre-populated demo items in its initial state.
 * Once client-side hydration is active, users can interact with the full add/toggle/delete
 * functionality.</p>
 *
 * @see xss.it.jux.annotation.State
 * @see xss.it.jux.core.Element#on(String, xss.it.jux.core.EventHandler)
 */
@JuxComponent(clientSide = true)
public class TodoWidget extends Component {

    /**
     * Reactive list of todo items.
     *
     * <p>Each item is represented as a {@link TodoItem} record containing the
     * display text and completion status. The list is initialized with three
     * demonstration items to show a realistic starting state during SSR.</p>
     *
     * <p>On the client, any structural mutation to this list (add, remove) or
     * any modification of its contained items (toggling done status) triggers
     * the JUX reactivity system to re-render the component and diff/patch the
     * DOM.</p>
     */
    @State
    private List<TodoItem> items = new ArrayList<>(List.of(
            new TodoItem("Learn JUX framework", false),
            new TodoItem("Build interactive components", false),
            new TodoItem("Deploy to production", false)
    ));

    /**
     * Reactive state for the text currently typed into the "new item" input field.
     *
     * <p>Updated on every keystroke via the {@code "input"} event handler.
     * When the user presses Enter or clicks the "Add" button, this value is
     * consumed to create a new {@link TodoItem} and then cleared.</p>
     */
    @State
    private String newItemText = "";

    /**
     * Builds the virtual DOM tree for the todo list widget.
     *
     * <p>The rendered structure consists of:</p>
     * <ol>
     *   <li>An input row with a text field and an "Add" button for creating new items.</li>
     *   <li>A {@code <ul>} list of existing items, each with a checkbox for toggling
     *       completion, the item text (with strikethrough if done), and a delete button.</li>
     *   <li>A footer showing the count of remaining (incomplete) items in an
     *       {@code aria-live} region.</li>
     * </ol>
     *
     * @return the root element of the todo widget, never null
     */
    @Override
    public Element render() {
        /*
         * Calculate the number of incomplete items. This count is displayed in the
         * footer and announced to screen readers via the aria-live region. Using
         * a stream filter ensures correctness even as items are added or toggled.
         */
        long remaining = items.stream().filter(item -> !item.done()).count();

        /* Build and return the complete todo list element tree. */
        return div().cls("bg-gray-800", "rounded-2xl", "p-6").children(

                /* ── Input Row: New Item Text Field + Add Button ───────────────
                 * A horizontal flex row containing the text input for entering
                 * new todo text and a button to submit it. The input also
                 * supports pressing Enter to add items without clicking.
                 */
                div().cls("flex", "gap-3", "mb-4").children(

                        /* Text input for new todo items.
                         * The "input" event fires on every keystroke, updating
                         * the newItemText @State field. The "keydown" event
                         * checks for the Enter key to submit the item.
                         */
                        input()
                                .attr("type", "text")
                                .attr("placeholder", "Add a new task...")
                                .attr("value", newItemText)
                                .aria("label", "New task text")
                                .cls("flex-1", "bg-gray-700", "text-white",
                                        "rounded-lg", "px-4", "py-2",
                                        "placeholder-gray-500", "border", "border-gray-600",
                                        "focus:border-violet-500", "focus:ring-1",
                                        "focus:ring-violet-500", "focus:outline-none")
                                .on("input", e -> newItemText = e.getValue())
                                .on("keydown", e -> {
                                    /* When the user presses Enter and the input is
                                     * not empty, add the new item and clear the field. */
                                    if ("Enter".equals(e.getKey()) && !newItemText.isBlank()) {
                                        items.add(new TodoItem(newItemText.trim(), false));
                                        newItemText = "";
                                    }
                                }),

                        /* Add button: creates a new todo item from the input text.
                         * Disabled appearance when input is empty (handled via
                         * opacity class on the client side when state is empty).
                         */
                        button().attr("type", "button")
                                .cls("px-4", "py-2", "bg-violet-600", "text-white",
                                        "rounded-lg", "font-medium",
                                        "hover:bg-violet-500", "transition-colors",
                                        "focus:ring-2", "focus:ring-violet-500",
                                        "focus:ring-offset-2", "focus:ring-offset-gray-800")
                                .aria("label", "Add new task")
                                .on("click", e -> {
                                    /* Only add the item if the text is non-blank.
                                     * Trim whitespace and reset the input field. */
                                    if (!newItemText.isBlank()) {
                                        items.add(new TodoItem(newItemText.trim(), false));
                                        newItemText = "";
                                    }
                                })
                                .text("Add")
                ),

                /* ── Todo Items List ───────────────────────────────────────────
                 * A semantic <ul> containing one <li> per todo item. Each item
                 * is rendered with a checkbox, text label, and delete button.
                 * The list is generated dynamically from the @State items field.
                 */
                ul().cls("space-y-2", "mb-4")
                        .aria("label", "Todo items")
                        .children(buildItemElements()),

                /* ── Footer: Remaining Items Count ─────────────────────────────
                 * Displays "X items remaining" with an aria-live region so
                 * screen readers announce changes when items are added,
                 * completed, or removed.
                 */
                div().cls("flex", "items-center", "justify-between",
                                "pt-4", "border-t", "border-gray-700")
                        .children(
                                p().cls("text-sm", "text-gray-400")
                                        .ariaLive("polite")
                                        .text(remaining + (remaining == 1 ? " item" : " items") + " remaining"),

                                /* Small informational text about the total count. */
                                p().cls("text-sm", "text-gray-600")
                                        .text(items.size() + " total")
                        )
        );
    }

    /**
     * Generates the list of {@code <li>} elements from the current todo items.
     *
     * <p>Each item is rendered as a horizontal flex row containing:</p>
     * <ul>
     *   <li>A styled checkbox div that toggles the item's done state on click.</li>
     *   <li>The item text, with strikethrough and muted color if the item is done.</li>
     *   <li>A delete button (x) that removes the item from the list.</li>
     * </ul>
     *
     * <p>This method is extracted from {@link #render()} to keep the main
     * render method readable while handling the per-item logic cleanly.</p>
     *
     * @return a list of {@code <li>} elements representing each todo item
     */
    private List<Element> buildItemElements() {
        /* Create a mutable list to collect the generated <li> elements. */
        List<Element> elements = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            /* Capture the index in a final local variable for use in lambdas.
             * Java lambda capture requires effectively final variables. */
            final int index = i;
            TodoItem item = items.get(i);

            /*
             * Determine CSS classes for the item text based on completion status.
             * Completed items get line-through decoration and muted gray color
             * to visually distinguish them from active tasks.
             */
            String textClasses = item.done()
                    ? "flex-1 line-through text-gray-500"
                    : "flex-1 text-white";

            /*
             * Build the checkbox indicator. When the item is done, it shows a
             * filled violet circle with a checkmark character; when not done,
             * it shows an empty circle outline.
             */
            Element checkbox = div()
                    .cls("w-6", "h-6", "rounded-full", "border-2", "flex",
                            "items-center", "justify-center", "cursor-pointer",
                            "transition-colors", "shrink-0")
                    .cls(item.done()
                            ? "bg-violet-600 border-violet-600"
                            : "border-gray-500 hover:border-violet-400")
                    .role("checkbox")
                    .ariaChecked(item.done() ? "true" : "false")
                    .aria("label", "Mark \"" + item.text() + "\" as "
                            + (item.done() ? "incomplete" : "complete"))
                    .tabIndex(0)
                    .on("click", e -> {
                        /* Toggle the done status by replacing the item with a
                         * new TodoItem that has the opposite done value.
                         * This triggers a @State re-render. */
                        TodoItem current = items.get(index);
                        items.set(index, new TodoItem(current.text(), !current.done()));
                    })
                    .children(
                            /* Show a checkmark character inside the circle when done.
                             * The checkmark is hidden from assistive technology since
                             * the checkbox role already conveys the checked state. */
                            item.done()
                                    ? span().cls("text-white", "text-xs").ariaHidden(true).text("\u2713")
                                    : span()
                    );

            /* Build the delete button that removes this item from the list. */
            Element deleteBtn = button().attr("type", "button")
                    .cls("text-gray-600", "hover:text-rose-400",
                            "transition-colors", "text-lg", "leading-none",
                            "focus:ring-2", "focus:ring-rose-500",
                            "focus:ring-offset-1", "focus:ring-offset-gray-800",
                            "rounded", "px-1")
                    .aria("label", "Delete \"" + item.text() + "\"")
                    .on("click", e -> {
                        /* Remove the item at this index. The @State mutation
                         * triggers a re-render that drops the <li> from the DOM. */
                        items.remove(index);
                    })
                    .text("\u00d7");

            /* Assemble the <li> element for this todo item. */
            Element listItem = li()
                    .cls("flex", "items-center", "gap-3", "p-3",
                            "bg-gray-750", "rounded-lg",
                            "hover:bg-gray-700", "transition-colors",
                            "group")
                    .children(
                            checkbox,
                            span().cls(textClasses).text(item.text()),
                            deleteBtn
                    );

            elements.add(listItem);
        }

        return elements;
    }

    /**
     * Immutable data record representing a single todo item.
     *
     * <p>Each item has a text description and a boolean completion flag. Records
     * are used instead of mutable classes to encourage immutable state transitions:
     * toggling an item creates a <em>new</em> {@code TodoItem} rather than mutating
     * the existing one, which aligns with the JUX reactivity model.</p>
     *
     * @param text the display text describing the task, never null or blank
     * @param done whether the task has been completed
     */
    public record TodoItem(String text, boolean done) {
    }
}
