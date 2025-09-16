/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PackageMemberVisitor {

    private final Map<String, ModuleMemberVisitor> moduleVisitors = new LinkedHashMap<>();

    public ModuleMemberVisitor createModuleVisitor(String moduleName, SemanticModel semanticModel) {
        ModuleMemberVisitor visitor = new ModuleMemberVisitor(semanticModel);
        moduleVisitors.put(moduleName, visitor);
        return visitor;
    }

    public Optional<ModuleMemberVisitor> getModuleVisitor(String moduleName) {
        if (moduleVisitors.containsKey(moduleName)) {
            return Optional.of(moduleVisitors.get(moduleName));
        }
        return Optional.empty();
    }

    public Optional<ListenerDeclarationNode> getListenerDeclaration(String moduleName, String listenerName) {
        return getModuleVisitor(moduleName)
                .flatMap(moduleVisitor -> moduleVisitor.getListenerDeclaration(listenerName));
    }

    public Optional<ClassDefinitionNode> getInterceptorServiceClassNode(String moduleName, String typeName) {
        return getModuleVisitor(moduleName)
                .flatMap(moduleVisitor -> moduleVisitor.getInterceptorServiceClassNode(typeName));
    }

    public Optional<TypeDefinitionNode> getTypeDefinitionNode(String moduleName, String typeName) {
        return getModuleVisitor(moduleName)
                .flatMap(moduleVisitor -> moduleVisitor.getTypeDefinitionNode(typeName));
    }

    public Optional<ModuleMemberVisitor.VariableDeclaredValue> getVariableDeclaredValue(String moduleName,
                                                                                        String variableName) {
        return getModuleVisitor(moduleName)
                .flatMap(moduleVisitor -> moduleVisitor.getVariableDeclaredValue(variableName));
    }

    public Optional<ServiceContractType> getServiceContractType(String moduleName, String typeName) {
        return getModuleVisitor(moduleName)
                .flatMap(moduleVisitor -> moduleVisitor.getServiceContractType(typeName));
    }
}
