package org.lirazs.gbackbone.validation.client.rule;

import org.lirazs.gbackbone.client.core.validation.Rule;
import org.lirazs.gbackbone.validation.client.Reflector;

import java.lang.annotation.Annotation;

/**
 * Created on 05/02/2016.
 */
public abstract class AnnotationRule<RULE_ANNOTATION extends Annotation, DATA_TYPE>
        implements Rule<DATA_TYPE> {

    protected final RULE_ANNOTATION ruleAnnotation;

    /**
     * Constructor. It is mandatory that all subclasses MUST have a constructor with the same
     * signature.
     *
     * @param ruleAnnotation  The rule {@link java.lang.annotation.Annotation} instance to which
     *      this rule is paired.
     */
    protected AnnotationRule(final RULE_ANNOTATION ruleAnnotation) {
        if (ruleAnnotation == null) {
            throw new IllegalArgumentException("'ruleAnnotation' cannot be null.");
        }
        this.ruleAnnotation = ruleAnnotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {

        //TODO: Support for localization of strings - check GWT solutions
        /*final int messageResId = Reflector.getAttributeValue(ruleAnnotation, "messageResId");

        return messageResId != -1
                ? context.getString(messageResId)
                : Reflector.getAttributeValue(ruleAnnotation, "message");*/
        return Reflector.getAttributeValue(ruleAnnotation, "message");
    }
}
