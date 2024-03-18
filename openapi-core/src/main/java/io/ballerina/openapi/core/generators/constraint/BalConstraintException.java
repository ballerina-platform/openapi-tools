package io.ballerina.openapi.core.generators.constraint;

public class BalConstraintException extends Exception {

    public BalConstraintException(String message, Throwable e) {
        super(message, e);
    }

    public BalConstraintException(String message) {
        super(message);
    }
}
