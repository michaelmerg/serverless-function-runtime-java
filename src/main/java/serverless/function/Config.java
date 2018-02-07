package serverless.function;

public class Config {

    public static final int DEFAULT_RUNTIME_PORT = 8080;

    private String version;
    private String handlerName;
    private int runtimePort = DEFAULT_RUNTIME_PORT;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public int getRuntimePort() {
        return runtimePort;
    }

    public void setRuntimePort(int runtimePort) {
        this.runtimePort = runtimePort;
    }
}
