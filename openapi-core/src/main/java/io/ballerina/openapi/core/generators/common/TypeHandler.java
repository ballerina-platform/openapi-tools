package io.ballerina.openapi.core.generators.common;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.core.generators.type.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.type.GeneratorConstants;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.generators.ArrayTypeGenerator;
import io.ballerina.openapi.core.generators.type.model.TypeGeneratorResult;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.type.model.TokenReturnType;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;

public class TypeHandler {
    private static TypeHandler typeHandlerInstance;
    private static BallerinaTypesGenerator ballerinaTypesGenerator;
    public final Map<String, TypeDefinitionNode> typeDefinitionNodes = new HashMap<>();
    private final Set<String> imports = new LinkedHashSet<>();

    private TypeHandler(OpenAPI openAPI, boolean isNullable) {
        GeneratorMetaData.createInstance(openAPI, isNullable);
    }

    public static void createInstance(OpenAPI openAPI, boolean isNullable) {
        typeHandlerInstance = new TypeHandler(openAPI, isNullable);
        ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI, isNullable);
    }

    public static TypeHandler getInstance() {
        return typeHandlerInstance;
    }

    public Map<String, TypeDefinitionNode> getTypeDefinitionNodes() {
        return typeDefinitionNodes;
    }

    public SyntaxTree generateTypeSyntaxTree() {
        NodeList<ModuleMemberDeclarationNode> typeMembers = AbstractNodeFactory.createNodeList(
                typeDefinitionNodes.values().toArray(new TypeDefinitionNode[typeDefinitionNodes.size()]));
        NodeList<ImportDeclarationNode> imports = generateImportNodes();
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, typeMembers, eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
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
            if (node.typeName().text().equals(io.ballerina.openapi.core.generators.type.GeneratorConstants.CONNECTION_CONFIG)) {
                ImportDeclarationNode importForHttp = io.ballerina.openapi.core.generators.type.GeneratorUtils.getImportDeclarationNode(
                        io.ballerina.openapi.core.generators.type.GeneratorConstants.BALLERINA,
                        io.ballerina.openapi.core.generators.type.GeneratorConstants.HTTP);
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
                boolean isHttpImportExist = imports.stream().anyMatch(importNode -> importNode.moduleName().stream()
                        .anyMatch(moduleName -> moduleName.text().equals(io.ballerina.openapi.core.generators.type.GeneratorConstants.HTTP)));

                if (!isHttpImportExist && typeInclusion.modulePrefix().text().equals(io.ballerina.openapi.core.generators.type.GeneratorConstants.HTTP)) {
                    ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                            io.ballerina.openapi.core.generators.type.GeneratorConstants.BALLERINA,
                            io.ballerina.openapi.core.generators.type.GeneratorConstants.HTTP);
                    imports.add(importForHttp);
                    break;
                }
            }
        }
    }

    public Optional<TypeDescriptorNode> getTypeNodeFromOASSchema(Schema schema) {
        TypeGeneratorResult typeGeneratorResult = ballerinaTypesGenerator.generateTypeDescriptorNodeForOASSchema(schema);
        handleSubtypes(typeGeneratorResult.subtypeDefinitions());
        return typeGeneratorResult.typeDescriptorNode();
    }


//    public SimpleNameReferenceNode createStatusCodeTypeInclusionRecord(String includedType, TypeDescriptorNode type) {
//        TypeGeneratorResult returnType = ballerinaTypesGenerator
//                .createTypeInclusionRecord(statusCode, type);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.nameReferenceNode().get();
//    }
//
//    public Optional<TypeDescriptorNode> generateTypeDescriptorForOASSchema(Schema<?> schema, String recordName) throws OASTypeGenException {
//        TypeGeneratorResult returnType = ballerinaTypesGenerator.generateTypeDescriptorNodeForOASSchema(schema, recordName);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.typeDescriptorNode();
//    }
//
//    public TypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) throws OASTypeGenException {
//        TypeGeneratorResult returnType = ArrayTypeGenerator.getArrayTypeDescriptorNode(items);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.typeDescriptorNode().get();
//    }
//
//    public Token getQueryParamTypeToken(Schema<?> schema) throws OASTypeGenException {
//        TokenReturnType returnType = ballerinaTypesGenerator.getQueryParamTypeToken(schema);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.token().get();
//    }
//
//    public TypeDescriptorNode getReferencedQueryParameterTypeFromSchema(Schema<?> schema, String typeName) throws OASTypeGenException {
//        TypeGeneratorResult returnType = ballerinaTypesGenerator.getReferencedQueryParamTypeFromSchema(schema, typeName);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.typeDescriptorNode().get();
//    }
//
//    public ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(
//            TypeDescriptorNode typeDescriptorNode) {
//        return ballerinaTypesGenerator.getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
//    }
//
//    public TypeDescriptorNode generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName) throws
//            OASTypeGenException {
//        TypeGeneratorResult returnType = ballerinaTypesGenerator.generateTypeDescriptorForJsonContent(schema, recordName);
//        handleSubtypes(returnType.subtypeDefinitions());
//        return returnType.typeDescriptorNode().get();
//    }
//
//    public TypeDescriptorNode generateTypeDescriptorForXMLContent() {
//        return ballerinaTypesGenerator.getSimpleNameReferenceNode(io.ballerina.openapi.core.generators.type.GeneratorConstants.XML);
//    }
//
//    public TypeDescriptorNode generateTypeDescriptorForMapStringContent() {
//        return ballerinaTypesGenerator.getSimpleNameReferenceNode(io.ballerina.openapi.core.generators.type.GeneratorConstants.MAP_STRING);
//    }
//
//    public TypeDescriptorNode generateTypeDescriptorForTextContent(String typeName) {
//        return ballerinaTypesGenerator.getSimpleNameReferenceNode(typeName);
//    }
//
//    public TypeDescriptorNode generateTypeDescriptorForOctetStreamContent() {
//        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
//                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
//                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
//        return createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
//                                createIdentifierToken(GeneratorConstants.BYTE)),
//                NodeFactory.createNodeList(dimensionNode));
//    }

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
