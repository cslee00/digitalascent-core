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


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterMetadata;
import com.digitalascent.core.aws.lambda.AbstractRequestHandler;
import com.digitalascent.core.aws.ssm.AwsParameterStoreRepository;
import com.digitalascent.core.spring.PropertySources;
import com.google.common.base.Stopwatch;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * Bootstraps and invokes lambda function, providing:
 * -external configuration / secrets management from AWS Parameter Store;
 * -dependency injection (incl. external configuration) via Spring
 */
// TODO - switch to AWS SDK 2.x
public abstract class AbstractSpringLambdaBootstrapper<I, O> extends AbstractRequestHandler<I, O> {

    private final BiFunction<I, Context, O> lambdaFunction;

    @SuppressWarnings("unchecked")
    protected AbstractSpringLambdaBootstrapper() {
        Stopwatch sw = Stopwatch.createStarted();

        List<Class<?>> configurationClasses = registerConfigurationClasses();

        AnnotationConfigApplicationContext springContext = createSpringContext(configurationClasses);

        //noinspection unchecked
        lambdaFunction = springContext.getBean(BiFunction.class);

        getLogger().info("Environment for lambda {}: {}", lambdaFunction.getClass(), System.getenv());
        getLogger().info("Bootstrapped lambda {} in {}ms", lambdaFunction.getClass(), sw.elapsed(TimeUnit.MILLISECONDS));
        // TODO - emit custom metric w/ bootstrap time?
    }

    private List<Class<?>> registerConfigurationClasses() {
        List<Class<?>> configurationClasses = new ArrayList<>();
        configurationClasses.add( getClass() );
        registerAdditionalConfigurationClasses(configurationClasses);
        return configurationClasses;
    }

    private AnnotationConfigApplicationContext createSpringContext(List<Class<?>> configurationClasses) {
        AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext(LambdaBootstrapConfiguration.class);
        configurationClasses.forEach(springContext::register);
        configureEnvironmentFromParameterStore(springContext);
        springContext.refresh();
        return springContext;
    }

    protected abstract void registerAdditionalConfigurationClasses(List<Class<?>> configurationClasses );

    @Override
    protected final O internalHandleRequest(I request, Context context) {
        return lambdaFunction.apply(request, context);
    }

    private void configureEnvironmentFromParameterStore(ApplicationContext springContext) {

        // TODO - multiple prefixes, to pull in common params as well as service specific params?
        // TODO - pull this from environment variable?
        String paramStorePrefix = "/some/param/store/prefix";

        Map<String, Object> parameterMap = loadParameters(paramStorePrefix);
        MapPropertySource propertySource = new MapPropertySource("aws::parameter-store::" + paramStorePrefix, parameterMap);

        // SECURITY / RELIABILITY NOTE: we only want Lambda functions to have configuration pulled from Parameter Store; remove all other sources
        // to reduce risk of misconfiguration, reduce security risk surface area.
        PropertySources.setAsOnlyPropertySource(springContext, propertySource);

        getLogger().info("Loaded configuration parameters into Spring property store: {}", propertySource);
    }

    private Map<String, Object> loadParameters(String prefix) {
        getLogger().info("Loading from parameter store for prefix {}", prefix);

        AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
        AwsParameterStoreRepository parameterStoreService = new AwsParameterStoreRepository();

        DescribeParametersRequest describeParametersRequest = new DescribeParametersRequest();
        // TODO - filter params
        Set<String> parameterNames = parameterStoreService.describeParameters(describeParametersRequest, ssm).map(ParameterMetadata::getName).collect(toImmutableSet());

        GetParametersRequest getParametersRequest = new GetParametersRequest();
        getParametersRequest.withNames(parameterNames).withWithDecryption(true);
        GetParametersResult result = ssm.getParameters(getParametersRequest);
        Stream<Parameter> stream = result.getParameters().stream();
        // TODO - we should likely take the prefix off the name, allowing the root path to be externalized,
        // supporting multiple installs of the same code at different root prefixes.  Test what param store returns and decide.
        // sort prefixes descending by length, remove longest matching prefix
        return stream.collect(toImmutableMap(Parameter::getName, Parameter::getValue));
    }
}
