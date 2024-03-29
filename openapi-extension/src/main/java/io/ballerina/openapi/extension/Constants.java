/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.extension;

/**
 * {@code Constants} contains the common constants for the open-api extensions.
 */
public interface Constants {
    // package details related constants
    String PACKAGE_ORG = "ballerina";
    String PACKAGE_NAME = "openapi";

    // open-api module related constants
    String SERVICE_INFO_ANNOTATION_IDENTIFIER = "ServiceInfo";
    String CONTRACT = "contract";
    String EMBED = "embed";

    // http module related constants
    String HTTP_PACKAGE_NAME = "http";
    String SERVICE_CONFIG_ANNOTATION_IDENTIFIER = "ServiceConfig";
    String OPEN_API_DEFINITION_FIELD = "openApiDefinition";
}
