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

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNION_TYPE_DESC;
import static io.ballerina.openapi.validator.Constants.APPLICATION_JSON;
import static io.ballerina.openapi.validator.Constants.BODY;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.HTTP_200;
import static io.ballerina.openapi.validator.Constants.HTTP_500;
import static io.ballerina.openapi.validator.Constants.HTTP_CODES;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This class for validate the return and response types.
 *
 * @since 2201.1.0
 */
public class ReturnValidator {
    private final SyntaxNodeAnalysisContext context;
    private final OpenAPI openAPI;
    private final String method;
    private final String path;

    private final Map<String, Node> balStatusCodes = new HashMap<>();
    private final List<String> balMediaTypes = new ArrayList<>();

    private final Location location;
    private final DiagnosticSeverity severity;

    public ReturnValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String method, String path,
                           Location location, DiagnosticSeverity severity) {
        this.context = context;
        this.openAPI = openAPI;
        this.method = method;
        this.path = path;
        this.location = location;
        this.severity = severity;
    }

    public void addBalStatusCodes (String statusCode, Node node) {
        balStatusCodes.put(statusCode, node);
    }

    public void addAllBalStatusCodes (Map<String, Node> statusCodes) {
        balStatusCodes.putAll(statusCodes);
    }

    public Map<String, Node> getBalStatusCodes() {
        return this.balStatusCodes;
    }

    public void addAllBalMediaTypes(List<String> balMediaType) {
        balMediaTypes.addAll(balMediaType);
    }

    public List<String> getBalMediaTypes() {
        return this.balMediaTypes;
    }

    /**
     * This util function for validate the ballerina return against to openapi response. Here we string ballerina
     * status code and , Mediatypes that user has used for the ballerina return.
     *
     * @param returnNode Ballerina Return Node from function signature.
     * @param responses OAS operation response.
     */
    public void validateReturnBallerinaToOas(TypeDescriptorNode returnNode, ApiResponses responses) {
        SyntaxKind kind = returnNode.kind();
        if (returnNode instanceof QualifiedNameReferenceNode) {
            QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) returnNode;
            validateQualifiedType(qNode, responses);
            //TODO: handle module level qualified name ex: `res:ResponseRecord`
        } else if (returnNode instanceof UnionTypeDescriptorNode) {
            // A|B|C|D
            UnionTypeDescriptorNode uNode = (UnionTypeDescriptorNode) returnNode;
            validateUnionType(uNode, responses);
        } else if (returnNode instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) returnNode;
            validateSimpleNameReference(simpleRefNode, responses);
        } else if (kind == ERROR_TYPE_DESC) {
            balStatusCodes.put(HTTP_500, null);
            if (!responses.containsKey(HTTP_500)) {
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(), severity,
                        HTTP_500,
                        method, path);
            }
        } else if (returnNode instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode optionalNode = (OptionalTypeDescriptorNode) returnNode;
            validateReturnBallerinaToOas((TypeDescriptorNode) optionalNode.typeDescriptor(), responses);
        } else if (returnNode instanceof BuiltinSimpleNameReferenceNode) {
            if (!responses.containsKey(HTTP_200)) {
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE,
                        returnNode.location(), severity, HTTP_200, method, path);
            } else {
                balStatusCodes.put(HTTP_200, (BuiltinSimpleNameReferenceNode) returnNode);
                String mediaType = getMediaType(kind);
                ApiResponse apiResponse = responses.get(HTTP_200);
                Content content = apiResponse.getContent();
                balMediaTypes.add(mediaType);
                if (content == null || !content.containsKey(mediaType)) {
                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_MEDIA_TYPE,
                            returnNode.location(), severity, mediaType, method, path);
                }
            }
        }
        //TODO: array type validation
    }

    /**
     * Validate Ballerina record against to openapi response.
     * ex: resource function  get pet() returns Pet {}
     *
     */
    private void validateSimpleNameReference(SimpleNameReferenceNode simpleRefNode, ApiResponses responses) {
        Optional<Symbol> symbol = context.semanticModel().symbol(simpleRefNode);
        //Access the status code and media type via record
        if (symbol.isEmpty()) {
            return;
        }
        if (symbol.get() instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol refType = (TypeReferenceTypeSymbol) symbol.get();
            TypeSymbol type = refType.typeDescriptor();
            if (type instanceof RecordTypeSymbol) {
                RecordTypeSymbol typeSymbol = (RecordTypeSymbol) type;
                List<TypeSymbol> typeInclusions = typeSymbol.typeInclusions();
                boolean isHttp = false;
                if (!typeInclusions.isEmpty()) {
                    for (TypeSymbol typeInSymbol : typeInclusions) {
                        if (HTTP.equals(typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                            isHttp = true;
                            Optional<String> code =
                                    generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                            if (code.isEmpty()) {
                                return;
                            }
                            balStatusCodes.put(code.get(), simpleRefNode);
                            if (!responses.containsKey(code.get())) {
                                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE,
                                        simpleRefNode.location(), severity, code.get(), method, path);
                            } else {
                                // handle media types
                                ApiResponse apiResponse = responses.get(code.get());
                                Map<String, RecordFieldSymbol> fields = typeSymbol.fieldDescriptors();
                                TypeSymbol bodyFieldType = fields.get(BODY).typeDescriptor();
                                TypeDescKind bodyKind = bodyFieldType.typeKind();
                                String mediaType = getMediaType(bodyKind);
                                balMediaTypes.add(mediaType);
                                Content content = apiResponse.getContent();
                                if (content != null) {
                                    MediaType oasMtype = content.get(mediaType);
                                    if (!content.containsKey(mediaType)) {
                                        // not media type equal
                                        updateContext(context, CompilationError.UNDOCUMENTED_RETURN_MEDIA_TYPE,
                                                simpleRefNode.location(), severity, mediaType, method, path);
                                        return;
                                    }
                                    if (oasMtype.getSchema() != null && oasMtype.getSchema().get$ref() != null &&
                                            bodyKind == TypeDescKind.TYPE_REFERENCE) {

                                        //TODO inline record both ballerina and schema
                                        //TODO array type
                                        Optional<String> schemaName =
                                                extractReferenceType(oasMtype.getSchema().get$ref());
                                        if (schemaName.isEmpty()) {
                                            return;
                                        }
                                        TypeValidatorUtils.validateRecordType(
                                                openAPI.getComponents().getSchemas().get(schemaName.get()),
                                                bodyFieldType,
                                                ((TypeReferenceTypeSymbol) bodyFieldType).definition().getName().get(),
                                                context, openAPI, schemaName.get(), severity);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isHttp) {
                    // validate normal Record status code 200 and application json
                    if (!responses.containsKey(HTTP_200)) {
                        updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, simpleRefNode.location(),
                                severity, HTTP_200, method, path);
                    } else {
                        // record validation
                        ApiResponse apiResponse = responses.get(HTTP_200);
                        Content content = apiResponse.getContent();
                        MediaType oasMtype = content.get(APPLICATION_JSON);
                        balStatusCodes.put(HTTP_200, simpleRefNode);
                        balMediaTypes.add(APPLICATION_JSON);

                        if (oasMtype.getSchema() != null && oasMtype.getSchema().get$ref() != null) {
                            Optional<String> schemaName = extractReferenceType(oasMtype.getSchema().get$ref());
                            if (schemaName.isEmpty()) {
                                return;
                            }
                            TypeValidatorUtils.validateRecordType(
                                    openAPI.getComponents().getSchemas().get(schemaName.get()),
                                    typeSymbol, refType.definition().getName().orElse(null),
                                    context, openAPI, schemaName.orElse(null), severity);
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate union type return.
     * ex: Http:Ok | Pet | Http:Accepted
     */
    private void validateUnionType(UnionTypeDescriptorNode uNode, ApiResponses responses) {
        List<Node> unionNodes = new ArrayList<>();
        while (uNode != null) {
            unionNodes.add(uNode.rightTypeDesc());
            if (uNode.leftTypeDesc().kind() == UNION_TYPE_DESC) {
                uNode = (UnionTypeDescriptorNode) uNode.leftTypeDesc();
            } else {
                unionNodes.add(uNode.leftTypeDesc());
                uNode = null;
            }
        }
        for (Node reNode : unionNodes) {
            if (reNode.kind() == QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode traversRNode = (QualifiedNameReferenceNode) reNode;
                validateQualifiedType(traversRNode, responses);
            } else if (reNode.kind() == SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) reNode;
                validateSimpleNameReference(simpleRefNode, responses);
            } else if (reNode.kind() == ERROR_TYPE_DESC) {
                balStatusCodes.put(HTTP_500, null);
                if (!responses.containsKey(HTTP_500)) {
                    assert false;
                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, uNode.location(), severity,
                            HTTP_500, method, path);
                }
            }  else if (reNode instanceof BuiltinSimpleNameReferenceNode) {
                if (!responses.containsKey(HTTP_200)) {
                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE,
                            reNode.location(), severity, HTTP_200, method, path);
                } else {
                    balStatusCodes.put(HTTP_200, (BuiltinSimpleNameReferenceNode) reNode);
                    String mediaType = getMediaType(reNode.kind());
                    ApiResponse apiResponse = responses.get(HTTP_200);
                    Content content = apiResponse.getContent();
                    balMediaTypes.add(mediaType);
                    if (content == null || !content.containsKey(mediaType)) {
                        updateContext(context, CompilationError.UNDOCUMENTED_RETURN_MEDIA_TYPE,
                                reNode.location(), severity, mediaType, method, path);
                    }
                }
            }
        }
    }

    /**
     * Validate qualifier return type ex: Http:Ok.
     *
     */
    private void validateQualifiedType(QualifiedNameReferenceNode qNode, ApiResponses responses) {
        qNode.identifier();
        // get the status code
        Optional<String> statusCode = generateApiResponseCode(qNode.identifier().toString().trim());
        if (statusCode.isPresent()) {
            String balCode = statusCode.get();
            balStatusCodes.put(balCode, qNode);
            if (!responses.containsKey(balCode)) {
                // Undocumented status code
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, qNode.location(), severity, balCode,
                        method, path);
            }
        }
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

    /**
     * Validate OAS response against to ballerina return type. Here mainly we pick the ballerina status code Map ,
     * and media type earlier we stored under the ballerina to openapi return validation.
     *
     * @param responses api Responses.
     */
    public void validateReturnOASToBallerina(ApiResponses responses) {
        responses.forEach((key, value) -> {
            if (!balStatusCodes.containsKey(key)) {
                // Unimplemented status code in swagger operation and get location via bal return node.
                updateContext(context, CompilationError.UNIMPLEMENTED_STATUS_CODE, location, severity, key, method,
                        path);
                return;
            }
            Content content = value.getContent();
            if (content == null) {
                return;
            }
            content.forEach((mediaType, schema) -> {
                if (balMediaTypes.contains(mediaType)) {
                    Node node = balStatusCodes.get(key);
                    if (node == null) {
                        // This can be null when the return type is not in service, and return type is error.
                        return;
                    }
                    if (schema.getSchema() == null) {
                        return;
                    }
                    ObjectSchema objectSchema = null;
                    if (schema.getSchema().get$ref() != null) {
                        //object schema
                        Optional<String> schemaName = extractReferenceType(schema.getSchema().get$ref());
                        if (schemaName.isEmpty()) {
                            return;
                        }
                        Schema<?> vschema = openAPI.getComponents().getSchemas().get(schemaName.get());
                        if (vschema instanceof ObjectSchema) {
                            objectSchema = (ObjectSchema) vschema;
                        }
                    }
                    if (schema.getSchema() instanceof ObjectSchema) {
                        objectSchema = (ObjectSchema) schema.getSchema();
                    }
                    SyntaxKind kind = node.kind();
                    if (kind == QUALIFIED_NAME_REFERENCE) {
                        //TODO: handle module level qualified name ex: `res:ResponseRecord`
                    } else if (kind == SIMPLE_NAME_REFERENCE) {
                        SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) node;
                        Optional<Symbol> symbol = context.semanticModel().symbol(simpleRefNode);
                        //Access the status code and media type via record
                        if (symbol.isEmpty()) {
                            return;
                        }
                        if (symbol.get() instanceof TypeReferenceTypeSymbol) {
                            TypeReferenceTypeSymbol refType = (TypeReferenceTypeSymbol) symbol.get();
                            TypeSymbol type = refType.typeDescriptor();
                            if (type instanceof RecordTypeSymbol) {
                                RecordTypeSymbol typeSymbol = (RecordTypeSymbol) type;
                                List<TypeSymbol> typeInclusions = typeSymbol.typeInclusions();
                                boolean isHttp = false;
                                if (!typeInclusions.isEmpty()) {
                                    for (TypeSymbol typeInSymbol : typeInclusions) {
                                        if (HTTP.equals(
                                                typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                                            isHttp = true;
                                            Map<String, RecordFieldSymbol> fields = typeSymbol.fieldDescriptors();
                                            TypeSymbol bodyFieldType = fields.get(BODY).typeDescriptor();
                                            //TODO inline record both ballerina and schema
                                            //TODO array type
                                            if (bodyFieldType instanceof  TypeReferenceTypeSymbol) {
                                                TypeValidatorUtils.validateObjectSchema(objectSchema, bodyFieldType,
                                                        context, ((TypeReferenceTypeSymbol) bodyFieldType)
                                                                .definition().getName().orElse("Anonymous Record"),
                                                        location, severity);
                                            }
                                        }
                                    }
                                    if (!isHttp) {
                                        TypeValidatorUtils.validateObjectSchema(objectSchema, typeSymbol,
                                                context, ((TypeReferenceTypeSymbol) typeSymbol).definition().getName().
                                                        orElse("Anonymous Record"), location, severity);
                                    }
                                } else {
                                    TypeValidatorUtils.validateObjectSchema(objectSchema, typeSymbol,
                                            context, refType.definition().getName().
                                                    orElse("Anonymous Record"), location, severity);
                                }

                            }
                        }
                    }
                } else {
                    // Unimplemented media type code in swagger operation and get location via bal return node.
                        updateContext(context, CompilationError.UNIMPLEMENTED_RESPONSE_MEDIA_TYPE,
                                location, severity, mediaType, method, path);
                    //TODO: array type handle
                }
            });
        });
        //TODO: array type validation
    }
}
