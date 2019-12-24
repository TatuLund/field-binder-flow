package org.vaadin.fieldbinder;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Binder validation status change. Represents the outcome of binder level
 * validation. Has information about the validation results for the
 * binding.
 * <p>
 * Use {@link FieldBinder#setValidationStatusHandler(FieldBinderValidationStatusHandler)}
 * to handle validation status changes.
 *
 * @see FieldBinderValidationStatusHandler
 * @see FieldBinder#setValidationStatusHandler(FieldBinderValidationStatusHandler)
 * @see FieldBinder#validate()
 * @see FieldBindingValidationStatus
 *
 */
public class FieldBinderValidationStatus implements Serializable {

    private final FieldBinder binder;
    private final FieldBindingValidationStatus<?> bindingStatus;

    /**
     * Convenience method for creating a unresolved validation status for the
     * given binder.
     * <p>
     * In practice this status means that the values might not be valid, but
     * validation errors should be hidden.
     *
     * @param source
     *            the source binder
     * @return a unresolved validation status
     */
    public static FieldBinderValidationStatus createUnresolvedStatus(
            FieldBinder source) {
        return new FieldBinderValidationStatus(source, FieldBindingValidationStatus.createUnresolvedStatus(source.getBinding()));
    }

    /**
     * Creates a new binder validation status for the given binder and
     * validation results.
     *
     * @param source
     *            the source binder
     * @param bindingStatus
     *            the validation results for the fields
     */
    public FieldBinderValidationStatus(FieldBinder source,
            FieldBindingValidationStatus<?> bindingStatus) {
        Objects.requireNonNull(bindingStatus,
                "binding status cannot be null");
        this.binder = source;
        this.bindingStatus = bindingStatus;
    }

    /**
     * Gets whether validation for the binder passed or not.
     *
     * @return {@code true} if validation has passed, {@code false} if not
     */
    public boolean isOk() {
        return !hasError();
    }

    /**
     * Gets whether the validation for the binder failed or not.
     *
     * @return {@code true} if validation failed, {@code false} if validation
     *         passed
     */
    public boolean hasError() {
        return bindingStatus.isError();
    }

    /**
     * Gets the source binder of the status.
     *
     * @return the source binder
     */
    public FieldBinder getBinder() {
        return binder;
    }

    /**
     * Gets validation errors.
     *
     * @return a list of all validation errors
     */
    public ValidationResult getValidationError() {
        ValidationResult error = (ValidationResult) getFieldValidationStatus().getResult().get();
        return error;
    }

    /**
     * Gets the field level validation statuses.
     * <p>
     * The field level validators have been added with
     * FieldBindingBuilder#withValidator(Validator).
     *
     * @return the field validation statuses
     */
    public FieldBindingValidationStatus<?> getFieldValidationStatus() {
        return bindingStatus;
    }

    /**
     * Gets the failed field level validation statuses.
     * <p>
     * The field level validators have been added with
     * FieldBindingBuilder#withValidator(Validator).
     *
     * @return a list of failed field level validation statuses
     */
    public Optional<FieldBindingValidationStatus<?>> getFieldValidationError() {
        return Optional.of(bindingStatus.isError() ? bindingStatus : null);
    }

    /**
     * Notifies all validation status handlers in bindings.
     *
     * @see #notifyBindingValidationStatusHandler(SerializablePredicate)
     */
    public void notifyBindingValidationStatusHandler() {
        notifyBindingValidationStatusHandler(t -> true);
    }

    /**
     * Notifies validation status handlers for bindings that pass given filter.
     * The filter should return {@code true} for each
     * {@link FieldBindingValidationStatus} that should be delegated to the status
     * handler in the binding.
     *
     * @see #notifyBindingValidationStatusHandler()
     *
     * @param filter
     *            the filter to select bindings to run status handling for
     */
    public void notifyBindingValidationStatusHandler(
            SerializablePredicate<FieldBindingValidationStatus<?>> filter) {
        bindingStatus.getBinding().getValidationStatusHandler().statusChange(bindingStatus);
    }
}
