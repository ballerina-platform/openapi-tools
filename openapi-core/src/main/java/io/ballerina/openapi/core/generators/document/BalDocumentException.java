package io.ballerina.openapi.core.generators.document;

public class BalDocumentException extends Exception {

    public BalDocumentException(String message, Throwable e) {
        super(message, e);
    }

    public BalDocumentException(String message) {
        super(message);
    }
}
