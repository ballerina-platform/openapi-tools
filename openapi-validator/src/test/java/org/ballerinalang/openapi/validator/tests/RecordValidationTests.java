/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.openapi.validator.Constants;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.TypeSymbolToJsonValidatorUtil;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests related to record validations.
 */
public class RecordValidationTests {
    private static SyntaxTree syntaxTree = null;
    private static BallerinaSemanticModel semanticModel = null;
    private static Inputs inputs = new Inputs();
    private static List<ValidationError> validationErrorList = new ArrayList<>();
    private static final Path RES_DIR = Paths.get("src/test/resources/openapiValidator/resources/recordValidation" +
            "/swagger/")
            .toAbsolutePath();
    private OpenAPI api;
    private Schema extractSchema;


    @Test(description = "Test for the valid", enabled = false)
    public void testValidRecord() throws OpenApiValidatorException, IOException {
        //Extract record type for the syntax tree
        inputs = BaseTests.returnBType("validRecord.bal", "validTest", "");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("validTests/validRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "User");
//        validationErrorList = SyntaxTreeToJsonValidatorUtil.validate(extractSchema, inputs.getSyntaxTree(),
//                inputs.getSemanticModel());
        Assert.assertTrue(validationErrorList.isEmpty());
    }

    @Test(description = "Test for the type mismatching", enabled = false)
    public void testTypeMismatchFieldRecord() throws OpenApiValidatorException, IOException {
        //Extract record type for the syntax tree
        inputs = BaseTests.returnBType("typeMisMatch.bal", "invalidTest", "User");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("invalidTests/typeMisMatch.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "User");
//        validationErrorList = SyntaxTreeToJsonValidatorUtil.validate(extractSchema, inputs.getSyntaxTree(),
//                inputs.getSemanticModel());
        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch) validationErrorList.get(0)).getFieldName(), "id");
    }

    @Test(description = "Test for extra field in the record", enabled = false)
    public void testExtraFiledInRecord() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("extraFieldInRecord.bal", "invalidTest",
                "ExtraFieldInRecord");
        Path contractPath = RES_DIR.resolve("invalidTests/extraFieldInRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "ExtraFieldInRecord");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "ExtraFieldInRecord", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(((MissingFieldInJsonSchema) validationErrorList.get(0)).getFieldName(), "status");
    }

    @Test(description = "Test for valid the nested record", enabled = false)
    public void test4NestedRecord() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("nested4Record.bal", "invalidTest",
                "FourNestedComponent");
        Path contractPath = RES_DIR.resolve("invalidTests/nested4Record.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "FourNestedComponent");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "FourNestedComponent", null);

        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrorList.get(0).getFieldName(), "month");
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(description = "Test for valid the nested record", enabled = false)
    public void testTypeMisMatchNestedRecord() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("nestedRecord.bal", "invalidTest", "NestedRecord");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("invalidTests/nestedRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "NestedRecord");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "NestedRecord", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch) validationErrorList.get(0)).getFieldName(), "id");
    }

    @Test(description = "Test for valid the nested record", enabled = false)
    public void testExtraFieldInNestedRecord() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("extraFieldInNestedRecord.bal", "invalidTest",
                "ExtraFieldInRecord");
        Path contractPath = RES_DIR.resolve("invalidTests/extraFieldInNestedRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "ExtraFieldInRecord");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "ExtraFieldInRecord", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(((MissingFieldInJsonSchema) validationErrorList.get(0)).getFieldName(), "tag");
    }

    @Test(description = "Test for valid the nested record", enabled = false)
    public void testArrayTypeFieldInRecord() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("extraFieldInNestedRecord.bal", "invalidTest",
                "ExtraFieldInRecord");
        Path contractPath = RES_DIR.resolve("invalidTests/arrayTypeFilRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "ExtraFieldInRecord");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "ExtraFieldInRecord", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(((MissingFieldInJsonSchema) validationErrorList.get(0)).getFieldName(), "tag");
    }

    //Primitive
    @Test(description = "Test for valid the nested record", enabled = false)
    public void testInteger() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("typeMisMatchPrimitive.bal", "invalidTest", "");
        Path contractPath = RES_DIR.resolve("invalidTests/integerPrimitive.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(((TypeMismatch) validationErrorList.get(0)).getFieldName(), "paramName");
    }

    @Test(description = "Test for valid the nested record", enabled = false)
    public void testArray() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("arrayB.bal", "invalidTest", "");
        Path contractPath = RES_DIR.resolve("invalidTests/arrayB.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(), ""
                        , null);

        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrorList.get(0).getFieldName(), "array");
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(description = "Test for valid the nested array", enabled = false)
    public void testNestedArray() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("arrayNB.bal", "invalidTest", "");
        Path contractPath = RES_DIR.resolve("invalidTests/arrayNB.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getSchema(api, "/user/{userId}");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "", null);

        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrorList.get(0).getFieldName(), "array");
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(enabled = false, description = "Type mismatch with record array")
    public void testRecordArrayType() throws IOException, OpenApiValidatorException {
        inputs = BaseTests.returnBType("arrayRB.bal", "invalidTest", "");
        Path contractPath = RES_DIR.resolve("invalidTests/arrayRB.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getSchema(api, "/user/{category}");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(), ""
                        , null);

        Assert.assertTrue(validationErrorList.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationErrorList.get(0).getFieldName(), "id");
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationErrorList).get(0)).getTypeBallerinaType(),
                Constants.Type.STRING);
    }

    //need to do schema to record
    @Test(description = "Test for extra field in the schema", enabled = false)
    public void testExtraFiledInSchema() throws OpenApiValidatorException, IOException {
        inputs = BaseTests.returnBType("extraFieldInRecord.bal", "invalidTest",
                "ExtraFieldInRecord");
        //Load yaml file
        Path contractPath = RES_DIR.resolve("invalidTests/extraFieldInRecord.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        extractSchema = ValidatorTest.getComponet(api, "ExtraFieldInRecord");
        validationErrorList = TypeSymbolToJsonValidatorUtil
                .validate(extractSchema, inputs.getParamType(), inputs.getSyntaxTree(), inputs.getSemanticModel(),
                        "", null);
        Assert.assertTrue(validationErrorList.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(((MissingFieldInJsonSchema) validationErrorList.get(0)).getFieldName(), "status");
    }

}
