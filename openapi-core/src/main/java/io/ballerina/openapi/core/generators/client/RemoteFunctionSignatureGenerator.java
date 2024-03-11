package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.openapi.core.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_100;

public class RemoteFunctionSignatureGenerator implements FunctionSignatureGenerator {
    OpenAPI openAPI;
    Operation operation;
    List<ClientDiagnostic> diagnostics;
    Map<String, Parameter> parameters;
    public  RemoteFunctionSignatureGenerator(Operation operation, Map<String, Parameter> parameters) {
        this.operation = operation;
        this.parameters = parameters;
    }

    @Override
    public FunctionSignatureNode generateFunctionSignature() {

        // 1. parameters - path , query, requestBody, headers
        List<Node> parameterList = new ArrayList<>();
        List<Parameter> parameters = operation.getParameters();
        List<Node> defaultable = new ArrayList<>();
//        List<Node> deprecatedParamDocComments = new ArrayList<>();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
//                Doc
//                if (parameter.getDescription() != null && !parameter.getDescription().isBlank()) {
//                    MarkdownDocumentationNode paramAPIDoc =
//                            DocCommentsGenerator.createAPIParamDocFromSring(getValidName(
//                                    parameter.getName(), false), parameter.getDescription());
//                    remoteFunctionDoc.add(paramAPIDoc);
//                }

                //Deprecated annotation
//                List<AnnotationNode> parameterAnnotationNodeList =
//                        getParameterAnnotationNodeList(parameter, deprecatedParamDocComments);
                if (parameter.get$ref() != null) {
                    String paramType = null;
                    try {
                        paramType = extractReferenceType(parameter.get$ref());
                    } catch (BallerinaOpenApiException e) {
                        DiagnosticMessages diagnostic = OAS_CLIENT_100;
                        ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(diagnostic.getCode(),
                                diagnostic.getDescription(), parameter.get$ref());
                        diagnostics.add(clientDiagnostic);
                    }
                    parameter = openAPI.getComponents().getParameters().get(paramType);
                }

                String in = parameter.getIn();


                switch (in) {
                    case "path":
                        Node param = getPathParameters(parameter, createNodeList(parameterAnnotationNodeList));
                        // Path parameters are always required.
                        parameterList.add(param);
                        parameterList.add(createToken(COMMA_TOKEN));
                        break;
                    case "query":
                        Node paramq = getQueryParameters(parameter, createNodeList(parameterAnnotationNodeList));
                        if (paramq instanceof RequiredParameterNode) {
                            parameterList.add(paramq);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(paramq);
                            defaultable.add(comma);
                        }
                        break;
                    case "header":
                        Node paramh = getHeaderParameter(parameter, createNodeList(parameterAnnotationNodeList));
                        if (paramh instanceof RequiredParameterNode) {
                            parameterList.add(paramh);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(paramh);
                            defaultable.add(comma);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        // 2. return statements
        return null;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
