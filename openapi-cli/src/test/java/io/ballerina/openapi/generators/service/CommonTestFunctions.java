/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.service;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class store all common functions for testing in service generation.
 *
 * @since 2.0.0
 */
public class CommonTestFunctions {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    //Get string as a content of ballerina file
    private static String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    public static void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String balfile, SyntaxTree syntaxTree)
            throws IOException, FormatterException {
        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("generators/service/ballerina"),
                balfile);
        String generatedSyntaxTree = syntaxTree.toString();
        System.out.println(generatedSyntaxTree);
        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }
}
