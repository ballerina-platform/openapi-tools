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
package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.type.extension.BallerinaTypeExtensioner;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.OpenAPISchema2JsonSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper.extractNodesFromProject;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getTypeName;

/**
 * This {@link TypeMapperImpl} class is the implementation of the {@link TypeMapper} interface.
 * This class provides the functionality for mapping the Ballerina types to OpenAPI type schema.
 *
 * @since 1.9.0
 */
public class TypeMapperImpl implements TypeMapper {

    public static final String COMPONENTS_SCHEMAS = "#/components/schemas/";
    public static final String DEFINITIONS = "#/definitions/";
    public static final String DEFINITIONS_FIELD = "definitions";
    public static final String JSON_SCHEMA_FIELD = "$schema";
    public static final String JSON_SCHEMA_VERSION = "http://json-schema.org/draft-04/schema#";
    private final Components components;
    private final AdditionalData componentMapperData;
    private final OpenAPISchema2JsonSchema converter = new OpenAPISchema2JsonSchema();

    public TypeMapperImpl(Components components, AdditionalData componentMapperData) {
        this.components = components;
        this.componentMapperData = componentMapperData;
    }

    public TypeMapperImpl(SyntaxNodeAnalysisContext context) {
        this.components = new Components().schemas(new HashMap<>());
        SemanticModel semanticModel = context.semanticModel();
        Project project = context.currentPackage().project();
        ModuleMemberVisitor moduleMemberVisitor = extractNodesFromProject(project, semanticModel);
        this.componentMapperData = new AdditionalData(semanticModel, moduleMemberVisitor);
    }

    public Schema getSchema(TypeSymbol typeSymbol) throws UnsupportedOperationException {
        return getSchema(typeSymbol, true).schema();
    }

