/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 
package io.ballerina.openapi.converter.utils;

import io.ballerina.openapi.converter.Constants;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Locale;

/**
 * Utilities used in Ballerina  to OpenAPI converter.
 */
public class ConverterCommonUtils {

    /**
     * This util function is for converting ballerina type to openapi type.
     * @param type this string type parameter according to ballerina type
     * @return  this return the string value of openAPI type
     */
    public static String convertBallerinaTypeToOpenAPIType(String type) {
        String convertedType;
        switch (type) {
            case Constants.INT:
                convertedType = Constants.OpenAPIType.INTEGER.toString();
                break;
            case Constants.STRING:
                convertedType = Constants.OpenAPIType.STRING.toString();
                break;
            case Constants.BOOLEAN:
                convertedType = Constants.OpenAPIType.BOOLEAN.toString();
                break;
            case Constants.ARRAY:
                convertedType = Constants.OpenAPIType.ARRAY.toString();
                break;
            case Constants.RECORD:
                convertedType = Constants.OpenAPIType.RECORD.toString();
                break;
            case Constants.DECIMAL:
                convertedType = Constants.OpenAPIType.NUMBER.toString();
                break;
            default:
                convertedType = "";
        }
        return convertedType;
    }

    /**
     * Retrieves a matching OpenApi {@link Schema} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return OpenApi {@link Schema} for type defined by {@code type}
     */
    public static Schema getOpenApiSchema(String type) {
        Schema schema;
        switch (type) {
            case Constants.STRING:
            case Constants.PLAIN:
                schema = new StringSchema();
                break;
            case Constants.BOOLEAN:
                schema = new BooleanSchema();
                break;
            case Constants.ARRAY:
                schema = new ArraySchema();
                break;
            case Constants.NUMBER:
            case Constants.INT:
            case Constants.INTEGER:
                schema = new IntegerSchema();
                break;
            case Constants.BYTE_ARRAY:
            case Constants.OCTET_STREAM:
                schema = new StringSchema();
                schema.setFormat("uuid");
                break;
            case Constants.DECIMAL:
                schema = new NumberSchema();
                schema.setFormat("double");
                break;
            case Constants.FLOAT:
                schema = new NumberSchema();
                schema.setFormat(Constants.FLOAT);
                break;
            case Constants.TYPE_REFERENCE:
            case Constants.TYPEREFERENCE:
            case Constants.XML:
            case Constants.JSON:
            default:
                schema = new Schema();
                break;
        }
        return schema;
    }

    /**
     * Generate operationId by removing special characters.
     *
     * @param identifier input function name, record name or operation Id
     * @return string with new generated name
     */
    public static String getValidName(String identifier) {
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(Constants.ESCAPE_PATTERN);
            StringBuilder validName = new StringBuilder();
            for (String part: split) {
                if (!part.isBlank()) {
                    if (split.length > 1) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                part.substring(1).toLowerCase(Locale.ENGLISH);
                    }
                    validName.append(part);
                }
            }
            identifier = validName.toString();
        }
        return identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1);
    }
}
