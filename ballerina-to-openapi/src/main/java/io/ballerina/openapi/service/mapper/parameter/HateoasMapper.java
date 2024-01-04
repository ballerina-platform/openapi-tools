/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.openapi.service.mapper.parameter.model.HateoasLink;

import java.util.List;
import java.util.Optional;

public class HateoasMapper {
    private int serviceId;
    public HateoasMapper(int serviceId) {
        this.serviceId = serviceId;
    }

    private int getServiceId(SemanticModel semanticModel, ServiceDeclarationNode serviceNode) {
        Optional<Symbol> serviceDeclarationOpt = semanticModel.symbol(serviceNode);
        ServiceDeclarationSymbol serviceSymbol = (ServiceDeclarationSymbol) serviceDeclarationOpt.get();
        serviceId = serviceSymbol.hashCode();
        return serviceId;
    }

    public void mapHateoasLink(int serviceId, FunctionDefinitionNode resourceFunction) {
        Optional<String> linkedTo = getResourceConfigAnnotation(resourceFunction)
                .flatMap(resourceConfig -> getValueForAnnotationFields(resourceConfig, "linkedTo"));
        if (linkedTo.isEmpty()) {
            return;
        }
        List<HateoasLink> links = getLinks(linkedTo.get());
    }

    // todo: implement this properly
    private List<HateoasLink> getLinks(String linkedTo) {
        return List.of();
    }

    private Optional<AnnotationNode> getResourceConfigAnnotation(FunctionDefinitionNode resourceFunction) {
        Optional<MetadataNode> metadata = resourceFunction.metadata();
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        MetadataNode metaData = metadata.get();
        NodeList<AnnotationNode> annotations = metaData.annotations();
        return annotations.stream()
                .filter(ann -> "http:ResourceConfig".equals(ann.annotReference().toString().trim()))
                .findFirst();
    }

    private Optional<String> getValueForAnnotationFields(AnnotationNode resourceConfigAnnotation, String fieldName) {
        return resourceConfigAnnotation
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
}
