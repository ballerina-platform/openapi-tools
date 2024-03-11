/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is to store negative tests related to openAPI CLI.
 */
public class NegativeCmdTests extends OpenAPICommandTest {
    @Test(description = "Test for invalid ballerina package in `add` sub command")
    public void testInvalidBallerinaPackage() throws IOException {
        Path resourceDir = Paths.get(System.getProperty("user.dir")).resolve("build/resources/test");
        Path packagePath = resourceDir.resolve(Paths.get("cmd"));
        String[] addArgs = {"--input", "petstore.yaml", "-p", packagePath.toString(),
                "--module", "delivery", "--nullable", "--license", "license.txt", "--mode", "client",
                "--client-methods", "resource"};
        Add add = new Add(printStream,  false);
        new CommandLine(add).parseArgs(addArgs);
        add.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: invalid Ballerina package directory:"));
    }
}
