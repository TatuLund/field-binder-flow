package org.vaadin.fieldbinder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ErrorMessageProvider;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.StatusChangeEvent;
import com.vaadin.flow.data.binder.StatusChangeListener;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * FieldBinder is a little sibling of {@link Binder} for special case of single
 * field bindings. FieldBinder enables to use same Converters, Validators and
 * similar API to Binder with single field binding.
 * 
 * FieldBinder connects one {@code Field} component with value with one direction binding. 
 * <p>
 * A binder is a <i>binding</i>, representing the mapping of a single field,
 * through converters and validators, and acts as a buffer for bound value. 
 * <p>
 * A binder instance can be bound to a single value and field instance at a time,
 * but can be rebound as needed.
 * <p>
 * Unless otherwise specified, {@code FieldBinder} method arguments cannot be null.
 *
 * @see FieldBindingBuilder
 * @see FieldBinding
 * @see HasValue
 */
public class FieldBinder<TARGET> implements Serializable {


    /**
     * Creates a binding between a field and a data property.
     *
     * @param <TARGET>
     *            the target data type of the binding, matches the field type
     *            until a converter has been set
     *
     * @see FieldBinder#forField(HasValue)
     */
    public interface FieldBindingBuilder<TARGET> extends Serializable {
        /**
         * Gets the field the binding is being built for.
         *
         * @return the field this binding is being built for
         */
        public HasValue<?, ?> getField();

        public FieldBinding<TARGET> bind(TARGET value);
        
        /**
         * Adds a validator to this binding. Validators are applied, in
         * registration order, when the field value is written to the backing
         * property. If any validator returns a failure, the property value is
         * not updated.
         *
         * @see #withValidator(SerializablePredicate, String)
         * @see #withValidator(SerializablePredicate, ErrorMessageProvider)
         *
         * @param validator
         *            the validator to add, not null
         * @return this binding, for chaining
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public FieldBindingBuilder<TARGET> withValidator(
                Validator<? super TARGET> validator);


        /**
         * A convenience method to add a validator to this binding using the
         * {@link Validator#from(SerializablePredicate, String)} factory method.
         * <p>
         * Validators are applied, in registration order, when the field value
         * is written to the backing property. If any validator returns a
         * failure, the property value is not updated.
         *
         * @see #withValidator(Validator)
         * @see #withValidator(SerializablePredicate, String, ErrorLevel)
         * @see #withValidator(SerializablePredicate, ErrorMessageProvider)
         * @see Validator#from(SerializablePredicate, String)
         *
         * @param predicate
         *            the predicate performing validation, not null
         * @param message
         *            the error message to report in case validation failure
         * @return this binding, for chaining
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default FieldBindingBuilder<TARGET> withValidator(
                SerializablePredicate<? super TARGET> predicate,
                String message) {
            return withValidator(Validator.from(predicate, message));
        }

        /**
         * A convenience method to add a validator to this binding using the
         * {@link Validator#from(SerializablePredicate, String, ErrorLevel)}
         * factory method.
         * <p>
         * Validators are applied, in registration order, when the field value
         * is written to the backing property. If any validator returns a
         * failure, the property value is not updated.
         *
         * @see #withValidator(Validator)
         * @see #withValidator(SerializablePredicate, String)
         * @see #withValidator(SerializablePredicate, ErrorMessageProvider,
         *      ErrorLevel)
         * @see Validator#from(SerializablePredicate, String)
         *
         * @param predicate
         *            the predicate performing validation, not null
         * @param message
         *            the error message to report in case validation failure
         * @param errorLevel
         *            the error level for failures from this validator, not null
         * @return this binding, for chaining
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default FieldBindingBuilder<TARGET> withValidator(
                SerializablePredicate<? super TARGET> predicate, String message,
                ErrorLevel errorLevel) {
            return withValidator(
                    Validator.from(predicate, message, errorLevel));
        }

        /**
         * A convenience method to add a validator to this binding using the
         * {@link Validator#from(SerializablePredicate, ErrorMessageProvider)}
         * factory method.
         * <p>
         * Validators are applied, in registration order, when the field value
         * is written to the backing property. If any validator returns a
         * failure, the property value is not updated.
         *
         * @see #withValidator(Validator)
         * @see #withValidator(SerializablePredicate, String)
         * @see #withValidator(SerializablePredicate, ErrorMessageProvider,
         *      ErrorLevel)
         * @see Validator#from(SerializablePredicate, ErrorMessageProvider)
         *
         * @param predicate
         *            the predicate performing validation, not null
         * @param errorMessageProvider
         *            the provider to generate error messages, not null
         * @return this binding, for chaining
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default FieldBindingBuilder<TARGET> withValidator(
                SerializablePredicate<? super TARGET> predicate,
                ErrorMessageProvider errorMessageProvider) {
            return withValidator(
                    Validator.from(predicate, errorMessageProvider));
        }

        /**
         * A convenience method to add a validator to this binding using the
         * {@link Validator#from(SerializablePredicate, ErrorMessageProvider, ErrorLevel)}
         * factory method.
         * <p>
         * Validators are applied, in registration order, when the field value
         * is written to the backing property. If any validator returns a
         * failure, the property value is not updated.
         *
         * @see #withValidator(Validator)
         * @see #withValidator(SerializablePredicate, String, ErrorLevel)
         * @see #withValidator(SerializablePredicate, ErrorMessageProvider)
         * @see Validator#from(SerializablePredicate, ErrorMessageProvider,
         *      ErrorLevel)
         *
         * @param predicate
         *            the predicate performing validation, not null
         * @param errorMessageProvider
         *            the provider to generate error messages, not null
         * @param errorLevel
         *            the error level for failures from this validator, not null
         * @return this binding, for chaining
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default FieldBindingBuilder<TARGET> withValidator(
                SerializablePredicate<? super TARGET> predicate,
                ErrorMessageProvider errorMessageProvider,
                ErrorLevel errorLevel) {
            return withValidator(Validator.from(predicate, errorMessageProvider,
                    errorLevel));
        }

        /**
         * Maps the binding to another data type using the given
         * {@link Converter}.
         * <p>
         * A converter is capable of converting between a presentation type,
         * which must match the current target data type of the binding, and a
         * model type, which can be any data type and becomes the new target
         * type of the binding. When invoking
         * {@link #bind(Object)}, the target type of the binding
         * must match the value type.
         * <p>
         * For instance, a {@code TextField} can be bound to an integer-typed
         * property using an appropriate converter such as a
         * {@link StringToIntegerConverter}.
         *
         * @param <NEWTARGET>
         *            the type to convert to
         * @param converter
         *            the converter to use, not null
         * @return a new binding with the appropriate type
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public <NEWTARGET> FieldBindingBuilder<NEWTARGET> withConverter(
                Converter<TARGET, NEWTARGET> converter);
        
        /**
         * Maps the binding to another data type using the mapping functions and
         * a possible exception as the error message.
         * <p>
         * The mapping functions are used to convert between a presentation
         * type, which must match the current target data type of the binding,
         * and a model type, which can be any data type and becomes the new
         * target type of the binding. When invoking
         * {@link #bind(Object)}, the target type of the binding
         * must match the value type.
         * <p>
         * For instance, a {@code TextField} can be bound to an integer-typed
         * property using appropriate functions such as:
         * <code>withConverter(Integer::valueOf, String::valueOf);</code>
         *
         * @param <NEWTARGET>
         *            the type to convert to
         * @param toModel
         *            the function which can convert from the old target type to
         *            the new target type
         * @param toPresentation
         *            the function which can convert from the new target type to
         *            the old target type
         * @return a new binding with the appropriate type
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default <NEWTARGET> FieldBindingBuilder<NEWTARGET> withConverter(
                SerializableFunction<TARGET, NEWTARGET> toModel,
                SerializableFunction<NEWTARGET, TARGET> toPresentation) {
            return withConverter(Converter.from(toModel, toPresentation,
                    exception -> exception.getMessage()));
        }

        /**
         * Maps the binding to another data type using the mapping functions and
         * the given error error message if a value cannot be converted to the
         * new target type.
         * <p>
         * The mapping functions are used to convert between a presentation
         * type, which must match the current target data type of the binding,
         * and a model type, which can be any data type and becomes the new
         * target type of the binding. When invoking
         * {@link #bind(Object)}, the target type of the binding
         * must match the value type.
         * <p>
         * For instance, a {@code TextField} can be bound to an integer-typed
         * property using appropriate functions such as:
         * <code>withConverter(Integer::valueOf, String::valueOf);</code>
         *
         * @param <NEWTARGET>
         *            the type to convert to
         * @param toModel
         *            the function which can convert from the old target type to
         *            the new target type
         * @param toPresentation
         *            the function which can convert from the new target type to
         *            the old target type
         * @param errorMessage
         *            the error message to use if conversion using
         *            <code>toModel</code> fails
         * @return a new binding with the appropriate type
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        public default <NEWTARGET> FieldBindingBuilder<NEWTARGET> withConverter(
                SerializableFunction<TARGET, NEWTARGET> toModel,
                SerializableFunction<NEWTARGET, TARGET> toPresentation,
                String errorMessage) {
            return withConverter(Converter.from(toModel, toPresentation,
                    exception -> errorMessage));
        }

        /**
         * Maps binding value {@code null} to given null representation and back
         * to {@code null} when converting back to model value.
         *
         * @param nullRepresentation
         *            the value to use instead of {@code null}
         * @return a new binding with null representation handling.
         */
        public default FieldBindingBuilder<TARGET> withNullRepresentation(
                TARGET nullRepresentation) {
            return withConverter(
                    fieldValue -> Objects.equals(fieldValue, nullRepresentation)
                            ? null
                            : fieldValue,
                    modelValue -> Objects.isNull(modelValue)
                            ? nullRepresentation
                            : modelValue);
        }

