package org.vaadin.fieldbinder;

import com.vaadin.flow.function.SerializableEventListener;

/**
 * Handler for {@link FieldBinderValidationStatus} changes.
 * <p>
 * {@link FieldBinder#setValidationStatusHandler(FieldBinderValidationStatusHandler)
 * Register} an instance of this class to be able to customize validation status
 * handling.
 * <p>
 * The default handler will show
 * {@link com.vaadin.ui.AbstractComponent#setComponentError(com.vaadin.server.ErrorMessage)
 * an error message} for failed field validations. 
 *
 * @see FieldBinderValidationStatus
 * @see FieldBinder#validate()
 * @see FieldBindingValidationStatus
 *
 */
@FunctionalInterface
public interface FieldBinderValidationStatusHandler
        extends SerializableEventListener {

    /**
     * Invoked when the validation status has changed in binder.
     *
     * @param statusChange
     *            the changed status
     */
    void statusChange(FieldBinderValidationStatus statusChange);
}
