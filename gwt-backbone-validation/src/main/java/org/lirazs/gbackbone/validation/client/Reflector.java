package org.lirazs.gbackbone.validation.client;

import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.reflection.client.*;
import org.lirazs.gbackbone.reflection.client.NoSuchMethodException;
import org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter;
import org.lirazs.gbackbone.validation.client.annotation.ValidateUsing;
import org.lirazs.gbackbone.validation.client.rule.AnnotationRule;
import org.lirazs.gbackbone.validation.client.rule.ContextualAnnotationRule;

import java.lang.annotation.Annotation;

/**
 * Created on 13/02/2016.
 */
public class Reflector {

    /**
     * Retrieve an attribute value from an {@link java.lang.annotation.Annotation}.
     *
     * @param annotation  An {@link java.lang.annotation.Annotation} instance.
     * @param attributeName  Attribute name.
     * @param <T>  Attribute value type.
     *
     * @return The attribute value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttributeValue(final Annotation annotation, final String attributeName) {
        return (T)ReflectionUtils.getAnnotationValueByName(annotation, attributeName);
    }

    /**
     * Checks if an annotation was annotated with the given annotation.
     *
     * @param inspected  The {@link java.lang.annotation.Annotation} to be checked.
     * @param expected  The {@link java.lang.annotation.Annotation} that we are looking for.
     *
     * @return true if the annotation is present, false otherwise.
     */
    public static boolean isAnnotated(final Class<? extends Annotation> inspected,
                                      final Class<? extends Annotation> expected) {
        boolean isAnnotated = false;

        ClassType classType = TypeOracle.Instance.getClassType(inspected);
        Annotation[] declaredAnnotations = classType.getDeclaredAnnotations();
        for (Annotation declaredAnnotation : declaredAnnotations) {
            isAnnotated = expected.equals(declaredAnnotation.annotationType());
            if (isAnnotated) {
                break;
            }
        }
        return isAnnotated;
    }

    /**
     * Finds and returns the correct
     * {@link org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter#getData(Object)}
     * {@link java.lang.reflect.Method}.
     *
     * @param dataAdapterType  The {@link org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter}
     *      class whose {@code getData(View)} method is required.
     *
     * @return The correct {@code getData(View)} method.
     */
    public static Method findGetDataMethod(final Class<? extends TargetDataAdapter> dataAdapterType) {
        Method getDataMethod = null;
        ClassType classType = TypeOracle.Instance.getClassType(dataAdapterType);
        Method[] declaredMethods = classType.getMethods();

        for (Method method : declaredMethods) {
            boolean methodNameIsGetData = "getData".equals(method.getName());

            if (methodNameIsGetData) {

                // Single 'View' parameter
                Parameter[] parameters = method.getParameters();

                int i = 0;
                Class<?>[] parameterTypes = new Class[parameters.length];
                for (Parameter parameter : parameters) {
                    parameterTypes[i++] = parameter.getClass();
                }

                boolean hasSingleViewParameter = parameterTypes.length == 1;

                if (hasSingleViewParameter) {
                    getDataMethod = method;
                    break;
                }
            }
        }
        return getDataMethod;
    }

