/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OpenAPI command cmd common class to handle temp dirs and outputs.
 */
public abstract class OpenAPICommandTest {
    protected Path tmpDir;
    protected PrintStream printStream;
    protected final Path resourceDir = Paths.get("src/test/resources/").toAbsolutePath();
    private ByteArrayOutputStream console;
    
    @BeforeClass
    public void setup() throws IOException {
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
    }

    /**
     * Execute the openapi command with arguments.
     * @param yamlFile      input yaml file
     * @param serviceName   service name for generated service file
     * @return source yaml filePath return
     */
    public Path getExecuteCommand(String yamlFile, String serviceName) {
        Path yamlPath = resourceDir.resolve(Paths.get(yamlFile));
        String[] args = {"--input", yamlPath.toString(), "--service-name", serviceName, "-o",
                         this.tmpDir.toString(), "--mode", "io/ballerina/openapi/corenew/service"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, this.tmpDir);
        new CommandLine(cmd).parseArgs(args);
        String output = "";
        try {
            cmd.execute();
        } catch (BLauncherException e) {
            output = e.getDetailedMessages().get(0);
        }
        return yamlPath;
    }

    //Delete generated service and schema files.
    public void deleteGeneratedFiles(String fileName) {
        File serviceFile = new File(this.tmpDir.resolve(fileName).toString());
        File schemaFile = new File(this.tmpDir.resolve("schema.bal").toString());
        serviceFile.delete();
        schemaFile.delete();
    }

    //Replace all whitespace in the given string.
    public String replaceWhiteSpace(String trimString) {
        return (trimString.trim()).replaceAll("\\s+", "");
    }

    //Replace contract file path in generated service file with common URL.
    public String replaceContractPath(Stream<String> expectedServiceLines, String expectedService,
                                      String generatedService) {
        Pattern pattern = Pattern.compile("\\bcontract\\b: \"(.*?)\"");
        Matcher matcher = pattern.matcher(generatedService);
        if (matcher.find()) {
            String contractPath = "contract: " + "\"" + matcher.group(1) + "\"";
            expectedService = expectedService.replaceAll("\\bcontract\\b: \"(.*?)\"",
                    Matcher.quoteReplacement(contractPath));
            expectedServiceLines.close();
        }
        return expectedService;
    }

    //Convert file content to one string.
    public String getStringFromFile(Path filePath) throws IOException {
        Stream<String> serviceLines = Files.lines(filePath);
        String generatedService = serviceLines.collect(Collectors.joining(System.lineSeparator()));
        serviceLines.close();
        return generatedService;
    }

    protected String readOutput(boolean slient) throws IOException {
        String output = "";
        output = this.console.toString();
        this.console.close();
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
        if (!slient) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }
}
