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

package com.digitalascent.core.aws.lamdba.framework.http;

import com.digitalascent.core.aws.lamdba.framework.ExceptionHandler;
import com.digitalascent.core.aws.lamdba.framework.exception.BadRequestException;
import com.digitalascent.core.aws.lamdba.framework.exception.InternalServerException;
import com.digitalascent.core.aws.lamdba.framework.exception.UnprocessableEntityException;
import com.digitalascent.core.base.SimpleApplicationObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@SuppressWarnings("unused")
public class HttpExceptionHandler extends SimpleApplicationObject implements ExceptionHandler<HttpRequest> {
    private final Map<Class<? extends Exception>, Integer> exceptionStatusCodeMap = ImmutableMap.of(BadRequestException.class, 400,
            InternalServerException.class, 500, UnprocessableEntityException.class, 422);

    @Override
    public void handle(HttpRequest request, Exception e, OutputStream outputStream, ObjectMapper objectMapper) {
        try {
            getLogger().error("{}", request.getPath(), e);
            HttpResponse response = new HttpResponse();
            response.setStatusCode(exceptionStatusCodeMap.getOrDefault(e.getClass(), 500));
            // deliberately omit exception detail from response body for security reasons, to not expose internal details / validation mechanisms
            objectMapper.writeValue(outputStream, response);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}
