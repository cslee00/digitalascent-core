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

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Spliterator that pulls elements from the provided queue.  Blocks waiting for elements from the queue, exiting
 * when the provided 'poison' element is encountered.  Exceptions from the async producer are propagated.
 *
 * @param <T> type of elements in the queue
 */
final class QueueSpliterator<T> implements Spliterator<T> {
    private final BlockingQueue<T> queue;
    private final T poison;
    private final Future<?> producerFuture;

    QueueSpliterator(BlockingQueue<T> queue, T poison, Future<?> producerFuture) {
        this.queue = checkNotNull(queue, "queue is required");
        this.poison = checkNotNull(poison, "poison is required");
        this.producerFuture = checkNotNull(producerFuture, "producerFuture is required");
    }

    @Override
    public int characteristics() {
        return Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.IMMUTABLE;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        final T next = Uninterruptibles.takeUninterruptibly(queue);
        if (next == poison) {
            try {
                // obtain result from producer, used to propagate any producer exceptions
                Uninterruptibles.getUninterruptibly(producerFuture);
            } catch (ExecutionException e) {
                // propagate producer exception
                Throwables.throwIfUnchecked(e.getCause());
                throw new RuntimeException(e.getCause());
            }
            return false;
        }
        action.accept(next);
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

}