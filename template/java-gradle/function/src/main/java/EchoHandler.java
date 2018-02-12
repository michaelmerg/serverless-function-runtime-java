import serverless.function.Context;
import serverless.function.RequestHandler;

import java.util.Map;

public class EchoHandler implements RequestHandler<String, String> {

    @Override
    public String handle(String input, Context context) {

        return input;
    }
}
