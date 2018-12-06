package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Parameters;
import com.aitusoftware.example.aeron.util.Publisher;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_EVENT_CREATED_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_TICKET_ASSIGNED_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_TICKET_REQUEST_FAILED_ID;
import static io.aeron.logbuffer.FrameDescriptor.MAX_MESSAGE_LENGTH;

public final class GenericEngineOutputPublisher
{
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH));
    private final Parameters parameters = new Parameters();

    void publishEventCreated(long eventId, Publisher publisher)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_EVENT_CREATED_ID);
        parameters.pushLong(eventId);
        long offerResult;
        do
        {
            offerResult = publisher.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    void publishTicketAssigned(long userId, long eventId, long ticketId, Publisher publisher)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_TICKET_ASSIGNED_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        parameters.pushLong(ticketId);
        long offerResult;
        do
        {
            offerResult = publisher.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    void publishTicketRequestFailed(long userId, long eventId, FailureReason failureReason, Publisher publisher)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_TICKET_REQUEST_FAILED_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        parameters.pushInt(failureReason.ordinal());
        long offerResult;
        do
        {
            offerResult = publisher.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

}
