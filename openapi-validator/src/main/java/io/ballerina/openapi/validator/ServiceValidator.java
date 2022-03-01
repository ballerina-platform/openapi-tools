/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.validator;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Location;

import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.EMBED;
import static io.ballerina.openapi.validator.Constants.OPENAPI_ANNOTATION;
import static io.ballerina.openapi.validator.ValidatorUtils.isHttpService;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;
import static io.ballerina.openapi.validator.error.CompilationError.NON_HTTP_SERVICE;

/**
 * This model used to filter and validate all the operations according to the given filter and filter the service
 * resource in the resource file.
 *
 * @since 2201.0.1
 */
public class ServiceValidator {
    private SyntaxNodeAnalysisContext context;

    public void initialize(SyntaxNodeAnalysisContext context) {
        this.context = context;
    }
    public void validate() {
        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) this.context.node();
        Package aPackage = context.currentPackage();
        DocumentId documentId = context.documentId();
        Optional<Path> path = aPackage.project().documentPath(documentId);
        Path ballerinaFilePath = path.orElse(null);
        Optional<MetadataNode> metadata = serviceNode.metadata();
        if (metadata.isPresent()) {
            Location location = serviceNode.location();
            // 1. Check openapi annotation is available
            // 2. Check contract path is available and exist
            MetadataNode serviceMetadata  = metadata.orElseThrow();
            NodeList<AnnotationNode> annotations = serviceMetadata.annotations();
            if (annotations.isEmpty()) {
                return;
            }
            boolean validatorEnable = false;
            for (AnnotationNode annotation: annotations) {
                Node node = annotation.annotReference();
                if (node.toString().trim().equals(OPENAPI_ANNOTATION)) {
                    // Test given service is http service, if not this will return WARNING and execute the ballerina
                    // file.
                    if (!isHttpService(serviceNode, context.semanticModel())) {
                        updateContext(context, NON_HTTP_SERVICE, location);
                        return;
                    }
                    Optional<MappingConstructorExpressionNode> mappingNode = annotation.annotValue();
                    if (mappingNode.isEmpty()) {
                        return;
                    }
                    MappingConstructorExpressionNode exprNode = mappingNode.get();
                    SeparatedNodeList<MappingFieldNode> fields = exprNode.fields();
                    // This condition for check the annotation has `embed` field only, if there, then validation
                    // disable.
                    boolean isEmbed = (fields.size() == 1 &&
                            (((SpecificFieldNode) fields.get(0)).fieldName().toString().trim().equals(EMBED)));
                    if (isEmbed) {
                        return;
                    }
                    // Annotation fields extract details

                }
            }
        } else {
            return;
        }

    }
}
