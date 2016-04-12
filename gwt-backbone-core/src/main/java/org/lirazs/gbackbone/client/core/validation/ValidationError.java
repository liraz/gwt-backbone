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

import java.util.List;

/**
 * Created on 05/02/2016.
 */
public class ValidationError {

    // can be a view or a model that this validation error is correlated to
    private final String attribute;
    private final Object target;
    private final List<Rule> failedRules;

    /**
     * Constructor.
     *
     * @param target  A failed target object.
     * @param failedRules  A {@link java.util.List} of failed
     *      {@link Rule}s.
     */
    public ValidationError(final String attribute, final Object target, final List<Rule> failedRules) {
        this.attribute = attribute;
        this.target = target;
        this.failedRules = failedRules;
    }

    /**
     *
     * @return The failed associated target
     */
    public Object getTarget() {
        return target;
    }

    /**
     *
     * @return The attribute associated to this error
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Gets the failed {@link Rule}s.
     *
     * @return A {@link java.util.List} of failed {@link Rule}s.
     */
    public List<Rule> getFailedRules() {
        return failedRules;
    }

    /**
     * Extracts error messages from multiple failed rules and returns a {@link java.lang.String}
     * object.
     *
     * @return A collated error message.
     */
    public String getCollatedErrorMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Rule failedRule : failedRules) {
            String message = failedRule.getMessage().trim();
            if (message.length() > 0) {
                stringBuilder.append(message).append('\n');
            }
        }
        return stringBuilder.toString().trim();
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "attribute=" + attribute +
                ", target=" + target +
                ", failedRules=" + failedRules +
                '}';
    }
}
