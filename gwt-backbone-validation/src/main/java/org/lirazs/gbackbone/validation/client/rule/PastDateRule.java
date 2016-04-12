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

import com.google.gwt.i18n.client.DateTimeFormat;
import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.ValidationContext;
import org.lirazs.gbackbone.validation.client.annotation.PastDate;

import java.util.Date;

@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class PastDateRule extends ContextualAnnotationRule<PastDate, String> {

    public PastDateRule(final ValidationContext validationContext, final PastDate past) {
        super(validationContext, past);
    }

    @Override
    public boolean isValid(final String dateString, String attribute) {
        DateTimeFormat dateFormat = getDateFormat();
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(dateString);
        } catch (IllegalArgumentException ignored) {}

        Date now = new Date();
        return parsedDate != null && parsedDate.before(now);
    }

    private DateTimeFormat getDateFormat() {
        String dateFormatString =  ruleAnnotation.dateFormat();
        return dateFormatString.isEmpty() ? DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL)
                : DateTimeFormat.getFormat(dateFormatString);
    }
}
