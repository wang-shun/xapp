package com.aitusoftware.example.aeron.cluster;

import com.aitusoftware.example.aeron.engine.TicketEngineInput;
import com.aitusoftware.example.aeron.util.Message;
import com.aitusoftware.example.aeron.util.MessageConstants;
import com.aitusoftware.example.aeron.util.PublisherReceiver;
import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;
import lombok.val;
import org.agrona.DirectBuffer;

public final class ClusteredTicketEngine implements ClusteredService
{
    private final TicketEngineInput ticketEngineInput;
    private final Message message = new Message();
    private final PublisherReceiver publisherReceiver;

    public ClusteredTicketEngine(final TicketEngineInput ticketEngineInput, final PublisherReceiver publisherReceiver)
    {
        this.ticketEngineInput = ticketEngineInput;
        this.publisherReceiver = publisherReceiver;
    }

    @Override
    public void onStart(Cluster cluster)
    {

    }

    @Override
    public void onSessionOpen(ClientSession session, long timestampMs)
    {

    }

    @Override
    public void onSessionClose(ClientSession session, long timestampMs, CloseReason closeReason)
    {

    }

    @Override
    public void onSessionMessage(ClientSession session, long timestampMs, DirectBuffer buffer, int offset, int length, Header header)
    {
        publisherReceiver.setPublisher(session::offer);
        message.reset(buffer, offset, length);
        val topicId = message.getInt();
        switch (topicId)
        {
            case MessageConstants.TOPIC_CREATE_EVENT_ID:
                invokeCreateEvent(message);
                break;
            case MessageConstants.TOPIC_BUY_TICKET_ID:
                invokeBuyTicket(message);
                break;
            case MessageConstants.TOPIC_REGISTER_FOR_EVENT_ID:
                invokeRegisterForEvent(message);
                break;
            default:
                // ignore, unknown topic
                break;
        }
    }

    @Override
    public void onTimerEvent(long correlationId, long timestampMs)
    {

    }

    @Override
    public void onTakeSnapshot(Publication snapshotPublication)
    {

    }

    @Override
    public void onLoadSnapshot(Image snapshotImage)
    {

    }

    @Override
    public void onRoleChange(Cluster.Role newRole)
    {

    }

    private void invokeRegisterForEvent(final Message message)
    {
        ticketEngineInput.registerForEvent(message.getLong(), message.getLong());
    }

    private void invokeBuyTicket(final Message message)
    {
        ticketEngineInput.buyTicket(message.getLong(), message.getLong());
    }

    private void invokeCreateEvent(final Message message)
    {
        ticketEngineInput.createEvent(message.getInt(), message.getLong(), message.getLong());
    }
}
