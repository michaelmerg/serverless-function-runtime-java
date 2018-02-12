package serverless.function;

import org.junit.Ignore;
import org.junit.Test;


public class RuntimeTest {

    @Test
    @Ignore
    public void test() throws InterruptedException {

        ServerlessFunctionRuntime runtime = new ServerlessFunctionRuntime();
        runtime.start();

        Thread.sleep(30000);

        runtime.stop();
    }
}
