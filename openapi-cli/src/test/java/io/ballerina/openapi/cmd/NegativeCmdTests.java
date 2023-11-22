package io.ballerina.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is to store negative tests related to openAPI CLI.
 */
public class NegativeCmdTests extends OpenAPICommandTest {
    @Test(description = "Test for invalid ballerina package in `add` sub command")
    public void testInvalidBallerinaPackage() throws IOException {
        Path resourceDir = Paths.get(System.getProperty("user.dir")).resolve("build/resources/test");
        Path packagePath = resourceDir.resolve(Paths.get("cmd"));
        String[] addArgs = {"--input", "petstore.yaml", "-p", packagePath.toString(),
                "--module", "delivery", "--nullable", "--license", "license.txt", "--mode", "client",
                "--client-methods", "resource"};
        Add add = new Add(printStream,  false);
        new CommandLine(add).parseArgs(addArgs);
        add.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("ERROR: invalid Ballerina package directory:"));
    }
}
