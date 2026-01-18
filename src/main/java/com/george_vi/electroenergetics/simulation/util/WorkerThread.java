package com.george_vi.electroenergetics.simulation.util;

import java.util.concurrent.*;

public class WorkerThread {

    private final ExecutorService executor;

    public WorkerThread(String name) {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName(name);
            t.setDaemon(true);
            return t;
        });
    }


    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
