package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.data.Pair;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.validation.Rule;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.TypeOracle;
import org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter;
import org.lirazs.gbackbone.validation.client.annotation.ValidateUsing;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;
import org.lirazs.gbackbone.validation.client.rule.AnnotationRule;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 12/02/2016.
 */
public class ValidationContext {

    Object controller;
    Map<Object, List<Pair<Rule, TargetDataAdapter>>> targetRulesMap;

    /**
     * Retrieves all targets that are annotated with the specified annotation.
     *
     * @param validationAnnotation  The annotation we are interested in.
     *
     * @return A {@link java.util.List} of targets annotated with the
     *      given annotation.
     */
    public List<Object> getAnnotatedTargets(final Class<? extends Annotation> validationAnnotation) {
        // Get the AnnotationRule class
        Class<? extends AnnotationRule> annotationRuleClass = getRuleClass(validationAnnotation);

        // Find all targets with the target rule
        List<Object> annotatedTargets = new ArrayList<>();
        Set<Object> targets = targetRulesMap.keySet();

        for (Object target : targets) {
            List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = targetRulesMap.get(target);

            for (Pair<Rule, TargetDataAdapter> ruleAdapterPair : ruleAdapterPairs) {
                boolean uniqueMatchingTarget =
                        annotationRuleClass.equals(ruleAdapterPair.getFirst().getClass())
                                && !annotatedTargets.contains(target);
                if (uniqueMatchingTarget) {
                    annotatedTargets.add(target);
                }
            }
        }

        return annotatedTargets;
    }

    /**
     * Retrieves the data from the given target using the appropriate
     * {@link TargetDataAdapter}.
     *
     * @param target  A target.
     * @param validationAnnotation  The annotation used to annotate the target.
     *
     * @return The data that's on the target.
     */
    public Object getData(final Object target, Class<? extends Annotation> validationAnnotation) {
        Object data = null;
        List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = targetRulesMap.get(target);
        Class<? extends AnnotationRule> annotationRuleClass = getRuleClass(validationAnnotation);

        for (Pair<Rule, TargetDataAdapter> ruleAdapterPair : ruleAdapterPairs) {
            if (annotationRuleClass.equals(ruleAdapterPair.getFirst().getClass())) {
                try {
                    if(controller instanceof Model) {
                        Model model = (Model) controller;
                        data = model.get((String) target);
                    } else {
                        data = ruleAdapterPair.getSecond().getData(target);
                    }
                } catch (ConversionException e) {
                    e.printStackTrace();
                }
            }
        }

        return data;
    }

    void setTargetRulesMap(final Map<Object, List<Pair<Rule, TargetDataAdapter>>> targetRulesMap) {
        this.targetRulesMap = targetRulesMap;
    }

    void setController(Object controller) {
        this.controller = controller;
    }

    private Class<? extends AnnotationRule> getRuleClass(final Class<? extends Annotation> validationAnnotation) {

        ClassType classType = TypeOracle.Instance.getClassType(validationAnnotation);
        ValidateUsing validateUsingAnnotation = classType.getAnnotation(ValidateUsing.class);

        return validateUsingAnnotation.value();
    }
}
