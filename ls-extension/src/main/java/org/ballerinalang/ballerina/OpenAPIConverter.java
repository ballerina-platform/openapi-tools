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
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.ballerinalang.ballerina.util.service.OpenApiEndpointMapper;
import org.ballerinalang.ballerina.util.service.OpenApiServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.ballerinalang.ballerina.util.service.OpenApiEndpointMapper.getServiceBasePath;
/**
 * OpenApi related utility classes.
 */

public class OpenAPIConverter {
    private static final Logger logger = LoggerFactory.getLogger(OpenAPIConverter.class);
    private SyntaxTree syntaxTree;
    private SemanticModel semanticModel;
    private List<ListenerDeclarationNode> endpoints = new ArrayList<>();

    public OpenAPIConverter(SyntaxTree syntaxTree, SemanticModel semanticModel) {
        this.syntaxTree = syntaxTree;
        this.semanticModel = semanticModel;
        this.endpoints = new ArrayList<>();
    }

    public String generateOAS3Definition(SyntaxTree ballerinaSource,  Boolean needJson) {
        // Take service name

        ModulePartNode modulePartNode = ballerinaSource.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a listen_declaration for the server part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.LISTENER_DECLARATION)) {
                ListenerDeclarationNode listener = (ListenerDeclarationNode) node;
                endpoints.add(listener);
            }
            if (syntaxKind.equals(SyntaxKind.ANNOTATION_DECLARATION)) {
                // TO-Do

            } else if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                OpenApiServiceMapper openApiServiceMapper = new OpenApiServiceMapper();
                openApiServiceMapper.setSemanticModel(semanticModel);
                ServiceDeclarationNode serviceDefinition = (ServiceDeclarationNode) node;
                String serviceName = getServiceBasePath(serviceDefinition);
                OpenAPI openapi = getOpenApiDefinition(new OpenAPI(), openApiServiceMapper, serviceName, endpoints);
                if (needJson) {
                    return Json.pretty(openapi);
                }
                return Yaml.pretty(openapi);
            }
        }
        return "Error occurred while generating OAS file.";
    }

    private OpenAPI getOpenApiDefinition(OpenAPI openapi, OpenApiServiceMapper openApiServiceMapper,
                                                String serviceName, List<ListenerDeclarationNode> endpoints) {
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a listen_declaration for the server part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceDefinition = (ServiceDeclarationNode) node;
                //Take base path of service
                String currentServiceName = getServiceBasePath(serviceDefinition);
                if (openapi.getServers() == null) {
                    OpenApiEndpointMapper openApiEndpointMapper = new OpenApiEndpointMapper();
                    openapi = openApiEndpointMapper.convertListenerEndPointToOpenAPI(openapi, endpoints,
                            serviceDefinition);
                    if (openapi.getServers().isEmpty()) {
                        List<Server> servers = new ArrayList<>();
                        Server server = new Server().url(currentServiceName);
                        servers.add(server);
                        openapi.setServers(servers);
                    }
                    // Generate openApi string for the mentioned service name.
                    if (!serviceName.isBlank()) {
                        if (currentServiceName.trim().equals(serviceName)) {
                            openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi,
                                    serviceName);
                        }
                    } else {
                    // If no service name mentioned, then generate openApi definition for the first service.
                    openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi,
                            currentServiceName.trim());
                    }
                }
            }
        }

        return openapi;
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
    private static String checkDuplicateFiles(Path outPath, String openApiName, Boolean isJson) {

        if (Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                openApiName = checkAvailabilityOfGivenName(openApiName, listFiles, isJson);
            }
        }
        return openApiName;
    }

    private static String checkAvailabilityOfGivenName(String openApiName, File[] listFiles, Boolean isJson) {

        for (File file : listFiles) {
            if (file.getName().equals(openApiName)) {
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
     *  This method for setting the file name for generated file.
     * @param listFiles         generated files
     * @param fileName          File name
     * @param duplicateCount    add the tag with duplicate number if file already exist
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, int duplicateCount, Boolean isJson) {
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


    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(filePath.toString(), "UTF-8");
            writer.print(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
