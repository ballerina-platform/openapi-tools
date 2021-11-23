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
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.openapi.converter.model.OpenAPIInfo;
import io.ballerina.openapi.converter.service.OpenAPIEndpointMapper;
import io.ballerina.openapi.converter.service.OpenAPIServiceMapper;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.CONTRACT;
import static io.ballerina.openapi.converter.Constants.JSON_EXTENSION;
import static io.ballerina.openapi.converter.Constants.OPENAPI_ANNOTATION;
import static io.ballerina.openapi.converter.Constants.SLASH;
import static io.ballerina.openapi.converter.Constants.SPECIAL_CHAR_REGEX;
import static io.ballerina.openapi.converter.Constants.TITLE;
import static io.ballerina.openapi.converter.Constants.VERSION;
import static io.ballerina.openapi.converter.Constants.YAML_EXTENSION;

/**
 * The ServiceToOpenAPIConverterUtils provide API for convert ballerina service into openAPI specification.
 *
 * @since 2.0.0
 */
public class ServiceToOpenAPIConverterUtils {

    /**
     * This method will generate  openapi definition Map lists with ballerina code.
     *
     * @param syntaxTree    - Syntax tree the related to ballerina service
     * @param semanticModel - Semantic model related to ballerina module
     * @param serviceName   - Service name that need to generate the openAPI specification
     * @param needJson      - Flag for enabling the generated file format with json or YAML
     * @param outPath       - Out put path to check given path has same named file
     * @param inputPath     - Input file path for resolve the annotation details
     * @return - {@link java.util.Map} with openAPI definitions for service nodes
     */
    public static List<OASResult> generateOAS3Definition(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                         String serviceName, Boolean needJson, Path outPath,
                                                         Path inputPath) {

        List<ListenerDeclarationNode> endpoints = new ArrayList<>();
        List<ServiceDeclarationNode> servicesToGenerate = new ArrayList<>();
        List<String> availableService = new ArrayList<>();
        List<OpenAPIConverterDiagnostic> diagnostics = new ArrayList<>();
        List<OASResult> outputs = new ArrayList<>();
        if (containErrors(semanticModel.diagnostics())) {
            DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_106;
            ExceptionDiagnostic error = new ExceptionDiagnostic(messages.getCode(), messages.getDescription(),
                    null);
            diagnostics.add(error);
        } else {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            extractListenersAndServiceNodes(serviceName, availableService, servicesToGenerate, modulePartNode,
                    endpoints);

            // If there are no services found for a given service name.
            if (serviceName != null && servicesToGenerate.isEmpty()) {
                DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_107;
                ExceptionDiagnostic error = new ExceptionDiagnostic(messages.getCode(), messages.getDescription(),
                        null, serviceName, availableService.toString());
                diagnostics.add(error);
            }
            // Generating for the services
            for (ServiceDeclarationNode serviceNode : servicesToGenerate) {
                String serviceNodeName = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServiceBasePath(serviceNode);
                String openApiName = getOpenApiFileName(syntaxTree.filePath(), serviceNodeName, needJson);
                //  Checked old generated file with same name
                openApiName = checkDuplicateFiles(outPath, openApiName, needJson);
                OASResult oasDefinition = generateOAS(serviceNode, endpoints, semanticModel, openApiName, inputPath);
                oasDefinition.setServiceName(openApiName);
                outputs.add(oasDefinition);
            }
        }
        if (!diagnostics.isEmpty()) {
            OASResult exceptions = new OASResult(null, diagnostics);
            outputs.add(exceptions);
        }
        return outputs;
    }

