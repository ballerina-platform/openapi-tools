///*
// * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package io.ballerina.openapi.generators.client;
//
//import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
//import io.ballerina.openapi.core.generators.common.model.Filter;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getImportDeclarationNode;
//
///**
// * This tests class for the tests imports in the generation.
// */
//public class ImportsTests {
//    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/negative")
//            .toAbsolutePath();
//
//    List<String> list1 = new ArrayList<>();
//    List<String> list2 = new ArrayList<>();
//    Filter filter = new Filter(list1, list2);
//    @Test(description = "Generate imports")
//    public void generateImports() {
//        ImportDeclarationNode importDeclarationNode = getImportDeclarationNode("ballerina",
//                "http");
//        Assert.assertEquals(importDeclarationNode.orgName().get().orgName().text(), "ballerina");
//        Assert.assertEquals(importDeclarationNode.moduleName().get(0).text(), "http");
//    }
//
//    @Test(description = "Generate remoteFunctions")
//    public void generateRemoteFunctions() {
//        ImportDeclarationNode importDeclarationNode = getImportDeclarationNode("ballerina",
//                "http");
//        Assert.assertEquals(importDeclarationNode.orgName().get().orgName().text(), "ballerina");
//        Assert.assertEquals(importDeclarationNode.moduleName().get(0).text(), "http");
//    }
//}
