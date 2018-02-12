package serverless.function;

public interface RequestHandler<I,O> {

    O handle(I input, Context context);
}
