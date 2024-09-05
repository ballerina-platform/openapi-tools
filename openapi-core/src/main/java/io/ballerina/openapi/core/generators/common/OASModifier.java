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

import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is to contain the sanitized utils for naming.
 *
 * @since 2.1.0
 */
public class OASModifier {
    private static final String REGEX_FOR_CURLY_BRACED_CONTENT = "\\{([^}]*)}";
    private static final String COMPONENT_REF_WITH_NAME_PATTERN = "\"$ref\":\"#/components/schemas/%s\"";
    private static final String COMPONENT_REF_REGEX_PATTERN = "(\\\"\\$ref\\\"\\s*:\\s*\\\"#/components/schemas/%s" +
            "\\\")|('\\$ref'\\s*:\\s*'#/components/schemas/%s')";
    private static final PrintStream outErrorStream = System.err;

    List<Diagnostic> diagnostics = new ArrayList<>();

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public Map<String, String> getProposedNameMapping(OpenAPI openapi) {
        Map<String, String> nameMap = new HashMap<>();
        if (openapi.getComponents() == null) {
            return Collections.emptyMap();
        }
        Components components = openapi.getComponents();
        Map<String, Schema> schemas = components.getSchemas();
        if (schemas == null) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, Schema> schemaEntry: schemas.entrySet()) {
            String modifiedName = getValidNameForType(schemaEntry.getKey());
            nameMap.put(schemaEntry.getKey(), modifiedName);
        }
        return getResolvedNameMapping(nameMap);
    }

    private static Map<String, String> getResolvedNameMapping(Map<String, String> nameMap) {
        Map<String, String> resolvedNames = new HashMap<>();
        Map<String, Integer> nameCount = new HashMap<>();

        for (Map.Entry<String, String> entry : nameMap.entrySet()) {
            String currentName = entry.getKey();
            String newName = entry.getValue();
            String resolvedName = newName;

            while (resolvedNames.containsValue(resolvedName)) {
                int count = nameCount.getOrDefault(newName, 1);
                resolvedName = newName + count;
                nameCount.put(newName, count + 1);
            }

            resolvedNames.put(currentName, resolvedName);
        }

        return resolvedNames;
    }


    public OpenAPI modifyWithBallerinaConventions(OpenAPI openapi, Map<String, String> nameMap)
            throws BallerinaOpenApiException {
        // This is for data type name modification
        openapi = modifyOASWithSchemaName(openapi, nameMap);
        Paths paths = openapi.getPaths();
        if (paths == null || paths.isEmpty()) {
            return openapi;
        }
        // This is for path parameter name modifications
        Paths modifiedPaths = new Paths();
        for (Map.Entry<String, PathItem> path : paths.entrySet()) {
            PathDetails result = updateParameterNameDetails(path);
            modifiedPaths.put(result.pathValue(), result.pathItem());
        }
        openapi.setPaths(modifiedPaths);
        return openapi;
    }

    public OpenAPI modifyWithBallerinaConventions(OpenAPI openapi) throws BallerinaOpenApiException {
        Map<String, String> proposedNameMapping = getProposedNameMapping(openapi);
        if (proposedNameMapping.isEmpty()) {
            return openapi;
        }
        return modifyWithBallerinaConventions(openapi, proposedNameMapping);
    }

    private static OpenAPI modifyOASWithSchemaName(OpenAPI openapi, Map<String, String> nameMap)
            throws BallerinaOpenApiException {
        Components components = openapi.getComponents();
        if (Objects.isNull(components) || nameMap.isEmpty() || Objects.isNull(components.getSchemas())) {
            return openapi;
        }

        Map<String, Schema> schemas = components.getSchemas();
        Map<String, Schema> modifiedSchemas = new HashMap<>();

        for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
            String schemaName = schema.getKey();
            String modifiedName = nameMap.get(schemaName);
            if (Objects.nonNull(modifiedName) && !modifiedName.equals(schemaName)) {
                modifiedSchemas.put(modifiedName, schema.getValue());
            } else {
                modifiedSchemas.put(schemaName, schema.getValue());
            }
        }

        components.setSchemas(modifiedSchemas);
        openapi.setComponents(components);

        String openApiJson = Json.pretty(openapi);
        for (Map.Entry<String, String> entry : nameMap.entrySet()) {
            String schemaName = entry.getKey();
            String modifiedName = entry.getValue();
            if (schemaName.equals(modifiedName)) {
                continue;
            }
            String patternString = COMPONENT_REF_REGEX_PATTERN.formatted(schemaName, schemaName);
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(openApiJson);

            String replacement = COMPONENT_REF_WITH_NAME_PATTERN.formatted(modifiedName).replace("$", "\\$");
            openApiJson = matcher.replaceAll(replacement);
        }

        ParseOptions parseOptions = new ParseOptions();
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(openApiJson, null, parseOptions);
        OpenAPI openAPI = parseResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            List<String> messages = parseResult.getMessages();
            if (Objects.nonNull(messages) && !messages.isEmpty()) {
                outErrorStream.println("Sanitized OpenAPI contains errors: ");
                messages.forEach(outErrorStream::println);
            }
            throw new BallerinaOpenApiException("Failed to generate the sanitized OpenAPI specification. Please " +
                    "consider generating the client/service without the sanitization option.");
        }
        return openAPI;
    }

    private static PathDetails updateParameterNameDetails(Map.Entry<String, PathItem> path) {
        PathItem pathItem = path.getValue();
        String pathValue = path.getKey();
        if (pathItem.getGet() != null) {
            Operation operation = pathItem.getGet();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setGet(operation);
        }
        if (pathItem.getPost() != null) {
            Operation operation = pathItem.getPost();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPost(operation);
        }
        if (pathItem.getPut() != null) {
            Operation operation = pathItem.getPut();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPut(operation);
        }
        if (pathItem.getDelete() != null) {
            Operation operation = pathItem.getDelete();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setDelete(operation);
        }
        if (pathItem.getPatch() != null) {
            Operation operation = pathItem.getPatch();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPatch(operation);
        }
        if (pathItem.getHead() != null) {
            Operation operation = pathItem.getHead();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setHead(operation);
        }
        if (pathItem.getOptions() != null) {
            Operation operation = pathItem.getOptions();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setOptions(operation);
        }
        if (pathItem.getTrace() != null) {
            Operation operation = pathItem.getTrace();
            if (operation.getParameters() == null) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setTrace(operation);
        }
        return new PathDetails(pathItem, pathValue);
    }

    private static String updateParameterNames(String pathValue, Operation operation) {
        Map<String, String> parameterNames = collectParameterNames(operation.getParameters());
        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            List<Parameter> modifiedParameters = new ArrayList<>();
            pathValue = updateParameters(pathValue, parameterNames, parameters, modifiedParameters);
            operation.setParameters(modifiedParameters);
        }
        return pathValue;
    }

    private static String updateParameters(String pathValue, Map<String, String> parameterNames,
                                           List<Parameter> parameters, List<Parameter> modifiedParameters) {

        for (Parameter parameter : parameters) {
            if (!parameter.getIn().equals("path")) {
                modifiedParameters.add(parameter);
                continue;
            }
            String oasPathParamName = parameter.getName();
            String modifiedPathParam = parameterNames.get(oasPathParamName);
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
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) + part.substring(1);
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

    public static String getValidNameForParameter(String identifier) {
        if (identifier.isBlank()) {
            return "param";
        }
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(GeneratorConstants.ESCAPE_PATTERN_FOR_MODIFIER);
            StringBuilder validName = new StringBuilder();
            for (String part : split) {
                if (!part.isBlank()) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) + part.substring(1);
                    validName.append(part);
                }
            }
            String modifiedName = validName.substring(0, 1).toLowerCase(Locale.ENGLISH) + validName.substring(1);
            identifier = split.length > 1 ? modifiedName : identifier;
        } else {
            identifier = "param" + identifier;
        }
        return identifier.trim();
    }

    public static Map<String, String> collectParameterNames(List<Parameter> parameters) {
        Map<String, String> parameterNames = new HashMap<>();
        for (Parameter parameter: parameters) {
            parameterNames.put(parameter.getName(), getValidNameForParameter(parameter.getName()));
        }
        return getResolvedNameMapping(parameterNames);
    }

    public static String replaceContentInBraces(String input, String expectedValue) {
        Pattern pattern = Pattern.compile(REGEX_FOR_CURLY_BRACED_CONTENT);
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();

        int lastEnd = 0;
        while (matcher.find()) {
            result.append(input, lastEnd, matcher.start());
            String content = getValidNameForParameter(matcher.group(1));
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
