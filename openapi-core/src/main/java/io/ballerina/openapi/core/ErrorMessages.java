/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core;

/**
 * Container for error messages of the OpenAPI command.
 *
 * @since 1.3.0
 */
public class ErrorMessages {

    public static String invalidFilePath(String path) {
        return String.format("OpenAPI contract doesn't exist in the given location:%n%s", path);
    }

    public static String invalidFileType() {
        return "Invalid file type. Provide either a .yaml or .json file.";
    }

    public static String invalidPathParamType(String parameterName) {
        return String.format("Invalid path parameter data type for the parameter: '%s'", parameterName);
    }

    public static String unsupportedOpenAPIVersion() {
        return "Provided OpenAPI contract version is not supported in the tool. " +
                "Use OpenAPI specification version 2 or higher";
    }
}
