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

package com.digitalascent.core.aws.lamdba.framework.validation;

import com.digitalascent.core.aws.lamdba.framework.RequestValidator;
import com.digitalascent.core.aws.lamdba.framework.exception.UnprocessableEntityException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public final class DefaultRequestValidator implements RequestValidator {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Override
    public void validate(Object request) {

        Set<ConstraintViolation<Object>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            // TODO - propagate violations
            throw new UnprocessableEntityException("TODO - pull violations into here");
/*            List<ConstraintViolationDescription> errors = new ArrayList<>();

            ConstraintViolationResponseError error = new ConstraintViolationResponseError();
            error.setMessage(UNPROCESSABLE_ENTITY_MESSAGE);

            for (ConstraintViolation<Object> violation : violations) {
                String attribute = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.add(new ConstraintViolationDescription(message, attribute));
            }

            error.setErrors(errors);
            throw new UnprocessableEntityException(error);*/
        }
    }
}
