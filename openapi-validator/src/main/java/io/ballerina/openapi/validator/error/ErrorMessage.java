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

package io.ballerina.openapi.validator.error;

/**
 * This enum class stores the all error messages.
 *
 * @since 1.1.0
 */
public enum ErrorMessage {
    ERROR_001("OpenAPI contract does not exist in the given location: ''{0}''"),
    ERROR_002("unsupported contract file type. Provide either a .yaml or .json file."),
    ERROR_003("given OpenAPI contract file path is an empty string."),
    ERROR_004("OpenAPI annotation is only supported on HTTP services."),
    ERROR_005("implementation type does not match with OpenAPI contract type " +
            "(expected ''{0}'', found ''{1}'') for the field ''{2}'' of type ''{3}''."),
    ERROR_006("implementation type does not match with OpenAPI contract type (expected ''{0}'',found ''{1}'') for the" +
            " parameter ''{2}'' in HTTP method ''{3}'' that associated with the path ''{4}''."),
    ERROR_007("the ''{0}'' field in the ''{1}'' record is not documented in the OpenAPI contract ''{2}'' schema."),
    ERROR_008("could not find OpenAPI object schema field ''{0}'' in ''{1}'' the Ballerina record."),
    ERROR_009("undefined parameter ''{0}'' for the method ''{1}'' of the resource associated with the path " +
            "''{2}'' is not documented in the OpenAPI contract."),
    ERROR_010("missing OpenAPI contract parameter ''{0}'' in the counterpart Ballerina service resource (method: " +
            "''{1}'', path: ''{2}'')."),
    ERROR_011("unexpected error occurred while reading the contract : {0}"),
    ERROR_012("invalid contract file: {0}"),
    ERROR_013("validator annotation attributes 'tags' and 'excludeTags' are not allowed to be configured at the" +
            " same time."),
    ERROR_014("validator annotation attributes 'operations' and 'excludeOperations' are not allowed to be configured" +
            " at the same time."),
    ERROR_015("missing Ballerina service resource method ''{0}'' with path ''{1}'' which is documented in the OpenAPI" +
            " contract."),
    ERROR_016("undefined resource(s) ''{0}'' with path ''{1}'' in the Ballerina service."),
    ERROR_017("missing Ballerina service resource for the path ''{0}'' which is documented in the OpenAPI contract."),
    ERROR_018("undefined resource path ''{0}'' in the Ballerina service."),
    ERROR_019("implementation type does not match with OpenAPI contract type (expected ''{0}'',found ''{1}'') for the" +
            " header ''{2}'' in HTTP method ''{3}'' that associated with the path ''{4}''."),
    ERROR_020("undefined header ''{0}'' for the method ''{1}'' of the resource associated with the path ''{2}'' is " +
            "not documented in the OpenAPI contract."),
    ERROR_021("undefined request body for the method ''{0}'' of the resource associated with the path ''{1}''" +
            " in the OpenAPI contract."),
    ERROR_022("implementation payload type does not match with OpenAPI contract content type ''{0}'' (expected " +
            "''{1}'',found ''{2}'') for the HTTP method ''{3}'' that associated with the path ''{4}''."),
    ERROR_023("undefined status code(s) ''{0}'' for return type in the counterpart Ballerina service resource" +
            " (method: ''{1}'', path: ''{2}'')."),
    ERROR_024("undefined resource return mediaType(s) ''{0}'' for return status code ''{1}'' in the counterpart" +
            " Ballerina service resource (method: ''{2}'', path: ''{3}'')."),
    ERROR_025("missing OpenAPI contract header ''{0}'' in the counterpart Ballerina service resource" +
            " (method: ''{1}'', path: ''{2}'')."),
    ERROR_026("missing OpenAPI contract request body media type ''{0}'' in the counterpart Ballerina service resource" +
                      " (method: ''{1}'', path: ''{2}'')."),
    ERROR_027("missing OpenAPI contract request body implementation in the counterpart Ballerina service resource" +
                      " (method: ''{0}'', path: ''{1}'')."),
    ERROR_028("missing implementation for return code(s) ''{0}'' in the counterpart Ballerina service " +
            "resource (method: ''{1}'', path: ''{2}'')."),
    ERROR_029("missing implementation for return mediaType(s) ''{0}'' for return code ''{1}'' in the HTTP method " +
            "''{2}'' that associated with the path ''{3}''."),
    ERROR_030("OpenAPI service validator can not be proceed with all four attributes (tags, operations, " +
            "excludeTags, excludeOperations) at the same time, Please select a valid combination from them."),
    ERROR_031("undefined mediaType(s) ''{0}'' for the request body in the counterpart Ballerina service resource " +
            "(method: ''{1}'', path: ''{2}'').");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
