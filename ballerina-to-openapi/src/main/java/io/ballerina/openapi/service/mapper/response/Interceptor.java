package io.ballerina.openapi.service.mapper.response;

import java.util.List;

public class Interceptor {
    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    private InterceptorType type;
    private String relativeResourcePath;
    private List<Object> returnTypes;

    public InterceptorType getType() {
        return type;
    }

    public void setType(InterceptorType type) {
        this.type = type;
    }

    public String getRelativeResourcePath() {
        return relativeResourcePath;
    }

    public void setRelativeResourcePath(String relativeResourcePath) {
        this.relativeResourcePath = relativeResourcePath;
    }

    public List<Object> getReturnTypes() {
        return returnTypes;
    }

    public void setReturnTypes(List<Object> returnTypes) {
        this.returnTypes = returnTypes;
    }
}
