/**
 * Copyright (c) KMG. All Rights Reserved..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.sbk.Derby;

import io.sbk.Jdbc.Jdbc;

/**
 * Class for Derby.
 */
public class Derby extends Jdbc {
    private final static String CONFIGFILE = "derby.properties";

    @Override
    public String getConfigFile() {
        return CONFIGFILE;
    }
}
