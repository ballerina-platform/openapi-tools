package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.generators.GeneratorConstants.URL;

/**
 * This class is used to generate util file syntax tree according to the generated client.
 */
public class BallerinaUtilGenerator {

    private boolean headersFound = false;
    private boolean queryParamsFound = false;
    private boolean requestBodyEncodingFound = false;

    private static final String createFormURLEncodedRequestBody = "createFormURLEncodedRequestBody";
    private static final String getDeepObjectStyleRequest = "getDeepObjectStyleRequest";
    private static final String getFormStyleRequest = "getFormStyleRequest";
    private static final String getSerializedArray = "getSerializedArray";
    private static final String getEncodedUri = "getEncodedUri";
    private static final String getOriginalKey = "getOriginalKey";
    private static final String getPathForQueryParam = "getPathForQueryParam";
    private static final String getMapForHeaders = "getMapForHeaders";


    /**
     * Set `queryParamsFound` flag to `true` when at least one query parameter found.
     *
     * @param flag     Function will be called only in the occasions where flag needs to be set to `true`
     */
    public void setQueryParamsFound(boolean flag) {
        this.queryParamsFound = flag;
    }

    /**
     * Set `headersFound` flag to `true` when at least one header found.
     *
     * @param flag     Function will be called only in the occasions where flag needs to be set to `true`
     */
    public void setHeadersFound(boolean flag) {
        this.headersFound = flag;
    }

    /**
     * Set `setRequestBodyEncodingFound` flag to `true` when at least one function found with URL-encoded request body.
     *
     * @param flag     Function will be called only in the occasions where value needs to be set to `true`.
     */
    public void setRequestBodyEncodingFound(boolean flag) {
        this.requestBodyEncodingFound = flag;
    }

    /**
     * Generates util file syntax tree.
     *
     * @return                      Syntax tree of the util.bal file
     * @throws URISyntaxException   When utils.bal file template is not found in the `resources/templates` directory.
     */
    public SyntaxTree generateUtilSyntaxTree() throws URISyntaxException {
        Set<String> functionNameList = new LinkedHashSet<>();
        if (requestBodyEncodingFound) {
            functionNameList.addAll(Arrays.asList(
                    createFormURLEncodedRequestBody, getDeepObjectStyleRequest, getFormStyleRequest,
                    getEncodedUri, getOriginalKey, getSerializedArray
            ));
        }
        if (queryParamsFound) {
            functionNameList.addAll(Arrays.asList(
                    getDeepObjectStyleRequest, getFormStyleRequest,
                    getEncodedUri, getOriginalKey, getSerializedArray, getPathForQueryParam
            ));
        }

        if (headersFound) {
            functionNameList.add(getMapForHeaders);
        }

        List<ModuleMemberDeclarationNode> functionDefinitionNodes = new ArrayList<>();

        URI uri = ClassLoader.getSystemResource("templates/").toURI();
        String mainPath = Paths.get(uri).toString();
        Path path = Paths.get(mainPath, "utils.bal");

        Project project = ProjectLoader.loadProject(path);
        Package currentPackage = project.currentPackage();
        DocumentId docId = currentPackage.getDefaultModule().documentIds().iterator().next();
        SyntaxTree syntaxTree = currentPackage.getDefaultModule().document(docId).syntaxTree();

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        for (ModuleMemberDeclarationNode node : members) {
            if (node.kind().equals(SyntaxKind.FUNCTION_DEFINITION)) {
                for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                    if (childNodeEntry.name().equals("functionName")) {
                        if (functionNameList.contains(childNodeEntry.node().get().toString())) {
                            functionDefinitionNodes.add(node);
                        }
                    }
                }
            }
        }

        List<ImportDeclarationNode> imports = new ArrayList<>();
        if (!(functionNameList.size() == 0 ||
                (functionNameList.size() == 1 && functionNameList.contains(getMapForHeaders)))) {
            ImportDeclarationNode importForHttp =
                    GeneratorUtils.getImportDeclarationNode(BALLERINA, URL);
            imports.add(importForHttp);
        }

        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode utilModulePartNode =
                createModulePartNode(importsList, createNodeList(functionDefinitionNodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree utilSyntaxTree = SyntaxTree.from(textDocument);
        return utilSyntaxTree.modifyWith(utilModulePartNode);
    }
}
