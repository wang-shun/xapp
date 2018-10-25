package com.aitusoftware.example.aeron.service;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.engine.EngineMessageHandler;
import com.aitusoftware.example.aeron.engine.TicketEngine;
import com.aitusoftware.example.aeron.engine.TicketEngineOutputPublisher;
import com.aitusoftware.example.aeron.util.ShutdownBarrierSingleton;
import io.aeron.Aeron;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchivingMediaDriver;
import io.aeron.archive.client.AeronArchive;
import io.aeron.driver.MediaDriver;
import lombok.val;
import org.agrona.concurrent.SleepingMillisIdleStrategy;

public final class TicketEngineService
{
    public static void main(final String[] args)
    {
        val aeronDirectory = Config.driverPath(args[0]).toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory);
        val archiveCtx = new Archive.Context().archiveDirectoryName(Config.archivePath(args[0]).toString()).
                aeronDirectoryName(aeronDirectory).controlChannel(Config.archiveControlRequestChannel()).
                recordingEventsChannel(Config.archiveRecordingEventsChannel());
        val aeronCtx = new Aeron.Context().aeronDirectoryName(aeronDirectory);
        val availabilityHandler = new ImageAvailabilityHandler();
        val archiveClientCtx = Config.archiveClientContext(10).
                aeronDirectoryName(driverCtx.aeronDirectoryName());
        try (val driver = ArchivingMediaDriver.launch(driverCtx, archiveCtx);
             val aeron = Aeron.connect(aeronCtx);
             val archiveClient = AeronArchive.connect(archiveClientCtx);
             val publication = archiveClient.addRecordedPublication(Config.applicationOutputChannelSpec(), 1);
             val subscription = aeron.addSubscription(Config.applicationInputChannelSpec(), 1,
                     availabilityHandler, availabilityHandler);
             val executor = new CloseableExecutor())
        {
            val ticketEngine = new TicketEngine(new TicketEngineOutputPublisher(publication));
            executor.execute(new EngineMessageHandler(ticketEngine, subscription, new SleepingMillisIdleStrategy(10L)));

            ShutdownBarrierSingleton.barrier().await();
        }
    }
}