    /**
     * Instantiates a {@link AnnotationRule} object for the given type.
     *
     * @param ruleType  The {@link AnnotationRule} class to be instantiated.
     * @param ruleAnnotation  The rule {@link java.lang.annotation.Annotation} associated with
     *      the {@link AnnotationRule}.
     *
     * @return The instantiated {@link AnnotationRule} object.
     */
    public static AnnotationRule instantiateRule(final Class<? extends AnnotationRule> ruleType,
                                                 final Annotation ruleAnnotation, final ValidationContext validationContext) {

        ClassType classType = TypeOracle.Instance.getClassType(ruleType);

        AnnotationRule rule = null;
        if (classType != null) {

            if (ReflectionUtils.isAssignable(ContextualAnnotationRule.class, ruleType)) {
                Constructor<?> constructor = classType.findConstructor(ReflectionUtils.getQualifiedSourceName(ValidationContext.class),
                        ReflectionUtils.getQualifiedSourceName(ruleAnnotation.annotationType()));

                if (constructor != null) {
                    rule = (AnnotationRule) constructor.newInstance(validationContext, ruleAnnotation);
                }

            } else if (ReflectionUtils.isAssignable(AnnotationRule.class, ruleType)) {
                Constructor<?> constructor = classType.findConstructor(ReflectionUtils.getQualifiedSourceName(ruleAnnotation.annotationType()));

                if (constructor != null) {
                    rule = (AnnotationRule) constructor.newInstance(ruleAnnotation);
                }
            }
        }

        return rule;
    }

    /**
     * Method finds the data type of the {@link AnnotationRule} that is tied up to the given rule
     * annotation.
     *
     * @param ruleAnnotation  Rule {@link java.lang.annotation.Annotation}.
     *
     * @return The expected data type for the
     *      {@link org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter}s.
     */
    public static Class<?> getRuleDataType(final Annotation ruleAnnotation) {
        ValidateUsing validateUsing = getValidateUsingAnnotation(ruleAnnotation.annotationType());
        return getRuleDataType(validateUsing);
    }

    /**
     * Method finds the data type of the {@link AnnotationRule} that is tied up to the given rule
     * annotation.
     *
     * @param validateUsing  The {@link ValidateUsing}
     *      instance.
     *
     * @return The expected data type for the
     *      {@link org.lirazs.gbackbone.validation.client.adapter.TargetDataAdapter}s.
     */
    public static Class<?> getRuleDataType(final ValidateUsing validateUsing) {
        Class<? extends AnnotationRule> rule = validateUsing.value();
        ClassType classType = TypeOracle.Instance.getClassType(rule);

        Method[] methods = classType.getMethods();
        return getRuleTypeFromIsValidMethod(rule, methods);
    }


    private static ValidateUsing getValidateUsingAnnotation(
            final Class<? extends Annotation> annotationType) {
        ValidateUsing validateUsing = null;
        ClassType classType = TypeOracle.Instance.getClassType(annotationType);

        Annotation[] declaredAnnotations = classType.getDeclaredAnnotations();
        for (Annotation annotation : declaredAnnotations) {
            if (ValidateUsing.class.equals(annotation.annotationType())) {
                validateUsing = (ValidateUsing) annotation;
                break;
            }
        }
        return validateUsing;
    }

    private static Class<?> getRuleTypeFromIsValidMethod(final Class<? extends AnnotationRule> rule,
                                                         final Method[] methods) {

        Class<?> returnType = null;
        for (Method method : methods) {
            Parameter[] parameters = method.getParameters();
            Class<?>[] parameterTypes = new Class[parameters.length];

            int i = 0;
            for (Parameter parameter : parameters) {
                parameterTypes[i++] = parameter.getClass();
            }

            if (matchesIsValidMethodSignature(method, parameterTypes)) {
                // This will be null, if there are no matching methods
                // in the class with a similar signature.
                if (returnType != null) {
                    String message = "Found duplicate 'boolean isValid(T)' method signature in '" + rule.getName() + "'.";
                    throw new IllegalStateException(message);
                }
                returnType = parameters[0].getTypeClass();
            }
        }
        return returnType;
    }

    private static boolean matchesIsValidMethodSignature(final Method method,
                                                         final Class<?>[] parameterTypes) {
        int modifiers = method.getModifiers();

        boolean isPublic = Modifier.isPublic(modifiers);
        boolean returnsBoolean = method.getReturnTypeName().equals("boolean");
        boolean matchesMethodName = "isValid".equals(method.getName());
        boolean hasSingleParameter = parameterTypes.length == 1;

        return isPublic && returnsBoolean && matchesMethodName && hasSingleParameter;
    }
}
