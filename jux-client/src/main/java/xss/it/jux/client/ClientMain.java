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

package xss.it.jux.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.NodeList;

import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

/**
 * TeaVM entry point for the JUX client-side runtime.
 *
 * <p>This class is the {@code main()} target specified in the TeaVM Gradle
 * configuration ({@code teavm.js.mainClass}). When the compiled JavaScript
 * bundle ({@code jux-client.js}) loads in the browser, execution begins here.</p>
 *
 * <h2>Responsibilities</h2>
 * <ol>
 *   <li><b>Discovery:</b> Scans the live DOM for elements bearing the
 *       {@code data-jux-id} attribute. Each such element represents a
 *       server-rendered component that needs client-side hydration.</li>
 *   <li><b>Resolution:</b> Maps the {@code data-jux-class} attribute value
 *       to a concrete {@link Component} subclass using a pre-registered
 *       class lookup table.</li>
 *   <li><b>Hydration:</b> Instantiates the component, invokes
 *       {@link Component#render()} to produce the virtual {@link Element}
 *       tree, then hydrates the existing DOM node (attaching event handlers
 *       and wiring up reactive state) via {@link JuxDomBridge#hydrate}.</li>
 *   <li><b>Lifecycle:</b> After hydration, invokes any {@link OnMount}
 *       methods on the component. Registers {@link OnUnmount} methods
 *       for cleanup when the component is removed.</li>
 * </ol>
 *
 * <h2>Data Attributes Contract</h2>
 * <p>The server-side renderer ({@code JuxRenderer} in jux-server) emits the
 * following attributes on the root element of every client-side component:</p>
 * <ul>
 *   <li>{@code data-jux-id} &mdash; a unique identifier for the component
 *       instance within the page (e.g. {@code "counter-1"}).</li>
 *   <li>{@code data-jux-class} &mdash; the fully-qualified Java class name
 *       of the component (e.g. {@code "com.example.widgets.Counter"}).</li>
 * </ul>
 *
 * <h2>TeaVM Considerations</h2>
 * <p>Because TeaVM compiles Java bytecode to JavaScript ahead-of-time,
 * dynamic class loading (e.g. {@code Class.forName()}) is <b>not</b>
 * available at runtime. Instead, component classes must be registered
 * in the {@link #COMPONENT_REGISTRY} map at compile time. The TeaVM
 * dead-code eliminator will include only the classes that are reachable
 * from this registry.</p>
 *
 * @see JuxDomBridge
 * @see StateManager
 * @see EventBinder
 */
public final class ClientMain {

    /**
     * Compile-time registry mapping fully-qualified class names to
     * component factory functions.
     *
     * <p>Each consumer application registers its client-side components
     * as {@code Supplier<Component>} factories. This avoids reflection
     * for instantiation, which TeaVM does not reliably support.</p>
     *
     * <p>The key is the fully-qualified class name (matching the
     * {@code data-jux-class} attribute emitted during SSR), and the
     * value is a factory that creates new instances.</p>
     */
    private static final Map<String, Supplier<Component>> COMPONENT_REGISTRY =
            new HashMap<>();

    /**
     * Map of active component instances keyed by their {@code data-jux-id}.
     *
     * <p>Used to track which components have been hydrated so that we can
     * invoke {@link OnUnmount} methods during teardown and prevent
     * double-hydration if the script runs more than once.</p>
     */
    private static final Map<String, Object> ACTIVE_COMPONENTS = new HashMap<>();

    /** The shared DOM bridge used for all element creation and patching. */
    private static final JuxDomBridge DOM_BRIDGE = new JuxDomBridge();

    /** The shared state manager that tracks reactive {@code @State} fields. */
    private static final StateManager STATE_MANAGER = new StateManager();

    /** The shared event binder for wiring DOM events. */
    private static final EventBinder EVENT_BINDER = new EventBinder();

