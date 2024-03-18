package io.ballerina.openapi.core.generators.common.exception;

public class TypeHanlderException extends Exception {

    public TypeHanlderException(String message, Throwable e) {
            super(message, e);
        }

    public TypeHanlderException(String message) {
            super(message);
        }
}
