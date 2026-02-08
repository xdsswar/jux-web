package xss.it.jux.reactive;

/**
 * Lifecycle callback interface for JUX components that require initialization.
 *
 * Classes implementing this interface are called after dependency injection
 * and property wiring is complete. This is the place to set up listeners,
 * bind properties, and perform initial data loading.
 */
public interface Initializable {

    /**
     * Called after the component has been fully constructed, injected,
     * and its properties have been wired.
     */
    void initialize();
}
