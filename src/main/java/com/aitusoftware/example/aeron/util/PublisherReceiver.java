package com.aitusoftware.example.aeron.util;

public interface PublisherReceiver
{
    void setPublisher(final Publisher publisher);

    void addPublisher(final long id, final Publisher publisher);

    void removePublisher(final long id);
}
