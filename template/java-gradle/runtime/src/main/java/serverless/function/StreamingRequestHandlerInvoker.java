package serverless.function;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class StreamingRequestHandlerInvoker implements HandlerInvoker {

    private StreamingRequestHandler handler;

    public StreamingRequestHandlerInvoker(StreamingRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public void invokeHandler(HttpExchange httpExchange, Context context) {

        OutputStream outputStreamWrapper = new OutputStream() {

            boolean sendResponseHeaders = true;
            OutputStream delegate = httpExchange.getResponseBody();

            @Override
            public void write(int b) throws IOException {
                if (sendResponseHeaders) {
                    sendResponseHeaders(httpExchange, context);
                    sendResponseHeaders = false;
                }
                this.delegate.write(b);
            }

            @Override
            public void close() throws IOException {
                this.delegate.close();
            }

            @Override
            public void flush() throws IOException {
                this.delegate.flush();
            }
        };

        this.handler.handle(httpExchange.getRequestBody(), outputStreamWrapper, context);
    }


    private void sendResponseHeaders(HttpExchange httpExchange, Context context) {
        int responseStatus = context.getResponseCode();
        httpExchange.getResponseHeaders().putAll(context.getResponseHeaders());
        try {
            httpExchange.sendResponseHeaders(responseStatus, context.getResponseLength());
        } catch (IOException e) {
            throw new RequestProcessingException("Error writing response headers", e);
        }
    }

}
