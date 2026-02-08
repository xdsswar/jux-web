/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 */

package xss.it.jux.clientdemo.client;

import xss.it.jux.client.ClientMain;
import xss.it.jux.clientdemo.components.AccordionWidget;
import xss.it.jux.clientdemo.components.ChartWidget;
import xss.it.jux.clientdemo.components.CounterWidget;
import xss.it.jux.clientdemo.components.DataTableWidget;
import xss.it.jux.clientdemo.components.FormSubmitWidget;
import xss.it.jux.clientdemo.components.LiveSearchWidget;
import xss.it.jux.clientdemo.components.ModalWidget;
import xss.it.jux.clientdemo.components.PollWidget;
import xss.it.jux.clientdemo.components.QuoteMachineWidget;
import xss.it.jux.clientdemo.components.StopwatchWidget;
import xss.it.jux.clientdemo.components.TabsWidget;
import xss.it.jux.clientdemo.components.ThemeToggleWidget;
import xss.it.jux.clientdemo.components.TodoWidget;
import xss.it.jux.clientdemo.components.UserBrowserWidget;

/**
 * TeaVM entry point for the client-side demo application.
 *
 * <p>This class is compiled to JavaScript by TeaVM via the Gradle plugin.
 * It registers all client-side component factories with the JUX runtime's
 * component registry, then starts the hydration process.</p>
 *
 * <p>Factories are registered as lambda references ({@code Widget::new})
 * instead of {@code Class} objects to avoid reflection-based instantiation,
 * which TeaVM does not reliably support.</p>
 *
 * @see ClientMain
 */
public final class DemoClientMain {

    private DemoClientMain() {
        // Entry point class; prevent instantiation.
    }

    /**
     * TeaVM compilation entry point.
     *
     * <p>Registers all demo components with the JUX client runtime, then
     * delegates to {@link ClientMain#main()} which discovers
     * {@code data-jux-id} elements in the DOM and hydrates them.</p>
     */
    public static void main(String[] args) {
        /* ── Interactive Components Page ──────────────────────────────── */
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.CounterWidget", CounterWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.TodoWidget", TodoWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.TabsWidget", TabsWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.AccordionWidget", AccordionWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.ModalWidget", ModalWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.LiveSearchWidget", LiveSearchWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.StopwatchWidget", StopwatchWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.ThemeToggleWidget", ThemeToggleWidget::new);

        /* ── API Demo Page ────────────────────────────────────────────── */
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.UserBrowserWidget", UserBrowserWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.QuoteMachineWidget", QuoteMachineWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.DataTableWidget", DataTableWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.FormSubmitWidget", FormSubmitWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.PollWidget", PollWidget::new);
        ClientMain.registerComponent(
                "xss.it.jux.clientdemo.components.ChartWidget", ChartWidget::new);

        /* Start the JUX client runtime: discover and hydrate components. */
        ClientMain.main();
    }
}
