package com.aitusoftware.example.aeron.util;

import org.agrona.MutableDirectBuffer;

public final class Parameters
{
    private MutableDirectBuffer buffer;
    private int offset;

    public void reset(final MutableDirectBuffer buffer)
    {
        this.buffer = buffer;
        this.offset = 0;
    }

    public void pushInt(final int value)
    {
        buffer.putInt(offset, value);
        offset += Integer.BYTES;
    }

    public void pushLong(final long value)
    {
        buffer.putLong(offset, value);
        offset += Long.BYTES;
    }


    public int length()
    {
        return offset;
    }
}
