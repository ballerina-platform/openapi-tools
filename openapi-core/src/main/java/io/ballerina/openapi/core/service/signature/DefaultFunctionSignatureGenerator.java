package io.ballerina.openapi.core.service.signature;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.parameter.HeaderParameterGenerator;
import io.ballerina.openapi.core.service.parameter.QueryParameterGenerator;
import io.ballerina.openapi.core.service.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.service.response.ReturnTypeGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

public class DefaultFunctionSignatureGenerator extends FunctionSignatureGenerator {

    public DefaultFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                      String path) {
        ParametersGeneratorResult parametersGeneratorResult;
        try {
            parametersGeneratorResult = generateParameters(operation);
        } catch (BallerinaOpenApiException e) {
            throw new RuntimeException(e);
        }
        List<Node> params = new ArrayList<>(parametersGeneratorResult.requiredParameters());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            RequiredParameterNode nodeForRequestBody;
            if (requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGenerator = RequestBodyGenerator
                        .getRequestBodyGenerator(oasServiceMetadata, path);
                try {
                    nodeForRequestBody = requestBodyGenerator.createRequestBodyNode(requestBody);
                } catch (OASTypeGenException e) {
                    throw new RuntimeException(e);
                }
                params.add(nodeForRequestBody);
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }

        // For creating the order of the parameters in the function
        if (!parametersGeneratorResult.defaultableParameters().isEmpty()) {
            params.addAll(parametersGeneratorResult.defaultableParameters());
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, path);
        ReturnTypeDescriptorNode returnNode;
        try {
            returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

        return createFunctionSignatureNode(
                createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnNode);

    }

    /**
     * This function for generating operation parameters.
     *
     * @param operation OAS operation
     * @throws OASTypeGenException when the parameter generation fails.
     */
    public ParametersGeneratorResult generateParameters(Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        List<Node> requiredParams = new ArrayList<>();
        List<Node> defaultableParams = new ArrayList<>();
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        // Handle header and query parameters
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter : parameters) {
                Node param;
                if (parameter.get$ref() != null) {
                    String referenceType = extractReferenceType(parameter.get$ref());
                    parameter = oasServiceMetadata.getOpenAPI().getComponents().getParameters().get(referenceType);
                }
                if (parameter.getIn().trim().equals(GeneratorConstants.HEADER)) {
                    HeaderParameterGenerator headerParamGenerator = new HeaderParameterGenerator(oasServiceMetadata);
                    param = headerParamGenerator.generateParameterNode(parameter);
                    diagnostics.addAll(headerParamGenerator.getDiagnostics());
                    if (headerParamGenerator.isNullableRequired()) {
                        isNullableRequired = true;
                    }
                    if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                        defaultableParams.add(param);
                        defaultableParams.add(comma);
                    } else {
                        requiredParams.add(param);
                        requiredParams.add(comma);
                    }
                } else if (parameter.getIn().trim().equals(GeneratorConstants.QUERY)) {
                    // todo : need to check on this
                    if (parameter.getRequired() != null && parameter.getRequired() &&
                            (parameter.getSchema() != null && parameter.getSchema().getNullable() != null &&
                                    parameter.getSchema().getNullable())) {
                        isNullableRequired = true;
                    }
                    // type  BasicType boolean|int|float|decimal|string ;
                    // public type () |BasicType|BasicType []| map<json>;
                    QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(oasServiceMetadata);
                    param = queryParameterGenerator.generateParameterNode(parameter);
                    diagnostics.addAll(queryParameterGenerator.getDiagnostics());
                    if (param != null) {
                        if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                            defaultableParams.add(param);
                            defaultableParams.add(comma);
                        } else {
                            requiredParams.add(param);
                            requiredParams.add(comma);
                        }
                    }
                }
            }
        }
        return new ParametersGeneratorResult(requiredParams, defaultableParams);
    }

    /**
     * Resolve requestBody reference.
     */
    private RequestBody resolveRequestBodyReference(RequestBody requestBody) {
        if (requestBody.get$ref() != null) {
            try {
                String requestBodyName = extractReferenceType(requestBody.get$ref());
                requestBody = resolveRequestBodyReference(oasServiceMetadata.getOpenAPI().getComponents()
                        .getRequestBodies().get(requestBodyName.trim()));
            } catch (BallerinaOpenApiException e) {
                throw new RuntimeException();
            }
        }
        return requestBody;
    }

    public record ParametersGeneratorResult(List<Node> requiredParameters, List<Node> defaultableParameters) {}
}
