/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.corenew.typegenerator;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
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
import io.ballerina.openapi.corenew.typegenerator.generators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.ArrayTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.PrimitiveTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.RecordTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.TypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.UnionTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.ballerina.openapi.corenew.typegenerator.model.ReturnType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.CONNECTION_CONFIG;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.HTTP;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getValidName;

/**
 * This class wraps the {@link Schema} from openapi models inorder to overcome complications
 * while populating syntax tree.
 *
 * @since 1.3.0
 */
public class BallerinaTypesGenerator {

    private final Set<String> imports = new LinkedHashSet<>();

    public final Map<String, TypeDefinitionNode> typeDefinitionNodes = new HashMap<>();
    private static BallerinaTypesGenerator typesGeneratorInstance;

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     * @param  typeDefinitionNodeList list of types generated by earlier generations
     */
    public BallerinaTypesGenerator(OpenAPI openAPI, boolean isNullable,
                                   List<TypeDefinitionNode> typeDefinitionNodeList) {
        GeneratorMetaData.createInstance(openAPI, isNullable, false);
    }

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     */
    public BallerinaTypesGenerator(OpenAPI openAPI, boolean isNullable) {
        this(openAPI, isNullable, new LinkedList<>());
    }

    /**
     * This public constructor is used to generate record and other relevant data type when the absent of the nullable
     * flag in the openapi command.
     *
     * @param openAPI OAS definition
     */
    public BallerinaTypesGenerator(OpenAPI openAPI) {
        this(openAPI, false, new LinkedList<>());
    }

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     * @param typeDefinitionNodeList list of types generated by earlier generations
     * @param generateServiceType indicate whether the service generation includes service type
     */
    public BallerinaTypesGenerator(OpenAPI openAPI, boolean isNullable, List<TypeDefinitionNode> typeDefinitionNodeList,
                                   boolean generateServiceType) {
        GeneratorMetaData.createInstance(openAPI, isNullable, generateServiceType);
    }

    public static void createInstance(OpenAPI openAPI, boolean isNullable, boolean generateServiceType) {
        typesGeneratorInstance =
                new BallerinaTypesGenerator(openAPI, isNullable, new ArrayList<>(), generateServiceType);
    }

    public static BallerinaTypesGenerator getInstance() {
        return typesGeneratorInstance;
    }

    /**
     * Generate syntaxTree for component schema.
     */
    public SyntaxTree generateTypeSyntaxTree() throws BallerinaOpenApiException {
//        OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
//        List<TypeDefinitionNode> typeDefinitionNodeListForSchema = new ArrayList<>();
////        if (openAPI.getComponents() != null) {
////            // Create typeDefinitionNode
////            Components components = openAPI.getComponents();
////            Map<String, Schema> schemas = components.getSchemas();
////            if (schemas != null) {
////                for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
////                    String schemaKey = schema.getKey().trim();
////                    if (GeneratorUtils.isValidSchemaName(schemaKey)) {
////                        typeDefinitionNodeListForSchema.add(getTypeDefinitionNode(schema.getValue(), schemaKey));
////                    }
////                }
////            }
////        }
//        //Create imports for the http module, when record has http type inclusions.
//        NodeList<ImportDeclarationNode> imports = generateImportNodes();
//        typeDefinitionNodeList.addAll(typeDefinitionNodeListForSchema);
//        // Create module member declaration
//        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
//                typeDefinitionNodeList.toArray(new TypeDefinitionNode[typeDefinitionNodeList.size()]));
//
//        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
//        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
//
//        TextDocument textDocument = TextDocuments.from("");
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);

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

