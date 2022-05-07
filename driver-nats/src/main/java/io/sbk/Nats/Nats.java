/**
 * Copyright (c) KMG. All Rights Reserved..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbk.Nats;

import io.nats.client.Options;
import io.sbk.api.DataReader;
import io.sbk.api.DataWriter;
import io.sbk.api.ParameterOptions;
import io.sbk.api.Storage;
import io.sbk.options.InputOptions;

import java.io.IOException;


/**
 * Class for Nats.
 */
public class Nats implements Storage<byte[]> {
    private String topicName;
    private String uri;
    private Options options;

    @Override
    public void addArgs(final InputOptions params) throws IllegalArgumentException {
        params.addOption("topic", true, "Topic name");
        params.addOption("uri", true, "Server URI");
    }

    @Override
    public void parseArgs(final ParameterOptions params) throws IllegalArgumentException {
        topicName = params.getOptionValue("topic", null);
        uri = params.getOptionValue("uri", null);
        if (uri == null) {
            throw new IllegalArgumentException("Error: Must specify Nats server IP address");
        }

        if (topicName == null) {
            throw new IllegalArgumentException("Error: Must specify Topic Name");
        }
        if (params.getReadersCount() < 1 || params.getWritersCount() < 1) {
            throw new IllegalArgumentException("Specify both Writer or readers for NATS Server");
        }
    }

    @Override
    public void openStorage(final ParameterOptions params) throws IOException {
        options = new Options.Builder().server(uri).maxReconnects(5).build();
    }

    @Override
    public void closeStorage(final ParameterOptions params) throws IOException {
    }

    @Override
    public DataWriter<byte[]> createWriter(final int id, final ParameterOptions params) {
        try {
            return new NatsWriter(id, params, topicName, options);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public DataReader<byte[]> createReader(final int id, final ParameterOptions params) {
        try {
            return new NatsCallbackReader(id, params, topicName, topicName + "-" + id, options);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
