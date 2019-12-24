package org.vaadin.fieldbinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Internal implementation of a {@code Result} that collects all possible
 * ValidationResults into one list. This class intercepts the normal chaining of
 * Converters and Validators, catching and collecting results.
 *
 * @param <R>
 *            the result data type
 */
class FieldValidationResultWrap<R> implements Result<R> {

    private final List<ValidationResult> resultList;
    private final Result<R> wrappedResult;

    FieldValidationResultWrap(Result<R> result, List<ValidationResult> resultList) {
        this.resultList = resultList;
        this.wrappedResult = result;
    }

    FieldValidationResultWrap(R value, ValidationResult result) {
        if (result.isError()) {
            wrappedResult = new FieldSimpleResult<>(null, result.getErrorMessage());
        } else {
            wrappedResult = new FieldSimpleResult<>(value, null);
        }
        this.resultList = new ArrayList<>();
        this.resultList.add(result);
    }

    List<ValidationResult> getValidationResults() {
        return Collections.unmodifiableList(resultList);
    }

    Result<R> getWrappedResult() {
        return wrappedResult;
    }

    @Override
    public <S> Result<S> flatMap(SerializableFunction<R, Result<S>> mapper) {
        Result<S> result = wrappedResult.flatMap(mapper);
        if (!(result instanceof FieldValidationResultWrap)) {
            return new FieldValidationResultWrap<S>(result, resultList);
        }

        List<ValidationResult> currentResults = new ArrayList<>(resultList);
        FieldValidationResultWrap<S> resultWrap = (FieldValidationResultWrap<S>) result;
        currentResults.addAll(resultWrap.getValidationResults());

        return new FieldValidationResultWrap<>(resultWrap.getWrappedResult(),
                currentResults);
    }

    @Override
    public void handle(SerializableConsumer<R> ifOk,
            SerializableConsumer<String> ifError) {
        wrappedResult.handle(ifOk, ifError);
    }

    @Override
    public boolean isError() {
        return wrappedResult.isError();
    }

    @Override
    public Optional<String> getMessage() {
        return wrappedResult.getMessage();
    }

    @Override
    public <X extends Throwable> R getOrThrow(
            SerializableFunction<String, ? extends X> exceptionProvider)
            throws X {
        return wrappedResult.getOrThrow(exceptionProvider);
    }

}
