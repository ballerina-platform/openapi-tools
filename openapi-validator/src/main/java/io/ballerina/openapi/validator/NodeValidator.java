/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

/**
 * This interface class created for the validated each sections in the resource function. Sections can be
 * parameters, headers, requestBody, returnType. Then we can make sure those sections have been validated to both
 * side OAS to Ballerina implementation and Ballerina to OAS implementation.
 *
 * @since 1.1.0
 */
public abstract class NodeValidator implements Validator {
    ValidatorContext validatorContext;

    public NodeValidator(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    @Override
    public void validate() {
        validateBallerinaToOpenAPI();
        validateOpenAPIToBallerina();
    }
    abstract void validateBallerinaToOpenAPI();
    abstract void validateOpenAPIToBallerina();
}
