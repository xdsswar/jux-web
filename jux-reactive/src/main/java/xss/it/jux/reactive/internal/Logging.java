package xss.it.jux.reactive.internal;

/**
 * Internal logging utility for the JUX reactive module.
 *
 * <p>Uses {@code System.err} instead of {@code System.getLogger()} for
 * compatibility with TeaVM, whose classlib does not provide the
 * {@link java.lang.System.Logger} API.</p>
 */
public class Logging {

    private static final ErrorLogger LOGGER = new ErrorLogger();

    public static ErrorLogger getLogger() {
        return LOGGER;
    }

    public static class ErrorLogger {

        public void warning(String msg, Throwable t) {
            System.err.println("[WARNING] xss.it.jux.reactive: " + msg);
            if (t != null) {
                t.printStackTrace(System.err);
            }
        }

        public void warning(String msg) {
            System.err.println("[WARNING] xss.it.jux.reactive: " + msg);
        }

        public void finest(String msg, Object... params) {
            // Trace-level messages are suppressed by default.
        }
    }
}