    private static boolean containErrors(List<Diagnostic> diagnostics) {
        return diagnostics != null && diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR);
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
            String service = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServiceBasePath(serviceNode);
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
     *
     * @deprecated use {@link #generateOAS(ServiceDeclarationNode, List, SemanticModel, String, Path)} instead.
     * The new API provides a list of {@code OASResult}, which contains all the diagnostic information collected
     * while generating the openAPI contract.
     */
    @Deprecated
    public static String generateOASForGivenFormat(ServiceDeclarationNode serviceDeclarationNode,
                                                   boolean needJson, List<ListenerDeclarationNode> endpoints,
                                                   SemanticModel semanticModel, String openApiName) {
        Optional<OpenAPI> openapi = generateOAS(serviceDeclarationNode, endpoints, semanticModel, openApiName,
                null).getOpenAPI();
        if (openapi.isPresent()) {
            OpenAPI openAPI = openapi.get();
            if (openAPI.getInfo().getTitle() == null || openAPI.getInfo().getTitle().equals(SLASH)) {
                openAPI.getInfo().setTitle(normalizeTitle(openApiName));
            }
            return needJson ? Json.pretty(openAPI) : Yaml.pretty(openAPI);
        }
        return "Error while generating openAPI contract";
    }

    /**
     * Provides an instance of {@code OASResult}, which contains the generated contract as well as
     * all the diagnostics information.
     *
     * @param serviceDefinition     Service Node related to ballerina service
     * @param endpoints             Listener endpoints that bind to service
     * @param semanticModel         Semantic model for given ballerina file
     * @param openApiFileName       OpenAPI file name
     * @param ballerinaFilePath     Input ballerina file Path
     * @return {@code OASResult}
     */
    public static OASResult generateOAS(ServiceDeclarationNode serviceDefinition,
                                        List<ListenerDeclarationNode> endpoints, SemanticModel semanticModel,
                                        String openApiFileName, Path ballerinaFilePath) {
        // 01.Fill the openAPI info section
        OASResult oasResult = fillOpenAPIInfoSection(serviceDefinition, semanticModel, openApiFileName,
                ballerinaFilePath);
        if (oasResult.getOpenAPI().isPresent() && oasResult.getDiagnostics().isEmpty()) {
            OpenAPI openapi = oasResult.getOpenAPI().get();
            if (openapi.getPaths() == null) {
                // Take base path of service
                OpenAPIServiceMapper openAPIServiceMapper = new OpenAPIServiceMapper(semanticModel);
                // 02. Filter and set the ServerURLs according to endpoints. Complete the servers section in OAS
                openapi = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServers(openapi, endpoints, serviceDefinition);
                // 03. Filter path and component sections in OAS.
                // Generate openApi string for the mentioned service name.
                openapi = openAPIServiceMapper.convertServiceToOpenAPI(serviceDefinition, openapi);
                return new OASResult(openapi, openAPIServiceMapper.getErrors());
            } else {
                return new OASResult(openapi, oasResult.getDiagnostics());
            }
        } else {
            return oasResult;
        }
    }

    /**
     * Generate file name with service basePath.
     */
    private static String getOpenApiFileName(String servicePath, String serviceName, boolean isJson) {

        String cleanedServiceName;
        if (serviceName.isBlank() || serviceName.equals(SLASH)) {
            cleanedServiceName = FilenameUtils.removeExtension(servicePath);
        } else {
            // Remove starting path separate if exists
            if (serviceName.startsWith(SLASH)) {
                serviceName = serviceName.substring(1);
            }
            // Replace rest of the path separators with hyphen
            cleanedServiceName = serviceName.replaceAll(SLASH, "_");
        }
        if (isJson) {
            return cleanedServiceName + Constants.OPENAPI_SUFFIX + JSON_EXTENSION;
        }
        return cleanedServiceName + Constants.OPENAPI_SUFFIX + YAML_EXTENSION;
    }

    /**
     * This method use for checking the duplicate files.
     *
     * @param outPath     output path for file generated
     * @param openApiName given file name
     * @return file name with duplicate number tag
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
     * @param listFiles      generated files
     * @param fileName       File name
     * @param duplicateCount add the tag with duplicate number if file already exist
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
            return fileName.split("\\.")[0] + "." + (duplicateCount) + JSON_EXTENSION;
        }
        return fileName.split("\\.")[0] + "." + (duplicateCount) + YAML_EXTENSION;
    }

    /**
     * This function is for completing the OpenAPI info section with package details and annotation details.
     *
     * @param serviceNode   Service node for relevant service.
     * @param semanticModel Semantic model for relevant project.
     * @param openapiFileName OpenAPI generated file name.
     * @param ballerinaFilePath Ballerina file path.
     * @return {@code OASResult}
     */
    private static OASResult fillOpenAPIInfoSection(ServiceDeclarationNode serviceNode, SemanticModel semanticModel,
                                                    String openapiFileName, Path ballerinaFilePath) {
        /**
         * First check the given service node has metadata with annotation details with `openapi:serviceInfo`,
         * if it is there, then {@link #parseServiceInfoAnnotationAttachmentDetails(List, AnnotationNode, Path)}
         * function extracts the annotation details and store details in {@code OpenAPIInfo} model using
         * {@link #updateOpenAPIInfoModel(SeparatedNodeList)} function. If the annotation contains the valid contract
         * path then we complete given OpenAPI specification using annotation details. if not we create new OpenAPI
         * specification and fill openAPI info sections.
         * If the annotation is not in the given service, then we filled the OpenAPI specification info section using
         * package details and title with service base path.
         * After completing these two process we normalized the OpenAPI specification by checking all the info
         * details are completed, if in case not completed, we complete empty fields with default values.
         */
        Optional<MetadataNode> metadata = serviceNode.metadata();
        List<OpenAPIConverterDiagnostic> diagnostics = new ArrayList<>();
        OpenAPI openAPI = new OpenAPI();
        String currentServiceName = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServiceBasePath(serviceNode);
        // 01. Set openAPI inFo section wit package details
        String version = getContractVersion(serviceNode, semanticModel);
        if (metadata.isPresent() && !metadata.get().annotations().isEmpty()) {
            MetadataNode metadataNode = metadata.get();
            NodeList<AnnotationNode> annotations = metadataNode.annotations();
            for (AnnotationNode annotation : annotations) {
                if (annotation.annotReference().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode ref = (QualifiedNameReferenceNode) annotation.annotReference();
                    String annotationName = ref.modulePrefix().text() + ":" + ref.identifier().text();
                    if (annotationName.equals(OPENAPI_ANNOTATION)) {
                        OASResult oasResult = parseServiceInfoAnnotationAttachmentDetails(diagnostics, annotation,
                                ballerinaFilePath);
                        return normalizeInfoSection(openapiFileName, currentServiceName, version, oasResult);
                    } else {
                        openAPI.setInfo(new Info().version(version).title(normalizeTitle(currentServiceName)));
                    }
                }
            }
        } else if (currentServiceName.equals(SLASH)) {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(openapiFileName)));
        } else {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(currentServiceName)));
        }

        return new OASResult(openAPI, diagnostics);
    }

    // Finalize the openAPI info section
    private static OASResult normalizeInfoSection(String openapiFileName, String currentServiceName, String version,
                                          OASResult oasResult) {
        if (oasResult.getOpenAPI().isPresent()) {
            OpenAPI openAPI = oasResult.getOpenAPI().get();
            if (openAPI.getInfo() == null) {
                String title = normalizeTitle(currentServiceName);
                if (currentServiceName.equals(SLASH)) {
                    title = normalizeTitle(openapiFileName);
                }
                openAPI.setInfo(new Info().title(title).version(version));
            } else {
                if (openAPI.getInfo().getTitle() == null) {
                    openAPI.getInfo().setTitle(normalizeTitle(currentServiceName));
                } else if (openAPI.getInfo().getTitle() != null && openAPI.getInfo().getTitle().equals(SLASH)) {
                    openAPI.getInfo().setTitle(normalizeTitle(openapiFileName));
                } else if (openAPI.getInfo().getTitle().isBlank()) {
                    openAPI.getInfo().setTitle(normalizeTitle(currentServiceName));
                } else if (openAPI.getInfo().getTitle() == null && currentServiceName.equals(SLASH)) {
                    openAPI.getInfo().setTitle(normalizeTitle(openapiFileName));
                }
                if (openAPI.getInfo().getVersion() == null || openAPI.getInfo().getVersion().isBlank()) {
                    openAPI.getInfo().setVersion(version);
                }
            }
            return new OASResult(openAPI, oasResult.getDiagnostics());
        } else {
            return oasResult;
        }
    }

    // Set contract version by default using package version.
    private static String getContractVersion(ServiceDeclarationNode serviceDefinition, SemanticModel semanticModel) {
        Optional<Symbol> symbol = semanticModel.symbol(serviceDefinition);
        String version = "1.0.0";
        if (symbol.isPresent()) {
            Symbol serviceSymbol = symbol.get();
            Optional<ModuleSymbol> module = serviceSymbol.getModule();
            if (module.isPresent()) {
                version = module.get().id().version();
            }
        }
        return version;
    }

    private static String normalizeTitle(String title) {
        if (title != null) {
            String[] splits = (title.replaceFirst(SLASH, "")).split(SPECIAL_CHAR_REGEX);
            StringBuilder stringBuilder = new StringBuilder();
            if (splits.length > 1) {
                for (String piece : splits) {
                    if (piece.isBlank()) {
                        continue;
                    }
                    stringBuilder.append(piece.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(piece.substring(1));
                    stringBuilder.append(" ");
                }
                title = stringBuilder.toString().trim();
            } else if (splits.length == 1 && !splits[0].isBlank()) {
                stringBuilder.append(splits[0].substring(0, 1).toUpperCase(Locale.ENGLISH))
                        .append(splits[0].substring(1));
                title = stringBuilder.toString().trim();
            }
            return title;
        }
        return null;
    }

    // Set annotation details  for info section.
    private static OASResult parseServiceInfoAnnotationAttachmentDetails(List<OpenAPIConverterDiagnostic> diagnostics,
                                                                         AnnotationNode annotation,
                                                                         Path ballerinaFilePath) {
        Location location = annotation.location();
        OpenAPI openAPI = new OpenAPI();
        Optional<MappingConstructorExpressionNode> content = annotation.annotValue();
        // If contract path there
        if (content.isPresent()) {
           SeparatedNodeList<MappingFieldNode> fields = content.get().fields();
           if (!fields.isEmpty()) {
               OpenAPIInfo openAPIInfo = updateOpenAPIInfoModel(fields);
               /**
                * If in case ballerina file path is getting null, then openAPI specification will be generated for given
                * services.
                */
               if (openAPIInfo.getContractPath().isPresent() && ballerinaFilePath != null) {
                   return updateExistingContractOpenAPI(diagnostics, location, openAPIInfo, ballerinaFilePath);
               } else if (openAPIInfo.getTitle().isPresent() && openAPIInfo.getVersion().isPresent()) {
                   openAPI.setInfo(new Info().version(openAPIInfo.getVersion().get()).title(normalizeTitle
                           (openAPIInfo.getTitle().get())));
               } else if (openAPIInfo.getVersion().isPresent()) {
                   openAPI.setInfo(new Info().version(openAPIInfo.getVersion().get()));
               } else if (openAPIInfo.getTitle().isPresent()) {
                   openAPI.setInfo(new Info().title(normalizeTitle(openAPIInfo.getTitle().get())));
               }
           }
        }
        return new OASResult(openAPI, diagnostics);
    }

    private static OASResult updateExistingContractOpenAPI(List<OpenAPIConverterDiagnostic> diagnostics,
                                                           Location location, OpenAPIInfo openAPIInfo,
                                                           Path ballerinaFilePath) {

        OASResult oasResult = resolveContractPath(diagnostics, location, openAPIInfo, ballerinaFilePath);
        Optional<OpenAPI> contract = oasResult.getOpenAPI();
        if (contract.isEmpty()) {
            return oasResult;
        }
        OpenAPI openAPI = contract.get();
        if (openAPIInfo.getVersion().isPresent() && openAPIInfo.getTitle().isPresent()) {
            // read the openapi
            openAPI.getInfo().setVersion(openAPIInfo.getVersion().get());
            openAPI.getInfo().setTitle(openAPIInfo.getTitle().get());
            diagnostics.addAll(oasResult.getDiagnostics());
            return new OASResult(openAPI, oasResult.getDiagnostics());
        } else if (openAPIInfo.getTitle().isPresent()) {
            openAPI.getInfo().setTitle(openAPIInfo.getTitle().get());
            return new OASResult(openAPI, oasResult.getDiagnostics());
        } else if (openAPIInfo.getVersion().isPresent()) {
            openAPI.getInfo().setVersion(openAPIInfo.getVersion().get());
            return new OASResult(openAPI, oasResult.getDiagnostics());
        } else {
            return oasResult;
        }
    }

    private static OpenAPIInfo updateOpenAPIInfoModel(SeparatedNodeList<MappingFieldNode> fields) {
        OpenAPIInfo.OpenAPIInfoBuilder infoBuilder = new OpenAPIInfo.OpenAPIInfoBuilder();
        for (MappingFieldNode field: fields) {
            String fieldName = ((SpecificFieldNode) field).fieldName().toString().trim();
            Optional<ExpressionNode> value = ((SpecificFieldNode) field).valueExpr();
            String fieldValue;
            if (value.isPresent()) {
                ExpressionNode expressionNode = value.get();
                if (!expressionNode.toString().trim().isBlank()) {
                    fieldValue = expressionNode.toString().trim().replaceAll("\"", "");
                    if (!fieldValue.isBlank()) {
                        switch (fieldName) {
                            case CONTRACT:
                                infoBuilder.contractPath(fieldValue);
                                break;
                            case TITLE:
                                infoBuilder.title(fieldValue);
                                break;
                            case VERSION:
                                infoBuilder.version(fieldValue);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        return infoBuilder.build();
    }
    private static OASResult resolveContractPath(List<OpenAPIConverterDiagnostic> diagnostics, Location location,
                                     OpenAPIInfo openAPIInfo, Path ballerinaFilePath) {
        OASResult oasResult;
        OpenAPI openAPI = null;
        Path openapiPath = Paths.get(openAPIInfo.getContractPath().get().replaceAll("\"", "").trim());
        Path relativePath = null;
        if (openapiPath.toString().isBlank()) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_110;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                    error.getDescription(), location);
            diagnostics.add(diagnostic);
        } else if (Paths.get(openapiPath.toString()).isAbsolute()) {
            relativePath = Paths.get(openapiPath.toString());
        } else {
            File file = new File(ballerinaFilePath.toString());
            File parentFolder = new File(file.getParent());
            File openapiContract = new File(parentFolder, openapiPath.toString());
            try {
                relativePath = Paths.get(openapiContract.getCanonicalPath());
            } catch (IOException e) {
                DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_108;
                ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode()
                        , error.getDescription(), location, e.toString());
                diagnostics.add(diagnostic);
            }
        }
        if (relativePath != null && Files.exists(relativePath)) {
            oasResult = ConverterCommonUtils.parseOpenAPIFile(relativePath.toString());
            if (oasResult.getOpenAPI().isPresent()) {
                openAPI = oasResult.getOpenAPI().get();
            }
            diagnostics.addAll(oasResult.getDiagnostics());
        }
        return new OASResult(openAPI, diagnostics);
    }
}
