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
import io.ballerina.openapi.validator.model.MetaData;
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
import static io.ballerina.openapi.validator.Constants.SQUARE_BRACKETS;
import static io.ballerina.openapi.validator.ValidatorUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;

/**
 * This RequestBodyValidator class includes all the code related to resource request body validate.
 *
 * @since 1.1.0
 */
public class RequestBodyValidator extends AbstractMetaData implements SectionValidator, Validator {
    private final RequestBody oasRequestBody;
    private final RequiredParameterNode body;

    public RequestBodyValidator(MetaData metaData, RequestBody oasRequestBody, RequiredParameterNode body) {
        super(metaData.getContext(), metaData.getOpenAPI(), metaData.getPath(), metaData.getMethod(),
                metaData.getSeverity(), metaData.getLocation());
        this.body = body;
        this.oasRequestBody = oasRequestBody;
    }

    @Override
    public void validate() {
        //Validate ballerina resource request body
        if (body != null) {
            validateBallerina();
        }
        //Validate OAS request body
        if (oasRequestBody != null) {
            validateOpenAPI();
        }
    }

    /**
     * Validate resource payload against to OAS operation request body.
     */
    @Override
    public void validateBallerina() {

        boolean isBodyExist = false;
        if (oasRequestBody != null && body != null) {
            // This flag is to trac the availability of requestBody has documented
            isBodyExist = true;
            boolean isMediaTypeExist = false;
            location = body.location();
            //Ballerina support type payload string|json|map<json>|xml|byte[]||record {| anydata...; |}[]
            // Get the payload type
            Node typeNode = body.typeName();
            SyntaxKind kind = typeNode.kind();
            String mediaType = ValidatorUtils.getMediaType(kind);

            List<String> mediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE,
                    body.annotations(), context.semanticModel());

            Content content = oasRequestBody.getContent();
            // Traverse request body  list , when it has multiple types
            // first check given media type is there,if not return diagnostic.if it is there check the payload type.
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
                                //TODO: && !(schema instanceof ComposedSchema)) for oneOf, allOf
                                return;
                            }
                            Optional<String> oasName = Optional.empty();
                            if (schema.get$ref() != null) {
                                oasName = extractReferenceType(schema.get$ref());
                            }
                            if (body.paramName().isEmpty() && oasName.isEmpty()) {
                                return;
                            }
                            Optional<Symbol> symbol = context.semanticModel().symbol(body);
                            if (symbol.isEmpty()) {
                                return;
                            }
                            String balRecordName = body.typeName().toString().trim();
                            TypeSymbol typeSymbol = ((ParameterSymbol) symbol.get()).typeDescriptor();
                            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                                validateRecordTypePayload(mediaType, schema,
                                        oasName.orElse(null), balRecordName, typeSymbol);
                                //TODO: inline record
                            } else if (typeSymbol instanceof ArrayTypeSymbol) {
                                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) typeSymbol;
                                validateArrayTypePayload(body, mediaType, oasMediaTypes, schema,
                                        arrayType);
                                return;
                            } else {
                                // TODO validate schema, int, boolean, map if any real world scenario. most of the case
                                //  come with schema
                                return;
                            }
                        }
                    }
                    if (!isMediaTypeExist) {
                        reportDiagnostic(context, CompilationError.UNDEFINED_REQUEST_MEDIA_TYPE,
                                body.location(), severity, mediaType, method, getNormalizedPath(path));
                    }
                }
            } else {
                //TODO: multiple media-types handle - Union type xml|json|Pet
            }
        }
        //TODO: http:Request type
        if (!isBodyExist) {
            reportDiagnostic(context, CompilationError.UNDEFINED_REQUEST_BODY, body.location(),
                    severity, method, getNormalizedPath(path));
        }
    }

    /**
     * This method is to validate array type request payload.
     */
    private void validateArrayTypePayload(RequiredParameterNode requestBodyNode,
                              String mediaType, List<String> oasMediaTypes, Schema<?> schema,
                              ArrayTypeSymbol arrayType) {

        String balRecordName;
        if (arrayType.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
            balRecordName = requestBodyNode.typeName().toString().trim().replaceAll("\\[", "")
                    .replaceAll("\\]", "");

            if (!(schema instanceof ArraySchema)) {
                reportDiagnostic(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                        requestBodyNode.location(),
                        severity, mediaType, oasMediaTypes.toString(),
                        requestBodyNode.paramName().get().toString().trim(), method, getNormalizedPath(path));
            } else {
                ArraySchema arraySchema = (ArraySchema) schema;
                if (arraySchema.getItems().get$ref() != null) {
                    String oasSchemaName = extractReferenceType(arraySchema.getItems().get$ref()).get();
                    schema = openAPI.getComponents().getSchemas().get(oasSchemaName);
                    // validate array record
                    TypeValidatorUtils.validateRecordType(schema, arrayType.memberTypeDescriptor(),
                            balRecordName, context, openAPI, oasSchemaName, severity);
                } else {
                    //TODO inline object schema
                }
            }
        } else {
            // TODO validate schema int[], boolean[] if any real world scenario. most of the
            //  case come with object schema
        }
    }

    /**
     * This method is to validate record type request payload.
     */
    private void validateRecordTypePayload(String mediaType, Schema<?> schema, String oasName, String balRecordName,
                           TypeSymbol typeSymbol) {

        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            Optional<String> itemType = extractReferenceType(arraySchema.getItems().get$ref());
            String item = "array";
            if (itemType.isPresent()) {
                item = itemType.get() + SQUARE_BRACKETS;
            }
            reportDiagnostic(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                    location, severity, mediaType, item, balRecordName, method,
                    getNormalizedPath(path));
            
        } else if (schema instanceof ObjectSchema || schema.get$ref() != null) {
            // validate- Record
            if (balRecordName.equals(oasName)) {
                schema = openAPI.getComponents().getSchemas().get(oasName);
                TypeValidatorUtils.validateRecordType(schema, typeSymbol, balRecordName,
                        context, openAPI, oasName, severity);
            } else {
                reportDiagnostic(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                        location, severity, mediaType, oasName,
                        balRecordName, method, getNormalizedPath(path));
            }
        }
    }

    /**
     * Validate OpenAPI request body against to ballerina payload.
     */
    @Override
    public void validateOpenAPI() {
        Content content = oasRequestBody.getContent();
        if (body == null) {
            reportDiagnostic(context, CompilationError.MISSING_REQUEST_BODY, location, severity, method, path);
            return;
        }
        if (content != null) {
            List<String> missingPayload = new ArrayList<>();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                SyntaxKind kind = body.typeName().kind();
                String mediaType = getMediaType(kind);
                if (mediaTypeEntry.getKey().equals(mediaType)) {
                    MediaType value = mediaTypeEntry.getValue();
                    Schema<?> schema = value.getSchema();

                    String balPayloadType = body.paramName().get().toString();
                    Optional<Symbol> symbol = context.semanticModel().symbol(body);
                    if (symbol.isEmpty()) {
                        return;
                    }
                    TypeSymbol typeSymbol = ((ParameterSymbol) symbol.get()).typeDescriptor();
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
                            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                                balPayloadType = body.typeName().toString().trim();
                            }
                            if (payloadSchema instanceof ObjectSchema) {
                                TypeValidatorUtils.validateObjectSchema((ObjectSchema) payloadSchema, typeSymbol,
                                        context, balPayloadType, location, severity);
                            }
                        }
                    } else if (schema instanceof ComposedSchema) {
                        //TODO: oneOf, allOf schema concept
                        return;
                    } else if (schema instanceof ArraySchema) {
                        ArraySchema arraySchema = (ArraySchema) schema;
                        validateArraySchema(typeSymbol, arraySchema);
                        return;
                    }
                } else {
                    //extra request body media type in OAS
                    missingPayload.add(mediaTypeEntry.getKey());
                }
            }
            if (content.entrySet().size() != 1) {
                reportDiagnostic(context, CompilationError.MISSING_REQUEST_MEDIA_TYPE, body.location(), severity,
                        missingPayload.toString(), method, path);
            }
        }
    }

    /**
     * Validate array type request body schema.
     */
    private void validateArraySchema(TypeSymbol typeSymbol, ArraySchema arraySchema) {
        Schema<?> schema;
        if (arraySchema.getItems().get$ref() != null) {
            String oasSchemaName = extractReferenceType(arraySchema.getItems().get$ref()).get();
            schema = openAPI.getComponents().getSchemas().get(oasSchemaName);
            if (typeSymbol instanceof ArrayTypeSymbol) {
                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) typeSymbol;
                if (arrayType.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol
                        && schema instanceof ObjectSchema) {
                    String balPayloadType = body.typeName().toString().trim()
                            .replaceAll("\\[", "")
                            .replaceAll("]", "");
                    TypeValidatorUtils.validateObjectSchema((ObjectSchema) schema,
                            arrayType.memberTypeDescriptor(),
                            context, balPayloadType, location, severity);
                }
            }
            // else given ballerina typeSymbol is not array type it will cover at the ballerina type
            // validation under the type mismatch.
        }
    }
}
