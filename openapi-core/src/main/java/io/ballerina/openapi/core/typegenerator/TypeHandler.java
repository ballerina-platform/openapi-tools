package io.ballerina.openapi.core.typegenerator;

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
import io.ballerina.openapi.core.typegenerator.model.GeneratorMetaData;
import io.ballerina.openapi.core.typegenerator.model.NameReferenceNodeReturnType;
import io.ballerina.openapi.core.typegenerator.model.TokenReturnType;
import io.ballerina.openapi.core.typegenerator.model.TypeDescriptorReturnType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.typegenerator.generators.ArrayTypeGenerator;

import java.util.HashMap;
import java.util.LinkedHashSet;
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

    private TypeHandler(OpenAPI openAPI, boolean isNullable, boolean generateServiceType) {
        GeneratorMetaData.createInstance(openAPI, isNullable, generateServiceType);
    }

    public static void createInstance(OpenAPI openAPI, boolean isNullable, boolean generateServiceType) {
        typeHandlerInstance = new TypeHandler(openAPI, isNullable, generateServiceType);
        ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI, isNullable, generateServiceType);
    }

    public static TypeHandler getInstance() {
        return typeHandlerInstance;
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
            if (node.typeName().text().equals(GeneratorConstants.CONNECTION_CONFIG)) {
                ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                        GeneratorConstants.BALLERINA,
                        GeneratorConstants.HTTP);
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
                        .anyMatch(moduleName -> moduleName.text().equals(GeneratorConstants.HTTP)));

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

    public SimpleNameReferenceNode createStatusCodeTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {
        NameReferenceNodeReturnType returnType = ballerinaTypesGenerator
                .createStatusCodeTypeInclusionRecord(statusCode, type);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.nameReferenceNode().get();
    }

    public Optional<TypeDescriptorNode> generateTypeDescriptorForOASSchema(Schema<?> schema, String recordName) throws BallerinaOpenApiException {
        TypeDescriptorReturnType returnType = ballerinaTypesGenerator.generateTypeDescriptorNodeForOASSchema(schema, recordName);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.typeDescriptorNode();
    }

    public TypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) throws BallerinaOpenApiException {
        TypeDescriptorReturnType returnType = ArrayTypeGenerator.getArrayTypeDescriptorNode(items);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.typeDescriptorNode().get();
    }

    public Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        TokenReturnType returnType = ballerinaTypesGenerator.getQueryParamTypeToken(schema);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.token().get();
    }

    public TypeDescriptorNode getReferencedQueryParameterTypeFromSchema(Schema<?> schema, String typeName) throws BallerinaOpenApiException {
        TypeDescriptorReturnType returnType = ballerinaTypesGenerator.getReferencedQueryParamTypeFromSchema(schema, typeName);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.typeDescriptorNode().get();
    }

    public ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(
            TypeDescriptorNode typeDescriptorNode) {
        return ballerinaTypesGenerator.getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
    }

    public TypeDescriptorNode generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName) throws
            BallerinaOpenApiException {
        TypeDescriptorReturnType returnType = ballerinaTypesGenerator.generateTypeDescriptorForJsonContent(schema, recordName);
        handleSubtypes(returnType.subtypeDefinitions());
        return returnType.typeDescriptorNode().get();
    }

    public TypeDescriptorNode generateTypeDescriptorForXMLContent() {
        return ballerinaTypesGenerator.getSimpleNameReferenceNode(GeneratorConstants.XML);
    }

    public TypeDescriptorNode generateTypeDescriptorForMapStringContent() {
        return ballerinaTypesGenerator.getSimpleNameReferenceNode(GeneratorConstants.MAP_STRING);
    }

    public TypeDescriptorNode generateTypeDescriptorForTextContent(String typeName) {
        return ballerinaTypesGenerator.getSimpleNameReferenceNode(typeName);
    }

    public TypeDescriptorNode generateTypeDescriptorForOctetStreamContent() {
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(GeneratorConstants.BYTE)),
                NodeFactory.createNodeList(dimensionNode));
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