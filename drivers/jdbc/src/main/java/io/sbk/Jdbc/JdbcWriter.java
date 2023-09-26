/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbk.Jdbc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.perl.api.PerlChannel;
import io.sbk.params.ParameterOptions;
import io.sbk.api.Status;
import io.sbk.api.Writer;
import io.sbk.data.DataType;
import io.sbk.system.Printer;
import io.time.Time;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Class for JDBC  Writer.
 */
public class JdbcWriter implements Writer<String> {
    final public JdbcConfig config;
    final public String data;
    final private Connection conn;
    final private Statement st;
    final private DataType<String> dType;
    final private String defaultInsertQuery;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public JdbcWriter(int writerID, ParameterOptions params,
                      JdbcConfig config, DataType<String> dType) throws IOException {
        final Properties props = new Properties();
        this.dType = dType;
        if (config.user != null) {
            props.put("user", config.user);

        }
        if (config.password != null) {
            props.put("password", config.password);
        }
        try {
            if (props.isEmpty()) {
                conn = DriverManager.getConnection(config.url);
            } else {
                conn = DriverManager.getConnection(config.url, props);
            }
            conn.setAutoCommit(config.autoCommit);
            st = conn.createStatement();

        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        this.config = config;
        this.data = dType.create(params.getRecordSize());
        this.defaultInsertQuery = "INSERT INTO " + this.config.table + " (DATA) VALUES ('" + this.data + "')";
    }

    public String gerWriteQuery() {
        return this.defaultInsertQuery;
    }


    @Override
    public void recordWrite(DataType<String> dType, String data, int size, Time time,
                            Status status, PerlChannel record) throws IOException {
        status.startTime = time.getCurrentTime();
        try {
            st.executeUpdate(gerWriteQuery());
        } catch (SQLException ex) {
            Printer.log.error("JDBC: recordWrite failed !");
            throw new IOException(ex);
        }
        status.endTime = time.getCurrentTime();
        record.send(status.startTime, status.endTime, 1, size);
    }


    @Override
    public CompletableFuture writeAsync(String data) throws IOException {
        try {
            st.executeUpdate(gerWriteQuery());
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        return null;
    }

    @Override
    public void sync() throws IOException {
        try {
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }
}