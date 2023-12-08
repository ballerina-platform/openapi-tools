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
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getAnnotationNodesFromServiceNode;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.handleReference;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

/**
 * This class for the mapping ballerina headers with OAS header parameter sections.
 *
 * @since 2.0.0
 */
public class OpenAPIHeaderMapper {
    private final SemanticModel semanticModel;
    private final Map<String, String> apidocs;
    private final ModuleMemberVisitor moduleMemberVisitor;
    private final TypeMapper typeMapper;

    public OpenAPIHeaderMapper(SemanticModel semanticModel, Map<String, String> apidocs,
                               ModuleMemberVisitor moduleMemberVisitor, TypeMapper typeMapper) {
        this.apidocs = apidocs;
        this.typeMapper = typeMapper;
        this.semanticModel = semanticModel;
        this.moduleMemberVisitor = moduleMemberVisitor;
    }

    /**
     * Handle header parameters in ballerina data type.
     *
     * @param headerParam    -  {@link RequiredParameterNode} type header parameter node
     */
    public List<Parameter> setHeaderParameter(RequiredParameterNode headerParam) {
        List<Parameter> parameters = new ArrayList<>();
        String headerName = unescapeIdentifier(extractHeaderName(headerParam));
        HeaderParameter headerParameter = new HeaderParameter();
        Node node = headerParam.typeName();
        Schema<?> headerTypeSchema;
        String isOptional = Constants.FALSE;
        Node headerDetailNode = headerParam.typeName();
        if (headerParam.typeName().kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            headerDetailNode = ((OptionalTypeDescriptorNode) headerParam.typeName()).typeDescriptor();
            isOptional = Constants.TRUE;
        }

        if (headerDetailNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode refNode = (SimpleNameReferenceNode) headerDetailNode;
            headerTypeSchema = handleReference(semanticModel, refNode, moduleMemberVisitor, typeMapper);
        } else {
            headerTypeSchema = MapperCommonUtils.getOpenApiSchema(getHeaderType(headerParam));
        }
        NodeList<AnnotationNode> annotations = getAnnotationNodesFromServiceNode(headerParam);
        if (!annotations.isEmpty()) {
            Optional<String> values = MapperCommonUtils.extractServiceAnnotationDetails(annotations,
                    "http:ServiceConfig", "treatNilableAsOptional");
            if (values.isPresent()) {
                isOptional = values.get();
            }
        }
        enableHeaderRequiredOption(headerParameter, node, headerTypeSchema, isOptional);
        if (apidocs != null && apidocs.containsKey(headerName)) {
            headerParameter.setDescription(apidocs.get(headerName.trim()));
        }
        completeHeaderParameter(parameters, headerName, headerParameter, headerTypeSchema, headerParam.annotations(),
                headerDetailNode, null);
        return parameters;
    }

    private String extractHeaderName(ParameterNode headerParam) {
        if (headerParam instanceof DefaultableParameterNode) {
            return ((DefaultableParameterNode) headerParam).paramName().get().text().replaceAll("\\\\", "");
        }
        return ((RequiredParameterNode) headerParam).paramName().get().text().replaceAll("\\\\", "");
    }

    /**
     * Handle header parameters in ballerina data type.
     *
     * @param headerParam    -  {@link DefaultableParameterNode} type header parameter node
     */
    public List<Parameter> setHeaderParameter(DefaultableParameterNode headerParam) {
        List<Parameter> parameters = new ArrayList<>();
        String headerName = extractHeaderName(headerParam);
        HeaderParameter headerParameter = new HeaderParameter();
        Schema<?> headerTypeSchema;
        if (headerParam.typeName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode refNode = (SimpleNameReferenceNode) headerParam.typeName();
            headerTypeSchema = handleReference(semanticModel, refNode, moduleMemberVisitor, typeMapper);
        } else {
            headerTypeSchema = MapperCommonUtils.getOpenApiSchema(getHeaderType(headerParam));
        }
        String defaultValueExpression = null;
        if (MapperCommonUtils.isSimpleValueLiteralKind(headerParam.expression().kind())) {
            defaultValueExpression = headerParam.expression().toString().trim();
        }
        if (headerParam.typeName().kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            headerTypeSchema.setNullable(true);
        }
        if (apidocs != null && apidocs.containsKey(headerName)) {
            headerParameter.setDescription(apidocs.get(headerName.trim()));
        }
        completeHeaderParameter(parameters, headerName, headerParameter, headerTypeSchema, headerParam.annotations(),
                headerParam.typeName(), defaultValueExpression);
        return parameters;
    }

    /**
     * Extract header type by removing its optional and array types.
     */
    private String getHeaderType(ParameterNode headerParam) {
        if (headerParam instanceof DefaultableParameterNode) {
            return ((DefaultableParameterNode) headerParam).typeName().toString().replaceAll("\\?", "").
                    replaceAll("\\[", "").replaceAll("]", "").trim();
        }
        return ((RequiredParameterNode) headerParam).typeName().toString().replaceAll("\\?", "").
                replaceAll("\\[", "").replaceAll("]", "").trim();
    }

    /**
     * Assign header values to OAS header parameter.
     */
    private void completeHeaderParameter(List<Parameter> parameters, String headerName, HeaderParameter headerParameter,
                                         Schema<?> headerSchema, NodeList<AnnotationNode> annotations, Node node,
                                         String defaultValueExpression) {

        if (!annotations.isEmpty()) {
            AnnotationNode annotationNode = annotations.get(0);
            headerName = getHeaderName(headerName, annotationNode);
        }
        if (node instanceof ArrayTypeDescriptorNode arrayNode) {
            ArraySchema arraySchema = new ArraySchema();
            SyntaxKind kind = arrayNode.memberTypeDesc().kind();
            Schema<?> itemSchema;
            if (kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode refNode = (SimpleNameReferenceNode) arrayNode.memberTypeDesc();
                itemSchema = handleReference(semanticModel, refNode, moduleMemberVisitor, typeMapper);
            } else {
                itemSchema = MapperCommonUtils.getOpenApiSchema(kind);
            }
            arraySchema.setItems(itemSchema);
            headerSchema = arraySchema;
        }
        headerSchema = MapperCommonUtils.setDefaultValue(headerSchema, defaultValueExpression);
        headerParameter.schema(headerSchema);
        headerParameter.setName(headerName);
        parameters.add(headerParameter);
    }

    private void enableHeaderRequiredOption(HeaderParameter headerParameter, Node node, Schema<?> headerSchema,
                                            String isOptional) {
        if (node.kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            headerSchema.setNullable(true);
            if (isOptional.equals(Constants.FALSE)) {
                headerParameter.setRequired(true);
            }
        } else {
            headerParameter.setRequired(true);
        }
    }

    /**
     * Extract header name from header annotation value.
     *
     * @param headerName        - Header name
     * @param annotationNode    - Related annotation for extract details
     * @return                  - Updated header name
     */
    private String getHeaderName(String headerName, AnnotationNode annotationNode) {
        if (annotationNode.annotValue().isPresent()) {
            MappingConstructorExpressionNode fieldNode = annotationNode.annotValue().get();
            SeparatedNodeList<MappingFieldNode> fields = fieldNode.fields();
            for (MappingFieldNode field: fields) {
                SpecificFieldNode sField = (SpecificFieldNode) field;
                if (sField.fieldName().toString().trim().equals("name") && sField.valueExpr().isPresent()) {
                    return sField.valueExpr().get().toString().trim().replaceAll("\"", "");
                }
            }
        }
        return headerName;
    }
}
