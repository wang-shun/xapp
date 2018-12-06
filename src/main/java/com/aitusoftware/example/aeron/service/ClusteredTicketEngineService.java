package com.aitusoftware.example.aeron.service;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.cluster.ClusteredTicketEngine;
import com.aitusoftware.example.aeron.engine.TicketEngine;
import com.aitusoftware.example.aeron.engine.TicketEngineOutputPublisher;
import com.aitusoftware.example.aeron.util.CompositeCloseable;
import com.aitusoftware.example.aeron.util.ShutdownBarrierSingleton;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import lombok.val;

import static com.aitusoftware.example.aeron.Config.IDLE_STRATEGY;

public final class ClusteredTicketEngineService
{
    static final String ENGINE_SERVICE_NAME = "engine";

    public static void main(final String[] args)
    {
        try (val closeable = launch())
        {
            ShutdownBarrierSingleton.barrier().await();
        }
    }

    public static CompositeCloseable launch()
    {
        val aeronDirectory = Config.driverPath(ENGINE_SERVICE_NAME).toString();
        val driverCtx = new MediaDriver.Context().aeronDirectoryName(aeronDirectory).
                conductorIdleStrategy(IDLE_STRATEGY).receiverIdleStrategy(IDLE_STRATEGY).
                senderIdleStrategy(IDLE_STRATEGY).threadingMode(ThreadingMode.SHARED);
        val archiveCtx = new Archive.Context().archiveDirectoryName(Config.archivePath(ENGINE_SERVICE_NAME).toString()).
                aeronDirectoryName(aeronDirectory).controlChannel(Config.archiveControlRequestChannel()).
                recordingEventsChannel(Config.archiveRecordingEventsChannel()).idleStrategySupplier(() -> IDLE_STRATEGY).
                threadingMode(ArchiveThreadingMode.SHARED);
        val clusterDirectoryName = Config.clusterPath(ENGINE_SERVICE_NAME).toString();
        val consensusModuleCtx = new ConsensusModule.Context().aeronDirectoryName(aeronDirectory).
                clusterDirectoryName(clusterDirectoryName).idleStrategySupplier(() -> IDLE_STRATEGY);
        val engineOutputPublisher = new TicketEngineOutputPublisher();
        val serviceContainerCtx = new ClusteredServiceContainer.Context().
                clusteredService(new ClusteredTicketEngine(new TicketEngine(engineOutputPublisher), engineOutputPublisher)).
                idleStrategySupplier(() -> IDLE_STRATEGY).aeronDirectoryName(aeronDirectory).
                clusterDirectoryName(clusterDirectoryName);
        val driver = ClusteredMediaDriver.launch(driverCtx, archiveCtx, consensusModuleCtx);
        val container = ClusteredServiceContainer.launch(serviceContainerCtx);

        return new CompositeCloseable(driver, container);
    }
}