/**
 * Copyright (c) KMG. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.perl.api;

public sealed interface GetPerlChannel permits Channel, Perl {

    /**
     * Get the PerlChannel to get the benchmark results.
     *
     * @return PerlChannel to get the benchmark results.
     */
    PerlChannel getPerlChannel();

}