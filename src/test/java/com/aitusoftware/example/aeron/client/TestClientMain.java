package com.aitusoftware.example.aeron.client;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class TestClientMain
{
    public static void main(String[] args) throws UnirestException
    {
        handleResponse(Unirest.get("http://localhost:8080/create?ticketCount=100"));
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        handleResponse(Unirest.get("http://localhost:8080/register?eventId=0&userId=17"));
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        handleResponse(Unirest.get("http://localhost:8080/buy?eventId=0&userId=17"));
    }

    private static void handleResponse(GetRequest getRequest) throws UnirestException
    {
        System.out.println(getRequest.asBinary().getStatus());
    }
}