    /**
     * Create Type Definition Node for a given OpenAPI schema.
     *
     * @param schema   OpenAPI schema
     * @param typeName IdentifierToken of the name of the type
     * @return {@link TypeDefinitionNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public TypeDefinitionNode getTypeDefinitionNode(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws BallerinaOpenApiException {
        IdentifierToken typeNameToken = AbstractNodeFactory.createIdentifierToken(GeneratorUtils.getValidName(
                typeName.trim(), true));
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, GeneratorUtils.getValidName(
                typeName.trim(), true), null, subTypesMap, pregeneratedTypeMap);
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
//        if (TypeGeneratorUtils.isConstraintAllowed(typeName, schema)) {
//            AnnotationNode constraintNode = TypeGeneratorUtils.generateConstraintNode(typeName, schema);
//            if (constraintNode != null) {
//                typeAnnotations.add(constraintNode);
//            }
//        }
//        TypeGeneratorUtils.getRecordDocs(schemaDocs, schema, typeAnnotations);
        TypeDefinitionNode typeDefinitionNode =
                typeGenerator.generateTypeDefinitionNode(typeNameToken, typeAnnotations);
        imports.addAll(typeGenerator.getImports());
        return typeDefinitionNode;
    }

    public static TypeDescriptorNode getTypeDescriptorNode(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws BallerinaOpenApiException {
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, GeneratorUtils.getValidName(
                typeName.trim(), true), null, subTypesMap, pregeneratedTypeMap);
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
//        if (TypeGeneratorUtils.isConstraintAllowed(typeName, schema)) {
//            AnnotationNode constraintNode = TypeGeneratorUtils.generateConstraintNode(typeName, schema);
//            if (constraintNode != null) {
//                typeAnnotations.add(constraintNode);
//            }
//        }
//        TypeGeneratorUtils.getRecordDocs(schemaDocs, schema, typeAnnotations);
        TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
        return typeDescriptorNode;
    }

    public SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return createSimpleNameReferenceNode(createIdentifierToken(name));
    }

    public QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        Token modulePrefixToken = AbstractNodeFactory.createIdentifierToken(modulePrefix);
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
    }

    /**
     * Generate typeDescriptor for given schema.
     */
    public Optional<TypeDescriptorNode> generateTypeDescriptorNodeForOASSchema(Schema<?> schema, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws BallerinaOpenApiException {
        if (schema == null) {
            return Optional.empty();
        }

        if (schema.get$ref() != null) {
            String schemaName = GeneratorUtils.getValidName(extractReferenceType(schema.get$ref()), true);
            String typeName = escapeIdentifier(schemaName);
            if (!pregeneratedTypeMap.containsKey(typeName)) {
                pregeneratedTypeMap.put(typeName, getSimpleNameReferenceNode(typeName));
                TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(schemaName), GeneratorUtils.getValidName(
                        schemaName.trim(), true), null, subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
                TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                        createToken(PUBLIC_KEYWORD),
                        createToken(TYPE_KEYWORD),
                        createIdentifierToken(typeName),
                        typeDescriptorNode,
                        createToken(SEMICOLON_TOKEN));
                subTypesMap.put(typeName, typeDefinitionNode);
            }
            return Optional.ofNullable(getSimpleNameReferenceNode(typeName));
        } else if (GeneratorUtils.isMapSchema(schema)) {
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode record = recordTypeGenerator.generateTypeDescriptorNode();
            return Optional.ofNullable(record);
        } else if (GeneratorUtils.getOpenAPIType(schema) != null) {
            String schemaType = GeneratorUtils.getOpenAPIType(schema);
            boolean isPrimitiveType = schemaType.equals(GeneratorConstants.INTEGER) || schemaType.equals(GeneratorConstants.NUMBER) ||
                    schemaType.equals(GeneratorConstants.BOOLEAN) || schemaType.equals(GeneratorConstants.STRING);
            if (GeneratorUtils.isArraySchema(schema)) {
                ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(schema, null, schemaType, subTypesMap, pregeneratedTypeMap);
                return arrayTypeGenerator.getTypeDescNodeForArraySchema(schema, subTypesMap);
            } else if (isPrimitiveType) {
                PrimitiveTypeGenerator primitiveTypeGenerator = new PrimitiveTypeGenerator(schema, null, subTypesMap, pregeneratedTypeMap);
                return Optional.ofNullable(primitiveTypeGenerator.generateTypeDescriptorNode());
                //This returns identifier node for the types: int, float, decimal, boolean, string
//                IdentifierToken identifierToken = createIdentifierToken(
//                        GeneratorUtils.convertOpenAPITypeToBallerina(schema),
//                        AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
//                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            } else {
                return Optional.empty();
            }
        } else if (schema.getOneOf() != null) {
            UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(schema, null, subTypesMap, pregeneratedTypeMap);
            return Optional.ofNullable(unionTypeGenerator.getUnionNodeForOneOf());
        } else if (schema.getAllOf() != null) {
            AllOfRecordTypeGenerator allOfRecordTypeGenerator = new AllOfRecordTypeGenerator(schema, null, subTypesMap, pregeneratedTypeMap);
            return Optional.ofNullable(allOfRecordTypeGenerator.generateTypeDescriptorNode());
        } else if (schema.getAnyOf() != null) {
            UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(schema, null, subTypesMap, pregeneratedTypeMap);
            return Optional.ofNullable(unionTypeGenerator.generateTypeDescriptorNode());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Create recordType TypeDescriptor.
     */
    public SimpleNameReferenceNode createTypeInclusionRecord(String statusCode, TypeDescriptorNode type, HashMap<String, TypeDefinitionNode> subTypesMap) {
        String recordName = statusCode + GeneratorUtils.getValidName(type.toString(), true);
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

        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.BODY, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        RecordFieldNode recordFieldNode = createRecordFieldNode(
                null, null,
                type,
                fieldName, null,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(recordFieldNode);

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
        subTypesMap.put(typeDefinitionNode.typeName().text(), typeDefinitionNode);
        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
    }

    public ReturnType generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName, HashMap<String, TypeDefinitionNode> subTypesMap) throws BallerinaOpenApiException {
        Optional<TypeDescriptorNode> returnTypeDecNode = generateTypeDescriptorNodeForOASSchema(schema, subTypesMap, new HashMap<>());
        if (returnTypeDecNode.isEmpty()) {
            return new ReturnType(recordName, Optional.of(TypeHandler.getSimpleNameReferenceNode(
                    GeneratorConstants.JSON)), Optional.empty());
        } else if (returnTypeDecNode.get() instanceof RecordTypeDescriptorNode recordNode) {
            if (recordName == null) {
                return new ReturnType(null, returnTypeDecNode, Optional.empty());
            }
            Token rname;
            if (schema.get$ref() != null) {
                rname = createIdentifierToken(GeneratorUtils.getValidName(extractReferenceType(schema.get$ref()), true));
            } else {
                rname = createIdentifierToken(recordName);
            }
            TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD),
                    rname,
                    recordNode,
                    createToken(SEMICOLON_TOKEN));
            return new ReturnType(recordName, Optional.of(recordNode), Optional.of(typeDefinitionNode));
        }
        return new ReturnType(recordName, Optional.of(returnTypeDecNode.get()), Optional.empty());
    }

    public Token getQueryParamTypeToken(Schema<?> schema, HashMap<String, TypeDefinitionNode> subTypesMap) throws BallerinaOpenApiException {
        if (schema instanceof MapSchema) {
            // handle inline record open
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, subTypesMap, new HashMap<>());
            TypeDescriptorNode recordNode = recordTypeGenerator.generateTypeDescriptorNode();
            return createIdentifierToken(recordNode.toSourceCode(),
                    GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE);
        } else {
            return createIdentifierToken(convertOpenAPITypeToBallerina(schema),
                    GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE);
        }
    }

    public RequiredParameterNode getMapJsonParameterNode(IdentifierToken parameterName, Parameter parameter, NodeList<AnnotationNode> annotations) {
        BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.MAP_JSON));
        if (parameter.getRequired()) {
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
        }
        OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        return createRequiredParameterNode(annotations, optionalNode, parameterName);
    }

    public ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(TypeDescriptorNode typeDescriptorNode) {
        return ArrayTypeGenerator.getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
    }
}
