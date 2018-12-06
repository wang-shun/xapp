package com.aitusoftware.example.aeron.client;

import com.aitusoftware.example.aeron.service.TicketEngineService;
import com.aitusoftware.example.aeron.service.TicketGatewayService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.google.common.truth.Truth.assertThat;

class ServiceTest
{
    @Test
    void shouldCreateAndRegisterForEvent() throws UnirestException, IOException
    {
        try (val engine = TicketEngineService.launch(); val gateway = TicketGatewayService.launchAsStandaloneClient())
        {
            handleResponse(Unirest.get("http://localhost:8080/ticket/create?ticketCount=100"));
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            handleResponse(Unirest.get("http://localhost:8080/ticket/register?eventId=0&userId=17"));
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            handleResponse(Unirest.get("http://localhost:8080/ticket/buy?eventId=0&userId=17"));
        }
    }

    private static void handleResponse(GetRequest getRequest) throws UnirestException
    {
        val responseCode = getRequest.asBinary().getStatus();
        assertThat(responseCode).isEqualTo(200);
    }
}
