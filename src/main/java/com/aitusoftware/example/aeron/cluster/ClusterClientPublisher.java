package com.aitusoftware.example.aeron.cluster;

import com.aitusoftware.example.aeron.util.Publisher;
import io.aeron.Aeron;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.cluster.codecs.EventCode;
import io.aeron.logbuffer.Header;
import lombok.val;
import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;

public final class ClusterClientPublisher implements Publisher, EgressListener, AutoCloseable
{
    private final Aeron aeron;
    private final AeronCluster aeronCluster;

    public ClusterClientPublisher(final Aeron aeron, final String egressChannel)
    {
        this.aeron = aeron;
        val ctx = new AeronCluster.Context();
        ctx.aeron(aeron).egressChannel(egressChannel).egressListener(this);
        aeronCluster = AeronCluster.connect(ctx);
    }

    @Override
    public long offer(final DirectBuffer buffer, final int offset, final int length)
    {
        return 0;
    }

    @Override
    public void onMessage(final long clusterSessionId, final long timestampMs, final DirectBuffer buffer, final int offset, final int length, final Header header)
    {

    }

    @Override
    public void sessionEvent(final long correlationId, final long clusterSessionId, final long leadershipTermId, final int leaderMemberId, final EventCode code, final String detail)
    {

    }

    @Override
    public void newLeader(final long clusterSessionId, final long leadershipTermId, final int leaderMemberId, final String memberEndpoints)
    {

    }

    @Override
    public void close() throws Exception
    {
        CloseHelper.quietClose(aeron);
        CloseHelper.quietClose(aeronCluster);
    }
}
