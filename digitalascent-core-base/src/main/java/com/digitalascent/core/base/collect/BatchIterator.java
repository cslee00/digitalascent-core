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

import com.google.common.base.Verify;

import java.util.Iterator;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Iterator over batches of elements.
 *
 * @param <T> type of elements to iterate over
 */
final class BatchIterator<T> implements Iterator<Iterable<T>> {
    private final BatchSupplier<T> batchSupplier;
    private Batch<T> currentBatch;
    private String lastToken;

    BatchIterator(BatchSupplier<T> batchSupplier) {
        this.batchSupplier = checkNotNull(batchSupplier);
    }

    @Override
    public boolean hasNext() {
        // if we haven't requested the first batch or there is a token for a subsequent batch
        return currentBatch == null || currentBatch.getNextToken() != null;
    }

    @Override
    public Iterable<T> next() {
        currentBatch = batchSupplier.nextBatch(currentBatch == null ? null : currentBatch.getNextToken());
        Verify.verify(currentBatch != null, "Null batch returned from %s", batchSupplier.getClass());
        if( lastToken != null && Objects.equals(lastToken,currentBatch.getNextToken())) {
            throw new IllegalStateException(String.format("Received the same batch token '%s' for two batches, aborting", lastToken));
        }

        lastToken = currentBatch.getNextToken();

        return currentBatch.getIterable();
    }
}
