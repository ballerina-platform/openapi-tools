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
package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class for the mapping ballerina headers with OAS header parameter sections.
 *
 * @since 2.0.0
 */
public class OpenAPIHeaderMapper {
    /**
     * Handle header parameters in ballerina data type.
     *
     * @param headerParam    -  Resource function parameter list
     */
    public List<Parameter> setHeaderParameter(RequiredParameterNode headerParam) {
        List<Parameter> parameters = new ArrayList<>();
        //Handle with string current header a support with only string and string[]
        String headerName = headerParam.paramName().get().text().replaceAll("\\\\", "");
        HeaderParameter headerParameter = new HeaderParameter();
        Node node = headerParam.typeName();
        StringSchema stringSchema = new StringSchema();
        if (node.kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            stringSchema.setNullable(true);
        } else {
            headerParameter.setRequired(true);
        }
        if (!headerParam.annotations().isEmpty()) {
            AnnotationNode annotationNode = headerParam.annotations().get(0);
            headerName = getHeaderName(headerName, annotationNode);
        }
        if (headerParam.typeName() instanceof ArrayTypeDescriptorNode)  {
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) headerParam.typeName();
            if (arrayNode.memberTypeDesc().kind() == SyntaxKind.STRING_TYPE_DESC) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setItems(stringSchema);
                headerParameter.schema(arraySchema);
                headerParameter.setName(headerName.replaceAll("\\\\", ""));
                parameters.add(headerParameter);
            }
        } else {
            headerParameter.schema(stringSchema);
            headerParameter.setName(headerName.replaceAll("\\\\", ""));
            parameters.add(headerParameter);
        }
        return parameters;
    }

    /*Extract header name from header annotation value */
    private String getHeaderName(String headerName, AnnotationNode annotationNode) {
        if (annotationNode.annotValue().isPresent()) {
            MappingConstructorExpressionNode fieldNode = annotationNode.annotValue().get();
            SeparatedNodeList<MappingFieldNode> fields = fieldNode.fields();
            for (MappingFieldNode field: fields) {
                SpecificFieldNode sField = (SpecificFieldNode) field;
                if (sField.fieldName().toString().trim().equals("name") && sField.valueExpr().isPresent()) {
                    headerName = sField.valueExpr().get().toString().trim().replaceAll("\"", "");
                }
            }
        }
        return headerName;
    }
}
