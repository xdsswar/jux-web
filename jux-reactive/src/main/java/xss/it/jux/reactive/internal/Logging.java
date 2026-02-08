package xss.it.jux.reactive.internal;

/**
 * Internal logging utility for the JUX reactive module.
 */
public class Logging {

    private static final ErrorLogger LOGGER = new ErrorLogger();

    public static ErrorLogger getLogger() {
        return LOGGER;
    }

    public static class ErrorLogger {
        private final System.Logger logger = System.getLogger("xss.it.jux.reactive");

        public void warning(String msg, Throwable t) {
            logger.log(System.Logger.Level.WARNING, msg, t);
        }

        public void warning(String msg) {
            logger.log(System.Logger.Level.WARNING, msg);
        }

        public void finest(String msg, Object... params) {
            logger.log(System.Logger.Level.TRACE, msg, params);
        }
    }
}
