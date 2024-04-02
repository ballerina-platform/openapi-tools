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
import io.ballerina.openapi.core.generators.constraint.ConstraintGeneratorImp;
import io.ballerina.openapi.core.generators.constraint.ConstraintResult;
import io.ballerina.openapi.core.generators.type.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.model.TypeGeneratorResult;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
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
    private static GeneratorMetaData generatorMetadata;
    public HashMap<String, TypeDefinitionNode> typeDefinitionNodes = new HashMap<>();
    private final Set<String> imports = new LinkedHashSet<>();

    private TypeHandler(OpenAPI openAPI, boolean isNullable) {
        generatorMetadata = GeneratorMetaData.createInstance(openAPI, isNullable);
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

    public static GeneratorMetaData getGeneratorMetadata() {
        return generatorMetadata;
    }

    public SyntaxTree generateTypeSyntaxTree() {
        ConstraintGeneratorImp constraintGenerator = new ConstraintGeneratorImp(GeneratorMetaData.getInstance().getOpenAPI(), typeDefinitionNodes);
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

    public Optional<TypeDescriptorNode> getTypeNodeFromOASSchema(Schema schema) {
        TypeGeneratorResult typeGeneratorResult = ballerinaTypesGenerator.generateTypeDescriptorNodeForOASSchema(schema);
        handleSubtypes(typeGeneratorResult.subtypeDefinitions());
        return typeGeneratorResult.typeDescriptorNode();
    }

    public TypeDescriptorNode createStatusCodeRecord(String statusCode, Schema bodySchema, Schema headersSchema) {
        TypeDescriptorNode bodyType;
        TypeDescriptorNode headersType;
        if (bodySchema != null && getTypeNodeFromOASSchema(bodySchema).isPresent()) {
            bodyType = getTypeNodeFromOASSchema(bodySchema).get();
        } else {
            bodyType = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANYDATA));
        }

        if (headersSchema != null && getTypeNodeFromOASSchema(headersSchema).isPresent()) {
            headersType = getTypeNodeFromOASSchema(headersSchema).get();
        } else {
            TypeDescriptorNode stringType = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.STRING));

            ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                    createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            TypeDescriptorNode stringArrType = createArrayTypeDescriptorNode(stringType, createNodeList(dimensionNode));

            UnionTypeDescriptorNode unionType = createUnionTypeDescriptorNode(stringType, createToken(PIPE_TOKEN),
                    stringArrType);
            TypeParameterNode headerParamNode = createTypeParameterNode(createToken(LT_TOKEN), unionType,
                    createToken(SyntaxKind.GT_TOKEN));
            headersType = createMapTypeDescriptorNode(createToken(MAP_KEYWORD), headerParamNode);
        }
        return createTypeInclusionRecord(statusCode, bodyType, headersType);
    }

    public TypeDescriptorNode generateHeaderType(Schema headersSchema) {
        TypeDescriptorNode headersType;
        if (headersSchema != null && getTypeNodeFromOASSchema(headersSchema).isPresent()) {
            headersType = getTypeNodeFromOASSchema(headersSchema).get();
        } else {
            TypeDescriptorNode stringType = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.STRING));

            ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                    createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            TypeDescriptorNode stringArrType = createArrayTypeDescriptorNode(stringType, createNodeList(dimensionNode));

            UnionTypeDescriptorNode unionType = createUnionTypeDescriptorNode(stringType, createToken(PIPE_TOKEN),
                    stringArrType);
            TypeParameterNode headerParamNode = createTypeParameterNode(createToken(LT_TOKEN), unionType,
                    createToken(SyntaxKind.GT_TOKEN));
            headersType = createMapTypeDescriptorNode(createToken(MAP_KEYWORD), headerParamNode);
        }
        return headersType;
    }

    public SimpleNameReferenceNode createTypeInclusionRecord(String statusCode, TypeDescriptorNode bodyType,
                                                              TypeDescriptorNode headersType) {
        String recordName = statusCode + GeneratorUtils.getValidName(bodyType.toString(), true);
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

        IdentifierToken bodyFieldName = createIdentifierToken(GeneratorConstants.BODY, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        RecordFieldNode bodyFieldNode = createRecordFieldNode(
                null, null,
                bodyType,
                bodyFieldName, null,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(bodyFieldNode);

        IdentifierToken headersFieldName = createIdentifierToken(GeneratorConstants.HEADERS, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        RecordFieldNode headersFieldNode = createRecordFieldNode(
                null, null,
                headersType,
                headersFieldName, null,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(headersFieldNode);

        NodeList<Node> fieldsList = createSeparatedNodeList(recordFields);
        Token bodyEndDelimiter = createIdentifierToken("|}");

        RecordTypeDescriptorNode recordTypeDescriptorNode = createRecordTypeDescriptorNode(
                recordKeyWord,
                bodyStartDelimiter,
                fieldsList, null,
                bodyEndDelimiter);

        TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                createToken(PUBLIC_KEYWORD),
                createToken(TYPE_KEYWORD),
                createIdentifierToken(recordName),
                recordTypeDescriptorNode,
                createToken(SEMICOLON_TOKEN));
        typeDefinitionNodes.put(recordName, typeDefinitionNode);
        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
    }

    /**
//     * Generate union type node when response has multiple content types.
//     */
//    private TypeDescriptorNode handleMultipleContents(Set<Map.Entry<String, MediaType>> contentEntries) {
//        Set<String> qualifiedNodes = new LinkedHashSet<>();
//        for (Map.Entry<String, MediaType> contentType : contentEntries) {
////            String recordName = getNewRecordName(pathRecord);
////            TypeDescriptorNode mediaTypeToken = ServiceGenerationUtils
////                    .generateTypeDescriptorForMediaTypes(contentType, recordName);
//            TypeDescriptorNode mediaTypeToken = getTypeNodeFromOASSchema(contentType.getValue().getSchema()).get();
//            if (mediaTypeToken == null) {
//                SimpleNameReferenceNode httpResponse = createSimpleNameReferenceNode(createIdentifierToken(
//                        io.ballerina.openapi.core.service.GeneratorConstants.ANYDATA));
//                qualifiedNodes.add(httpResponse.name().text());
//            } else if (mediaTypeToken instanceof NameReferenceNode) {
////                setCountForRecord(countForRecord++);
//                qualifiedNodes.add(mediaTypeToken.toSourceCode());
//            } else {
//                qualifiedNodes.add(mediaTypeToken.toSourceCode());
//            }
//        }
//
//        if (qualifiedNodes.size() == 1) {
//            return NodeParser.parseTypeDescriptor(qualifiedNodes.iterator().next());
//        }
//        String unionType = String.join(io.ballerina.openapi.core.service.GeneratorConstants.PIPE, qualifiedNodes);
//        if (qualifiedNodes.contains(io.ballerina.openapi.core.service.GeneratorConstants.ANYDATA)) {
//            return NodeParser.parseTypeDescriptor(io.ballerina.openapi.core.service.GeneratorConstants.ANYDATA);
//        }
//        return NodeParser.parseTypeDescriptor(unionType);
//    }

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
