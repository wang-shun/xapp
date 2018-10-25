# Example Aeron-based application

## Stateless service

Publishes messages into a stateful service, receives responses.

## Stateful service

Receives messages, does some processing on in-memory state, sends responses.

## Infrastructure

### Archive

Streams in to/out of the stateful service are archived.
A stateless service can replay the outbound stream of the stateful service.

### Monitoring

Message rates, error counts, window lengths, etc are published.

### MDC

Outbound messages should be multicast to all stateless services.