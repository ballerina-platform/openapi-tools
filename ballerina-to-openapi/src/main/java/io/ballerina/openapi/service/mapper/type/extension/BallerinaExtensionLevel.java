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
 * This {@link BallerinaExtensionLevel} represents the Ballerina extension levels.
 */
public enum BallerinaExtensionLevel {
    DISABLED("0"),
    EXTERNAL_PACKAGE_TYPES("1"),
    SAME_PACKAGE_DIFFERENT_MODULE_TYPES("2"),
    ALL_REFERENCED_TYPES("3");

    private final String value;

    BallerinaExtensionLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BallerinaExtensionLevel fromValue(String value) {
        for (BallerinaExtensionLevel level : BallerinaExtensionLevel.values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
