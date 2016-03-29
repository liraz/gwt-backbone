/*
 * Copyright (C) 2015 Mobs & Geeks
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

/**
 * A generic class for comparing values across two targets.
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
    public boolean isValid(final DATA_TYPE confirmValue) {
        List<Object> sourceTargets = mValidationContext.getAnnotatedTargets(mSourceClass);
        int nSourceViews = sourceTargets.size();

        if (nSourceViews == 0) {
            String message = StringUtils.format(
                    "You should have a view annotated with '%s' to use '%s'.",
                    mSourceClass.getName(), mConfirmClass.getName());
            throw new IllegalStateException(message);
        } else if (nSourceViews > 1) {
            String message = StringUtils.format(
                    "More than 1 field annotated with '%s'.", mSourceClass.getName());
            throw new IllegalStateException(message);
        }

        // There's only one, then we're good to go :)
        Object target = sourceTargets.get(0);
        Object sourceValue = mValidationContext.getData(target, mSourceClass);

        return confirmValue != null ? confirmValue.equals(sourceValue) : sourceValue == null;
    }
}
