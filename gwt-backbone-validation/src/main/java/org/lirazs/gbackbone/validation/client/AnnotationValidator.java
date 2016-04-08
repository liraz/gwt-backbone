package org.lirazs.gbackbone.validation.client;

import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.Pair;
import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.client.core.validation.Rule;
import org.lirazs.gbackbone.client.core.validation.ValidationError;
import org.lirazs.gbackbone.client.core.validation.Validator;
import org.lirazs.gbackbone.reflection.client.*;
import org.lirazs.gbackbone.validation.client.adapter.*;
import org.lirazs.gbackbone.validation.client.annotation.*;
import org.lirazs.gbackbone.validation.client.exception.ConversionException;
import org.lirazs.gbackbone.validation.client.rule.AnnotationRule;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created on 12/02/2016.
 */
public class AnnotationValidator implements Validator {

    private final Registry registry = new Registry();

    // Holds adapter entries that are mapped to corresponding views.
    private final Map<Class<?>, Map<Class<?>, TargetDataAdapter>> registeredAdaptersMap = new HashMap<>();

    // Attributes
    private Object mController;
    private ValidationContext mValidationContext;
    private Map<String, List<Pair<Rule, TargetDataAdapter>>> mAttributeRulesMap;
    private Map<String, Object> mAttributeKeyTargetMap;
    private TargetValidatedAction mTargetValidatedAction;
    private ValidationListener mValidationListener;

    /**
     * Constructor.
     *
     * @param controller  The class containing targets to be validated.
     */
    public AnnotationValidator(final Object controller) {
        assertNotNull(controller, "controller");
        mController = controller;
        mTargetValidatedAction = new DefaultTargetValidatedAction();

        // Instantiate a ValidationContext
        mValidationContext = new ValidationContext();
        // Else, lazy init ValidationContext in #getRuleAdapterPair(Annotation, Field)
        // or void #put(TARGET, QuickRule<TARGET>) by obtaining a Context from one of the
        // View instances.

        // when the controller is also a ValidationListener
        if(controller instanceof ValidationListener) {
            setValidationListener((ValidationListener) controller);
        }
    }

    /**
     * The controller object associated with this validator.
     *
     * @return
     */
    public Object getController() {
        return mController;
    }

    /**
     * Registers {@link TargetDataAdapter}s for the associated
     * {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule}s and their targets.
     *
     * @param targetType  The target type on which the {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule}
     *      can be used.
     * @param targetDataAdapter  The {@link TargetDataAdapter}
     *      that can get the data for the {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule} from the
     *      target.
     * @param ruleAnnotations  Varargs of rule {@link java.lang.annotation.Annotation}s that
     *      can be used with the target and the
     *      {@link TargetDataAdapter}.
     *
     * @param <TARGET>  Type parameter that is a subclass of target class.
     * @param <DATA_TYPE>  Data type expected by the {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule} and
     *      is returned by the {@link TargetDataAdapter}.
     */
    public <TARGET, DATA_TYPE> void register(
            final Class<TARGET> targetType,
            final TargetDataAdapter<TARGET, DATA_TYPE> targetDataAdapter,
            final Class<? extends Annotation>... ruleAnnotations) {

        registry.register(targetType, targetDataAdapter, ruleAnnotations);
    }

    /**
     * An elaborate method for registering custom rule annotations.
     *
     * @param annotation  The annotation that you want to register.
     * @param targetType  The target type.
     * @param targetDataAdapter  An instance of the
     *      {@link TargetDataAdapter} for your target.
     *
     * @param <TARGET>  The target for which the
     *      {@link java.lang.annotation.Annotation} and
     *      {@link TargetDataAdapter} is being registered.
     */
    public <TARGET> void registerAnnotation(final Class<? extends Annotation> annotation, final Class<TARGET> targetType,
            final TargetDataAdapter<TARGET, ?> targetDataAdapter) {

        registry.register(targetType, targetDataAdapter, annotation);
    }

    /**
     * Registers a {@link TargetDataAdapter} for the given target.
     *
     * @param targetType  The target for which a
     *      {@link TargetDataAdapter} is being registered.
     * @param targetDataAdapter  A {@link TargetDataAdapter} instance.
     *
     * @param <TARGET>  The target type.
     * @param <DATA_TYPE>  The {@link TargetDataAdapter} type.
     */
    public <TARGET, DATA_TYPE> void registerAdapter(
            final Class<TARGET> targetType, final TargetDataAdapter<TARGET, DATA_TYPE> targetDataAdapter) {
        assertNotNull(targetType, "targetType");
        assertNotNull(targetDataAdapter, "targetDataAdapter");

        Map<Class<?>, TargetDataAdapter> dataTypeAdapterMap = registeredAdaptersMap.get(targetType);
        if (dataTypeAdapterMap == null) {
            dataTypeAdapterMap = new HashMap<>();
            registeredAdaptersMap.put(targetType, dataTypeAdapterMap);
        }

        // Find adapter's data type
        Method getDataMethod = Reflector.findGetDataMethod(targetDataAdapter.getClass());
        Class adapterDataType = getDataMethod.getReturnTypeClass();

        dataTypeAdapterMap.put(adapterDataType, targetDataAdapter);
    }

