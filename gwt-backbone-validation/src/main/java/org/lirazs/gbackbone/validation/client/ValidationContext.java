package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.data.Pair;
import org.lirazs.gbackbone.client.core.validation.Rule;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.TypeOracle;
import org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter;
import org.lirazs.gbackbone.validation.client.annotation.ValidateUsing;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;
import org.lirazs.gbackbone.validation.client.rule.AnnotationRule;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created on 12/02/2016.
 */
public class ValidationContext {
    Map<String, List<Pair<Rule, TargetDataAdapter>>> attributeRulesMap;
    Map<String, Object> attributeTargetMap;

    /**
     * Retrieves all attributes that are annotated with the specified annotation.
     *
     * @param validationAnnotation  The annotation we are interested in.
     *
     * @return A {@link java.util.List} of attributes annotated with the
     *      given annotation.
     */
    public Set<String> getAnnotatedAttributes(final Class<? extends Annotation> validationAnnotation) {
        // Get the AnnotationRule class
        Class<? extends AnnotationRule> annotationRuleClass = getRuleClass(validationAnnotation);

        // Find all targets with the target rule
        Set<String> annotatedAttributes = new HashSet<>();
        Set<String> attributes = attributeRulesMap.keySet();

        for (String attribute : attributes) {
            List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = attributeRulesMap.get(attribute);

            for (Pair<Rule, TargetDataAdapter> ruleAdapterPair : ruleAdapterPairs) {
                boolean uniqueMatchingTarget =
                        annotationRuleClass.equals(ruleAdapterPair.getFirst().getClass())
                                && !annotatedAttributes.contains(attribute);
                if (uniqueMatchingTarget) {
                    annotatedAttributes.add(attribute);
                }
            }
        }

        return annotatedAttributes;
    }

    /**
     * Retrieves the data from the given target using the appropriate
     * {@link TargetDataAdapter}.
     *
     * @param attribute
     * @return The data that's on the target.
     */
    public Object getData(final String attribute) {
        return getData(attribute, null);
    }

    /**
     * Retrieves the data from the given target using the appropriate
     * {@link TargetDataAdapter}.
     *
     * @param validationAnnotation  The annotation used to annotate the target.
     *
     * @return The data that's on the target.
     */
    public Object getData(final String attribute, Class<? extends Annotation> validationAnnotation) {
        Object data = null;

        Object target = attributeTargetMap.get(attribute);
        List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = attributeRulesMap.get(attribute);
        Class<? extends AnnotationRule> annotationRuleClass = validationAnnotation != null ?
                getRuleClass(validationAnnotation) : null;

        for (Pair<Rule, TargetDataAdapter> ruleAdapterPair : ruleAdapterPairs) {
            if (annotationRuleClass == null || annotationRuleClass.equals(ruleAdapterPair.getFirst().getClass())) {
                try {
                    data = ruleAdapterPair.getSecond().getData(target, attribute);
                    if(data != null)
                        break;
                } catch (ConversionException e) {
                    e.printStackTrace();
                }
            }
        }

        return data;
    }

    public void setAttributeTargetMap(Map<String, Object> attributeTargetMap) {
        this.attributeTargetMap = attributeTargetMap;
    }

    void setAttributeRulesMap(final Map<String, List<Pair<Rule, TargetDataAdapter>>> attributeRulesMap) {
        this.attributeRulesMap = attributeRulesMap;
    }

    private Class<? extends AnnotationRule> getRuleClass(final Class<? extends Annotation> validationAnnotation) {

        ClassType classType = TypeOracle.Instance.getClassType(validationAnnotation);
        ValidateUsing validateUsingAnnotation = classType.getAnnotation(ValidateUsing.class);

        return validateUsingAnnotation.value();
    }
}
