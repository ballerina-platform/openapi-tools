/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiSchema;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static org.ballerinalang.generators.GeneratorUtils.escapeIdentifier;

/**
 *
 */
public class BallerinaSchemaGenerator {
    private static final PrintStream outStream = System.err;

    public static SyntaxTree generateSyntaxTree(Path definitionPath)
            throws OpenApiException, FormatterException, IOException {
        OpenAPI openApi = parseOpenAPIFile(definitionPath.toString());
        // imports null
        // TypeDefinitionNodes their
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        if (openApi.getComponents() != null) {
            //Create typeDefinitionNode
            Components components = openApi.getComponents();
            if (components.getSchemas() != null) {
                Map<String, Schema> schemas = components.getSchemas();
                for (Map.Entry<String, Schema> schema: schemas.entrySet()) {
                    List<String> required = schema.getValue().getRequired();

                    //1.typeKeyWord
                    Token typeKeyWord = AbstractNodeFactory.createIdentifierToken("type ");
                    //2.typeName
                    IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(schema.getKey());
                    //3.typeDescriptor - RecordTypeDescriptor
                    //3.1 recordKeyWord
                    Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
                    //3.2 bodyStartDelimiter
                    Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{ ");
                    //3.3 fields
                    if (schema.getValue().getProperties() != null) {
                        Map<String, Schema> fields = schema.getValue().getProperties();
                        List<Node> recordFieldList = new ArrayList<>();
                        for (Map.Entry<String, Schema> field : fields.entrySet()) {
                            //Generate RecordFiled
                            RecordFieldNode recordFieldNode;
                            TypeDescriptorNode fieldTypeName = extractOpenApiSchema(field.getValue());
                            Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
                            //FiledName
                            IdentifierToken fieldName =
                                    AbstractNodeFactory.createIdentifierToken(" " + escapeIdentifier(field.getKey().trim()));
                            //GetType
//                                Token questionMarkToken = AbstractNodeFactory.createIdentifierToken("");
                            if (required != null) {
                                if (!required.contains(field.getKey().trim())) {
                                    Token questionMarkToken = AbstractNodeFactory.createIdentifierToken("?");
                                    recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                                            fieldTypeName, fieldName, questionMarkToken, semicolonToken);
                                } else {
                                    recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                                            fieldTypeName, fieldName, null, semicolonToken);
                                }
                            } else {
                                recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                                        fieldTypeName, fieldName, null, semicolonToken);
                            }
                            recordFieldList.add(recordFieldNode);
                        }
                        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                        Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken("}");
                        RecordTypeDescriptorNode recordTypeDescriptorNode =
                                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                        fieldNodes, null, bodyEndDelimiter);
                        Token semicolon = AbstractNodeFactory.createIdentifierToken(";");
                        TypeDefinitionNode typeDefinitionNode = NodeFactory.createTypeDefinitionNode(null, null,
                                typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
                        typeDefinitionNodeList.add(typeDefinitionNode);
                    }
                }
            }
        }
        //Create imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();


        // Create module member declaration
