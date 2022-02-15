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
 * Container for Constants used in validator plugin.
 */
public class Constants {
    public static final String PACKAGE = "openapi";
    public static final String ANNOTATION_NAME = "ServiceInfo";
    public static final String ATTRIBUTE_CONTRACT = "contract";
    public static final String HTTP = "http";
    public static final String RESOURCE_CONFIG = "ResourceConfig";
    public static final String PATH = "path";
    public static final String METHODS = "methods";
    public static final String GET = "get";
    public static final String POST = "post";
    public static final String PUT = "put";
    public static final String DELETE = "delete";
    public static final String HEAD = "head";
    public static final String PATCH = "patch";
    public static final String OPTIONS = "options";
    public static final String TRACE = "trace";
    public static final String ATTRIBUTE_TAGS = "tags";
    public static final String ATTRIBUTE_OPERATIONS = "operations";
    public static final String BODY = "body";
    public static final String ATTRIBUTE_FAIL_ON_ERRORS = "failOnErrors";
    public static final String ATTRIBUTE_EXCLUDE_TAGS = "excludeTags";
    public static final String ATTRIBUTE_EXCLUDE_OPERATIONS = "excludeOperations";
    public static final String METHOD = "method";
    public static final String ARRAY = "array";
    public static final String ARRAY_BRACKETS = "[]";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String INTEGER = "integer";
    public static final String OBJECT = "object";
    public static final String DECIMAL = "decimal";
    public static final String RECORD = "record";
    public static final String NUMBER = "number";
    public static final String INT = "int";
    public static final String USER_DIR = "user.dir";
    public static final String FALSE = "false";
    public static final String HTTP_PAYLOAD = "http:Payload";

    /**
     * Enum Type for handle the validation error types.
     */
    public enum Type {
        STRING, INT, RECORD, OBJECT, ARRAY, BOOLEAN, DECIMAL, ANYDATA, INTEGER, NUMBER
    }
}
