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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.openapi.extension.Constants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * {@code OpenApiContractResolver} resolves the provided OpenAPI doc via `openapi:ServiceInfo` annotation.
 */
public final class OpenApiContractResolver {
    public ResolverResponse resolve(AnnotationNode serviceInfoAnnotation, Path projectRoot) {
        Optional<MappingConstructorExpressionNode> mappingConstructorExpressionNode =
                serviceInfoAnnotation.annotValue();
        if (mappingConstructorExpressionNode.isEmpty()) {
            // if details not available do not proceed
            return new ResolverResponse(false);
        }

        MappingConstructorExpressionNode exprNode = mappingConstructorExpressionNode.get();
        SeparatedNodeList<MappingFieldNode> fieldsOpt = exprNode.fields();
        if (fieldsOpt.isEmpty()) {
            // if details not available do not proceed
            return new ResolverResponse(false);
        }

        Optional<SeparatedNodeList<MappingFieldNode>> annotationFieldsOpt = serviceInfoAnnotation
                .annotValue().map(MappingConstructorExpressionNode::fields);
        if (annotationFieldsOpt.isEmpty()) {
            // annotation fields are not available, hence will not proceed
            return new ResolverResponse(false);
        }

        SeparatedNodeList<MappingFieldNode> annotationFields = annotationFieldsOpt.get();
        Optional<SpecificFieldNode> contractFieldOpt = annotationFields.stream()
                .filter(fld -> fld instanceof SpecificFieldNode)
                .map(fld -> (SpecificFieldNode) fld)
                .filter(fld -> Constants.CONTRACT.equals(fld.fieldName().toString().trim()))
                .findFirst();
        if (contractFieldOpt.isEmpty()) {
            // could not find the `contract` field in the service-info annotation, hence will not proceed
            return new ResolverResponse(false);
        }

        SpecificFieldNode openApiContract = contractFieldOpt.get();
        Optional<ExpressionNode> openApiContractValueOpt = openApiContract.valueExpr();
        if (openApiContractValueOpt.isEmpty()) {
            // could not find the value for `contract` field in the service-info annotation,
            // hence will not proceed
            return new ResolverResponse(true);
        }

        ExpressionNode openApiContractValue = openApiContractValueOpt.get();
        String openApiContractPath = openApiContractValue.toString()
                .replaceAll("\"", "").trim();
        if (openApiContractPath.isBlank()) {
            // `contract` value is empty, hence will not proceed
            return new ResolverResponse(true);
        }

        Path pathToOpenApiContract = getPathToOpenApiContract(openApiContractPath, projectRoot);
        if (!Files.exists(pathToOpenApiContract)) {
            // could not find open-api contract file, hence will not proceed
            return new ResolverResponse(true);
        }

        return new ResolverResponse(pathToOpenApiContract);
    }

    private Path getPathToOpenApiContract(String openApiPath, Path projectRoot) {
        Path openApiDocPath = Paths.get(openApiPath);
        if (openApiDocPath.isAbsolute()) {
            return openApiDocPath;
        } else {
            return projectRoot.resolve(openApiDocPath);
        }
    }

    public static class ResolverResponse {
        private final boolean contractAvailable;
        private Path contractPath;

        ResolverResponse(boolean contractAvailable) {
            this.contractAvailable = contractAvailable;
        }

        ResolverResponse(Path contractPath) {
            this.contractAvailable = true;
            this.contractPath = contractPath;
        }

        public boolean isContractAvailable() {
            return contractAvailable;
        }

        public Optional<Path> getContractPath() {
            return Optional.ofNullable(this.contractPath);
        }
    }
}
