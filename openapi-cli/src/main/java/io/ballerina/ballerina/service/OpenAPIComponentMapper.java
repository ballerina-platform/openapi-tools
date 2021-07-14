package io.ballerina.ballerina.service;

import io.ballerina.ballerina.Constants;
import io.ballerina.ballerina.ConverterUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class OpenAPIComponentMapper {
    private Components components;
    private SemanticModel semanticModel;
    private ConverterUtils converterUtils = new ConverterUtils();

    public OpenAPIComponentMapper(Components components, SemanticModel semanticModel) {

        this.components = components;
        this.semanticModel = semanticModel;
    }

    public void handleRecordPayload(SimpleNameReferenceNode queryParam, Map<String, Schema> schema,
                                    TypeSymbol typeSymbol) {
        String componentName = typeSymbol.getName().orElseThrow().trim();
        Schema componentSchema = new Schema();
        componentSchema.setType("object");
        Map<String, Schema> schemaProperties = new HashMap<>();
        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
            // Handle record type request body
            if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
                    String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
                    Schema property = converterUtils.getOpenApiSchema(type);
                    if (type.equals(Constants.TYPE_REFERENCE) && property.get$ref().
                            equals("#/components/schemas/true")) {
                        property.set$ref(field.getValue().typeDescriptor().getName().orElseThrow().trim());
                        Optional<TypeSymbol> recordSymbol = semanticModel.type(field.getValue().location().lineRange());
                        TypeSymbol recordVariable =  recordSymbol.orElseThrow();
                        if (recordVariable.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) recordVariable;
                            handleRecordPayload(queryParam, schema, typeRecord);
                            schema = components.getSchemas();
                        }
                    }
                    if (property instanceof ArraySchema) {
                        setArrayProperty(queryParam, schema, field.getValue(), (ArraySchema) property);
                        schema = components.getSchemas();
                    }
                    schemaProperties.put(field.getKey(), property);
                }
                componentSchema.setProperties(schemaProperties);
            }
        }
        if (schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        } else if (schema == null) {
            schema = new HashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
    }

    private void setArrayProperty(SimpleNameReferenceNode queryParam, Map<String, Schema> schema,
                                  RecordFieldSymbol field, ArraySchema property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Schema symbolProperty  = converterUtils.getOpenApiSchema(symbol.typeKind().getName());
        if (symbolProperty.get$ref() != null && symbolProperty.get$ref().equals("#/components/schemas/true")) {
            symbolProperty.set$ref(symbol.getName().orElseThrow().trim());
            //Set the record model to the definition
            if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
                handleRecordPayload(queryParam, schema, typeRecord);
            }
        }
        //Handle nested array type
        if (arrayDimensions > 1) {
            property.setItems(handleArray(arrayDimensions - 1, symbolProperty, new ArraySchema()));
        } else {
            property.setItems(symbolProperty);
        }
    }

    private ArraySchema handleArray(int arrayDimensions, Schema property, ArraySchema arrayProperty) {
        if (arrayDimensions > 1) {
            ArraySchema narray = new ArraySchema();
            arrayProperty.setItems(handleArray(arrayDimensions - 1, property,  narray));
        } else if (arrayDimensions == 1) {
            arrayProperty.setItems(property);
        }
        return arrayProperty;
    }

}
