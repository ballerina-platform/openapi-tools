package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.swagger.v3.oas.models.OpenAPI;

public class DocCommentGeneratorImp {
    private final OpenAPI openAPI;
    private SyntaxTree syntaxTree;
    private final GenSrcFile.GenFileType type;
    private final boolean isProxyService;

    public DocCommentGeneratorImp(OpenAPI openAPI, SyntaxTree syntaxTree, GenSrcFile.GenFileType type,
                                  boolean isProxyService) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
        this.type = type;
        this.isProxyService = isProxyService;
    }
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        //separate the main sections in syntax tree
        // ex: get a client syntax tree

        //todo this is only integrate to type handler rest of the  service adn client integrate later
        switch (type) {
            case GEN_CLIENT:
                //generate client doc comments
                ClientDocCommentGenerator clientDocCommentGenerator = new ClientDocCommentGenerator(syntaxTree,
                        openAPI, true);
                syntaxTree = clientDocCommentGenerator.updateSyntaxTreeWithDocComments();
                break;
            case GEN_SERVICE:
                //generate service doc comments
                ServiceDocCommentGenerator serviceDocCommentGenerator = new ServiceDocCommentGenerator(syntaxTree,
                        openAPI, isProxyService);
                syntaxTree = serviceDocCommentGenerator.updateSyntaxTreeWithDocComments();
                break;
            case GEN_SERVICE_TYPE:
                //generate service type doc comments
                break;
            case GEN_TYPE:
                TypesDocCommentGenerator typesDocCommentGenerator = new TypesDocCommentGenerator(syntaxTree, openAPI);
                syntaxTree = typesDocCommentGenerator.updateSyntaxTreeWithDocComments();
                break;
            case TEST_SRC:
                //generate test src doc comments
                break;
            case CONFIG_SRC:
                //generate config src doc comments
                break;
            case UTIL_SRC:
                //generate util src doc comments
                break;
            case CACHE_SRC:
                //generate cache src doc comments
                break;
        }
        return syntaxTree;
    }
}
