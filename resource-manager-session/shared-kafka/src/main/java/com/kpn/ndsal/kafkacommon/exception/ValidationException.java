package com.kpn.ndsal.kafkacommon.exception;

import java.util.Collections;
import java.util.Set;

import com.networknt.schema.ValidationMessage;

public class ValidationException extends RuntimeException {
    private final Set<ValidationMessage> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.emptySet();
    }

    public ValidationException(Set<ValidationMessage> errors) {
        super("Validation errors");
        this.errors = errors != null ? errors : Collections.emptySet();
    }

    public ValidationException(String message, Set<ValidationMessage> errors) {
        super(message);
        this.errors = errors;
    }

    public Set<ValidationMessage> getErrors() {
        return errors;
    }
}
