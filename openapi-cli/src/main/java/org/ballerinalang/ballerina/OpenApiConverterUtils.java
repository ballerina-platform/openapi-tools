/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.converter.SwaggerConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.ballerina.service.ConverterConstants;
import org.ballerinalang.ballerina.service.OpenApiEndpointMapper;
import org.ballerinalang.ballerina.service.OpenApiServiceMapper;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.openapi.utils.CodegenUtils.writeFile;

/**
 * OpenApi related utility classes.
 */

public class OpenApiConverterUtils {

    private static SyntaxTree syntaxTree;
    private static SemanticModel semanticModel;
    private static Project project;
    private static List<ListenerDeclarationNode> endpoints = new ArrayList<>();


    /**
     * This util for generating files when not available with specific service name.
     * @param servicePath
     * @param outPath
     * @throws IOException
     * @throws OpenApiConverterException
     * @return
     */
    public static void  generateOAS3DefinitionsAllService(Path servicePath, Path outPath, String serviceName)
            throws IOException, OpenApiConverterException {

        // Load project instance for single ballerina file
        project = SingleFileProject.load(servicePath);
        //travers and filter service

        // Take package name for project
        Package packageName = project.currentPackage();
        // Take module instance for traversing the syntax tree
        Module currentModule = packageName.getDefaultModule();
        semanticModel =  currentModule.getCompilation().getSemanticModel();
        Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
        while (documentIterator.hasNext()) {
            DocumentId docId = documentIterator.next();
            Document doc = currentModule.document(docId);
            syntaxTree = doc.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node : modulePartNode.members()) {
                SyntaxKind syntaxKind = node.kind();
                // Load a listen_declaration for the server part in the yaml spec
                if (syntaxKind.equals(SyntaxKind.LISTENER_DECLARATION)) {
                    ListenerDeclarationNode listener = (ListenerDeclarationNode) node;
                    endpoints.add(listener);
                }
                // Load a service Node
                if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                    ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                    NodeList<Node> serviceNameNodes = serviceNode.absoluteResourcePath();
                     String openApiName = getOpenApiFileName(syntaxTree.filePath(), serviceName);
                     String ballerinaSource = readFromFile(servicePath);
                     String openApiSource = generateOAS3Definitions(syntaxTree, serviceName);

                    //  Checked old generated file with same name
                    openApiName = checkDuplicateFiles(outPath, openApiName);
                    writeFile(outPath.resolve(openApiName), openApiSource);
                }
            }
        }
    }

    private static String getOpenApiFileName(String servicePath, String serviceName) {
        String openApiFile;

        if (!serviceName.isBlank()) {
            openApiFile = serviceName + ConverterConstants.OPENAPI_SUFFIX;
        } else {
            openApiFile = servicePath != null ?
                    FilenameUtils.removeExtension(servicePath.toString()) + ConverterConstants.OPENAPI_SUFFIX :
                    null;
        }

        return openApiFile + ConverterConstants.YAML_EXTENSION;
    }

    /**
     *
     * @param ballerinaSource
     * @param serviceName
     * @return
     */
    public static String generateOAS3Definitions(SyntaxTree ballerinaSource, String serviceName)
            throws OpenApiConverterException {
        //travers syntax tree
        //check top level node for get the annotation attachment for openapi

        //If no annotations are defined, assume it's not generated by any command and proceed with
        //just compile to get OpenApi JSON

        ModulePartNode modulePartNode = ballerinaSource.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            if (syntaxKind.equals(SyntaxKind.ANNOTATION_DECLARATION)) {
                // TO-Do

            } else if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                NodeList<ImportDeclarationNode> imports = modulePartNode.imports();

                String httpAlias = getAlias(imports, Constants.BALLERINA_HTTP_PACKAGE_NAME);
                String openApiAlias = getAlias(imports, Constants.OPENAPI_PACKAGE_NAME);
                OpenApiServiceMapper openApiServiceMapper = new OpenApiServiceMapper(httpAlias, openApiAlias);

                Swagger openapi = getOpenApiDefinition(new Swagger(), openApiServiceMapper, serviceName,
                        syntaxTree, endpoints, semanticModel);
                String openApiSource = openApiServiceMapper.generateOpenApiString(openapi);
                SwaggerConverter converter = new SwaggerConverter();
                SwaggerDeserializationResult result = new SwaggerParser().readWithInfo(openApiSource);

                if (result.getMessages().size() > 0) {
                    throw new OpenApiConverterException("Please check the mentioned service is available " +
                            "in the ballerina source, or there content is valid");
                }
                return Yaml.pretty(converter.convert(result).getOpenAPI());
            }
        }
        return serviceName;
    }

    private static String readFromFile(Path servicePath) throws IOException {
        String source = FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
        return source;
    }

    /**
     * Gets the alias for a given module from a bLang file root node.
     *
     * @param imports The root node.
     * @param packageName        The module name.
     * @return The alias.
     */
    private static String getAlias(NodeList<ImportDeclarationNode> imports, String packageName) {

        if ((imports != null) && (!imports.isEmpty())) {
            for (ImportDeclarationNode importNode : imports) {
                SeparatedNodeList<IdentifierToken> tokens = importNode.moduleName();
                Optional<ImportOrgNameNode> orgName = importNode.orgName();
                String packagePath = "";
                if (orgName != null && orgName.isPresent()) {
                    packagePath = orgName.get().toString();
                }
                for (IdentifierToken token : tokens) {
                    packagePath = packagePath + token.text();
                    if (packageName.equals(packagePath)) {
                        return  token.text();
                    }
                }
            }
        }
        return null;
    }

    private static Swagger getOpenApiDefinition(Swagger openapi, OpenApiServiceMapper openApiServiceMapper,
                                                String serviceName, SyntaxTree topCompilationUnit,
                                                List<ListenerDeclarationNode> endpoints, SemanticModel semanticModel) {
        Map<String, Model> definitions = new HashMap<>();

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a listen_declaration for the server part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceDefinition = (ServiceDeclarationNode) node;
                //Take base path of service
                String currentServiceName = getServiceBasePath(serviceDefinition);
                if (openapi.getBasePath() == null) {
                    openapi = new OpenApiEndpointMapper()
                        .convertBoundEndpointsToOpenApi(endpoints, serviceDefinition, openapi);

                    // Generate openApi string for the mentioned service name.
                    if (!serviceName.isBlank()) {
                        if (currentServiceName.equals(serviceName)) {
                        openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi, serviceName);
                        }
                    } else {
                    // If no service name mentioned, then generate openApi definition for the first service.
                    openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi,
                            currentServiceName);
                    }
                }

            } else if (syntaxKind.equals(SyntaxKind.TYPE_DEFINITION)) {
                //Map records into swagger definitions
                TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) node;
                if (typeDefinitionNode.typeDescriptor() instanceof RecordTypeSymbol) {
                    Model model = new ModelImpl();
                    // TODO schema generation
//                    model.setProperties(propertyMap);
//                    definitions.put(typeNode.getName().getValue(), model);
                    openapi.setDefinitions(definitions);
                }
            }
        }

        return openapi;
    }

    public static String getServiceBasePath(ServiceDeclarationNode serviceDefinition) {

        String currentServiceName = "";
        NodeList<Node> serviceNameNodes = serviceDefinition.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName = currentServiceName + serviceBasedPathNode.toString();
        }
        return currentServiceName;
    }

    //need to refactor with project API
    public static Property createOpenApiPropertyForBallerinaField(TypeNode node) {
        Property property = null;
        if (node instanceof BLangArrayType) {
            final BLangArrayType fieldTypeNode = (BLangArrayType) node;
            ArrayProperty arr = new ArrayProperty();
            arr.setItems(mapBallerinaTypes(fieldTypeNode.getElementType()
                    .type.getKind().typeName(), true));
            property = arr;
        } else if (node instanceof BLangBuiltInRefTypeNode) {
            final BLangBuiltInRefTypeNode fieldTypeNode = (BLangBuiltInRefTypeNode) node;
            property = mapBallerinaTypes(fieldTypeNode.typeKind.typeName(), false);
        } else if (node instanceof BLangConstrainedType) {
            //TODO handle constrained types
        } else if (node instanceof BLangErrorType) {
            //TODO Error type is handled as string variables. Need to discuss
            final BLangErrorType fieldTypeNode = (BLangErrorType) node;
            final BType bErrorType = fieldTypeNode.type;
            if (bErrorType instanceof BErrorType) {
                assert false;
//                property = mapBallerinaTypes(((BErrorType) bErrorType)
//                        .getReasonType().getKind().typeName(), false);
            }
        } else if (node instanceof BLangFiniteTypeNode) {
            //TODO handle finite types
        } else if (node instanceof BLangFunctionTypeNode) {
            //TODO handle function types
        } else if (node instanceof BLangObjectTypeNode) {
            //TODO handle object types
        } else if (node instanceof BLangRecordTypeNode) {
            //TODO handle record types
        } else if (node instanceof BLangStructureTypeNode) {
            //TODO handle structure types
        } else if (node instanceof BLangTupleTypeNode) {
            //TODO handle tuple types
        } else if (node instanceof BLangUnionTypeNode) {
            //TODO handle union types
        } else if (node instanceof BLangUserDefinedType) {
            final BLangUserDefinedType fieldTypeNode = (BLangUserDefinedType) node;
            property = mapBallerinaTypes(fieldTypeNode.getTypeName().value, false);
        } else if (node instanceof BLangValueType) {
            final BLangValueType fieldTypeNode = (BLangValueType) node;
            property = mapBallerinaTypes(fieldTypeNode.getTypeKind().typeName(), false);
        }
        return property;
    }

    public static Property mapBallerinaTypes(String type, boolean isArray) {
        switch (type) {
            case "any":
                //TODO handle any type to OpenApi
                return null;
            case "int":
                return new IntegerProperty();
            case "string":
                return new StringProperty();
            case "boolean":
                return new BooleanProperty();
            case "decimal":
                return new DecimalProperty();
            case "byte":
                return new ByteArrayProperty();
            case "float":
                return new FloatProperty();
            case "json":
                //TODO json is mapped to Object property. Will need to handle it properly.
                return new ObjectProperty();
            default:
                //TODO handle unmatched type
                return null;
        }
    }

    /**
     * This method use for checking the duplicate files.
     * @param outPath       output path for file generated
     * @param openApiName   given file name
     * @return              file name with duplicate number tag
     */
    private static String checkDuplicateFiles(Path outPath, String openApiName) {

        if (Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                openApiName = checkAvailabilityOfGivenName(openApiName, listFiles);
            }
        }
        return openApiName;
    }

    private static String checkAvailabilityOfGivenName(String openApiName, File[] listFiles) {

        for (File file : listFiles) {
            if (file.getName().equals(openApiName)) {
                String userInput = System.console().readLine("There is already a/an " + file.getName() +
                        " in the location. Do you want to override the file [Y/N]? ");
                if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                    int duplicateCount = 0;
                    openApiName = setGeneratedFileName(listFiles, openApiName, duplicateCount);
                }
            }
        }
        return openApiName;
    }

    /**
     *  This method for setting the file name for generated file.
     * @param listFiles         generated files
     * @param fileName          File name
     * @param duplicateCount    add the tag with duplicate number if file already exist
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, int duplicateCount) {

        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        return fileName.split("\\.")[0] + "." + (duplicateCount) + ".yaml";
    }
}
