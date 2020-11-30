package org.ballerinalang.ballerina;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;
import org.ballerinalang.ballerina.openapi.convertor.service.ConverterConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class OpenApiConverterUtils {

    /**
     * This util for generating files when not available with specific service name.
     * @param servicePath
     * @param outPath
     * @throws IOException
     * @throws OpenApiConverterException
     * @return
     */
    public static void  generateOAS3DefinitionsAllService(Path servicePath, Path outPath)
            throws IOException, OpenApiConverterException {

        // Load project instance for single ballerina file
        Project project = SingleFileProject.load(servicePath);
        //travers and filter service

        // Take package name for project
        Package packageName = project.currentPackage();
        // Take module instance for traversing the sysntax tree
        Module currentModule = packageName.getDefaultModule();
        Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
        while (documentIterator.hasNext()) {
            DocumentId docId = documentIterator.next();
            Document doc = currentModule.document(docId);
            SyntaxTree syntaxTree = doc.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            for (Node node : modulePartNode.members()) {
                SyntaxKind syntaxKind = node.kind();
                // Load a listen_declaration for the server part in the yaml spec
                if (syntaxKind.equals(SyntaxKind.LISTENER_DECLARATION)) {

                }
                // Load a service Node
                if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                    ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                     String serviceName =  serviceNode.serviceName().toString();
                     String fileName = getOpenApiFileName(syntaxTree.filePath(), serviceName);
                     String ballerinaSource = readFromFile(servicePath);
                     String openApiSource = generateOAS3Definitions(ballerinaSource, serviceName);
                }
            }
        }
    }

    private static String getOpenApiFileName(String servicePath, String serviceName) {
//        Path file = servicePath.getFileName();
        String openApiFile;

        if (StringUtils.isNotBlank(serviceName)) {
            openApiFile = serviceName + ConverterConstants.OPENAPI_SUFFIX;
        } else {
            openApiFile = servicePath != null ?
                    FilenameUtils.removeExtension(servicePath.toString()) + ConverterConstants.OPENAPI_SUFFIX :
                    null;
        }

        return openApiFile + ConverterConstants.YAML_EXTENSION;
    }

    /**
     *
     * @param ballerinaSource
     * @param serviceName
     * @return
     */
    public static String generateOAS3Definitions(String ballerinaSource, String serviceName) {

    }

    private static String readFromFile(Path servicePath) throws IOException {
        String source = FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
        return source;
    }

}
