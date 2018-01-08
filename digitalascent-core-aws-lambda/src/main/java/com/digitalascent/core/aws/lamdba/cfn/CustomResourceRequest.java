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

package com.digitalascent.core.aws.lamdba.cfn;


import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
public final class CustomResourceRequest {
    private CustomResourceRequestType requestType;
    private String responseURL;
    private String requestId;
    private String resourceType;
    private String logicalResourceId;
    private String stackId;
    private Map<String, Object> resourceProperties = ImmutableMap.of();
    private Map<String, Object> oldResourceProperties = ImmutableMap.of();

    public boolean isCreateRequest() {
        return requestType == CustomResourceRequestType.CREATE;
    }

    public boolean isUpdateRequest() {
        return requestType == CustomResourceRequestType.UPDATE;
    }

    public boolean isDeleteRequest() {
        return requestType == CustomResourceRequestType.DELETE;
    }

    public String getResponseURL() {
        return responseURL;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getLogicalResourceId() {
        return logicalResourceId;
    }
    public String getStackId() {
        return stackId;
    }

    public Map<String, Object> getResourceProperties() {
        return ImmutableMap.copyOf(resourceProperties);
    }

    public Map<String, Object> getOldResourceProperties() {
        return ImmutableMap.copyOf(oldResourceProperties);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("requestType", requestType)
                .add("responseURL", responseURL)
                .add("requestId", requestId)
                .add("resourceType", resourceType)
                .add("logicalResourceId", logicalResourceId)
                .add("stackId", stackId)
                .add("resourceProperties", resourceProperties)
                .add("oldResourceProperties", oldResourceProperties)
                .toString();
    }
}
