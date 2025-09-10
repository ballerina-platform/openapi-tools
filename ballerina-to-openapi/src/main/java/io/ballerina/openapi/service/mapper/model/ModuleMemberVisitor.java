/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getTypeDescriptor;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.isHttpServiceContract;

/**
 * Visitor to get the TypeDefinitionNode and ListenerDeclarationNodes.
 *
 * @since 1.6.0
 */
public class ModuleMemberVisitor extends NodeVisitor {

    Set<TypeDefinitionNode> typeDefinitionNodes = new LinkedHashSet<>();
    Set<ListenerDeclarationNode> listenerDeclarationNodes = new LinkedHashSet<>();
    Set<ClassDefinitionNode> interceptorServiceClassNodes = new LinkedHashSet<>();
    Set<ServiceContractType> serviceContractTypeNodes = new LinkedHashSet<>();
    Map<String, VariableDeclaredValue> variableDeclarations = new LinkedHashMap<>();
    SemanticModel semanticModel;

    public record VariableDeclaredValue(ExpressionNode value, boolean isConfigurable) {
    }

    public ModuleMemberVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {
        typeDefinitionNodes.add(typeDefinitionNode);
        Node descriptorNode = getTypeDescriptor(typeDefinitionNode);
        if (descriptorNode.kind().equals(SyntaxKind.OBJECT_TYPE_DESC) &&
                isHttpServiceContract(descriptorNode, semanticModel)) {
            serviceContractTypeNodes.add(new ServiceContractType(typeDefinitionNode));
        }
    }

    @Override
    public void visit(ListenerDeclarationNode listenerDeclarationNode) {
        listenerDeclarationNodes.add(listenerDeclarationNode);
    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {
        interceptorServiceClassNodes.add(classDefinitionNode);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        TypedBindingPatternNode typedBindingPatternNode = moduleVariableDeclarationNode.typedBindingPattern();
        TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
        BindingPatternNode bindingPatternNode = typedBindingPatternNode.bindingPattern();
        if (!bindingPatternNode.kind().equals(SyntaxKind.CAPTURE_BINDING_PATTERN) ||
                !(typeDescriptorNode.kind().equals(SyntaxKind.INT_TYPE_DESC) ||
                        typeDescriptorNode.kind().equals(SyntaxKind.VAR_TYPE_DESC))) {
            return;
        }

        CaptureBindingPatternNode captureBindingPatternNode = (CaptureBindingPatternNode) bindingPatternNode;
        if (captureBindingPatternNode.variableName().isMissing()) {
            return;
        }
        String variableName = captureBindingPatternNode.variableName().text();
        Optional<ExpressionNode> variableValue = moduleVariableDeclarationNode.initializer();
        boolean isConfigurable = moduleVariableDeclarationNode.qualifiers().stream()
                .anyMatch(token -> token.kind().equals(SyntaxKind.CONFIGURABLE_KEYWORD));
        variableValue.ifPresent(expressionNode -> variableDeclarations.put(variableName,
                new VariableDeclaredValue(expressionNode, isConfigurable)));
    }

    @Override
    public void visit(ConstantDeclarationNode constantDeclarationNode) {
        String variableName = constantDeclarationNode.variableName().text();
        Node variableValue = constantDeclarationNode.initializer();
        if (variableValue instanceof ExpressionNode valueExpression) {
            // Constant declarations are always non-configurable
            variableDeclarations.put(variableName, new VariableDeclaredValue(valueExpression, false));
        }
    }

    public Set<ListenerDeclarationNode> getListenerDeclarationNodes() {
        return listenerDeclarationNodes;
    }

    public Optional<TypeDefinitionNode> getTypeDefinitionNode(String typeName) {
        for (TypeDefinitionNode typeDefinitionNode : typeDefinitionNodes) {
            if (MapperCommonUtils.unescapeIdentifier(typeDefinitionNode.typeName().text()).equals(typeName)) {
                return Optional.of(typeDefinitionNode);
            }
        }
        return Optional.empty();
    }

    public Optional<ClassDefinitionNode> getInterceptorServiceClassNode(String typeName) {
        for (ClassDefinitionNode classDefinitionNode : interceptorServiceClassNodes) {
            if (MapperCommonUtils.unescapeIdentifier(classDefinitionNode.className().text()).equals(typeName)) {
                return Optional.of(classDefinitionNode);
            }
        }
        return Optional.empty();
    }

    public boolean hasVariableDeclaration(String variableName) {
        return variableDeclarations.containsKey(variableName);
    }

    public VariableDeclaredValue getVariableDeclaredValue(String variableName) {
        return variableDeclarations.get(variableName);
    }

    public Set<ServiceContractType> getServiceContractTypeNodes() {
        return serviceContractTypeNodes;
    }
}
