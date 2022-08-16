/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.perl.api.impl;

import io.perl.data.Bytes;
import io.perl.config.LatencyConfig;
import io.perl.api.LatencyPercentiles;
import io.perl.api.LatencyRecord;
import io.perl.api.LatencyRecordWindow;
import io.perl.api.ReportLatencies;
import io.time.Time;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Iterator;


/**
 * class for Performance statistics.
 */
@NotThreadSafe
final public class HashMapLatencyRecorder extends LatencyRecordWindow {
    final private HashMap<Long, Long> latencies;
    final private int maxHashMapSizeMB;
    final private long maxHashMapSizeBytes;
    final private int incBytes;
    private long hashMapBytesCount;

    /**
     * Constructor  HashMapLatencyRecorder initializing all values.
     *
     * @param lowLatency            long
     * @param highLatency           long
     * @param totalLatencyMax       long
     * @param totalRecordsMax       long
     * @param bytesMax              long
     * @param percentiles           double[]
     * @param time                  Time
     * @param maxHashMapSizeMB      int
     */
    public HashMapLatencyRecorder(long lowLatency, long highLatency, long totalLatencyMax, long totalRecordsMax, long bytesMax,
                                  double[] percentiles, Time time, int maxHashMapSizeMB) {
        super(lowLatency, highLatency, totalLatencyMax, totalRecordsMax, bytesMax, percentiles, time);
        this.latencies = new HashMap<>();
        this.maxHashMapSizeMB = maxHashMapSizeMB;
        this.maxHashMapSizeBytes = (long) maxHashMapSizeMB * Bytes.BYTES_PER_MB;
        this.incBytes = LatencyConfig.LATENCY_VALUE_SIZE_BYTES * 2;
        this.hashMapBytesCount = 0;
    }


    @Override
    public void reset(long startTime) {
        super.reset(startTime);
        this.latencies.clear();
        this.hashMapBytesCount = 0;
    }

    @Override
    public boolean isFull() {
        return (this.hashMapBytesCount > this.maxHashMapSizeBytes) || super.isOverflow();
    }

    @Override
    public long getMaxMemoryBytes() {
        return maxHashMapSizeBytes;
    }


    @Override
    public void copyPercentiles(LatencyPercentiles percentiles, ReportLatencies copyLatencies) {
        if (copyLatencies != null) {
            copyLatencies.reportLatencyRecord(this);
        }
        percentiles.reset(validLatencyRecords);
        Iterator<Long> keys = latencies.keySet().stream().sorted().iterator();
        long curIndex = 0;
        while (keys.hasNext()) {
            final long latency = keys.next();
            final long count = latencies.get(latency);
            final long nextIndex = curIndex + count;

            if (copyLatencies != null) {
                copyLatencies.reportLatency(latency, count);
            }
            percentiles.copyLatency(latency, count, curIndex, nextIndex);
            curIndex = nextIndex;
            latencies.remove(latency);
        }
        hashMapBytesCount = 0;
    }


    @Override
    public void reportLatencyRecord(LatencyRecord record) {
        super.update(record);
    }


    @Override
    public void reportLatency(long latency, long count) {
        Long val = latencies.get(latency);
        if (val == null) {
            val = 0L;
            hashMapBytesCount += incBytes;
        }
        latencies.put(latency, val + count);
    }

    /**
     * Record the latency.
     *
     * @param startTime start time.
     * @param events    number of events(records).
     * @param bytes     number of bytes.
     * @param latency   latency value in milliseconds.
     */
    @Override
    public void recordLatency(long startTime, int events, int bytes, long latency) {
        if (record(events, bytes, latency)) {
            reportLatency(latency, events);
        }
    }


}