    /**
     * Private constructor &mdash; this class is not meant to be instantiated.
     * It serves purely as the TeaVM entry point with static methods.
     */
    private ClientMain() {
        // Utility class; prevent instantiation.
    }

    /**
     * TeaVM compilation entry point.
     *
     * <p>This method is called automatically when the compiled JavaScript
     * bundle loads in the browser. It waits for the DOM to be ready (the
     * script tag is placed at {@code BODY_END} by the resource pipeline,
     * so the DOM is typically already parsed), then initiates the hydration
     * process.</p>
     *
     * <p><b>Execution flow:</b></p>
     * <ol>
     *   <li>Obtain the {@link Window} and {@link HTMLDocument} via TeaVM JSO.</li>
     *   <li>Call {@link #discoverAndHydrate(HTMLDocument)} to find and
     *       hydrate all server-rendered client-side components.</li>
     * </ol>
     *
     */
    public static void main() {
        /*
         * Obtain the browser window and document through TeaVM's JSO bridge.
         * These are thin Java wrappers around the native browser objects.
         */
        Window window = Window.current();
        HTMLDocument document = window.getDocument();

        /*
         * The jux-client.js script is injected at BODY_END by the JUX resource
         * pipeline, so the DOM is fully parsed by the time this executes.
         * However, to be defensive, we verify the document ready state.
         */
        String readyState = getDocumentReadyState();

        if ("loading".equals(readyState)) {
            /*
             * If somehow the script runs before DOM parsing completes
             * (e.g. if a consumer injects it in <head> without defer),
             * wait for DOMContentLoaded before hydrating.
             */
            document.addEventListener("DOMContentLoaded",
                    evt -> discoverAndHydrate(document));
        } else {
            /*
             * DOM is already parsed ("interactive" or "complete").
             * Hydrate immediately.
             */
            discoverAndHydrate(document);
        }
    }

    /**
     * Retrieve the current {@code document.readyState} via a JSBody bridge.
     *
     * <p>TeaVM's {@code HTMLDocument} does not expose {@code readyState}
     * directly, so we use a {@code @JSBody} annotation to inline the
     * property access into the generated JavaScript.</p>
     *
     * @return one of {@code "loading"}, {@code "interactive"}, or {@code "complete"}
     */
    @JSBody(params = {}, script = "return document.readyState;")
    private static native String getDocumentReadyState();

