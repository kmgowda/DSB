/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbk.driver.Kafka;

import io.perl.api.PerlChannel;
import io.sbk.logger.ReadRequestsLogger;
import io.sbk.params.ParameterOptions;
import io.sbk.api.Reader;
import io.sbk.api.Status;
import io.sbk.data.DataType;
import io.sbk.system.Printer;
import io.time.Time;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.EOFException;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Class for Kafka reader/consumer.
 */
public class KafkaReader implements Reader<byte[]> {
    final private KafkaConsumer<byte[], byte[]> consumer;
    final private Duration timeoutDuration;
    final private int maxPollRecords;

    public KafkaReader(int id, ParameterOptions params, String topicName, Properties consumerProps,
                       int timeoutMS) throws IOException {
        this.consumer = new KafkaConsumer<>(consumerProps);
        this.consumer.subscribe(Arrays.asList(topicName));
        this.timeoutDuration = Duration.ofMillis(timeoutMS);
        int maxPollRecords =  Integer.MAX_VALUE;
        try {
            maxPollRecords = (int) consumerProps.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            Printer.log.warn("Max Poll Records set to "+ maxPollRecords);
        }
        this.maxPollRecords = maxPollRecords;
    }

    @Override
    public void recordRead(DataType dType, int size, Time time, Status status, PerlChannel perlChannel) throws IOException {
        status.startTime = time.getCurrentTime();
        final ConsumerRecords<byte[], byte[]> records = consumer.poll(timeoutDuration);
        status.endTime = time.getCurrentTime();
        if (records.isEmpty()) {
            status.records = 0;
        } else {
            status.bytes = 0;
            status.records = 0;
            for (ConsumerRecord<byte[], byte[]> record : records) {
                status.bytes += record.value().length;
                status.records += 1;
            }
            perlChannel.send(status.startTime, status.endTime, status.records, status.bytes);
        }
    }

    @Override
    public void recordRead(DataType dType, int size, Time time, Status status, PerlChannel perlChannel, int id,
                    ReadRequestsLogger logger) throws EOFException, IOException {
        status.startTime = time.getCurrentTime();
        logger.recordReadRequests(id, status.startTime, (long) size * maxPollRecords, maxPollRecords);
        final ConsumerRecords<byte[], byte[]> records = consumer.poll(timeoutDuration);
        status.endTime = time.getCurrentTime();
        if (records.isEmpty()) {
            status.records = 0;
        } else {
            status.bytes = 0;
            status.records = 0;
            for (ConsumerRecord<byte[], byte[]> record : records) {
                status.bytes += record.value().length;
                status.records += 1;
            }
            perlChannel.send(status.startTime, status.endTime, status.records, status.bytes);
        }
    }

    @Override
    public void recordReadTime(DataType dType, int size, Time time, Status status, PerlChannel perlChannel,
                        int id, ReadRequestsLogger logger) throws EOFException, IOException {
        logger.recordReadRequests(id, time.getCurrentTime(), (long) size * maxPollRecords, maxPollRecords);
        recordReadTime(dType, size, time, status, perlChannel);
    }

    @Override
    public void recordReadTime(DataType dType, int size, Time time, Status status, PerlChannel perlChannel)
            throws IOException {
        final ConsumerRecords<byte[], byte[]> records = consumer.poll(timeoutDuration);
        status.endTime = time.getCurrentTime();
        if (records.isEmpty()) {
            status.records = 0;
        } else {
            status.bytes = 0;
            status.records = 0;
            status.startTime = 0;
            for (ConsumerRecord<byte[], byte[]> record : records) {
                status.bytes += record.value().length;
                status.records += 1;
                if (status.startTime == 0) {
                    status.startTime = dType.getTime(record.value());
                }
            }
            perlChannel.send(status.startTime, status.endTime, status.records, status.bytes);
        }
    }

    @Override
    public byte[] read() {
        final ConsumerRecords<byte[], byte[]> records = consumer.poll(timeoutDuration);
        if (records.isEmpty()) {
            return null;
        }
        return records.iterator().next().value();
    }

    @Override
    public void close() {
        consumer.close();
    }
}