import serverless.function.Context;

import java.util.Map;

public class Handler {

    public static class Input {
        public String name;
    }

    public static class Output {
        public int length;
    }

    public void handle(String input, Context context) {

//        Output output = new Output();
//        output.length = input.name.length();
//        return input;
    }
}
