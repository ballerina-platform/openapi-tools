/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@link DiagnosticMessages} enum class for containing the error message related to ballerina to openapi command.
 *
 * @since 1.0.0
 */
public enum DiagnosticMessages {
    OAS_CONVERTOR_100("OAS_CONVERTOR_100",
            "Generated OpenAPI definition does not contain details for the `default` " +
                    "resource method in the Ballerina service.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_101("OAS_CONVERTOR_101", "Unexpected value: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_102("OAS_CONVERTOR_102", "Invalid mediaType: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_103("OAS_CONVERTOR_103", "No related status code for: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_104("OAS_CONVERTOR_104",
            "Generated OpenAPI definition does not contain details for the " +
                    "resource function which has `http:Request` parameters in the Ballerina service.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_105("OAS_CONVERTOR_105",
            "Generated OpenAPI definition does not contain details for the " +
                    "resource function which has `http:Response` as return type in the Ballerina service.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_106("OAS_CONVERTOR_106", "Given Ballerina file contains compilation error(s).",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_107("OAS_CONVERTOR_107", "No Ballerina HTTP services found with name '%s' to" +
            " generate an OpenAPI specification. These services are available in ballerina file. %s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_108("OAS_CONVERTOR_108", "Failed to generate OpenAPI definition due to: %s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_109("OAS_CONVERTOR_109", "OpenAPI contract doesn't exist in the given location:%s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_110("OAS_CONVERTOR_110", "OpenAPI contract path can not be blank.",
                      DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_111("OAS_CONVERTOR_111", "Unsupported file type. Provide a valid contract " +
            "file in .yaml or .json format.",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_112("OAS_CONVERTOR_112", "Provided OpenAPI contract contains parsing error(s).",
                      DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_113("OAS_CONVERTOR_113",
            "Generated OpenAPI definition does not contain `http:Request` body information of the `GET` method," +
                    " as it's not supported by the OpenAPI specification.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_114("OAS_CONVERTOR_114", "Generated OpenAPI definition does not contain information " +
            "for Ballerina type '%s'. ", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_115("OAS_CONVERTOR_115", "Given Ballerina file does not contain any HTTP service.",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_116("OAS_CONVERTOR_116", "Failed to parse the Number value due to: %s ",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_117("OAS_CONVERTOR_117", "Generated OpenAPI definition does not contain `%s` request" +
            " body information, as it's not supported by the OpenAPI tool.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_118("OAS_CONVERTOR_118", "Generated OpenAPI definition does not contain variable " +
            "assignment '%s' in constraint validation.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_119("OAS_CONVERTOR_119", "Given REGEX pattern '%s' is not supported by the OpenAPI " +
            "tool, it may also not support interpolation within the REGEX pattern.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_120("OAS_CONVERTER_120", "Ballerina Date constraints might not be reflected in the " +
            "OpenAPI definition", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_121("OAS_CONVERTOR_121", "Generated OpenAPI definition does not contain the mapping " +
            "for the unsupported type: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_122("OAS_CONVERTOR_122", "Generated OpenAPI definition does not contain the mapping " +
            "for the unsupported union type: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_123("OAS_CONVERTOR_123", "Generated OpenAPI definition does not contain the mapping " +
            "for the unsupported tuple type: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_124("OAS_CONVERTOR_124", "Generated OpenAPI definition does not contain the default " +
            "value for the record field: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_125("OAS_CONVERTOR_125", "Generated OpenAPI specification excludes details for " +
            "operation with rest parameter in the resource path", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_126("OAS_CONVERTOR_126", "Generated OpenAPI definition does not contain the response" +
            "/request parameter information from the interceptor pipeline. Cause: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_127("OAS_CONVERTOR_127", "Generated OpenAPI definition does not contain the request" +
            " parameter information from the interceptor pipeline. Cause: %s", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_128("OAS_CONVERTOR_128", "Given example path `%s` is not exist, therefore " +
            "generated yaml does not contain example for `%s` ", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_129("OAS_CONVERTOR_129", "Given example path `%s` should be JSON file for" +
            " example `%s`", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_130("OAS_CONVERTOR_130", "Generated OpenAPI definition does not contain the example" +
            " details since the JSON value provided for the example has parser errors", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_131("OAS_CONVERTOR_131", "`%s` example can not have a blank path",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_132("OAS_CONVERTOR_132", "Generated OpenAPI definition does not contain the example " +
            "for type: `%s`, since the example value has parser errors", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_133("OAS_CONVERTOR_133", "Generated OpenAPI definition does not contain the example " +
            "for record field: `%s.%s`, since the example value has parser errors", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_134("OAS_CONVERTOR_134", "Generated OpenAPI definition does not contain the `%s` for" +
            " `%s` parameter: `%s`, since the example value(s) has parser errors", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_135("OAS_CONVERTOR_135", "Example skipped for request parameter: `%s`, since the request payload " +
            "type implies more than one content-type", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_136("OAS_CONVERTOR_136", "Generated OpenAPI definition for the service contract type: " +
            "'%s' has errors", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_137("OAS_CONVERTOR_137", "Failed to obtain the OpenAPI definition from the service contract" +
            " type: '%s'", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_138("OAS_CONVERTOR_138", "Failed to find the package to obtain the OpenAPI definition from the " +
            "service contract type: '%s'", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_139("OAS_CONVERTOR_139", "Failed to find the OpenAPI definition resource for the " +
            "service contract type: '%s'", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_140("OAS_CONVERTOR_140", "Failed to resolve recursive references for `expand: true` option",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_141("OAS_CONVERTOR_141", "Generated OpenAPI definition does not have the server information",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_142("OAS_CONVERTOR_142", "The server port value cannot be obtained since the value is provided" +
            " via a variable defined outside the current module", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_143("OAS_CONVERTOR_143", "Unsupported expression found for the server port value",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_144("OAS_CONVERTOR_144", "The variable value provided for the server port, cannot be found" +
            " in the current module", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_145("OAS_CONVERTOR_145", "The server port is defined as a configurable. Hence, using the" +
            " default value to generate the server information", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_146("OAS_CONVERTOR_146", "The configurable value provided for the port should have a default value" +
            " to generate the server details", DiagnosticSeverity.ERROR);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }
}
