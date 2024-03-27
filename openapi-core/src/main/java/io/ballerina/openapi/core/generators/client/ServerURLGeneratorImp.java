package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;

public class ServerURLGeneratorImp implements ServerURLGenerator {
    private final List<Server> servers;
    private final List<Diagnostic> diagnostics;
    public ServerURLGeneratorImp(List<Server> servers, List<Diagnostic> diagnostics) {
        this.servers = servers;
        this.diagnostics = diagnostics;
    }

    @Override
    public ParameterNode generateServerURL() {

            String serverURL;
            Server selectedServer = servers.get(0);
            if (!selectedServer.getUrl().startsWith("https:") && servers.size() > 1) {
                for (Server server : servers) {
                    if (server.getUrl().startsWith("https:")) {
                        selectedServer = server;
                        break;
                    }
                }
            }
            if (selectedServer.getUrl() == null) {
                //TODO: diagnostic log
                return getServiceURLNode("/");
            } else if (selectedServer.getVariables() != null) {
                ServerVariables variables = selectedServer.getVariables();
                for (ServerVariable variable  : variables.values()) {
                  if (variable.getDefault().isBlank() && variable.getEnum().size() == 0) {
                      //TODO: diagnostic log
                    return getServiceURLNode("/");
                  }
                }
                URL url;
                String resolvedUrl = GeneratorUtils.buildUrl(selectedServer.getUrl(), variables);
                try {
                    url = new URL(resolvedUrl);
                    serverURL = url.toString();
                } catch (MalformedURLException e) {
                    serverURL = "/";
                    //TODO: diagnostic log
//                    throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
//                            selectedServer.getUrl(), e);
                }

            } else {
                serverURL = selectedServer.getUrl();

            }
            return getServiceURLNode(serverURL);
    }


    /**
     * Generate the serviceUrl parameters of the client class init method.
     *
     * @param serviceUrl service Url given in the OpenAPI file
     * @return {@link DefaultableParameterNode} when server URl is given in the OpenAPI file
     * {@link RequiredParameterNode} when server URL is not given in the OpenAPI file
     */
    private ParameterNode getServiceURLNode(String serviceUrl) {
        ParameterNode serviceURLNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("string"));
        IdentifierToken serviceURLVarName = createIdentifierToken(GeneratorConstants.SERVICE_URL);

        if (serviceUrl.equals("/")) {
            serviceURLNode = createRequiredParameterNode(annotationNodes, serviceURLType, serviceURLVarName);
        } else {
            BasicLiteralNode expression = createBasicLiteralNode(STRING_LITERAL,
                    createIdentifierToken('"' + serviceUrl + '"'));
            serviceURLNode = createDefaultableParameterNode(annotationNodes, serviceURLType,
                    serviceURLVarName, createIdentifierToken("="), expression);
        }
        return serviceURLNode;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
