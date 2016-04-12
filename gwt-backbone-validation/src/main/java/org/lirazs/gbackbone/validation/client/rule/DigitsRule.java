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
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.annotation.Digits;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class DigitsRule extends AnnotationRule<Digits, String> {

    public DigitsRule(final Digits digits) {
        super(digits);
    }

    @Override
    public boolean isValid(final String digits, String attribute) {
        int integer = ruleAnnotation.integer();
        int fraction = ruleAnnotation.fraction();

        String digitsRegex = StringUtils.format("(\\d{0,%d})(\\.\\d{1,%d})?", integer, fraction);
        return digits.matches(digitsRegex);
    }
}
