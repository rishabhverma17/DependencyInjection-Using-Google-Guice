package logger;

import exception.LoggingException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class SlowLogger implements Logger {
    private final String identifier;
    private final OutputStream stream;
    private final List<String> buffer;

    @Inject
    public SlowLogger(@Named("console-output-stream") final OutputStream stream, @Named("logger.fast.identifier")  String identifier) {
        this.identifier = identifier;
        this.stream = stream;
        buffer = new ArrayList<>();
    }

    @Override
    public boolean write(final String data) {
        buffer.add(identifier + " " + data + "\n");
        return true;
    }

    @Override
    public CompletableFuture<Void> flushAsync() {
        CompletableFuture<Void> result = CompletableFuture.completedFuture(null);
        ExecutorService service = Executors.newSingleThreadExecutor();
        for (final String word : buffer) {
            result = result.thenAcceptAsync(__ -> {
                try {
                    Thread.sleep(1000);
                    stream.write(word.getBytes());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, service);
        }
        return result;
    }

    @Override
    public boolean close() {
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            throw new LoggingException(e);
        }
        return false;
    }
}
