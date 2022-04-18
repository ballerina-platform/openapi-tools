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
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNION_TYPE_DESC;
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

    private List<String> balStatusCodes = new ArrayList<>();

    public ReturnValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String method, String path) {

        this.context = context;
        this.openAPI = openAPI;
        this.method = method;
        this.path = path;
    }

    public void validateReturnBallerinaToOas(ReturnTypeDescriptorNode returnNode, ApiResponses responses) {

        // validate against status code - done
        // validate against content type -
        // validate against record  if any
        // step 01 -- care for single return types
        // step 02 -- union type
        // validate can be qualified identifier
        // it can be record with status code including media-type
        // Having status code is mandatory in OpenAPI
        // check for ballerina errors and not mentioned status code
        //-------handle normal status code with qualified name
        SyntaxKind kind = returnNode.type().kind();
        if (kind == QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) returnNode.type();
            qNode.identifier();
            // get the status code
            Optional<String> statusCode = generateApiResponseCode(qNode.identifier().toString().trim());
            if (statusCode.isPresent()) {
                String balCode = statusCode.get();
                balStatusCodes.add(balCode);
                if (!responses.containsKey(balCode)) {
                    // Undocumented status code
                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(),
                            balCode, method, path);
                }
            }
        } else if (kind == UNION_TYPE_DESC) {
            // A|B|C|D
            UnionTypeDescriptorNode uNode = (UnionTypeDescriptorNode) returnNode.type();
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
                    Optional<String> statusCode =
                            generateApiResponseCode(traversRNode.identifier().toString().trim());
                    if (statusCode.isPresent()) {
                        String balCode = statusCode.get();
                        balStatusCodes.add(balCode);
                        if (!responses.containsKey(balCode)) {
                            // Undocumented status code
                            updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(),
                                    balCode, method, path);
                        } else {
                            // handle media types
                        }
                    }
                }
            }
        } else if (kind == SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) returnNode.type();
            Optional<Symbol> symbol = context.semanticModel().symbol(simpleRefNode);
            //Access the status code and media type via record
            if (symbol.isEmpty()) {
                return;
            }
            if (symbol.get() instanceof  TypeReferenceTypeSymbol) {
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
                                Optional<String> code = generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                                if (code.isEmpty()) {
                                    return;
                                }
                                balStatusCodes.add(code.get());
                                if (!responses.containsKey(code.get())) {
                                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(),
                                            code.get(), method, path);
                                } else {
                                    // media type validation
                                }
                            }
                        }
                    }
                    ////////////------------
                    if (!isHttp) {
                        // validate normal Record status code 200 and application json

                    }
                }
//
//                if (!isHttp) {
//                    // validate normal Record status code 200 and application json
//
//                }
            }
///// validate- Record
//            TypeValidatorUtils.validateRecordType(schema, typeSymbol, balRecordName,
//                    context, openAPI, oasSchemaName);
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

}
