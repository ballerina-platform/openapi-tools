/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCE;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.assertOnErrorStream;

/**
 * These negative tests are related to the `--export-openapi` flag for `bal build` command.
 */
public class BuildExtensionNegativeTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/build");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Ballerina package has compilation error")
    public void packageHasCompilationErrors() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "build");
        buildArgs.add("--export-openapi");
        Process process = getProcess(buildArgs, TEST_RESOURCE.resolve("package_with_compilation_issue"));

        String out = "ERROR [main.bal:(10:1,10:2)] invalid token '}'\n" +
                "error: compilation contains errors";
        //Thread for wait out put generate
        Thread.sleep(5000);
        // compare generated file has not included constraint annotation for scenario record field.
        Assert.assertFalse(Files.exists(RESOURCE.resolve("\"build/package_with_compilation_issue/target/openapi/")));
        process.waitFor();
        assertOnErrorStream(process, out);
    }
}
