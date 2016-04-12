/*
 * Copyright 2016, Liraz Shilkrot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.lirazs.gbackbone.client.core.validation;

import org.lirazs.gbackbone.reflection.client.Reflectable;

/**
 * Created on 05/02/2016.
 */
@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        //TODO: IMPORTANT - assignable constructors - if true all assigning classes will generate constructors !!
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public interface Rule<VALIDATABLE> {

    /**
     * Checks if the rule is valid.
     *
     * @param validatable  Element on which the validation is applied, could be a data type or a View.
     *
     * @return true if valid, false otherwise.
     */
    boolean isValid(VALIDATABLE validatable, String attribute);

    /**
     * Returns a failure message associated with the rule.
     *
     * @return A failure message.
     */
    String getMessage();
}
