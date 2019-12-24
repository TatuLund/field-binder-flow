package org.vaadin.fieldbinder;

import com.vaadin.flow.function.SerializableEventListener;

/**
 * Handler for {@link FieldBindingValidationStatus} changes.
 * <p>
 * FieldBindingBuilder#withValidationStatusHandler(FieldBindingValidationStatusHandler)
 * Register} an instance of this class to be able to override the default
 * handling, which is to show
 * {@link com.vaadin.ui.AbstractComponent#setComponentError(com.vaadin.server.ErrorMessage)
 * an error message} for failed field validations.
 *
 * FieldBindingBuilder#withValidationStatusHandler(FieldBindingValidationStatusHandler)
 * @see FieldBindingValidationStatus
 *
 */
@FunctionalInterface
public interface FieldBindingValidationStatusHandler
        extends SerializableEventListener {

    /**
     * Invoked when the validation status has changed in a binding.
     *
     * @param statusChange
     *            the changed status
     */
    public void statusChange(FieldBindingValidationStatus<?> statusChange);
}
