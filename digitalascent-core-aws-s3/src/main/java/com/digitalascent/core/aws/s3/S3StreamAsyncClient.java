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
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsRequest;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.digitalascent.core.base.LambdaCheckedExceptionRethrowers.rethrowingFunction;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class S3StreamAsyncClient {
    private final S3AsyncClient s3AsyncClient;
    private final int queueSize;

    public S3StreamAsyncClient(S3AsyncClient s3AsyncClient) {
        this( s3AsyncClient,5 );
    }

    public S3StreamAsyncClient(S3AsyncClient s3AsyncClient, int queueSize) {
        this.s3AsyncClient = checkNotNull(s3AsyncClient, "s3AsyncClient is required");
        checkArgument(queueSize > 0, "queueSize > 0 : %s", queueSize);
        this.queueSize = queueSize;
    }

    public S3AsyncClient s3AsyncClient() {
        return s3AsyncClient;
    }

    public Stream<ListPartsResponse> listParts(ListPartsRequest.Builder requestBuilder ) {
        checkNotNull(requestBuilder, "requestBuilder is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListPartsResponse previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.partNumberMarker(previousResponse != null ? previousResponse.nextPartNumberMarker() : null );

            return s3AsyncClient.listParts(requestBuilder.build());
        }, queueSize), false).map(rethrowingFunction(CompletableFuture::get));
    }

    public Stream<ListObjectsResponse> listObjects(ListObjectsRequest.Builder requestBuilder ) {
        checkNotNull(requestBuilder, "requestBuilder is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListObjectsResponse previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.marker(previousResponse != null ? previousResponse.nextMarker() : null );

            return s3AsyncClient.listObjects(requestBuilder.build());
        }, queueSize), false).map(rethrowingFunction(CompletableFuture::get));
    }

    public Stream<ListObjectVersionsResponse> listObjectVersions(ListObjectVersionsRequest.Builder requestBuilder ) {
        checkNotNull(requestBuilder, "requestBuilder is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListObjectVersionsResponse previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.keyMarker(previousResponse != null ? previousResponse.nextKeyMarker() : null );

            return s3AsyncClient.listObjectVersions(requestBuilder.build());
        }, queueSize), false).map(rethrowingFunction(CompletableFuture::get));
    }

    public Stream<ListMultipartUploadsResponse> listMultipartUploads(ListMultipartUploadsRequest.Builder requestBuilder ) {
        checkNotNull(requestBuilder, "requestBuilder is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListMultipartUploadsResponse previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.uploadIdMarker( previousResponse != null ? previousResponse.nextUploadIdMarker() : null );
            requestBuilder.keyMarker(previousResponse != null ? previousResponse.nextKeyMarker() : null );

            return s3AsyncClient.listMultipartUploads(requestBuilder.build());
        }, queueSize), false).map(rethrowingFunction(CompletableFuture::get));
    }

    public Stream<ListObjectsV2Response> listObjectsV2(ListObjectsV2Request.Builder requestBuilder) {
        checkNotNull(requestBuilder, "requestBuilder is required");

        return StreamSupport.stream(new ContinuationTokenSpliterator<>((ListObjectsV2Response previousResponse) -> {
            if (previousResponse != null && !previousResponse.isTruncated()) {
                // response is complete, no need to make further requests
                return null;
            }
            // set new continuation token (possibly null, in the case of the first request)
            requestBuilder.continuationToken(previousResponse != null ? previousResponse.nextContinuationToken() : null );

            return s3AsyncClient.listObjectsV2(requestBuilder.build());
        }, queueSize), false).map(rethrowingFunction(CompletableFuture::get));
    }
}
