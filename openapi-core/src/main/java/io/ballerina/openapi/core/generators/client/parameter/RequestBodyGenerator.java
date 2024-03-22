package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.APPLICATION_OCTET_STREAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.EMPTY_RECORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isComposedSchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isObjectSchema;

public class RequestBodyGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    RequestBody requestBody;
    List<ClientDiagnostic> diagnostics;

    RequestBodyGenerator(RequestBody requestBody, OpenAPI openAPI) {
        this.requestBody = requestBody;
        this.openAPI = openAPI;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode() {
        Content requestBodyContent;
        String referencedRequestBodyName = "";
        if (requestBody.get$ref() != null) {
            try {
                referencedRequestBodyName = extractReferenceType(requestBody.get$ref()).trim();
            } catch (BallerinaOpenApiException e) {
                //todo diagnostic

            }
            RequestBody referencedRequestBody = openAPI.getComponents()
                    .getRequestBodies().get(referencedRequestBodyName);
            requestBodyContent = referencedRequestBody.getContent();
            // note : when there is referenced request body, the description at the reference is ignored.
            // Need to consider the description at the component level
            requestBody.setDescription(referencedRequestBody.getDescription());
        } else {
            requestBodyContent = requestBody.getContent();
        }
        Iterator<Map.Entry<String, MediaType>> iterator = requestBodyContent.entrySet().iterator();
        while (iterator.hasNext()) {
            // This implementation currently for first content type
            Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
            Schema schema = mediaTypeEntry.getValue().getSchema();
            String paramType;
            //Take payload type
            if (schema != null && GeneratorUtils.isSupportedMediaType(mediaTypeEntry)) {
                String mediaTypeEntryKey = mediaTypeEntry.getKey();
                if (mediaTypeEntryKey.equals(APPLICATION_OCTET_STREAM) ||
                        mediaTypeEntryKey.matches("application/.*\\+octet-stream")) {
                    paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                } else {
                    if (schema.get$ref() != null) {
                        try {
                            paramType = getValidName(extractReferenceType(schema.get$ref().trim()), true);
                        } catch (BallerinaOpenApiException e) {
                            //todo diagnostic
                        }
                    } else if (getOpenAPIType(schema) != null && !getOpenAPIType(schema).equals(ARRAY) &&
                            !getOpenAPIType(schema).equals(
                                    OBJECT)) {
                        paramType = TypeHandler.getInstance.(schema);
                    } else if (isArraySchema(schema)) {
                        //TODO: handle nested array - this is impossible to handle
                        paramType = getRequestBodyParameterForArraySchema(operationId, mediaTypeEntry, schema);
                    } else if (isObjectSchema(schema) || schema.getProperties() != null) {
                        paramType = getRequestBodyParameterForObjectSchema(referencedRequestBodyName, schema);
                    } else { // composed and object schemas are handled by the flatten
                        paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                    }
                }
            } else {
                paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true);
            }
        }
        return null;
    }

    /**
     * Generate RequestBody for array type schema.
     */
    private String getRequestBodyParameterForArraySchema(String operationId, Map.Entry<String, MediaType> next,
                                                         Schema<?> arraySchema) throws BallerinaOpenApiException {

        String paramType;
        Schema<?> arrayItems = arraySchema.getItems();
        if (getOpenAPIType(arrayItems) != null) {
            paramType = convertOpenAPITypeToBallerina(arrayItems) + SQUARE_BRACKETS;
        } else if (arrayItems.get$ref() != null) {
            paramType = getValidName(extractReferenceType(arrayItems.get$ref()), true) + SQUARE_BRACKETS;
        } else if (isComposedSchema(arrayItems)) {
            paramType = "CompoundArrayItem" + getValidName(operationId, true) + "Request";
            // TODO - Add API doc by checking requestBody
            TypeDefinitionNode arrayTypeNode =
                    ballerinaSchemaGenerator.getTypeDefinitionNode(arraySchema, paramType, new ArrayList<>());
            GeneratorUtils.updateTypeDefNodeList(paramType, arrayTypeNode, typeDefinitionNodeList);
        } else {
            paramType = getBallerinaMediaType(next.getKey().trim(), true) + SQUARE_BRACKETS;
        }
        return paramType;
    }


    private String getRequestBodyParameterForObjectSchema (String recordName, Schema objectSchema)
            throws BallerinaOpenApiException {
        recordName = getValidName(recordName + "_RequestBody", true);
        if (objectSchema.getProperties() == null || objectSchema.getProperties().isEmpty()) {
            return EMPTY_RECORD;
        }
        TypeDefinitionNode record =
                ballerinaSchemaGenerator.getTypeDefinitionNode(objectSchema, recordName, new ArrayList<>());
        GeneratorUtils.updateTypeDefNodeList(recordName, record, typeDefinitionNodeList);
        return recordName;
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}