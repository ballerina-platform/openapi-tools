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

package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
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
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OASGenerationMetaInfo;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.openapi.service.mapper.model.OpenAPIInfo;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.CONTRACT;
import static io.ballerina.openapi.service.mapper.Constants.HYPHEN;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_ANNOTATION;
import static io.ballerina.openapi.service.mapper.Constants.SLASH;
import static io.ballerina.openapi.service.mapper.Constants.SPECIAL_CHAR_REGEX;
import static io.ballerina.openapi.service.mapper.Constants.TITLE;
import static io.ballerina.openapi.service.mapper.Constants.VERSION;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.containErrors;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOpenApiFileName;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.isHttpService;

/**
 * The ServiceToOpenAPIConverterUtils provide API for convert ballerina service into openAPI specification.
 *
 * @since 2.0.0
 */
public class ServiceToOpenAPIMapper {

    /**
     * This method will generate  openapi definition Map lists with ballerina code.
     *
     * @param syntaxTree    - Syntax tree the related to ballerina service
     * @param semanticModel - Semantic model related to ballerina module
     * @param serviceName   - Service name that need to generate the openAPI specification
     * @param needJson      - Flag for enabling the generated file format with json or YAML
     * @param inputPath     - Input file path for resolve the annotation details
     * @return - {@link java.util.Map} with openAPI definitions for service nodes
     */
    public static List<OASResult> generateOAS3Definition(Project project, SyntaxTree syntaxTree,
                                                         SemanticModel semanticModel,
                                                         String serviceName, Boolean needJson,
                                                         Path inputPath) {
        Map<String, ServiceDeclarationNode> servicesToGenerate = new HashMap<>();
        List<String> availableService = new ArrayList<>();
        List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
        List<OASResult> outputs = new ArrayList<>();
        if (containErrors(semanticModel.diagnostics())) {
            DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_106;
            ExceptionDiagnostic error = new ExceptionDiagnostic(messages.getCode(), messages.getDescription(),
                    null);
            diagnostics.add(error);
        } else {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            extractServiceNodes(serviceName, availableService, servicesToGenerate, modulePartNode, semanticModel);
            // If there are no services found for a given mapper name.
            if (serviceName != null && servicesToGenerate.isEmpty()) {
                DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_107;
                ExceptionDiagnostic error = new ExceptionDiagnostic(messages.getCode(), messages.getDescription(),
                        null, serviceName, availableService.toString());
                diagnostics.add(error);
            }
            // Generating openapi specification for selected services
            for (Map.Entry<String, ServiceDeclarationNode> serviceNode : servicesToGenerate.entrySet()) {
                String openApiName = getOpenApiFileName(syntaxTree.filePath(), serviceNode.getKey(), needJson);
                OASGenerationMetaInfo.OASGenerationMetaInfoBuilder builder =
                        new OASGenerationMetaInfo.OASGenerationMetaInfoBuilder();
                builder.setServiceDeclarationNode(serviceNode.getValue())
                        .setSemanticModel(semanticModel)
                        .setOpenApiFileName(openApiName)
                        .setBallerinaFilePath(inputPath)
                        .setProject(project);
                OASGenerationMetaInfo oasGenerationMetaInfo = builder.build();
                OASResult oasDefinition = generateOAS(oasGenerationMetaInfo);
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

    /**
     * Filter all the end points and service nodes.
     */
    private static void extractServiceNodes(String serviceName, List<String> availableService,
                                            Map<String, ServiceDeclarationNode> servicesToGenerate,
                                            ModulePartNode modulePartNode, SemanticModel semanticModel) {
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                if (isHttpService(serviceNode, semanticModel)) {
                    // Here check the service is related to the http
                    // module by checking listener type that attached to service endpoints.
                    Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceNode);
                    if (serviceSymbol.isPresent() && serviceSymbol.get() instanceof ServiceDeclarationSymbol) {
                        String service = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServiceBasePath(serviceNode);
                        String updateServiceName = service;
                        //`String updateServiceName` used to track the service
                        // name for service file contains multiple service node.
                        //example:
                        //<pre>
                        //    listener http:Listener ep1 = new (443, config = {host: "pets-tore.swagger.io"});
                        //    service /hello on ep1 {
                        //        resource function post hi(@http:Payload json payload) {
                        //       }
                        //    }
                        //    service /hello on new http:Listener(9090) {
                        //        resource function get hi() {
                        //        }
                        //    }
                        //</pre>
                        // Using absolute path we generate file name, therefore having same name may overwrite
                        // the file, due to this suppose to use hashcode as identity factor for the file name.
                        // Generated file name for above example -> hello_openapi.yaml, hello_45673_openapi
                        //.yaml
                        if (servicesToGenerate.containsKey(service)) {
                            updateServiceName = service + HYPHEN + serviceSymbol.get().hashCode();
                        }
                        if (serviceName != null) {
                            // Filtering by service name
                            availableService.add(service);
                            if (serviceName.equals(service)) {
                                servicesToGenerate.put(updateServiceName, serviceNode);
                            }
                        } else {
                            // To generate for all services
                            servicesToGenerate.put(updateServiceName, serviceNode);
                        }
                    }
                }
            }
        }
    }

