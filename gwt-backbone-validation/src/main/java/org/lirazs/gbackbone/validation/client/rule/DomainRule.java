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


import org.apache.commons.validator.routines.DomainValidator;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.annotation.Domain;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class DomainRule extends AnnotationRule<Domain, String> {

    public DomainRule(final Domain domain) {
        super(domain);
    }

    @Override
    public boolean isValid(final String domain, String attribute) {
        boolean allowLocal = ruleAnnotation.allowLocal();
        DomainValidator domainValidator = DomainValidator.getInstance(allowLocal);
        return domainValidator.isValid(domain);
    }
}
