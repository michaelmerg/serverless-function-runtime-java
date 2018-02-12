package serverless.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private final String functionName;
    private final String functionVersion;
    private final String callId;
    private int responseCode = 200;
    private final Map<String, List<String>> responseHeaders = new HashMap<>();
    private int responseLength = 0;

    protected Context(String functionName, String functionVersion, String callId) {
        this.functionName = functionName;
        this.functionVersion = functionVersion;
        this.callId = callId;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getFunctionVersion() {
        return this.functionVersion;
    }

    public Logger getLogger() {
        return StdOutLogger.getLogger(functionName + ":" + functionVersion + ":" + callId);
    }

    String getCallId() {
        return this.callId;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    int getResponseCode() {
        return responseCode;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public int getResponseLength() {
        return responseLength;
    }

    public void setResponseLength(int responseLength) {
        this.responseLength = responseLength;
    }

    // int getRemainingTimeInMillis();

    // int getMemoryLimitInMB();

}
