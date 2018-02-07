package serverless.function;

public interface Logger {

    public static enum Level {DEBUG, INFO, WARNING, ERROR;}

    void log(Level level, String message);

    void debug(String message);

    void info(String message);

    void warn(String message);

    void warn(String message, Throwable t);

    void err(String message);

    void err(String message, Throwable t);
}
