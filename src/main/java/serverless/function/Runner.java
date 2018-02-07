package serverless.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Runner {

    static ObjectMapper objectMapper = new ObjectMapper();
 
    public static void main(String[] args) throws Exception {



        String handlerName = "EchoFunc::execute";

        String handlerClassName = handlerName.substring(0, handlerName.lastIndexOf("::"));
        String handlerMethodName = handlerName.substring(handlerName.lastIndexOf("::") + 2);

        Object handler = Class.forName(handlerClassName).newInstance();
        Optional<Method> handlerMethodOptional = asList(handler.getClass().getDeclaredMethods())
                .stream()
                .filter(m -> (
                    m.getName().equals(handlerMethodName) &&
                            m.getParameterCount() == 2 &&
                            Context.class.equals(m.getParameterTypes()[1])
                ))
                .findFirst();

        Method handlerMethod = handlerMethodOptional.get();
        System.out.println("str = "+handlerMethod.getParameterTypes()[0]);

        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/", httpExchange -> {
            OutputStream responseBody = httpExchange.getResponseBody();
            try {
                Object input = convertInput(httpExchange.getRequestBody());
                String output = convertOutput(handlerMethod.invoke(handler, input, null));
                httpExchange.sendResponseHeaders(200, output.length());
                responseBody.write(output.getBytes());
            } catch (Exception e) {
                httpExchange.sendResponseHeaders(500, 0);
            } finally {
                responseBody.close();
            }
        });
        server.start();
    }

    private static String convertOutput(Object output) throws IOException {

        if (String.class.isInstance(output)) {
            return (String) output;
        }

        return objectMapper.writeValueAsString(output);
    }

    public static Object convertInput(InputStream input) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
