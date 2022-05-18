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
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNION_TYPE_DESC;
import static io.ballerina.openapi.validator.Constants.ANONYMOUS_RECORD;
import static io.ballerina.openapi.validator.Constants.APPLICATION_JSON;
import static io.ballerina.openapi.validator.Constants.BODY;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.HTTP_200;
import static io.ballerina.openapi.validator.Constants.HTTP_202;
import static io.ballerina.openapi.validator.Constants.HTTP_500;
import static io.ballerina.openapi.validator.Constants.HTTP_CODES;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;

/**
 * This class for validate the return and response types.
 *
 * @since 1.1.0
 */
public class ReturnTypeValidator extends NodeValidator {
    private final Map<String, Node> balStatusCodes = new HashMap<>();
    private final Map<String, List<String>> balMediaTypes = new HashMap<>();
    private TypeDescriptorNode returnNode;
    private final ApiResponses responses;

    public ReturnTypeValidator(ValidatorContext validatorContext, TypeDescriptorNode returnNode,
                               ApiResponses responses) {
        super(validatorContext);
        this.returnNode = returnNode;
        this.responses = responses;
    }

    /**
     * This util function for validate the ballerina return against to openapi response. Here we string ballerina
     * status code and , Media types that user has used for the ballerina return.
     */
    @Override
    public void validateBallerinaToOpenAPI() {
        boolean isOptional = false;
        if (returnNode == null) {
            balStatusCodes.put(HTTP_202, null);
        } else {
            SyntaxKind kind = returnNode.kind();
            if (returnNode instanceof QualifiedNameReferenceNode) {
                QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) returnNode;
                validateQualifiedType(qNode);
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
            } else if (returnNode instanceof OptionalTypeDescriptorNode) {
                isOptional = true;
                OptionalTypeDescriptorNode optionalNode = (OptionalTypeDescriptorNode) returnNode;
                returnNode = (TypeDescriptorNode) optionalNode.typeDescriptor();
                validateBallerinaToOpenAPI();
            } else if (returnNode instanceof BuiltinSimpleNameReferenceNode) {
                balStatusCodes.put(HTTP_200, returnNode);
                if (responses.containsKey(HTTP_200)) {
                    String mediaType = getMediaType(kind);
                    fillMediaTypes(HTTP_200, mediaType);
                }
            }
        }

