package io.ballerina.openapi.core.generators.client.exception;

public class ClientException  extends Exception {

    public ClientException(String message, Throwable e) {
        super(message, e);
    }

    public ClientException(String message) {
        super(message);
    }
}
