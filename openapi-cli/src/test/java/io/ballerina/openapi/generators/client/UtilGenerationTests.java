package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.generators.common.GeneratorTestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getDiagnostics;

/**
Test util file generation for ballerina connectors.
 */
public class UtilGenerationTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/client/utils").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    private static final String CREATE_FORM_URLENCODED_REQUEST_BODY = "createFormURLEncodedRequestBody";
    private static final String GET_DEEP_OBJECT_STYLE_REQUEST = "getDeepObjectStyleRequest";
    private static final String GET_FORM_STYLE_REQUEST = "getFormStyleRequest";
    private static final String GET_SERIALIZED_ARRAY = "getSerializedArray";
    private static final String GET_ENCODED_URI = "getEncodedUri";
    private static final String GET_ORIGINAL_KEY = "getOriginalKey";
    private static final String GET_PATH_FOR_QUERY_PARAM = "getPathForQueryParam";
    private static final String GET_MAP_FOR_HEADERS = "getMapForHeaders";
    private static final String GET_SERIALIZED_RECORD_ARRAY = "getSerializedRecordArray";


    @Test(description = "Test default util file generation")
    public void testDefaultUtilFileGen() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/no_util.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        ballerinaClientGenerator.generateSyntaxTree();
        SyntaxTree utlisSyntaxTree = ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("client/ballerina/default_util.bal",
                utlisSyntaxTree);
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with query parameters")
    public void testUtilFileGenForQueryParams() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/query_param.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(CREATE_FORM_URLENCODED_REQUEST_BODY, GET_MAP_FOR_HEADERS);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();

        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with headers")
    public void testUtilFileGenForHeader() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/header.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(CREATE_FORM_URLENCODED_REQUEST_BODY,
                GET_DEEP_OBJECT_STYLE_REQUEST, GET_FORM_STYLE_REQUEST, GET_SERIALIZED_ARRAY,
                GET_ORIGINAL_KEY, GET_PATH_FOR_QUERY_PARAM, GET_SERIALIZED_RECORD_ARRAY);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with URL encoded request body")
    public void testUtilFileGenURLEncodedRequestBody() throws IOException, BallerinaOpenApiException,
            OASTypeGenException, FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/url_encoded.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(GET_PATH_FOR_QUERY_PARAM, GET_MAP_FOR_HEADERS);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with URL encoded " +
            "request body with encoding styles specified")
    public void testUtilFileGenURLEncodedRequestWithEncoding() throws IOException, BallerinaOpenApiException,
            ClientException, FormatterException, OASTypeGenException {
        Path definitionPath = RESDIR.resolve("swagger/url_encoded_with_map.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<String> invalidFunctionNames = Arrays.asList(GET_PATH_FOR_QUERY_PARAM, GET_MAP_FOR_HEADERS);
        Assert.assertTrue(checkUtil(invalidFunctionNames,
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()));
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition when all the scenarios are given")
    public void testCompleteUtilFileGen() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/complete_util_gen.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Test the utilsbal file generation when only in:query api-key auth given")
    public void testApiKeyauthUtilGen() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/apikey_with_no_query_param.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().
                getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    //Todo enable after multipart form data handing in RB with response
    @Test(description = "Validate the util functions generated for OpenAPI definition with multi part request bodies",
            enabled = false)
    public void testMultipartBodyParts() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/multipart_formdata.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator()
                .getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Validate the util functions generated for OpenAPI definition with multi part " +
            "request custom bodies", enabled = false)
    public void testMultipartCustomBodyParts() throws IOException, BallerinaOpenApiException,
            OASTypeGenException, FormatterException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/multipart_formdata_custom.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        SyntaxTree clientSyntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator()
                .getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(clientSyntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    private boolean checkUtil(List<String> invalidFunctionNames, SyntaxTree utilSyntaxTree) {
        ModulePartNode modulePartNode = utilSyntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        if (members.size() > 0) {
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
        return false;
    }

    private BallerinaClientGenerator getBallerinaClientGenerator(Path definitionPath) throws IOException,
            BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        return ballerinaClientGenerator;
    }

    @AfterClass
    public void cleanUp() throws IOException {
        GeneratorTestUtils.deleteGeneratedFiles();
    }
}

