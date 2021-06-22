/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import org.ballerinalang.ballerina.util.OpenApiConverterException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ballerina conversion to OpenApi will test in this class.
 */
public class OpenApiConverterUtilsTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testMultiplePayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "multiple_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec with record payload")
    public void testRecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("record_payload_service.bal");
        String yamlFile = "record_payload.yaml";
        compareWithGeneratedFile(ballerinaFilePath, yamlFile);
    }

    @Test(description = "Generate OpenAPI spec with nested record payload")
    public void testNestedRecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nestedRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with nested payload")
    public void testNested2RecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nested2Record_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_2record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_array.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordFiledPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayRecordfield_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "record_field_array.yaml");
    }

    @Test(description = "Generate OpenAPI spec for resource has .")
    public void testPathscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec multiple resource")
    public void testPathscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with multipath including .")
    public void testPathscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with path parameter including .")
    public void testPathscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with simple query parameter")
    public void testQueryscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario01.yaml");
    }

    // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
    //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType[];
    @Test(description = "Generate OpenAPI spec with optional query parameter")
    public void testQueryscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array type query parameter")
    public void testQueryscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with optional type query parameter")
    public void testQueryscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with header type parameter")
    public void testHeadscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "header_scenario01.yaml");
    }

    @Test(description = "RequestBody without mediaType")
    public void testRequestBodyscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario01.yaml");
    }

    @Test(description = "RequestBody without mediaType, record type has payload")
    public void testRequestBodyscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario02.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type has payload")
    public void testRequestBodyscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario03.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with array field has payload")
    public void testRequestBodyscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario04.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario05() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario05.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario06() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario06.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario07() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario07.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario07.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario08() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario08.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario08.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario09() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario09.yaml");
    }

    // Need to handle further implementation
    @Test(description = "RequestBody without mediaType, payload has inline record type", enabled = false)
    public void testRequestBodyscenario10() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario10.yaml");
    }

    @Test(description = "Response scenario01 without return type")
    public void testResponse01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario01.yaml");
    }

    @Test(description = "Response scenario02 without return type")
    public void testResponse02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario02.yaml");
    }

    @Test(description = "Response scenario03 - return type with Record")
    public void testResponse03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario03.yaml");
    }

    @Test(description = "Response scenario 04 - Response has multiple responses without content type", enabled = false)
    public void testResponse04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario04.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema")
    public void testResponse05() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario05.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema", enabled = false)
    public void testResponse06() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario06.yaml");
    }

    @Test(description = "Response scenario 09 - Error response with a schema")
    public void testResponse09() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario09.yaml");
    }

    @Test(description = "Response scenario 10 - Array type response with a schema")
    public void testResponse10() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario10.yaml");
    }


    @Test(description = "Generate OpenAPI spec for build project")
    public void testMIMERecordFiledPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("mime_with_recordpayload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "mime_with_record_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response02.yaml");
    }

    //Listeners
    @Test(description = "Generate OpenAPI spec for single listener")
    public void testListners01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listner only have port")
    public void testListners02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listners")
    public void testListners03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple records")
    public void testMultipleRecords() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario11.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario11.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        deleteDirectory(this.tempDir);
    }

    private void deleteDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            //ignore
        }
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void deleteGeneratedFiles(String filename) {
        try {
            Files.deleteIfExists(this.tempDir.resolve(filename));
            Files.deleteIfExists(this.tempDir.resolve("schema.bal"));
        } catch (IOException e) {
            //Ignore the exception
        }
    }

    private void compareWithGeneratedFile(Path ballerinaFilePath, String yamlFile) throws OpenApiConverterException {
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"), yamlFile);
            Project project = ValidatorTest.getProject(ballerinaFilePath);
            SyntaxTree syntaxTree = ValidatorTest.getSyntaxTree(project);
            SemanticModel semanticModel = ValidatorTest.getSemanticModel(project);
            OpenAPIConverter openApiConverter = new OpenAPIConverter(syntaxTree, semanticModel);
            String generatedYaml = openApiConverter.generateOAS3Definition(syntaxTree, false);
            generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
            expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
            Assert.assertTrue(generatedYaml.contains(expectedYamlContent));

        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteDirectory(this.tempDir);
            System.gc();
        }
    }

//    @Test
//    public void testWithLS() {
//        WorkspaceManager workspaceManager = WorkspaceManager.getInstance(new LanguageServerContextImpl());
//
//        OpenAPIConverterService openAPIConverterService = new OpenAPIConverterService();
//        openAPIConverterService.init();
//
//    }
}
