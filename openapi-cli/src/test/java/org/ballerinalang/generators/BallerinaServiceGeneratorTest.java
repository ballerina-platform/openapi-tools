package org.ballerinalang.generators;

import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

public class BallerinaServiceGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("generators"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);


    @Test(description = "Generate importors")
    public void generateImports() throws IOException, BallerinaOpenApiException {
        String definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners.yaml").toString();
        BallerinaServiceGenerator.genetrateSyntaxTree(definitionPath, "listeners", filter);
    }



}
