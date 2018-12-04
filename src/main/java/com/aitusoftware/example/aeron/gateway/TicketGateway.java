package com.aitusoftware.example.aeron.gateway;

import com.aitusoftware.example.aeron.engine.FailureReason;
import com.aitusoftware.example.aeron.engine.TicketEngineInput;
import com.aitusoftware.example.aeron.engine.TicketEngineOutput;
import com.aitusoftware.example.aeron.service.CloseableExecutor;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.aitusoftware.example.aeron.gateway.UriParameterExtractor.extractNumberParam;

public final class TicketGateway implements TicketEngineOutput
{
    private final TicketEngineInput ticketEngineInput;
    private HttpServer server;

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
        System.out.printf("gateway register(%d, %d)%n", userId, eventId);
        ticketEngineInput.registerForEvent(userId, eventId);
    }

    private void buy(final long userId, final long eventId)
    {
        System.out.printf("gateway buy(%d, %d)%n", userId, eventId);
        ticketEngineInput.buyTicket(userId, eventId);
    }

    public void startServer(final CloseableExecutor executor) throws IOException
    {
        server = HttpServerProvider.provider().createHttpServer(
                new InetSocketAddress("0.0.0.0", 8080), 10);
        server.setExecutor(executor);
        val context = server.createContext("/ticket");
        context.setHandler(exchange -> {
            val uri = exchange.getRequestURI().toString();
            System.out.printf("Received request on URI %s%n", uri);
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

            exchange.sendResponseHeaders(200, 2);
            exchange.getResponseBody().write("OK".getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().close();
        });
        server.start();
    }

    public void stopServer()
    {
        Optional.ofNullable(server).ifPresent(server -> server.stop(1));
    }

}