package com.aitusoftware.example.aeron.service;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CloseableExecutor implements AutoCloseable, Executor
{
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void close()
    {
        executorService.shutdownNow();
    }

    @Override
    public void execute(final Runnable command)
    {
        executorService.execute(command);
    }
}
