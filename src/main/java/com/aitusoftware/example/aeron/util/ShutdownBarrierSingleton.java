package com.aitusoftware.example.aeron.util;

import org.agrona.concurrent.ShutdownSignalBarrier;

public final class ShutdownBarrierSingleton
{
    private static final ShutdownSignalBarrier BARRIER = new ShutdownSignalBarrier();

    public static ShutdownSignalBarrier barrier()
    {
        return BARRIER;
    }
}
