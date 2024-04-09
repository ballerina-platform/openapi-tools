package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
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
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class RequestBodyGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    RequestBody requestBody;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    List<ParameterNode> headerParameters = new ArrayList<>();

    public RequestBodyGenerator(RequestBody requestBody, OpenAPI openAPI) {
        this.requestBody = requestBody;
        this.openAPI = openAPI;
    }

    public List<ParameterNode> getHeaderParameters() {
        return headerParameters;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode() {
        Content requestBodyContent;
        String referencedRequestBodyName;
        TypeDescriptorNode typeDescNode = null;
        if (requestBody.get$ref() != null) {
            try {
                referencedRequestBodyName = extractReferenceType(requestBody.get$ref()).trim();
            } catch (BallerinaOpenApiException e) {
                ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_109,
                        requestBody.get$ref());
                diagnostics.add(diagnostic);
                return Optional.empty();
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
            Schema<?> schema = mediaTypeEntry.getValue().getSchema();
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
                    } else if (getOpenAPIType(schema) != null || schema.getProperties() != null) {
                        Optional<TypeDescriptorNode> resultNode = TypeHandler.getInstance().
                                getTypeNodeFromOASSchema(schema);

                        if (resultNode.isEmpty()) {
                            if (schema instanceof ArraySchema) {
                                paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true) + "[]";
                                typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                            } else {
                                paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                                typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                            }
                        } else {
                            typeDescNode = resultNode.get();
                            if (schema instanceof ArraySchema && typeDescNode.toSourceCode().contains("anydata")) {
                                paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true) + "[]";
                                typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                            }
                        }

                    } else {
                        paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                        typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                    }
                }

                //handle headers
                if (mediaTypeEntryKey.equals(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA) ||
                        mediaTypeEntryKey.matches("multipart/.*\\+form-data")) {
                    extractHeaders(mediaTypeEntry);
                }
            } else {
                if (mediaTypeEntry.getKey().equals(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA)
                        && mediaTypeEntry.getValue().getEncoding() != null) {
                    extractHeaders(mediaTypeEntry);
                } else {
                    paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true);
                    typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                }
            }
            break;
        }
        if (typeDescNode != null) {
            String reqBody = typeDescNode.toSourceCode().equals(HTTP_REQUEST) ? "request" : "payload";
            return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeDescNode,
                    createIdentifierToken(reqBody)));
        } else {
            return Optional.empty();
        }
    }

    private void extractHeaders(Map.Entry<String, MediaType> mediaTypeEntry) {
        Map<String, Encoding> encodingHeaders = mediaTypeEntry.getValue().getEncoding();
        if (encodingHeaders == null) {
            return;
        }
        List<String> headerList = new ArrayList<>();
        for (Map.Entry<String, Encoding> entry : encodingHeaders.entrySet()) {
            Map<String, Header> headers = entry.getValue().getHeaders();
            if (headers != null) {
                for (Map.Entry<String, Header> header : headers.entrySet()) {
                    if (!headerList.contains(getValidName(header.getKey(), false))) {
                        RequestBodyHeaderParameter requestBodyHeaderParameter = new RequestBodyHeaderParameter(header);
                        Optional<ParameterNode> parameterNode = requestBodyHeaderParameter.generateParameterNode();

                        parameterNode.ifPresent(node -> headerParameters.add(node));
                        diagnostics.addAll(requestBodyHeaderParameter.getDiagnostics());
                        headerList.add(getValidName(header.getKey(), false));
                    }
                }
            }
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
