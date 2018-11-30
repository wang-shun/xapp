package com.aitusoftware.example.aeron.service;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.engine.TicketEngineInputPublisher;
import com.aitusoftware.example.aeron.gateway.GatewayMessageHandler;
import com.aitusoftware.example.aeron.gateway.TicketGateway;
import com.aitusoftware.example.aeron.util.ShutdownBarrierSingleton;
import io.aeron.Aeron;
import io.aeron.archive.client.AeronArchive;
import io.aeron.driver.MediaDriver;
import lombok.val;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

import java.io.IOException;

public final class TicketGatewayService
{
    public static void main(final String[] args) throws IOException
    {
        val aeronDirectory = Config.driverPath("gateway").toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory);
        val aeronCtx = new Aeron.Context().aeronDirectoryName(aeronDirectory);
        val availabilityHandler = new ImageAvailabilityHandler();
        val archiverContext = Config.archiveClientContext(0).aeronDirectoryName(aeronDirectory);
        try (val driver = MediaDriver.launchEmbedded(driverCtx);
             val aeron = Aeron.connect(aeronCtx);
             val publication = aeron.addPublication(Config.applicationInputChannelSpec(), 1);
             val subscription = aeron.addSubscription(Config.applicationOutputChannelSpec(), 1,
                     availabilityHandler, availabilityHandler);
             val executor = new CloseableExecutor();
             val archive = AeronArchive.connect(archiverContext))
        {
            val ticketGateway = new TicketGateway(new TicketEngineInputPublisher(publication));
            executor.execute(new GatewayMessageHandler(ticketGateway, subscription, new SleepingMillisIdleStrategy(10L), archive));
            ticketGateway.startServer();

            ShutdownBarrierSingleton.barrier().await();
        }
    }
}