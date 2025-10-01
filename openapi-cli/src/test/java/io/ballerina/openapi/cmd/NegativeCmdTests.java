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

    @Test(description = "Test without the input OpenAPI file in `flatten` sub command")
    public void testFlattenWithOutInputOpenAPIFile() throws IOException {
        String[] addArgs = {"-f", "json"};
        Flatten flatten = new Flatten(printStream, false);
        new CommandLine(flatten).parseArgs(addArgs);
        flatten.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: an OpenAPI definition path is required to flatten the OpenAPI " +
                "definition"));
    }

    @Test(description = "Test with the invalid input OpenAPI file in `flatten` sub command")
    public void testFlattenWithInvalidInputOpenAPIFile() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi-1.json"};
        Flatten flatten = new Flatten(printStream, false);
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: error occurred while reading the OpenAPI definition file"));
    }

    @Test(description = "Test with invalid invalid input OpenAPI file extension in `flatten` sub command")
    public void testFlattenWithInvalidInputOpenAPIFileExtension() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.txt"};
        Flatten flatten = new Flatten(printStream, false);
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: invalid input OpenAPI definition file extension. The OpenAPI " +
                "definition file should be in YAML or JSON format"));
    }

    @Test(description = "Test with the invalid output OpenAPI file format in `flatten` sub command")
    public void testFlattenWithInvalidOutputOpenAPIFileFormat() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-f", "txt", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten(printStream, false);
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("WARNING: invalid output format. The output format should be either " +
                "\"json\" or \"yaml\".Defaulting to format of the input file"));
    }

    @Test(description = "Test with the input OpenAPI file in `flatten` sub command which has parsing issues")
    public void testFlattenWithInputOpenAPIFileParsingIssues() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi_invalid.json", "-f", "txt", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten(printStream, false);
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("WARNING: invalid output format. The output format should be either " +
                "\"json\" or \"yaml\".Defaulting to format of the input file"));
    }

    @Test(description = "Test without the input OpenAPI file in `align` sub command")
    public void testAlignWithOutInputOpenAPIFile() throws IOException {
        String[] addArgs = {"-f", "json"};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(addArgs);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: an OpenAPI definition path is required to align the OpenAPI " +
                "definition"));
    }

    @Test(description = "Test with the invalid input OpenAPI file in `align` sub command")
    public void testAlignWithInvalidInputOpenAPIFile() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/align/openapi-1.json"};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(args);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: error occurred while reading the OpenAPI definition file"));
    }

    @Test(description = "Test with invalid input OpenAPI file extension in `align` sub command")
    public void testAlignWithInvalidInputOpenAPIFileExtension() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.txt"};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(args);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: invalid input OpenAPI definition file extension. The OpenAPI " +
                "definition file should be in YAML or JSON format"));
    }

    @Test(description = "Test with the invalid output OpenAPI file format in `align` sub command")
    public void testAlignWithInvalidOutputOpenAPIFileFormat() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-f", "txt", "-o", tmpDir.toString()};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(args);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("WARNING: invalid output format. The output format should be either " +
                "\"json\" or \"yaml\".Defaulting to format of the input file"));
    }

    @Test(description = "Test with the input OpenAPI file in `align` sub command which has parsing issues")
    public void testAlignWithInputOpenAPIFileParsingIssues() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_invalid.json", "-f", "txt", "-o",
                tmpDir.toString()};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(args);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("WARNING: invalid output format. The output format should be either " +
                "\"json\" or \"yaml\".Defaulting to format of the input file"));
    }

    @Test(description = "Test with the input OpenAPI file with Swagger V2 in `align` sub command")
    public void testAlignWithSwaggerV2() throws IOException {
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_2.0.yaml", "-o", tmpDir.toString()};
        Align align = new Align(printStream, false);
        new CommandLine(align).parseArgs(args);
        align.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("WARNING: Swagger version 2.0 found in the OpenAPI definition. The " +
                "generated OpenAPI definition will be in OpenAPI version 3.0.x"));
    }

    @Test(description = "Test add command inside a workspace")
    public void testAddCommandInsideWorkspace() throws IOException {
        Path workspaceDir = Paths.get(System.getProperty("user.dir")).resolve("build/resources/test/cmd/workspace");
        String[] addArgs = {"--input", "petstore.yaml", "-p", workspaceDir.toString(),
                "--module", "delivery", "--nullable", "--license", "license.txt", "--mode", "client",
                "--client-methods", "resource"};
        Add add = new Add(printStream,  false);
        new CommandLine(add).parseArgs(addArgs);
        add.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: provided directory: "));
        Assert.assertTrue(output.contains(" is a Ballerina workspace which is not supported by the tool. " +
                "Please provide a Ballerina package directory"));
    }
}
