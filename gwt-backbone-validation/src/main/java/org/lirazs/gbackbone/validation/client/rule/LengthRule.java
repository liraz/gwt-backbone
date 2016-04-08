/*
 * Copyright (C) 2014 Mobs & Geeks
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
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.annotation.Length;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class LengthRule extends AnnotationRule<Length, String> {

    public LengthRule(final Length length) {
        super(length);
    }

    @Override
    public boolean isValid(final String text, String attribute) {
        /*if (text == null) {
            throw new IllegalArgumentException("'text' cannot be null.");
        }*/
        int ruleMin = ruleAnnotation.min();
        int ruleMax = ruleAnnotation.max();

        // Assert min is <= max
        assertMinMax(ruleMin, ruleMax);

        // Trim?
        int length = text != null ? (ruleAnnotation.trim() ? text.trim().length() : text.length()) : 0;

        // Check for min length
        boolean minIsValid = true;
        if (ruleMin != Integer.MIN_VALUE) { // Min is set
            minIsValid = length >= ruleMin;
        }

        // Check for max length
        boolean maxIsValid = true;
        if (ruleMax != Integer.MAX_VALUE) { // Max is set
            maxIsValid = length <= ruleMax;
        }

        return minIsValid && maxIsValid;
    }

    private void assertMinMax(int min, int max) {
        if (min > max) {
            String message = StringUtils.format(
                    "'min' (%d) should be less than or equal to 'max' (%d).", min, max);
            throw new IllegalStateException(message);
        }
    }

    @Override
    public String getMessage() {
        return ruleAnnotation.message();
    }
}
