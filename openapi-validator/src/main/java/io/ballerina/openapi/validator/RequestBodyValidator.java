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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
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

        Node body = method.getValue().getBody();
        boolean isBodyExist = false;
        if (oasOperation.getRequestBody() != null && body != null) {
            // This flag is to trac the availability of requestBody has documented
            isBodyExist = true;
            if (body instanceof RequiredParameterNode) {
                boolean isMediaTypeExist = false;
                //Ballerina support type payload string|json|map<json>|xml|byte[]||record {| anydata...; |}[]
                RequiredParameterNode requestBodyNode = (RequiredParameterNode) body;
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
                            }
                        }
                        if (mediaType != null && !isMediaTypeExist) {
                            updateContext(context, CompilationError.TYPEMISMATCH_REQUEST_BODY_PAYLOAD,
                                    body.location(), mediaType, oasMediaTypes.toString(),
                                    requestBodyNode.paramName().get().toString().trim(), method.getKey(),
                                    getNormalizedPath(method.getValue().getPath()));
                        }
                    }
                } else {
                    //Todo: multiple media-types handle
                }
            }
        }
        //TODO: http:Request type
        if (!isBodyExist) {
            updateContext(context, CompilationError.UNDOCUMENTED_REQUEST_BODY, body.location(), method.getKey(),
                    getNormalizedPath(method.getValue().getPath()));
        }
    }


}
