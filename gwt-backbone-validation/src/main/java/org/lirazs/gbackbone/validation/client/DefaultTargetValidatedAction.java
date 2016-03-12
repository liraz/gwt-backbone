package org.lirazs.gbackbone.validation.client;

/**
 * Created on 26/02/2016.
 */
public class DefaultTargetValidatedAction implements TargetValidatedAction {

    @Override
    public void onAllRulesPassed(final Object target) {

        //TODO: This is important - but i don't know how all views can be supported with this
        /*boolean isTextView = view instanceof TextView;
        if (isTextView) {
            ((TextView) view).setError(null);
        }*/
    }
}
