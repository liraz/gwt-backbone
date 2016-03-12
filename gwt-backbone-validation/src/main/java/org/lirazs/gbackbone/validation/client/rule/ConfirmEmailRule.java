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

import org.lirazs.gbackbone.validation.client.ValidationContext;
import org.lirazs.gbackbone.validation.client.annotation.ConfirmEmail;
import org.lirazs.gbackbone.validation.client.annotation.Email;


public class ConfirmEmailRule extends SameValueContextualRule<ConfirmEmail, Email, String> {

    protected ConfirmEmailRule(final ValidationContext validationContext,
            final ConfirmEmail confirmEmail) {
        super(validationContext, confirmEmail, Email.class);
    }

    @Override
    public boolean isValid(final String confirmValue) {
        return super.isValid(confirmValue);
    }
}
