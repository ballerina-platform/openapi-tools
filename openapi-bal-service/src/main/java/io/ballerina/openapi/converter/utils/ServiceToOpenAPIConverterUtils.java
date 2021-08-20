/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.openapi.converter.utils;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.service.OpenAPIEndpointMapper;
import io.ballerina.openapi.converter.service.OpenAPIServiceMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.converter.Constants.SPLIT_PATTERN;

/**
 * The ServiceToOpenAPIConverterUtils provide API for convert ballerina service into openAPI specification.
 *
 * @since 2.0.0
 */
public class ServiceToOpenAPIConverterUtils {

    /**
     * This method will generate  openapi definition Map list with ballerina code.
     *
     * @param syntaxTree  - Syntax tree the related to ballerina service
     * @param semanticModel - Semantic model related to ballerina module
     * @param serviceName - Service name that need to generate the openAPI specification
     * @param needJson    - Flag for enabling the generated file format with json or YAML
     * @param outPath     - Out put path to check given path has same name file
     * @return            - {@link java.util.Map} with openAPI definitions for service nodes
     * @throws OpenApiConverterException when code generation is fail
     */
    public static Map<String, String> generateOAS3Definition(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                             String serviceName, Boolean needJson, Path outPath)
            throws OpenApiConverterException {
        Map<String, String> openAPIDefinitions = new HashMap<>();
        List<ListenerDeclarationNode> endpoints = new ArrayList<>();
        List<ServiceDeclarationNode> servicesToGenerate = new ArrayList<>();
        List<String> availableService = new ArrayList<>();

        if (!semanticModel.diagnostics().isEmpty()) {
            throw new OpenApiConverterException("Given ballerina file has syntax/compilation error.");
        } else {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            extractListenersAndServiceNodes(serviceName, availableService, servicesToGenerate, modulePartNode,
                    endpoints);

            // If there are no services found for a given service name.
            if (serviceName != null && servicesToGenerate.isEmpty()) {
                throw new OpenApiConverterException("No Ballerina services found with name '" + serviceName +
                        "' to generate an OpenAPI specification. These services are " +
                        "available in ballerina file. " + availableService.toString());
            }

            // Generating for the services
            for (ServiceDeclarationNode serviceNode : servicesToGenerate) {
                String serviceNodeName = new OpenAPIEndpointMapper().getServiceBasePath(serviceNode);
                String openApiName = getOpenApiFileName(syntaxTree.filePath(), serviceNodeName, needJson);
                String openApiSource = generateOASForGivenFormat(serviceNode, serviceNodeName, needJson, endpoints,
                        semanticModel, openApiName);
                //  Checked old generated file with same name
                openApiName = checkDuplicateFiles(outPath, openApiName, needJson);
                openAPIDefinitions.put(openApiName, openApiSource);
            }
        }
        return openAPIDefinitions;
    }

