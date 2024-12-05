/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.service.ServiceContractGenerator;
import io.ballerina.openapi.core.generators.service.ServiceDeclarationGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServiceContractTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service");
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "Test default service object type name")
    public void testDefaultServiceTypeNameInGeneratedService() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/default_service_type_name.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/default_service_type_name.bal", syntaxTree);
    }

    @Test(description = "Test custom service object type name")
    public void testCustomServiceTypeNameInGeneratedService() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/custom_service_type_name.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withServiceObjectTypeName("CustomServiceObjectTypeName")
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/custom_service_type_name.bal", syntaxTree);
    }

    @Test(description = "Test custom service object type name with special characters")
    public void testCustomServiceTypeNameWithSpecialCharacters() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/custom_name_with_special_characters.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withServiceObjectTypeName("CustomServiceObject-TypeName1")
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/custom_name_with_special_characters.bal", syntaxTree);
    }

    @Test(description = "Test custom service object type name with a whitespace")
    public void testCustomServiceTypeNameWithWhitespace() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/custom_name_with_whitespace.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withServiceObjectTypeName("CustomServiceObject TypeName2")
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/custom_name_with_whitespace.bal", syntaxTree);
    }

    @Test(description = "Test custom service object type name with an empty value")
    public void testCustomServiceTypeNameWithEmptyValue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/custom_name_with_empty_value.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withServiceObjectTypeName("")
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/custom_name_with_empty_value.bal", syntaxTree);
    }

    @Test(description = "Test custom service object type name with only whitespace")
    public void testCustomServiceTypeNameWithOnlyWhitespace() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/service_type/custom_name_only_whitespace.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withServiceObjectTypeName("    ")
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        ServiceContractGenerator serviceContractGenerator = new ServiceContractGenerator(oasServiceMetadata,
                ballerinaServiceGenerator.getFunctionsList());
        syntaxTree = serviceContractGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "service_type/custom_name_only_whitespace.bal", syntaxTree);
    }
}
