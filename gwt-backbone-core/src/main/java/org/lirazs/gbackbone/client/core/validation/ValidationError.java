package org.lirazs.gbackbone.client.core.validation;

import java.util.List;

/**
 * Created on 05/02/2016.
 */
public class ValidationError {

    // can be a view or a model that this validation error is correlated to
    private final Object target;
    private final List<Rule> failedRules;

    /**
     * Constructor.
     *
     * @param target  A failed target object.
     * @param failedRules  A {@link java.util.List} of failed
     *      {@link Rule}s.
     */
    public ValidationError(final Object target, final List<Rule> failedRules) {
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
                "target=" + target +
                ", failedRules=" + failedRules +
                '}';
    }
}
