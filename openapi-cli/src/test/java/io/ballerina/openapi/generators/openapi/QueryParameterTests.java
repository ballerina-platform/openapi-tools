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

package io.ballerina.openapi.generators.openapi;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for query parameter mapping.
 */
public class QueryParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }
    // These are the data types that current ballerina query parameters can have.[effect from slbeta3]
    // type BasicType boolean|int|float|decimal|string ;
    // public type  QueryParamType <map>json | () |BasicType|BasicType[];

    @Test(description = "Required query parameter")
    public void testQueryScenario01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario01.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario01.yaml");
    }

    @Test(description = "When the query parameter has nullable enable it default behave as optional parameter")
    public void testQueryScenario02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario02.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario02.yaml");
    }

    @Test(description = "Require query parameter array type.")
    public void testQueryscenario03() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario03.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario03.yaml");
    }

    @Test(description = "Query parameter has array type with nullable value (ex: int[]? offset)")
    public void testQueryscenario04() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario04.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario04.yaml");
    }

    @Test(description = "Query parameter has service config with optional field false")
    public void testQueryscenario05() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario05.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario05.yaml");
    }

    @Test(description = "Query parameter has default parameter.")
    public void testQueryscenario06() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario06.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario06.yaml");
    }

    //Disable till http module support the nilable array `int?[]` in query parameter
    @Test(description = "Query parameter has array type with null values (ex: int?[] offset)", enabled = false)
    public void testQueryscenario07() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario07.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario07.yaml");
    }

    @Test(description = "Default parameter scenarios do not support with tool")
    public void testQueryscenario08() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario08.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario08.yaml");
    }

    @Test(description = "Default parameter scenarios")
    public void testQueryscenario09() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query/query_scenario09.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "query/query_scenario09.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        TestUtils.deleteDirectory(this.tempDir);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
