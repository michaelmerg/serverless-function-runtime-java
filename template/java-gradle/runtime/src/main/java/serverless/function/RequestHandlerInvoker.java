package serverless.function;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

public class RequestHandlerInvoker implements HandlerInvoker {

    private final static String HANDLER_METHOD_NAME = "handle";

    private static final Gson GSON = new Gson();
    private final Class<?> inputClass;

    private RequestHandler handler;

    RequestHandlerInvoker(RequestHandler handler) {
        this.handler = handler;
        this.inputClass = obtainInputClass(handler);
    }

    private Class<?> obtainInputClass(RequestHandler handler) {

        Optional<Method> method = stream(handler.getClass().getDeclaredMethods())
                .filter(m -> HANDLER_METHOD_NAME.equals(m.getName()))
                .findFirst();

        if (method.isPresent()) {
            return method.get().getParameterTypes()[0];
        } else {
            throw new ServerlessFunctionException("Handler method not found.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invokeHandler(HttpExchange httpExchange, Context context) {
        Object inputObject = convertInput(httpExchange.getRequestBody());
        Object outputOpject = this.handler.handle(inputObject, context);
        String output = convertOutput(outputOpject);

        int responseStatus = context.getResponseCode();
        httpExchange.getResponseHeaders().putAll(context.getResponseHeaders());
        try {
            httpExchange.sendResponseHeaders(responseStatus, output.length());
        } catch (IOException e) {
            throw new RequestProcessingException("Error writing response headers", e);
        }

        try {
            httpExchange.getResponseBody().write(output.getBytes());
        } catch (IOException e) {
            throw new RequestProcessingException("Error writing output", e);
        }
    }

    private Object convertInput(InputStream input) {

        if (input == null) {
            return null;
        }


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            if (String.class.equals(this.inputClass)) {
                return reader.lines().collect(Collectors.joining("\n"));
            } else {
                return GSON.fromJson(reader, this.inputClass);
            }
        } catch (IOException e) {
            throw new RequestProcessingException("Error closing input stream", e);
        } catch (Exception e) {
            throw new RequestProcessingException("Error converting input", e);
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
}
