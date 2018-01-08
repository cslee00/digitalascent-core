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
import com.digitalascent.core.aws.lamdba.framework.RequestParser;
import com.digitalascent.core.aws.lamdba.framework.exception.BadRequestException;
import com.digitalascent.core.aws.lamdba.framework.exception.InternalServerException;
import com.digitalascent.core.base.SimpleApplicationObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JacksonObjectMapperRequestParser<T> extends SimpleApplicationObject implements RequestParser<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> requestClass;

    public JacksonObjectMapperRequestParser(ObjectMapper objectMapper, Class<T> requestClass) {
        this.objectMapper = checkNotNull(objectMapper, "objectMapper is required");
        this.requestClass = checkNotNull(requestClass, "requestClass is required");
    }

    @Override
    public T parse(InputStream is, Context context) throws BadRequestException, InternalServerException {
        try {
            return objectMapper.readValue(is, requestClass);
        } catch (JsonParseException e) {
            throw new BadRequestException(e);
        } catch (UnrecognizedPropertyException e) {
            throw new BadRequestException("Unrecognized property: " + e.getPropertyName(), e);
        } catch (JsonMappingException e) {
            throw new BadRequestException(e);
        } catch (Exception e) {
            throw new InternalServerException(e);
        }
    }
}
