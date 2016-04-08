package org.lirazs.gbackbone.validation.client.rule;

import org.lirazs.gbackbone.reflection.client.Reflectable;
import org.lirazs.gbackbone.validation.client.annotation.AssertFalse;

/**
 * Created on 05/02/2016.
 */
@Reflectable(classAnnotations = false, fields = false, methods = true, constructors = true,
        fieldAnnotations = false, relationTypes=false,
        superClasses=false, assignableClasses=false)
public class AssertFalseRule extends AnnotationRule<AssertFalse, Boolean> {

    public AssertFalseRule(AssertFalse assertFalse) {
        super(assertFalse);
    }

    @Override
    public boolean isValid(Boolean value, String attribute) {
        if (value == null) {
            throw new IllegalArgumentException("'data' cannot be null.");
        }
        return !value;
    }

    @Override
    public String getMessage() {
        return ruleAnnotation.message();
    }
}