//        NodeList<Node> members = NodeFactory.createNodeList(typeDefinitionNodeList);
        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(typeDefinitionNodeList.get(0));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        System.out.println(Formatter.format(syntaxTree.modifyWith(modulePartNode)));
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI     URI for the OpenAPI contract
     * @return {@link OpenAPI}  OpenAPI model
     * @throws OpenApiException in case of exception
     */
    public static OpenAPI parseOpenAPIFile(String definitionURI) throws OpenApiException, IOException {
        Path contractPath = Paths.get(definitionURI);
        if (!Files.exists(contractPath)) {
            throw new OpenApiException(ErrorMessages.invalidFilePath(definitionURI));
        }
        if (!(definitionURI.endsWith(".yaml") || definitionURI.endsWith(".json"))) {
            throw new OpenApiException(ErrorMessages.invalidFile());
        }
        String openAPIFileContent = Files.readString(Paths.get(definitionURI));
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);
        if (parseResult.getMessages().size() > 0) {
            throw new OpenApiException(ErrorMessages.parserException(definitionURI));
        }
        OpenAPI api = parseResult.getOpenAPI();
        return api;
    }

    /**
     * Common method to extract OpenApi Schema type objects in to Ballerina type compatible schema objects.
     *
     * @param schema - OpenApi Schema
     */
    private static TypeDescriptorNode extractOpenApiSchema(Schema schema) {

        if (schema.getType() != null || schema.getProperties() != null) {
            if (schema.getType() != null && ((schema.getType().equals("integer") || schema.getType().equals("number"))
                    || schema.getType().equals("string") || schema.getType().equals("boolean"))) {
                 Token typeName =
                        AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(schema.getType().trim()));
                return NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
//                setSchemaType(typeSchema, "boolean");
            } else if (schema.getType() != null && schema.getType().equals("array")) {
                if (schema instanceof ArraySchema) {
                    final ArraySchema arraySchema = (ArraySchema) schema;
//                    typeSchema.setArray(true);
                    if (arraySchema.getItems() != null) {
                        BallerinaOpenApiSchema arrayItems = new BallerinaOpenApiSchema();
//                        extractOpenApiSchema(arraySchema.getItems(), arrayItems, isInline, composedSchemaType);
//                        typeSchema.setItemsSchema(arrayItems);
                    }
                }
            } else if ((schema.getType() != null && schema.getType().equals("object")) ||
                    schema.getProperties() != null) {
                if (schema.getProperties() != null) {
                    List<String> requiredBlock = schema.getRequired();
                    final Iterator<Map.Entry<String, Schema>> propertyIterator = schema.getProperties()
                            .entrySet().iterator();
                    List<BallerinaOpenApiSchema> propertyList = new ArrayList<>();

                    while (propertyIterator.hasNext()) {
                        final Map.Entry<String, Schema> nextProp = propertyIterator.next();
                        String propName = nextProp.getKey();
                        if (requiredBlock == null || (!requiredBlock.isEmpty() &&
                                !requiredBlock.contains(propName))) {
                            propName += "?";
                        }
                        final Schema propValue = nextProp.getValue();
                        BallerinaOpenApiSchema propertySchema = new BallerinaOpenApiSchema();

                        propertySchema.setSchemaName(escapeIdentifier(propName));
//                        extractOpenApiSchema(propValue, propertySchema, isInline, composedSchemaType);

                        propertyList.add(propertySchema);
                    }

//                    typeSchema.setHasChildElements(true);
//                    typeSchema.setSchemaProperties(propertyList);
                }
            } else {
                outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
//                setSchemaType(typeSchema, "any");
            }
        } else if (schema.get$ref() != null) {
//            if (composedSchemaType != null && composedSchemaType.equals("ALL_OF")) {
////                setComposedSchemaType(typeSchema, extractReferenceType(schema.get$ref()));
//            } else {
////                setSchemaType(typeSchema, extractReferenceType(schema.get$ref()));
//            }

        } else {
            //This contains a fallback to Ballerina common type `any` if the OpenApi specification type is not defined
            // or not compatible with any of the current Ballerina types.
            outStream.println("Encountered an unsupported type. Type `any` would be used for the field.");
//            setSchemaType(typeSchema, "any");
        }
        Token typeName =
                AbstractNodeFactory.createIdentifierToken("any");
        return NodeFactory.createBuiltinSimpleNameReferenceNode(null, typeName);
    }

    private static Collection<TypeDefinitionNode> getTokenList(List<TypeDefinitionNode> stringList) {
        List<TypeDefinitionNode> tokenList = new LinkedList<>();
        for (TypeDefinitionNode value : stringList) {
            tokenList.add(value);
        }
        return tokenList;
    }

}
