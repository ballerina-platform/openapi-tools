package io.ballerina.openapi.corenew.typegenerator;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.generators.ArrayTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.TypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.ballerina.openapi.corenew.typegenerator.model.ReturnType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.CONNECTION_CONFIG;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.HTTP;

public class TypeHandler {

    public static final Map<String, TypeDefinitionNode> typeDefinitionNodes = new HashMap<>();
    private final Set<String> imports = new LinkedHashSet<>();
    private static TypeHandler typeHandlerInstance;

    private TypeHandler(OpenAPI openAPI, boolean isNullable, List<TypeDefinitionNode> typeDefinitionNodeList,
                        boolean generateServiceType) {
        GeneratorMetaData.createInstance(openAPI, isNullable, generateServiceType);
    }

    public static void createInstance(OpenAPI openAPI, boolean isNullable, boolean generateServiceType) {
        typeHandlerInstance =
                new TypeHandler(openAPI, isNullable, new ArrayList<>(), generateServiceType);
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
            if (node.typeName().text().equals(CONNECTION_CONFIG)) {
                ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                        GeneratorConstants.BALLERINA,
                        HTTP);
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
                        .anyMatch(moduleName -> moduleName.text().equals(HTTP)));

                if (!isHttpImportExist && typeInclusion.modulePrefix().text().equals(HTTP)) {
                    ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(
                            GeneratorConstants.BALLERINA,
                            HTTP);
                    imports.add(importForHttp);
                    break;
                }
            }
        }
    }

    public static SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return BallerinaTypesGenerator.getInstance().getSimpleNameReferenceNode(name);
    }

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        return BallerinaTypesGenerator.getInstance().getQualifiedNameReferenceNode(modulePrefix, identifier);
    }

    public SimpleNameReferenceNode createTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {
        HashMap subTypesMap = new HashMap<String, TypeDefinitionNode>();
        SimpleNameReferenceNode nameReferenceNode = BallerinaTypesGenerator.getInstance().createTypeInclusionRecord(statusCode, type, subTypesMap);
        handleSubtypes(subTypesMap);
        return nameReferenceNode;
    }

    public Optional<TypeDescriptorNode> generateTypeDescriptorForOASSchema(Schema<?> schema, String recordName) throws BallerinaOpenApiException {
        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap();
        Optional<TypeDescriptorNode> typeDescriptorNode = BallerinaTypesGenerator.getInstance().generateTypeDescriptorNodeForOASSchema(schema, subTypesMap, new HashMap<>());
        handleSubtypes(subTypesMap);
        if (typeDescriptorNode.isPresent()) {
            typeDefinitionNodes.put(recordName, createTypeDefinitionNode(null,
                    createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                    createIdentifierToken(recordName), typeDescriptorNode.get(), createToken(SyntaxKind.SEMICOLON_TOKEN)));
        }
        return typeDescriptorNode;
    }

    public ArrayTypeDescriptorNode getArrayTypeDescriptorNode(OpenAPI openAPI, Schema<?> items) throws BallerinaOpenApiException {
//        ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(items,)
        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
        ArrayTypeDescriptorNode typeDescriptorNode = ArrayTypeGenerator.getArrayTypeDescriptorNode(openAPI, items, subTypesMap, new HashMap<>());
        handleSubtypes(subTypesMap);
        return typeDescriptorNode;
    }

    public Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
        Token token = BallerinaTypesGenerator.getInstance().getQueryParamTypeToken(schema, subTypesMap);
        handleSubtypes(subTypesMap);
        return token;
    }

    public NameReferenceNode getReferencedQueryParameterTypeFromSchema(Schema<?> schema, String typeName) throws BallerinaOpenApiException {
        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, typeName, null, subTypesMap, new HashMap<>());
        TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
//        Optional<TypeDescriptorNode> nameReferenceNode = BallerinaTypesGenerator.getInstance().generateTypeDescriptorNodeForOASSchema(schema, subTypesMap, new HashMap<>());
//        if (nameReferenceNode.isPresent()) {
//            subTypesMap.put(typeName, createTypeDefinitionNode(null,
//                    createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
//                    createIdentifierToken(typeName), nameReferenceNode.get(), createToken(SyntaxKind.SEMICOLON_TOKEN)));
//        }
        subTypesMap.put(typeName, createTypeDefinitionNode(null,
                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
                createIdentifierToken(typeName), typeDescriptorNode, createToken(SyntaxKind.SEMICOLON_TOKEN)));
        handleSubtypes(subTypesMap);
        return createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(typeName));
    }

    public RequiredParameterNode getMapJsonParameterNode(IdentifierToken parameterName, Parameter parameter, NodeList<AnnotationNode> annotations) {
        return BallerinaTypesGenerator.getInstance().getMapJsonParameterNode(parameterName, parameter, annotations);
    }

    public ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(TypeDescriptorNode typeDescriptorNode) {
        return BallerinaTypesGenerator.getInstance().getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
    }

    public TypeDescriptorNode generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName) throws BallerinaOpenApiException {
        HashMap subTypesMap = new HashMap<String, TypeDefinitionNode>();
        ReturnType returnType = BallerinaTypesGenerator.getInstance().generateTypeDescriptorForJsonContent(schema, recordName, subTypesMap);
        if (returnType.originalType().isPresent()) {
            typeDefinitionNodes.put(returnType.recordName() != null ? returnType.recordName() : recordName, returnType.originalType().get());
        }
        handleSubtypes(subTypesMap);
        return returnType.nameReferenceNode().get();
    }

    public TypeDescriptorNode generateTypeDescriptorForXMLContent(Schema<?> schema) {
        return getSimpleNameReferenceNode(GeneratorConstants.XML);
    }

    public TypeDescriptorNode generateTypeDescriptorForMapStringContent(Schema<?> schema) {
        return getSimpleNameReferenceNode(GeneratorConstants.MAP_STRING);
    }

    public TypeDescriptorNode generateTypeDescriptorForTextContent(Schema<?> schema, String typeName) {
        return getSimpleNameReferenceNode(typeName);
    }

    public TypeDescriptorNode generateTypeDescriptorForOctetStreamContent(Schema<?> schema) {
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