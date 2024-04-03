/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.extension.doc.gen;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.openapi.extension.Constants;
import io.ballerina.openapi.extension.OpenApiDiagnosticCode;
import io.ballerina.openapi.extension.context.OpenApiDocContext;
import io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper;
import io.ballerina.openapi.service.mapper.model.OASGenerationMetaInfo;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.SLASH;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.normalizeTitle;
import static io.ballerina.openapi.extension.context.OpenApiDocContextHandler.getContextHandler;
import static io.ballerina.openapi.extension.doc.DocGenerationUtils.getDiagnostics;

/**
 * {@code AbstractOpenApiDocGenerator} contains the basic utilities required for OpenAPI doc generation.
 */
public abstract class AbstractOpenApiDocGenerator implements OpenApiDocGenerator {
    private static final String FILE_NAME_FORMAT = "%d.json";

    private final OpenApiContractResolver contractResolver;

    public AbstractOpenApiDocGenerator() {
        this.contractResolver = new OpenApiContractResolver();
    }

    @Override
    public void generate(OpenApiDocConfig config, SyntaxNodeAnalysisContext context, NodeLocation location) {
        try {
            int serviceId = config.getServiceSymbol().hashCode();
            Package currentPackage = config.getCurrentPackage();
            Path srcRoot = currentPackage.project().sourceRoot();

            // find the project root path
            Path projectRoot = retrieveProjectRoot(srcRoot);

            ServiceDeclarationNode serviceNode = config.getServiceNode();
            Optional<AnnotationNode> serviceInfoAnnotationOpt = getServiceInfoAnnotation(serviceNode);
            if (serviceInfoAnnotationOpt.isPresent()) {
                AnnotationNode serviceInfoAnnotation = serviceInfoAnnotationOpt.get();

                boolean embed = retrieveValueForAnnotationFields(
                        serviceInfoAnnotation, Constants.EMBED)
                        .map(Boolean::parseBoolean)
                        .orElse(false);

                // use the available open-api doc and update the context
                OpenApiContractResolver.ResolverResponse resolverResponse = this.contractResolver
                        .resolve(serviceInfoAnnotation, projectRoot);
                if (resolverResponse.isContractAvailable()) {
                    // could not find the open-api contract file, hence will not proceed
                    if (resolverResponse.getContractPath().isEmpty()) {
                        return;
                    }
                    String openApiDefinition = Files.readString(resolverResponse.getContractPath().get());
                    updateOpenApiContext(context, serviceId, openApiDefinition, embed);
                } else {
                    // generate open-api doc and update the context if the `contract` configuration is not available
                    generateOpenApiDoc(currentPackage.project(), config, context, location, embed);
                }
            }
        } catch (IOException | RuntimeException e) {
            // currently, we do not have open-api doc generation logic for following scenarios:
            //  1. default resources and for scenarios
            //  2. returning http-response from a resource
            // hence logs are disabled for now
        }
    }

    private void updateOpenApiContext(SyntaxNodeAnalysisContext context, int serviceId, String openApiDefinition,
                                      boolean embed) {
        OpenApiDocContext.OpenApiDefinition openApiDef = new OpenApiDocContext
                .OpenApiDefinition(serviceId, openApiDefinition, embed);
        getContextHandler().updateContext(context.moduleId(), context.documentId(), openApiDef);
    }

    private void updateCompilerContext(SyntaxNodeAnalysisContext context, NodeLocation location,
                                       OpenApiDiagnosticCode errorCode) {
        Diagnostic diagnostic = getDiagnostics(errorCode, location);
        context.reportDiagnostic(diagnostic);
    }

    private Optional<AnnotationNode> getServiceInfoAnnotation(ServiceDeclarationNode serviceNode) {
        Optional<MetadataNode> metadata = serviceNode.metadata();
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        MetadataNode metaData = metadata.get();
        NodeList<AnnotationNode> annotations = metaData.annotations();
        String serviceInfoAnnotation = String.format("%s:%s",
                Constants.PACKAGE_NAME, Constants.SERVICE_INFO_ANNOTATION_IDENTIFIER);
        return annotations.stream()
                .filter(ann -> serviceInfoAnnotation.equals(ann.annotReference().toString().trim()))
                .findFirst();
    }

    private Optional<String> retrieveValueForAnnotationFields(AnnotationNode serviceInfoAnnotation, String fieldName) {
        return serviceInfoAnnotation
                .annotValue()
                .map(MappingConstructorExpressionNode::fields)
                .flatMap(fields ->
                        fields.stream()
                            .filter(fld -> fld instanceof SpecificFieldNode)
                            .map(fld -> (SpecificFieldNode) fld)
                            .filter(fld -> fieldName.equals(fld.fieldName().toString().trim()))
                            .findFirst()
                ).flatMap(SpecificFieldNode::valueExpr)
                .map(en -> en.toString().trim());
    }

    private void generateOpenApiDoc(Project project, OpenApiDocConfig config, SyntaxNodeAnalysisContext context,
                                    NodeLocation location, boolean embed) {
        if (!embed) {
            return;
        }
        int serviceId = config.getServiceSymbol().hashCode();
        String targetFile = String.format(FILE_NAME_FORMAT, serviceId);
        OASGenerationMetaInfo.OASGenerationMetaInfoBuilder builder = new
                OASGenerationMetaInfo.OASGenerationMetaInfoBuilder();
        builder.setServiceDeclarationNode(config.getServiceNode())
                .setSemanticModel(config.getSemanticModel())
                .setOpenApiFileName(targetFile)
                .setBallerinaFilePath(null)
                .setProject(project);
        OASResult oasResult = ServiceToOpenAPIMapper.generateOAS(builder.build());
        Optional<OpenAPI> openApiOpt = oasResult.getOpenAPI();
        if (!oasResult.getDiagnostics().isEmpty() || openApiOpt.isEmpty()) {
            OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_107;
            updateCompilerContext(context, location, errorCode);
            return;
        }
        OpenAPI openApi = openApiOpt.get();
        if (openApi.getInfo().getTitle() == null || openApi.getInfo().getTitle().equals(SLASH)) {
            openApi.getInfo().setTitle(normalizeTitle(targetFile));
        }
        String openApiDefinition = Json.pretty(openApi);
        updateOpenApiContext(context, serviceId, openApiDefinition, embed);
    }

    protected Path retrieveProjectRoot(Path projectRoot) {
        return projectRoot;
    }
}
