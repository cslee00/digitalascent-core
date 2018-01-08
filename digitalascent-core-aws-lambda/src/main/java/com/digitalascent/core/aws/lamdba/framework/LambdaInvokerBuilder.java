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

import com.digitalascent.core.aws.lamdba.framework.exception.DefaultExceptionHandler;
import com.digitalascent.core.aws.lamdba.framework.json.JacksonObjectMapperRequestParser;
import com.digitalascent.core.aws.lamdba.framework.json.JacksonObjectMapperResponseHandler;
import com.digitalascent.core.aws.lamdba.framework.validation.ChainedRequestValidator;
import com.digitalascent.core.aws.lamdba.framework.validation.DefaultRequestValidator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class LambdaInvokerBuilder<Request, Response> {

    private ObjectMapperCustomizer objectMapperCustomizer = (objectMapper) -> {
    };
    private RequestParser<Request> requestParser;
    private ResponseHandler<Request, Response> responseHandler;
    private ExceptionHandler<Request> exceptionHandler;
    private final List<RequestValidator> requestValidators = new ArrayList<>();

    public LambdaInvoker build(LambdaRequestHandler<Request, Response> requestHandler) {
        Preconditions.checkNotNull(requestHandler, "requestHandler is required");

        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();

        @SuppressWarnings("unchecked")
        Class<Request> requestClass = (Class<Request>) type.getActualTypeArguments()[0];

        ObjectMapper objectMapper = createObjectMapper();

        if (requestParser == null) {
            requestParser = new JacksonObjectMapperRequestParser<>(objectMapper, requestClass);
        }
        if (requestValidators.isEmpty()) {
            requestValidators.add(new DefaultRequestValidator());
        }
        if (responseHandler == null) {
            responseHandler = new JacksonObjectMapperResponseHandler<>();
        }
        if (exceptionHandler == null) {
            exceptionHandler = new DefaultExceptionHandler<>();
        }

        return new DefaultLambdaInvoker<>(requestClass, objectMapper, requestParser, new ChainedRequestValidator(requestValidators), responseHandler, exceptionHandler, requestHandler);
    }

    public LambdaInvokerBuilder<Request, Response> withRequestParser(RequestParser<Request> requestParser) {
        this.requestParser = Preconditions.checkNotNull(requestParser, "requestParser is required");
        return this;
    }

    public LambdaInvokerBuilder<Request, Response> withResponseHandler(ResponseHandler<Request, Response> responseHandler) {
        this.responseHandler = Preconditions.checkNotNull(responseHandler, "responseHandler is required");
        return this;
    }

    public LambdaInvokerBuilder<Request, Response> withExceptionHandler(ExceptionHandler<Request> exceptionHandler) {
        this.exceptionHandler = Preconditions.checkNotNull(exceptionHandler, "exceptionHandler is required");
        return this;
    }

    public LambdaInvokerBuilder<Request, Response> addRequestValidator(RequestValidator requestValidator) {
        Preconditions.checkNotNull(requestValidator, "requestValidator is required");

        if (requestValidators.isEmpty()) {
            requestValidators.add(new DefaultRequestValidator());
        }
        requestValidators.add(requestValidator);
        return this;
    }

    public LambdaInvokerBuilder<Request, Response> withObjectMapperCustomizer(ObjectMapperCustomizer objectMapperCustomizer) {
        this.objectMapperCustomizer = checkNotNull(objectMapperCustomizer, "objectMapperCustomizer is required");
        return this;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // deserialization options
        objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

        // serialization options
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapperCustomizer.customize(objectMapper);
        return objectMapper;
    }
}
