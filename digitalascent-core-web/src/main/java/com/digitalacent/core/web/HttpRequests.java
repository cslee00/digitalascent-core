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

package com.digitalacent.core.web;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public final class HttpRequests {

    @Nonnull
    public static Optional<String> headerValue(HttpServletRequest request, String headerName) {
        checkNotNull(request, "request is required");
        checkArgument(!isNullOrEmpty(headerName), "headerName is required to be non-null & not empty: %s", headerName);

        return Optional.ofNullable(request.getHeader(headerName));
    }

    /**
     * Returns the first header value from the supplied list of header names
     *
     * @param request
     * @param headerNames
     * @return
     */
    @Nonnull
    public static Optional<String> headerValue(HttpServletRequest request, String... headerNames) {
        checkNotNull(request, "request is required");
        checkNotNull(headerNames, "headerNames is required");

        return Arrays.stream(headerNames).map(request::getHeader).filter(Objects::nonNull).findFirst();
    }

    @Nonnull
    public static Stream<Cookie> cookies(HttpServletRequest request) {
        checkNotNull(request, "request is required");

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Stream.empty();
        }

        return Arrays.stream(cookies);
    }

    @Nonnull
    public static Optional<Cookie> cookie(HttpServletRequest request, String cookieName) {
        checkNotNull(request, "request is required");
        checkArgument(!isNullOrEmpty(cookieName), "headerName is required to be non-null & not empty: %s", cookieName);

        return cookies(request).filter(cookie -> Objects.equals(cookie.getName(), cookieName)).findFirst();
    }

    @Nonnull
    public static Optional<String> cookieValue(HttpServletRequest request, String cookieName) {
        checkNotNull(request, "request is required");
        checkArgument(!isNullOrEmpty(cookieName), "cookieName is required to be non-null & not empty: %s", cookieName);

        return cookie(request, cookieName).map(Cookie::getValue);
    }


    private HttpRequests() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
