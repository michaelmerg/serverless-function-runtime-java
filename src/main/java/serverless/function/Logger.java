package serverless.function;

public interface Logger {

    void debug(String message);

    void info(String message);

    void warn(String message);

    void err(String message);

}
