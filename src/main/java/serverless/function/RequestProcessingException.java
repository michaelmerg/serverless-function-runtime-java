package serverless.function;

public class RequestProcessingException extends ServerlessFunctionException {

    public RequestProcessingException(String message) {
        super(message);
    }

    public RequestProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
