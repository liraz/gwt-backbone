/*
 * Copyright (C) 2016 Liraz Shilkrot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lirazs.gbackbone.validation.client.rule;

import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.validation.client.ValidationContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * A generic class for comparing values across two attributes.
 *
 * @see @ConfirmEmail
 * @see @ConfirmPassword
 *
 */
public class SameValueContextualRule<CONFIRM extends Annotation, SOURCE extends Annotation, DATA_TYPE>
        extends ContextualAnnotationRule<CONFIRM, DATA_TYPE> {
    private Class<SOURCE> mSourceClass;
    private Class<CONFIRM> mConfirmClass;

    protected SameValueContextualRule(final ValidationContext validationContext,
            final CONFIRM confirmAnnotation, final Class<SOURCE> sourceClass) {
        super(validationContext, confirmAnnotation);
        mSourceClass = sourceClass;
        mConfirmClass = (Class<CONFIRM>) confirmAnnotation.annotationType();
    }

    @Override
    public boolean isValid(final DATA_TYPE confirmValue, String attribute) {
        Set<String> sourceAttributes = mValidationContext.getAnnotatedAttributes(mSourceClass);
        int nSourceAttributes = sourceAttributes.size();

        if (nSourceAttributes == 0) {
            String message = StringUtils.format(
                    "You should have a attribute annotated with '%s' to use '%s'.",
                    mSourceClass.getName(), mConfirmClass.getName());
            throw new IllegalStateException(message);
        } else if (nSourceAttributes > 1) {
            String message = StringUtils.format(
                    "More than 1 field annotated with '%s'.", mSourceClass.getName());
            throw new IllegalStateException(message);
        }

        // There's only one, then we're good to go :)
        String sourceAttribute = sourceAttributes.iterator().next();

        Object sourceValue = mValidationContext.getData(sourceAttribute, mSourceClass);

        return confirmValue != null ? confirmValue.equals(sourceValue) : sourceValue == null;
    }
}
