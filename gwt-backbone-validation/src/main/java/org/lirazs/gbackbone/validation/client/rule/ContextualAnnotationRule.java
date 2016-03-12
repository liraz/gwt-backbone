package org.lirazs.gbackbone.validation.client.rule;

import org.lirazs.gbackbone.validation.client.ValidationContext;

import java.lang.annotation.Annotation;

/**
 * Created on 26/02/2016.
 */
public abstract class ContextualAnnotationRule<RULE_ANNOTATION extends Annotation, DATA_TYPE>
        extends AnnotationRule<RULE_ANNOTATION, DATA_TYPE> {

    protected ValidationContext mValidationContext;

    /**
     * Constructor. All subclasses MUST have a constructor with the same signature.
     *
     * @param validationContext  A {@link ValidationContext}.
     * @param ruleAnnotation  The rule {@link java.lang.annotation.Annotation} instance to which
     *      this rule is paired.
     */
    protected ContextualAnnotationRule(final ValidationContext validationContext,
                                       final RULE_ANNOTATION ruleAnnotation) {
        super(ruleAnnotation);
        if (validationContext == null) {
            throw new IllegalArgumentException("'validationContext' cannot be null.");
        }
        mValidationContext = validationContext;
    }
}
