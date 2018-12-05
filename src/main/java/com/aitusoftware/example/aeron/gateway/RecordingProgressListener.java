package com.aitusoftware.example.aeron.gateway;

import io.aeron.Subscription;
import io.aeron.archive.client.RecordingEventsAdapter;
import io.aeron.archive.client.RecordingEventsListener;

public final class RecordingProgressListener implements RecordingEventsListener
{
    private final RecordingEventsAdapter recordingEventsAdapter;

    public RecordingProgressListener(final Subscription recordingEventsSubscription)
    {
        recordingEventsAdapter = new RecordingEventsAdapter(
                this, recordingEventsSubscription, 100);

    }

    int doWork()
    {
        return recordingEventsAdapter.poll();
    }

    @Override
    public void onStart(long recordingId, long startPosition, int sessionId,
                        int streamId, String channel, String sourceIdentity)
    {
        System.out.printf("Recording start: %d @ %d (%s)%n", recordingId, startPosition, channel);
    }

    @Override
    public void onProgress(long recordingId, long startPosition, long position)
    {
        System.out.printf("Recording progress: %d (%d/%d)%n", recordingId, startPosition, position);
    }

    @Override
    public void onStop(long recordingId, long startPosition, long stopPosition)
    {
        System.out.printf("Recording stop: %d @ %d%n", recordingId, startPosition);
    }
}