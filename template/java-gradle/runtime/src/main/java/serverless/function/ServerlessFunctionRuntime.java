package serverless.function;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerlessFunctionRuntime {

    private static final String CONTEXT_PATH = "/";
    private static final int SERVER_STOP_TIMEOUT_SEC = 0;
    private static final int HTTP_THREAD_POOL_SIZE = 25;

    private static final Logger LOG = StdOutLogger.getLogger(null);

    private Config config;
    private HttpServer server;
    private ExecutorService httpThreadPool;

    private HandlerInvoker invoker;

    ServerlessFunctionRuntime(Config config) {
        this.config = config;
        initialize();
    }

    public static void main(String[] args) {
        Config config = ConfigLoader.loadConfig("./function.json");
        ServerlessFunctionRuntime runtime = new ServerlessFunctionRuntime(config);
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop));
        runtime.start();
    }

    private void initialize() {
        Object handler = makeHandlerInstance();
        this.invoker = makeHandlerInvoker(handler);
    }

    private Object makeHandlerInstance() {
        Class<?> handlerClass;
        try {
            handlerClass = Class.forName(this.config.getHandlerName());
        } catch (ClassNotFoundException e) {
            throw new ServerlessFunctionException(
                    "Could not find handler class '" + this.config.getHandlerName() + "'.", e);
        }

        try {
            return handlerClass.newInstance();
        } catch (InstantiationException e) {
            throw new ServerlessFunctionException("Unable to instantiate handler class.", e);
        } catch (IllegalAccessException e) {
            throw new ServerlessFunctionException("Handler class is inaccessible. Set modifier to 'public'.", e);
        }
    }

    private HandlerInvoker makeHandlerInvoker(Object handler) {
        if (handler instanceof RequestHandler) {
            return new RequestHandlerInvoker((RequestHandler) handler);
        } else if (handler instanceof StreamingRequestHandler) {
            return new StreamingRequestHandlerInvoker((StreamingRequestHandler) handler);
        } else {
            throw new ServerlessFunctionException("Handler must implement either " + RequestHandler.class.getName()
                    + " or " + StreamingRequestHandler.class.getName());
        }
    }

    public void start() {
        server = createHttpServer();
        httpThreadPool = Executors.newFixedThreadPool(HTTP_THREAD_POOL_SIZE);
        server.setExecutor(this.httpThreadPool);
        server.createContext(CONTEXT_PATH, this::processHttpRequest);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(SERVER_STOP_TIMEOUT_SEC);
            server = null;
            httpThreadPool.shutdownNow();
        }
    }

    private HttpServer createHttpServer() {
        try {
            return HttpServer.create(new InetSocketAddress(config.getRuntimePort()), 0);
        } catch (IOException e) {
            throw new ServerlessFunctionException("Error create http server for runtime.", e);
        }
    }

    private void processHttpRequest(HttpExchange httpExchange) {
        Context context = makeContext(httpExchange);
        try {
            this.invoker.invokeHandler(httpExchange, context);

        } catch (RequestProcessingException e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            sendErrorResponse(httpExchange, writer.toString());
        } catch (Exception t) {
            LOG.err("Unhandled error handling request.", t);
        } finally {
            closeResponse(httpExchange);
        }
    }

    private void closeResponse(HttpExchange httpExchange) {
        try {
            httpExchange.getResponseBody().close();
        } catch (IOException e) {
            LOG.err("Error closing response.", e);
        }
    }

    private Context makeContext(HttpExchange httpExchange) {
        String callId = httpExchange.getRequestHeaders().getFirst("x-call-id");
        return new Context(config.getName(), config.getVersion(), callId);
    }

    private void sendErrorResponse(HttpExchange httpExchange, String output) {

        try {
            httpExchange.sendResponseHeaders(500, output.length());

            OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(output.getBytes());
        } catch (IOException e) {
            LOG.err("Error sending error response.", e);
        }
    }
}
