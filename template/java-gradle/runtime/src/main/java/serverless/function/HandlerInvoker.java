package serverless.function;


import com.sun.net.httpserver.HttpExchange;

public interface HandlerInvoker {

    void invokeHandler(HttpExchange httpExchange, Context context);
}
