package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.apache.commons.lang3.StringUtils;
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
import static io.ballerina.openapi.TestUtil.OUT;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.TEST_DISTRIBUTION_PATH;

/**
 * This test class is for storing the schema related integrations.
 */
public class SchemaGenerationTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/schema");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Tests with record field has constraint value with union type.")
    public void constraintWithUnionType() throws IOException, InterruptedException {
        String openapiFilePath = "union_type.yaml";
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

        // Todo: Set a proper tes for capture the terminal out come for cli command.
        String out = "WARNING: constraints in OpenAPI contract will be ignored for the field `service_class`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates`, " +
                "as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_anyOf`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_oneOF_array`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_anyOf_array`, as " +
                "constraints are not supported on Ballerina union types";
        //Thread for wait out put generate
        Thread.sleep(5000);
        // compare generated file has not included constraint annotation for scenario record field.
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("types.bal", "schema/union.bal");
        process.waitFor();
    }

    @Test
    public void testConstraintWithArrayTypeWhenNullableEnable() throws IOException, InterruptedException {
        String openapiFilePath = "array.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--nullable");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("types.bal")));
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("types.bal", "schema/array.bal");
    }
}
