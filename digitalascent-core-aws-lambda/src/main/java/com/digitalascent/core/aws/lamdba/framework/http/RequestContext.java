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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@SuppressWarnings("unused")
public final class RequestContext extends DynamicJsonObject {
    @Pattern(regexp = "[0-9]{12}")
    private String accountId;

    @NotBlank
    private String resourceId;

    @NotBlank
    private String stage;

    @Pattern(regexp = "[a-f0-9\\-]{20,}")
    private String requestId;

    @Pattern(regexp = "^/.*")
    private String resourcePath;

    @NotNull
    private HttpMethod httpMethod;

    private Identity identity;
    private Authorizer authorizer;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("resourceId", resourceId)
                .add("stage", stage)
                .add("requestId", requestId)
                .add("resourcePath", resourcePath)
                .add("httpMethod", httpMethod)
                .add("identity", identity)
                .add("authorizer", authorizer)
                .add("additionalProperties", getAdditionalProperties())
                .toString();
    }

    public Identity getIdentity() {
        return identity;
    }

    public Authorizer getAuthorizer() {
        return authorizer;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getStage() {
        return stage;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
}
