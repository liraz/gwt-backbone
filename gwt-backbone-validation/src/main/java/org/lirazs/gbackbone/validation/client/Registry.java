package org.lirazs.gbackbone.validation.client;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.query.client.GQuery;
import org.lirazs.gbackbone.reflection.client.ClassType;
import org.lirazs.gbackbone.reflection.client.ReflectionUtils;
import org.lirazs.gbackbone.reflection.client.TypeOracle;
import org.lirazs.gbackbone.validation.client.adapter.*;
import org.lirazs.gbackbone.validation.client.annotation.ValidateUsing;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maintains a registry of all targets and
 * TargetDataAdapters that are registered to rule
 * {@link java.lang.annotation.Annotation}s.
 */
public class Registry {

    // Stock adapters that come with backbone validation
    private static final Map<Class<?>, Map<Class<?>, TargetDataAdapter>> stockAdaptersMap = new HashMap<>();

    private Map<Class<? extends Annotation>, HashMap<Class<?>, TargetDataAdapter>> mappingsMap;

    Registry() {
        mappingsMap = new HashMap<>();
    }

    /**
     * This is a convenience method for Annotations that operate on GQuery/InputElement/SelectElement.
     * Use this to register your custom annotation if your {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule} performs validations on
     * {@link java.lang.String}s, {@link java.lang.Integer}s, {@link java.lang.Float}s and
     * {@link java.lang.Double} values.
     *
     * @param ruleAnnotations  Varargs of rule {@link java.lang.annotation.Annotation}s that operate
     *      on GQuery/InputElement/SelectElement's.
     */
    @SuppressWarnings("unchecked")
    public void register(final Class<? extends Annotation>... ruleAnnotations) {
        for (Class<? extends Annotation> ruleAnnotation : ruleAnnotations) {
            ClassType classType = TypeOracle.Instance.getClassType(ruleAnnotation);

            final ValidateUsing validateUsing = classType.getAnnotation(ValidateUsing.class);
            final Class<?> ruleDataType = Reflector.getRuleDataType(validateUsing);

            // GQuery adapters
            Map<Class<?>, TargetDataAdapter> viewDataAdapterMap =
                    stockAdaptersMap.get(GQuery.class);
            if (viewDataAdapterMap != null) {
                TargetDataAdapter dataAdapter = viewDataAdapterMap.get(ruleDataType);
                if (dataAdapter != null) {
                    register(GQuery.class, dataAdapter, ruleAnnotation);
                } else {
                    String message = "Unable to find a matching adapter for `" + ruleAnnotation.getName() + "`, that returns a `"
                            + ruleDataType + "`.";
                    throw new IllegalStateException(message);
                }
            }

            // input element adapters
            viewDataAdapterMap = stockAdaptersMap.get(InputElement.class);
            if (viewDataAdapterMap != null) {
                TargetDataAdapter dataAdapter = viewDataAdapterMap.get(ruleDataType);
                if (dataAdapter != null) {
                    register(InputElement.class, dataAdapter, ruleAnnotation);
                } else {
                    String message = "Unable to find a matching adapter for `" + ruleAnnotation.getName() + "`, that returns a `"
                            + ruleDataType + "`.";
                    throw new IllegalStateException(message);
                }
            }

            // select element adapters
            viewDataAdapterMap = stockAdaptersMap.get(SelectElement.class);
            if (viewDataAdapterMap != null) {
                TargetDataAdapter dataAdapter = viewDataAdapterMap.get(ruleDataType);
                if (dataAdapter != null) {
                    register(SelectElement.class, dataAdapter, ruleAnnotation);
                } else {
                    String message = "Unable to find a matching adapter for `" + ruleAnnotation.getName() + "`, that returns a `"
                            + ruleDataType + "`.";
                    throw new IllegalStateException(message);
                }
            }
        }
    }

    /**
     * Registers {@link TargetDataAdapter}s for the associated
     * {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule}s and their targets.
     *
     * @param viewType  The target type on which the {@link org.lirazs.gbackbone.validation.client.rule.AnnotationRule}
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
            final Class<TARGET> viewType,
            final TargetDataAdapter<TARGET, DATA_TYPE> targetDataAdapter,
            final Class<? extends Annotation>... ruleAnnotations) {

        if (ruleAnnotations != null && ruleAnnotations.length > 0) {
            for (Class<? extends Annotation> ruleAnnotation : ruleAnnotations) {
                register(ruleAnnotation, viewType, targetDataAdapter);
            }
        }
    }

    /**
     * Retrieve all registered rule annotations.
     *
     * @return {@link java.util.Set} containing all registered rule
     *      {@link java.lang.annotation.Annotation}s.
     */
    public Set<Class<? extends Annotation>> getRegisteredAnnotations() {
        return mappingsMap.keySet();
    }

