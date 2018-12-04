package com.aitusoftware.example.aeron.engine;

import lombok.val;
import org.agrona.collections.Long2ObjectHashMap;

public final class TicketEngine implements TicketEngineInput
{
    private final TicketEngineOutput output;
    private final Long2ObjectHashMap<Event> eventMap = new Long2ObjectHashMap<>();
    private long eventId = 0L;

    public TicketEngine(final TicketEngineOutput output)
    {
        this.output = output;
    }

    @Override
    public void createEvent(final int ticketCount, final long ticketSalesStart, final long ticketSalesEnd)
    {
        val newEventId = eventId++;
        eventMap.put(newEventId, new Event(ticketSalesStart, ticketSalesEnd, ticketCount, newEventId));
        output.eventCreated(newEventId);
    }

    @Override
    public void buyTicket(final long userId, final long eventId)
    {
        System.out.printf("engine buy(%d, %d)%n", userId, eventId);
        if (eventMap.containsKey(eventId))
        {
            val event = eventMap.get(eventId);
            val failureReason = event.buyTicket(userId);
            if (failureReason == FailureReason.NONE)
            {
                output.ticketAssigned(userId, eventId, event.lastAssignedTicketId());
            }
            else
            {
                output.ticketRequestFailed(userId, eventId, failureReason);
            }
        }
        else
        {
            output.ticketRequestFailed(userId, eventId, FailureReason.UNKNOWN_EVENT);
        }
    }

    @Override
    public void registerForEvent(final long userId, final long eventId)
    {
        if (eventMap.containsKey(eventId))
        {
            val event = eventMap.get(eventId);
            event.register(userId);
        }
    }
}