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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.validator.routines.CreditCardValidator;
import org.lirazs.gbackbone.validation.client.annotation.CreditCard;


public class CreditCardRule extends AnnotationRule<CreditCard, String> {
    private static final Map<CreditCard.Type, Long> CARD_TYPE_REGISTRY =
            new HashMap<CreditCard.Type, Long>(){{
                put(CreditCard.Type.AMEX, CreditCardValidator.AMEX);
                put(CreditCard.Type.DINERS, CreditCardValidator.DINERS);
                put(CreditCard.Type.DISCOVER, CreditCardValidator.DISCOVER);
                put(CreditCard.Type.MASTERCARD, CreditCardValidator.MASTERCARD);
                put(CreditCard.Type.VISA, CreditCardValidator.VISA);
            }};

    protected CreditCardRule(final CreditCard creditCard) {
        super(creditCard);
    }

    @Override
    public boolean isValid(final String creditCardNumber) {
        CreditCard.Type[] types = ruleAnnotation.cardTypes();
        HashSet<CreditCard.Type> typesSet = new HashSet<CreditCard.Type>(Arrays.asList(types));

        long options = 0;
        if (!typesSet.contains(CreditCard.Type.NONE)) {
            for (CreditCard.Type type : typesSet) {
                options += CARD_TYPE_REGISTRY.get(type);
            }
        } else {
            options = CreditCardValidator.NONE;
        }

        return new CreditCardValidator(options).isValid(creditCardNumber.replaceAll("\\s", ""));
    }
}