    /**
     * Set a {@link ValidationListener} to the
     * {@link AnnotationValidator}.
     *
     * @param validationListener  A {@link ValidationListener}
     *      instance. null throws an {@link java.lang.IllegalArgumentException}.
     */
    public void setValidationListener(final ValidationListener validationListener) {
        assertNotNull(validationListener, "validationListener");
        this.mValidationListener = validationListener;
    }

    /**
     * Set a {@link TargetValidatedAction} to the {@link AnnotationValidator}.
     *
     * @param viewValidatedAction  A {@link TargetValidatedAction}
     *      instance.
     */
    public void setTargetValidatedAction(final TargetValidatedAction viewValidatedAction) {
        this.mTargetValidatedAction = viewValidatedAction;
    }

    /**
     * Validates all annotations.
     * Asynchronous validation.
     */
    @Override
    public void validate() {
        createRulesSafelyAndLazily(false);
        validateFields();
    }

    @Override
    public List<ValidationError> isValid() {
        createRulesSafelyAndLazily(false);
        return getValidationErrors(mAttributeRulesMap);
    }

    @Override
    public List<ValidationError> isValid(Options attributes) {
        createRulesSafelyAndLazily(false);
        return getValidationErrors(mAttributeRulesMap, attributes.keySet());
    }

    private void validateFields() {
        createRulesSafelyAndLazily(false);
        triggerValidationListenerCallback(validateTill());
    }

    private synchronized List<ValidationError> validateTill() {
        // Have we registered a validation listener?
        assertNotNull(mValidationListener, "validationListener");

        // Everything good. Bingo! validate ;)
        return getValidationErrors(mAttributeRulesMap);
    }

    /**
     * Add one or more {@link QuickRule}s for a target.
     *
     * @param target  A target for which
     *      {@link QuickRule}(s) are to be added.
     * @param quickRules  Varargs of {@link QuickRule}s.
     *
     * @param <TARGET>  The target type for which the
     *      {@link QuickRule}s are being registered.
     */
    public <TARGET> void put(String attribute, final TARGET target, final QuickRule<TARGET>... quickRules) {
        assertNotNull(attribute, "attribute");
        assertNotNull(target, "target");
        assertNotNull(quickRules, "quickRules");
        if (quickRules.length == 0) {
            throw new IllegalArgumentException("'quickRules' cannot be empty.");
        }

        if (mValidationContext == null) {
            mValidationContext = new ValidationContext();
        }

        // Create rules
        createRulesSafelyAndLazily(true);

        // If there are no rules, create an empty list
        List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = mAttributeRulesMap.get(attribute);
        ruleAdapterPairs = ruleAdapterPairs == null
                ? new ArrayList<Pair<Rule, TargetDataAdapter>>() : ruleAdapterPairs;

        // Add the quick rule to existing rules
        for (QuickRule quickRule : quickRules) {
            if (quickRule != null) {
                ruleAdapterPairs.add(new Pair<Rule, TargetDataAdapter>(quickRule, null));
            }
        }
        mAttributeRulesMap.put(attribute, ruleAdapterPairs);
    }

    /**
     * Remove all {@link Rule}s for the given target.
     *
     * @param attribute  The attribute whose rules should be removed.
     */
    public void removeRules(final String attribute) {
        assertNotNull(attribute, "attribute");
        if (mAttributeRulesMap == null) {
            createRulesSafelyAndLazily(false);
        }
        mAttributeRulesMap.remove(attribute);
    }

    boolean isValidationAnnotation(final Class<? extends Annotation> annotation) {
        return registry.getRegisteredAnnotations().contains(annotation);
    }

    private static void assertNotNull(final Object object, final String argumentName) {
        if (object == null) {
            String message = StringUtils.format("'%s' cannot be null.", argumentName);
            throw new IllegalArgumentException(message);
        }
    }

    private void createRulesSafelyAndLazily(final boolean addingQuickRules) {
        // Create rules lazily, because we don't have to worry about the order of
        // instantiating the Validator.
        if (mAttributeRulesMap == null) {
            final List<Field> annotatedFields = getValidationAnnotatedFields(mController.getClass());
            mAttributeRulesMap = createRules(annotatedFields);
            mAttributeKeyTargetMap = createAttributes(annotatedFields);

            mValidationContext.setAttributeTargetMap(mAttributeKeyTargetMap);
            mValidationContext.setAttributeRulesMap(mAttributeRulesMap);
        }

        if (!addingQuickRules && mAttributeRulesMap.size() == 0) {
            String message = "No rules found. You must have at least one rule to validate. "
                    + "If you are using custom annotations, make sure that you have registered "
                    + "them using the 'Validator.register()' method.";
            throw new IllegalStateException(message);
        }
    }

