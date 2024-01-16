/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package io.ballerina.openapi.service.mapper.model;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.Objects;

/**
 * This class will hold operation details specific to HTTP operation.
 */
public class OperationBuilder {

    private Operation operation;
    private String path;
    private String httpOperation;
    private Components components;

    public OperationBuilder() {
        this.operation = new io.swagger.v3.oas.models.Operation();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (!path.startsWith("/")) {
            this.path = "/" + path;
        } else {
            this.path = path;
        }
    }

    public String getHttpOperation() {
        return httpOperation;
    }

    public void setHttpOperation(String httpOperation) {
        this.httpOperation = httpOperation;
    }

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

    public boolean hasDataBinding() {
        return Objects.nonNull(this.operation.getRequestBody()) || Objects.nonNull(this.operation.getParameters());
    }

    public void setParameter(Parameter parameter) {
        operation.addParametersItem(parameter);
    }

    public void setRequestBody(RequestBody requestBody) {
        if (Objects.isNull(operation.getRequestBody())) {
            operation.setRequestBody(requestBody);
        }
    }

    public void overrideRequestBody(RequestBody requestBody) {
        operation.setRequestBody(requestBody);
    }

    public void setApiResponses(ApiResponses apiResponses) {
        operation.setResponses(apiResponses);
    }

    public void setOperationId(String operationId) {
        operation.setOperationId(operationId);
    }

    public void setSummary(String summary) {
        operation.setSummary(summary);
    }
}
