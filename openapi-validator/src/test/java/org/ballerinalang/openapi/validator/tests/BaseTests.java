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

import io.ballerina.compiler.internal.parser.tree.STFunctionDefinitionNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionSignatureNode;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STRequiredParameterNode;
import io.ballerina.compiler.internal.parser.tree.STServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * This BaseTests class use for main initializing of the tests.
 */
public class BaseTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/").toAbsolutePath();
    private final String dummyContent = "function foo() {\n}";

    @Test (description = "tests for validator on build", enabled = true)
    public void testBuildProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("openapiValidator");

        // 1. Initializing the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2. Load the package
        Package currentPackage = project.currentPackage();
        // 3. Load default module
        Module defaultModule = currentPackage.getDefaultModule();
        // 4. Load syntax tree
        PackageName pkgName = PackageName.from("openapiValidator");
        ModuleName moduleName = ModuleName.from(pkgName, "validTest");
        Module module = currentPackage.module(moduleName);
        Iterator<DocumentId> documentIterator = module.documentIds().iterator();
        while (documentIterator.hasNext()) {
            DocumentId docId = documentIterator.next();
            Document doc = module.document(docId);
            SyntaxTree syntaxTree = doc.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node: modulePartNode.members()) {
                SyntaxKind syntaxKind = node.kind();
                // Take the service for validation
                if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                    STServiceDeclarationNode stServiceDeclarationNode =
                            (STServiceDeclarationNode)node.internalNode();
                    STNode stNode = stServiceDeclarationNode.serviceBody;
                    // Extract Service Body for validate
                    if (stNode.kind.equals(SyntaxKind.SERVICE_BODY)) {
                        int count = stNode.bucketCount();
                        for (int i = 0; i < count; i++) {
                            if (stNode.childInBucket(i).kind.equals(SyntaxKind.LIST)) {
                                int listBucketCount = stNode.childInBucket(i).bucketCount();
                                for (int j = 0; j < listBucketCount ; j++) {
                                    if (stNode.childInBucket(i).childInBucket(j).kind.equals(SyntaxKind.FUNCTION_DEFINITION)) {
                                        STFunctionDefinitionNode functionDefinitionNode =
                                                (STFunctionDefinitionNode) stNode.childInBucket(i).childInBucket(j);
                                        // Function Signature handle parameter
                                        STFunctionSignatureNode stFunctionSignatureNode =
                                                (STFunctionSignatureNode) functionDefinitionNode.functionSignature;
                                        STNodeList stNodeParameter =
                                                (STNodeList) stFunctionSignatureNode.parameters;
                                        int parameterCount = stNodeParameter.bucketCount();
                                        for (int k = 0; k < parameterCount ; k++) {
                                            if (stNodeParameter.childInBucket(k).kind.equals(SyntaxKind.REQUIRED_PARAM)) {
                                                STRequiredParameterNode stRequiredParameterNode =
                                                        (STRequiredParameterNode) stNodeParameter.childInBucket(k);
                                                if (stRequiredParameterNode.typeName.kind.equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                                                    //take the record parameter
                                                }
                                            }
                                        }

                                        // ToDo function body handle
//                                        STNode functionNode = stNode.childInBucket(i).childInBucket(j);
//                                        int functionChildList = functionNode.bucketCount();
//                                        for (int k = 0; k < functionChildList ; k++) {
//                                            if (functionNode.childInBucket(k).kind.equals(SyntaxKind.FUNCTION_SIGNATURE)) {
//                                                int functionList = functionNode.bucketCount();
//                                                for (int l = 0; l < functionList ; l++) {
//                                                    if (functionNode.childInBucket(l).kind.equals(SyntaxKind.LIST)) {
//                                                        List<STNode> functionNodeList =
//                                                                (List<STNode>) functionNode.childInBucket(l);
//                                                        for (STNode functionStNode : functionNodeList) {
//                                                            int parameterCount = functionStNode.bucketCount();
//                                                            for (int m = 0; m < parameterCount ; m++) {
//                                                                if (functionStNode.childInBucket(m).kind.equals(SyntaxKind.REQUIRED_PARAM)) {
//                                                                    STRequiredParameterNode parameterNode =
//                                                                            (STRequiredParameterNode)functionNode.childInBucket(m);
//                                                                    if (parameterNode.typeName.kind.equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
//                                                                        int countRequiredPara =
//                                                                                parameterNode.typeName.bucketCount();
//                                                                        for (int n = 0; n < countRequiredPara ; n++) {
//                                                                            STSimpleNameReferenceNode parameter =
//                                                                                    (STSimpleNameReferenceNode) parameterNode.typeName.childInBucket(n);
//                                                                        }
//
//
//                                                                    }
//                                                                }
//                                                            }
//
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
                                    }

                                }
                            }
                        }
                    }

                }
            }
        }


//        Assert.assertEquals(defaultModule.documentIds().size(), 2);
        // 4. Compile the module
        PackageCompilation compilation = currentPackage.getCompilation();

        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 2);

    }

}
