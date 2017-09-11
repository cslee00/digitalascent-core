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
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public final class HttpSessions {

    @Nonnull
    public static <T> Optional<T> attribute(HttpServletRequest request, String attributeName, Class<T> type ) {
        return attribute( request.getSession(false), attributeName, type );
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> Optional<T> attribute(@Nullable HttpSession session, String attributeName, Class<T> type ) {
        checkArgument(!isNullOrEmpty(attributeName), "attributeName is required to be non-null & not empty: %s", attributeName);
        checkNotNull(type, "type is required");

        if( session == null ) {
            return Optional.empty();
        }

        Object val = session.getAttribute(attributeName);
        if( val == null ) {
            return Optional.empty();
        }

        if( !Objects.equals(type, val.getClass())) {
            throw new IllegalStateException(String.format("Expected type '%s', got type '%s' for session attribute '%s'", type, val.getClass(), attributeName));
        }
        return Optional.of((T)val);
    }

    private HttpSessions() {
        throw new AssertionError("Cannot instantiate " + getClass());
    }
}
