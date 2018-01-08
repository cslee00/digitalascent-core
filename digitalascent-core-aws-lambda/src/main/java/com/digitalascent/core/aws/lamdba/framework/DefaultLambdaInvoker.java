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

package com.digitalascent.core.aws.lamdba.framework;

import com.amazonaws.services.lambda.runtime.Context;
import com.digitalascent.core.aws.lamdba.framework.exception.InternalServerException;
import com.digitalascent.core.base.SimpleApplicationObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;

final class DefaultLambdaInvoker<Request, Response> extends SimpleApplicationObject implements LambdaInvoker {
    private final RequestParser<Request> requestParser;
    private final ResponseHandler<Request, Response> responseHandler;
    private final RequestValidator requestValidator;
    private final ExceptionHandler<Request> exceptionHandler;
    private final Class<Request> requestClass;
    private final LambdaRequestHandler<Request, Response> requestHandler;
    private final ObjectMapper objectMapper;

    DefaultLambdaInvoker(Class<Request> requestClass, ObjectMapper objectMapper, RequestParser<Request> requestParser,
                         RequestValidator requestValidator,
                         ResponseHandler<Request, Response> responseHandler,
                         ExceptionHandler<Request> exceptionHandler,
                         LambdaRequestHandler<Request, Response> requestHandler) {
        this.requestClass = checkNotNull(requestClass, "requestClass is required");
        this.objectMapper = checkNotNull(objectMapper, "objectMapper is required");
        this.requestParser = checkNotNull(requestParser, "requestParser is required");
        this.responseHandler = checkNotNull(responseHandler, "responseHandler is required");
        this.requestValidator = checkNotNull(requestValidator, "requestValidator is required");
        this.exceptionHandler = checkNotNull(exceptionHandler, "exceptionHandler is required");
        this.requestHandler = checkNotNull(requestHandler, "requestHandler is required");
    }

    public void invoke(InputStream inputStream, OutputStream outputStream, Context context) {
        Request request = null;
        try {
            if (!Void.class.isAssignableFrom(requestClass)) {
                if( getLogger().isDebugEnabled()) {
                    inputStream = debugInput( inputStream );
                }
                request = requestParser.parse(inputStream, context);
                requestValidator.validate(request);
            }

            try {
                Response response = requestHandler.handler(request, context);
                responseHandler.handle(request, response, outputStream, context, objectMapper);
            } catch (Exception e) {
                throw new InternalServerException(e);
            }
        } catch (Exception e) {
            getLogger().debug("Exception occurred, passing to exception handler", e);
            exceptionHandler.handle(request, e, outputStream, objectMapper );
        }
    }

    private InputStream debugInput(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
        ByteStreams.copy(inputStream, outputStream);
        getLogger().debug("Incoming request: {}", new String( outputStream.toByteArray(), StandardCharsets.UTF_8));
        return new ByteArrayInputStream( outputStream.toByteArray());
    }
}
