package serverless.function;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

public class RuntimeTest {

    @Test
    public void testSimpleEchoRequestHandler() throws Exception {

        Config config = new Config();
        config.setRuntimePort(8888);
        config.setHandlerName("test_handler.EchoHandler");
        ServerlessFunctionRuntime runtime = new ServerlessFunctionRuntime(config);
        runtime.start();

        Request request = Request.Post("http://localhost:8888").bodyString("Hello Echo!", ContentType.TEXT_PLAIN);
        String responseContent = request.execute().returnContent().asString();

        Assert.assertEquals("Hello Echo!", responseContent);
        runtime.stop();
    }
}
