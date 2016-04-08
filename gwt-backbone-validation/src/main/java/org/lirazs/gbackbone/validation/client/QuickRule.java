package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.validation.Rule;

/**
 * Created on 13/02/2016.
 */
public abstract class QuickRule<TARGET> implements Rule<TARGET> {

    /**
     * Constructor.
     *
     */
    protected QuickRule() {
    }

    /**
     * Checks if the rule is valid.
     *
     * @param target The target on which the rule has to be applied.
     *
     * @return true if valid, false otherwise.
     */
    public abstract boolean isValid(TARGET target, String attribute);
}
