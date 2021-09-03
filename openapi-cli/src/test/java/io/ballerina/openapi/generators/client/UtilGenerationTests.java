package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;

/**
Test util file generation for ballerina connectors.
 */
public class UtilGenerationTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/client/utils").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    private static final String createFormURLEncodedRequestBody = "createFormURLEncodedRequestBody";
    private static final String getDeepObjectStyleRequest = "getDeepObjectStyleRequest";
    private static final String getFormStyleRequest = "getFormStyleRequest";
    private static final String getSerializedArray = "getSerializedArray";
    private static final String getEncodedUri = "getEncodedUri";
    private static final String getOriginalKey = "getOriginalKey";
    private static final String getPathForQueryParam = "getPathForQueryParam";
    private static final String getMapForHeaders = "getMapForHeaders";

    @Test(description = "Test empty util file generation")
    public void testEmptyUtilFileGen() throws IOException, BallerinaOpenApiException,
            FormatterException, URISyntaxException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/no_util.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        List<String> invalidFunctionNames = Arrays.asList(createFormURLEncodedRequestBody, getDeepObjectStyleRequest,
                getFormStyleRequest, getSerializedArray, getEncodedUri, getOriginalKey,
                getPathForQueryParam, getMapForHeaders);

        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with query parameters")
    public void testUtilFileGenForQueryParams() throws IOException, BallerinaOpenApiException,
            FormatterException, URISyntaxException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/query_param.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(createFormURLEncodedRequestBody, getMapForHeaders);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with headers")
    public void testUtilFileGenForHeader() throws IOException, BallerinaOpenApiException,
            FormatterException, URISyntaxException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/header.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(createFormURLEncodedRequestBody, getDeepObjectStyleRequest,
                getFormStyleRequest, getSerializedArray, getEncodedUri, getOriginalKey,
                getPathForQueryParam);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with URL encoded request body")
    public void testUtilFileGenURLEncodedRequestBody() throws IOException, BallerinaOpenApiException,
            FormatterException, URISyntaxException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/url_encoded.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(getMapForHeaders);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    private boolean checkUtil(List<String> invalidFunctionNames, SyntaxTree utilSyntaxTree) {
        ModulePartNode modulePartNode = utilSyntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind().equals(SyntaxKind.FUNCTION_DEFINITION)) {
                for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                    if (childNodeEntry.name().equals("functionName")) {
                        if (invalidFunctionNames.contains(childNodeEntry.node().get().toString())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}

