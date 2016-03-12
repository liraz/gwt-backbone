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

import org.lirazs.gbackbone.validation.client.annotation.Select;


public class SelectRule extends AnnotationRule<Select, Integer> {

    protected SelectRule(final Select select) {
        super(select);
    }

    @Override
    public boolean isValid(final Integer index) {
        if (index == null) {
            throw new IllegalArgumentException("'index' cannot be null.");
        }
        return ruleAnnotation.defaultSelection() != index;
    }
}
