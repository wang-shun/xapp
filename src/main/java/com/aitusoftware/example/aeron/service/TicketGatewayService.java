package com.aitusoftware.example.aeron.service;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.engine.TicketEngineInputPublisher;
import com.aitusoftware.example.aeron.gateway.GatewayMessageHandler;
import com.aitusoftware.example.aeron.gateway.RecordingProgressListener;
import com.aitusoftware.example.aeron.gateway.TicketGateway;
import com.aitusoftware.example.aeron.util.CompositeCloseable;
import com.aitusoftware.example.aeron.util.ShutdownBarrierSingleton;
import io.aeron.Aeron;
import io.aeron.archive.client.AeronArchive;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import lombok.val;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

import java.io.IOException;

import static com.aitusoftware.example.aeron.Config.IDLE_STRATEGY;
import static com.aitusoftware.example.aeron.service.ClusteredTicketEngineService.ENGINE_SERVICE_NAME;

public final class TicketGatewayService
{
    private static final String GATEWAY_SERVICE_NAME = "gateway";

    public static void main(final String[] args) throws IOException
    {
        try (val closeable = launchAsStandaloneClient())
        {
            ShutdownBarrierSingleton.barrier().await();
        }
    }

    public static CompositeCloseable launchAsClusterClient() throws IOException
    {
        val aeronDirectory = Config.driverPath(GATEWAY_SERVICE_NAME).toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory).
                senderIdleStrategy(IDLE_STRATEGY).receiverIdleStrategy(IDLE_STRATEGY).
                conductorIdleStrategy(IDLE_STRATEGY).threadingMode(ThreadingMode.SHARED);
        val aeronCtx = new Aeron.Context().aeronDirectoryName(aeronDirectory);
        val availabilityHandler = new ImageAvailabilityHandler();
        val archiverContext = Config.archiveClientContext(0).
                aeronDirectoryName(Config.driverPath(ENGINE_SERVICE_NAME).toString()).idleStrategy(IDLE_STRATEGY);
        val driver = MediaDriver.launchEmbedded(driverCtx);
        val aeron = Aeron.connect(aeronCtx);
        val publication = aeron.addPublication(Config.applicationInputChannelSpec(), 1);
        val subscription = aeron.addSubscription(Config.applicationOutputChannelSpec(), 1,
                availabilityHandler, availabilityHandler);
        val executor = new CloseableExecutor();
        val archive = AeronArchive.connect(archiverContext);
        // When in cluster client mode, the recorded stream should be the cluster client's egress channel
        val recordingEventsSubscription = aeron.addSubscription(
                Config.archiveRecordingEventsChannel(),
                AeronArchive.Configuration.recordingEventsStreamId());
        val ticketGateway = new TicketGateway(new TicketEngineInputPublisher(publication::offer));
        val closeable = new CompositeCloseable(driver, aeron, publication, subscription, executor, archive, recordingEventsSubscription, ticketGateway::stopServer);
        executor.execute(new GatewayMessageHandler(ticketGateway, subscription, new SleepingMillisIdleStrategy(10L), archive,
                new RecordingProgressListener(recordingEventsSubscription)));
        ticketGateway.startServer(executor);

        return closeable;
    }

    public static CompositeCloseable launchAsStandaloneClient() throws IOException
    {
        val aeronDirectory = Config.driverPath(GATEWAY_SERVICE_NAME).toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory).
                senderIdleStrategy(IDLE_STRATEGY).receiverIdleStrategy(IDLE_STRATEGY).
                conductorIdleStrategy(IDLE_STRATEGY).threadingMode(ThreadingMode.SHARED);
        val aeronCtx = new Aeron.Context().aeronDirectoryName(aeronDirectory);
        val availabilityHandler = new ImageAvailabilityHandler();
        val archiverContext = Config.archiveClientContext(0).
                aeronDirectoryName(aeronDirectory).idleStrategy(IDLE_STRATEGY);
        val driver = MediaDriver.launchEmbedded(driverCtx);
        val aeron = Aeron.connect(aeronCtx);
        val publication = aeron.addPublication(Config.applicationInputChannelSpec(), 1);
        val subscription = aeron.addSubscription(Config.applicationOutputChannelSpec(), 1,
                availabilityHandler, availabilityHandler);
        val executor = new CloseableExecutor();
        val archive = AeronArchive.connect(archiverContext);
        val recordingEventsSubscription = aeron.addSubscription(
                Config.archiveRecordingEventsChannel(),
                AeronArchive.Configuration.recordingEventsStreamId());
        val ticketGateway = new TicketGateway(new TicketEngineInputPublisher(publication::offer));
        val closeable = new CompositeCloseable(driver, aeron, publication, subscription, executor, archive, recordingEventsSubscription, ticketGateway::stopServer);
        executor.execute(new GatewayMessageHandler(ticketGateway, subscription, new SleepingMillisIdleStrategy(10L), archive,
                new RecordingProgressListener(recordingEventsSubscription)));
        ticketGateway.startServer(executor);

        return closeable;
    }
}