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
import io.ballerina.openapi.core.service.ServiceDeclarationGenerator;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for header generation.
 *
 * @since 2.0.0
 */
public class HeaderTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @DataProvider(name = "intHeaderTestData")
    public Object[][] intFormatTestData() {
        return new Object[][]{
                {"generators/service/swagger/headers/multiHeaderParam.yaml", "headers/header_parameters.bal"},
                {"generators/service/swagger/headers/header_integer_signed32.yaml",
                        "headers/header_integer_signed32.bal"},
                {"generators/service/swagger/headers/header_integer_invalid.yaml",
                        "headers/header_integer_invalid.bal"},
        };
    }

    //Scenario 03 - Header parameters.
    @Test(dataProvider = "intHeaderTestData", description = "Generate functionDefinitionNode for Header parameters")
    public void generateHeaderParameter(final String swaggerPath, final String balPath)
            throws IOException, OASTypeGenException {
        Path definitionPath = RES_DIR.resolve(swaggerPath);
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        final SyntaxTree syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(balPath, syntaxTree);
    }
}
