/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.core.generators.common;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class is to contain the sanitized utils for naming.
 *
 * @since 2.1.0
 */
public class OASModifier {
    OpenAPI openapi;
    List<Diagnostic> diagnostics = new ArrayList<>();

    public OASModifier(OpenAPI openapi) {
        this.openapi = openapi;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    //modifyWithBallerinaConventions 
    public OpenAPI modifyWithBallerinaConventions() {
        Paths paths = openapi.getPaths();
        Components components = openapi.getComponents();
        List<String> modifiedSchemaNames = new ArrayList<>();
        Map<String, Schema> modifiedSchemas = new HashMap<>();
        Map<String, RequestBody> modifiedRequestBodies = new HashMap<>();
        Map<String, ApiResponse> modifiedResponses = new HashMap<>();
        Map<String, Parameter> modifiedReusableParameters = new HashMap<>();

        if (components != null) {
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
                    String schemaName = schema.getKey();
                    String modifiedName = getValidNameForType(schemaName);

                    if (!schemaName.equals(modifiedName) && (schema.getKey().contains(modifiedName) ||
                            modifiedSchemaNames.contains(modifiedName))) {
                        // todo: check the duplication, till providing the name this will give same name as it is.
                        continue;
                    }
                    modifiedSchemaNames.add(modifiedName);

                    // replace only for reference
                    // 1.Update the reference at the component sections
                    for (Map.Entry<String, Schema> componentSchema : schemas.entrySet()) {
                        Schema value = componentSchema.getValue();
                        updateSchemaWithReference(schemaName, modifiedName, value);
                        modifiedSchemas.put(modifiedName, value);
                    }
                    
                    if (components.getSchemas() != null) {
                        components.setSchemas(modifiedSchemas);
                    }
                    
                    if (paths == null || paths.isEmpty()) {
                        openapi.setComponents(components);
                        return openapi;
                    }

                    // 2. Update OAS reusable requestBody with reference details
                    if (components.getRequestBodies() != null) {
                        for (Map.Entry<String, RequestBody> requestBody : components.getRequestBodies().entrySet()) {
                            RequestBody requestBodyValue = updateRequestBodySchemaType(schemaName, modifiedName,
                                    requestBody.getValue());
                            modifiedRequestBodies.put(requestBody.getKey(), requestBodyValue);
                        }
                    }
                    // 3. Update OAS reusable response with reference details
                    if (components.getResponses() != null) {
                        Map<String, ApiResponse> responsesEntry = components.getResponses();
                        for (Map.Entry<String, ApiResponse> response : responsesEntry.entrySet()) {
                            ApiResponse modifiedResponse = updateSchemaTypeForResponses(schemaName, modifiedName,
                                    response.getValue());
                            modifiedResponses.put(response.getKey(), modifiedResponse);
                        }
                    }
                    // 4. Update OAS reusable parameters with reference details
                    if (components.getParameters() != null) {
                        Map<String, Parameter> parameters = components.getParameters();
                        for (Map.Entry<String, Parameter> param : parameters.entrySet()) {
                            Parameter parameter = param.getValue();
                            if (parameter.getSchema() != null) {
                                Schema<?> paramSchema = parameter.getSchema();
                                String ref = paramSchema.get$ref();
                                if (ref != null) {
                                    updateRef(schemaName, modifiedName, paramSchema);
                                }
                                parameter.setSchema(paramSchema);
                            }
                            modifiedReusableParameters.put(param.getKey(), parameter);
                        }
                    }
                    // 5. Update OAS reusable headers with reference details
                    if (components.getHeaders() != null) {
                        Map<String, Header> headers = components.getHeaders();
                        for (Map.Entry<String, Header> header : headers.entrySet()) {
                            Header headerValue = header.getValue();
                            Schema<?> type = headerValue.getSchema();
                            String ref = type.get$ref();
                            if (ref != null) {
                                updateRef(schemaName, modifiedName, type);
                            }
                            headerValue.setSchema(type);
                            header.setValue(headerValue);
                        }
                    }
                    // 6. Update Operation data type with reference details
                    Set<Map.Entry<String, PathItem>> operations = paths.entrySet();
                    updateOASOperations(schemaName, modifiedName, operations);
                }
            }

            if (components.getRequestBodies() != null) {
                components.setRequestBodies(modifiedRequestBodies);
            }
            if (components.getResponses() != null) {
                components.setResponses(modifiedResponses);
            }
            if (components.getParameters() != null) {
                components.setParameters(modifiedReusableParameters);
            }
            openapi.setComponents(components);
        }

