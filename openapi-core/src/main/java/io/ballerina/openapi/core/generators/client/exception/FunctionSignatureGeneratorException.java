package io.ballerina.openapi.core.generators.client.exception;

public class FunctionSignatureGeneratorException extends Exception {
    public FunctionSignatureGeneratorException(String message, Throwable e) {
        super(message, e);
    }

    public FunctionSignatureGeneratorException(String message) {
        super(message);
    }
}
