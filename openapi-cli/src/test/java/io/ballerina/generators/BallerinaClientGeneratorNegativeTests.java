package io.ballerina.generators;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.generators.GeneratorUtils.getImportDeclarationNode;

public class BallerinaClientGeneratorNegativeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/negative").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    @Test(description = "Generate imports")
    public void generateImports() {
        ImportDeclarationNode importDeclarationNode= getImportDeclarationNode("ballerina", "http");
        Assert.assertEquals(importDeclarationNode.orgName().get().orgName().text(),"ballerina");
        Assert.assertEquals(importDeclarationNode.moduleName().get(0).text(), "http");
    }

    @Test(description = "Generate remoteFunctions")
    public void generateRemoteFunctions() {
        ImportDeclarationNode importDeclarationNode= getImportDeclarationNode("ballerina", "http");
        Assert.assertEquals(importDeclarationNode.orgName().get().orgName().text(),"ballerina");
        Assert.assertEquals(importDeclarationNode.moduleName().get(0).text(), "http");
    }



}
