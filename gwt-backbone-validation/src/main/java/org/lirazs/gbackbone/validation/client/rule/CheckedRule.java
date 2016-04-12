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

import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.annotation.Checked;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class CheckedRule extends AnnotationRule<Checked, Boolean> {

    public CheckedRule(final Checked checked) {
        super(checked);
    }

    @Override
    public boolean isValid(final Boolean value, String attribute) {
        if (value == null) {
            throw new IllegalArgumentException("'data' cannot be null.");
        }
        return ruleAnnotation.value() == value;
    }

    @Override
    public String getMessage() {
        return ruleAnnotation.message();
    }
}
