package io.ballerina.openapi.validator;

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
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
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
import static io.ballerina.openapi.validator.ValidatorUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

public class RequestBodyValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;

    public RequestBodyValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI) {
        this.context = context;
        this.openAPI = openAPI;
    }

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
            String mediaType = null;
            mediaType = ValidatorUtils.getMediaType(kind);

            List<String> mediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, "mediaType",
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
                        if (mediaType != null && Objects.equals(oasMedia.getKey(), mediaType)) {
                            isMediaTypeExist = true;
                            if (oasMedia.getValue().getSchema() == null) {
                                return;
                            }
                            Schema<?> schema = oasMedia.getValue().getSchema();
                            if (schema == null || schema.get$ref() == null && !(schema instanceof ObjectSchema)) {
                                // type issue
                                //TODO: && !(schema instanceof ComposedSchema)) for oneOf, allOf
                                return;
                            }
                            Optional<String> oasName = extractReferenceType(schema.get$ref());
                            if (requestBodyNode.paramName().isEmpty() && oasName.isEmpty()) {
                                return;
                            }
                            Optional<Symbol> symbol = context.semanticModel().symbol(requestBodyNode);
                            if (symbol.isEmpty()) {
                                return;
                            }
                            String balRecordName = requestBodyNode.paramName().get().toString();
                            TypeSymbol typeSymbol = ((ParameterSymbol) symbol.get()).typeDescriptor();
                            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                                balRecordName = requestBodyNode.typeName().toString().trim();
                            }
                            String oasSchemaName =  oasName.get();
                            schema = openAPI.getComponents().getSchemas().get(oasSchemaName);
                            /// validate- Record
                            TypeValidatorUtils.validateRecordType(schema, typeSymbol, balRecordName,
                                    context, openAPI, oasSchemaName);

                            //TODO: array type validation
                        }
                    }
                    if (mediaType != null && !isMediaTypeExist) {
                        updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                requestBodyNode.location(), mediaType, oasMediaTypes.toString(),
                                requestBodyNode.paramName().get().toString().trim(), method.getKey(),
                                getNormalizedPath(method.getValue().getPath()));
                    }
                }
            } else {
                //Todo: multiple media-types handle
            }
        }
        //TODO: http:Request type
        if (!isBodyExist) {
            updateContext(context, CompilationError.UNDOCUMENTED_REQUEST_BODY, requestBodyNode.location(), method.getKey(),
                    getNormalizedPath(method.getValue().getPath()));
        }
    }

    public void validateOASRequestBody(RequiredParameterNode body, RequestBody oasBody, String path,
                                             String method, Location location) {

        Content content = oasBody.getContent();
        if (body == null) {
            updateContext(context, CompilationError.UNIMPLEMENTED_REQUEST_BODY, location, method, path);
            return;
        }
        if (content != null) {
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                SyntaxKind kind = body.typeName().kind();
                String mediaType = getMediaType(kind);
                if (mediaTypeEntry.getKey().equals(mediaType)) {
                    // manage the payload type
                    MediaType value = mediaTypeEntry.getValue();
                    Schema schema = value.getSchema();
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
                            Schema payloadSchema = schemas.get(schemaName.get());
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
                                         context, openAPI, schemaName.get(), balRecordName);
                            } else if (payloadSchema instanceof ComposedSchema) {
                                //TODO: oneOf, AllOF handle
                                return;
                            } else {
                                //TODO: array schema
                                return;
                            }
                        }
                    }
                } else {
                    //unimplemented
                    updateContext(context, CompilationError.UNIMPLEMENTED_MEDIA_TYPE, body.location(),
                            mediaTypeEntry.getKey(), method, path);
                }
            }
        }
    }
}
