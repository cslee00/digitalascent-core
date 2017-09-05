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


import com.amazonaws.services.lambda.runtime.Context;
import com.digitalascent.core.base.SimpleApplicationObject;

import java.util.function.BiFunction;

public abstract class AbstractAwsLamdbaFunction<I, O> extends SimpleApplicationObject implements BiFunction<I, Context, O> {

    @Override
    public O apply(I request, Context context) {
        return internalApply(request, context);
    }

    protected abstract O internalApply(I request, Context context);
}
