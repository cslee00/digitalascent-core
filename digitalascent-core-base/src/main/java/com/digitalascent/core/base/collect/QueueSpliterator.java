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

final class QueueSpliterator<T> implements Spliterator<T> {
    private final BlockingQueue<T> queue;
    private final T poison;
    private final Future<?> producerFuture;

    QueueSpliterator(BlockingQueue<T> queue, T poison, Future<?> producerFuture) {
        this.queue = checkNotNull(queue);
        this.poison = checkNotNull(poison);
        this.producerFuture = checkNotNull(producerFuture);
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
                Uninterruptibles.getUninterruptibly(producerFuture);
            } catch (ExecutionException e) {
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