    /**
     * Register a component factory in the compile-time registry.
     *
     * <p>Called by each consumer application's TeaVM entry point.
     * Each client-side component must be registered here so that the
     * hydration process can instantiate it when it encounters a matching
     * {@code data-jux-class} attribute in the DOM.</p>
     *
     * <p>Using a {@code Supplier} factory avoids reflection-based
     * instantiation, which TeaVM does not reliably support.</p>
     *
     * @param className the fully-qualified class name, must match the
     *                  {@code data-jux-class} attribute value in the SSR HTML
     * @param factory   a supplier that creates new component instances
     * @throws NullPointerException if either argument is null
     */
    public static void registerComponent(String className,
                                          Supplier<Component> factory) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        COMPONENT_REGISTRY.put(className, factory);
    }

    /**
     * Discover all server-rendered client-side components in the DOM and
     * hydrate each one.
     *
     * <p>This method performs a querySelectorAll for every element with
     * a {@code data-jux-id} attribute. For each match it:</p>
     * <ol>
     *   <li>Reads the {@code data-jux-id} to get the instance identifier.</li>
     *   <li>Reads the {@code data-jux-class} to resolve the component class.</li>
     *   <li>Skips the element if it has already been hydrated (present in
     *       {@link #ACTIVE_COMPONENTS}).</li>
     *   <li>Instantiates the component via its no-arg constructor.</li>
     *   <li>Calls {@link Component#render()} to obtain the virtual tree.</li>
     *   <li>Uses {@link JuxDomBridge#hydrate(Element, HTMLElement)} to wire
     *       event handlers onto the existing server-rendered DOM.</li>
     *   <li>Uses {@link EventBinder#bindAnnotatedHandlers(Object, HTMLElement)}
     *       to bind {@code @On}-annotated methods.</li>
     *   <li>Registers the component with {@link StateManager} for reactive
     *       {@code @State} field tracking.</li>
     *   <li>Invokes any {@link OnMount}-annotated methods on the component.</li>
     *   <li>Stores the component in {@link #ACTIVE_COMPONENTS} for lifecycle
     *       management.</li>
     * </ol>
     *
     * @param document the browser document to scan for hydratable components
     */
    private static void discoverAndHydrate(HTMLDocument document) {
        /*
         * querySelectorAll returns all elements in the document that have
         * the data-jux-id attribute. These were emitted by JuxRenderer
         * during SSR for every @JuxComponent(clientSide = true).
         */
        @SuppressWarnings("unchecked")
        NodeList<HTMLElement> juxElements =
                (NodeList<HTMLElement>) (NodeList<?>) document.querySelectorAll("[data-jux-id]");

        int count = juxElements.getLength();

        for (int i = 0; i < count; i++) {
            HTMLElement domElement = (HTMLElement) juxElements.item(i);

            /* Read the component instance ID and class name from data attributes. */
            String juxId = domElement.getAttribute("data-jux-id");
            String juxClassName = domElement.getAttribute("data-jux-class");

            /* Skip if already hydrated (defensive against double-execution). */
            if (ACTIVE_COMPONENTS.containsKey(juxId)) {
                continue;
            }

            /* Skip if the class name attribute is missing or empty. */
            if (juxClassName == null || juxClassName.isEmpty()) {
                logWarning("Element with data-jux-id='" + juxId
                        + "' is missing data-jux-class attribute; skipping hydration.");
                continue;
            }

            /* Resolve the component factory from the registry. */
            Supplier<Component> factory = COMPONENT_REGISTRY.get(juxClassName);

            if (factory == null) {
                /*
                 * The class was not registered. This can happen if the
                 * consumer's TeaVM entry point did not include it, or if
                 * the component was added after the last client build.
                 */
                logWarning("No registered component factory for '"
                        + juxClassName + "'; skipping hydration of '"
                        + juxId + "'.");
                continue;
            }

            /* Hydrate the individual component. */
            hydrateComponent(juxId, juxClassName, factory, domElement);
        }
    }

    /**
     * Hydrate a single component: instantiate, render, bind events, wire
     * state, and invoke the {@link OnMount} lifecycle hook.
     *
     * @param juxId     the unique component instance ID from {@code data-jux-id}
     * @param className the fully-qualified class name (for error messages)
     * @param factory   the factory that creates new component instances
     * @param domElement the existing server-rendered DOM element to hydrate
     */
    private static void hydrateComponent(String juxId,
                                          String className,
                                          Supplier<Component> factory,
                                          HTMLElement domElement) {
        try {
            /*
             * Instantiate the component using the registered factory.
             *
             * NOTE: On the client side, Spring DI is not available. Components
             * that require @Autowired dependencies should have their client-side
             * behavior limited to UI interactions (event handling, state changes)
             * and fetch data via HTTP APIs.
             */
            Component component = factory.get();

            /*
             * Produce the virtual Element tree by calling render().
             * This tree describes the expected DOM structure and carries
             * the event handlers that need to be bound.
             */
            Element virtualTree = component.render();

            /*
             * Hydrate: walk the virtual tree alongside the real DOM tree,
             * attaching event handlers from the virtual tree to matching
             * real DOM elements. The DOM structure is NOT modified here;
             * it was already rendered correctly by the server.
             *
             * Set the active component on the bridge so that event handlers
             * attached during hydration will automatically notify the
             * StateManager after execution, enabling the reactive loop.
             */
            DOM_BRIDGE.setActiveComponent(component);
            DOM_BRIDGE.hydrate(virtualTree, domElement);
            DOM_BRIDGE.setActiveComponent(null);

            /*
             * Bind @On-annotated methods on the component class to their
             * target DOM elements. These are method-level event bindings
             * that complement the element-level .on() handlers.
             */
            EVENT_BINDER.bindAnnotatedHandlers(component, domElement);

            /*
             * Register the component with the StateManager so that changes
             * to @State fields trigger automatic re-rendering and DOM patching.
             * The StateManager snapshots the initial @State values and will
             * diff against them on subsequent notifyStateChange() calls.
             */
            STATE_MANAGER.registerComponent(component, domElement, virtualTree);

            /*
             * Invoke the onMount() lifecycle method. This is where
             * developers initialize third-party libraries, set up WebSocket
             * connections, access Canvas contexts, etc.
             *
             * Direct method call — no reflection needed. Components override
             * onMount() in the Component base class.
             */
            component.onMount();

            /*
             * Record the component as active. This prevents double-hydration
             * and allows us to invoke @OnUnmount when the component is
             * removed from the DOM.
             */
            ACTIVE_COMPONENTS.put(juxId, component);

        } catch (Exception e) {
            /*
             * If hydration fails for one component, log the error but do
             * not prevent other components from hydrating. Resilience over
             * correctness here; a broken widget should not take down the
             * entire page's interactivity.
             */
            logError("Failed to hydrate component '" + juxId + "' ("
                    + className + "): " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Tear down a previously hydrated component by its instance ID.
     *
     * <p>Invokes any {@link OnUnmount} lifecycle methods on the component,
     * then removes it from the active components map. Called when a
     * component's DOM element is removed from the page (e.g. during
     * navigation or parent re-render).</p>
     *
     * @param juxId the component instance ID (from {@code data-jux-id})
     */
    public static void destroyComponent(String juxId) {
        Object component = ACTIVE_COMPONENTS.remove(juxId);

        if (component instanceof Component comp) {
            /* Invoke the onUnmount() lifecycle method for cleanup. */
            comp.onUnmount();
        }
    }

    /**
     * Returns the shared {@link StateManager} instance.
     *
     * <p>Exposed for use by generated proxy classes that need to notify
     * the state manager when an {@code @State} field is modified.</p>
     *
     * @return the singleton state manager
     */
    public static StateManager getStateManager() {
        return STATE_MANAGER;
    }

    /**
     * Returns the shared {@link JuxDomBridge} instance.
     *
     * <p>Exposed for use by components and internal framework code that
     * need to create or patch DOM elements.</p>
     *
     * @return the singleton DOM bridge
     */
    public static JuxDomBridge getDomBridge() {
        return DOM_BRIDGE;
    }

    /**
     * Returns the shared {@link EventBinder} instance.
     *
     * <p>Exposed for use by the state manager when re-binding events
     * after a re-render cycle.</p>
     *
     * @return the singleton event binder
     */
    public static EventBinder getEventBinder() {
        return EVENT_BINDER;
    }

    /**
     * Log a warning message to the browser console.
     *
     * <p>Uses {@link Window#alert(String)} as a fallback-safe approach.
     * In production, this would use {@code console.warn()} via a JSO
     * bridge. For the structural implementation, messages are written
     * to standard error which TeaVM maps to {@code console.error()}.</p>
     *
     * @param message the warning message to log
     */
    private static void logWarning(String message) {
        /*
         * TeaVM maps System.err.println to console.error in the browser.
         * A more refined implementation would use a JSO binding to
         * console.warn(), but this is sufficient for the structural impl.
         */
        System.err.println("[JUX WARN] " + message);
    }

    /**
     * Log an error message to the browser console.
     *
     * @param message the error message to log
     */
    private static void logError(String message) {
        System.err.println("[JUX ERROR] " + message);
    }
}
