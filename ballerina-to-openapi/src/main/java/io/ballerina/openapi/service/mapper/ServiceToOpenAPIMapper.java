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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapper;
import io.ballerina.openapi.service.mapper.metainfo.MetaInfoMapper;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OASGenerationMetaInfo;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.openapi.service.mapper.model.ResourceFunction;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDeclaration;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDefinition;
import io.ballerina.openapi.service.mapper.model.ServiceContractType;
import io.ballerina.openapi.service.mapper.model.ServiceDeclaration;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.HYPHEN;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.containErrors;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOpenApiFileName;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.isHttpService;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.isHttpServiceContract;

/**
 * The ServiceToOpenAPIConverterUtils provide API for convert ballerina service into openAPI specification.
 *
 * @since 2.0.0
 */
public final class ServiceToOpenAPIMapper {

    private ServiceToOpenAPIMapper() {
    }

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
        Map<String, ServiceNode> servicesToGenerate = new HashMap<>();
        List<String> availableService = new ArrayList<>();
        List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
        List<OASResult> outputs = new ArrayList<>();
        if (containErrors(semanticModel.diagnostics())) {
            ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_106);
            diagnostics.add(error);
        } else {
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            extractServiceNodes(serviceName, availableService, servicesToGenerate, modulePartNode, semanticModel);
            // If there are no services found for a given mapper name.
            if (serviceName != null && servicesToGenerate.isEmpty()) {
                ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_107, serviceName,
                        availableService.toString());
                diagnostics.add(error);
            }
            // Generating openapi specification for selected services
            for (Map.Entry<String, ServiceNode> serviceNode : servicesToGenerate.entrySet()) {
                String openApiName = getOpenApiFileName(syntaxTree.filePath(), serviceNode.getKey(), needJson);
                OASResult oasDefinition = generateOasFroServiceNode(project, openApiName,
                        semanticModel, inputPath, serviceNode.getValue());
                outputs.add(oasDefinition);
            }
        }
        if (!diagnostics.isEmpty()) {
            OASResult exceptions = new OASResult(null, diagnostics);
            outputs.add(exceptions);
        }
        return outputs;
    }

    public static OASResult generateOasFroServiceNode(Project project, String openApiName, SemanticModel semanticModel,
                                                       Path inputPath, ServiceNode serviceNode) {
        OASGenerationMetaInfo.OASGenerationMetaInfoBuilder builder =
                new OASGenerationMetaInfo.OASGenerationMetaInfoBuilder();
        builder.setServiceNode(serviceNode)
                .setSemanticModel(semanticModel)
                .setOpenApiFileName(openApiName)
                .setBallerinaFilePath(inputPath)
                .setProject(project);
        OASGenerationMetaInfo oasGenerationMetaInfo = builder.build();
        OASResult oasDefinition = generateOAS(oasGenerationMetaInfo);
        oasDefinition.setServiceName(openApiName);
        return oasDefinition;
    }

    /**
     * Filter all the end points and service nodes.
     */
    private static void extractServiceNodes(String serviceName, List<String> availableService,
                                            Map<String, ServiceNode> servicesToGenerate,
                                            ModulePartNode modulePartNode, SemanticModel semanticModel) {
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                if (isHttpService(serviceNode, semanticModel)) {
                    // Here check the service is related to the http
                    // module by checking listener type that attached to service endpoints.
                    ServiceNode service = new ServiceDeclaration(serviceNode, semanticModel);
                    Optional<Symbol> serviceSymbol = service.getSymbol(semanticModel);
                    if (serviceSymbol.isPresent() && serviceSymbol.get() instanceof ServiceDeclarationSymbol) {
                        addService(serviceName, availableService, service, servicesToGenerate, serviceSymbol.get());
                    }
                }
            } else if (syntaxKind.equals(SyntaxKind.TYPE_DEFINITION)) {
                Node descriptorNode = ((TypeDefinitionNode) node).typeDescriptor();
                // TODO: Distinct service types should work here
                if (descriptorNode.kind().equals(SyntaxKind.OBJECT_TYPE_DESC) &&
                        isHttpServiceContract(descriptorNode, semanticModel)) {
                    ServiceNode service = new ServiceContractType((TypeDefinitionNode) node);
                    Optional<Symbol> serviceSymbol = service.getSymbol(semanticModel);
                    serviceSymbol.ifPresent(symbol ->
                            addService(serviceName, availableService, service, servicesToGenerate, symbol));
                }
            }
        }
    }

    public static Optional<ServiceNode> getServiceNode(Node node, SemanticModel semanticModel) {
        if (node instanceof ServiceDeclarationNode serviceNode && isHttpService(serviceNode, semanticModel)) {
            return Optional.of(new ServiceDeclaration(serviceNode, semanticModel));
        } else if (node instanceof TypeDefinitionNode serviceTypeNode &&
                serviceTypeNode.typeDescriptor() instanceof ObjectTypeDescriptorNode serviceNode &&
                isHttpServiceContract(serviceNode, semanticModel)) {
            return Optional.of(new ServiceContractType((TypeDefinitionNode) node));
        }
        return Optional.empty();
    }

    private static void addService(String serviceName, List<String> availableService, ServiceNode service,
                                   Map<String, ServiceNode> servicesToGenerate, Symbol serviceSymbol) {
        String basePath = service.absoluteResourcePath();
        String updateServiceName = basePath;
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
        if (servicesToGenerate.containsKey(basePath)) {
            updateServiceName = basePath + HYPHEN + serviceSymbol.hashCode();
        }
        if (serviceName != null) {
            // Filtering by service name
            availableService.add(basePath);
            if (serviceName.equals(basePath)) {
                servicesToGenerate.put(updateServiceName, service);
            }
        } else {
            // To generate for all services
            servicesToGenerate.put(updateServiceName, service);
        }
    }

    /**
     * Provides an instance of {@code OASResult}, which contains the generated contract as well as
     * all the diagnostics information.
     *
     * @param oasGenerationMetaInfo Includes the service definition node, endpoints, semantic model, openapi file
     *                              name and ballerina file path
     * @return {@code OASResult}
     */
    public static OASResult generateOAS(OASGenerationMetaInfo oasGenerationMetaInfo) {
        ServiceNode serviceDefinition = oasGenerationMetaInfo.getServiceNode();
        SemanticModel semanticModel = oasGenerationMetaInfo.getSemanticModel();
        Package currentPackage = oasGenerationMetaInfo.getProject().currentPackage();
        ModuleMemberVisitor moduleMemberVisitor = extractNodesFromProject(oasGenerationMetaInfo.getProject(),
                semanticModel);
        Set<ListenerDeclarationNode> listeners = moduleMemberVisitor.getListenerDeclarationNodes();
        Set<ServiceContractType> serviceContractTypes = moduleMemberVisitor.getServiceContractTypeNodes();
        String openApiFileName = oasGenerationMetaInfo.getOpenApiFileName();
        Path ballerinaFilePath = oasGenerationMetaInfo.getBallerinaFilePath();
        // 01.Fill the openAPI info section
        OASResult oasResult = InfoMapper.getOASResultWithInfo(serviceDefinition, semanticModel,
                openApiFileName, ballerinaFilePath);
        if (oasResult.getOpenAPI().isPresent() && oasResult.getDiagnostics().isEmpty()) {
            OpenAPI openapi = oasResult.getOpenAPI().get();
            List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
            if (openapi.getPaths() == null) {
                ServiceMapperFactory serviceMapperFactory = new ServiceMapperFactory(openapi, semanticModel,
                        moduleMemberVisitor, diagnostics, serviceDefinition);

                ServersMapper serversMapperImpl = serviceMapperFactory.getServersMapper(listeners, serviceDefinition);
                serversMapperImpl.setServers();

                if (oasAvailableViaServiceContract(serviceDefinition)) {
                    return updateOasResultWithServiceContract((ServiceDeclaration) serviceDefinition, currentPackage,
                            oasResult, semanticModel, serviceContractTypes);
                }

                convertServiceToOpenAPI(serviceDefinition, serviceMapperFactory);

                ConstraintMapper constraintMapper = serviceMapperFactory.getConstraintMapper();
                constraintMapper.setConstraints();

                HateoasMapper hateoasMapper = serviceMapperFactory.getHateoasMapper();
                hateoasMapper.setOpenApiLinks(serviceDefinition, openapi);

                MetaInfoMapper metaInfoMapper = serviceMapperFactory.getMetaInfoMapper();
                metaInfoMapper.setResourceMetaData(serviceDefinition, openapi, ballerinaFilePath);
                diagnostics.addAll(metaInfoMapper.getDiagnostics());

                if (openapi.getComponents().getSchemas().isEmpty()) {
                    openapi.setComponents(null);
                }
                return new OASResult(openapi, diagnostics);
            } else {
                return new OASResult(openapi, oasResult.getDiagnostics());
            }
        } else {
            return oasResult;
        }
    }

    static boolean oasAvailableViaServiceContract(ServiceNode serviceNode) {
        return serviceNode.kind().equals(ServiceNode.Kind.SERVICE_DECLARATION) &&
                ((ServiceDeclaration) serviceNode).implementsServiceContract();
    }

    private static OASResult updateOasResultWithServiceContract(ServiceDeclaration serviceDeclaration, Package pkg,
                                                                OASResult oasResult, SemanticModel semanticModel,
                                                                Set<ServiceContractType> serviceContractTypes) {
        Optional<OpenAPI> openAPI = serviceDeclaration.getOpenAPIFromServiceContract(pkg, semanticModel,
                serviceContractTypes, oasResult.getDiagnostics());
        if (openAPI.isEmpty()) {
            return oasResult;
        }

        OpenAPI openApiFromServiceContract = openAPI.get();
        if (oasResult.getOpenAPI().isEmpty()) {
            oasResult.setOpenAPI(openApiFromServiceContract);
            return oasResult;
        }

        // Copy info
        Info existingOpenAPIInfo = oasResult.getOpenAPI().get().getInfo();
        // Update title
        existingOpenAPIInfo.setTitle(openApiFromServiceContract.getInfo().getTitle());
        openApiFromServiceContract.setInfo(existingOpenAPIInfo);

        // Copy servers
        String basePath = extractBasePath(openApiFromServiceContract);
        List<Server> existingServers = oasResult.getOpenAPI().get().getServers();
        existingServers.forEach(
                server -> server.setUrl(server.getUrl() + basePath)
        );
        openApiFromServiceContract.setServers(existingServers);
        oasResult.setOpenAPI(openApiFromServiceContract);
        return oasResult;
    }

    private static String extractBasePath(OpenAPI openApiFromServiceContract) {
        List<Server> servers = openApiFromServiceContract.getServers();
        if (Objects.isNull(servers) || servers.isEmpty()) {
            return null;
        }
        String[] parts = servers.get(0).getUrl().split("\\{server}:\\{port}");
        return servers.get(0).getUrl().split("\\{server}:\\{port}").length == 2 ? parts[1] : "";
    }

    /**
     * Travers every syntax tree and collect all the listener nodes.
     *
     * @param project - current project
     */
    public static ModuleMemberVisitor extractNodesFromProject(Project project, SemanticModel semanticModel) {
        ModuleMemberVisitor balNodeVisitor = new ModuleMemberVisitor(semanticModel);
        project.currentPackage().moduleIds().forEach(moduleId -> {
            Module module = project.currentPackage().module(moduleId);
            module.documentIds().forEach(documentId -> {
                SyntaxTree syntaxTreeDoc = module.document(documentId).syntaxTree();
                syntaxTreeDoc.rootNode().accept(balNodeVisitor);
            });
        });
        return balNodeVisitor;
    }

    private static void convertServiceToOpenAPI(ServiceNode serviceNode, ServiceMapperFactory serviceMapperFactory) {
        NodeList<Node> functions = serviceNode.members();
        List<ResourceFunction> resources = new ArrayList<>();
        for (Node function: functions) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                resources.add(new ResourceFunctionDefinition((FunctionDefinitionNode) function));
            } else if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION)) {
                resources.add(new ResourceFunctionDeclaration((MethodDeclarationNode) function));
            }
        }
        ResourceMapper resourceMapper = serviceMapperFactory.getResourceMapper(resources);
        resourceMapper.setOperation();
    }
}