    public SchemaResult getSchema(TypeSymbol typeSymbol, boolean enableExpansion)
            throws UnsupportedOperationException {
        if (!isAnydata(typeSymbol)) {
            throw new UnsupportedOperationException("Only 'anydata' type is supported for JSON schema generation.");
        }
        Components components = new Components().schemas(new HashMap<>());
        AdditionalData componentMapperData = cloneComponentMapperData(enableExpansion);
        Schema schema = getTypeSchema(typeSymbol, components, componentMapperData);
        if (componentMapperData.enableExpansion() && componentMapperData.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.getCode().equals("OAS_CONVERTOR_140"))) {
            throw new UnsupportedOperationException("Recursive references are not supported for JSON schema " +
                    "generation with reference expansion.");
        }
        BallerinaTypeExtensioner.removeExtensionFromSchema(schema);
        return new SchemaResult(schema, components);
    }

    public String getJsonSchemaString(TypeSymbol typeSymbol) throws UnsupportedOperationException {
        return getJsonSchemaString(typeSymbol, false);
    }

    public String getJsonSchemaString(TypeSymbol typeSymbol, boolean enableExpansion)
            throws UnsupportedOperationException {
        SchemaResult result = getSchema(typeSymbol, enableExpansion);
        converter.process(result.schema());
        Map<String, Object> jsonSchema = result.schema().getJsonSchema();
        jsonSchema.put(JSON_SCHEMA_FIELD, JSON_SCHEMA_VERSION);
        if (componentMapperData.enableExpansion()) {
            return getJsonString(jsonSchema);
        }
        populateDefinitions(result.components(), jsonSchema);
        return getJsonString(jsonSchema);
    }

    private void populateDefinitions(Components components, Map<String, Object> jsonSchema) {
        Map<String, Object> definitions = new HashMap<>();
        Map<String, Schema> schemas = components.getSchemas();
        BallerinaTypeExtensioner.removeExtensionFromComponents(components);
        if (Objects.nonNull(schemas)) {
            schemas.forEach((key, value) -> {
                if (Objects.nonNull(value)) {
                    converter.process(value);
                    definitions.put(key, value.getJsonSchema());
                }
            });
        }
        if (!definitions.isEmpty()) {
            jsonSchema.put(DEFINITIONS_FIELD, definitions);
        }
    }

    private static String getJsonString(Map<String, Object> jsonSchema) {
        String jsonSchemaString = Json.pretty(jsonSchema);
        return jsonSchemaString.replaceAll(COMPONENTS_SCHEMAS, DEFINITIONS);
    }

    private AdditionalData cloneComponentMapperData(boolean enableExpansion) {
        return new AdditionalData(componentMapperData.semanticModel(), componentMapperData.moduleMemberVisitor(),
                new ArrayList<>(), false, enableExpansion);
    }

    private boolean isAnydata(TypeSymbol typeSymbol) {
        return typeSymbol.subtypeOf(componentMapperData.semanticModel().types().ANYDATA);
    }

    public Schema getTypeSchema(TypeSymbol typeSymbol) {
        return getTypeSchema(typeSymbol, components, componentMapperData, false);
    }

    public static Schema getTypeSchema(TypeSymbol typeSymbol, Components components,
                                       AdditionalData componentMapperData) {
        return getTypeSchema(typeSymbol, components, componentMapperData, false);
    }

    public static Schema getTypeSchema(TypeSymbol typeSymbol, Components components, AdditionalData componentMapperData,
                                       boolean skipNilType) {
        String typeName = typeSymbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE) ?
                MapperCommonUtils.getTypeName(typeSymbol) : "";
        if (componentMapperData.enableExpansion()) {
            if (componentMapperData.visitedTypes().contains(MapperCommonUtils.getTypeName(typeSymbol))) {
                ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_140);
                componentMapperData.diagnostics().add(error);
                return null;
            }
            if (!typeName.isEmpty()) {
                componentMapperData.visitedTypes().add(typeName);
            }
            TypeSymbol referredType = ReferenceTypeMapper.getReferredType(typeSymbol);
            if (Objects.nonNull(referredType)) {
                typeSymbol = referredType;
            }
        }
        Schema schema = switch (typeSymbol.typeKind()) {
            case MAP -> MapTypeMapper.getSchema((MapTypeSymbol) typeSymbol, components, componentMapperData);
            case ARRAY ->
                    ArrayTypeMapper.getSchema((ArrayTypeSymbol) typeSymbol, components, componentMapperData);
            case TYPE_REFERENCE -> ReferenceTypeMapper.getSchema((TypeReferenceTypeSymbol) typeSymbol, components,
                    componentMapperData);
            case RECORD -> RecordTypeMapper.getSchema((RecordTypeSymbol) typeSymbol, components,
                    null, componentMapperData);
            case INTERSECTION ->
                    ReadOnlyTypeMapper.getSchema((IntersectionTypeSymbol) typeSymbol, components, componentMapperData);
            case UNION ->
                    UnionTypeMapper.getSchema((UnionTypeSymbol) typeSymbol, components, componentMapperData,
                            skipNilType);
            case TABLE ->
                    TableTypeMapper.getSchema((TableTypeSymbol) typeSymbol, components, componentMapperData);
            case TUPLE ->
                    TupleTypeMapper.getSchema((TupleTypeSymbol) typeSymbol, components, componentMapperData);
            case ERROR ->
                    ErrorTypeMapper.getSchema((ErrorTypeSymbol) typeSymbol, components, componentMapperData);
            default -> SimpleTypeMapper.getTypeSchema(typeSymbol, componentMapperData);
        };
        if (componentMapperData.enableExpansion() && !typeName.isEmpty()) {
            componentMapperData.visitedTypes().remove(typeName);
        }
        return schema;
    }

    protected static void createComponentMapping(TypeReferenceTypeSymbol typeSymbol, Components components,
                                                 AdditionalData componentMapperData) {
        Map<String, Schema> schemas = components.getSchemas();
        if (schemas.containsKey(getTypeName(typeSymbol))) {
            return;
        }
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        AbstractTypeMapper mapper = switch (referredType.typeKind()) {
            case TYPE_REFERENCE -> new ReferenceTypeMapper(typeSymbol, componentMapperData);
            case MAP -> new MapTypeMapper(typeSymbol, componentMapperData);
            case RECORD -> new RecordTypeMapper(typeSymbol, componentMapperData);
            case INTERSECTION -> new ReadOnlyTypeMapper(typeSymbol, componentMapperData);
            case ARRAY -> new ArrayTypeMapper(typeSymbol, componentMapperData);
            case UNION -> new UnionTypeMapper(typeSymbol, componentMapperData);
            case TABLE -> new TableTypeMapper(typeSymbol, componentMapperData);
            case TUPLE -> new TupleTypeMapper(typeSymbol, componentMapperData);
            case ERROR -> new ErrorTypeMapper(typeSymbol, componentMapperData);
            default -> new SimpleTypeMapper(typeSymbol, componentMapperData);
        };
        components.addSchemas(getTypeName(typeSymbol), null);
        mapper.addToComponents(components);
    }

    public Map<String, Schema> getSchemaForRecordFields(Map<String, RecordFieldSymbol> recordFieldMap,
                                                        Set<String> requiredFields, String recordName,
                                                        boolean treatNilableAsOptional) {

        RecordTypeMapper.RecordFieldMappingContext context = new RecordTypeMapper.RecordFieldMappingContext(
                recordFieldMap, components, requiredFields, recordName, treatNilableAsOptional,
                false, componentMapperData, new HashSet<>());
        return RecordTypeMapper.mapRecordFields(context);
    }

    public TypeSymbol getReferredType(TypeSymbol typeSymbol) {
        return ReferenceTypeMapper.getReferredType(typeSymbol);
    }

    public IntersectionTypeSymbol getReferredIntersectionType(TypeSymbol typeSymbol) {
        return ReferenceTypeMapper.getReferredIntersectionType(typeSymbol);
    }
}
