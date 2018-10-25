package com.aitusoftware.example.aeron.engine;

import org.agrona.collections.LongHashSet;

final class Event
{
    private final long start;
    private final long end;
    private final int ticketCount;
    private final long eventId;
    private final LongHashSet registeredUsers = new LongHashSet();

    private int remainingTickets;

    Event(final long start, final long end, final int ticketCount, final long eventId)
    {
        this.start = start;
        this.end = end;
        this.ticketCount = ticketCount;
        this.eventId = eventId;
        remainingTickets = ticketCount;
    }

    void register(final long userId)
    {
        registeredUsers.add(userId);
    }

    int lastAssignedTicketId()
    {
        return remainingTickets;
    }

    FailureReason buyTicket(final long userId)
    {
        if (!registeredUsers.contains(userId))
        {
            return FailureReason.USER_NOT_REGISTERED_FOR_EVENT;
        }
        if (remainingTickets != 0)
        {
            remainingTickets--;
            return FailureReason.NONE;
        }
        return FailureReason.NO_TICKETS_LEFT;
    }
}
