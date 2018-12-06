package com.aitusoftware.example.aeron.util;

import org.agrona.CloseHelper;

import java.util.Arrays;

public final class CompositeCloseable implements AutoCloseable
{
    private final AutoCloseable[] closeables;

    public CompositeCloseable(final AutoCloseable... closeables)
    {
        this.closeables = closeables;
    }

    @Override
    public void close()
    {
        Arrays.stream(closeables).forEach(CloseHelper::quietClose);
    }
}