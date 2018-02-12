package serverless.function;

import java.time.Instant;

public class StdOutLogger implements Logger {

    public static Logger getLogger(String loggerName) {
        return new StdOutLogger();
    }

    @Override
    public void debug(String message) {

        log(Level.DEBUG, message);
    }

    @Override
    public void info(String message) {
        log(Level.INFO, message);
    }

    @Override
    public void warn(String message) {
        log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable t) {
        warn(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void err(String message) {
        log(Level.ERROR, message);
    }

    @Override
    public void err(String message, Throwable t) {
        err(message);
        t.printStackTrace(System.out);
    }

    @Override
    public void log(Level level, String message) {

        Instant instant = Instant.now();
        System.out.printf("%s %s: %s%n", instant, level, message);
    }
}
