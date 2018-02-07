package serverless.function;

public interface Context {

    String getFunctionName();

    String getFunctionVersion();

    Logger getLogger();

    int getRemainingTimeInMillis();

    int getMemoryLimitInMB();

}
