package org.lirazs.gbackbone.validation.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.client.core.data.Options;
import org.lirazs.gbackbone.client.core.data.Pair;
import org.lirazs.gbackbone.client.core.model.Model;
import org.lirazs.gbackbone.client.core.util.StringUtils;
import org.lirazs.gbackbone.client.core.validation.Rule;
import org.lirazs.gbackbone.client.core.validation.ValidationError;
import org.lirazs.gbackbone.client.core.validation.Validator;
import org.lirazs.gbackbone.client.core.view.View;
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

    private static final Registry registry = new Registry();

    // Holds adapter entries that are mapped to corresponding views.
    private final Map<Class<?>, Map<Class<?>, TargetDataAdapter>> registeredAdaptersMap = new HashMap<>();

    // Attributes
    private Object mController;
    private ValidationContext mValidationContext;
    private Map<Object, List<Pair<Rule, TargetDataAdapter>>> mTargetRulesMap;
    private Map<Object, String> mTargetAttributeKeyMap;
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
     * An elaborate method for registering custom rule annotations.
     *
     * @param annotation  The annotation that you want to register.
     * @param targetType  The target type.
     * @param viewDataAdapter  An instance of the
     *      {@link TargetDataAdapter} for your target.
     *
     * @param <TARGET>  The target for which the
     *      {@link java.lang.annotation.Annotation} and
     *      {@link TargetDataAdapter} is being registered.
     */
    public static <TARGET> void registerAnnotation(
            final Class<? extends Annotation> annotation, final Class<TARGET> targetType,
            final TargetDataAdapter<TARGET, ?> viewDataAdapter) {

        ClassType classType = TypeOracle.Instance.getClassType(annotation);

        //ValidateUsing validateUsing = classType.getAnnotation(ValidateUsing.class);
        //Class ruleDataType = Reflector.getRuleDataType(validateUsing);

        registry.register(targetType, viewDataAdapter, annotation);
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
        return getValidationErrors(mTargetRulesMap);
    }

    @Override
    public List<ValidationError> isValid(Options attributes) {
        createRulesSafelyAndLazily(false);
        return getValidationErrors(mTargetRulesMap, attributes);
    }

    private void validateFields() {
        createRulesSafelyAndLazily(false);
        triggerValidationListenerCallback(validateTill());
    }

    private synchronized List<ValidationError> validateTill() {
        // Have we registered a validation listener?
        assertNotNull(mValidationListener, "validationListener");

        // Everything good. Bingo! validate ;)
        return getValidationErrors(mTargetRulesMap);
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
    public <TARGET> void put(final TARGET target, final QuickRule<TARGET>... quickRules) {
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
        List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = mTargetRulesMap.get(target);
        ruleAdapterPairs = ruleAdapterPairs == null
                ? new ArrayList<Pair<Rule, TargetDataAdapter>>() : ruleAdapterPairs;

        // Add the quick rule to existing rules
        for (QuickRule quickRule : quickRules) {
            if (quickRule != null) {
                ruleAdapterPairs.add(new Pair<Rule, TargetDataAdapter>(quickRule, null));
            }
        }
        mTargetRulesMap.put(target, ruleAdapterPairs);
    }

    /**
     * Remove all {@link Rule}s for the given target.
     *
     * @param target  The target whose rules should be removed.
     */
    public void removeRules(final Object target) {
        assertNotNull(target, "target");
        if (mTargetRulesMap == null) {
            createRulesSafelyAndLazily(false);
        }
        mTargetRulesMap.remove(target);
    }

    static boolean isValidationAnnotation(final Class<? extends Annotation> annotation) {
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
        if (mTargetRulesMap == null) {
            final List<Field> annotatedFields = getValidationAnnotatedFields(mController.getClass());
            mTargetRulesMap = createRules(annotatedFields);
            mTargetAttributeKeyMap = createAttributes(annotatedFields);

            mValidationContext.setController(mController);
            mValidationContext.setTargetRulesMap(mTargetRulesMap);
        }

        if (!addingQuickRules && mTargetRulesMap.size() == 0) {
            String message = "No rules found. You must have at least one rule to validate. "
                    + "If you are using custom annotations, make sure that you have registered "
                    + "them using the 'Validator.register()' method.";
            throw new IllegalStateException(message);
        }
    }

    private List<Field> getValidationAnnotatedFields(final Class<?> controllerClass) {
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
        while (!superClass.equals(Object.class) &&
                !superClass.equals(Model.class) &&
                !superClass.equals(View.class)) {

            List<Field> targetFields = getTargetFields(superClass);
            if (targetFields.size() > 0) {
                controllerTargetFields.addAll(targetFields);
            }
            superClass = superClass.getSuperclass();
        }

        return controllerTargetFields;
    }

    private List<Field> getTargetFields(final Class<?> clazz) {
        List<Field> targetFields = new ArrayList<>();
        ClassType classType = TypeOracle.Instance.getClassType(clazz);
        Field[] declaredFields = classType.getFields();

        for (Field field : declaredFields) {
            // target fields for now can be GQuery/InputElement/SelectElement or annotated with @ModelBind
            // @ModelBind - future! & @ModelBind(twoway=true)
            String typeName = field.getTypeName();

            if(typeName.equals("com.google.gwt.query.client.GQuery")
                    || typeName.equals("com.google.gwt.dom.client.InputElement")
                    || typeName.equals("com.google.gwt.dom.client.SelectElement")
                    || mController instanceof Model) { // for instance of Model we accept all fields

                //TODO: Find a way to deal with custom input/select/gquery elements?!... not sure
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

    private Map<Object, List<Pair<Rule, TargetDataAdapter>>> createRules(
            final List<Field> annotatedFields) {

        final Map<Object, List<Pair<Rule, TargetDataAdapter>>> targetRulesMap =
                new LinkedHashMap<>();

        for (Field field : annotatedFields) {
            final ArrayList<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = new ArrayList<>();
            final Annotation[] fieldAnnotations = field.getAnnotations();

            for (Annotation fieldAnnotation : fieldAnnotations) {
                if (isValidationAnnotation(fieldAnnotation.annotationType())) {
                    Pair<Rule, TargetDataAdapter> ruleAdapterPair =
                            getRuleAdapterPair(fieldAnnotation, field);
                    ruleAdapterPairs.add(ruleAdapterPair);
                }
            }

            targetRulesMap.put(getTarget(field), ruleAdapterPairs);
        }

        return targetRulesMap;
    }

    private Map<Object, String> createAttributes(final List<Field> annotatedFields) {

        final Map<Object, String> attributesMap = new LinkedHashMap<>();
        for (Field field : annotatedFields) {
            attributesMap.put(getTarget(field), field.getName());
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

    private Object getTarget(final Field field) {
        Object target = null;
        try {
            if(mController instanceof Model) { // name of the field will be used as the key
                target = field.getName();
            } else {
                target = field.getFieldValue(mController);
            }

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

    private void triggerValidationListenerCallback(final List<ValidationError> validationErrors) {

        if (validationErrors.size() == 0) {
            mValidationListener.onValidationSucceeded();
        } else {
            mValidationListener.onValidationFailed(validationErrors);
        }
    }

    private List<ValidationError> getValidationErrors(final Map<Object, List<Pair<Rule, TargetDataAdapter>>> targetRulesMap) {
        return getValidationErrors(targetRulesMap, null);
    }

    private List<ValidationError> getValidationErrors(final Map<Object, List<Pair<Rule, TargetDataAdapter>>> targetRulesMap, final Options attributes) {

        final List<ValidationError> validationErrors = new ArrayList<>();
        final Set<?> targets = targetRulesMap.keySet();

        for (Object target : targets) {
            List<Pair<Rule, TargetDataAdapter>> ruleAdapterPairs = targetRulesMap.get(target);
            int nRules = ruleAdapterPairs.size();

            // Validate all the rules for the given target.
            List<Rule> failedRules = null;
            for (int i = 0; i < nRules; i++) {

                Pair<Rule, TargetDataAdapter> ruleAdapterPair = ruleAdapterPairs.get(i);

                Rule failedRule;
                if(attributes != null && !attributes.isEmpty()) {
                    Object data = attributes.get(mTargetAttributeKeyMap.get(target));
                    failedRule = validateDataWithRule(data, ruleAdapterPair.getFirst());
                } else {
                    failedRule = validateTargetWithRule(
                            target, ruleAdapterPair.getFirst(), ruleAdapterPair.getSecond());
                }

                if (failedRule != null) {
                    if (failedRules == null) {
                        failedRules = new ArrayList<>();
                        validationErrors.add(new ValidationError(target, failedRules));
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


    private Rule validateTargetWithRule(final Object target, final Rule rule,
                                      final TargetDataAdapter dataAdapter) {

        boolean valid = false;
        if (rule instanceof AnnotationRule) {
            Object data;

            try {
                if(mController instanceof Model) {
                    Model model = (Model) mController;
                    data = model.get((String) target); // target now is a name of the field
                } else {
                    data = dataAdapter.getData(target);
                }
                valid = rule.isValid(data);
            } catch (ConversionException e) {
                valid = false;
                e.printStackTrace();
            }
        } else if (rule instanceof QuickRule) {
            valid = rule.isValid(target);
        }

        return valid ? null : rule;
    }

    private Rule validateDataWithRule(final Object data, final Rule rule) {

        boolean valid = false;
        if (rule instanceof AnnotationRule) {
            valid = rule.isValid(data);
        }
        return valid ? null : rule;
    }

    private void triggerViewValidatedCallback(final TargetValidatedAction viewValidatedAction,
                                              final Object target) {
        viewValidatedAction.onAllRulesPassed(target);
    }

    private Object getLastTarget() {
        final Set<?> targets = mTargetRulesMap.keySet();

        Object lastTarget = null;
        for (Object target : targets) {
            lastTarget = target;
        }

        return lastTarget;
    }

    private Object getTargetBefore(final Object target) {
        List targets = new ArrayList(mTargetRulesMap.keySet());

        final int nViews = targets.size();
        Object currentTarget;
        Object previousTarget = null;
        for (int i = 0; i < nViews; i++) {
            currentTarget = targets.get(i);
            if (currentTarget == target) {
                previousTarget = i > 0 ? targets.get(i - 1) : null;
                break;
            }
        }

        return previousTarget;
    }

    static {
        // CheckBoxBooleanAdapter
        registry.register(GQuery.class, /*Boolean.class,*/
                new GQueryBooleanAdapter(),
                AssertFalse.class, AssertTrue.class, Checked.class);
        registry.register(InputElement.class, /*Boolean.class,*/
                new InputElementBooleanAdapter(),
                AssertFalse.class, AssertTrue.class, Checked.class);

        // SpinnerIndexAdapter
        registry.register(GQuery.class, /*Integer.class,*/
                new GQueryIntegerAdapter(),
                Select.class);
        registry.register(SelectElement.class, /*Integer.class,*/
                new SelectElementIntegerAdapter(),
                Select.class);

        // TextViewDoubleAdapter
        registry.register(GQuery.class, /*Double.class,*/
                new GQueryDoubleAdapter(),
                DecimalMax.class, DecimalMin.class);
        registry.register(InputElement.class, /*Double.class,*/
                new InputElementDoubleAdapter(),
                DecimalMax.class, DecimalMin.class);

        // TextViewIntegerAdapter
        registry.register(GQuery.class, /*Integer.class,*/
                new GQueryIntegerAdapter(),
                Max.class, Min.class);
        registry.register(InputElement.class, /*Integer.class,*/
                new InputElementIntegerAdapter(),
                Max.class, Min.class);

        // TextViewStringAdapter
        registry.register(GQuery.class, /*String.class,*/
                new GQueryStringAdapter(),
                ConfirmEmail.class, ConfirmPassword.class, CreditCard.class,
                Digits.class, Domain.class, Email.class, FutureDate.class,
                IpAddress.class, Isbn.class, Length.class, Required.class,
                Password.class, PastDate.class, Pattern.class, Url.class);
        registry.register(InputElement.class, /*String.class,*/
                new InputElementStringAdapter(),
                ConfirmEmail.class, ConfirmPassword.class, CreditCard.class,
                Digits.class, Domain.class, Email.class, FutureDate.class,
                IpAddress.class, Isbn.class, Length.class, Required.class,
                Password.class, PastDate.class, Pattern.class, Url.class);
    }
}