        // This is for path parameter name modifications
        Paths modifiedPaths = new Paths();
        for (Map.Entry<String, PathItem> path : paths.entrySet()) {
            PathDetails result = updateParameterName(modifiedSchemas, path);
            modifiedPaths.put(result.pathValue(), result.pathItem());
        }
        openapi.setPaths(modifiedPaths);
        return openapi;
    }

    private static PathDetails updateParameterName(Map<String, Schema> modifiedSchemas, Map.Entry<String,
            PathItem> path) {
        PathItem pathItem = path.getValue();
        String pathValue = path.getKey();
        if (pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setGet(operation);
        }
        if (pathItem.getPost() != null) {
            Operation operation = pathItem.getPost();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setPost(operation);
        }
        if (pathItem.getPut() != null) {
            Operation operation = pathItem.getPut();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setPut(operation);
        }
        if (pathItem.getDelete() != null) {
            Operation operation = pathItem.getDelete();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setDelete(operation);
        }
        if (pathItem.getPatch() != null) {
            Operation operation = pathItem.getPatch();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setPatch(operation);
        }
        if (pathItem.getHead() != null) {
            Operation operation = pathItem.getHead();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setHead(operation);
        }
        if (pathItem.getOptions() != null) {
            Operation operation = pathItem.getOptions();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setOptions(operation);
        }
        if (pathItem.getTrace() != null) {
            Operation operation = pathItem.getTrace();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(modifiedSchemas, pathValue, operation);
            pathItem.setTrace(operation);
        }
        return new PathDetails(pathItem, pathValue);
    }

    private static String updateParameterNames(Map<String, Schema> modifiedSchemas, String pathValue,
                                               Operation operation) {
        List<String> parameterNames = collectParameterNames(operation.getParameters());
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            List<Parameter> modifiedParameters = new ArrayList<>();
            pathValue = updateParameters(modifiedSchemas, pathValue, parameterNames, parameters,
                    modifiedParameters);
            operation.setParameters(modifiedParameters);
        }
        return pathValue;
    }

    private static String updateParameters(Map<String, Schema> modifiedSchemas, String pathValue,
                                           List<String> parameterNames, List<Parameter> parameters,
                                           List<Parameter> modifiedParameters) {
        for (Parameter parameter : parameters) {
            if (!parameter.getIn().equals("path")) {
                modifiedParameters.add(parameter);
                continue;
            }
            String oasPathParamName = parameter.getName();
            Optional<String> modifiedPathParamResult = getValidNameForParameter(oasPathParamName);
            if (modifiedPathParamResult.isEmpty()) {
                //todo diagnostic, severity error
                modifiedParameters.add(parameter);
                continue;
            }
            String modifiedPathParam = modifiedPathParamResult.get();
            if (!modifiedPathParam.equals(oasPathParamName) && parameterNames.contains(modifiedPathParam)) {
                // todo: handle by adding abc_1 or abc_2
                modifiedParameters.add(parameter);
                continue;
            }
            // check given parameter has name which similar to component schema name
            if (modifiedSchemas.containsKey(modifiedPathParam)) {
                modifiedPathParam = "param" + getValidNameForParameter(modifiedPathParam).get();
            }
            parameterNames.add(modifiedPathParam);
            parameter.setName(modifiedPathParam);
            modifiedParameters.add(parameter);
            pathValue = replaceContentInBraces(pathValue, modifiedPathParam);
        }
        return pathValue;
    }

    /**
     * Record for storing return data.
     *
     * @param pathItem
     * @param pathValue
     */
    private record PathDetails(PathItem pathItem, String pathValue) {
    }

    /**
     * This util is to update the all OAS operations in given path with modified schema name.
     *
     * @param schemaName   - original schema name
     * @param modifiedName - modified schema name
     * @param operations   - OAS operation.
     */
    private static void updateOASOperations(String schemaName, String modifiedName,
                                            Set<Map.Entry<String, PathItem>> operations) {
        for (Map.Entry<String, PathItem> path : operations) {
            PathItem pathItem = path.getValue();
            if (pathItem.getGet() != null) {
                Operation operation = pathItem.getGet();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setGet(operation);
            }
            if (pathItem.getPost() != null) {
                Operation operation = pathItem.getPost();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setPost(operation);
            }
            if (pathItem.getPut() != null) {
                Operation operation = pathItem.getPut();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setPut(operation);
            }
            if (pathItem.getDelete() != null) {
                Operation operation = pathItem.getDelete();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setDelete(operation);
            }
            if (pathItem.getPatch() != null) {
                Operation operation = pathItem.getPatch();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setPatch(operation);
            }
            if (pathItem.getHead() != null) {
                Operation operation = pathItem.getHead();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setHead(operation);
            }
            if (pathItem.getOptions() != null) {
                Operation operation = pathItem.getOptions();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setOptions(operation);
            }
            if (pathItem.getTrace() != null) {
                Operation operation = pathItem.getTrace();
                updateSchemaReferenceDetailsWithOperation(schemaName, modifiedName, operation);
                pathItem.setTrace(operation);
            }
        }
    }

    private static void updateObjectPropertyRef(Map<String, Schema> properties, String schemaName,
                                                String modifiedName) {
        if (properties != null) {
            for (Map.Entry<String, Schema> property : properties.entrySet()) {
                Schema fieldValue = property.getValue();
                updateSchemaWithReference(schemaName, modifiedName, fieldValue);
                properties.put(property.getKey(), fieldValue);
            }
        }
    }

    /**
     * Common util for handle all the schema type by updating the reference details.
     *
     * @param schemaName   - original schema name
     * @param modifiedName - modified schema
     * @param typeSchema   - type schema
     */
    private static void updateSchemaWithReference(String schemaName, String modifiedName, Schema typeSchema) {
        String ref = typeSchema.get$ref();
        if (ref != null) {
            updateRef(schemaName, modifiedName, typeSchema);
        } else if (typeSchema instanceof ObjectSchema objectSchema) {
            Map<String, Schema> objectSchemaProperties = objectSchema.getProperties();
            updateObjectPropertyRef(objectSchemaProperties, schemaName, modifiedName);
        } else if (typeSchema instanceof ArraySchema arraySchema) {
            Schema<?> items = arraySchema.getItems();
            updateSchemaWithReference(schemaName, modifiedName, items);
        } else if (typeSchema instanceof MapSchema mapSchema) {
            updateObjectPropertyRef(mapSchema.getProperties(), schemaName, modifiedName);
        } else if (typeSchema.getProperties() != null) {
            updateObjectPropertyRef(typeSchema.getProperties(), schemaName, modifiedName);
        } else if (typeSchema instanceof ComposedSchema composedSchema) {
            if (composedSchema.getAllOf() != null) {
                List<Schema> allOf = composedSchema.getAllOf();
                List<Schema> modifiedAllOf = new ArrayList<>();
                for (Schema schema : allOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedAllOf.add(schema);
                }
                composedSchema.setAllOf(modifiedAllOf);
            }
            if (composedSchema.getOneOf() != null) {
                List<Schema> oneOf = composedSchema.getOneOf();
                List<Schema> modifiedOneOf = new ArrayList<>();
                for (Schema schema : oneOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedOneOf.add(schema);
                }
                composedSchema.setOneOf(modifiedOneOf);
            }
            if (composedSchema.getAnyOf() != null) {
                List<Schema> anyOf = composedSchema.getAnyOf();
                List<Schema> modifiedAnyOf = new ArrayList<>();
                for (Schema schema : anyOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedAnyOf.add(schema);
                }
                composedSchema.setAnyOf(modifiedAnyOf);
            }
        } 
        if (typeSchema.getAdditionalProperties() != null && 
                typeSchema.getAdditionalProperties() instanceof Schema<?> addtionalSchema) {
            updateSchemaWithReference(schemaName, modifiedName, addtionalSchema);
        } 
    }

    private static void updateRef(String schemaName, String modifiedName, Schema value) {
        String ref = value.get$ref().replaceAll("\\s+", "");
        String patternString = "/" + schemaName.replaceAll("\\s+", "") + "(?!\\S)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(ref);
        ref = matcher.replaceAll("/" + modifiedName);
        value.set$ref(ref);
    }

    /**
     * This util is to update the given OAS operation with modified schema name.
     *
     * @param schemaName   - original schema name
     * @param modifiedName - modified schema name
     * @param operation    - OAS operation.
     */
    private static void updateSchemaReferenceDetailsWithOperation(String schemaName, String modifiedName,
                                                                  Operation operation) {
        // update parameters type
        if (operation.getParameters() != null) {
            updateParameterSchemaType(schemaName, modifiedName, operation);
        }
        // update request body
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null) {
            RequestBody modifiedRequestBody = updateRequestBodySchemaType(schemaName, modifiedName, requestBody);
            operation.setRequestBody(modifiedRequestBody);
        }
        // update response
        ApiResponses responses = operation.getResponses();
        responses.forEach((statusCode, response) -> {
            response = updateSchemaTypeForResponses(schemaName, modifiedName, response);
            responses.put(statusCode, response);
        });
    }

    private static ApiResponse updateSchemaTypeForResponses(String schemaName, String modifiedName,
                                                            ApiResponse response) {
        Content content = response.getContent();
        if (content != null) {
            for (Map.Entry<String, MediaType> stringMediaTypeEntry : content.entrySet()) {
                MediaType mediaType = stringMediaTypeEntry.getValue();
                Schema<?> responseSchemaType = mediaType.getSchema();
                if (responseSchemaType != null) {
                    String ref = responseSchemaType.get$ref();
                    if (ref != null) {
                        updateSchemaWithReference(schemaName, modifiedName, responseSchemaType);
                    }

                    mediaType.setSchema(responseSchemaType);
                }
                content.put(stringMediaTypeEntry.getKey(), mediaType);
            }
            response.setContent(content);
        }
        return response;
    }

    private static RequestBody updateRequestBodySchemaType(String schemaName, String modifiedName,
                                                           RequestBody requestBody) {
        Content content = requestBody.getContent();
        if (content != null) {
            for (Map.Entry<String, MediaType> stringMediaTypeEntry : content.entrySet()) {
                MediaType mediaType = stringMediaTypeEntry.getValue();
                Schema<?> requestSchemaType = mediaType.getSchema();
                if (requestSchemaType != null) {
                    updateSchemaWithReference(schemaName, modifiedName, requestSchemaType);
                    mediaType.setSchema(requestSchemaType);
                }
                content.put(stringMediaTypeEntry.getKey(), mediaType);
            }
        }
        requestBody.setContent(content);
        return requestBody;
    }

    private static void updateParameterSchemaType(String schemaName, String modifiedName, Operation operation) {
        List<Parameter> modifiedParameters = new ArrayList<>();
        for (Parameter parameter : operation.getParameters()) {
            if (parameter.getSchema() != null) {
                Schema<?> paramSchema = parameter.getSchema();
                updateSchemaWithReference(schemaName, modifiedName, paramSchema);
                parameter.setSchema(paramSchema);
            }
            modifiedParameters.add(parameter);
        }
        operation.setParameters(modifiedParameters);
    }

    public static String getValidNameForType(String identifier) {
        if (identifier.isBlank()) {
            return "\\" + identifier;
        }
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(GeneratorConstants.ESCAPE_PATTERN_FOR_MODIFIER);
            StringBuilder validName = new StringBuilder();
            for (String part : split) {
                if (!part.isBlank()) {
                    if (split.length > 1) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                part.substring(1).toLowerCase(Locale.ENGLISH);
                    }
                    validName.append(part);
                }
            }
            identifier = validName.toString();
        } else {
            identifier = "Schema" + identifier;
        }
        return (identifier.substring(0, 1).toUpperCase(Locale.ENGLISH) + identifier.substring(1)).trim();
    }

    public static Optional<String> getValidNameForParameter(String identifier) {
        if (identifier.isBlank()) {
            return Optional.empty();
        }
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(GeneratorConstants.ESCAPE_PATTERN_FOR_MODIFIER);
            StringBuilder validName = new StringBuilder();
            for (String part : split) {
                if (!part.isBlank()) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                part.substring(1).toLowerCase(Locale.ENGLISH);
                    validName.append(part);
                }
            }
            String modifiedName = validName.substring(0, 1).toLowerCase(Locale.ENGLISH) + validName.substring(1);
            identifier = split.length > 1 ? modifiedName : identifier;
        } else {
            identifier = "param" + identifier;
        }
        return Optional.of(identifier.trim());
    }

    public static List<String> collectParameterNames(List<Parameter> parameters) {
        return parameters.stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    public static String replaceContentInBraces(String input, String expectedValue) {
        String regex = "\\{([^}]*)\\}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();

        int lastEnd = 0;
        while (matcher.find()) {
            result.append(input, lastEnd, matcher.start());
            String content = getValidNameForParameter(matcher.group(1)).get();
            if (content.equals(expectedValue)) {
                // Replace the content with the replacement
                result.append("{").append(expectedValue).append("}");
            } else {
                result.append(matcher.group());
            }
            lastEnd = matcher.end();
        }

        // Append the remaining text after the last match
        result.append(input.substring(lastEnd));
        return result.toString();
    }
}
