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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.digitalascent.core.aws.lamdba.framework.AbstractLambdaRequestHandler;
import com.digitalascent.core.aws.lamdba.framework.LambdaInvokerBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.net.MediaType;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("unused")
public abstract class CustomResourceLambdaRequestHandler extends AbstractLambdaRequestHandler<CustomResourceRequest,CustomResourceResponse> {

    @SuppressWarnings("FeatureEnvy")
    @Override
    protected final void customizeBuilder(LambdaInvokerBuilder<CustomResourceRequest, CustomResourceResponse> builder) {
        builder.withObjectMapperCustomizer(objectMapper -> {
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        });

        builder.withResponseHandler(((request, response, outputStream, context, objectMapper) -> writeResponse(request, response, objectMapper)));

        builder.withExceptionHandler(((request, e, outputStream, objectMapper) -> {
            CustomResourceResponse response = CustomResourceResponse.create(request);
            response.setStatus(CustomResourceResponseStatus.FAILED);
            response.setReason(e.getMessage());
            writeResponse(request,response, objectMapper);
        }));
    }

    @Override
    public final CustomResourceResponse handler(CustomResourceRequest request, Context context) {
        CustomResourceResponse response = CustomResourceResponse.create(request);
        handleCustomResource( request, response, context );
        return response;
    }

    protected abstract void handleCustomResource(CustomResourceRequest request, CustomResourceResponse response, Context context);


    private void writeResponse(CustomResourceRequest request, CustomResourceResponse response, ObjectMapper objectMapper) {

        getLogger().info("Writing {} to {}", response, request.getResponseURL());

        int timeout = 1;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        try {
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

                HttpPost httpPost = new HttpPost(request.getResponseURL());
                httpPost.setHeader("Content-Type", MediaType.JSON_UTF_8.toString());

                String body = objectMapper.writeValueAsString(response);
                httpPost.setEntity(new StringEntity(body));

                try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                    getLogger().info("Response status {}, headers {}", httpResponse.getStatusLine(), Arrays.toString(httpResponse.getAllHeaders()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
