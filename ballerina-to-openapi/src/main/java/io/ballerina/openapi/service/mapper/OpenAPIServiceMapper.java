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
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractServiceAnnotationDetails;

/**
 * OpenAPIServiceMapper provides functionality for reading and writing OpenApi, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between openapi and ballerina.
 *
 * @since 2.0.0
 */
public class OpenAPIServiceMapper {
    private final SemanticModel semanticModel;
    private final List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
    private final ModuleMemberVisitor moduleMemberVisitor;
    private final OpenAPI openAPI;
    private final ServiceDeclarationNode serviceNode;

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Initializes a service parser for OpenApi.
     */
    public OpenAPIServiceMapper(ServiceDeclarationNode serviceNode, OpenAPI openAPI,
                                SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor) {
        // Default object mapper is JSON mapper available in openApi utils.
        this.semanticModel = semanticModel;
        this.moduleMemberVisitor = moduleMemberVisitor;
        this.serviceNode = serviceNode;
        this.openAPI = openAPI;
    }

    public void convertServiceToOpenAPI() {
        NodeList<Node> functions = serviceNode.members();
        List<FunctionDefinitionNode> resources = new ArrayList<>();
        for (Node function: functions) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                resources.add((FunctionDefinitionNode) function);
            }
        }
        TypeMapper typeMapper = new TypeMapper(openAPI, semanticModel, moduleMemberVisitor, diagnostics);
        OpenAPIResourceMapper resourceMapper = new OpenAPIResourceMapper(openAPI, resources, semanticModel,
                diagnostics, typeMapper, isTreatNilableAsOptionalParameter());
        resourceMapper.addMapping();
        ConstraintMapper constraintMapper = new ConstraintMapper(openAPI, moduleMemberVisitor, diagnostics);
        constraintMapper.addMapping();
    }

    private boolean isTreatNilableAsOptionalParameter() {
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
