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

package com.digitalascent.core.spring;


import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public final class PropertySources {

    public static MutablePropertySources getMutablePropertySources(ApplicationContext context) {
        checkNotNull(context);
        ConfigurableEnvironment environment = (ConfigurableEnvironment) context.getEnvironment();
        return environment.getPropertySources();
    }

    public static void removeAllPropertySources(ApplicationContext context ) {
        checkNotNull(context);
        MutablePropertySources propertySources = getMutablePropertySources(context);
        removeAllPropertySources( propertySources );
    }

    private static void removeAllPropertySources(MutablePropertySources propertySources ) {
        checkNotNull( propertySources );
        Set<String> propertySourceNames = new HashSet<>();
        propertySources.forEach( propertySource -> propertySourceNames.add(propertySource.getName()) );
        propertySourceNames.forEach(propertySources::remove);
    }

    public static <T> void  setAsOnlyPropertySource(ApplicationContext context, PropertySource<T> propertySource ) {
        checkNotNull(context);
        checkNotNull(propertySource);
        MutablePropertySources propertySources = getMutablePropertySources(context);

        removeAllPropertySources( propertySources );

        propertySources.addFirst(propertySource);
    }


    private PropertySources() {
        throw new AssertionError( "Cannot instantiate " + getClass() );
    }
}
