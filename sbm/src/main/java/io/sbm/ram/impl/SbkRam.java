/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.sbm.ram.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import io.micrometer.core.instrument.util.IOUtils;
import io.perl.api.impl.PerlBuilder;
import io.sbk.api.Benchmark;
import io.sbk.config.Config;
import io.sbm.config.RamConfig;
import io.sbk.exception.HelpException;
import io.sbm.logger.RamLogger;
import io.sbm.logger.impl.RamPrometheusLogger;
import io.sbm.params.RamParameterOptions;
import io.sbm.params.impl.SbkRamParameters;
import io.sbk.system.Printer;
import io.time.Time;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Main class of SBK Server.
 */
final public class SbkRam {
    final static String CONFIG_FILE = "ram.properties";
    final static String BANNER_FILE = "ram-banner.txt";

    /**
     * Run the Performance Benchmarking .
     *
     * @param args            command line arguments.
     * @param applicationName name of the application. will be used in the 'help' message. if it is 'null' , SbkServer is used by default.
     * @param outLogger       Logger object to write the benchmarking results; if it is 'null' , the default Prometheus
     *                        logger will be used.
     * @throws ParseException           If an exception occurred while parsing command line arguments.
     * @throws IllegalArgumentException If an exception occurred due to invalid arguments.
     * @throws IOException              If an exception occurred due to write or read failures.
     * @throws InstantiationException   if the exception occurred due to initiation failures.
     * @throws InterruptedException     If an exception occurred if the writers and readers are interrupted.
     * @throws ExecutionException       If an exception occurred.
     * @throws TimeoutException         If an exception occurred if an I/O operation is timed out.
     */
    public static void run(final String[] args, final String applicationName,
                           RamLogger outLogger) throws ParseException, IllegalArgumentException,
            IOException, InterruptedException, ExecutionException, TimeoutException, InstantiationException {
        final Benchmark benchmark;
        try {
            benchmark = buildBenchmark(args, applicationName, outLogger);
        } catch (HelpException ex) {
            return;
        }
        final CompletableFuture<Void> ret = benchmark.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            benchmark.stop();
        }));
        ret.get();
    }


    /**
     * Build the Benchmark Object.
     *
     * @param args            command line arguments.
     * @param applicationName name of the application. will be used in the 'help' message. if it is 'null' , storage name is used by default.
     * @param outLogger       Logger object to write the benchmarking results; if it is 'null' , the default Prometheus
     *                        logger will be used.
     * @return Benchmark Interface
     * @throws HelpException            if '-help' option is supplied.
     * @throws ParseException           If an exception occurred while parsing command line arguments.
     * @throws IllegalArgumentException If an exception occurred due to invalid arguments.
     * @throws IOException              If an exception occurred due to write or read failures.
     */
    public static Benchmark buildBenchmark(final String[] args, final String applicationName,
                                           RamLogger outLogger) throws ParseException, IllegalArgumentException,
            IOException, HelpException {
        final RamParameterOptions params;
        final RamLogger logger;
        final RamConfig ramConfig;
        final Time time;
        final String version = SbkRam.class.getPackage().getImplementationVersion();
        final String appName = Objects.requireNonNullElse(applicationName, RamConfig.NAME);

        Printer.log.info(IOUtils.toString(SbkRam.class.getClassLoader().getResourceAsStream(BANNER_FILE)));
        Printer.log.info(RamConfig.DESC);
        Printer.log.info(RamConfig.NAME.toUpperCase() + " Version: " + version);
        Printer.log.info(RamConfig.NAME.toUpperCase() + " Website: " + Config.SBK_WEBSITE_NAME);
        Printer.log.info("Arguments List: " + Arrays.toString(args));
        Printer.log.info("Java Runtime Version: " + System.getProperty("java.runtime.version"));

        final ObjectMapper mapper = new ObjectMapper(new JavaPropsFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ramConfig = mapper.readValue(SbkRam.class.getClassLoader().getResourceAsStream(CONFIG_FILE),
                RamConfig.class);

        // disable CSV
        ramConfig.csv = false;
        logger = Objects.requireNonNullElseGet(outLogger, RamPrometheusLogger::new);

        params = new SbkRamParameters(appName, ramConfig.port, ramConfig.maxConnections);
        logger.addArgs(params);
        try {
            params.parseArgs(args);
            logger.parseArgs(params);
        } catch (UnrecognizedOptionException ex) {
            params.printHelp();
            Printer.log.error(ex.toString());
            throw ex;
        } catch (HelpException ex) {
            System.out.println("\n" + ex.getHelpText());
            throw ex;
        }

        time = PerlBuilder.buildTime(logger);
        return new SbkRamBenchmark(ramConfig, params, logger, time);
    }


}
