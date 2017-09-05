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

package com.digitalascent.core.base.concurrent;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps an executor, storing futures for executed tasks & waiting until all tasks have completed.
 */
public final class AsyncExecutionGroup {
    private final ListeningExecutorService executorService;
    private final List<ListenableFuture<?>> futures = new ArrayList<>();

    public AsyncExecutionGroup(ListeningExecutorService executorService) {
        this.executorService = checkNotNull(executorService);
    }

    public <T> ListenableFuture<T> execute(Callable<T> callable) {
        ListenableFuture<T> future = executorService.submit(callable);
        futures.add(future);
        return future;
    }

    public void execute(Runnable runnable) {
        ListenableFuture<?> future = executorService.submit(runnable);
        futures.add(future);
    }

    public <T> T whenAllSucceed(Callable<T> c) {
        try {
            return Uninterruptibles.getUninterruptibly(Futures.whenAllSucceed(futures).call(c, MoreExecutors.directExecutor()));
        } catch (ExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }
}
