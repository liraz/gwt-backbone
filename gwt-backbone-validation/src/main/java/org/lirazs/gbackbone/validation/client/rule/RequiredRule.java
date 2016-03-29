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


import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.ValidationContext;
import org.lirazs.gbackbone.validation.client.annotation.Required;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class RequiredRule extends ContextualAnnotationRule<Required, String> {

    public RequiredRule(final ValidationContext validationContext, final Required required) {
        super(validationContext, required);
    }

    @Override
    public boolean isValid(final String data) {
        boolean isEmpty = false;
        if (data != null) {
            String text = ruleAnnotation.trim() ? data.trim() : data;

            String emptyText = ruleAnnotation.emptyText();
            isEmpty = emptyText.equals(text) || "".equals(text);
        }

        return !isEmpty;
    }
}
