package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.APPLICATION_OCTET_STREAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;

public class RequestBodyGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    RequestBody requestBody;
    List<ClientDiagnostic> diagnostics;

    public RequestBodyGenerator(RequestBody requestBody, OpenAPI openAPI) {
        this.requestBody = requestBody;
        this.openAPI = openAPI;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode(boolean treatDefaultableAsRequired) {
        Content requestBodyContent;
        String referencedRequestBodyName = "";
        TypeDescriptorNode typeDescNode = null;
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
        if (requestBodyContent == null || requestBodyContent.isEmpty()) {
            return Optional.empty();
        }
        for (Map.Entry<String, MediaType> mediaTypeEntry : requestBodyContent.entrySet()) {
            // This implementation currently for first content type
            Schema schema = mediaTypeEntry.getValue().getSchema();
            String paramType;
            //Take payload type
            if (schema != null && GeneratorUtils.isSupportedMediaType(mediaTypeEntry)) {
                String mediaTypeEntryKey = mediaTypeEntry.getKey();
                if (mediaTypeEntryKey.equals(APPLICATION_OCTET_STREAM) ||
                        mediaTypeEntryKey.matches("application/.*\\+octet-stream")) {
                    paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                    typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                } else {
                    if (schema.get$ref() != null) {
                        Optional<TypeDescriptorNode> node = TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
                        if (node.isEmpty()) {
                            return Optional.empty();
                        }
                        typeDescNode = node.get();
                    } else if (getOpenAPIType(schema) != null && !getOpenAPIType(schema).equals(ARRAY) &&
                            !getOpenAPIType(schema).equals(OBJECT)) {
                        Optional<TypeDescriptorNode> resultNode = TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
                        if (resultNode.isEmpty()) {
                            return Optional.empty();
                        }
                        typeDescNode = resultNode.get();

                    } else {
                        paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                        typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                    }
                }
            } else {
                //todo
//                createServiceDeclarationNode(MetadataNode)
                Map<String, Encoding> encoding = mediaTypeEntry.getValue().getEncoding();
                if (mediaTypeEntry.getKey().equals(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA)
                        && encoding != null) {
                    List<String> headerList = new ArrayList<>();
//                    for (Map.Entry<String, Encoding> entry : encoding.entrySet()) {
//                        Map<String, Header> headers = entry.getValue().getHeaders();
//                        if (headers != null) {
//                            for (Map.Entry<String, Header> header : headers.entrySet()) {
//                                if (!headerList.contains(header.getKey())) {
//                                    Node headerParameter = getHeaderEncoding(header, requestBodyDoc);
//                                    if (headerParameter instanceof RequiredParameterNode) {
//                                        parameterList.add(headerParameter);
//                                        parameterList.add(createToken((COMMA_TOKEN)));
//                                    } else {
//                                        defaultable.add(headerParameter);
//                                        defaultable.add(createToken((COMMA_TOKEN)));
//                                    }
//                                    headerList.add(header.getKey());
//                                }
//                            }
//                        }
//                    }

                } else {
                    paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true);
                    typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
    //                return Optional.empty();
                }
            }
            break;
        }
        String reqBody = typeDescNode.toString().equals(HTTP_REQUEST)? "request": "payload";
        return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeDescNode, createIdentifierToken(reqBody)));
    }

//    /**
//     * Generate RequestBody for array type schema.
//     */
//    private String getRequestBodyParameterForArraySchema(String operationId, Map.Entry<String, MediaType> next,
//                                                         Schema<?> arraySchema) throws BallerinaOpenApiException {
//
//        String paramType;
//        Schema<?> arrayItems = arraySchema.getItems();
//        if (getOpenAPIType(arrayItems) != null) {
//            paramType = convertOpenAPITypeToBallerina(arrayItems) + SQUARE_BRACKETS;
//        } else if (arrayItems.get$ref() != null) {
//            paramType = getValidName(extractReferenceType(arrayItems.get$ref()), true) + SQUARE_BRACKETS;
//        } else if (isComposedSchema(arrayItems)) {
//            paramType = "CompoundArrayItem" + getValidName(operationId, true) + "Request";
//            // TODO - Add API doc by checking requestBody
//            TypeDefinitionNode arrayTypeNode =
//                    ballerinaSchemaGenerator.getTypeDefinitionNode(arraySchema, paramType, new ArrayList<>());
//            GeneratorUtils.updateTypeDefNodeList(paramType, arrayTypeNode, typeDefinitionNodeList);
//        } else {
//            paramType = getBallerinaMediaType(next.getKey().trim(), true) + SQUARE_BRACKETS;
//        }
//        return paramType;
//    }


//    private String getRequestBodyParameterForObjectSchema (String recordName, Schema objectSchema)
//            throws BallerinaOpenApiException {
//        recordName = getValidName(recordName + "_RequestBody", true);
//        if (objectSchema.getProperties() == null || objectSchema.getProperties().isEmpty()) {
//            return EMPTY_RECORD;
//        }
//        TypeDefinitionNode record =
//                ballerinaSchemaGenerator.getTypeDefinitionNode(objectSchema, recordName, new ArrayList<>());
//        GeneratorUtils.updateTypeDefNodeList(recordName, record, typeDefinitionNodeList);
//        return recordName;
//    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
