package com.aitusoftware.example.aeron.util;

import org.agrona.DirectBuffer;

public final class Message
{
    private DirectBuffer buffer;
    private int length;
    private int offset;
    private int internalOffset;

    public void reset(final DirectBuffer buffer, final int offset, final int length)
    {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
        this.internalOffset = 0;
    }

    public int getInt()
    {
        if (internalOffset >= length)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        final int index = offset + internalOffset;
        internalOffset += Integer.BYTES;
        return buffer.getInt(index);
    }

    public long getLong()
    {
        if (internalOffset >= length)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        final int index = offset + internalOffset;
        internalOffset += Long.BYTES;
        return buffer.getLong(index);
    }
}
