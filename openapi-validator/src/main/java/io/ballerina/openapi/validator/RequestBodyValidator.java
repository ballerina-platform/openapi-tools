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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.ResourceMethod;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.validator.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.validator.ValidatorUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This RequestBodyValidator class includes all the code related to resource request body validate.
 *
 * @since 2201.1.0
 */
public class RequestBodyValidator {

    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private DiagnosticSeverity severity;

    public RequestBodyValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, DiagnosticSeverity severity) {

        this.context = context;
        this.openAPI = openAPI;
        this.severity = severity;
    }

    /**
     * Validate resource payload against to OAS operation request body.
     *
     * @param method       Validate ballerina method details
     * @param oasOperation OAS operation
     */
    public void validateBallerinaRequestBody(Map.Entry<String, ResourceMethod> method, Operation oasOperation) {

        RequiredParameterNode requestBodyNode = method.getValue().getBody();
        boolean isBodyExist = false;
        if (oasOperation.getRequestBody() != null && requestBodyNode != null) {
            // This flag is to trac the availability of requestBody has documented
            isBodyExist = true;
            boolean isMediaTypeExist = false;
            //Ballerina support type payload string|json|map<json>|xml|byte[]||record {| anydata...; |}[]
            // Get the payload type
            Node typeNode = requestBodyNode.typeName();
            SyntaxKind kind = typeNode.kind();
            String mediaType = ValidatorUtils.getMediaType(kind);

            List<String> mediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE,
                    requestBodyNode.annotations(), context.semanticModel());

            RequestBody oasRequestBody = oasOperation.getRequestBody();
            Content content = oasRequestBody.getContent();
            // Traverse request body  list , when it has multiple types
            // first check given media type is there
            if (mediaTypes.isEmpty()) {
                if (content != null) {
                    List<String> oasMediaTypes = new ArrayList<>();
                    for (Map.Entry<String, MediaType> oasMedia : content.entrySet()) {
                        oasMediaTypes.add(oasMedia.getKey());
                        if (Objects.equals(oasMedia.getKey(), mediaType)) {
                            isMediaTypeExist = true;
                            if (oasMedia.getValue().getSchema() == null) {
                                return;
                            }
                            Schema<?> schema = oasMedia.getValue().getSchema();
                            if (schema == null || (schema.get$ref() == null && !(schema instanceof ObjectSchema) &&
                                    !(schema instanceof ArraySchema))) {
                                // type issue
                                //TODO: && !(schema instanceof ComposedSchema)) for oneOf, allOf
                                return;
                            }
                            Optional<String> oasName = Optional.empty();
                            if (schema.get$ref() != null) {
                                oasName = extractReferenceType(schema.get$ref());
                            }
                            if (requestBodyNode.paramName().isEmpty() && oasName.isEmpty()) {
                                return;
                            }
                            Optional<Symbol> symbol = context.semanticModel().symbol(requestBodyNode);
                            if (symbol.isEmpty()) {
                                return;
                            }
                            String balRecordName = requestBodyNode.typeName().toString().trim();
                            TypeSymbol typeSymbol = ((ParameterSymbol) symbol.get()).typeDescriptor();
                            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                                if (schema instanceof ArraySchema) {
                                    ArraySchema arraySchema = (ArraySchema) schema;
                                    updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                            requestBodyNode.location(), severity, balRecordName,
                                            extractReferenceType(arraySchema.getItems().get$ref()).get() + "[]",
                                            method.getKey(),
                                            getNormalizedPath(method.getValue().getPath()));
                                } else if (schema instanceof ObjectSchema || schema.get$ref() != null) {
                                    // validate- Record
                                    String oasSchema = oasName.get();
                                    if (balRecordName.equals(oasSchema)) {
                                        schema = openAPI.getComponents().getSchemas().get(oasSchema);
                                        TypeValidatorUtils.validateRecordType(schema, typeSymbol, balRecordName,
                                                context, openAPI, oasSchema, severity);
                                    } else {
                                        updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                                requestBodyNode.location(), severity, mediaType, balRecordName,
                                                oasSchema,
                                                method.getKey(),
                                                getNormalizedPath(method.getValue().getPath()));
                                    }
                                }
                                //TODO: inline record
                            } else if (typeSymbol instanceof ArrayTypeSymbol) {
                                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) typeSymbol;
                                if (arrayType.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                                    balRecordName = requestBodyNode.typeName().toString()
                                            .trim().replaceAll("\\[", "")
                                            .replaceAll("\\]", "");

                                    if (!(schema instanceof ArraySchema)) {
                                        updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                                requestBodyNode.location(), severity, mediaType,
                                                oasMediaTypes.toString(),
                                                requestBodyNode.paramName().get().toString().trim(), method.getKey(),
                                                getNormalizedPath(method.getValue().getPath()));
                                        return;
                                    } else {
                                        ArraySchema arraySchema = (ArraySchema) schema;
                                        if (arraySchema.getItems().get$ref() != null) {
                                            String oasSchemaName =
                                                    extractReferenceType(arraySchema.getItems().get$ref()).get();
                                            schema = openAPI.getComponents().getSchemas().get(oasSchemaName);
                                            /// validate- Record
                                            TypeValidatorUtils.validateRecordType(schema,
                                                    arrayType.memberTypeDescriptor(),
                                                    balRecordName, context, openAPI, oasSchemaName, severity);
                                        } else {
                                            //TODO inline object schema
                                        }
                                    }
                                } else {
                                    // TODO validate schema int[], boolean[] if any real world scenario. most of the
                                    //  case come with schema
                                    return;
                                }

                            } else {
                                // TODO validate schema, int, boolean, map if any real world scenario. most of the case
                                //  come with schema
                                return;
                            }
                        }
                    }
                    if (!isMediaTypeExist) {
                        updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                requestBodyNode.location(), severity, mediaType,
                                oasMediaTypes.toString(), method.getKey(),
                                getNormalizedPath(method.getValue().getPath()));
                    }
                }
            } else {
                //Todo: multiple media-types handle
            }
        }
        //TODO: http:Request type
        if (!isBodyExist) {
            updateContext(context, CompilationError.UNDOCUMENTED_REQUEST_BODY, requestBodyNode.location(), severity,
                    method.getKey(),
                    getNormalizedPath(method.getValue().getPath()));
        }
    }

    /**
     * Validate OAS request body against to ballerina payload.
     */
    public void validateOASRequestBody(RequiredParameterNode body, RequestBody oasBody, String path,
                                       String method, Location location) {
        Content content = oasBody.getContent();
        if (body == null) {
            updateContext(context, CompilationError.UNIMPLEMENTED_REQUEST_BODY, location, severity, method, path);
            return;
        }
        if (content != null) {
            List<String> missingPayload = new ArrayList<>();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                SyntaxKind kind = body.typeName().kind();
                String mediaType = getMediaType(kind);
                if (mediaTypeEntry.getKey().equals(mediaType)) {
                    // manage the payload type
                    MediaType value = mediaTypeEntry.getValue();
                    Schema<?> schema = value.getSchema();
                    if (schema != null && schema.get$ref() != null) {
                        String ref = schema.get$ref();
                        Optional<String> schemaName = extractReferenceType(ref);
                        if (schemaName.isEmpty()) {
                            return;
                        }
                        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
                        // This condition add due to bug in the swagger parser
                        // issue:https://github.com/swagger-api/swagger-parser/issues/1643
                        if (schemas.containsKey(schemaName.get())) {
                            Schema<?> payloadSchema = schemas.get(schemaName.get());
                            String balRecordName = body.paramName().get().toString();
                            Optional<Symbol> symbol = context.semanticModel().symbol(body);
                            if (symbol.isEmpty()) {
                                return;
                            }
                            TypeSymbol typeSymbol = ((ParameterSymbol) symbol.get()).typeDescriptor();
                            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                                balRecordName = body.typeName().toString().trim();
                            }
                            if (payloadSchema instanceof ObjectSchema) {
                                //Get balllerina typeSymbol
                                TypeValidatorUtils.validateObjectSchema((ObjectSchema) payloadSchema, typeSymbol,
                                        context, balRecordName, severity);
                            } else if (payloadSchema instanceof ComposedSchema) {
                                //TODO: oneOf, AllOF handle
                                return;
                            } else if (schema instanceof ArraySchema) {
                                ArraySchema arraySchema = (ArraySchema) schema;
                                if (arraySchema.getItems().get$ref() != null) {
                                    String oasSchemaName =
                                            extractReferenceType(arraySchema.getItems().get$ref()).get();
                                    schema = openAPI.getComponents().getSchemas().get(oasSchemaName);
                                    if (typeSymbol instanceof ArrayTypeSymbol) {
                                        ArrayTypeSymbol arrayType = (ArrayTypeSymbol) typeSymbol;
                                        if (arrayType.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol
                                                && schema instanceof ObjectSchema) {
                                            balRecordName = body.typeName().toString().trim()
                                                    .replaceAll("\\[", "")
                                                    .replaceAll("\\]", "");
                                            TypeValidatorUtils.validateObjectSchema((ObjectSchema) schema,
                                                    arrayType.memberTypeDescriptor(),
                                                    context, balRecordName, severity);
                                        }
                                    }
                                }
                                //TODO inline object schema array
                                return;
                            }
                        }
                    }
                } else {
                    //unimplemented
                    missingPayload.add(mediaTypeEntry.getKey());
                }
            }
            if (content.entrySet().size() != 1) {
                updateContext(context, CompilationError.UNIMPLEMENTED_MEDIA_TYPE, body.location(), severity,
                        missingPayload.toString(), method, path);
            }
        }
    }
}
