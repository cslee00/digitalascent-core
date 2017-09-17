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

import com.digitalascent.core.base.collect.Batch;
import com.digitalascent.core.base.collect.MoreStreams;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.stream.Stream;

public final class S3Utils {

    Stream<String> listRootPrefixes(String bucketName, String delimiter, S3Client s3Client) {

        return MoreStreams.batchLoadingStream(nextToken -> {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .delimiter(delimiter)
                    .maxKeys(1000)
                    .bucket(bucketName)
                    .continuationToken(nextToken)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return new Batch<>(response.nextContinuationToken(), response.commonPrefixes());
        }).map(CommonPrefix::prefix);
    }

    private S3Utils() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