    /**
     * Retrieves the registered {@link TargetDataAdapter} associated
     * with the rule {@link java.lang.annotation.Annotation} and
     * target. If no registered adapter is not found, the method looks for a
     * compatible adapter instead.
     *
     * @param <TARGET>  Type parameter that ensures type safety for the target and
     *      the TargetDataAdapter.
     *
     * @param annotationType  The rule annotation type that requires a data adapter.
     * @param viewType  The target whose adapter we are looking for.
     *
     * @return Registered or compatible
     *      {@link TargetDataAdapter} or null if none is
     *      found.
     */
    @SuppressWarnings("unchecked")
    public <TARGET> TargetDataAdapter<TARGET, ?> getDataAdapter(
            final Class< ? extends Annotation> annotationType,
            final Class viewType) {

        HashMap<Class<?>, TargetDataAdapter> viewDataAdapterHashMap =
                mappingsMap.get(annotationType);

        // Check for a direct match
        TargetDataAdapter matchingViewAdapter = null;
        if (viewDataAdapterHashMap != null) {
            matchingViewAdapter = viewDataAdapterHashMap.get(viewType);

            // If no 'ViewDataAdapter' is registered, check for a compatible one
            if (matchingViewAdapter == null) {
                matchingViewAdapter = getCompatibleTargetDataAdapter(viewDataAdapterHashMap,
                        viewType);
            }
        }

        return matchingViewAdapter;
    }


    private <TARGET, DATA_TYPE> void register(
            final Class<? extends Annotation> ruleAnnotation,
            final Class<TARGET> view,
            final TargetDataAdapter<TARGET, DATA_TYPE> targetDataAdapter) {

        // Get the view-adapter pairs registered to a rule annotation.
        HashMap<Class<?>, TargetDataAdapter> viewAdapterPairs;
        if (mappingsMap.containsKey(ruleAnnotation)) {
            viewAdapterPairs = mappingsMap.get(ruleAnnotation);
        } else {
            viewAdapterPairs = new HashMap<>();
            mappingsMap.put(ruleAnnotation, viewAdapterPairs);
        }

        if (viewAdapterPairs.containsKey(view)) {
            //String message = String.format("A '%s' for '%s' has already been registered.",
            //        ruleAnnotation.getName(), view.getName());
        } else {
            viewAdapterPairs.put(view, targetDataAdapter);
        }
    }

    private <TARGET> TargetDataAdapter getCompatibleTargetDataAdapter(
            final HashMap<Class<?>, TargetDataAdapter> targetDataAdapterHashMap,
            final Class<TARGET> targetType) {

        TargetDataAdapter compatibleViewAdapter = null;
        Set<Class<?>> registeredTargets = targetDataAdapterHashMap.keySet();
        for (Class<?> registeredTarget : registeredTargets) {
            //TODO: Check if we need this if here...
            //if (ReflectionUtils.isAssignable(registeredTarget, targetType)) {
                compatibleViewAdapter = targetDataAdapterHashMap.get(registeredTarget);
            //}
        }
        return compatibleViewAdapter;
    }

    // Register all views along with their corresponding adapters
    static {
        HashMap<Class<?>, TargetDataAdapter> adapters;

        // GQuery
        adapters = new HashMap<>();
        adapters.put(Boolean.class, new GQueryBooleanAdapter());
        adapters.put(Double.class, new GQueryDoubleAdapter());
        adapters.put(Float.class, new GQueryFloatAdapter());
        adapters.put(Integer.class, new GQueryIntegerAdapter());
        adapters.put(String.class, new GQueryStringAdapter());
        adapters.put(String[].class, new GQueryStringArrayAdapter());
        stockAdaptersMap.put(GQuery.class, adapters);


        // InputElement
        adapters = new HashMap<>();
        adapters.put(Boolean.class, new InputElementBooleanAdapter());
        adapters.put(Double.class, new InputElementDoubleAdapter());
        adapters.put(Float.class, new InputElementFloatAdapter());
        adapters.put(Integer.class, new InputElementIntegerAdapter());
        adapters.put(String.class, new InputElementStringAdapter());
        stockAdaptersMap.put(InputElement.class, adapters);


        // SelectElement
        adapters = new HashMap<>();
        adapters.put(Double.class, new SelectElementDoubleAdapter());
        adapters.put(Float.class, new SelectElementFloatAdapter());
        adapters.put(Integer.class, new SelectElementIntegerAdapter());
        adapters.put(String.class, new SelectElementStringAdapter());
        stockAdaptersMap.put(SelectElement.class, adapters);
    }
}
