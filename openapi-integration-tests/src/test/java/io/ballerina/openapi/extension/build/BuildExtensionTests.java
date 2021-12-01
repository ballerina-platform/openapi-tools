package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * TODO: add doc.
 */
public class BuildExtensionTests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/build");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Check ballerina to openapi generator command with annotation")
    public void annotationWithTitleAndVersion() throws IOException, InterruptedException {
        executeCommand("project_1");
    }

    private void executeCommand(String resourcePath) throws IOException {
        List<String> buildArgs = new LinkedList<>();
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve(resourcePath), buildArgs);
    }

}