    protected List<Field> getValidationAnnotatedFields(final Class<?> controllerClass) {
        Set<Class<? extends Annotation>> validationAnnotations =
                registry.getRegisteredAnnotations();

        List<Field> annotatedFields = new ArrayList<>();
        List<Field> controllerTargetFields = getControllerTargetFields(controllerClass);
        for (Field field : controllerTargetFields) {
            if (isValidationAnnotatedField(field, validationAnnotations)) {
                annotatedFields.add(field);
            }
        }

        return annotatedFields;
    }

    private List<Field> getControllerTargetFields(final Class<?> controllerClass) {
        List<Field> controllerTargetFields = new ArrayList<>();

        // Fields declared in the controller
        controllerTargetFields.addAll(getTargetFields(controllerClass));

        // Inherited fields
        Class<?> superClass = controllerClass.getSuperclass();
        // don't start grabbing the View or Model fields - just stop there...
        while (eligibleSuperClassTargetFieldsScan(superClass)) {

            List<Field> targetFields = getTargetFields(superClass);
            if (targetFields.size() > 0) {
                controllerTargetFields.addAll(targetFields);
            }
            superClass = superClass.getSuperclass();
        }

        return controllerTargetFields;
    }

    protected boolean eligibleSuperClassTargetFieldsScan(Class<?> superClass) {
        return !superClass.equals(Object.class);
    }

    private List<Field> getTargetFields(final Class<?> clazz) {
        List<Field> targetFields = new ArrayList<>();
        ClassType classType = TypeOracle.Instance.getClassType(clazz);
        Field[] declaredFields = classType.getFields();

        for (Field field : declaredFields) {

            if(isValidationAnnotatedField(field, registry.getRegisteredAnnotations())) {
                targetFields.add(field);
            }
        }

        return targetFields;
    }

    private boolean isValidationAnnotatedField(final Field field,
                                             final Set<Class<? extends Annotation>> registeredAnnotations) {
        boolean hasValidationAnnotation = false;

        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            hasValidationAnnotation = registeredAnnotations.contains(annotation.annotationType());
            if (hasValidationAnnotation) {
                break;
            }
        }

