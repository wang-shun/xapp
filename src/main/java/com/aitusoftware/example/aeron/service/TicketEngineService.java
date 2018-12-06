package com.aitusoftware.example.aeron.service;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.engine.EngineMessageHandler;
import com.aitusoftware.example.aeron.engine.TicketEngine;
import com.aitusoftware.example.aeron.engine.TicketEngineOutputPublisher;
import com.aitusoftware.example.aeron.util.CompositeCloseable;
import com.aitusoftware.example.aeron.util.ShutdownBarrierSingleton;
import io.aeron.Aeron;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.ArchivingMediaDriver;
import io.aeron.archive.client.AeronArchive;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import lombok.val;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

import static com.aitusoftware.example.aeron.Config.IDLE_STRATEGY;

public final class TicketEngineService
{
    public static void main(final String[] args)
    {
        try (val closeable = launch())
        {
            ShutdownBarrierSingleton.barrier().await();
        }
    }

    public static CompositeCloseable launch()
    {
        val aeronDirectory = Config.driverPath("engine").toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory).
                conductorIdleStrategy(IDLE_STRATEGY).receiverIdleStrategy(IDLE_STRATEGY).
                senderIdleStrategy(IDLE_STRATEGY).threadingMode(ThreadingMode.SHARED);
        val archiveCtx = new Archive.Context().archiveDirectoryName(Config.archivePath("engine").toString()).
                aeronDirectoryName(aeronDirectory).controlChannel(Config.archiveControlRequestChannel()).
                recordingEventsChannel(Config.archiveRecordingEventsChannel()).idleStrategySupplier(() -> IDLE_STRATEGY).
                threadingMode(ArchiveThreadingMode.SHARED);
        val aeronCtx = new Aeron.Context().aeronDirectoryName(aeronDirectory);
        val availabilityHandler = new ImageAvailabilityHandler();
        val archiveClientCtx = Config.archiveClientContext(10).
                aeronDirectoryName(driverCtx.aeronDirectoryName());

        val driver = ArchivingMediaDriver.launch(driverCtx, archiveCtx);
        val aeron = Aeron.connect(aeronCtx);
        val archiveClient = AeronArchive.connect(archiveClientCtx);
        val publication = archiveClient.addRecordedPublication(Config.applicationOutputChannelSpec(), 1);
        val subscription = aeron.addSubscription(Config.applicationInputChannelSpec(), 1,
                availabilityHandler, availabilityHandler);
        val executor = new CloseableExecutor();

        val closeable = new CompositeCloseable(driver, aeron, archiveClient, publication, subscription, executor);
        val ticketEngine = new TicketEngine(new TicketEngineOutputPublisher(publication));
        executor.execute(new EngineMessageHandler(ticketEngine, subscription, new SleepingMillisIdleStrategy(10L)));

        return closeable;
    }
}