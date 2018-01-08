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

package com.digitalascent.core.aws.lamdba.framework.json;

import com.amazonaws.services.lambda.runtime.Context;
import com.digitalascent.core.aws.lamdba.framework.ResponseHandler;
import com.digitalascent.core.aws.lamdba.framework.exception.InternalServerException;
import com.digitalascent.core.base.SimpleApplicationObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;

public final class JacksonObjectMapperResponseHandler<RequestType, ResponseType> extends SimpleApplicationObject implements ResponseHandler<RequestType, ResponseType> {
    public JacksonObjectMapperResponseHandler() {
    }

    @Override
    public void handle(RequestType request, ResponseType response, OutputStream outputStream, Context context, ObjectMapper objectMapper) throws InternalServerException {
        try {
            objectMapper.writeValue(outputStream, response);
        } catch (IOException e) {
            throw new InternalServerException(e);
        }
    }
}
