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
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapperInterface;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OASGenerationMetaInfo;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.swagger.v3.oas.models.OpenAPI;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.HYPHEN;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.containErrors;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractServiceAnnotationDetails;
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
                        String service = ServersMapper.getServiceBasePath(serviceNode);
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
        OASResult oasResult = InfoMapper.getOASResultWithInfo(serviceDefinition, semanticModel,
                openApiFileName, ballerinaFilePath);
        if (oasResult.getOpenAPI().isPresent() && oasResult.getDiagnostics().isEmpty()) {
            OpenAPI openapi = oasResult.getOpenAPI().get();
            List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
            if (openapi.getPaths() == null) {
                // Take base path of service
                // 02. Filter and set the ServerURLs according to endpoints. Complete the server section in OAS
                ServersMapper serversMapper = new ServersMapper(openapi, listeners, serviceDefinition);
                serversMapper.setServers();
                // 03. Filter path and component sections in OAS.
                // Generate openApi string for the mentioned service name.
                convertServiceToOpenAPI(serviceDefinition, openapi, semanticModel, moduleMemberVisitor, diagnostics);
                ConstraintMapperInterface constraintMapper = new ConstraintMapper(openapi, moduleMemberVisitor,
                        diagnostics);
                constraintMapper.addMapping();
                return new OASResult(openapi, diagnostics);
            } else {
                return new OASResult(openapi, oasResult.getDiagnostics());
            }
        } else {
            return oasResult;
        }
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

    private static void convertServiceToOpenAPI(ServiceDeclarationNode serviceNode, OpenAPI openAPI,
                                                SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor,
                                                List<OpenAPIMapperDiagnostic> diagnostics) {
        NodeList<Node> functions = serviceNode.members();
        List<FunctionDefinitionNode> resources = new ArrayList<>();
        for (Node function: functions) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                resources.add((FunctionDefinitionNode) function);
            }
        }
        AdditionalData additionalData = new AdditionalData(semanticModel, moduleMemberVisitor, diagnostics);
        ResourceMapperInterface resourceMapper = new ResourceMapper(openAPI, resources, additionalData,
                isTreatNilableAsOptionalParameter(serviceNode));
        resourceMapper.addMapping();
    }

    private static boolean isTreatNilableAsOptionalParameter(ServiceDeclarationNode serviceNode) {
        if (serviceNode.metadata().isEmpty()) {
            return true;
        }

        MetadataNode metadataNode = serviceNode.metadata().get();
        NodeList<AnnotationNode> annotations = metadataNode.annotations();

        if (!annotations.isEmpty()) {
            Optional<String> values = extractServiceAnnotationDetails(annotations,
                    "http:ServiceConfig", "treatNilableAsOptional");
            if (values.isPresent()) {
                return values.get().equals(Constants.TRUE);
            }
        }
        return true;
    }
}
