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

package com.digitalascent.aws;

import software.amazon.awssdk.auth.AwsCredentialsProvider;
import software.amazon.awssdk.auth.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.ElasticContainerCredentialsProvider;
import software.amazon.awssdk.auth.InstanceProfileCredentialsProvider;

public final class AwsUtils {

    /**
     * Provides a credentials provider that only checks managed credential stores, specifically EC2 instance profiles and ECS container credentials.
     *
     * Preferred (for security) over default provider chains that also check environment variables, system properties, local files, etc. - locations that could
     * be compromised
     *
     */
    public static AwsCredentialsProvider managedCredentialsProvider() {
        AwsCredentialsProvider[] credentialsProviders = new AwsCredentialsProvider[] {
                ElasticContainerCredentialsProvider.builder()
                        .build(),
                InstanceProfileCredentialsProvider.builder()
                        .build()
        };

        return AwsCredentialsProviderChain.builder()
                .reuseLastProviderEnabled(true)
                .credentialsProviders(credentialsProviders)
                .build();
    }

    private AwsUtils() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