        /**
         * Sets the given {@code label} to show an error message if validation
         * fails.
         * <p>
         * The validation state of each field is updated whenever the user
         * modifies the value of that field. The validation state is by default
         * shown using {@link HasValidation#setErrorMessage} which is used
         * by the layout that the field is shown in. Most built-in layouts will
         * show this as a red exclamation mark icon next to the component, so
         * that hovering or tapping the icon shows a tooltip with the message
         * text.
         * <p>
         * This method allows to customize the way a binder displays error
         * messages to get more flexibility than what
         * {@link HasValidation#setErrorMessage} provides (it replaces the
         * default behavior).
         * <p>
         * This is just a shorthand for
         * {@link #withValidationStatusHandler(FieldBindingValidationStatusHandler)}
         * method where the handler instance hides the {@code label} if there is
         * no error and shows it with validation error message if validation
         * fails. It means that it cannot be called after
         * {@link #withValidationStatusHandler(FieldBindingValidationStatusHandler)}
         * method call or
         * {@link #withValidationStatusHandler(FieldBindingValidationStatusHandler)}
         * after this method call.
         *
         * @see #withValidationStatusHandler(FieldBindingValidationStatusHandler)
         * @see HasValidation#setErrorMessage(ErrorMessage)
         * @param label
         *            label to show validation status for the field
         * @return this binding, for chaining
         */
        public default FieldBindingBuilder<TARGET> withStatusLabel(
                HasText label) {
            return withValidationStatusHandler(status -> {
                label.setText(status.getMessage().orElse(""));
                // Only show the label when validation has failed
                setVisible(label, status.isError());
            });
        }

        /**
         * Sets a {@link FieldBindingValidationStatusHandler} to track validation
         * status changes.
         * <p>
         * The validation state of each field is updated whenever the user
         * modifies the value of that field. The validation state is by default
         * shown using {@link HasValidation#setErrorMessage} which is used
         * by the layout that the field is shown in. Most built-in layouts will
         * show this as a red exclamation mark icon next to the component, so
         * that hovering or tapping the icon shows a tooltip with the message
         * text.
         * <p>
         * This method allows to customize the way a binder displays error
         * messages to get more flexibility than what
         * {@link HasValidation#setErrorMessage} provides (it replaces the
         * default behavior).
         * <p>
         * The method may be called only once. It means there is no chain unlike
         * {@link #withValidator(Validator)} or
         * {@link #withConverter(Converter)}. Also it means that the shorthand
         * method {@link #withStatusLabel(Label)} also may not be called after
         * this method.
         *
         * @see #withStatusLabel(Label)
         * @see HasValidation#setErrorMessage(ErrorMessage)
         * @param handler
         *            status change handler
         * @return this binding, for chaining
         */
        public FieldBindingBuilder<TARGET> withValidationStatusHandler(
                FieldBindingValidationStatusHandler handler);

