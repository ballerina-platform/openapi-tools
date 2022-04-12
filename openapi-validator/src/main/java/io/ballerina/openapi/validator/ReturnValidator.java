/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.HTTP_CODES;
import static io.ballerina.openapi.validator.Constants.HTTP_RESPONSE;
import static io.ballerina.openapi.validator.Constants.WILD_CARD_CONTENT_KEY;
import static io.ballerina.openapi.validator.Constants.WILD_CARD_SUMMARY;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

public class ReturnValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private String method;
    private String path;

    public ReturnValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String method, String path) {
        this.context = context;
        this.openAPI = openAPI;
        this.method = method;
        this.path = path;
    }

    public void validateReturnBallerinaToOas(ReturnTypeDescriptorNode returnNode, ApiResponses responses) {

        Map<String, Schema> schemas = new HashMap<>();
        Optional<ApiResponses> ballerinaAPIResponse = getAPIResponses(returnNode.type(), schemas);
        boolean isReturnExist = false;

        if (ballerinaAPIResponse.isEmpty()) {
            return;
        }
        ballerinaAPIResponse.get().forEach((key, value) -> {
            if (!responses.containsKey(key)) {
                // Undocumented status code
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(), key,
                        method, path);

            } else {

            }
        });
    }

    /**
     * This method for handle the return type has non annotations.
     *
     * @return {@link io.swagger.v3.oas.models.responses.ApiResponses} for operation.
     */
    private Optional<ApiResponses> getAPIResponses(Node typeNode, Map<String, Schema> schemas) {
        ApiResponse apiResponse = new ApiResponse();
        MediaType mediaType = new MediaType();
        String mediaTypeString;
        switch (typeNode.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                return  handleQualifiedNameType(qNode);
//            case INT_TYPE_DESC:
//            case STRING_TYPE_DESC:
//            case BOOLEAN_TYPE_DESC:
//                String type =  typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
//                Schema schema = ConverterCommonUtils.getOpenApiSchema(type);
//                Optional<String> media = convertBallerinaMIMEToOASMIMETypes(type, customMediaPrefix);
//                if (media.isPresent()) {
//                    mediaType.setSchema(schema);
//                    apiResponse.description(HTTP_200_DESCRIPTION);
//                    apiResponse.content(new Content().addMediaType(media.get(), mediaType));
//                    apiResponses.put(HTTP_200, apiResponse);
//                    return Optional.of(apiResponses);
//                } else {
//                    return Optional.empty();
//                }
//            case JSON_TYPE_DESC:
//                apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
//                mediaType.setSchema(new ObjectSchema());
//                mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix.get() +
//                        JSON_POSTFIX : MediaType.APPLICATION_JSON;
//                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
//                apiResponse.description(HTTP_200_DESCRIPTION);
//                apiResponses.put(HTTP_200, apiResponse);
//                return Optional.of(apiResponses);
//            case XML_TYPE_DESC:
//                apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
//                mediaType.setSchema(new ObjectSchema());
//                mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix.get() +
//                        XML_POSTFIX  : MediaType.APPLICATION_XML;
//                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
//                apiResponse.description(HTTP_200_DESCRIPTION);
//                apiResponses.put(HTTP_200, apiResponse);
//                return Optional.of(apiResponses);
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode recordNode  = (SimpleNameReferenceNode) typeNode;
//                handleReferenceResponse(recordNode, schemas, new ApiResponses());
                return Optional.of(handleReferenceResponse(recordNode, schemas, new ApiResponses()));
//            case UNION_TYPE_DESC:
//                return mapUnionReturns(operationAdaptor, apiResponses,
//                        (UnionTypeDescriptorNode) typeNode, customMediaPrefix, headers);
//            case RECORD_TYPE_DESC:
//                return mapInlineRecordInReturn(operationAdaptor, apiResponses,
//                        (RecordTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
//            case ARRAY_TYPE_DESC:
//                return getApiResponsesForArrayTypes(operationAdaptor, apiResponses,
//                        (ArrayTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
//            case ERROR_TYPE_DESC:
//                // Return type is given as error or error? in the ballerina it will generate 500 response.
//                apiResponse.description("Found unexpected output");
//                mediaType.setSchema(new StringSchema());
//                apiResponse.content(new Content().addMediaType(MediaType.TEXT_PLAIN, mediaType));
//                apiResponses.put("500", apiResponse);
//                return Optional.of(apiResponses);
//            case OPTIONAL_TYPE_DESC:
//                return getAPIResponses(operationAdaptor, apiResponses,
//                        ((OptionalTypeDescriptorNode) typeNode).typeDescriptor(), customMediaPrefix, headers);
            default:
//                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_101;
//                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage, this.location,
//                        typeNode.kind().toString());
//                errors.add(error);
                return Optional.empty();
        }
    }

    /**
     * This function is for handling {@code QualifiedNameReferenceNode} type returns.
     */
    private Optional<ApiResponses> handleQualifiedNameType(QualifiedNameReferenceNode qNode) {
        ApiResponse apiResponse = new ApiResponse();
        ApiResponses apiResponses = new ApiResponses();
        if (qNode.modulePrefix().text().equals(HTTP)) {
            String typeName = qNode.modulePrefix().text() + ":" + qNode.identifier().text();
            if (typeName.equals(HTTP_RESPONSE)) {
                apiResponse.description("Any Response");
                Content content = new Content();
                content.put(WILD_CARD_CONTENT_KEY, new MediaType().example(new Example()
                        .summary(WILD_CARD_SUMMARY)));
                apiResponse.setContent(content);
                apiResponses.put(Constants.DEFAULT, apiResponse);
                return Optional.of(apiResponses);
            } else {
                Optional<String> code = generateApiResponseCode(qNode.identifier().toString().trim());
                if (code.isPresent()) {
                    apiResponse.description(qNode.identifier().toString().trim());
                    apiResponses.put(code.get(), apiResponse);
                    return Optional.of(apiResponses);
                } else {
                    return Optional.empty();
                }
            }
        } else {
//            Symbol symbol = semanticModel.symbol(qNode).get();
//            if (symbol instanceof TypeReferenceTypeSymbol) {
//                TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol;
//                TypeSymbol typeSymbol = typeRef.typeDescriptor();
//                if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
//                    RecordTypeSymbol recordType = (RecordTypeSymbol) typeSymbol;
//                    ApiResponses responses = handleRecordTypeSymbol(qNode.identifier().text().trim(),
//                            components.getSchemas(), customMediaPrefix, typeRef,
//                            new OpenAPIComponentMapper(components), recordType, headers);
//                    apiResponses.putAll(responses);
//                    return Optional.of(apiResponses);
//                }
//            }
        }
        return Optional.empty();
    }

    /**
     * Get related http status code.
     */
    private Optional<String> generateApiResponseCode(String identifier) {
        if (HTTP_CODES.containsKey(identifier)) {
            return Optional.of(HTTP_CODES.get(identifier));
        } else {
            return Optional.empty();
        }
    }

    private Optional<ApiResponses> handleReferenceResponse(SimpleNameReferenceNode referenceNode,
                                         Map<String, Schema> schema, ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        Optional<Symbol> symbol = context.semanticModel().symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handle record for components
        String mediaTypeString;
        // Check typeInclusion is related to the http status code
        if (referenceNode.parent().kind().equals(ARRAY_TYPE_DESC)) {
//            io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
//            ArraySchema arraySchema = new ArraySchema();
//            arraySchema.setItems(new Schema().$ref(Con.unescapeIdentifier(
//                    referenceNode.name().toString().trim())));
//            media.setSchema(arraySchema);
//            mediaTypeString = MediaType.APPLICATION_JSON;
//            apiResponse.content(new Content().addMediaType(mediaTypeString, media));
//            apiResponses.put(HTTP_200, apiResponse);

        } else if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            if (typeReferenceTypeSymbol.typeDescriptor().typeKind() == TypeDescKind.RECORD) {
                RecordTypeSymbol returnRecord = (RecordTypeSymbol) typeReferenceTypeSymbol.typeDescriptor();
                String referenceName = referenceNode.name().toString().trim();
                ApiResponses responses = handleRecordTypeSymbol(referenceName, schema, customMediaPrefix,
                        typeSymbol, componentMapper, returnRecord, headers);
                apiResponses.putAll(responses);
            }
        }
        //Check content and status code if it is in 200 range then add the header
        operationAdaptor.getOperation().setResponses(apiResponses);
    }

}
