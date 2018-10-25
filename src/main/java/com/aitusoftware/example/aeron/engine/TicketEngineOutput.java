package com.aitusoftware.example.aeron.engine;

public interface TicketEngineOutput
{
    void eventCreated(final long eventId);
    void ticketAssigned(final long userId, final long eventId, final long ticketId);
    void ticketRequestFailed(final long userId, final long eventId, final FailureReason failureReason);
}
