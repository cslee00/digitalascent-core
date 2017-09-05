/*
 * Copyright 2017-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalascent.core.base.collect;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Batch<T> {
    private final String nextToken;
    private final Iterable<T> iterable;

    public static <T> Batch<T> emptyBatch() {
        return new Batch<>(null, ImmutableList.of());
    }

    public Batch(@Nullable String nextToken, Iterable<T> iterable) {
        this.nextToken = nextToken;
        this.iterable = checkNotNull(iterable);
    }

    String getNextToken() {
        return nextToken;
    }

    Iterable<T> getIterable() {
        return iterable;
    }
}
