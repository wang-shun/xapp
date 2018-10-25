package com.aitusoftware.example.aeron;

import io.aeron.archive.client.AeronArchive;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Config
{
    private static final Path DRIVER_ROOT_PATH = Paths.get("/dev/shm");
    private static final int APPLICATION_INPUT_PORT = 15678;
    private static final int APPLICATION_OUTPUT_PORT = 15680;

    public static Path driverPath(final String service) {
        return DRIVER_ROOT_PATH.resolve(service).resolve("driver");
    }

    public static Path archivePath(final String service) {
        return DRIVER_ROOT_PATH.resolve(service).resolve("archive");
    }

    public static String applicationInputChannelSpec()
    {
        return localChannel(APPLICATION_INPUT_PORT);
    }

    public static String applicationOutputChannelSpec()
    {
        return localChannel(APPLICATION_OUTPUT_PORT);
    }

    public static String archiveControlRequestChannel()
    {
        return localChannel(16001);
    }

    public static String archiveClientControlResponseChannel(final int offset)
    {
        return localChannel(17003 + offset);
    }

    public static String archiveRecordingEventsChannel()
    {
        return localChannel(16005);
    }

    public static AeronArchive.Context archiveClientContext(final int offset)
    {
        return new AeronArchive.Context().
                controlRequestChannel(archiveControlRequestChannel()).
                controlResponseChannel(archiveClientControlResponseChannel(offset)).
                recordingEventsChannel(archiveRecordingEventsChannel());
    }

    private static String localChannel(final int port)
    {
        return "aeron:udp?endpoint=localhost:" + port;
    }
}