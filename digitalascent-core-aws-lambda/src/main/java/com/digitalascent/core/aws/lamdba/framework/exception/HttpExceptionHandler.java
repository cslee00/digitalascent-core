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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import java.io.OutputStream;
import java.util.Map;

public class HttpExceptionHandler<Request> implements ExceptionHandler<Request> {
    private final Map<Class<? extends Throwable>,Integer> exceptionStatusCodeMap = ImmutableMap.of( BadRequestException.class, 400, InternalServerException.class, 500, UnprocessableEntityException.class, 422 );
    @Override
    public void handle(Request request, Exception e, OutputStream outputStream, ObjectMapper objectMapper) {
        int statusCode = exceptionStatusCodeMap.getOrDefault(e.getClass(),500);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }
}
