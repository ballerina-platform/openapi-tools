/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.service.mapper.type.extension;

/**
 * This {@link BallerinaPackage} record represents the Ballerina package details.
 * @param orgName organization name
 * @param pkgName package name
 * @param moduleName module name
 * @param version version
 * @param modulePrefix module prefix
 * @param name type name
 * @since 2.1.0
 */
public record BallerinaPackage(String orgName, String pkgName, String moduleName, String version,
                               String modulePrefix, String name) {
}
