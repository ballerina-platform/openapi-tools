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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.generators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.ArrayTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.PrimitiveTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.RecordTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.TypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.UnionTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.CONNECTION_CONFIG;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorConstants.HTTP;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getValidName;
import static io.ballerina.openapi.corenew.typegenerator.generators.ArrayTypeGenerator.getTypeDescNodeForArraySchema;
import static io.ballerina.openapi.corenew.typegenerator.generators.UnionTypeGenerator.getUnionNodeForOneOf;

/**
 * This class wraps the {@link Schema} from openapi models inorder to overcome complications
 * while populating syntax tree.
 *
 * @since 1.3.0
 */
public class BallerinaTypesGenerator {

    private final List<TypeDefinitionNode> typeDefinitionNodeList;
    private final Set<String> imports = new LinkedHashSet<>();

    private static final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();
    private static final List<String> queryParamSupportedTypes =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN, GeneratorConstants.OBJECT));


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
        this.typeDefinitionNodeList = typeDefinitionNodeList;
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
        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }

    public List<TypeDefinitionNode> generateTypeDefinitionNode(Schema typeSchema, String typeName) throws BallerinaOpenApiException {
        List<TypeDefinitionNode> typeDefinitionNodeListForSchema = new ArrayList<>();
        if (GeneratorUtils.isValidSchemaName(typeName)) {
            typeDefinitionNodeListForSchema.add(getTypeDefinitionNode(typeSchema, typeName));
        }
        return typeDefinitionNodeListForSchema;
    }

    /**
     * Generate syntaxTree for component schema.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
        List<TypeDefinitionNode> typeDefinitionNodeListForSchema = new ArrayList<>();
        if (openAPI.getComponents() != null) {
            // Create typeDefinitionNode
            Components components = openAPI.getComponents();
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
                    String schemaKey = schema.getKey().trim();
                    if (GeneratorUtils.isValidSchemaName(schemaKey)) {
                        typeDefinitionNodeListForSchema.add(getTypeDefinitionNode(schema.getValue(), schemaKey));
                    }
                }
            }
        }
        //Create imports for the http module, when record has http type inclusions.
        NodeList<ImportDeclarationNode> imports = generateImportNodes();
        typeDefinitionNodeList.addAll(typeDefinitionNodeListForSchema);
        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                typeDefinitionNodeList.toArray(new TypeDefinitionNode[typeDefinitionNodeList.size()]));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private NodeList<ImportDeclarationNode> generateImportNodes() {
        Set<ImportDeclarationNode> importDeclarationNodes = new LinkedHashSet<>();
        // Imports for the http module, when record has http type inclusions.
        if (!typeDefinitionNodeList.isEmpty()) {
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
        for (TypeDefinitionNode node : typeDefinitionNodeList) {
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
    public TypeDefinitionNode getTypeDefinitionNode(Schema schema, String typeName)
            throws BallerinaOpenApiException {
        IdentifierToken typeNameToken = AbstractNodeFactory.createIdentifierToken(GeneratorUtils.getValidName(
                typeName.trim(), true));
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, GeneratorUtils.getValidName(
                typeName.trim(), true), null);
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
        if (typeGenerator instanceof ArrayTypeGenerator && !typeGenerator.getTypeDefinitionNodeList().isEmpty()) {
            typeDefinitionNodeList.addAll(typeGenerator.getTypeDefinitionNodeList());
        } else if (typeGenerator instanceof RecordTypeGenerator &&
                !typeGenerator.getTypeDefinitionNodeList().isEmpty()) {
            removeDuplicateNode(typeGenerator.getTypeDefinitionNodeList());
        } else if (typeGenerator instanceof AllOfRecordTypeGenerator &&
                !typeGenerator.getTypeDefinitionNodeList().isEmpty()) {
            removeDuplicateNode(typeGenerator.getTypeDefinitionNodeList());
        } else if (typeGenerator instanceof UnionTypeGenerator &&
                !typeGenerator.getTypeDefinitionNodeList().isEmpty()) {
            removeDuplicateNode(typeGenerator.getTypeDefinitionNodeList());
        }
        imports.addAll(typeGenerator.getImports());
        return typeDefinitionNode;
    }

    /**
     * Remove duplicate of the TypeDefinitionNode.
     */
    private void removeDuplicateNode(List<TypeDefinitionNode> newConstraintNode) {

        for (TypeDefinitionNode newNode : newConstraintNode) {
            boolean isExist = false;
            for (TypeDefinitionNode oldNode : typeDefinitionNodeList) {
                if (newNode.typeName().text().equals(oldNode.typeName().text())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                typeDefinitionNodeList.add(newNode);
            }
        }
    }

    public static SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return createSimpleNameReferenceNode(createIdentifierToken(name));
    }

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        Token modulePrefixToken = AbstractNodeFactory.createIdentifierToken(modulePrefix);
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
    }

    /**
     * Generate typeDescriptor for given schema.
     */
    public static Optional<TypeDescriptorNode> generateTypeDescNodeForOASSchema(Schema<?> schema)
            throws BallerinaOpenApiException {
        if (schema == null) {
            return Optional.empty();
        }

        if (schema.get$ref() != null) {
            String schemaName = GeneratorUtils.getValidName(extractReferenceType(schema.get$ref()), true);
            return Optional.ofNullable(getSimpleNameReferenceNode(schemaName));
        } else if (GeneratorUtils.isMapSchema(schema)) {
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null);
            TypeDescriptorNode record = recordTypeGenerator.generateTypeDescriptorNode();
            return Optional.ofNullable(record);
        } else if (GeneratorUtils.getOpenAPIType(schema) != null) {
            String schemaType = GeneratorUtils.getOpenAPIType(schema);
            boolean isPrimitiveType = schemaType.equals(GeneratorConstants.INTEGER) || schemaType.equals(GeneratorConstants.NUMBER) ||
                    schemaType.equals(GeneratorConstants.BOOLEAN) || schemaType.equals(GeneratorConstants.STRING);
            if (GeneratorUtils.isArraySchema(schema)) {
                return getTypeDescNodeForArraySchema(schema);
            } else if (isPrimitiveType) {
                PrimitiveTypeGenerator primitiveTypeGenerator = new PrimitiveTypeGenerator(schema, null);
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
            Iterator<Schema> iterator = schema.getOneOf().iterator();
            return Optional.ofNullable(getUnionNodeForOneOf(iterator));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Create recordType TypeDescriptor.
     */
    public static SimpleNameReferenceNode createReturnTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {

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

        typeInclusionRecords.put(recordName, typeDefinitionNode);

        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
    }

    /**
     * Generate TypeDescriptor for all the mediaTypes.
     *
     * Here return the @code{ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode>} for return type.
     * Left node(key) of the return tuple represents the return type node for given mediaType details, Right Node
     * (Value) of the return tuple represents the newly generated TypeDefinitionNode for return type if it has inline
     * objects.
     */
    public static ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> generateTypeDescriptorForMediaTypes(
            Map.Entry<String, MediaType> mediaType, String recordName) throws BallerinaOpenApiException {
        String mediaTypeContent = selectMediaType(mediaType.getKey().trim());

        MediaType value = mediaType.getValue();
        Schema<?> schema = value.getSchema();
        switch (mediaTypeContent) {
            case GeneratorConstants.APPLICATION_JSON:
                Optional<TypeDescriptorNode> returnTypeDecNode = generateTypeDescNodeForOASSchema(schema);
                if (returnTypeDecNode.isEmpty()) {
                    return ImmutablePair.of(Optional.ofNullable(TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.JSON)),
                            Optional.empty());
                } else if (returnTypeDecNode.get() instanceof RecordTypeDescriptorNode recordNode) {
                    if (recordName == null) {
                        return ImmutablePair.of(returnTypeDecNode, Optional.empty());
                    }
                    TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                            createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD),
                            createIdentifierToken(recordName),
                            recordNode,
                            createToken(SEMICOLON_TOKEN));
                    return ImmutablePair.of(returnTypeDecNode, Optional.of(typeDefinitionNode));
                } else {
                    return ImmutablePair.of(returnTypeDecNode, Optional.empty());
                }
            case GeneratorConstants.APPLICATION_XML:
                return ImmutablePair.of(Optional.ofNullable(getSimpleNameReferenceNode(GeneratorConstants.XML)),
                        Optional.empty());
            case GeneratorConstants.APPLICATION_URL_ENCODE:
                return ImmutablePair.of(Optional.ofNullable(TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.MAP_STRING)),
                        Optional.empty());
            case GeneratorConstants.TEXT:
                return ImmutablePair.of(Optional.ofNullable(TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.STRING)),
                        Optional.empty());
            case GeneratorConstants.APPLICATION_OCTET_STREAM:
                ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                return ImmutablePair.of(Optional.ofNullable(
                        createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                                        createIdentifierToken(GeneratorConstants.BYTE)),
                                NodeFactory.createNodeList(dimensionNode))), Optional.empty());
            default:
                return ImmutablePair.of(Optional.empty(), Optional.empty());
        }
    }

    public static TypeDescriptorNode getReturnNodeForSchemaType(Set<Map.Entry<String, MediaType>> contentEntries, String recordName)
            throws BallerinaOpenApiException {
        TypeDescriptorNode returnNode = null;
        for (Map.Entry<String, MediaType> next : contentEntries) {
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>>
                    mediaTypeToken = generateTypeDescriptorForMediaTypes(next, recordName);
            // right node represents the newly generated node for if there is an inline record in the returned
            // tuple.
            Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;

            if (rightNode.isPresent()) {
                typeInclusionRecords.put(recordName, rightNode.get());
                returnNode = createSimpleNameReferenceNode(createIdentifierToken(recordName));
            } else {
                returnNode = mediaTypeToken.left.orElseGet(
                        () -> createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(GeneratorConstants.ANYDATA)));
            }
        }
        return returnNode;
    }

    /**
     * This util function is for generating type node for request payload in resource function.
     */
    public static Optional<TypeDescriptorNode> getNodeForPayloadType(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {

        Optional<TypeDescriptorNode> typeName;
        if (mediaType.getValue() != null && mediaType.getValue().getSchema() != null &&
                mediaType.getValue().getSchema().get$ref() != null) {
            String reference = mediaType.getValue().getSchema().get$ref();
            String schemaName = GeneratorUtils.getValidName(extractReferenceType(reference), true);
            String mediaTypeContent = selectMediaType(mediaType.getKey().trim());
            IdentifierToken identifierToken;
            switch (mediaTypeContent) {
                case GeneratorConstants.APPLICATION_XML:
                    identifierToken = createIdentifierToken(GeneratorConstants.XML);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    break;
                case GeneratorConstants.TEXT:
                    identifierToken = createIdentifierToken(schemaName);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    break;
                case GeneratorConstants.APPLICATION_OCTET_STREAM:
                    ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                            createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                    typeName = Optional.ofNullable(createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(
                                    null, createIdentifierToken(GeneratorConstants.BYTE)),
                            NodeFactory.createNodeList(dimensionNode)));
                    break;
                case GeneratorConstants.APPLICATION_JSON:
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(schemaName)));
                    break;
                case GeneratorConstants.APPLICATION_URL_ENCODE:
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.MAP_STRING)));
                    //Commented due to the data binding issue in the ballerina http module
                    //TODO: Related issue:https://github.com/ballerina-platform/ballerina-standard-library/issues/4090
