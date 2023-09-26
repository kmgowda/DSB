/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbk.drivers.NatsStream;

import io.nats.streaming.AckHandler;
import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options.Builder;
import io.nats.streaming.StreamingConnection;
import io.perl.api.PerlChannel;
import io.sbk.params.ParameterOptions;
import io.sbk.api.Status;
import io.sbk.api.Writer;
import io.sbk.data.DataType;
import io.sbk.system.Printer;
import io.time.Time;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Class for NATS Stream Writer.
 */
public class NatsStreamWriter implements Writer<byte[]> {
    final private StreamingConnection producer;
    final private String topic;

    public NatsStreamWriter(int writerID, ParameterOptions params,
                            String topicName, NatsStreamClientConfig config, Builder builder) throws IOException {
        this.topic = topicName;
        try {
            producer = NatsStreaming.connect(config.clusterName, String.valueOf(writerID + params.getReadersCount()),
                    builder.build());
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void recordWrite(DataType<byte[]> dType, byte[] data, int size, Time time,
                            Status status, PerlChannel record) {
        final long ctime = time.getCurrentTime();
        status.startTime = ctime;
        status.bytes = size;
        status.records = 1;
        final String[] guid = new String[1];
        final AckHandler acb = (s, e) -> {
            if (e != null || !guid[0].equals(s)) {
                Printer.log.error("NAT Streaming Writer failed !");
            } else {
                final long endTime = time.getCurrentTime();
                record.send(ctime, endTime, 1, size);
            }
        };
        try {
            guid[0] = producer.publish(topic, data, acb);
        } catch (InterruptedException | TimeoutException | IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public CompletableFuture writeAsync(byte[] data) throws IOException {
        try {
            producer.publish(topic, data);
        } catch (InterruptedException | TimeoutException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void sync() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}