        /**
         * Sets the field to be required. This means two things:
         * <ol>
         * <li>the required indicator will be displayed for this field</li>
         * <li>the field value is validated for not being empty, i.e. that the
         * field's value is not equal to what {@link HasValue#getEmptyValue()}
         * returns</li>
         * </ol>
         * <p>
         * For localizing the error message, use
         * {@link #asRequired(ErrorMessageProvider)}.
         *
         * @see #asRequired(ErrorMessageProvider)
         * @see HasValue#setRequiredIndicatorVisible(boolean)
         * @see HasValue#isEmpty()
         * @param errorMessage
         *            the error message to show for the invalid value
         * @return this binding, for chaining
         */
        public default FieldBindingBuilder<TARGET> asRequired(
                String errorMessage) {
            return asRequired(context -> errorMessage);
        }

        /**
         * Sets the field to be required. This means two things:
         * <ol>
         * <li>the required indicator will be displayed for this field</li>
         * <li>the field value is validated for not being empty, i.e. that the
         * field's value is not equal to what {@link HasValue#getEmptyValue()}
         * returns</li>
         * </ol>
         * <p>
         * For setting an error message, use {@link #asRequired(String)}.
         * <p>
         * For localizing the error message, use
         * {@link #asRequired(ErrorMessageProvider)}.
         *
         * @see #asRequired(String)
         * @see #asRequired(ErrorMessageProvider)
         * @see HasValue#setRequiredIndicatorVisible(boolean)
         * @see HasValue#isEmpty()
         * @return this binding, for chaining
         */
        public default FieldBindingBuilder<TARGET> asRequired() {
            return asRequired(context -> "");
        }

        /**
         * Sets the field to be required. This means two things:
         * <ol>
         * <li>the required indicator will be displayed for this field</li>
         * <li>the field value is validated for not being empty, i.e. that the
         * field's value is not equal to what {@link HasValue#getEmptyValue()}
         * returns</li>
         * </ol>
         *
         * @see HasValue#setRequiredIndicatorVisible(boolean)
         * @see HasValue#isEmpty()
         * @param errorMessageProvider
         *            the provider for localized validation error message
         * @return this binding, for chaining
         */
        public FieldBindingBuilder<TARGET> asRequired(
                ErrorMessageProvider errorMessageProvider);

        /**
         * Sets the field to be required and delegates the required check to a
         * custom validator. This means two things:
         * <ol>
         * <li>the required indicator will be displayed for this field</li>
         * <li>the field value is validated by {@code requiredValidator}</li>
         * </ol>
         *
         * @see HasValue#setRequiredIndicatorVisible(boolean)
         * @param requiredValidator
         *            validator responsible for the required check
         * @return this binding, for chaining
         */
        public FieldBindingBuilder<TARGET> asRequired(
                Validator<TARGET> requiredValidator);
        
    }

    /**
     * An internal implementation of {@code BindingBuilder}.
     *
     * @param <FIELDVALUE>
     *            the value type of the field
     * @param <TARGET>
     *            the target data type of the binding, matches the field type
     *            until a converter has been set
     */
    protected static class FieldBindingBuilderImpl<FIELDVALUE, TARGET>
            implements FieldBindingBuilder<TARGET> {

        private FieldBinder<TARGET> binder;

        private final HasValue<?,FIELDVALUE> field;
        private FieldBindingValidationStatusHandler statusHandler;
        private boolean isStatusHandlerChanged;

        private boolean bound;

        /**
         * Contains all converters and validators chained together in the
         * correct order.
         */
        private Converter<FIELDVALUE, ?> converterValidatorChain;

        /**
         * Creates a new binding builder associated with the given field.
         * Initializes the builder with the given converter chain and status
         * change handler.
         *
         * @param binder
         *            the binder this instance is connected to, not null
         * @param field
         *            the field to bind, not null
         * @param converterValidatorChain
         *            the converter/validator chain to use, not null
         * @param statusHandler
         *            the handler to track validation status, not null
         */
        protected FieldBindingBuilderImpl(FieldBinder<TARGET> binder,
                HasValue<?,FIELDVALUE> field,
                Converter<FIELDVALUE, TARGET> converterValidatorChain,
                FieldBindingValidationStatusHandler statusHandler) {
            this.field = field;
            this.binder = binder;
            this.converterValidatorChain = converterValidatorChain;
            this.statusHandler = statusHandler;
        }

        @Override
        public FieldBinding<TARGET> bind(TARGET value) {
            checkUnbound();
            Objects.requireNonNull(value, "value cannot be null");

            FieldBindingImpl<FIELDVALUE, TARGET> binding = new FieldBindingImpl<>(
                    this, value);

            getBinder().binding = binding;
            binding.initFieldValue(value);
            getBinder().fireStatusChangeEvent(false);

            bound = true;

            return binding;
        }

        @SuppressWarnings("unchecked")
        private Converter<TARGET, Object> createConverter(Class<?> getterType) {
            return Converter.from(fieldValue -> getterType.cast(fieldValue),
                    propertyValue -> (TARGET) propertyValue, exception -> {
                        throw new RuntimeException(exception);
                    });
        }

        @Override
        public FieldBindingBuilder<TARGET> withValidator(
                Validator<? super TARGET> validator) {
            checkUnbound();
            Objects.requireNonNull(validator, "validator cannot be null");

            converterValidatorChain = ((Converter<FIELDVALUE, TARGET>) converterValidatorChain)
                    .chain(new ValidatorAsConverter<>(validator));
            return this;
        }

        @Override
        public <NEWTARGET> FieldBindingBuilder<NEWTARGET> withConverter(
                Converter<TARGET, NEWTARGET> converter) {
            return withConverter(converter, true);
        }

        @Override
        public FieldBindingBuilder<TARGET> withValidationStatusHandler(
                FieldBindingValidationStatusHandler handler) {
            checkUnbound();
            Objects.requireNonNull(handler, "handler cannot be null");
            if (isStatusHandlerChanged) {
                throw new IllegalStateException("A "
                        + FieldBindingValidationStatusHandler.class.getSimpleName()
                        + " has already been set");
            }
            isStatusHandlerChanged = true;
            statusHandler = handler;
            return this;
        }

        @Override
        public FieldBindingBuilder<TARGET> asRequired(
                ErrorMessageProvider errorMessageProvider) {
            return asRequired(Validator.from(
                    value -> !Objects.equals(value, field.getEmptyValue()),
                    errorMessageProvider));
        }

        @Override
        public FieldBindingBuilder<TARGET> asRequired(
                Validator<TARGET> customRequiredValidator) {
            checkUnbound();
            field.setRequiredIndicatorVisible(true);
            return withValidator(customRequiredValidator);
        }

        /**
         * Implements {@link #withConverter(Converter)} method with additional
         * possibility to disable (reset) default null representation converter.
         * <p>
         * The method {@link #withConverter(Converter)} calls this method with
         * {@code true} provided as the second argument value.
         *
         * @see #withConverter(Converter)
         *
         * @param converter
         *            the converter to use, not null
         * @param resetNullRepresentation
         *            if {@code true} then default null representation will be
         *            deactivated (if not yet), otherwise it won't be removed
         * @return a new binding with the appropriate type
         * @param <NEWTARGET>
         *            the type to convert to
         * @throws IllegalStateException
         *             if {@code bind} has already been called
         */
        protected <NEWTARGET> FieldBindingBuilder<NEWTARGET> withConverter(
                Converter<TARGET, NEWTARGET> converter,
                boolean resetNullRepresentation) {
            checkUnbound();
            Objects.requireNonNull(converter, "converter cannot be null");

            if (resetNullRepresentation) {
                getBinder().initialConverters.get(field).setIdentity();
            }

            converterValidatorChain = ((Converter<FIELDVALUE, TARGET>) converterValidatorChain)
                    .chain(converter);

            return (FieldBindingBuilder<NEWTARGET>) this;
        }
        
        /**
         * Returns the {@code Binder} connected to this {@code Binding}
         * instance.
         *
         * @return the binder
         */
        protected FieldBinder<TARGET> getBinder() {
            return binder;
        }

        /**
         * Throws if this binding is already completed and cannot be modified
         * anymore.
         *
         * @throws IllegalStateException
         *             if this binding is already bound
         */
        protected void checkUnbound() {
            if (bound) {
                throw new IllegalStateException(
                        "cannot modify binding: already bound to a property");
            }
        }

        @Override
        public HasValue<?,FIELDVALUE> getField() {
            return field;
        }
    }    
    
