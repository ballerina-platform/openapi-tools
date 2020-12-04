/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.SyntaxTreeToJsonValidatorUtil;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RecordValidationTests {
    private static BType paramType = null;
    private static SyntaxTree syntaxTree = null;
    private static BallerinaSemanticModel semanticModel = null;
    private static Inputs inputs = new Inputs();
    private static List<ValidationError> validationErrorList = new ArrayList<>();
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/openapiValidator/ballerina-files");
    private static final Path RES_DIR = Paths.get("src/test/resources/openapiValidator/resources/recordValidation" +
            "/swagger/")
            .toAbsolutePath();
    private OpenAPI api;
    private Schema extractSchema;


    @Test(description = "Test for the valid", enabled = true)
    public void testValidRecord() throws OpenApiValidatorException {
        //Extract record type for the syntax tree
        inputs = BaseTests.returnBType("validRecord.bal", "validTest");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("validTests/validRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "User");
        validationErrorList = SyntaxTreeToJsonValidatorUtil.validate(extractSchema, inputs.getSyntaxTree(), inputs.getSemanticModel());
        Assert.assertTrue(validationErrorList.isEmpty());
    }

    @Test(description = "Test for the type mismatching", enabled = true)
    public void testTypeMismatchinFieldRecord() throws OpenApiValidatorException {
        //Extract record type for the syntax tree
        inputs = BaseTests.returnBType("typeMisMatch.bal", "invalidTest");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatch.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "User");
        validationErrorList = SyntaxTreeToJsonValidatorUtil.validate(extractSchema, inputs.getSyntaxTree(), inputs.getSemanticModel());
        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch)validationErrorList.get(0)).getFieldName(),"id");
    }

    @Test(description = "Test for valid the nested record", enabled = true)
    public void testNestedRecord() throws OpenApiValidatorException {
        //Extract record type for the syntax tree
        Path balfile = RESOURCE_DIRECTORY.resolve("validTest/nestedRecord.bal");
        inputs = BaseTests.returnBType("nestedRecord.bal", "validTest");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("validTests/nestedRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
        validationErrorList = SyntaxTreeToJsonValidatorUtil.validate(extractSchema, inputs.getSyntaxTree(), inputs.getSemanticModel());
//        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
//        Assert.assertEquals(((TypeMismatch)validationErrorList.get(0)).getFieldName(),"id");
    }
}