    /**
     * Provides an instance of {@code OASResult}, which contains the generated contract as well as
     * all the diagnostics information.
     *
     * @param oasGenerationMetaInfo    Includes the service definition node, endpoints, semantic model, openapi file
     *                                 name and ballerina file path
     * @return {@code OASResult}
     */
    public static OASResult generateOAS(OASGenerationMetaInfo oasGenerationMetaInfo) {
        ServiceDeclarationNode serviceDefinition = oasGenerationMetaInfo.getServiceDeclarationNode();
        ModuleMemberVisitor moduleMemberVisitor = extractNodesFromProject(oasGenerationMetaInfo.getProject());
        Set<ListenerDeclarationNode> listeners = moduleMemberVisitor.getListenerDeclarationNodes();
        SemanticModel semanticModel = oasGenerationMetaInfo.getSemanticModel();
        String openApiFileName = oasGenerationMetaInfo.getOpenApiFileName();
        Path ballerinaFilePath = oasGenerationMetaInfo.getBallerinaFilePath();
        // 01.Fill the openAPI info section
        OASResult oasResult = fillOpenAPIInfoSection(serviceDefinition, semanticModel, openApiFileName,
                ballerinaFilePath);
        if (oasResult.getOpenAPI().isPresent() && oasResult.getDiagnostics().isEmpty()) {
            OpenAPI openapi = oasResult.getOpenAPI().get();
            if (openapi.getPaths() == null) {
                // Take base path of service
                OpenAPIServiceMapper openAPIServiceMapper = new OpenAPIServiceMapper(serviceDefinition, openapi,
                        semanticModel, moduleMemberVisitor);
                // 02. Filter and set the ServerURLs according to endpoints. Complete the server section in OAS
                openapi = OpenAPIEndpointMapper.ENDPOINT_MAPPER.getServers(openapi, listeners, serviceDefinition);
                // 03. Filter path and component sections in OAS.
                // Generate openApi string for the mentioned service name.
                openAPIServiceMapper.convertServiceToOpenAPI();
                return new OASResult(openapi, openAPIServiceMapper.getDiagnostics());
            } else {
                return new OASResult(openapi, oasResult.getDiagnostics());
            }
        } else {
            return oasResult;
        }
    }

    /**
     * This function is for completing the OpenAPI info section with package details and annotation details.
     *
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
     *
     * @param serviceNode   Service node for relevant service.
     * @param semanticModel Semantic model for relevant project.
     * @param openapiFileName OpenAPI generated file name.
     * @param ballerinaFilePath Ballerina file path.
     * @return {@code OASResult}
     */
    private static OASResult fillOpenAPIInfoSection(ServiceDeclarationNode serviceNode, SemanticModel semanticModel,
                                                    String openapiFileName, Path ballerinaFilePath) {
        Optional<MetadataNode> metadata = serviceNode.metadata();
        List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
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
                        setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
                    }
                } else {
                    setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
                }
            }
        } else {
            setInfoDetailsIfServiceNameAbsent(openapiFileName, openAPI, currentServiceName, version);
        }

        return new OASResult(openAPI, diagnostics);
    }

    /**
     * Generates openAPI Info section when the service base path is absent or `/`.
     */
    private static void setInfoDetailsIfServiceNameAbsent(String openapiFileName, OpenAPI openAPI,
                                                          String currentServiceName, String version) {
        if (currentServiceName.equals(SLASH) || currentServiceName.isBlank()) {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(openapiFileName)));
        } else {
            openAPI.setInfo(new Info().version(version).title(normalizeTitle(currentServiceName)));
        }
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
    private static OASResult parseServiceInfoAnnotationAttachmentDetails(List<OpenAPIMapperDiagnostic> diagnostics,
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
               // If in case ballerina file path is getting null, then openAPI specification will be generated for
               // given services.
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

    private static OASResult updateExistingContractOpenAPI(List<OpenAPIMapperDiagnostic> diagnostics,
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
    private static OASResult resolveContractPath(List<OpenAPIMapperDiagnostic> diagnostics, Location location,
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
        } else {
            Path path = Paths.get(openapiPath.toString());
            if (path.isAbsolute()) {
                relativePath = path;
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
        }
        if (relativePath != null && Files.exists(relativePath)) {
            oasResult = MapperCommonUtils.parseOpenAPIFile(relativePath.toString());
            if (oasResult.getOpenAPI().isPresent()) {
                openAPI = oasResult.getOpenAPI().get();
            }
            diagnostics.addAll(oasResult.getDiagnostics());
        }
        return new OASResult(openAPI, diagnostics);
    }

    /**
     * Travers every syntax tree and collect all the listener nodes.
     *
     * @param project - current project
     */
    public static ModuleMemberVisitor extractNodesFromProject(Project project) {
        ModuleMemberVisitor balNodeVisitor = new ModuleMemberVisitor();
        project.currentPackage().moduleIds().forEach(moduleId -> {
            Module module = project.currentPackage().module(moduleId);
            module.documentIds().forEach(documentId -> {
                SyntaxTree syntaxTreeDoc = module.document(documentId).syntaxTree();
                syntaxTreeDoc.rootNode().accept(balNodeVisitor);
            });
        });
        return balNodeVisitor;
    }
}
