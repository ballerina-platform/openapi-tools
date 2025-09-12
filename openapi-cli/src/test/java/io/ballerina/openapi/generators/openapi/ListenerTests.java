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

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.service.mapper.ServersMapperImpl;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This Test class for storing all the endpoint related tests
 * {@link ServersMapperImpl}.
 */
public class ListenerTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }
    //Listeners
    @Test(description = "Generate OpenAPI spec for single listener")
    public void testListeners01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario01.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener only have port")
    public void testListeners02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario02.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listeners")
    public void testListeners03() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario03.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec for ExplicitNewExpressionNode listeners")
    public void testListeners04() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario04.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listeners")
    public void testListeners05() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario05.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario05.yaml");
    }

    @Test(description = "When given ballerina file contain some compilation issue.")
    public void testListeners06() {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario06.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null
                , false);
        List<OpenAPIMapperDiagnostic> errors = openApiConverter.getDiagnostics();
        Assert.assertTrue(errors.isEmpty());
    }

    @Test(description = "Generate OpenAPI spec for http load balancer listeners")
    public void testListeners07() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_http_load_balancer.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/with_check_key_word.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with named port arguments")
    public void testListenerWithNamedPort() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario07.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario07.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with named port arguments")
    public void testPositionalArgsWithEmptyListenerConfig() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario08.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario08.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testListenerWithPositionalPort() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario09.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario09.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testListenerWithNamedPortAndHostInConfig() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario10.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario10.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testListenerWithNamedPortAndHost() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario11.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario11.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testPositionalArgsWithPortAndHostConfig() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario12.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario12.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testNamedArgsWithPortAndEmptyConfig() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario13.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario13.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listener with positional port arguments")
    public void testPositionalPortAndNamedConfigWithHost() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_scenario14.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_scenario14.yaml");
    }

    @Test
    public void testListenerPortWithVariable() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_variable.bal");
        Path expectedFilePath = RES_DIR.resolve("expected_gen/listeners");
        List<String> expectedYamlFiles = List.of("api_default_openapi.yaml", "api_v1_openapi.yaml",
                "api_v2_openapi.yaml", "api_v3_openapi.yaml", "api_v4_openapi.yaml", "api_v5_openapi.yaml");
        List<OpenAPIMapperDiagnostic> diagnostics = TestUtils.compareGeneratedFiles(ballerinaFilePath,
                expectedFilePath, expectedYamlFiles);
        Assert.assertEquals(diagnostics.size(), 1);
        OpenAPIMapperDiagnostic diagnostic = diagnostics.getFirst();
        Assert.assertEquals(diagnostic.getCode(), "OAS_CONVERTOR_145");
        Assert.assertTrue(diagnostic.getMessage().contains("The server port is defined as a configurable. Hence, " +
                "using the default value to generate the server information"));
        Assert.assertTrue(diagnostic.getLocation().isPresent());
        Assert.assertEquals(diagnostic.getLocation().get().lineRange().toString(), "(4:26,4:30)");
    }

    private List<OpenAPIMapperDiagnostic> runNegativeListenerTest(Path ballerinaFilePath) throws IOException {
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);
        Assert.assertEquals(Files.list(tempDir).count(), 0);
        List<OpenAPIMapperDiagnostic> diagnostics = openApiConverter.getDiagnostics();
        TestUtils.deleteDirectory(tempDir);
        System.gc();
        return diagnostics;
    }

    private void validateCommonNegativeDiagnostics(List<OpenAPIMapperDiagnostic> diagnostics, String expectedCode,
                                                   String expectedMessageFragment, String expectedLocation) {
        Assert.assertEquals(diagnostics.size(), 2);
        OpenAPIMapperDiagnostic diagnostic1 = diagnostics.getFirst();
        Assert.assertEquals(diagnostic1.getCode(), expectedCode);
        Assert.assertTrue(diagnostic1.getMessage().contains(expectedMessageFragment));
        Assert.assertTrue(diagnostic1.getLocation().isPresent());
        Assert.assertEquals(diagnostic1.getLocation().get().lineRange().toString(), expectedLocation);

        OpenAPIMapperDiagnostic diagnostic2 = diagnostics.getLast();
        Assert.assertEquals(diagnostic2.getCode(), "OAS_CONVERTOR_141");
        Assert.assertTrue(diagnostic2.getMessage().contains("Generated OpenAPI definition does not have the server"));
    }

    @Test
    public void testListenerPortWithVariableNegative1() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_neg_1.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_143",
                "Unsupported expression found for the server port value", "(2:11,2:19)");
    }

    @Test
    public void testListenerPortWithVariableNegative2() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_neg_2.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_143", "Unsupported expression found for the" +
                " server port value", "(4:11,4:20)");
    }

    @Test
    public void testListenerPortWithVariableNegative3() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_neg_3.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_142", "The server port value cannot be " +
                "obtained since the value is provided via a variable defined outside the current module",
                "(2:11,2:34)");
    }

    @Test
    public void testListenerPortWithVariableNegative4() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_neg_4.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_143", "Unsupported expression found for the" +
                " server port value", "(2:16,2:24)");
    }

    @Test
    public void testListenerPortWithVariableNegative5() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_neg_5.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_146", "The configurable value provided for the" +
                        " port should have a default value to generate the server details",
                "(2:24,2:25)");
    }

    @Test
    public void testListenerPortWithVariableWithModules() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_modules/main.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "listeners/listener_port_variable_modules.yaml");
    }

    @Test
    // TODO: This should pass when the ModuleMemberVisitor is made module aware
    public void testListenerPortWithVariableNegativeWithModules() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners/listener_with_port_modules_neg/main.bal");
        List<OpenAPIMapperDiagnostic> diagnostics = runNegativeListenerTest(ballerinaFilePath);
        validateCommonNegativeDiagnostics(diagnostics, "OAS_CONVERTOR_142", "The server port value cannot be " +
                        "obtained since the value is provided via a variable defined outside the current module",
                "(4:41,4:49)");
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
