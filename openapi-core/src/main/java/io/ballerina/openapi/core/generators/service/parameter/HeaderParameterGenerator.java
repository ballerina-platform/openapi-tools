/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.ballerina.openapi.core.generators.service.ServiceGenerationUtils;
import io.ballerina.openapi.core.generators.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.generators.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isArraySchema;

public class HeaderParameterGenerator extends ParameterGenerator {

    private static final List<String> paramSupportedTypes =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

    public HeaderParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    /**
     * This function for generating parameter ST node for header.
     * <pre> resource function get pets(@http:Header {name:"x-request-id"} string header) </pre>
     */
    @Override
    public ParameterNode generateParameterNode(Parameter parameter) throws UnsupportedOASDataTypeException,
            InvalidReferenceException {
        Schema<?> schema = parameter.getSchema();
        String headerType = GeneratorConstants.STRING;
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName = createIdentifierToken(GeneratorUtils.escapeIdentifier(parameter.getName()
                        .toLowerCase(Locale.ENGLISH)), AbstractNodeFactory.createEmptyMinutiaeList(),
                GeneratorUtils.SINGLE_WS_MINUTIAE);

        if (getOpenAPIType(schema) == null && schema.get$ref() == null) {
            // Header example:
            // 01.<pre>
            //       in: header
            //       name: X-Request-ID
            //       schema: {}
            //  </pre>
            diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_106, parameter.getName()));
        } else if (schema.get$ref() != null) {
            String type = getValidName(extractReferenceType(schema.get$ref()), true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type.trim());
            if (paramSupportedTypes.contains(getOpenAPIType(refSchema)) || isArraySchema(refSchema)) {
                TypeHandler.getInstance().getTypeNodeFromOASSchema(schema, false);
                headerType = type;
            } else {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_105, parameter.getName(),
                        getOpenAPIType(refSchema)));
            }
        } else if (paramSupportedTypes.contains(getOpenAPIType(schema)) || isArraySchema(schema)) {
            headerType = convertOpenAPITypeToBallerina(schema, true).trim();
        } else {
            diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_105, parameter.getName(),
                    getOpenAPIType(schema)));
        }

        if (isArraySchema(schema)) {
            // TODO: Support nested arrays
            Schema<?> items = schema.getItems();
            String arrayType = GeneratorConstants.STRING;
            if (getOpenAPIType(items) == null && items.get$ref() == null) {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_104, parameter.getName()));
            } else if (items.get$ref() != null) {
                String type = getValidName(extractReferenceType(items.get$ref()), true);
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type.trim());
                if (paramSupportedTypes.contains(getOpenAPIType(refSchema))) {
                    arrayType = type;
                } else {
                    diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_103,
                            parameter.getName(), type));
                }
            } else if (!paramSupportedTypes.contains(getOpenAPIType(items))) {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_103, parameter.getName(),
                        getOpenAPIType(items)));
            } else if (items.getEnum() != null && !items.getEnum().isEmpty()) {
                arrayType = OPEN_PAREN_TOKEN.stringValue() + convertOpenAPITypeToBallerina(items, true) +
                        CLOSE_PAREN_TOKEN.stringValue();
            } else {
                arrayType = convertOpenAPITypeToBallerina(items, true);
            }
            BuiltinSimpleNameReferenceNode headerArrayItemTypeName = createBuiltinSimpleNameReferenceNode(
                    null, createIdentifierToken(arrayType));
            ArrayDimensionNode arrayDimensionNode = NodeFactory.createArrayDimensionNode(
                    createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            NodeList<ArrayDimensionNode> nodeList = createNodeList(arrayDimensionNode);
            headerTypeName = createArrayTypeDescriptorNode(headerArrayItemTypeName, nodeList);
        } else {
            headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                    headerType, GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE));
        }
        // Create annotation for header
        // TODO: This code block is to be enabled when handle the headers handle additional parameters
        // MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(
        //        createToken(SyntaxKind.OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(),
        //        createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        AnnotationNode headerNode = ServiceGenerationUtils.getAnnotationNode(GeneratorConstants.HEADER_ANNOT, null);
        NodeList<AnnotationNode> headerAnnotations = createNodeList(headerNode);
        // Handle optional values in headers
        if (!parameter.getRequired()) {
            // If optional it behaves like default value with null ex:(string? header)
            // If schema has an enum and that has a null values then the type is already nill. Hence, the check.
            headerTypeName = headerTypeName.toString().trim().endsWith(NILLABLE) ? headerTypeName :
                    createOptionalTypeDescriptorNode(headerTypeName,
                            createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        }
        // Handle default values in headers
        if (schema.getDefault() != null) {
            return getDefaultableHeaderNode(schema, headerTypeName, parameterName, headerAnnotations);
        }
        // Handle header with parameter required true and nullable ture ex: (string? header)
        if (parameter.getRequired() && schema.getNullable() != null && schema.getNullable().equals(true)) {
            isNullableRequired = true;
            headerTypeName = headerTypeName.toString().trim().endsWith(NILLABLE) ? headerTypeName :
                    createOptionalTypeDescriptorNode(headerTypeName,
                            createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        }
        return createRequiredParameterNode(headerAnnotations, headerTypeName, parameterName);
    }

    /**
     * Generate ballerina default headers.
     */
    private DefaultableParameterNode getDefaultableHeaderNode(Schema<?> schema, TypeDescriptorNode headerTypeName,
                                                              IdentifierToken parameterName,
                                                              NodeList<AnnotationNode> headerAnnotations) {

        if (!getOpenAPIType(schema).equals(GeneratorConstants.ARRAY)) {
            return createDefaultableParameterNode(headerAnnotations, headerTypeName, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken("\"" +
                            schema.getDefault().toString() + "\"")));
        }
        return createDefaultableParameterNode(headerAnnotations, headerTypeName, parameterName,
                createToken(SyntaxKind.EQUAL_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
    }
}
