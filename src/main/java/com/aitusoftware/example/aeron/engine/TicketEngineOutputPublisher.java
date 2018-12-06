package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Publisher;
import com.aitusoftware.example.aeron.util.PublisherReceiver;
import io.aeron.Publication;
import lombok.val;
import org.agrona.collections.Long2ObjectHashMap;

public final class TicketEngineOutputPublisher implements TicketEngineOutput, PublisherReceiver
{
    private final GenericEngineOutputPublisher genericEngineOutputPublisher =
            new GenericEngineOutputPublisher();
    private final Long2ObjectHashMap<Publisher> receivedPublishers = new Long2ObjectHashMap<>();
    private Publisher currentPublisher;

    public TicketEngineOutputPublisher(final Publication publication)
    {
        this.currentPublisher = publication::offer;
    }

    public TicketEngineOutputPublisher()
    {
    }

    @Override
    public void eventCreated(final long eventId)
    {
        val iterator = receivedPublishers.values().iterator();
        while (iterator.hasNext())
        {
            genericEngineOutputPublisher.publishEventCreated(eventId, iterator.next());
        }
    }

    @Override
    public void ticketAssigned(final long userId, final long eventId, final long ticketId)
    {
        genericEngineOutputPublisher.publishTicketAssigned(userId, eventId, ticketId, currentPublisher);
    }

    @Override
    public void ticketRequestFailed(final long userId, final long eventId, final FailureReason failureReason)
    {
        genericEngineOutputPublisher.publishTicketRequestFailed(userId, eventId, failureReason, currentPublisher);
    }

    public void setPublisher(final Publisher publisher)
    {
        this.currentPublisher = publisher;
    }

    @Override
    public void removePublisher(final long id)
    {
        receivedPublishers.remove(id);
    }

    @Override
    public void addPublisher(final long id, final Publisher publisher)
    {
        receivedPublishers.put(id, publisher);
    }
}