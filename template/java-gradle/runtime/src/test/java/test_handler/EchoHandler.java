package test_handler;

import serverless.function.Context;
import serverless.function.RequestHandler;

public class EchoHandler implements RequestHandler<String, String> {

    @Override
    public String handle(String input, Context context) {

        return input;
    }
}
