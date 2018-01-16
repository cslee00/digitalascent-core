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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

/*
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/set-up-lambda-proxy-integrations.html#api-gateway-simple-proxy-for-lambda-input-format
 */
@SuppressWarnings("unused")
public class HttpRequest extends DynamicJsonObject {

    @Pattern(regexp = "^/.*")
    private String resource;

    @Pattern(regexp = "^/.*")
    private String path;

    @NotNull
    private HttpMethod httpMethod;

    private Map<String, String> headers = ImmutableMap.of();
    private Map<String, String> queryStringParameters = ImmutableMap.of();
    private Map<String, String> pathParameters = ImmutableMap.of();
    private Map<String, String> stageVariables = ImmutableMap.of();

    @NotNull
    private RequestContext requestContext;
    private String body;
    private boolean base64Encoded;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("resource", resource)
                .add("path", path)
                .add("httpMethod", httpMethod)
                .add("headers", headers)
                .add("queryStringParameters", queryStringParameters)
                .add("pathParameters", pathParameters)
                .add("stageVariables", stageVariables)
                .add("requestContext", requestContext)
                .add("body", body)
                .add("base64Encoded", base64Encoded)
                .add("additionalProperties", getAdditionalProperties())
                .toString();
    }

    public String getResource() {
        return resource;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String> getHeaders() {
        return ImmutableMap.copyOf(headers);
    }

    public Map<String, String> getQueryStringParameters() {
        return ImmutableMap.copyOf(queryStringParameters);
    }

    public Map<String, String> getPathParameters() {
        return ImmutableMap.copyOf(pathParameters);
    }

    public Map<String, String> getStageVariables() {
        return ImmutableMap.copyOf(stageVariables);
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public String getBody() {
        return body;
    }

    public boolean isBase64Encoded() {
        return base64Encoded;
    }
}