//                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(
//                            GeneratorUtils.getValidName(schemaName, true))));
                    break;
                default:
                    ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeTokens =
                            generateTypeDescriptorForMediaTypes(mediaType, null);
                    if (mediaTypeTokens.getLeft().isPresent()) {
                        typeName = mediaTypeTokens.getLeft();
                    } else {
                        identifierToken = createIdentifierToken(GeneratorConstants.HTTP_REQUEST);
                        typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    }
            }
        } else {
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeTokens =
                    generateTypeDescriptorForMediaTypes(mediaType, null);
            typeName = mediaTypeTokens.left;
        }
        return typeName;
    }

    // Create ArrayTypeDescriptorNode using Schema
    public static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(OpenAPI openAPI, Schema<?> items) throws BallerinaOpenApiException {
        String arrayName;
        if (items.get$ref() != null) {
            String referenceType = extractReferenceType(items.get$ref());
            String type = getValidName(referenceType, true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
            if (queryParamSupportedTypes.contains(getOpenAPIType(refSchema))) {
                arrayName = type;
            } else {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), type));
            }
        } else {
            arrayName = convertOpenAPITypeToBallerina(items);
            if (items.getEnum() != null && !items.getEnum().isEmpty()) {
                arrayName = OPEN_PAREN_TOKEN.stringValue() + arrayName + CLOSE_PAREN_TOKEN.stringValue();
            }
        }
        Token arrayNameToken = createIdentifierToken(arrayName, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        BuiltinSimpleNameReferenceNode memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, arrayNameToken);
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);

        if (items.getNullable() != null && items.getNullable() && items.getEnum() == null) {
            // generate -> int?[]
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(memberTypeDesc,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createArrayTypeDescriptorNode(optionalNode, nodeList);
        }
        // generate -> int[]
        return createArrayTypeDescriptorNode(memberTypeDesc, nodeList);
    }

    public static Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        if (schema instanceof MapSchema) {
            // handle inline record open
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null);
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

    /**
     *
     * This util is used for selecting standard media type by looking at the user defined media type.
     */
    private static String selectMediaType(String mediaTypeContent) {
        if (mediaTypeContent.matches("application/.*\\+json") || mediaTypeContent.matches(".*/json")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_JSON;
        } else if (mediaTypeContent.matches("application/.*\\+xml") || mediaTypeContent.matches(".*/xml")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_XML;
        } else if (mediaTypeContent.matches("text/.*")) {
            mediaTypeContent = GeneratorConstants.TEXT;
        }  else if (mediaTypeContent.matches("application/.*\\+octet-stream")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_OCTET_STREAM;
        } else if (mediaTypeContent.matches("application/.*\\+x-www-form-urlencoded")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_URL_ENCODE;
        }
        return mediaTypeContent;
    }
}
