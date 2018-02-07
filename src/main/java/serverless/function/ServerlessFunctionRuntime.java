package serverless.function;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class ServerlessFunctionRuntime {

    private static final String CONTEXT_PATH = "/";
    private static final int SERVER_STOP_TIMEOUT_SEC = 60;
    private static final int HTTP_THREAD_POOL_SIZE = 25;
    private static final String HANDLER_METHOD_SEPARATOR = "::";
    private static final String DEFAULT_HANDLER_METHOD_NAME = "handle";

    private static final Gson GSON = new Gson();
    private static final Logger LOG = StdOutLogger.getLogger(null);

    private Config config;
    private String handlerClassName;
    private String handlerMethodName;
    private HttpServer server;

    private Object handler;
    private Method handlerMethod;
    private ExecutorService httpThreadPool;

    ServerlessFunctionRuntime() {

        loadConfig();
        initialize();
    }

    public static void main(String[] args) {
        ServerlessFunctionRuntime runtime = new ServerlessFunctionRuntime();
        Runtime.getRuntime().addShutdownHook(new Thread(runtime::stop));
        runtime.start();
    }

    private void loadConfig() {
        InputStream configFile = this.getClass().getClassLoader().getResourceAsStream("./function/function.json");
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile))) {
                this.config = GSON.fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            throw new ServerlessFunctionException("Error reading config.", e);
        }
    }

    private void initialize() {
        String handlerName = config.getHandlerName();

        int separatorIndex = handlerName.lastIndexOf(HANDLER_METHOD_SEPARATOR);
        if (separatorIndex != -1) {
            this.handlerClassName = handlerName.substring(0, separatorIndex);
            this.handlerMethodName = handlerName.substring(separatorIndex + HANDLER_METHOD_SEPARATOR.length());
        } else {
            this.handlerClassName = handlerName;
            this.handlerMethodName = DEFAULT_HANDLER_METHOD_NAME;
        }

        this.handler = makeHandlerInstance();
        this.handlerMethod = findHandlerMethod();
    }


    private Object makeHandlerInstance() {
        Class<?> handlerClass;
        try {
            handlerClass = Class.forName(handlerClassName);
        } catch (ClassNotFoundException e) {
            throw new ServerlessFunctionException("Could not find handler class '" + handlerClassName + "'.", e);
        }

        try {
            return handlerClass.newInstance();
        } catch (InstantiationException e) {
            throw new ServerlessFunctionException("Unable to instantiate handler class.", e);
        } catch (IllegalAccessException e) {
            throw new ServerlessFunctionException("Handler class is inaccessible. Set modifier to 'public'.", e);
        }
    }

    private Method findHandlerMethod() {

        Optional<Method> method = stream(handler.getClass().getDeclaredMethods())
                .filter(m -> (
                        m.getName().equals(handlerMethodName) &&
                                m.getParameterCount() == 2 &&
                                Context.class.equals(m.getParameterTypes()[1])
                ))
                .findFirst();

        if (method.isPresent()) {
            return method.get();
        } else {
            throw new ServerlessFunctionException(
                    "Could not find handler method '" + handlerMethodName + "(Object input, Context context)'.");
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
        InputStream requestBody = httpExchange.getRequestBody();
        Context context = makeContext(httpExchange);
        String output = "";
        int responseStatus = 200;
        try {
            Object inputObject = convertInput(requestBody);
            Object outputObject = invokeHandler(inputObject, context);
            output = convertOutput(outputObject);

            responseStatus = context.getResponseCode();
            httpExchange.getResponseHeaders().putAll(context.getResponseHeaders());

        } catch (RequestProcessingException e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            output = writer.toString();
            responseStatus = 500;
        } catch (Exception t) {
            LOG.err("Unhandled error handling request.", t);
        } finally {
            sendResponse(httpExchange, responseStatus, output);
        }
    }

    private Context makeContext(HttpExchange httpExchange) {
        String callId = httpExchange.getRequestHeaders().getFirst("x-call-id");
        return new Context(config.getName(), config.getVersion(), callId);
    }

    private Object convertInput(InputStream input) {

        if (input == null) {
            return null;
        }

        Class<?> inputClass = this.handlerMethod.getParameterTypes()[0];
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            if (String.class.equals(inputClass)) {
                return reader.lines().collect(Collectors.joining("\n"));
            } else {
                return GSON.fromJson(reader, inputClass);
            }
        } catch (IOException e) {
            throw new RequestProcessingException("Error closing input stream", e);
        } catch (Exception e) {
            throw new RequestProcessingException("Error converting input", e);
        }
    }

    private Object invokeHandler(Object input, Context context) {
        try {
            return handlerMethod.invoke(handler, input, context);
        } catch (IllegalAccessException e) {
            throw new RequestProcessingException("Handler method is inaccessible. Set modifier to 'public'.", e);
        } catch (InvocationTargetException e) {
            throw new RequestProcessingException("Unhandled exception thrown in handler.", e);
        }
    }

    private String convertOutput(Object output) {

        if (output == null) {
            return "";
        }

        if (String.class.isInstance(output)) {
            return (String) output;
        }

        return GSON.toJson(output);
    }

    private void sendResponse(HttpExchange httpExchange, int responseStatus, String output) {

        try {
            httpExchange.sendResponseHeaders(responseStatus, output.length());

            OutputStream responseBody = httpExchange.getResponseBody();
            responseBody.write(output.getBytes());
            responseBody.close();
        } catch (IOException e) {
            LOG.err("Error sending response.", e);
        }
    }
}
