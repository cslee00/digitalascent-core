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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/*
 * http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/crpg-ref-responses.html
 */
@SuppressWarnings("unused")
public final class CustomResourceResponse {
    private final String stackId;
    private final String requestId;
    private final String logicalResourceId;
    private CustomResourceResponseStatus status = CustomResourceResponseStatus.FAILED;
    private String reason;
    private String physicalResourceId;
    private boolean noEcho;

    public static CustomResourceResponse create(CustomResourceRequest request) {
        checkNotNull(request, "request is required");

        return new CustomResourceResponse(request.getRequestId(), request.getLogicalResourceId(),request.getStackId());
    }

    private CustomResourceResponse(String requestId, String logicalResourceId, String stackId) {
        checkArgument(!isNullOrEmpty(requestId), "requestId is required to be non-null & not empty: %s", requestId);
        checkArgument(!isNullOrEmpty(logicalResourceId), "logicalResourceId is required to be non-null & not empty: %s", logicalResourceId);
        checkArgument(!isNullOrEmpty(stackId), "stackId is required to be non-null & not empty: %s", stackId);

        this.requestId = requestId;
        this.logicalResourceId = logicalResourceId;
        this.stackId = stackId;
    }

    public String getStackId() {
        return stackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getLogicalResourceId() {
        return logicalResourceId;
    }

    public CustomResourceResponseStatus getStatus() {
        return status;
    }

    public void setStatus(CustomResourceResponseStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPhysicalResourceId() {
        return physicalResourceId;
    }

    public void setPhysicalResourceId(String physicalResourceId) {
        this.physicalResourceId = physicalResourceId;
    }

    public boolean isNoEcho() {
        return noEcho;
    }

    public void setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stackId", stackId)
                .add("requestId", requestId)
                .add("logicalResourceId", logicalResourceId)
                .add("status", status)
                .add("reason", reason)
                .add("physicalResourceId", physicalResourceId)
                .add("noEcho", noEcho)
                .toString();
    }
}
