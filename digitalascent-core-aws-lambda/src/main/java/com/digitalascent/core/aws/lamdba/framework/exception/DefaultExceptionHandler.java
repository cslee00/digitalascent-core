/*
 * Copyright 2017-2018 the original author or authors.
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

package com.digitalascent.core.aws.lamdba.framework.exception;

import com.digitalascent.core.aws.lamdba.framework.ExceptionHandler;
import com.digitalascent.core.base.SimpleApplicationObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import java.io.OutputStream;

public final class DefaultExceptionHandler<Request> extends SimpleApplicationObject implements ExceptionHandler<Request> {
    @Override
    public void handle(Request request, Exception e, OutputStream outputStream, ObjectMapper objectMapper) {
        getLogger().error("Exception occurred", e);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }
}
