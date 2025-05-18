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
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
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
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PATH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SLASH;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

/**
 * This class is to contain the aligned utils for naming.
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
    public static final String ENDS_WITH_FULLSTOP = "\\.$";
    public static final String EMPTY = "";
    public static final String LINE_BREAK = "\n";
    public static final String SLASH = "/";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSED_BRACE = "}";
    public static final String NAME = "name";
    public static final String DOC = "doc";
    public static final String BASEPATH = "basepath";
    public static final List<String> DEFAULT_ALIGNMENT_TYPES = List.of(NAME, DOC, BASEPATH);

    List<Diagnostic> diagnostics = new ArrayList<>();

    public OpenAPI modify(OpenAPI openAPI) throws BallerinaOpenApiException {
        return modify(openAPI, DEFAULT_ALIGNMENT_TYPES);
    }

    public OpenAPI modify(OpenAPI openAPI, List<String> alignmentTypes) throws BallerinaOpenApiException {
        if (alignmentTypes.contains(NAME)) {
            openAPI = modifyWithBallerinaNamingConventions(openAPI);
        }
        if (alignmentTypes.contains(DOC)) {
            openAPI = modifyDescriptions(openAPI);
        }
        if (alignmentTypes.contains(BASEPATH)) {
            openAPI = modifyWithCommonBasePath(openAPI);
        }
        return openAPI;
    }

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


    public OpenAPI modifyWithBallerinaNamingConventions(OpenAPI openapi, Map<String, String> nameMap)
            throws BallerinaOpenApiException {
        openapi = modifyOASWithSchemaName(openapi, nameMap);
        modifyOASWithParameterName(openapi);
        modifyOASWithObjectPropertyName(openapi);
        modifyOASWithInlineObjectPropertyName(openapi);
        return openapi;
    }

    private void modifyOASWithInlineObjectPropertyName(OpenAPI openAPI) {
        openAPI.getPaths().forEach((path, pathItem) -> modifyNameInPathItem(pathItem));
    }

    private void modifyNameInPathItem(PathItem pathItem) {
        pathItem.readOperationsMap()
                .forEach((method, operation) -> modifyNameInOperationWithInlineObjectSchema(operation));
    }

    private void modifyNameInOperationWithInlineObjectSchema(Operation operation) {
        if (Objects.nonNull(operation.getRequestBody()) && Objects.nonNull(operation.getRequestBody().getContent())) {
            modifyNameInInlineObjectContent(operation.getRequestBody().getContent());
        }
        modifyNameInParametersWithInlineObjectSchema(operation.getParameters());
        modifyNameInResponsesWithInlineObjectSchema(operation.getResponses());
    }

    private void modifyNameInParametersWithInlineObjectSchema(List<Parameter> parameters) {
        if (Objects.isNull(parameters)) {
            return;
        }

        for (Parameter parameter : parameters) {
            if (Objects.nonNull(parameter.getSchema())) {
                modifyNameInInlineObjectSchema(parameter.getSchema());
            }
        }
    }

    private void modifyNameInResponsesWithInlineObjectSchema(Map<String, ApiResponse> responses) {
        responses.forEach((status, response) -> {
            if (Objects.nonNull(response.getContent())) {
                modifyNameInInlineObjectContent(response.getContent());
            }
        });
    }

    private static void modifyNameInInlineObjectSchema(Schema<?> schema) {
        if (Objects.isNull(schema)) {
            return;
        }
        modifyNameInInlineComposedSchema(schema);
        modifyNameInInlineObjectSchemaInternal(schema);
        modifyNameInInlineObjectSchemaItems(schema);
        modifyNameInInlineObjectSchemaWithAdditionalProperties(schema);
        modifyNameInInlineObjectSchemaProperties(schema);
    }

    private static void modifyNameInInlineComposedSchema(Schema<?> schema) {
        if (schema instanceof ComposedSchema composedSchema) {
            modifyNameInSubSchemas(composedSchema.getAllOf());
            modifyNameInSubSchemas(composedSchema.getAnyOf());
            modifyNameInSubSchemas(composedSchema.getOneOf());
        }
    }

    private static void modifyNameInSubSchemas(List<Schema> subSchemas) {
        if (Objects.nonNull(subSchemas)) {
            subSchemas.forEach(OASModifier::modifyNameInInlineObjectSchema);
        }
    }

    private static void modifyNameInInlineObjectSchemaInternal(Schema<?> schema) {
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

    private static void modifyNameInInlineObjectSchemaItems(Schema<?> schema) {
        if (Objects.nonNull(schema.getItems())) {
            modifyNameInInlineObjectSchema(schema.getItems());
        }
    }

    private static void modifyNameInInlineObjectSchemaWithAdditionalProperties(Schema<?> schema) {
        if (schema.getAdditionalProperties() instanceof Schema) {
            modifyNameInInlineObjectSchema((Schema) schema.getAdditionalProperties());
        }
    }

    private static void modifyNameInInlineObjectSchemaProperties(Schema<?> schema) {
        if (Objects.nonNull(schema.getProperties())) {
            schema.getProperties().values().forEach(OASModifier::modifyNameInInlineObjectSchema);
        }
    }

    private static void modifyNameInInlineObjectContent(Map<String, io.swagger.v3.oas.models.media.MediaType> content) {
        for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> entry : content.entrySet()) {
            Schema schema = entry.getValue().getSchema();
            modifyNameInInlineObjectSchema(schema);
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
            modifyNameInInlineComposedSchema(schemaValue);
        }
    }

    private static void modifyOASWithParameterName(OpenAPI openapi) {
        Paths paths = openapi.getPaths();
        if (Objects.isNull(paths) || paths.isEmpty()) {
            return;
        }

        Paths modifiedPaths = new Paths();
        for (Map.Entry<String, PathItem> path : paths.entrySet()) {
            PathDetails result = updateParameterNameDetails(openapi, path);
            modifiedPaths.put(result.pathValue(), result.pathItem());
        }
        openapi.setPaths(modifiedPaths);
    }

    public OpenAPI modifyWithBallerinaNamingConventions(OpenAPI openapi) throws BallerinaOpenApiException {
        Map<String, String> proposedNameMapping = getProposedNameMapping(openapi);
        if (proposedNameMapping.isEmpty()) {
            return openapi;
        }
        return modifyWithBallerinaNamingConventions(openapi, proposedNameMapping);
    }

    private static OpenAPI modifyOASWithSchemaName(OpenAPI openapi, Map<String, String> nameMap)
            throws BallerinaOpenApiException {
        SwaggerParseResult parseResult = getOASWithSchemaNameModification(openapi, nameMap);
        OpenAPI openAPI = parseResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            List<String> messages = parseResult.getMessages();
            if (Objects.nonNull(messages) && !messages.isEmpty()) {
                outErrorStream.println("Aligning schema name for Ballerina contains errors: ");
                messages.forEach(outErrorStream::println);
            }
            throw new BallerinaOpenApiException("Failed to align the OpenAPI specification for Ballerina. Please " +
                    "consider generating the client/service without the sanitization option.");
        }
        removeDefaultServers(openAPI);
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

        Map<String, SecurityScheme> securitySchemes = components.getSecuritySchemes();
        if (Objects.nonNull(securitySchemes) && !securitySchemes.isEmpty()) {
            securitySchemes.forEach((s, securityScheme) -> addBallerinaNameExtension(securityScheme));
        }

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

    private static PathDetails updateParameterNameDetails(OpenAPI openAPI, Map.Entry<String, PathItem> path) {
        PathItem pathItem = path.getValue();
        String pathValue = path.getKey();
        pathValue = updateParameterNames(openAPI, pathValue, pathItem);
        if (Objects.nonNull(pathItem.getGet())) {
            Operation operation = pathItem.getGet();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setGet(operation);
        }
        if (Objects.nonNull(pathItem.getPost())) {
            Operation operation = pathItem.getPost();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setPost(operation);
        }
        if (Objects.nonNull(pathItem.getPut())) {
            Operation operation = pathItem.getPut();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setPut(operation);
        }
        if (Objects.nonNull(pathItem.getDelete())) {
            Operation operation = pathItem.getDelete();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setDelete(operation);
        }
        if (Objects.nonNull(pathItem.getPatch())) {
            Operation operation = pathItem.getPatch();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setPatch(operation);
        }
        if (Objects.nonNull(pathItem.getHead())) {
            Operation operation = pathItem.getHead();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setHead(operation);
        }
        if (Objects.nonNull(pathItem.getOptions())) {
            Operation operation = pathItem.getOptions();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setOptions(operation);
        }
        if (Objects.nonNull(pathItem.getTrace())) {
            Operation operation = pathItem.getTrace();
            if (Objects.isNull(operation.getParameters())) {
                return new PathDetails(pathItem, pathValue);
            }
            pathValue = updateParameterNames(openAPI, pathValue, operation);
            pathItem.setTrace(operation);
        }
        return new PathDetails(pathItem, pathValue);
    }

    private static String updateParameterNames(OpenAPI openAPI, String pathValue, PathItem pathItem) {
        List<Parameter> parameters = pathItem.getParameters();
        if (Objects.nonNull(parameters)) {
            Map<String, String> parameterNames = collectParameterNames(openAPI, pathItem.getParameters());
            List<Parameter> modifiedParameters = new ArrayList<>();
            pathValue = updateParameters(openAPI, pathValue, parameterNames, parameters, modifiedParameters);
            pathItem.setParameters(modifiedParameters);
        }
        return pathValue;
    }

    private static String updateParameterNames(OpenAPI openAPI, String pathValue, Operation operation) {
        Map<String, String> parameterNames = collectParameterNames(openAPI, operation.getParameters());
        List<Parameter> parameters = operation.getParameters();
        if (Objects.nonNull(parameters)) {
            List<Parameter> modifiedParameters = new ArrayList<>();
            pathValue = updateParameters(openAPI, pathValue, parameterNames, parameters, modifiedParameters);
            operation.setParameters(modifiedParameters);
        }
        return pathValue;
    }

    private static String updateParameters(OpenAPI openAPI, String pathValue, Map<String, String> parameterNames,
                                           List<Parameter> parameters, List<Parameter> modifiedParameters) {

        for (Parameter parameter : parameters) {
            Parameter refParameter = parameter;
            if (Objects.nonNull(parameter.get$ref())) {
                try {
                    refParameter = openAPI.getComponents()
                            .getParameters()
                            .get(extractReferenceType(parameter.get$ref()));
                } catch (InvalidReferenceException e) {
                    // Ignore
                    modifiedParameters.add(parameter);
                    continue;
                }
            }
            String location = refParameter.getIn();
            if (Objects.isNull(location)) {
                modifiedParameters.add(parameter);
                continue;
            }
            if (location.equals(PATH)) {
                String modifiedPathParam = parameterNames.get(refParameter.getName());
                refParameter.setName(modifiedPathParam);
                pathValue = replaceContentInBraces(pathValue, modifiedPathParam);
            } else if (location.equals(QUERY) || location.equals(HEADER)) {
                addBallerinaNameExtension(refParameter);
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

        String alignedName = getValidNameForParameter(parameterName);
        if (parameterName.equals(alignedName)) {
            return;
        }

        if (Objects.isNull(parameter.getExtensions())) {
            parameter.setExtensions(new HashMap<>());
        }
        parameter.getExtensions().put(BALLERINA_NAME_EXT, alignedName);
    }

    private static void addBallerinaNameExtension(SecurityScheme securityScheme) {
        String securitySchemeName = securityScheme.getName();
        if (Objects.isNull(securitySchemeName)) {
            return;
        }

        String alignedName = getValidNameForParameter(securitySchemeName);
        if (securitySchemeName.equals(alignedName)) {
            return;
        }

        if (Objects.isNull(securityScheme.getExtensions())) {
            securityScheme.setExtensions(new HashMap<>());
        }
        securityScheme.getExtensions().put(BALLERINA_NAME_EXT, alignedName);
    }

    private static Schema getSchemaWithBallerinaNameExtension(String propertyName, Schema<?> propertySchema) {
        String alignedPropertyName = getValidNameForParameter(propertyName);
        if (propertyName.equals(alignedPropertyName)) {
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
        propertySchema.getExtensions().put(BALLERINA_NAME_EXT, alignedPropertyName);
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
        String ref = value.get$ref().replaceAll("\\s+", EMPTY);
        String patternString = SLASH + schemaName.replaceAll("\\s+", EMPTY) + "(?!\\S)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(ref);
        ref = matcher.replaceAll(SLASH + modifiedName);
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
        if (!identifier.matches("\\b[0-9]*\\b") && identifier.matches(".*[a-zA-Z].*")) {
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
            identifier = prefix + GeneratorUtils.escapeIdentifier(identifier);
        }
        return identifier;
    }

    public static Map<String, String> collectParameterNames(OpenAPI openAPI, List<Parameter> parameters) {
        Map<String, String> parameterNames = new HashMap<>();
        for (Parameter parameter: parameters) {
            if (Objects.nonNull(parameter.get$ref())) {
                try {
                    parameter = openAPI.getComponents()
                            .getParameters()
                            .get(extractReferenceType(parameter.get$ref()));
                } catch (InvalidReferenceException e) {
                    // Ignore
                }
            }
            String parameterName = parameter.getName();
            if (Objects.isNull(parameterName)) {
                continue;
            }
            parameterNames.put(parameter.getName(), getValidNameForParameter(parameterName));
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
                result.append(OPEN_BRACE).append(expectedValue).append(CLOSED_BRACE);
            } else {
                result.append(matcher.group());
            }
            lastEnd = matcher.end();
        }

        // Append the remaining text after the last match
        result.append(input.substring(lastEnd));
        return result.toString();
    }

    public OpenAPI modifyDescriptions(OpenAPI openapi) {
        Paths paths = openapi.getPaths();
        if (Objects.isNull(paths) || paths.isEmpty()) {
            return openapi;
        }
        paths.forEach((path, pathItem) -> modifyDescriptionInPath(openapi, pathItem));

        Components components = openapi.getComponents();
        if (Objects.isNull(components) || Objects.isNull(components.getSchemas())) {
            return openapi;
        }
        components.getSchemas().forEach((schemaName, schema) -> {
            modifyObjectPropertyDescriptions(schema);
        });
        return openapi;
    }

    private static void modifyDescriptionInPath(OpenAPI openapi, PathItem pathItem) {
        List<Parameter> parametersForPath = pathItem.getParameters();
        if (Objects.nonNull(parametersForPath) && !parametersForPath.isEmpty()) {
            parametersForPath.forEach(parameter -> modifyParameterDescription(openapi, parameter));
        }
        pathItem.readOperationsMap()
                .forEach((method, operation) -> modifyDescriptionInOperation(openapi, operation));
    }

    private static void modifyDescriptionInOperation(OpenAPI openapi, Operation operation) {
        modifyParameterDescriptions(openapi, operation);
        modifyRequestBodyDescription(openapi, operation);
        modifyApiResponseDescriptions(openapi, operation);
    }

    private static void modifyObjectPropertyDescriptions(Schema schema) {
        if (Objects.isNull(schema)) {
            return;
        }

        String description = schema.getDescription();
        if (Objects.nonNull(description)) {
            String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
            schema.setDescription(modifiedDescription);
        }

        if (schema instanceof ObjectSchema objectSchema) {
            if (Objects.isNull(objectSchema.getProperties()) || objectSchema.getProperties().isEmpty()) {
                return;
            }
            objectSchema.getProperties()
                    .forEach((propertyName, property) -> modifyPropertyDescription(property));
        } else if (schema instanceof ComposedSchema composedSchema) {
            modifyDescriptionInInlineComposedSchema(composedSchema);
        } else if (schema instanceof ArraySchema) {
            if (Objects.isNull(schema.getItems())) {
                return;
            }
            modifyPropertyDescription(schema.getItems());
        }
    }

    private static void modifyPropertyDescription(Schema schema) {
        String description = schema.getDescription();
        if (Objects.isNull(description)) {
            return;
        }
        String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
        schema.setDescription(modifiedDescription);
    }

    private static void modifyApiResponseDescriptions(OpenAPI openapi, Operation operation) {
        ApiResponses responses = operation.getResponses();
        if (Objects.isNull(responses)) {
            return;
        }
        Collection<ApiResponse> values = responses.values();
        Iterator<ApiResponse> iteratorRes = values.iterator();
        if (iteratorRes.hasNext()) {
            ApiResponse response = iteratorRes.next();
            String description = response.getDescription();
            if (Objects.nonNull(description)) {
                String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
                response.setDescription(modifiedDescription);
            } else if (Objects.nonNull(response.get$ref())) {
                try {
                    if (Objects.isNull(openapi.getComponents()) ||
                            Objects.isNull(openapi.getComponents().getResponses())) {
                        return;
                    }
                    ApiResponse apiResponseRef = openapi.getComponents().getResponses()
                            .get(extractReferenceType(response.get$ref()));
                    description = apiResponseRef.getDescription();
                    if (Objects.isNull(description)) {
                        return;
                    }
                    String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
                    apiResponseRef.setDescription(modifiedDescription);
                } catch (InvalidReferenceException e) {
                    // Ignore
                }
            }
        }
    }

    private static void modifyRequestBodyDescription(OpenAPI openapi, Operation operation) {
        RequestBody requestBody = operation.getRequestBody();
        if (Objects.isNull(requestBody)) {
            return;
        }
        String description = requestBody.getDescription();
        if (Objects.nonNull(description)) {
            String modifiedDescription = getModifiedReqBodyDescription(description, requestBody);
            requestBody.setDescription(modifiedDescription);
        } else if (Objects.nonNull(requestBody.get$ref())) {
            try {
                if (Objects.isNull(openapi.getComponents()) ||
                        Objects.isNull(openapi.getComponents().getRequestBodies())) {
                    return;
                }
                RequestBody requestBodyRef = openapi.getComponents().getRequestBodies()
                        .get(extractReferenceType(requestBody.get$ref()));
                description = requestBodyRef.getDescription();
                if (Objects.isNull(description)) {
                    return;
                }
                String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
                requestBodyRef.setDescription(modifiedDescription);
            } catch (InvalidReferenceException e) {
                // Ignore
            }
        }
    }

    private static String getModifiedReqBodyDescription(String description, RequestBody requestBody) {
        String modifiedDescription;
        if (description.contains(LINE_BREAK)) {
            List<String> descriptionLines = new ArrayList<>(Arrays.asList(requestBody.getDescription()
                    .split(LINE_BREAK)));
            String firstLine = descriptionLines.removeFirst();
            descriptionLines.addFirst(firstLine.replaceAll(ENDS_WITH_FULLSTOP, EMPTY));
            modifiedDescription = String.join(LINE_BREAK, descriptionLines);
        } else {
            modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
        }
        return modifiedDescription;
    }

    private static void modifyParameterDescriptions(OpenAPI openapi, Operation operation) {
        if (Objects.isNull(operation.getParameters())) {
            return;
        }
        operation.getParameters().forEach(parameter -> modifyParameterDescription(openapi, parameter));
    }

    private static void modifyParameterDescription(OpenAPI openapi, Parameter parameter) {
        String description = parameter.getDescription();
        if (Objects.nonNull(description)) {
            String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
            parameter.setDescription(modifiedDescription);
        } else if (Objects.nonNull(parameter.get$ref())) {
            try {
                if (Objects.isNull(openapi.getComponents()) ||
                        Objects.isNull(openapi.getComponents().getParameters())) {
                    return;
                }
                Parameter parameterRef = openapi.getComponents().getParameters()
                        .get(extractReferenceType(parameter.get$ref()));
                description = parameterRef.getDescription();
                if (Objects.isNull(description)) {
                    return;
                }
                String modifiedDescription = description.replaceAll(ENDS_WITH_FULLSTOP, EMPTY);
                parameterRef.setDescription(modifiedDescription);
            } catch (InvalidReferenceException e) {
                // Ignore
            }
        }
    }

    private static void modifyDescriptionInSubSchemas(List<Schema> subSchemas) {
        if (Objects.nonNull(subSchemas)) {
            subSchemas.forEach(OASModifier::modifyObjectPropertyDescriptions);
        }
    }

    private static void modifyDescriptionInInlineComposedSchema(ComposedSchema composedSchema) {
        modifyDescriptionInSubSchemas(composedSchema.getAllOf());
        modifyDescriptionInSubSchemas(composedSchema.getAnyOf());
        modifyDescriptionInSubSchemas(composedSchema.getOneOf());
    }

    public OpenAPI modifyWithCommonBasePath(OpenAPI openapi) {
        Paths paths = openapi.getPaths();
        List<Server> servers = openapi.getServers();
        if (Objects.isNull(paths) || paths.isEmpty() || Objects.isNull(servers) || servers.isEmpty()) {
            return openapi;
        }

        List<String> pathValues = new ArrayList<>(paths.keySet());
        CommonPathResult commonPathResult = getCommonPathResult(pathValues);
        commonPathResult.commonPath().ifPresent(commonPath -> {
            modifyPathNames(openapi, paths, commonPathResult.pathMap());
            modifyServersUrl(servers, commonPath);
        });
        return openapi;
    }

    private static void modifyServersUrl(List<Server> servers, String commonPath) {
        servers.stream()
                .filter(server -> Objects.nonNull(server.getUrl()))
                .forEach(server -> server.setUrl(server.getUrl().concat(commonPath)));
    }

    private static void modifyPathNames(OpenAPI openapi, Paths paths, Map<String, String> pathMap) {
        Paths modifiedPaths = new Paths();
        paths.forEach((pathValue, pathItem) ->
                modifiedPaths.put(pathMap.get(pathValue), pathItem)
        );
        openapi.setPaths(modifiedPaths);
    }

    private record CommonPathResult(Optional<String> commonPath, Map<String, String> pathMap) {
    }

    private static CommonPathResult getCommonPathResult(List<String> pathValues) {
        if (pathValues == null || pathValues.isEmpty()) {
            return new CommonPathResult(Optional.empty(), Map.of());
        }

        Optional<String> commonPath = getCommonPath(pathValues);
        if (commonPath.isEmpty()) {
            return new CommonPathResult(Optional.empty(), Map.of());
        }

        Map<String, String> pathMap = new HashMap<>();
        for (String path : pathValues) {
            pathMap.put(path, removeCommonPath(path, commonPath.get()));
        }
        return new CommonPathResult(commonPath, pathMap);
    }

    private static Optional<String> getCommonPath(List<String> pathValues) {
        String commonPath = getPathWithoutLastSegment(pathValues.getFirst());
        for (int i = 1; i < pathValues.size(); i++) {
            String path = pathValues.get(i);
            String nonParameterizedPath = getNonParameterizedPath(path);
            if (nonParameterizedPath.startsWith(commonPath)) {
                continue;
            }
            commonPath = calculateCommonPath(getPathWithoutLastSegment(nonParameterizedPath), commonPath);
            if (commonPath.isEmpty()) {
                break;
            }
        }
        return (commonPath.isEmpty() || commonPath.equals(SLASH)) ? Optional.empty() : Optional.of(commonPath);
    }

    private static String calculateCommonPath(String path, String commonPath) {
        String[] pathSegments = path.split(SLASH);
        String[] commonPathSegments = commonPath.split(SLASH);
        StringJoiner commonPathBuilder = new StringJoiner(SLASH);
        for (int i = 0; i < Math.min(pathSegments.length, commonPathSegments.length); i++) {
            if (pathSegments[i].equals(commonPathSegments[i])) {
                commonPathBuilder.add(pathSegments[i]);
            } else {
                break;
            }
        }
        return commonPathBuilder.toString();
    }

    private static String getPathWithoutLastSegment(String path) {
        if (path.endsWith(SLASH)) {
            return path;
        }
        String[] pathSegments = path.split(SLASH);
        return Arrays.stream(pathSegments, 0, pathSegments.length - 1)
                .collect(Collectors.joining(SLASH));
    }

    private static String removeCommonPath(String path, String commonPath) {
        return path.replace(commonPath, EMPTY);
    }

    private static String getNonParameterizedPath(String path) {
        StringJoiner nonParameterizedPath = new StringJoiner(SLASH);
        for (String part : path.split(SLASH)) {
            if (part.startsWith(OPEN_BRACE) && part.endsWith(CLOSED_BRACE)) {
                break;
            }
            nonParameterizedPath.add(part);
        }
        return nonParameterizedPath.toString();
    }

    private static void removeDefaultServers(OpenAPI openAPI) {
        List<Server> servers = openAPI.getServers();
        if (Objects.nonNull(servers) && servers.size() == 1 && servers.getFirst().equals(new Server().url(SLASH))) {
            openAPI.setServers(null);
        }
    }
}
