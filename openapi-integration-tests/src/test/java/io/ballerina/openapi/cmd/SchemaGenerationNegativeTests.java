/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.OUT;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.TEST_DISTRIBUTION_PATH;
import static io.ballerina.openapi.TestUtil.assertOnErrorStream;

/**
 * This test class is for storing the schema related integrations to negative scenarios.
 */
public class SchemaGenerationNegativeTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/schema");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Tests with record field has constraint value with string type with unsupported patterns.")
    public void constraintWithUnsupportedStringPattern() throws IOException, InterruptedException {
        String openapiFilePath = "unsupported_string_pattern.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "openapi");
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());

        String balFile = "bal";

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        buildArgs.add(0, TEST_DISTRIBUTION_PATH.resolve(DISTRIBUTION_FILE_NAME).resolve("bin")
                .resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(buildArgs, ' '));
        ProcessBuilder pb = new ProcessBuilder(buildArgs);
        pb.directory(TEST_RESOURCE.toFile());
        Process process = pb.start();

        String out = "WARNING: skipped generation for unsupported pattern in ballerina: " +
                "^(?!(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)) \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^(?![0-9]+$)(?!-)[a-zA-Z0-9-]{2," +
                "49}[a-zA-Z0-9]$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: (https?:\\/\\/)?([\\da-z\\.-]+)\\." +
                "([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^[A-Za-z\\-\\_\\/]+$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^.*(?=.{6,1000})(?=.*\\d)" +
                "(?=.*[a-z])(?=.*[A-Z]).*$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^[\\x09\\x0A\\x0D\\x20\\x23\\x2D\\" +
                "x30-\\x39\\x40-\\x5A\\x5E-\\x5F\\x61-\\x7A\\x7E-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]{1,100}$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^[a-z0-9\\-\\_\\.]+$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: ^(?!\\s)(.*)(\\S)$ \n" +
                "WARNING: skipped generation for unsupported pattern in ballerina: \\+[0-9]{1,3}\\-[0-9()+\\-]{1,30} ";
        //Thread for wait out put generate
        Thread.sleep(5000);
        // compare generated file has not included constraint annotation for scenario record field.
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("types.bal", "schema/unsupported_string_pattern.bal");
        process.waitFor();
        assertOnErrorStream(process, out);
    }

    @Test(description = "Tests with record field has constraint value with string type with invalid patterns.")
    public void constraintWithStringInvalidPattern() throws IOException, InterruptedException {
        String openapiFilePath = "invalid_pattern_string.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "openapi");
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());

        String balFile = "bal";

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        buildArgs.add(0, TEST_DISTRIBUTION_PATH.resolve(DISTRIBUTION_FILE_NAME).resolve("bin")
                .resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(buildArgs, ' '));
        ProcessBuilder pb = new ProcessBuilder(buildArgs);
        pb.directory(TEST_RESOURCE.toFile());
        Process process = pb.start();

        String out = "WARNING: invalid flag in regular expression: (A)?(?(1)B|C) ";
        //Thread for wait out put generate
        Thread.sleep(5000);
        // compare generated file has not included constraint annotation for scenario record field.
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("types.bal", "schema/invalid_string_pattern.bal");
        process.waitFor();
        assertOnErrorStream(process, out);
    }
}
