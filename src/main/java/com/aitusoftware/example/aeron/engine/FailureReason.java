package com.aitusoftware.example.aeron.engine;

public enum FailureReason
{
    NO_TICKETS_LEFT,
    USER_NOT_REGISTERED_FOR_EVENT,
    EVENT_NOT_YET_OPEN,
    EVENT_CLOSED,
    UNKNOWN_EVENT,
    NONE;
}
