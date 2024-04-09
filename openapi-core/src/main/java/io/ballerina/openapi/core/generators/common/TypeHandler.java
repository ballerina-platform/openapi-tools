/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.openapi.core.generators.common;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.constraint.ConstraintGeneratorImp;
import io.ballerina.openapi.core.generators.constraint.ConstraintResult;
import io.ballerina.openapi.core.generators.document.DocCommentGeneratorImp;
import io.ballerina.openapi.core.generators.type.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.type.model.TypeGeneratorResult;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMapTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAP_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

public class TypeHandler {
    private static TypeHandler typeHandlerInstance;

    private static BallerinaTypesGenerator ballerinaTypesGenerator;
    public HashMap<String, TypeDefinitionNode> typeDefinitionNodes = new HashMap<>();
    private final Set<String> imports = new LinkedHashSet<>();

    private TypeHandler() {}

    public static void createInstance(OpenAPI openAPI, boolean isNullable) {
        typeHandlerInstance = new TypeHandler();
        ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI, isNullable);
    }

    public static TypeHandler getInstance() {
        return typeHandlerInstance;
    }

    public List<Diagnostic> getDiagnostics() {
        return ballerinaTypesGenerator.getDiagnostics();
    }

    public void addTypeDefinitionNode(String key, TypeDefinitionNode typeDefinitionNode) {
        typeDefinitionNodes.put(key, typeDefinitionNode);
    }

    public SyntaxTree generateTypeSyntaxTree() {
        ConstraintGeneratorImp constraintGenerator = new ConstraintGeneratorImp(GeneratorMetaData
                .getInstance().getOpenAPI(), typeDefinitionNodes);
        ConstraintResult constraintResult = constraintGenerator.updateTypeDefinitionsWithConstraints();
        typeDefinitionNodes = constraintResult.typeDefinitionNodeHashMap();
        boolean isConstraintAvailable = constraintResult.isConstraintAvailable();
        if (isConstraintAvailable) {
            imports.add("import ballerina/constraint;");
        }
        NodeList<ModuleMemberDeclarationNode> typeMembers = AbstractNodeFactory.createNodeList(
                typeDefinitionNodes.values().toArray(new TypeDefinitionNode[typeDefinitionNodes.size()]));
        NodeList<ImportDeclarationNode> imports = generateImportNodes();
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, typeMembers, eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = syntaxTree.modifyWith(modulePartNode);
        DocCommentGeneratorImp docCommentGenerator = new DocCommentGeneratorImp(GeneratorMetaData.getInstance()
                .getOpenAPI(), syntaxTree, GenSrcFile.GenFileType.GEN_TYPE, false);
        return docCommentGenerator.updateSyntaxTreeWithDocComments();
    }


    private NodeList<ImportDeclarationNode> generateImportNodes() {
        Set<ImportDeclarationNode> importDeclarationNodes = new LinkedHashSet<>();
        // Imports for the http module, when record has http type inclusions.
        if (!typeDefinitionNodes.isEmpty()) {
            importsForTypeDefinitions(importDeclarationNodes);
        }
        //Imports for constraints
        if (!imports.isEmpty()) {
            for (String importValue : imports) {
                ImportDeclarationNode importDeclarationNode = NodeParser.parseImportDeclaration(importValue);
                importDeclarationNodes.add(importDeclarationNode);
            }
        }
        if (importDeclarationNodes.isEmpty()) {
            return createEmptyNodeList();
        }
        return createNodeList(importDeclarationNodes);
    }

    private void importsForTypeDefinitions(Set<ImportDeclarationNode> imports) {
        for (TypeDefinitionNode node : typeDefinitionNodes.values()) {
            if (!(node.typeDescriptor() instanceof RecordTypeDescriptorNode)) {
                continue;
            }
            boolean isHttpImportExist = imports.stream().anyMatch(importNode -> importNode.moduleName().stream()
                    .anyMatch(moduleName -> moduleName.text().equals(GeneratorConstants.HTTP)));
            if (node.typeName().text().equals(GeneratorConstants.CONNECTION_CONFIG) && !isHttpImportExist) {
                ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                        GeneratorConstants.BALLERINA, GeneratorConstants.HTTP);
                imports.add(importForHttp);
            }
            RecordTypeDescriptorNode record = (RecordTypeDescriptorNode) node.typeDescriptor();
            for (Node field : record.fields()) {
                if (!(field instanceof TypeReferenceNode) ||
                        !(((TypeReferenceNode) field).typeName() instanceof QualifiedNameReferenceNode)) {
                    continue;
                }
                TypeReferenceNode recordField = (TypeReferenceNode) field;
                QualifiedNameReferenceNode typeInclusion = (QualifiedNameReferenceNode) recordField.typeName();

                if (!isHttpImportExist && typeInclusion.modulePrefix().text().equals(GeneratorConstants.HTTP)) {
                    ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                            GeneratorConstants.BALLERINA,
                            GeneratorConstants.HTTP);
                    imports.add(importForHttp);
                    break;
                }
            }
        }
    }

    public Optional<TypeDescriptorNode> getTypeNodeFromOASSchema(Schema schema) {
        return getTypeNodeFromOASSchema(schema, false);
    }

    public Optional<TypeDescriptorNode> getTypeNodeFromOASSchema(Schema schema, boolean overrideNullable) {
        TypeGeneratorResult typeGeneratorResult = ballerinaTypesGenerator
                .generateTypeDescriptorNodeForOASSchema(schema, overrideNullable);
        handleSubtypes(typeGeneratorResult.subtypeDefinitions());
        return typeGeneratorResult.typeDescriptorNode();
    }

    public TypeDescriptorNode generateHeaderType(Schema headersSchema) {
        TypeDescriptorNode headersType;
        if (headersSchema != null && getTypeNodeFromOASSchema(headersSchema).isPresent()) {
            headersType = getTypeNodeFromOASSchema(headersSchema).get();
        } else {
            TypeDescriptorNode stringType = createSimpleNameReferenceNode(createIdentifierToken(
                    GeneratorConstants.STRING));

            ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                    createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            TypeDescriptorNode stringArrType = createArrayTypeDescriptorNode(stringType,
                    createNodeList(dimensionNode));

            UnionTypeDescriptorNode unionType = createUnionTypeDescriptorNode(stringType, createToken(PIPE_TOKEN),
                    stringArrType);
            TypeParameterNode headerParamNode = createTypeParameterNode(createToken(LT_TOKEN), unionType,
                    createToken(SyntaxKind.GT_TOKEN));
            headersType = createMapTypeDescriptorNode(createToken(MAP_KEYWORD), headerParamNode);
        }
        return headersType;
    }

    public SimpleNameReferenceNode createTypeInclusionRecord(String statusCode, TypeDescriptorNode bodyType,
                                                             TypeDescriptorNode headersType, String method) {
        String recordName;
        if (bodyType != null) {
            String bodyTypeStr = bodyType.toString().replaceAll("[\\[\\\\]]", "Array");
            recordName = statusCode + GeneratorUtils.getValidName(bodyTypeStr, true);
        } else {
            recordName = statusCode;
        }

        RecordTypeDescriptorNode recordTypeDescriptorNode = getRecordTypeDescriptorNode(statusCode, bodyType,
                headersType);

        if (typeDefinitionNodes.containsKey(recordName) &&
                (Objects.nonNull(typeDefinitionNodes.get(recordName).typeDescriptor()) &&
                !typeDefinitionNodes.get(recordName).typeDescriptor().toSourceCode()
                        .equals(recordTypeDescriptorNode.toSourceCode()))) {
            recordName = method.toUpperCase() + recordName;
        }

        TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                createToken(PUBLIC_KEYWORD),
                createToken(TYPE_KEYWORD),
                createIdentifierToken(recordName),
                recordTypeDescriptorNode,
                createToken(SEMICOLON_TOKEN));
        typeDefinitionNodes.put(recordName, typeDefinitionNode);
        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
    }

    private static RecordTypeDescriptorNode getRecordTypeDescriptorNode(String statusCode, TypeDescriptorNode bodyType, TypeDescriptorNode headersType) {
        Token recordKeyWord = createToken(RECORD_KEYWORD);
        Token bodyStartDelimiter = createIdentifierToken("{|");
        // Create record fields
        List<Node> recordFields = new ArrayList<>();
        // Type reference node
        Token asteriskToken = createIdentifierToken("*");
        QualifiedNameReferenceNode typeNameField = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                statusCode);
        TypeReferenceNode typeReferenceNode = createTypeReferenceNode(
                asteriskToken,
                typeNameField,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(typeReferenceNode);

        IdentifierToken bodyFieldName = createIdentifierToken(GeneratorConstants.BODY,
                GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        if (bodyType != null) {
            RecordFieldNode bodyFieldNode = createRecordFieldNode(
                    null, null,
                    bodyType,
                    bodyFieldName, null,
                    createToken(SyntaxKind.SEMICOLON_TOKEN));
            recordFields.add(bodyFieldNode);
        }

        IdentifierToken headersFieldName = createIdentifierToken(GeneratorConstants.HEADERS,
                GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        RecordFieldNode headersFieldNode = createRecordFieldNode(
                null, null,
                headersType,
                headersFieldName, null,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(headersFieldNode);

        NodeList<Node> fieldsList = createSeparatedNodeList(recordFields);
        Token bodyEndDelimiter = createIdentifierToken("|}");

        return createRecordTypeDescriptorNode(
                recordKeyWord,
                bodyStartDelimiter,
                fieldsList, null,
                bodyEndDelimiter);
    }

    private void handleSubtypes(HashMap<String, TypeDefinitionNode> subTypesMap) {
        if (!subTypesMap.isEmpty()) {
            subTypesMap.forEach((recordName, typeDefinitionNode) -> {
                if (!typeDefinitionNodes.containsKey(recordName)) {
                    typeDefinitionNodes.put(typeDefinitionNode.typeName().text(), typeDefinitionNode);
                }
            });
        }
    }

}