    /**
     * Filter all the end points and service nodes.
     */
    private static void extractListenersAndServiceNodes(String serviceName, List<String> availableService,
                                                        List<ServiceDeclarationNode> servicesToGenerate,
                                                        ModulePartNode modulePartNode,
                                                        List<ListenerDeclarationNode> endpoints) {

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
                extractServiceDeclarationNodes(serviceName, availableService, servicesToGenerate, serviceNode);
            }
        }
    }

    /**
     * Filter all the serviceNodes in syntax tree.
     */
    private static void extractServiceDeclarationNodes(String serviceName, List<String> availableService,
                                                List<ServiceDeclarationNode> servicesToGenerate,
                                                ServiceDeclarationNode serviceNode) {

        if (serviceName != null) {
            // Filtering by service name
            String service = new OpenAPIEndpointMapper().getServiceBasePath(serviceNode);
            availableService.add(service);
            if (serviceName.equals(service)) {
                servicesToGenerate.add(serviceNode);
            }
        } else {
            // To generate for all services
            servicesToGenerate.add(serviceNode);
        }
    }

    /**
     * Generate openAPI definition according to the given format JSON or YAML.
     */
    public static String generateOASForGivenFormat(ServiceDeclarationNode serviceDeclarationNode, String serviceName,
                                                    boolean needJson, List<ListenerDeclarationNode> endpoints,
                                                    SemanticModel semanticModel, String openApiName)
            throws OpenApiConverterException {
        OpenAPI openapi = generateOpenAPIDefinition(new OpenAPI(), serviceName, endpoints, serviceDeclarationNode,
                semanticModel);
        if (openapi.getInfo().getTitle() == null) {
            openapi = getInfo(openapi, openApiName);
        }
        if (needJson) {
            return Json.pretty(openapi);
        }
        return Yaml.pretty(openapi);
    }

    /**
     * Generated OpenAPI specification with openAPI object.
     */
    private static OpenAPI generateOpenAPIDefinition(OpenAPI openapi, String serviceName,
                                                     List<ListenerDeclarationNode> endpoints,
                                                     ServiceDeclarationNode serviceDefinition,
                                                     SemanticModel semanticModel)
            throws OpenApiConverterException {
        // Take base path of service
        OpenAPIServiceMapper openAPIServiceMapper = new OpenAPIServiceMapper(semanticModel);
        String currentServiceName = new OpenAPIEndpointMapper().getServiceBasePath(serviceDefinition);
        // 01. Set openAPI inFor section wit details
        openapi = getInfo(openapi, currentServiceName);
        // 02. Filter and set the ServerURLs according to endpoints. Complete the servers section in OAS
        openapi = new OpenAPIEndpointMapper().getServers(openapi, endpoints, serviceDefinition);
        // 03. Filter path and component sections in OAS.
        // Generate openApi string for the mentioned service name.
        if (!serviceName.isBlank() && currentServiceName.trim().equals(serviceName)) {
            openapi = openAPIServiceMapper.convertServiceToOpenAPI(serviceDefinition, openapi, serviceName);
        } else {
            // If no service name mentioned, then generate openApi definition for the first service.
            openapi = openAPIServiceMapper.convertServiceToOpenAPI(serviceDefinition, openapi,
                    currentServiceName.trim());
        }
        return openapi;
    }

    //Set the OAS info section details
    private static OpenAPI getInfo(OpenAPI openapi, String currentServiceName) {

        String[] splits = (currentServiceName.replaceFirst("/", "")).split(SPLIT_PATTERN);
        StringBuilder stringBuilder = new StringBuilder();
        String title = null;
        if (splits.length > 1) {
            for (String piece: splits) {
                if (piece.isBlank()) {
                    continue;
                }
                stringBuilder.append(piece.substring(0, 1).toUpperCase(Locale.ENGLISH) + piece.substring(1));
                stringBuilder.append(" ");
            }
            title = stringBuilder.toString().trim();
        } else if (splits.length == 1 && !splits[0].isBlank()) {
            stringBuilder.append(splits[0].substring(0, 1).toUpperCase(Locale.ENGLISH) + splits[0].substring(1));
            title = stringBuilder.toString().trim();
        }

        openapi.setInfo(new io.swagger.v3.oas.models.info.Info().version("1.0.0").title(title));
        return openapi;
    }

    /**
     * Generate file name with service basePath.
     */
    private static String getOpenApiFileName(String servicePath, String serviceName, boolean isJson) {
        String cleanedServiceName;
        if (serviceName.isBlank() || serviceName.equals("/")) {
            cleanedServiceName = FilenameUtils.removeExtension(servicePath);
        } else {
            // Remove starting path separate if exists
            if (serviceName.startsWith("/")) {
                serviceName = serviceName.substring(1);
            }

            // Replace rest of the path separators with hyphen
            cleanedServiceName = serviceName.replaceAll("/", "_");
        }
        if (isJson) {
            return cleanedServiceName + Constants.OPENAPI_SUFFIX + Constants.JSON_EXTENSION;
        }
        return cleanedServiceName + Constants.OPENAPI_SUFFIX + Constants.YAML_EXTENSION;
    }

    /**
     * This method use for checking the duplicate files.
     *
     * @param outPath       output path for file generated
     * @param openApiName   given file name
     * @return              file name with duplicate number tag
     */
    private static String checkDuplicateFiles(Path outPath, String openApiName, Boolean isJson) {

        if (outPath != null && Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                openApiName = checkAvailabilityOfGivenName(openApiName, listFiles, isJson);
            }
        }
        return openApiName;
    }

    private static String checkAvailabilityOfGivenName(String openApiName, File[] listFiles, Boolean isJson) {

        for (File file : listFiles) {
            if (System.console() != null && file.getName().equals(openApiName)) {
                String userInput = System.console().readLine("There is already a/an " + file.getName() +
                            " in the location. Do you want to override the file? [y/N] ");
                if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                    int duplicateCount = 0;
                    openApiName = setGeneratedFileName(listFiles, openApiName, duplicateCount, isJson);
                }
            }
        }
        return openApiName;
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles         generated files
     * @param fileName          File name
     * @param duplicateCount    add the tag with duplicate number if file already exist
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, int duplicateCount, boolean isJson) {
        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        if (isJson) {
            return fileName.split("\\.")[0] + "." + (duplicateCount) + ".json";
        }
        return fileName.split("\\.")[0] + "." + (duplicateCount) + ".yaml";
    }
}
