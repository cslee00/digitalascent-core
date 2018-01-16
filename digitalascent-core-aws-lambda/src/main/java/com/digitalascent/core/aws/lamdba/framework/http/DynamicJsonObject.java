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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Superclass that allows Jackson to place any unresolved properties into the 'additionalProperties' field.
 */
abstract class DynamicJsonObject {
    @JsonAnySetter
    private Map<String, String> additionalProperties = ImmutableMap.of();

    public Map<String, String> getAdditionalProperties() {
        return ImmutableMap.copyOf(additionalProperties);
    }
}
