/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbk.Pulsar;

import io.sbk.params.ParameterOptions;
import io.sbk.api.Reader;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class for Pulsar reader/consumer.
 */
public class PulsarReader implements Reader<byte[]> {
    final private Consumer<byte[]> consumer;
    final private ParameterOptions params;

    public PulsarReader(int readerId, ParameterOptions params, String topicName,
                        String subscriptionName, PulsarClient client) throws IOException {
        this.params = params;
        final SubscriptionInitialPosition position = params.isWriteAndRead() ? SubscriptionInitialPosition.Latest :
                SubscriptionInitialPosition.Earliest;

        try {
            this.consumer = client.newConsumer()
                    .topic(topicName)
                    // Allow multiple consumers to attach to the same subscription
                    // and get messages dispatched as a queue
                    .subscriptionType(SubscriptionType.Exclusive)
                    .subscriptionName(subscriptionName)
                    .subscriptionInitialPosition(position)
                    .receiverQueueSize(1)
                    .subscribe();
        } catch (PulsarClientException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte[] read() throws IOException {
        try {
            return consumer.receive(params.getTimeoutMS(), TimeUnit.SECONDS).getData();
        } catch (PulsarClientException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            consumer.close();
        } catch (PulsarClientException ex) {
            throw new IOException(ex);
        }
    }
}