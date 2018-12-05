package com.aitusoftware.example.aeron.gateway;

import com.aitusoftware.example.aeron.Config;
import com.aitusoftware.example.aeron.engine.FailureReason;
import com.aitusoftware.example.aeron.engine.TicketEngineOutput;
import com.aitusoftware.example.aeron.util.Message;
import com.aitusoftware.example.aeron.util.MessageConstants;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.archive.client.AeronArchive;
import io.aeron.archive.client.RecordingDescriptorConsumer;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import lombok.val;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.IdleStrategy;

import java.util.concurrent.TimeUnit;

public final class GatewayMessageHandler implements FragmentHandler, Runnable
{
    private static final FailureReason[] FAILURE_REASONS = FailureReason.values();
    private final TicketEngineOutput ticketEngineOutput;
    private final Subscription subscription;
    private final IdleStrategy idleStrategy;
    private final AeronArchive archive;
    private final RecordingProgressListener progressListener;
    private final Message message = new Message();
    private final FragmentHandler fragmentHandler = new FragmentAssembler(this);
    private long nextRecordingQueryTimestamp;

    public GatewayMessageHandler(
            final TicketEngineOutput ticketEngineOutput,
            final Subscription subscription,
            final IdleStrategy idleStrategy,
            final AeronArchive archive,
            final RecordingProgressListener progressListener)
    {
        this.ticketEngineOutput = ticketEngineOutput;
        this.subscription = subscription;
        this.idleStrategy = idleStrategy;
        this.archive = archive;
        this.progressListener = progressListener;
    }

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
        if (replaying)
        {
            System.out.println("Replaying message");
        }
        message.reset(buffer, offset, length);
        val topicId = message.getInt();
        switch (topicId)
        {
            case MessageConstants.TOPIC_EVENT_CREATED_ID:
                invokeEventCreated(message);
                break;
            case MessageConstants.TOPIC_TICKET_ASSIGNED_ID:
                invokeTicketAssigned(message);
                break;
            case MessageConstants.TOPIC_TICKET_REQUEST_FAILED_ID:
                invokeRequestFailed(message);
                break;
            default:
                // ignore, unknown topic
                break;
        }
    }

    private void invokeRequestFailed(final Message message)
    {
        ticketEngineOutput.ticketRequestFailed(message.getLong(), message.getLong(), FAILURE_REASONS[message.getInt()]);
    }

    private void invokeTicketAssigned(final Message message)
    {
        ticketEngineOutput.ticketAssigned(message.getLong(), message.getLong(), message.getLong());
    }

    private void invokeEventCreated(final Message message)
    {
        ticketEngineOutput.eventCreated(message.getLong());
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            final int workDone = subscription.poll(fragmentHandler, 100);
            checkForRecordings();
            idleStrategy.idle(workDone + (progressListener != null ? progressListener.doWork() : 0));
        }
    }

    private Subscription replaySubscription;
    private boolean replaying;

    private void checkForRecordings()
    {
        if (replaySubscription != null)
        {
            replaying = true;
            replaySubscription.poll(fragmentHandler, 100);
            replaying = false;
        }
        if (System.nanoTime() > nextRecordingQueryTimestamp)
        {
            archive.listRecordingsForUri(0, Integer.MAX_VALUE,
                    Config.applicationOutputChannelSpec(), 1, new RecordingDescriptorConsumer()
                    {
                        @Override
                        public void onRecordingDescriptor(long controlSessionId,
                                                          long correlationId,
                                                          long recordingId,
                                                          long startTimestamp,
                                                          long stopTimestamp,
                                                          long startPosition,
                                                          long stopPosition,
                                                          int initialTermId,
                                                          int segmentFileLength,
                                                          int termBufferLength,
                                                          int mtuLength,
                                                          int sessionId,
                                                          int streamId,
                                                          String strippedChannel,
                                                          String originalChannel,
                                                          String sourceIdentity)
                        {
                            if (stopPosition > 0 && replaySubscription == null && originalChannel.startsWith(Config.applicationOutputChannelSpec()))
                            {
                                archive.startReplay(recordingId, 0, stopPosition, Config.replayChannel(), 0);
                                replaySubscription = archive.context().aeron().addSubscription(Config.replayChannel(), 0);
                            }
                        }
                    });
            nextRecordingQueryTimestamp = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
        }
    }
}