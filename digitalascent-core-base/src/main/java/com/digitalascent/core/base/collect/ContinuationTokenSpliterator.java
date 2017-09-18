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

import com.digitalascent.core.base.SimpleApplicationObject;
import com.google.common.util.concurrent.Uninterruptibles;

import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Spliterator that provides async IO retrieval of source elements in a chained continuation-token model, where the first response
 * contains an optional continuation token to be used in a subsequent request.  Async calls are made via a ContinuableResponseSource
 * with up to the specified number of responses outstanding in a queue before blocking, waiting for the stream to consume responses.
 *
 * This allows the latency of API calls to be overlapped with processing earlier API results, while providing a Stream interface
 * to API responses.
 *
 * @param <ResponseT>
 */
public final class ContinuationTokenSpliterator<ResponseT> extends SimpleApplicationObject implements Spliterator<CompletableFuture<ResponseT>> {

    private final CompletableFuture<ResponseT> poison = new CompletableFuture<>();
    private final BlockingQueue<CompletableFuture<ResponseT>> queue;
    private final ContinuableResponseSource<ResponseT> continuableResponseSource;
    private final Function<ResponseT,String> continuationTokenExtractor;

    private boolean firstAdvance = true;

    public ContinuationTokenSpliterator(ContinuableResponseSource<ResponseT> continuableResponseSource, Function<ResponseT, String> continuationTokenExtractor, int queueSize) {
        this.continuableResponseSource = checkNotNull(continuableResponseSource, "continuableResponseSource is required");
        this.continuationTokenExtractor = checkNotNull(continuationTokenExtractor, "continuationTokenExtractor is required");

        checkArgument(queueSize > 0, "queueSize > 0 : %s", queueSize);
        this.queue = new ArrayBlockingQueue<>(queueSize);
    }

    @Override
    public boolean tryAdvance(Consumer<? super CompletableFuture<ResponseT>> action) {
        if (firstAdvance) {
            // optimization - only make the initial request when initially advancing; subsequent requests
            // will continue asynchronously (chained off initial request, only one outstanding at a time)
            invokeRequest( null );
            firstAdvance = false;
        }

        CompletableFuture<ResponseT> completableResponse = Uninterruptibles.takeUninterruptibly(queue);
        if (completableResponse == poison) {
            return false;
        }
        action.accept(completableResponse);
        return true;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void invokeRequest(@Nullable String continuationToken) {
        CompletableFuture<ResponseT> completableFuture = continuableResponseSource.invoke(continuationToken);
        Uninterruptibles.putUninterruptibly(queue, completableFuture);

        // callback to chain next API call using continuation token from previous call
        completableFuture.whenComplete((continuableResponse, exception) -> {
            if( continuableResponse == null || exception != null ) {
                // error condition; exception will be propagated to consumer when it get()s this future
                // just in case we poison the queue to avoid a deadlock
                Uninterruptibles.putUninterruptibly(queue, poison);
                return;
            }

            String nextContinuationToken = continuationTokenExtractor.apply(continuableResponse);
            if(!isNullOrEmpty(nextContinuationToken)) {
                invokeRequest(nextContinuationToken);
            } else {
                // done - terminate queue consumer
                Uninterruptibles.putUninterruptibly(queue, poison);
            }
        });
    }

    @Override
    public Spliterator<CompletableFuture<ResponseT>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.IMMUTABLE;
    }
}
