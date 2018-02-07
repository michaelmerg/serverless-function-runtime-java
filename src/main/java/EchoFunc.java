import serverless.function.Context;

public class EchoFunc {

    public double execute(String input, Context context) {
        return Double.parseDouble(input);
    }
}
