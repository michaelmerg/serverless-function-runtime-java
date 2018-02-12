package serverless.function;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamingRequestHandler {

    void handle(InputStream input, OutputStream output, Context context);
}
