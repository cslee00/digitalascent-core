/*
 * Copyright 2017-2017 the original author or authors.
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

package com.digitalascent.core.aws.ssm;


import com.digitalascent.core.base.SimpleApplicationObject;

public final class AwsParameterStoreRepository extends SimpleApplicationObject {

/*    public Stream<ParameterMetadata> describeParameters(DescribeParametersRequest describeParametersRequest, AWSSimpleSystemsManagement ssm) {
        checkNotNull(describeParametersRequest, "describeParametersRequest is required");
        checkNotNull(ssm, "ssm is required");

        return MoreStreams.batchLoadingStream(nextToken -> {
            describeParametersRequest.setNextToken(nextToken);
            DescribeParametersResult result = ssm.describeParameters(describeParametersRequest);
            return new Batch<>( result.getNextToken(),result.getParameters() );
        });
    }*/
}
