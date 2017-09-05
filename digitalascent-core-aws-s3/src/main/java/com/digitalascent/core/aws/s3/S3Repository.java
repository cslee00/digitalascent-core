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

package com.digitalascent.core.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.digitalascent.core.base.collect.Batch;
import com.digitalascent.core.base.collect.MoreStreams;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class S3Repository {

    Stream<String> listRootPrefixes( String bucketName, String delimiter, AmazonS3 s3 ) {
        ListObjectsV2Request request = new ListObjectsV2Request();
        request.setDelimiter(delimiter);
        request.setBucketName(bucketName);
        request.setMaxKeys(1000);

        return MoreStreams.batchLoadingStream(nextToken -> {
           request.setContinuationToken(nextToken);
           ListObjectsV2Result result = s3.listObjectsV2(request);
           return new Batch<>(result.getNextContinuationToken(), result.getCommonPrefixes());
        });
    }
}
