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

package com.digitalascent.core.aws.lambda;


import com.digitalascent.core.base.SimpleApplicationObject;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public abstract class AbstractRequestHandler<I, O> extends SimpleApplicationObject implements RequestHandler<I, O> {
    @Override
    public final O handleRequest(I request, Context context) {
        Stopwatch sw = Stopwatch.createStarted();
        O retVal = internalHandleRequest(request, context);
        long elapsedMicros = sw.elapsed(TimeUnit.MICROSECONDS);
        getLogger().info("Executed {} {} in {}ms", context.getInvokedFunctionArn(), context.getAwsRequestId(), elapsedMicros / 1000.0);
        // TODO - feed metrics somewhere
        return retVal;
    }

    protected abstract O internalHandleRequest(I request, Context context);
}
