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


import com.digitalascent.core.base.concurrent.Threads;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

public final class MoreStreams {

    /**
     * Create a stream that synchronously lazy-loads batches of elements from the provided supplier.
     *
     * @param batchSupplier the supplier that provides batches to expose in the stream
     * @param <T>           type of element
     * @return Stream of elements that are lazy-loaded in batches from the provided supplier
     */
    public static <T> Stream<T> batchLoadingStream(BatchSupplier<T> batchSupplier) {
        checkNotNull(batchSupplier, "batchSupplier is required");

        Stream<Iterable<T>> batchStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new BatchIterator<>(batchSupplier), Spliterator.ORDERED | Spliterator.IMMUTABLE),
                false);

        return batchStream.flatMap(i -> StreamSupport.stream(i.spliterator(), false));
    }

    /**
     * Create a stream that <i>asynchronously</i> lazy-loads batches of elements from the provided supplier.
     * Useful for suppliers that load batches from API calls to overlap producer network/processing latency
     * with consumption of batches.
     *
     * @param batchSupplier the supplier that provides batches to expose in the stream
     * @param <T>           type of element
     * @param queueSize     number of batches to allow to be queued before blocking the supplier from adding more batches
     * @return Stream of elements that are <i>asynchronously</i> lazy-loaded in batches from the provided supplier
     */
    public static <T> Stream<T> queuedBatchLoadingStream(BatchSupplier<T> batchSupplier, int queueSize) {
        checkNotNull(batchSupplier, "batchSupplier is required");
        checkArgument(queueSize > 0, "queueSize must be > 0 : %s", queueSize);

        BlockingQueue<Iterable<T>> queue = new ArrayBlockingQueue<>(queueSize);
        Iterable<T> poison = new ArrayList<>();

        // load batches in a separate thread, governed by the queue size (blocking when queue is full)
        // only use a single thread as batches are chained (the result of one batch has the token to load the next batch)
        ExecutorService executorService = Executors.newFixedThreadPool(1, Threads.defaultThreadFactory("MoreStreams.batchLoadingStream:" + batchSupplier.getClass()));
        Future<?> batchProducerFuture = executorService.submit(() -> {
            try {
                boolean done = false;
                Batch<T> currentBatch = Batch.emptyBatch();
                String lastToken = null;
                while (!done) {
                    currentBatch = batchSupplier.nextBatch(currentBatch.getNextToken());

                    verify(currentBatch != null, "Null batch returned from %s", batchSupplier.getClass());
                    verify(lastToken == null || !Objects.equals(lastToken, currentBatch.getNextToken()), "Received the same batch token '%s' for two batches, aborting", lastToken);

                    Uninterruptibles.putUninterruptibly(queue, currentBatch.getIterable());
                    done = currentBatch.getNextToken() == null;
                    lastToken = currentBatch.getNextToken();
                }
            } finally {
                // always poison the queue, notifying consumer that this producer is finished, even in the event of an exception here
                Uninterruptibles.putUninterruptibly(queue, poison);
            }
        });
        executorService.shutdown();

        // pull each batch off the queue and return a stream for it
        return queueStream(queue, poison, batchProducerFuture).flatMap(batch -> StreamSupport.stream(batch.spliterator(), false));
    }

    /**
     * Creates a stream supplying elements from the provided BlockingQueue, terminating when the <b>poison</b> element is encountered
     *
     * @param queue          the queue to extract elements from
     * @param poison         element that terminates the stream
     * @param producerFuture future for the producer of elements consumed by this queue, used to propagate exceptions
     */
    private static <T> Stream<T> queueStream(BlockingQueue<T> queue, T poison, Future<?> producerFuture) {
        return StreamSupport.stream(new QueueSpliterator<>(queue, poison, producerFuture), false);
    }

    private MoreStreams() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
