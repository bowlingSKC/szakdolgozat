package balint.lenart.utils;

import javafx.application.Platform;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FXUtils {

    public static void runAndWait(Runnable run) throws ExecutionException, InterruptedException {
        FutureTask<Void> task = new FutureTask<>(run, null);
        Platform.runLater(task);
        task.get();
    }

}
