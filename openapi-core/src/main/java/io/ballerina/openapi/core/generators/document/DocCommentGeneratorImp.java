package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.swagger.v3.oas.models.OpenAPI;

public class DocCommentGeneratorImp {
    private OpenAPI openAPI;
    private SyntaxTree syntaxTree;
    private GenSrcFile.GenFileType type;

    public DocCommentGeneratorImp(OpenAPI openAPI, SyntaxTree syntaxTree, GenSrcFile.GenFileType type) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
        this.type = type;
    }
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        //separate the main sections in syntax tree
        // ex: get a client syntax tree
        switch (type) {
            case GEN_CLIENT:
                //generate client doc comments
                ClientDocCommentGenerator clientDocCommentGenerator = new ClientDocCommentGenerator(syntaxTree, openAPI);

                break;
            case GEN_SERVICE:
                //generate service doc comments
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