        return hasValidationAnnotation;
    }

    private Map<String, List<Pair<Rule, TargetDataAdapter>>> createRules(
            final List<Field> annotatedFields) {

        final Map<String, List<Pair<Rule, TargetDataAdapter>>> attributeRulesMap =
                new LinkedHashMap<>();

        for (Field field : annotatedFields) {
            String attribute = getAttribute(field);

            // If there are no rules, create an empty list
            List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = attributeRulesMap.get(attribute);
            ruleAdapterPairs = ruleAdapterPairs == null
                    ? new ArrayList<Pair<Rule, TargetDataAdapter>>() : ruleAdapterPairs;

            final Annotation[] fieldAnnotations = field.getAnnotations();

            for (Annotation fieldAnnotation : fieldAnnotations) {
                if (isValidationAnnotation(fieldAnnotation.annotationType())) {
                    Pair<Rule, TargetDataAdapter> ruleAdapterPair =
                            getRuleAdapterPair(fieldAnnotation, field);
                    ruleAdapterPairs.add(ruleAdapterPair);
                }
            }
            attributeRulesMap.put(attribute, ruleAdapterPairs);
        }

        return attributeRulesMap;
    }

    private Map<String, Object> createAttributes(final List<Field> annotatedFields) {

        final Map<String, Object> attributesMap = new LinkedHashMap<>();
        for (Field field : annotatedFields) {

            attributesMap.put(getAttribute(field), getTarget(field));
        }

        return attributesMap;
    }

    private Pair<Rule, TargetDataAdapter> getRuleAdapterPair(final Annotation validationAnnotation,
                                                           final Field targetField) {

        final Class<? extends Annotation> annotationType = validationAnnotation.annotationType();
        final Class targetFieldType = targetField.getTypeClass();
        final Class<?> ruleDataType = Reflector.getRuleDataType(validationAnnotation);

        final TargetDataAdapter dataAdapter = getDataAdapter(annotationType, targetFieldType, ruleDataType);

        // If no matching adapter is found, throw.
        if (dataAdapter == null) {
            String viewType = targetFieldType.getName();
            String message = StringUtils.format(
                    "To use '%s' on '%s', register a '%s' that returns a '%s' from the '%s'.",
                    annotationType.getName(),
                    viewType,
                    TargetDataAdapter.class.getName(),
                    ruleDataType,
                    viewType);
            throw new UnsupportedOperationException(message);
        }

        if (mValidationContext == null) {
            mValidationContext = new ValidationContext();
        }

        final Class<? extends AnnotationRule> ruleType = getRuleType(validationAnnotation);
        final AnnotationRule rule = Reflector.instantiateRule(ruleType,
                validationAnnotation, mValidationContext);

        return new Pair<Rule, TargetDataAdapter>(rule, dataAdapter);
    }

    private TargetDataAdapter getDataAdapter(final Class<? extends Annotation> annotationType,
                                           final Class viewFieldType, final Class<?> adapterDataType) {

        // Get an adapter from the stock registry
        TargetDataAdapter dataAdapter = registry.getDataAdapter(annotationType, viewFieldType);

        // If we are unable to find a validation stock adapter, check the registered adapters
        if (dataAdapter == null) {
            Map<Class<?>, TargetDataAdapter> dataTypeAdapterMap = registeredAdaptersMap.get(viewFieldType);
            dataAdapter = dataTypeAdapterMap != null
                    ? dataTypeAdapterMap.get(adapterDataType)
                    : null;
        }

        return dataAdapter;
    }

    private Class<? extends AnnotationRule> getRuleType(final Annotation ruleAnnotation) {
        ClassType classType = TypeOracle.Instance.getClassType(ruleAnnotation.annotationType());
        ValidateUsing validateUsing = classType.getAnnotation(ValidateUsing.class);

        return validateUsing != null ? validateUsing.value() : null;
    }

    protected Object getTarget(final Field field) {
        Object target = null;
        try {
            // if the target is a view input, we need to query the view input
            target = field.getFieldValue(mController);

            if (target == null) {
                String message = StringUtils.format("'%s %s' is null.",
                        field.getType().getSimpleSourceName(), field.getName());
                throw new IllegalStateException(message);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return target;
    }

    protected String getAttribute(final Field field) {
        //TODO: support for @Attribute annotation so the name can be changed..
        return field.getName();
    }

    private void triggerValidationListenerCallback(final List<ValidationError> validationErrors) {

        if (validationErrors.size() == 0) {
            mValidationListener.onValidationSucceeded();
        } else {
            mValidationListener.onValidationFailed(validationErrors);
        }
    }

    private List<ValidationError> getValidationErrors(final Map<String, List<Pair<Rule, TargetDataAdapter>>> attributeRulesMap) {
        return getValidationErrors(attributeRulesMap, null);
    }

    private List<ValidationError> getValidationErrors(final Map<String, List<Pair<Rule, TargetDataAdapter>>> attributeRulesMap,
                                                      final Collection<String> attributes) {

        final List<ValidationError> validationErrors = new ArrayList<>();

        Object target;
        Collection<String> attributesToValidate;

        if(attributes != null && !attributes.isEmpty()) {
            attributesToValidate = attributes;
        } else {
            attributesToValidate = mAttributeKeyTargetMap.keySet();
        }

        for (String attribute : attributesToValidate) {
            target = mAttributeKeyTargetMap.get(attribute);

            List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = attributeRulesMap.get(attribute);
            int nRules = ruleAdapterPairs.size();

            // Validate all the rules for the given target.
            List<Rule> failedRules = null;

            for (int i = 0; i < nRules; i++) {
                Pair<Rule, TargetDataAdapter> ruleAdapterPair = ruleAdapterPairs.get(i);

                Rule failedRule = validateAttributeTargetWithRule(attribute, target,
                        ruleAdapterPair.getFirst(), ruleAdapterPair.getSecond());

                if (failedRule != null) {
                    if (failedRules == null) {
                        failedRules = new ArrayList<>();
                        validationErrors.add(new ValidationError(attribute, target, failedRules));
                    }
                    failedRules.add(failedRule);
                }
            }

            // Callback if a target passes all rules
            boolean viewPassedAllRules = (failedRules == null || failedRules.size() == 0);
            if (viewPassedAllRules && mTargetValidatedAction != null) {
                triggerViewValidatedCallback(mTargetValidatedAction, target);
            }
        }


        return validationErrors;
    }


    private Rule validateAttributeTargetWithRule(final String attribute, final Object target, final Rule rule,
                                      final TargetDataAdapter dataAdapter) {

        boolean valid = false;
        if (rule instanceof AnnotationRule) {
            Object data;

            try {
                data = dataAdapter.getData(target, attribute);
                valid = rule.isValid(data, attribute);
            } catch (ConversionException e) {
                valid = false;
                e.printStackTrace();
            }
        } else if (rule instanceof QuickRule) {
            valid = rule.isValid(target, attribute);
        }

        return valid ? null : rule;
    }

    private void triggerViewValidatedCallback(final TargetValidatedAction viewValidatedAction,
                                              final Object target) {
        viewValidatedAction.onAllRulesPassed(target);
    }
}
