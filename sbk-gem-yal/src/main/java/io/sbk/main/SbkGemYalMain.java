/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.sbk.main;

import io.sbk.exception.HelpException;
import io.sbk.gem.impl.SbkGemYal;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Class SbkGemYalMain.
 */
public final class SbkGemYalMain {

    /**
     * The main Method of sbk-gem-yal module.
     *
     * @param args String[]
     */
    public static void main(final String[] args) {
        try {
            SbkGemYal.run(args, null, null, null);
        } catch (HelpException ex) {
            System.exit(2);
        } catch (ParseException | IllegalArgumentException | IOException | TimeoutException | InterruptedException |
                 ExecutionException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException | IllegalAccessException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
