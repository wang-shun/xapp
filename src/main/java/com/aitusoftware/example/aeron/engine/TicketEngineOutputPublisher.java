package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Parameters;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_EVENT_CREATED_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_TICKET_ASSIGNED_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_TICKET_REQUEST_FAILED_ID;

public final class TicketEngineOutputPublisher implements TicketEngineOutput
{
    private static final int MAX_MESSAGE_LENGTH = 28;
    private final Publication publication;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH));
    private final Parameters parameters = new Parameters();

    public TicketEngineOutputPublisher(final Publication publication)
    {
        this.publication = publication;
    }

    @Override
    public void eventCreated(final long eventId)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_EVENT_CREATED_ID);
        parameters.pushLong(eventId);
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    @Override
    public void ticketAssigned(final long userId, final long eventId, final long ticketId)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_TICKET_ASSIGNED_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        parameters.pushLong(ticketId);
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    @Override
    public void ticketRequestFailed(final long userId, final long eventId, final FailureReason failureReason)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_TICKET_REQUEST_FAILED_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        parameters.pushInt(failureReason.ordinal());
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }
}