    /**
     * Represents the binding between a field and a data property.
     *
     * @param <TARGET>
     *            the target data type of the binding, matches the field type
     *            unless a converter has been set
     *
     * @see FieldBinder#forField(HasValue)
     */    
    public interface FieldBinding<TARGET> extends Serializable {

        /**
         * Gets the field the binding uses.
         *
         * @return the field for the binding
         */
        public HasValue<?,?> getField();

        /**
         * Validates the field value and returns a {@code ValidationStatus}
         * instance representing the outcome of the validation. This method is a
         * short-hand for calling {@link #validate(boolean)} with
         * {@code fireEvent} {@code true}.
         *
         * @see #validate(boolean)
         * @see FieldBinder#validate()
         * @see Validator#apply(Object, ValueContext)
         *
         * @return the validation result.
         */
        public default FieldBindingValidationStatus<TARGET> validate() {
            return validate(true);
        }

        /**
         * Validates the field value and returns a {@code ValidationStatus}
         * instance representing the outcome of the validation.
         *
         * @see #validate()
         *
         * @param fireEvent
         *            {@code true} to fire status event; {@code false} to not
         * @return the validation result.
         */
        public FieldBindingValidationStatus<TARGET> validate(boolean fireEvent);

        /**
         * Gets the validation status handler for this Binding.
         *
         * @return the validation status handler for this binding
         */
        public FieldBindingValidationStatusHandler getValidationStatusHandler();

        /**
         * Returns typed validated bound value from binding buffer, if value was not
         * valid, then returns null
         *
         * @return validated value or null
         *             
         */
        public TARGET getValue();
        
        /**
         * Unbinds the binding from its respective {@code FieldBinder} Removes any
         * {@code ValueChangeListener} {@code Registration} from associated
         * {@code HasValue}.
         */
        public void unbind();

        /**
         * Sets the read-only status on for this Binding. Setting a Binding
         * read-only will mark the field read-only.
         * <p>
         * This helper method is the preferred way to control the read-only
         * state of the bound field.
         *
         * @param readOnly
         *            {@code true} to set binding read-only; {@code false} to
         *            enable writes
         */
        public void setReadOnly(boolean readOnly);

        /**
         * Gets the current read-only status for this Binding.
         *
         * @see #setReadOnly(boolean)
         *
         * @return {@code true} if read-only; {@code false} if not
         */
        public boolean isReadOnly();

    }