        if (!isOptional) {
            //Undefined status code
            Set<String> ballerinaCodes = balStatusCodes.keySet();
            Set<String> oasKeys = responses.keySet();
            List<String> undefineCode = new ArrayList<>(ballerinaCodes);
            undefineCode.removeAll(oasKeys);
            if (undefineCode.size() > 0) {
                reportDiagnostic(validatorContext.getContext(), CompilationError.UNDEFINED_RETURN_CODE,
                        validatorContext.getLocation(), validatorContext.getSeverity(),
                        undefineCode.toString(), validatorContext.getMethod(), validatorContext.getPath());
            }

            //Undefined mediaTypes
            balMediaTypes.forEach((key, value) -> {
                if (responses.containsKey(key)) {
                    ApiResponse apiResponse = responses.get(key);
                    if (apiResponse.getContent() != null) {
                        Set<String> oasMediaTypes = apiResponse.getContent().keySet();
                        List<String> undefinedMediaTypes = new ArrayList<>(value);
                        undefinedMediaTypes.removeAll(oasMediaTypes);
                        if (undefinedMediaTypes.size() > 0) {
                            reportDiagnostic(validatorContext, CompilationError.UNDEFINED_RETURN_MEDIA_TYPE,
                                    undefinedMediaTypes.toString(), key, validatorContext.getMethod(),
                                    validatorContext.getPath());
                        }
                    } else {
                        reportDiagnostic(validatorContext, CompilationError.UNDEFINED_RETURN_MEDIA_TYPE,
                                value.toString(), key, validatorContext.getMethod(), validatorContext.getPath());
                    }
                }
            });
        }
        //TODO: array type validation
    }

    /**
     * Validate Ballerina record against to openapi response.
     * ex: resource function  get pet() returns Pet {}
     */
    private void validateSimpleNameReference(SimpleNameReferenceNode simpleRefNode, ApiResponses responses) {
        Optional<Symbol> symbol = validatorContext.getContext().semanticModel().symbol(simpleRefNode);
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
                Map<String, Schema> oasSchemas = validatorContext.getOpenAPI().getComponents().getSchemas();
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
                            if (responses.containsKey(code.get())) {
                                // handle media types
                                ApiResponse apiResponse = responses.get(code.get());
                                Map<String, RecordFieldSymbol> fields = typeSymbol.fieldDescriptors();
                                TypeSymbol bodyFieldType = fields.get(BODY).typeDescriptor();
                                TypeDescKind bodyKind = bodyFieldType.typeKind();
                                String mediaType = getMediaType(bodyKind);
                                fillMediaTypes(code.get(), mediaType);
                                Content content = apiResponse.getContent();
                                if (content != null) {
                                    MediaType oasMtype = content.get(mediaType);
                                    if (!content.containsKey(mediaType)) {
                                        // not media type equal
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
                                                oasSchemas.get(
                                                        schemaName.get()), bodyFieldType,
                                                ((TypeReferenceTypeSymbol) bodyFieldType).definition().getName().get(),
                                                validatorContext.getContext(), validatorContext.getOpenAPI(),
                                                schemaName.get(), validatorContext.getSeverity());
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isHttp) {
                    // validate normal Record status code 200 and application json
                    if (responses.containsKey(HTTP_200)) {
                        // record validation
                        ApiResponse apiResponse = responses.get(HTTP_200);
                        Content content = apiResponse.getContent();
                        MediaType oasMType = content.get(APPLICATION_JSON);
                        balStatusCodes.put(HTTP_200, simpleRefNode);
                        fillMediaTypes(HTTP_200, APPLICATION_JSON);
                        if (oasMType.getSchema() != null && oasMType.getSchema().get$ref() != null) {
                            Optional<String> schemaName = extractReferenceType(oasMType.getSchema().get$ref());
                            if (schemaName.isEmpty()) {
                                return;
                            }
                            TypeValidatorUtils.validateRecordType(
                                    oasSchemas.get(schemaName.get()),
                                    typeSymbol, refType.definition().getName().orElse(null),
                                    validatorContext.getContext(), validatorContext.getOpenAPI(),
                                    schemaName.orElse(null), validatorContext.getSeverity());
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
                validateQualifiedType(traversRNode);
            } else if (reNode.kind() == SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) reNode;
                validateSimpleNameReference(simpleRefNode, responses);
            } else if (reNode.kind() == ERROR_TYPE_DESC) {
                balStatusCodes.put(HTTP_500, null);
            } else if (reNode instanceof BuiltinSimpleNameReferenceNode) {
                balStatusCodes.put(HTTP_200, reNode);
                if (responses.containsKey(HTTP_200)) {
                    String mediaType = getMediaType(reNode.kind());
                    fillMediaTypes(HTTP_200, mediaType);
                }
            }
        }
    }

    private void fillMediaTypes(String code, String mediaType) {
        List<String> mediaTypes;
        if (balMediaTypes.containsKey(code)) {
            mediaTypes = balMediaTypes.get(code);
        } else {
            mediaTypes = new ArrayList<>();
        }
        mediaTypes.add(mediaType);
        balMediaTypes.put(code, mediaTypes);
    }

    /**
     * Validate qualifier return type ex: Http:Ok.
     */
    private void validateQualifiedType(QualifiedNameReferenceNode qNode) {
        // get the status code
        Optional<String> statusCode = generateApiResponseCode(qNode.identifier().toString().trim());
        if (statusCode.isPresent()) {
            String balCode = statusCode.get();
            balStatusCodes.put(balCode, qNode);
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
     */
    @Override
    public void validateOpenAPIToBallerina() {
        Set<String> ballerinaCodes = balStatusCodes.keySet();
        Set<String> oasKeys = responses.keySet();
        List<String> missingCode = new ArrayList<>(oasKeys);
        missingCode.removeAll(ballerinaCodes);
        if (missingCode.size() > 0) {
            // Unimplemented status codes in swagger operation and get location via bal return node.
            reportDiagnostic(validatorContext, CompilationError.MISSING_STATUS_CODE,
                    missingCode.toString(), validatorContext.getMethod(), validatorContext.getPath());
        }

        responses.forEach((key, value) -> {
            if (!balStatusCodes.containsKey(key)) {
                return;
            }
            Content content = value.getContent();
            if (content == null) {
                return;
            }
            // Unimplemented media type code in swagger operation and get location via bal return node.
            Set<String> oasMediaTypes = content.keySet();
            List<String> ballerinaMediaTypes = balMediaTypes.get(key);
            List<String> missingMediaTypes = new ArrayList<>(oasMediaTypes);

            if (ballerinaMediaTypes == null) {
                reportDiagnostic(validatorContext, CompilationError.MISSING_RESPONSE_MEDIA_TYPE,
                        oasMediaTypes.toString(), key, validatorContext.getMethod(), validatorContext.getPath());
                return;
            }
            missingMediaTypes.removeAll(ballerinaMediaTypes);
            if (missingMediaTypes.size() > 0) {
                reportDiagnostic(validatorContext, CompilationError.MISSING_RESPONSE_MEDIA_TYPE,
                        missingMediaTypes.toString(), key, validatorContext.getMethod(), validatorContext.getPath());
            }
            content.forEach((mediaType, schema) -> {
                if (ballerinaMediaTypes.contains(mediaType)) {
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
                        Schema<?> valueSchema = validatorContext.getOpenAPI().getComponents().getSchemas()
                                .get(schemaName.get());
                        if (valueSchema instanceof ObjectSchema) {
                            objectSchema = (ObjectSchema) valueSchema;
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
                        validateObjectSchema(objectSchema, simpleRefNode);
                    }
                }
            });
        });
        //TODO: array type validation
    }

    /**
     * Validate object schema with record.
     */
    private void validateObjectSchema(ObjectSchema objectSchema, SimpleNameReferenceNode simpleRefNode) {
        Optional<Symbol> symbol = validatorContext.getContext().semanticModel().symbol(simpleRefNode);
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
                            if (bodyFieldType instanceof TypeReferenceTypeSymbol) {
                                TypeValidatorUtils.validateObjectSchema(objectSchema, bodyFieldType,
                                        validatorContext.getContext(),
                                        ((TypeReferenceTypeSymbol) bodyFieldType).definition().getName()
                                                .orElse(ANONYMOUS_RECORD), validatorContext.getLocation(),
                                        validatorContext.getSeverity());
                            }
                        }
                    }
                    if (!isHttp) {
                        TypeValidatorUtils.validateObjectSchema(objectSchema, typeSymbol, validatorContext.getContext(),
                                ((TypeReferenceTypeSymbol) typeSymbol).definition().getName().orElse(ANONYMOUS_RECORD),
                                validatorContext.getLocation(), validatorContext.getSeverity());
                    }
                } else {
                    TypeValidatorUtils.validateObjectSchema(objectSchema, typeSymbol, validatorContext.getContext(),
                            refType.definition().getName().orElse(ANONYMOUS_RECORD), validatorContext.getLocation(),
                            validatorContext.getSeverity());
                }
            }
        }
    }
}
