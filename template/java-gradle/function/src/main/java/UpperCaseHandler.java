import serverless.function.Context;
import serverless.function.StreamingRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class UpperCaseHandler implements StreamingRequestHandler {

    @Override
    public void handle(InputStream input, OutputStream output, Context context) {

        InputStreamReader reader = new InputStreamReader(input);

        char[] buffer = new char[1];
        try {
            while (reader.read(buffer) != -1) {
                char upperChar = Character.toUpperCase(buffer[0]);
                output.write(upperChar);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
