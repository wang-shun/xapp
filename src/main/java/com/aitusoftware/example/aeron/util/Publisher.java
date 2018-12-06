package com.aitusoftware.example.aeron.util;

import org.agrona.DirectBuffer;

public interface Publisher
{
    long offer(final DirectBuffer buffer, final int offset, final int length);
}
