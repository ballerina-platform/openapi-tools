/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator;

/**
 * Container for error messages of the OpenAPI validator plugin.
 */
class ErrorMessages {

    static String[] invalidFilePath(String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0001;
        error[1] = String.format("OpenAPI contract doesn't exist in the given location:%n%s", path);
        return error;
    }

    static String[] invalidFile() {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0002;
        error[1] = "Invalid file type. Provide either a .yaml or .json file.";
        return  error;
    }

    static String[] parserException(String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0003;
        error[1] =  String.format("Couldn't read the OpenAPI contract from the given file: %s", path);
        return  error;
    }

    static String[] undocumentedResourcePath(String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0004;
        error[1] =  String.format("Ballerina service contains a Resource that is not"
                + " documented in the OpenAPI contract."
                + " Error Resource path '%s'", path);
        return  error;
    }

    static String[] undocumentedResourceParameter(String paramName, String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0005;
        error[1] =  String.format("'%s' parameter for the method ''%s'' " +
                "of the resource associated with the path ''%s'' " +
                "is not documented in the OpenAPI contract.", paramName, method, path);
        return  error;
    }

    static String[] undocumentedResourceMethods(String methods, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0006;
        error[1] =  String.format("OpenAPI contract doesn't contain the" +
                " documentation for http method(s) '%s' for the path '%s'.", methods, path);
        return  error;
    }

    static String[] unimplementedOpenAPIPath(String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0007;
        error[1] =  String.format("Could not find a Ballerina service resource for the path ''%s'' " +
                "which is documented in the OpenAPI contract.", path);
        return  error;
    }

    static String[] unimplementedOpenAPIOperationsForPath(String methods, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0008;
        error[1] =  String.format("Could not find Ballerina service resource(s) for http method(s) ''%s'' " +
                "for the path '%s' which is documented in the OpenAPI contract.", methods, path);
        return  error;
    }

    static String[] unimplementedParameterForOperation(String paramName, String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0009;
        error[1] =  String.format("Missing OpenAPI contract parameter ''%s'' in the counterpart Ballerina service " +
                "resource (method: ''%s'', path: ''%s'')", paramName, method, path);
        return  error;
    }

    static String[] undocumentedFieldInRecordParam(String fieldName, String paramName, String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0010;
        error[1] =  String.format("The ''%s'' field in the ''%s'' type record of the parameter " +
                        "is not documented in the OpenAPI contract for the method ''%s'' of the path ''%s''.",
                fieldName, paramName, method, path);
        return  error;
    }

    static String[] unimplementedFieldInOperation(String fieldName, String paramName, String operation, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0011;
        error[1] =  String.format("Could not find the '%s' field in the record type of the parameter '%s' " +
                        "for the method '%s' of the path '%s' which is documented in the OpenAPI contract.",
                fieldName, paramName, operation, path);
        return  error;
    }

    static String[] tagFilterEnable() {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0012;
        error[1] =  String.format("Both Tags and excludeTags fields include the same tag(s). Make sure to use one" +
                " field of tag filtering when using the OpenAPI annotation. ");
        return  error;
    }

    static String[] operationFilterEnable() {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0013;
        error[1] =  String.format("Both Operations and excludeOperations fields include" +
                " the same operation(s). Make sure to use one field of operation filtering" +
                " when using the openapi annotation.");
        return  error;
    }

    static String[] typeMismatching(String fieldName, String openapiType, String ballerinType,
                                  String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0014;
        error[1] =  String.format("Type mismatch with parameter ''%s'' for the method" +
                        " ''%s'' of the path '%s'.In OpenAPI contract its type is ''%s'' and resources type " +
                        "is ''%s''. ",
                fieldName, method,  path, openapiType, ballerinType);
        return  error;
    }

    static String[] typeMismatchingRecord(String fieldName, String paramName, String openapiType, String ballerinType,
                                        String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0015;
        error[1] =  String.format("Type mismatching ''%s'' field in the record type of the parameter " +
                        "''%s'' for the method ''%s'' of the path ''%s''.In OpenAPI contract its type is ''%s'' and " +
                        "resources type is ''%s''. ",
                fieldName, paramName, method,  path, openapiType , ballerinType);
        return  error;
    }

    static String[] typeMismatchOneOfObject(String fieldName, String paramName, String openapiType, String ballerinType,
                                          String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0016;
        error[1] =  String.format("Type mismatch with '%s' field in the object type of the parameter '%s'" +
                        " for the method '%s' of the path '%s'.OpenAPI object schema expected '%s' type and " +
                        "resources has '%s' type for field.",
                fieldName, paramName, method,  path, openapiType, ballerinType);
        return  error;
    }

    static String[] typeMismatchOneOfRecord(String fieldName, String paramName, String openapiType, String ballerinType,
                                          String method, String path) {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0017;
        error[1] =  String.format("Type mismatch with '%s' field in the record type of the parameter '%s' " +
                        "for the method '%s' of the path '%s'.OpenAPI object schema expected '%s' " +
                        "type and resources has '%s' type for field.",
                fieldName, paramName, method,  path, openapiType, ballerinType);
        return  error;
    }

    static String[] contactFileMissinginPath() {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0018;
        error[1] = "Contract file is not existed in the given path should be applied.";
        return error;
    }

    static String[] contractPathEmpty() {
        String[] error = new String[2];
        error[0] = ValidatorErrorCode.BAL_OPENAPI_VALIDATOR_0020;
        error[1] = "Given OpenAPI contract file path is an empty string.";
        return error;
    }

    static String couldNotFindLocation(String fieldName) {
        return String.format("Could not find the location for %s .", fieldName);
    }
}
