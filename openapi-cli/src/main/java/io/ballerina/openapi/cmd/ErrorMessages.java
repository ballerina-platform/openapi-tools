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

package io.ballerina.openapi.cmd;

/**
 * This class contains the messages constants required for OpenApi tool.
 *
 * @since 1.3.0
 */
public class ErrorMessages {

    public static final String CLIENT_GENERATION_FAILED = "Error occurred when generating client for OpenAPI contract";

    public static final String MISSING_CONTRACT_PATH = "An OpenAPI definition path is required to generate the " +
            "service. \ne.g: bal openapi --input <OpenAPIContract> or <Ballerina file>";

    private ErrorMessages() {
        throw new AssertionError();
    }
}
