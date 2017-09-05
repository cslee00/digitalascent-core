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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

@SuppressWarnings("unused")
public final class MoreStreams {

    public static <T> Stream<T> batchLoadingStream(BatchSupplier<T> batchSupplier) {
        checkNotNull( batchSupplier );
        Stream<Iterable<T>> batchStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new BatchIterator<>(batchSupplier), Spliterator.ORDERED | Spliterator.IMMUTABLE),
                false);

        return batchStream.flatMap(i -> StreamSupport.stream(i.spliterator(), false));
    }

    public static <T> Stream<T> batchLoadingStream(BatchSupplier<T> batchSupplier, int queueSize) {
        checkNotNull( batchSupplier );
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
                    Uninterruptibles.putUninterruptibly(queue, currentBatch.getIterable());
                    done = currentBatch.getNextToken() == null;

                    if (lastToken != null && Objects.equals(lastToken, currentBatch.getNextToken())) {
                        throw new IllegalStateException(String.format("Received the same batch token '%s' for two batches, aborting", lastToken));
                    }

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
     * @param queue
     * @param poison
     * @param future
     */
    private static <T> Stream<T> queueStream(BlockingQueue<T> queue, T poison, Future<?> future) {
        return StreamSupport.stream(new QueueSpliterator<>(queue, poison, future), false);
    }

    private MoreStreams() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
