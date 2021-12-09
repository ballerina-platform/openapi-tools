package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * This test class is for contain the openapi validator plugin tests.
 */
public class ValidatorTests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/validator");
    public static final String WHITESPACE_PATTERN = "\\s+";

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "OpenAPI validator plugin test for multiple services")
    public void testMultipleServices() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_1");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " ERROR [service.bal:(4:1,19:2)] Could not find q parameter in the Ballerina service " +
                "resource for the method get of the path /weather which is documented in the OpenAPI contract";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }
}
