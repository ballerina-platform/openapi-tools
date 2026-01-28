/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.generators.common;

import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.OASModifier;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This contains the OAS modification tests.
 */
public class OASModifierTests {
    //TODO: enable these tests separately, currently fix was tested by using connectors in manually.
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/sanitizer").toAbsolutePath();
    @Test(description = "Functionality tests for getBallerinaOpenApiType", enabled = false)
    public void testForRecordName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("record.yaml");
        Path expectedPath = RES_DIR.resolve("modified_record.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASModifier oasModifier = new OASModifier();
        Map<String, String> proposedNameMapping = oasModifier.getProposedNameMapping(openAPI);
        OpenAPI modifiedOAS = oasModifier.modifyWithBallerinaNamingConventions(openAPI, proposedNameMapping);
        // file comparison
        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
        Assert.assertEquals(modifiedOAS, expectedFileContent);
    }

    public void pathParameter() {

    }

    // expect: not modify the query parameter names
    public void queryParameter() {

    }

    public void duplicateRecordName () {

    }

    public void pathAndRecordHasSameName() {

    }

    public void parameterNameHasBlank() {

    }

    @Test
    public void recursiveRecordName() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RES_DIR.resolve("recursive_record.yaml");
//        Path expectedPath = RES_DIR.resolve("modified_recursive_record.yaml");
//        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
//        OASSanitizer oasSanitizer = new OASSanitizer(openAPI);
//        OpenAPI sanitized = oasSanitizer.sanitized();
        // file comparison
//        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
//        Assert.assertEquals(sanitized, expectedFileContent);
    }

    // parameter in separate section
    // request section
    // response section
    // recursive section

    @Test(description = "Test parameter name generation with special characters at the beginning")
    public void testParameterNameWithLeadingSpecialChars() {
        // Test that different leading special characters generate unique parameter names
        String plusOne = OASModifier.getValidNameForParameter("+1");
        String minusOne = OASModifier.getValidNameForParameter("-1");
        String starOne = OASModifier.getValidNameForParameter("*1");
        String slashOne = OASModifier.getValidNameForParameter("/1");

        // Verify uniqueness
        Assert.assertNotEquals(plusOne, minusOne, "+1 and -1 should generate different parameter names");
        Assert.assertNotEquals(plusOne, starOne, "+1 and *1 should generate different parameter names");
        Assert.assertNotEquals(minusOne, starOne, "-1 and *1 should generate different parameter names");
        Assert.assertNotEquals(slashOne, plusOne, "/1 and +1 should generate different parameter names");

        // Verify expected format
        Assert.assertEquals(plusOne, "plus1", "+1 should generate 'plus1'");
        Assert.assertEquals(minusOne, "minus1", "-1 should generate 'minus1'");
        Assert.assertEquals(starOne, "star1", "*1 should generate 'star1'");
        Assert.assertEquals(slashOne, "slash1", "/1 should generate 'slash1'");
    }

    @Test(description = "Test parameter name generation preserves current behavior for special chars in middle")
    public void testParameterNameWithMiddleSpecialChars() {
        // Test that special characters in the middle are handled as before
        String userName = OASModifier.getValidNameForParameter("user_name");
        String userId = OASModifier.getValidNameForParameter("user-id");
        String userCount = OASModifier.getValidNameForParameter("user.count");

        // Verify current behavior is preserved (special chars removed, parts capitalized)
        Assert.assertEquals(userName, "userName", "user_name should generate 'userName'");
        Assert.assertEquals(userId, "userId", "user-id should generate 'userId'");
        Assert.assertEquals(userCount, "userCount", "user.count should generate 'userCount'");
    }

    @Test(description = "Test type name generation with special characters at the beginning")
    public void testTypeNameWithLeadingSpecialChars() {
        // Test that different leading special characters generate unique type names
        String plusType = OASModifier.getValidNameForType("+Response");
        String minusType = OASModifier.getValidNameForType("-Response");
        String atType = OASModifier.getValidNameForType("@Response");
        String hashType = OASModifier.getValidNameForType("#Response");

        // Verify uniqueness
        Assert.assertNotEquals(plusType, minusType, "+Response and -Response should generate different names");
        Assert.assertNotEquals(atType, hashType, "@Response and #Response should generate different names");

        // Verify expected format (first letter should be uppercase)
        Assert.assertEquals(plusType, "PlusResponse", "+Response should generate 'PlusResponse'");
        Assert.assertEquals(minusType, "MinusResponse", "-Response should generate 'MinusResponse'");
        Assert.assertEquals(atType, "AtResponse", "@Response should generate 'AtResponse'");
        Assert.assertEquals(hashType, "HashResponse", "#Response should generate 'HashResponse'");
    }

    @Test(description = "Test type name generation preserves current behavior for special chars in middle")
    public void testTypeNameWithMiddleSpecialChars() {
        // Test that special characters in the middle are handled as before
        String userInfo = OASModifier.getValidNameForType("user_info");
        String apiResponse = OASModifier.getValidNameForType("api-response");

        // Verify current behavior is preserved
        Assert.assertEquals(userInfo, "UserInfo", "user_info should generate 'UserInfo'");
        Assert.assertEquals(apiResponse, "ApiResponse", "api-response should generate 'ApiResponse'");
    }

    @Test(description = "Test multiple leading special characters")
    public void testMultipleLeadingSpecialChars() {
        // Test identifiers with multiple leading special characters
        String multiSpecial1 = OASModifier.getValidNameForParameter("++counter");
        String multiSpecial2 = OASModifier.getValidNameForParameter("--value");
        String multiSpecial3 = OASModifier.getValidNameForParameter("+-mixed");

        // Verify they generate unique names
        Assert.assertNotEquals(multiSpecial1, multiSpecial2);
        Assert.assertNotEquals(multiSpecial1, multiSpecial3);
        Assert.assertNotEquals(multiSpecial2, multiSpecial3);

        // Verify expected format
        Assert.assertEquals(multiSpecial1, "plusPlusCounter", "++counter should generate 'plusPlusCounter'");
        Assert.assertEquals(multiSpecial2, "minusMinusValue", "--value should generate 'minusMinusValue'");
        Assert.assertEquals(multiSpecial3, "plusMinusMixed", "+-mixed should generate 'plusMinusMixed'");
    }

    @Test(description = "Test that paths with similar but different version segments are handled correctly")
    public void testBasePathWithVersionMismatch() throws IOException, BallerinaOpenApiException {
        // Test case from https://github.com/ballerina-platform/ballerina-library/issues/8574
        // When paths have /v2/ and /v2.1/, the common base path extraction should not incorrectly
        // match /v2 as a prefix of /v2.1, causing paths to become .1/organizations/... instead of /...
        Path definitionPath = RES_DIR.resolve("basepath_version_mismatch.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASModifier oasModifier = new OASModifier();
        OpenAPI modifiedOAS = oasModifier.modifyWithCommonBasePath(openAPI);

        // Verify all paths still start with /
        io.swagger.v3.oas.models.Paths paths = modifiedOAS.getPaths();
        for (String pathKey : paths.keySet()) {
            Assert.assertTrue(pathKey.startsWith("/"),
                    "Path '" + pathKey + "' should start with '/' after base path modification");
        }

        // The paths should either be unchanged (if no common base path found) or properly modified
        // In this case, /v2/ and /v2.1/ should not share a common base path
        // since they are different path segments
        Assert.assertTrue(paths.containsKey("/v2/organizations") || paths.containsKey("/organizations"),
                "Path /v2/organizations should be present (original or with common path removed)");
        Assert.assertTrue(paths.containsKey("/v2.1/organizations/{organizationId}/accounts") ||
                        paths.containsKey("/organizations/{organizationId}/accounts"),
                "Path /v2.1/organizations/{organizationId}/accounts should be present (original or with common path removed)");
    }
}
