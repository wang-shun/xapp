package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Publisher;
import com.aitusoftware.example.aeron.util.PublisherReceiver;
import io.aeron.Publication;

public final class TicketEngineOutputPublisher implements TicketEngineOutput, PublisherReceiver
{
    private final GenericEngineOutputPublisher genericEngineOutputPublisher =
            new GenericEngineOutputPublisher();
    private Publisher publisher;

    public TicketEngineOutputPublisher(final Publication publication)
    {
        this.publisher = publication::offer;
    }

    public TicketEngineOutputPublisher()
    {
    }

    @Override
    public void eventCreated(final long eventId)
    {
        genericEngineOutputPublisher.publishEventCreated(eventId, publisher);
    }

    @Override
    public void ticketAssigned(final long userId, final long eventId, final long ticketId)
    {
        genericEngineOutputPublisher.publishTicketAssigned(userId, eventId, ticketId, publisher);
    }

    @Override
    public void ticketRequestFailed(final long userId, final long eventId, final FailureReason failureReason)
    {
        genericEngineOutputPublisher.publishTicketRequestFailed(userId, eventId, failureReason, publisher);
    }

    public void setPublisher(final Publisher publisher)
    {
        this.publisher = publisher;
    }
}