package com.aitusoftware.example.aeron.gateway;

import com.aitusoftware.example.aeron.engine.FailureReason;
import com.aitusoftware.example.aeron.engine.TicketEngineInput;
import com.aitusoftware.example.aeron.engine.TicketEngineOutput;
import com.sun.net.httpserver.spi.HttpServerProvider;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class TicketGateway implements TicketEngineOutput
{
    private final TicketEngineInput ticketEngineInput;

    public TicketGateway(final TicketEngineInput ticketEngineInput)
    {
        this.ticketEngineInput = ticketEngineInput;
    }

    @Override
    public void eventCreated(final long eventId)
    {
        System.out.printf("eventCreated(%d)%n", eventId);
    }

    @Override
    public void ticketAssigned(final long userId, final long eventId, final long ticketId)
    {
        System.out.printf("ticketAssigned(%d, %d, %d)%n", userId, eventId, ticketId);
    }

    @Override
    public void ticketRequestFailed(final long userId, final long eventId, final FailureReason failureReason)
    {
        System.out.printf("ticketRequestFailed(%d, %d, %s)%n", userId, eventId, failureReason);
    }

    private void createEvent(final int ticketCount)
    {
        ticketEngineInput.createEvent(ticketCount, System.currentTimeMillis(),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L));
    }

    private void register(final long userId, final long eventId)
    {
        ticketEngineInput.registerForEvent(userId, eventId);
    }

    private void buy(final long userId, final long eventId)
    {
        ticketEngineInput.buyTicket(userId, eventId);
    }

    public void startServer() throws IOException
    {
        val server = HttpServerProvider.provider().createHttpServer(
                new InetSocketAddress("localhost", 8080), 10);
        val context = server.createContext("/ticket");
        context.setHandler(exchange -> {
            val uri = exchange.getRequestURI().toString();
            if (uri.contains("/create"))
            {
                createEvent((int) extractNumberParam(uri, "ticketCount"));
            }
            else if (uri.contains("/register"))
            {
                register(extractNumberParam(uri, "userId"), extractNumberParam(uri, "eventId"));
            }
            else if (uri.contains("/buy"))
            {
                buy(extractNumberParam(uri, "userId"), extractNumberParam(uri, "eventId"));
            }

            exchange.sendResponseHeaders(200, 0);
        });
        server.start();
    }

    private static long extractNumberParam(final String input, final String paramName)
    {
        val matcher = Pattern.compile("%s=([0-9]+)").matcher(input);
        if (matcher.find())
        {
            return Long.parseLong(matcher.group(1));
        }

        return Long.MIN_VALUE;
    }
}