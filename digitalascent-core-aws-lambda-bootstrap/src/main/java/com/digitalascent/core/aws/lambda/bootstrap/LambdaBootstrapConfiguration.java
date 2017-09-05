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

package com.digitalascent.core.aws.lambda.bootstrap;

import com.digitalascent.core.spring.PropertySources;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;


@Configuration
class LambdaBootstrapConfiguration {
    @Bean
    public ApplicationListener<ContextRefreshedEvent> applicationListener() {
        // wipe out property sources to remove any sensitive information after it has been used
        // this will BREAK any prototype beans that may use properties; the proper pattern is to pull those properties
        // into a singleton configuration object and inject that where needed.  or don't use prototype beans (please!)
        return event -> PropertySources.removeAllPropertySources(event.getApplicationContext());
    }
}
