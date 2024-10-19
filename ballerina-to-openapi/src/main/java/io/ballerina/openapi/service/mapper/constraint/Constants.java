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

package io.ballerina.openapi.service.mapper.constraint;

/**
 * The {@link Constants} class contains the constants used in the constraint mapper.
 *
 * @since 1.9.0
 */
public final class Constants {

    private Constants() {
    }

    public static final String ARRAY = "array";
    public static final String STRING = "string";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String OBJECT = "object";

    public static final String VALUE = "value";

    public static final String MIN_VALUE = "minValue";
    public static final String MAX_VALUE = "maxValue";
    public static final String MIN_VALUE_EXCLUSIVE = "minValueExclusive";
    public static final String MAX_VALUE_EXCLUSIVE = "maxValueExclusive";
    public static final String PATTERN = "pattern";
    public static final String LENGTH = "length";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
}
