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

@SuppressWarnings("unused")
public final class Identity extends DynamicJsonObject {
    private String cognitoIdentityPoolId;
    private String accountId;
    private String cognitoIdentityId;
    private String caller;
    private String apiKey;
    private String sourceIp;
    private String cognitoAuthenticationType;
    private String cognitoAuthenticationProvider;
    private String userArn;
    private String userAgent;
    private String user;

    public String getCognitoIdentityPoolId() {
        return cognitoIdentityPoolId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCognitoIdentityId() {
        return cognitoIdentityId;
    }

    public String getCaller() {
        return caller;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getCognitoAuthenticationType() {
        return cognitoAuthenticationType;
    }

    public String getCognitoAuthenticationProvider() {
        return cognitoAuthenticationProvider;
    }

    public String getUserArn() {
        return userArn;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cognitoIdentityPoolId", cognitoIdentityPoolId)
                .add("accountId", accountId)
                .add("cognitoIdentityId", cognitoIdentityId)
                .add("caller", caller)
                .add("apiKey", apiKey)
                .add("sourceIp", sourceIp)
                .add("cognitoAuthenticationType", cognitoAuthenticationType)
                .add("cognitoAuthenticationProvider", cognitoAuthenticationProvider)
                .add("userArn", userArn)
                .add("userAgent", userAgent)
                .add("user", user)
                .add( "additionalProperties", getAdditionalProperties() )
                .toString();
    }
}
