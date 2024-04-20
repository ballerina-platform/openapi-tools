/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtil.OUT;
import static io.ballerina.openapi.TestUtil.TEST_DISTRIBUTION_PATH;
import static io.ballerina.openapi.extension.build.BuildExtensionTests.DISTRIBUTION_FILE_NAME;

/**
 * This is abstract class for containing the main common test methods for the other subclasses.
 */
public class OpenAPITest {
    protected Path tmpDir;
    protected PrintStream printStream;
    private ByteArrayOutputStream console;
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @BeforeClass
    public void setup() throws IOException {
        TestUtil.cleanDistribution();
        this.tmpDir = Files.createTempDirectory("openapi-cmd-test-out-" + System.nanoTime());
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(this.tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });
        this.console.close();
        this.printStream.close();
        deleteGeneratedFiles();
        TestUtil.cleanDistribution();
    }

    @AfterTest
    public void removeGeneratedFile() throws IOException {
        deleteGeneratedFiles();
    }

    // Delete generated service and schema files.
    public void deleteGeneratedFiles() throws IOException {

        File schemaFile = new File(this.tmpDir.resolve("types.bal").toString());
        schemaFile.delete();
        File clientFile = new File(this.tmpDir.resolve("client.bal").toString());
        clientFile.delete();
    }

    private static String getStringFromGivenBalFile(Path expectedServiceFile) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining(System.lineSeparator()));
        expectedServiceLines.close();
        return expectedServiceContent.trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
    }

    public void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String generated, String balfile) throws IOException {
        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve(balfile));
        String generatedFile = getStringFromGivenBalFile(this.tmpDir.resolve(generated));
        generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedFile.contains(expectedBallerinaContent));
    }

    public Process getProcess(List<String> buildArgs, Path testResourcePath) throws IOException {
        String balFile = "bal";
        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        buildArgs.add(0, TEST_DISTRIBUTION_PATH.resolve(DISTRIBUTION_FILE_NAME).resolve("bin")
                .resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(buildArgs, ' '));
        ProcessBuilder pb = new ProcessBuilder(buildArgs);
        pb.directory(testResourcePath.toFile());
        return pb.start();
    }
}
