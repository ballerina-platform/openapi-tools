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
import io.swagger.v3.oas.models.responses.ApiResponse;
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

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PATH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY;

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
    private static final String BALLERINA_NAME_EXT = "x-ballerina-name";

    List<Diagnostic> diagnostics = new ArrayList<>();

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public Map<String, String> getProposedNameMapping(OpenAPI openapi) {
        Map<String, String> nameMap = new HashMap<>();
        if (Objects.isNull(openapi.getComponents())) {
            return Collections.emptyMap();
        }
        Components components = openapi.getComponents();
        Map<String, Schema> schemas = components.getSchemas();
        if (Objects.isNull(schemas)) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, Schema> schemaEntry: schemas.entrySet()) {
            String modifiedName = getValidNameForType(schemaEntry.getKey());
            nameMap.put(schemaEntry.getKey(), modifiedName);
        }
        return getResolvedNameMapping(nameMap);
    }

    public static Map<String, String> getResolvedNameMapping(Map<String, String> nameMap) {
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
        openapi = modifyOASWithSchemaName(openapi, nameMap);
        modifyOASWithParameterName(openapi);
        modifyOASWithObjectPropertyName(openapi);
        modifyOASWithInlineObjectPropertyName(openapi);
        return openapi;
    }

    private void modifyOASWithInlineObjectPropertyName(OpenAPI openAPI) {
        openAPI.getPaths().forEach((path, pathItem) -> processPathItem(pathItem));
    }

    private void processPathItem(PathItem pathItem) {
        pathItem.readOperationsMap().forEach((method, operation) -> processOperationWithInlineObjectSchema(operation));
    }

    private void processOperationWithInlineObjectSchema(Operation operation) {
        if (Objects.nonNull(operation.getRequestBody())) {
            updateInlineObjectInContent(operation.getRequestBody().getContent());
        }
        processParametersWithInlineObjectSchema(operation.getParameters());
        processResponsesWithInlineObjectSchema(operation.getResponses());
    }

    private void processParametersWithInlineObjectSchema(List<Parameter> parameters) {
        if (Objects.isNull(parameters)) {
            return;
        }

        for (Parameter parameter : parameters) {
            if (Objects.nonNull(parameter.getSchema())) {
                updateInlineObjectSchema(parameter.getSchema());
            }
        }
    }

    private void processResponsesWithInlineObjectSchema(Map<String, ApiResponse> responses) {
        responses.forEach((status, response) -> {
            if (Objects.nonNull(response.getContent())) {
                updateInlineObjectInContent(response.getContent());
            }
        });
    }

    private void updateInlineObjectSchema(Schema<?> schema) {
        if (Objects.isNull(schema)) {
            return;
        }
        handleInlineObjectSchemaInComposedSchema(schema);
        handleInlineObjectSchema(schema);
        handleInlineObjectSchemaItems(schema);
        handleInlineObjectSchemaInAdditionalProperties(schema);
        handleInlineObjectSchemaProperties(schema);
    }

    private void handleInlineObjectSchemaInComposedSchema(Schema<?> schema) {
        if (schema instanceof ComposedSchema composedSchema) {
            processSubSchemas(composedSchema.getAllOf());
            processSubSchemas(composedSchema.getAnyOf());
            processSubSchemas(composedSchema.getOneOf());
        }
    }

    private void processSubSchemas(List<Schema> subSchemas) {
        if (Objects.nonNull(subSchemas)) {
            subSchemas.forEach(this::updateInlineObjectSchema);
        }
    }

    private static void handleInlineObjectSchema(Schema<?> schema) {
        if (isInlineObjectSchema(schema)) {
            Map<String, Schema> properties = schema.getProperties();
            if (Objects.nonNull(properties) && !properties.isEmpty()) {
                schema.setProperties(getPropertiesWithBallerinaNameExtension(properties));
            }
        }
    }

    private static boolean isInlineObjectSchema(Schema<?> schema) {
        return schema instanceof ObjectSchema ||
                (Objects.isNull(schema.getType()) && Objects.isNull(schema.get$ref()) &&
                        Objects.nonNull(schema.getProperties()));
    }

    private void handleInlineObjectSchemaItems(Schema<?> schema) {
        if (Objects.nonNull(schema.getItems())) {
            updateInlineObjectSchema(schema.getItems());
        }
    }

    private void handleInlineObjectSchemaInAdditionalProperties(Schema<?> schema) {
        if (schema.getAdditionalProperties() instanceof Schema) {
            updateInlineObjectSchema((Schema) schema.getAdditionalProperties());
        }
    }

    private void handleInlineObjectSchemaProperties(Schema<?> schema) {
        if (Objects.nonNull(schema.getProperties())) {
            schema.getProperties().values().forEach(this::updateInlineObjectSchema);
        }
    }

    private void updateInlineObjectInContent(Map<String, io.swagger.v3.oas.models.media.MediaType> content) {
        for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> entry : content.entrySet()) {
            Schema schema = entry.getValue().getSchema();
            updateInlineObjectSchema(schema);
        }
    }

    private static void modifyOASWithObjectPropertyName(OpenAPI openapi) {
        Components components = openapi.getComponents();
        if (Objects.isNull(components) || Objects.isNull(components.getSchemas())) {
            return;
        }

        Map<String, Schema> schemas = components.getSchemas();
        for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
            Schema schemaValue = schema.getValue();
            if (schemaValue instanceof ObjectSchema objectSchema) {
                Map<String, Schema> properties = objectSchema.getProperties();
                if (Objects.nonNull(properties) && !properties.isEmpty()) {
                    objectSchema.setProperties(getPropertiesWithBallerinaNameExtension(properties));
                }
            }
        }
    }

    private static void modifyOASWithParameterName(OpenAPI openapi) {
        Paths paths = openapi.getPaths();
        if (Objects.isNull(paths) || paths.isEmpty()) {
            return;
        }

        Paths modifiedPaths = new Paths();
        for (Map.Entry<String, PathItem> path : paths.entrySet()) {
            PathDetails result = updateParameterNameDetails(path);
            modifiedPaths.put(result.pathValue(), result.pathItem());
        }
        openapi.setPaths(modifiedPaths);
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
        SwaggerParseResult parseResult = getOASWithSchemaNameModification(openapi, nameMap);
        OpenAPI openAPI = parseResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            List<String> messages = parseResult.getMessages();
            if (Objects.nonNull(messages) && !messages.isEmpty()) {
                outErrorStream.println("Schema name sanitization contains errors: ");
                messages.forEach(outErrorStream::println);
            }
            throw new BallerinaOpenApiException("Failed to generate the sanitized OpenAPI specification. Please " +
                    "consider generating the client/service without the sanitization option.");
        }
        return openAPI;
    }

    public static SwaggerParseResult getOASWithSchemaNameModification(OpenAPI openapi, Map<String, String> nameMap) {
        Components components = openapi.getComponents();
        if (Objects.isNull(components) || nameMap.isEmpty() || Objects.isNull(components.getSchemas())) {
            SwaggerParseResult result = new SwaggerParseResult();
            result.setOpenAPI(openapi);
            return result;
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
        return new OpenAPIParser().readContents(openApiJson, null, parseOptions);
    }

    private static PathDetails updateParameterNameDetails(Map.Entry<String, PathItem> path) {
        PathItem pathItem = path.getValue();
        String pathValue = path.getKey();
        if (Objects.nonNull(pathItem.getGet())) {
            Operation operation = pathItem.getGet();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setGet(operation);
        }
        if (Objects.nonNull(pathItem.getPost())) {
            Operation operation = pathItem.getPost();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPost(operation);
        }
        if (Objects.nonNull(pathItem.getPut())) {
            Operation operation = pathItem.getPut();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPut(operation);
        }
        if (Objects.nonNull(pathItem.getDelete())) {
            Operation operation = pathItem.getDelete();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setDelete(operation);
        }
        if (Objects.nonNull(pathItem.getPatch())) {
            Operation operation = pathItem.getPatch();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setPatch(operation);
        }
        if (Objects.nonNull(pathItem.getHead())) {
            Operation operation = pathItem.getHead();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setHead(operation);
        }
        if (Objects.nonNull(pathItem.getOptions())) {
            Operation operation = pathItem.getOptions();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(pathValue, operation);
            pathItem.setOptions(operation);
        }
        if (Objects.nonNull(pathItem.getTrace())) {
            Operation operation = pathItem.getTrace();
            if (Objects.isNull(operation.getParameters())) {
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
        if (Objects.nonNull(parameters)) {
            List<Parameter> modifiedParameters = new ArrayList<>();
            pathValue = updateParameters(pathValue, parameterNames, parameters, modifiedParameters);
            operation.setParameters(modifiedParameters);
        }
        return pathValue;
    }

    private static String updateParameters(String pathValue, Map<String, String> parameterNames,
                                           List<Parameter> parameters, List<Parameter> modifiedParameters) {

        for (Parameter parameter : parameters) {
            String location = parameter.getIn();
            if (location.equals(PATH)) {
                String modifiedPathParam = parameterNames.get(parameter.getName());
                parameter.setName(modifiedPathParam);
                pathValue = replaceContentInBraces(pathValue, modifiedPathParam);
            } else if (location.equals(QUERY) || location.equals(HEADER)) {
                addBallerinaNameExtension(parameter);
            }
            modifiedParameters.add(parameter);
        }
        return pathValue;
    }

    private static void addBallerinaNameExtension(Parameter parameter) {
        String parameterName = parameter.getName();
        if (Objects.isNull(parameterName)) {
            return;
        }

        String sanitizedName = getValidNameForParameter(parameterName);
        if (parameterName.equals(sanitizedName)) {
            return;
        }

        if (Objects.isNull(parameter.getExtensions())) {
            parameter.setExtensions(new HashMap<>());
        }
        parameter.getExtensions().put(BALLERINA_NAME_EXT, sanitizedName);
    }

    private static Schema getSchemaWithBallerinaNameExtension(String propertyName, Schema<?> propertySchema) {
        String sanitizedPropertyName = getValidNameForParameter(propertyName);
        if (propertyName.equals(sanitizedPropertyName)) {
            return propertySchema;
        }

        if (Objects.nonNull(propertySchema.get$ref())) {
            Schema<Object> refSchema = new Schema<>();
            refSchema.set$ref(propertySchema.get$ref());
            propertySchema.set$ref(null);
            propertySchema.addAllOfItem(refSchema);
            propertySchema.setType(null);
        }

        if (Objects.isNull(propertySchema.getExtensions())) {
            propertySchema.setExtensions(new HashMap<>());
        }
        propertySchema.getExtensions().put(BALLERINA_NAME_EXT, sanitizedPropertyName);
        return propertySchema;
    }

    private static Map<String, Schema> getPropertiesWithBallerinaNameExtension(Map<String, Schema> properties) {
        Map<String, Schema> modifiedProperties = new HashMap<>();
        for (Map.Entry<String, Schema> property : properties.entrySet()) {
            Schema<?> propertySchema = property.getValue();
            String propertyName = property.getKey();
            modifiedProperties.put(propertyName, getSchemaWithBallerinaNameExtension(propertyName, propertySchema));
        }
        return modifiedProperties;
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
        if (Objects.nonNull(properties)) {
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
        if (Objects.nonNull(ref)) {
            updateRef(schemaName, modifiedName, typeSchema);
        } else if (typeSchema instanceof ObjectSchema objectSchema) {
            Map<String, Schema> objectSchemaProperties = objectSchema.getProperties();
            updateObjectPropertyRef(objectSchemaProperties, schemaName, modifiedName);
        } else if (typeSchema instanceof ArraySchema arraySchema) {
            Schema<?> items = arraySchema.getItems();
            updateSchemaWithReference(schemaName, modifiedName, items);
        } else if (typeSchema instanceof MapSchema mapSchema) {
            updateObjectPropertyRef(mapSchema.getProperties(), schemaName, modifiedName);
        } else if (Objects.nonNull(typeSchema.getProperties())) {
            updateObjectPropertyRef(typeSchema.getProperties(), schemaName, modifiedName);
        } else if (typeSchema instanceof ComposedSchema composedSchema) {
            if (Objects.nonNull(composedSchema.getAllOf())) {
                List<Schema> allOf = composedSchema.getAllOf();
                List<Schema> modifiedAllOf = new ArrayList<>();
                for (Schema schema : allOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedAllOf.add(schema);
                }
                composedSchema.setAllOf(modifiedAllOf);
            }
            if (Objects.nonNull(composedSchema.getOneOf())) {
                List<Schema> oneOf = composedSchema.getOneOf();
                List<Schema> modifiedOneOf = new ArrayList<>();
                for (Schema schema : oneOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedOneOf.add(schema);
                }
                composedSchema.setOneOf(modifiedOneOf);
            }
            if (Objects.nonNull(composedSchema.getAnyOf())) {
                List<Schema> anyOf = composedSchema.getAnyOf();
                List<Schema> modifiedAnyOf = new ArrayList<>();
                for (Schema schema : anyOf) {
                    updateSchemaWithReference(schemaName, modifiedName, schema);
                    modifiedAnyOf.add(schema);
                }
                composedSchema.setAnyOf(modifiedAnyOf);
            }
        } 
        if (Objects.nonNull(typeSchema.getAdditionalProperties()) && 
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
        identifier = getValidStringFromIdentifier(identifier, "Schema");
        return (identifier.substring(0, 1).toUpperCase(Locale.ENGLISH) + identifier.substring(1)).trim();
    }

    public static String getValidNameForParameter(String identifier) {
        if (identifier.isBlank()) {
            return "param";
        }
        identifier = getValidStringFromIdentifier(identifier, "param");
        return (identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1)).trim();
    }

    private static String getValidStringFromIdentifier(String identifier, String prefix) {
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
            identifier = prefix + identifier;
        }
        return identifier;
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
