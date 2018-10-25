package com.aitusoftware.example.aeron.engine;

public interface TicketEngineInput
{
    void createEvent(final int ticketCount, final long ticketSalesStart, final long ticketSalesEnd);
    void buyTicket(final long userId, final long eventId);
    void registerForEvent(final long userId, final long eventId);
}