    /**
     * An internal implementation of {@code FieldBinding}.
     *
     * @param <FIELDVALUE>
     *            the value type of the field
     * @param <TARGET>
     *            the target data type of the binding, matches the field type
     *            unless a converter has been set
     */
    protected static class FieldBindingImpl<FIELDVALUE, TARGET>
            implements FieldBinding<TARGET> {

        private FieldBinder<TARGET> binder;

        private HasValue<?,FIELDVALUE> field;
        private final FieldBindingValidationStatusHandler statusHandler;

        private TARGET value;

        private boolean readOnly;

        private final Registration onValueChange;
        private boolean valueInit = false;
        
        /**
         * Contains all converters and validators chained together in the
         * correct order.
         */
        private final Converter<FIELDVALUE, TARGET> converterValidatorChain;

        public FieldBindingImpl(FieldBindingBuilderImpl<FIELDVALUE, TARGET> builder,
                TARGET value) {
            this.binder = builder.getBinder();
            this.field = builder.field;
            this.statusHandler = builder.statusHandler;
            converterValidatorChain = ((Converter<FIELDVALUE, TARGET>) builder.converterValidatorChain);

            onValueChange = getField()
                    .addValueChangeListener(this::handleFieldValueChange);

            this.value = value;            
        }
        
        @Override
        public HasValue<?,FIELDVALUE> getField() {
            return field;
        }

        /**
         * Finds an appropriate locale to be used in conversion and validation.
         *
         * @return the found locale, not null
         */
        protected Locale findLocale() {
            Locale locale = null;
            if (UI.getCurrent() != null) {
                locale = UI.getCurrent().getLocale();
            }
            if (locale == null) {
                locale = Locale.getDefault();
            }
            return locale;
        }

        @Override
        public FieldBindingValidationStatus<TARGET> validate(boolean fireEvent) {
            Objects.requireNonNull(binder,
                    "This Binding is no longer attached to a Binder");
            FieldBindingValidationStatus<TARGET> status = doValidation();
            if (fireEvent) {
                getBinder().getValidationStatusHandler()
                        .statusChange(new FieldBinderValidationStatus(getBinder(),status));
                getBinder().fireStatusChangeEvent(status.isError());
            }
            return status;
        }

        /**
         * Removes this binding from its binder and unregisters the
         * {@code ValueChangeListener} from any bound {@code HasValue}.
         */
        @Override
        public void unbind() {
            if (onValueChange != null) {
                onValueChange.remove();
            }
            binder.removeBindingInternal(this);
            binder = null;
            field = null;
        }

        /**
         * Returns the field value run through all converters and validators,
         * but doesn't pass the {@link BindingValidationStatus} to any status
         * handler.
         *
         * @return the result of the conversion
         */
        private Result<TARGET> doConversion() {
            FIELDVALUE fieldValue = field.getValue();
            return converterValidatorChain.convertToModel(fieldValue,
                    createValueContext());
        }
        
        private FieldBindingValidationStatus<TARGET> toValidationStatus(
                Result<TARGET> result) {
            return new FieldBindingValidationStatus<>(result, this);
        }

        /**
         * Returns the field value run through all converters and validators,
         * but doesn't pass the {@link BindingValidationStatus} to any status
         * handler.
         *
         * @return the validation status
         */
        private FieldBindingValidationStatus<TARGET> doValidation() {
            return toValidationStatus(doConversion());
        }

        /**
         * Creates a value context from the current state of the binding and its
         * field.
         *
         * @return the value context
         */
        protected ValueContext createValueContext() {
            if (field instanceof Component) {
                return new ValueContext((Component) field, field);
            }
            return new ValueContext(null, field, findLocale());
        }
        
        /**
         * Sets the field value to be the one given in @see {@link FieldBindingBuilder#bind(Object)}
         *
         * @param value
         *            the value to be set in Field
         */
        private void initFieldValue(TARGET value) {
            assert value != null;
            assert onValueChange != null;
            valueInit = true;
            try {
                getField().setValue(convertDataToFieldType(value));
            } finally {
                valueInit = false;
            }
        }

        private FIELDVALUE convertDataToFieldType(TARGET value) {
            TARGET target = value;
            ValueContext valueContext = createValueContext();
            return converterValidatorChain.convertToPresentation(target,
                    valueContext);
        }

        private TARGET convertDataToModelType(FIELDVALUE value) {
        	FIELDVALUE fieldValue = value;
            ValueContext valueContext = createValueContext();
            return converterValidatorChain.convertToModel(fieldValue, valueContext).getOrThrow(e -> new NumberFormatException());
        }


        /**
         * Handles the value change triggered by the bound field.
         *
         * @param event
         */
        private void handleFieldValueChange(
                ValueChangeEvent<FIELDVALUE> event) {
            // Don't handle change events when setting initial value
            if (valueInit) {
                return;
            }

            if (binder != null) {
                // Inform binder of changes; 
                getBinder().handleFieldValueChange(this);
                getBinder().fireEvent(event);
            }
        }

        /**
         * Write the field value by invoking the setter function on the given
         * if the value passes all registered validators.
         *
         */
        private FieldBindingValidationStatus<TARGET> writeFieldValue() {
            
            Result<TARGET> result = doConversion();
            if (!isReadOnly()) {
                result.ifOk(convertedValue -> value = convertedValue);
            }
            return toValidationStatus(result);
        }
     
        /**
         * Returns the {@code Binder} connected to this {@code Binding}
         * instance.
         *
         * @return the binder
         */
        protected FieldBinder<TARGET> getBinder() {
            return binder;
        }
        
        @Override
        public FieldBindingValidationStatusHandler getValidationStatusHandler() {
            return statusHandler;
        }

        @Override
        public TARGET getValue() {
        	return value;
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
            getField().setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() {
            return readOnly;
        }

    }

    /**
     * Wraps a validator as a converter.
     * <p>
     * The type of the validator must be of the same type as this converter or a
     * super type of it.
     *
     * @param <T>
     *            the type of the converter
     */
    private static class ValidatorAsConverter<T> implements Converter<T, T> {

        private final Validator<? super T> validator;

        /**
         * Creates a new converter wrapping the given validator.
         *
         * @param validator
         *            the validator to wrap
         */
        public ValidatorAsConverter(Validator<? super T> validator) {
            this.validator = validator;
        }

        @Override
        public Result<T> convertToModel(T value, ValueContext context) {
            ValidationResult validationResult = validator.apply(value, context);
            return new FieldValidationResultWrap<>(value, validationResult);
        }

        @Override
        public T convertToPresentation(T value, ValueContext context) {
            return value;
        }

    }

    /**
     * Converter decorator-strategy pattern to use initially provided "delegate"
     * converter to execute its logic until the {@code setIdentity()} method is
     * called. Once the method is called the class changes its behavior to the
     * same as {@link Converter#identity()} behavior.
     */
    private static class ConverterDelegate<FIELDVALUE>
            implements Converter<FIELDVALUE, FIELDVALUE> {

        private Converter<FIELDVALUE, FIELDVALUE> delegate;

        private ConverterDelegate(Converter<FIELDVALUE, FIELDVALUE> converter) {
            delegate = converter;
        }

        @Override
        public Result<FIELDVALUE> convertToModel(FIELDVALUE value,
                ValueContext context) {
            if (delegate == null) {
                return Result.ok(value);
            } else {
                return delegate.convertToModel(value, context);
            }
        }

        @Override
        public FIELDVALUE convertToPresentation(FIELDVALUE value,
                ValueContext context) {
            if (delegate == null) {
                return value;
            } else {
                return delegate.convertToPresentation(value, context);
            }
        }

        void setIdentity() {
            delegate = null;
        }
    }        

    private FieldBinding<?> binding = null;

    private final List<Validator<?>> validators = new ArrayList<>();

    private final Map<HasValue<?,?>, ConverterDelegate<?>> initialConverters = new IdentityHashMap<>();

    private HashMap<Class<?>, List<SerializableConsumer<?>>> listeners = new HashMap<>();

    private HasText statusLabel;

    private FieldBinderValidationStatusHandler statusHandler;

    private FieldBinding<?> changedBinding = null;

    public FieldBinder() {
    }
    
    private void fireStatusChangeEvent(boolean hasValidationErrors) {
        FieldBinderStatusChangeEvent event = new FieldBinderStatusChangeEvent(this,
                hasValidationErrors);
        fireEvent(event);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fireEvent(Object event) {
        listeners.entrySet().stream().filter(
                entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .forEach(entry -> {
                    for (Consumer consumer : entry.getValue()) {
                        consumer.accept(event);
                    }
                });
    }
 
    /**
     * Creates a new binding for the given field. The returned builder may be
     * further configured before invoking
     * {@link FieldBindingBuilder#bind(Object)} which completes the
     * binding. Until {@code Binding.bind} is called, the binding has no effect.
     * <p>
     * <strong>Note:</strong> Not all {@link HasValue} implementations support
     * passing {@code null} as the value. For these the Binder will
     * automatically change {@code null} to a null representation provided by
     * {@link HasValue#getEmptyValue()}. This conversion is one-way only, if you
     * want to have a two-way mapping back to {@code null}, use
     * {@link FieldBindingBuilder#withNullRepresentation(Object)}.
     *
     * @param <FIELDVALUE>
     *            the value type of the field
     * @param field
     *            the field to be bound, not null
     * @return the new binding
     */
    public <FIELDVALUE> FieldBindingBuilder<FIELDVALUE> forField(
            HasValue<?,FIELDVALUE> field) {
        Objects.requireNonNull(field, "field cannot be null");
        // clear previous errors for this field
        clearError(field);
        getStatusLabel().ifPresent(label -> label.setText(""));

        return createBinding(field, createNullRepresentationAdapter(field),
                this::handleValidationStatus)
                        .withValidator(field instanceof HasValidator
                                ? ((HasValidator) field).getDefaultValidator()
                                : Validator.alwaysPass());
    }

    private <FIELDVALUE> Converter<FIELDVALUE, FIELDVALUE> createNullRepresentationAdapter(
            HasValue<?,FIELDVALUE> field) {
        Converter<FIELDVALUE, FIELDVALUE> nullRepresentationConverter = Converter
                .from(fieldValue -> fieldValue,
                        modelValue -> Objects.isNull(modelValue)
                                ? field.getEmptyValue()
                                : modelValue,
                        exception -> exception.getMessage());
        ConverterDelegate<FIELDVALUE> converter = new ConverterDelegate<>(
                nullRepresentationConverter);
        initialConverters.put(field, converter);
        return converter;
    }

    /**
     * Return the field this binder has been bound to.
     *
     * @return the fields with the binding
     */
    public HasValue<?,?> getField() {
        return binding.getField();
    }

    /**
     * Finds and removes FieldBinding for the given field. Note that this method
     * and other overloads of removeBinding method do not reset component errors
     * that might have been added to the field and do not remove required
     * indicator of the field no matter if it was set by FieldBinder or not. To reset
     * component errors, {@code field.setErrorMessage(null)} should be called
     * and to remove required indicator,
     * {@code field.setRequiredIndicatorVisible(false)} should be called.
     *
     * @see HasValidation#setErrorMessage
     * @see HasValidation#setRequiredIndicatorVisible
     *
     * @param field
     *            the field to remove from the binding
     * @throws IllegalArgumentException
     *             if the given Field is not in this Binder
     */
    public void removeBinding(HasValue<?,?> field) {
        Objects.requireNonNull(field, "Field can not be null");
        if (binding.getField() != field) {
            throw new IllegalArgumentException(
                    "Provided Field is not in this Binder");        	
        }
        binding.unbind();
    }

    /**
     * Removes the given Binding from this Binder.
     *
     * @see FieldBinder#removeBinding(HasValue)
     * @see HasValidation#setErrorMessage
     * @see HasValidation#setRequiredIndicatorVisible
     *
     * @param binding
     *            the binding to remove
     *
     * @throws IllegalArgumentException
     *             if the given Binding is not in this Binder
     */
    public void removeBinding(FieldBinding<?> binding)
            throws IllegalArgumentException {
        Objects.requireNonNull(binding, "Binding can not be null");
        if (this.binding != binding) {
            throw new IllegalArgumentException(
                    "Provided Binding is not in this Binder");
        }
        binding.unbind();
    }

    private static void setVisible(HasText label, boolean visible) {
        if (visible) {
            label.getElement().getStyle().remove("display");
        } else {
            label.getElement().getStyle().set("display", "none");
        }
    }

    /**
     * Removes (internally) the {@code FieldBinding} from the bound properties map
     * (if present) and from the list of {@code FieldBinding}s. Note that this DOES
     * NOT remove the {@code ValueChangeListener} that the {@code FieldBinding} might
     * have registered with any {@code FieldHasValue}s or decouple the {@code FieldBinder}
     * from within the {@code Binding}. To do that, use
     *
     * {@link FieldBinding#unbind()}
     *
     * This method should just be used for internal cleanup.
     *
     * @param binding
     *            The {@code FieldBinding} to remove from the binding map
     */
    protected void removeBindingInternal(FieldBinding<?> binding) {
        binding = null;
    }
    
    /**
     * Returns the binding for this binder.
     *
     * @return the binding
     */
    protected FieldBinding<?> getBinding() {
        return binding;
    }

//    /**
//     * Returns the event router for this binder.
//     *
//     * @return the event router, not null
//     */
//    protected EventRouter getEventRouter() {
//        if (eventRouter == null) {
//            eventRouter = new EventRouter();
//        }
//        return eventRouter;
//    }

    /**
     * Check whether any of the bound fields' have uncommitted changes.
     * <p>
     *
     * @return whether any bound field's value has changed
     */
    public boolean hasChanges() {
        return changedBinding != null;
    }

    /**
     * Default {@link FieldBindingValidationStatusHandler} functional method
     * implementation.
     *
     * @param status
     *            the validation status
     */
    protected void handleValidationStatus(FieldBindingValidationStatus<?> status) {
        HasValue<?,?> source = status.getField();
        clearError(source);
        if (status.isError()) {
            Optional<ValidationResult> firstError = status
                    .getValidationResults().stream()
                    .filter(ValidationResult::isError).findFirst();
            if (firstError.isPresent()) {
                // Failed with a Validation error
                handleError(source, firstError.get());
            } else {
                // Conversion error
                status.getResult()
                        .ifPresent(result -> handleError(source, result));
            }
        } else {
            // Show first non-error ValidationResult message.
            status.getValidationResults().stream()
                    .filter(result -> result.getErrorLevel().isPresent())
                    .findFirst()
                    .ifPresent(result -> handleError(source, result));
        }
    }

    /**
     * Handles a validation error emitted when trying to write the value of the
     * given field. The default implementation sets the
     * {@link HasValidation#setErrorMessage(ErrorMessage) component error}
     * of the field if it is a HasValidation, otherwise does nothing.
     *
     * @param field
     *            the field with the invalid value
     * @param result
     *            the validation error result
     */
    protected void handleError(HasValue<?,?> field, ValidationResult result) {
        result.getErrorLevel().ifPresent(level -> {
            if (field instanceof HasValidation) {
                HasValidation fieldWithValidation = (HasValidation) field;
                fieldWithValidation.setInvalid(true);
                fieldWithValidation.setErrorMessage(result.getErrorMessage());
            }
        });
    }

    /**
     * Clears the error condition of the given field, if any. The default
     * implementation clears the
     * {@link HasValidation#setErrorMessage(ErrorMessage) component error}
     * of the field if it is a HasValidation, otherwise does nothing.
     *
     * @param field
     *            the field with an invalid value
     */
    protected void clearError(HasValue<?,?> field) {
        if (field instanceof HasValidation) {
            HasValidation fieldWithValidation = (HasValidation) field;
            fieldWithValidation.setInvalid(false);
        }
    }

    /**
     * Sets the status handler to track form status changes.
     * <p>
     * Setting this handler will override the default behavior, which is to let
     * fields show their validation status messages and show binder level
     * validation errors or OK status in the label set with
     * {@link #setStatusLabel(Label)}.
     * <p>
     * This handler cannot be set after the status label has been set with
     * {@link #setStatusLabel(Label)}, or {@link #setStatusLabel(Label)} cannot
     * be used after this handler has been set.
     *
     * @param statusHandler
     *            the status handler to set, not <code>null</code>
     * @throws NullPointerException
     *             for <code>null</code> status handler
     * @see #setStatusLabel(Label)
     * @see FieldBindingBuilder#withValidationStatusHandler(FieldBindingValidationStatusHandler)
     */
    public void setValidationStatusHandler(
            FieldBinderValidationStatusHandler statusHandler) {
        Objects.requireNonNull(statusHandler, "Cannot set a null "
                + FieldBinderValidationStatusHandler.class.getSimpleName());
        if (statusLabel != null) {
            throw new IllegalStateException("Cannot set "
                    + FieldBinderValidationStatusHandler.class.getSimpleName()
                    + " if a status label has already been set.");
        }
        this.statusHandler = statusHandler;
    }

    /**
     * Validates the values of all bound fields and returns the validation
     * status.

     * @return validation status for the binder
     */
    public FieldBinderValidationStatus validate() {
        return validate(true);
    }
    
    /**
     * Validates the value of the bound field and returns the validation
     * status. This method can fire validation status events. Firing the events
     * depends on the given {@code boolean}.
     *
     * @param fireEvent
     *            {@code true} to fire validation status events; {@code false}
     *            to not
     * @return validation status for the binder
     */
    protected FieldBinderValidationStatus validate(boolean fireEvent) {
        FieldBindingValidationStatus<?> bindingStatus = validateBinding();

        FieldBinderValidationStatus validationStatus = null;
        if (validators.isEmpty() || bindingStatus.isError()) {
            validationStatus = new FieldBinderValidationStatus(this,
                    bindingStatus);
        }
        if (fireEvent) {
            getValidationStatusHandler().statusChange(validationStatus);
            fireStatusChangeEvent(validationStatus.hasError());
        }
        return validationStatus;
    }
    
    /**
     * Runs all currently configured field level validators
     * <p>
     * <b>Note:</b> Calling this method will not trigger status change events,
     * unlike {@link #validate()} and will not modify the UI. To also update
     * error indicators on fields, use {@code validate().isOk()}.
     * <p>
     *
     * @see #validate()
     *
     * @return whether this binder is in a valid state
     */
    public boolean isValid() {
        return validate(false).isOk();
    }
    
    /**
     * Validates the binding and returns the result of the validation as a 
     * of validation status.
     * <p>
     *
     * @return a validation results for the binding
     */
    private FieldBindingValidationStatus<?> validateBinding() {
        return ((FieldBindingImpl<?, ?>) getBinding()).doValidation();

    }    
        
    /**
     * Sets the label to show the binder level validation errors not related to
     * any specific field.
     * <p>
     * Only the one validation error message is shown in this label at a time.
     * <p>
     * This is a convenience method for
     * {@link #setValidationStatusHandler(FieldBinderValidationStatusHandler)}, which
     * means that this method cannot be used after the handler has been set.
     * Also the handler cannot be set after this label has been set.
     *
     * @param statusLabel
     *            the status label to set
     * @see #setValidationStatusHandler(FieldBinderValidationStatusHandler)
     * @see FieldBindingBuilder#withStatusLabel(Label)
     */
    public void setStatusLabel(HasText statusLabel) {
        if (statusHandler != null) {
            throw new IllegalStateException("Cannot set status label if a "
                    + FieldBinderValidationStatusHandler.class.getSimpleName()
                    + " has already been set.");
        }
        this.statusLabel = statusLabel;
    }

    /**
     * Gets the status label or an empty optional if none has been set.
     *
     * @return the optional status label
     * @see #setStatusLabel(Label)
     */
    public Optional<HasText> getStatusLabel() {
        return Optional.ofNullable(statusLabel);
    }
   
    /**
     * Gets the status handler of this binder.
     *
     * @return the status handler used, never <code>null</code>
     * @see #setValidationStatusHandler(FieldBinderValidationStatusHandler)
     */
    public FieldBinderValidationStatusHandler getValidationStatusHandler() {
        return Optional.ofNullable(statusHandler)
                .orElse(this::handleBinderValidationStatus);
    }
    
    /**
     * The default binder level status handler.
     * <p>
     * Passes all field related results to the Binding status handlers. All
     * other status changes are displayed in the status label, if one has been
     * set with {@link #setStatusLabel(Label)}.
     *
     * @param binderStatus
     *            status of validation results from binding
     */
    protected void handleBinderValidationStatus(
            FieldBinderValidationStatus binderStatus) {
        // let field events go to binding status handlers
        binderStatus.notifyBindingValidationStatusHandler();

        // show first possible error or OK status in the label if set
        if (getStatusLabel().isPresent()) {
    		String statusMessage = "";
        	if (binderStatus.getFieldValidationError().isPresent() && binderStatus.getFieldValidationError().get().getMessage().isPresent()) {
        		statusMessage = binderStatus.getFieldValidationError().get().getMessage().get();
        	}
            getStatusLabel().get().setText(statusMessage);
        }
    }
    
    /**
     * Adds status change listener to the binder.
     * <p>
     * The {@link FieldBinder} status is changed whenever any of the following
     * happens:
     * <ul>
     * <li>{@link FieldBindingBuilder#bind(Object)} is called
     * <li>{@link FieldBinder#validate()} or {@link FieldBinding#validate()} is called
     * </ul>
     *
     * @see #forField(HasValue)
     * @see #validate()
     * @see FieldBinding#validate()
     *
     * @param listener
     *            status change listener to add, not null
     * @return a registration for the listener
     */
    public Registration addStatusChangeListener(StatusChangeListener listener) {
        return addListener(StatusChangeEvent.class, listener::statusChange);
    }

    /**
     * Adds a listener to the binder.
     *
     * @param eventType
     *            the type of the event
     * @param method
     *            the consumer method of the listener
     * @param <T>
     *            the event type
     * @return a registration for the listener
     */
    protected <T> Registration addListener(Class<T> eventType,
            SerializableConsumer<T> method) {
        List<SerializableConsumer<?>> list = listeners
                .computeIfAbsent(eventType, key -> new ArrayList<>());
        list.add(method);
        return () -> list.remove(method);
    }    
    
    /**
     * Adds field value change listener to the field in the binder.
     * <p>
     * Added listener is notified every time whenever any bound field value is
     * changed, i.e. the UI component value was changed, passed all the
     * conversions and validations. The
     * {@link ValueChangeListener} to the field in the {@link FieldBinder}.
     * <p>
     * The listener is added to the field regardless of whether the method is
     * invoked before or after field is bound.
     *
     * @see ValueChangeEvent
     * @see ValueChangeListener
     *
     * @param listener
     *            a field value change listener
     * @return a registration for the listener
     */
    public Registration addValueChangeListener(
            ValueChangeListener<? super ValueChangeEvent<?>> listener) {
        return addListener(ValueChangeEvent.class, listener::valueChanged);
    }
    
    /**
     * Creates a new binding with the given field.
     *
     * @param <FIELDVALUE>
     *            the value type of the field
     * @param <TARGET>
     *            the target data type
     * @param field
     *            the field to bind, not null
     * @param converter
     *            the converter for converting between FIELDVALUE and TARGET
     *            types, not null
     * @param handler
     *            the handler to notify of status changes, not null
     * @return the new incomplete binding
     */
    protected <FIELDVALUE, TARGET> FieldBindingBuilder<TARGET> createBinding(
            HasValue<?,FIELDVALUE> field, Converter<FIELDVALUE, TARGET> converter,
            FieldBindingValidationStatusHandler handler) {
        FieldBindingBuilder<TARGET> newBinding = doCreateBinding(field,
                converter, handler);
        return newBinding;
    }
    
    protected <FIELDVALUE, TARGET> FieldBindingBuilder<TARGET> doCreateBinding(
            HasValue<?,FIELDVALUE> field, Converter<FIELDVALUE, TARGET> converter,
            FieldBindingValidationStatusHandler handler) {
        return new FieldBindingBuilderImpl<>((FieldBinder<TARGET>) this, field, converter, handler);
    }

    /**
     * Informs the FieldBinder that a value in Binding was changed. This method will
     * trigger validating and writing 
     *
     * @param binding
     *            the binding whose value has been changed
     * @param event
     *            the value change event
     */
    protected void handleFieldValueChange(FieldBinding<?> binding) {
    	changedBinding = binding;
        doWriteIfValid(changedBinding);
    }

    /**
     * Writes changes from the bound field to the internal value if all
     * validators  pass.
     * <p>
     * Valid value can be fetched with {@link FieldBinding#getValue()}
     * <p>
     * 
     * @return {@code true} if there was no validation errors and the value was
     *         updated, {@code false} otherwise
     */
    private boolean writeIfValid() {
        return doWriteIfValid(binding).isOk();
    }

    /**
     * Returns the typed validated value of the binding, or otherwise null
     * 
     * This is short hand to {@link FieldBinding#getValue()}
     * 
     * @return validated value
     */
    public TARGET getValue() {
    	return (TARGET) binding.getValue();
    }
    
    /**
     * Writes the field values into binding
     *
     * @param binding
     *            the binding to update
     * @return a field validation status
     */
    @SuppressWarnings({ "unchecked" })
    private FieldBinderValidationStatus doWriteIfValid(FieldBinding<?> binding) {

        // First run field level validation
        FieldBindingValidationStatus<?> bindingResult = binding.validate(false);

        if (!bindingResult.isError()) {
            ((FieldBindingImpl<?, ?>) binding).writeFieldValue();
        }

        // Generate status object and fire events.
        FieldBinderValidationStatus status = new FieldBinderValidationStatus(this,
        		bindingResult);
        getValidationStatusHandler().statusChange(status);
        fireStatusChangeEvent(!status.isOk());
        return status;
    }
    
    /**
     * Creates a new binding for the given field. The returned builder may be
     * further configured before invoking
     * {@link FieldBindingBuilder#bind(Object)} which completes the binding.
     * Until {@code Binding.bind} is called, the binding has no effect.
     * <p>
     * <strong>Note:</strong> Not all {@link HasValue} implementations support
     * passing {@code null} as the value. For these the Binder will
     * automatically change {@code null} to a null representation provided by
     * {@link HasValue#getEmptyValue()}. This conversion is one-way only, if you
     * want to have a two-way mapping back to {@code null}, use
     * {@link FieldBindingBuilder#withNullRepresentation(Object)}.
     *
     * @param <TARGET>
     *            the value type of the field
     * @param field
     *            the field to be bound, not null
     * @return the new binding
     */
    public static <TARGET> FieldBindingBuilder<TARGET> of(HasValue<?,TARGET> field) {
        return new FieldBinder<>().forField(field);
        
    }

}
