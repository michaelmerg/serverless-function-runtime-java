package serverless.function;

public class ServerlessFunctionException extends RuntimeException {

    public ServerlessFunctionException(String message) {

        super(message);
    }

    public ServerlessFunctionException(String message, Throwable cause) {

        super(message, cause);
    }
}
