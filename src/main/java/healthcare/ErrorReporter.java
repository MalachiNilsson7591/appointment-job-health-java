package healthcare;

import java.io.IOException;

public interface ErrorReporter {
    void capture(String exceptionPayload) throws IOException, InterruptedException;
}
