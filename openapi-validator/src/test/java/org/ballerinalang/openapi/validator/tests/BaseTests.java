/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceBodyNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.tools.text.LinePosition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.testng.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

/**
 * This BaseTests class use for main initializing of the tests.
 */
public class BaseTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/").toAbsolutePath();

    public static Inputs returnBType(String file, String testModule) {
        Inputs inputs = new Inputs();
        Path projectPath = RESOURCE_DIRECTORY.resolve("openapiValidator/ballerina-files").resolve(file);
        final TypeSymbol[] paramType = {null};
        //should be relative to src root
        Path fileName = Paths.get(file);

        // 1. Initializing the project instance
        SingleFileProject project = null;
        try {
//            project = BuildProject.load(projectPath);
            project = SingleFileProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2. Load the package
        Package currentPackage = project.currentPackage();
        // 3. Load default module
        Module module = currentPackage.getDefaultModule();
        // 4. Load syntax tree
        PackageName pkgName = PackageName.from("openapiValidator");
        ModuleName moduleName = ModuleName.from(pkgName, testModule);
//        Module module = currentPackage.module(moduleName);
        Iterator<DocumentId> documentIterator = module.documentIds().iterator();
        while (documentIterator.hasNext()) {
            DocumentId docId = documentIterator.next();
            Document doc = module.document(docId);
            SyntaxTree syntaxTree = doc.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
//            WorkspaceManager workspaceManager = new BallerinaWorkspaceManager();
            // Load semantic Model for given ballerina file
            SemanticModel semanticModel = currentPackage.module(moduleName).getCompilation().getSemanticModel();
            for (Node node : modulePartNode.members()) {
                SyntaxKind syntaxKind = node.kind();
                // Take the service for validation
                if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                    ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                    ServiceBodyNode srvBNode = (ServiceBodyNode) serviceDeclarationNode.serviceBody();
                    // Extract Service Body for validate
                    int count = srvBNode.internalNode().bucketCount();
                    STNode internalNode = srvBNode.internalNode();
                    // Get resource list
                    NodeList<Node> resourceList = srvBNode.resources();
                    for (Node resource : resourceList) {
                        if (resource instanceof FunctionDefinitionNode) {
                            FunctionDefinitionNode functionNode = (FunctionDefinitionNode) resource;
                            FunctionSignatureNode functionSignatureNode = functionNode.functionSignature();
                            SeparatedNodeList<ParameterNode> parameterList = functionSignatureNode.parameters();
                            for (ParameterNode paramNode : parameterList) {
                                if (paramNode instanceof RequiredParameterNode) {
                                    RequiredParameterNode requiredParameterNode = (RequiredParameterNode) paramNode;
                                    if (requiredParameterNode.typeName().kind()
                                            .equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                                        Optional<Symbol> symbol = semanticModel.symbol(fileName.toString(),
                                                LinePosition.from(requiredParameterNode.lineRange().startLine().line(),
                                                        requiredParameterNode.lineRange().startLine().offset()));
                                        symbol.ifPresent(symbol1 -> {
                                            paramType[0] = ((TypeReferenceTypeSymbol) symbol1).typeDescriptor();
                                            // return record type
                                        });
                                        inputs.setParamType(paramType[0]);
                                        inputs.setSyntaxTree(requiredParameterNode.syntaxTree());
                                        inputs.setSemanticModel(semanticModel);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return inputs;
    }
    //Filter the given Schema
    public static Schema getComponet(OpenAPI api, String componentName) {
        return api.getComponents().getSchemas().get(componentName);
    }

    public static Schema getSchema(OpenAPI api, String path) {
        return  api.getPaths().get(path).getGet().getParameters().get(0).getSchema();
    }
}
