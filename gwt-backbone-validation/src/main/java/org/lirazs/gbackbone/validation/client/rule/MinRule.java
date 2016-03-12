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

import org.lirazs.gbackbone.validation.client.annotation.Min;


public class MinRule extends AnnotationRule<Min, Integer> {

    protected MinRule(final Min min) {
        super(min);
    }

    @Override
    public boolean isValid(final Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("'Integer' cannot be null.");
        }
        int minValue = ruleAnnotation.value();
        return value >= minValue;
    }
}
