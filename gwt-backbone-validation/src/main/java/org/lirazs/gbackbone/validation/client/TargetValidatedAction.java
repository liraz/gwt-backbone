package org.lirazs.gbackbone.validation.client;

/**
 * Interface that provides a callback when all {@link org.lirazs.gbackbone.client.core.validation.Rule}s
 * associated with a target passes.
 *
 */
public interface TargetValidatedAction {

    /**
     * Called when all rules associated with the target passes.
     *
     * @param target  The target that has passed validation.
     */
    void onAllRulesPassed(Object target);
}
