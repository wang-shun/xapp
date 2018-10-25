package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Parameters;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_BUY_TICKET_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_CREATE_EVENT_ID;
import static com.aitusoftware.example.aeron.util.MessageConstants.TOPIC_REGISTER_FOR_EVENT_ID;

public final class TicketEngineInputPublisher implements TicketEngineInput
{
    private static final int MAX_MESSAGE_LENGTH = 24;
    private final Publication publication;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH));
    private final Parameters parameters = new Parameters();

    public TicketEngineInputPublisher(final Publication publication)
    {
        this.publication = publication;
    }

    @Override
    public void createEvent(
            final int ticketCount,
            final long ticketSalesStart, final long ticketSalesEnd)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_CREATE_EVENT_ID);
        parameters.pushInt(ticketCount);
        parameters.pushLong(ticketSalesStart);
        parameters.pushLong(ticketSalesEnd);
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    @Override
    public void buyTicket(final long userId, final long eventId)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_BUY_TICKET_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }

    @Override
    public void registerForEvent(final long userId, final long eventId)
    {
        parameters.reset(buffer);
        parameters.pushInt(TOPIC_REGISTER_FOR_EVENT_ID);
        parameters.pushLong(userId);
        parameters.pushLong(eventId);
        long offerResult;
        do
        {
            offerResult = publication.offer(buffer, 0, parameters.length());
        }
        while (offerResult < 0);
    }
}