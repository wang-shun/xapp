package com.aitusoftware.example.aeron.engine;

import com.aitusoftware.example.aeron.util.Message;
import com.aitusoftware.example.aeron.util.MessageConstants;
import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import lombok.val;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.IdleStrategy;

public final class EngineMessageHandler implements FragmentHandler, Runnable
{
    private final TicketEngineInput ticketEngineInput;
    private final Subscription subscription;
    private final IdleStrategy idleStrategy;
    private final Message message = new Message();
    private final FragmentHandler handler = new FragmentAssembler(this);

    public EngineMessageHandler(
            final TicketEngineInput ticketEngineInput,
            final Subscription subscription,
            final IdleStrategy idleStrategy)
    {
        this.ticketEngineInput = ticketEngineInput;
        this.subscription = subscription;
        this.idleStrategy = idleStrategy;
    }

    @Override
    public void onFragment(final DirectBuffer buffer, final int offset, final int length, final Header header)
    {
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

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            final int workDone = subscription.poll(handler, 100);
            idleStrategy.idle(workDone);
        }
    }
}