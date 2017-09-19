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

import com.digitalascent.core.base.collect.ContinuationTokenSpliterator;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.digitalascent.core.base.LambdaCheckedExceptionRethrowers.rethrowingFunction;
import static com.google.common.base.Preconditions.checkNotNull;

public final class S3Streams {

    public static Stream<ListObjectsV2Response> listObjectsV2(ListObjectsV2Request.Builder requestBuilder, S3AsyncClient s3Client) {
        checkNotNull(requestBuilder, "requestBuilder is required");
        checkNotNull(s3Client, "s3Client is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListObjectsV2Response previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.continuationToken(previousResponse != null ? previousResponse.nextContinuationToken() : null );

            return s3Client.listObjectsV2(requestBuilder.build());
        }, 5), false).map(rethrowingFunction(CompletableFuture::get));
    }

    public static Stream<String> listCommonPrefixesStream(ListObjectsV2Request.Builder builder, S3AsyncClient s3Client) {
        return listObjectsV2(builder, s3Client).flatMap(response -> response.commonPrefixes().stream()).map(CommonPrefix::prefix);
    }

    private S3Streams() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}