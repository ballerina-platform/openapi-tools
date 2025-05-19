/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MarkdownCodeLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownCodeLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.replaceContentWithinBrackets;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIDescriptionDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDocFromString;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDeprecatedAnnotationDetails;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDisplayAnnotation;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getParameterDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getRequestBodyDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getResponseDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.updatedAnnotationInParameterNode;

public class ServiceDocCommentGenerator implements DocCommentsGenerator {
    OpenAPI openAPI;
    SyntaxTree syntaxTree;

    public ServiceDocCommentGenerator(SyntaxTree syntaxTree, OpenAPI openAPI, boolean isProxyService) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
    }
    @Override
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        //collect all the operation details
        HashMap<String, OperationDetails> operationDetailsMap = new HashMap<>();
        Paths paths = openAPI.getPaths();
        extractOperations(operationDetailsMap, paths);
        //Generate type doc comments
        Node rootNode = syntaxTree.rootNode();
        ModulePartNode modulePartNode = (ModulePartNode) rootNode;
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        List<ModuleMemberDeclarationNode> updatedMembers = new ArrayList<>();
        members.forEach(member -> {
            if (member.kind().equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode classDef = (ServiceDeclarationNode) member;
                HashMap<String, Node> updatedList = new HashMap<>();
                NodeList<Node> classMembers = classDef.members();
                //sort these members according to .toString();
                List<String> sortedMembers = new ArrayList<>();
                List<Node> clientInitNodes = new ArrayList<>();
                classMembers.forEach(classMember -> {
                    String sortKey = "";
                    if (classMember.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                        FunctionDefinitionNode funcDef = (FunctionDefinitionNode) classMember;
                            sortKey = funcDef.toSourceCode();
                            sortedMembers.add(sortKey);
                            NodeList<Node> nodes = funcDef.relativeResourcePath();
                            StringBuilder path = new StringBuilder();
                            for (Node node: nodes) {
                                path.append(node.toString().replace("\"", ""));
                            }
                            String key = replaceContentWithinBrackets(path.toString(), "XXX") + "_" +
                                    funcDef.functionName().text();
                            funcDef = updateDocCommentsForFunctionNode(operationDetailsMap, funcDef, key);

                        classMember = funcDef;
                    } else {
                        clientInitNodes.add(classMember);
                    }
                    updatedList.put(sortKey, classMember);
                });
                //sort the members
                List<Node> sortedNodes = new ArrayList<>();
                sortedNodes.addAll(clientInitNodes);
                sortedMembers.sort(String::compareTo);
                for (String memberStr: sortedMembers) {
                    sortedNodes.add(updatedList.get(memberStr));
                }
                member = classDef.modify(
                        classDef.metadata().orElse(null),
                        classDef.qualifiers(),
                        classDef.serviceKeyword(),
                        classDef.typeDescriptor().orElse(null),
                        classDef.absoluteResourcePath(),
                        classDef.onKeyword(),
                        classDef.expressions(),
                        classDef.openBraceToken(),
                        updatedList.isEmpty() ? classDef.members() : createNodeList(sortedNodes),
                        classDef.closeBraceToken(),
                        classDef.semicolonToken().orElse(null));
            }

            updatedMembers.add(member);
            NodeList<ModuleMemberDeclarationNode> clientMembers = AbstractNodeFactory.createNodeList(updatedMembers);
            ModulePartNode updatedmodulePartNode = modulePartNode.modify(modulePartNode.imports(), clientMembers,
                    modulePartNode.eofToken());

            syntaxTree = syntaxTree.modifyWith(updatedmodulePartNode);
        });
        return syntaxTree;
    }

    private void extractOperations(HashMap<String, OperationDetails> operationDetailsMap, Paths paths) {
        paths.forEach((path, pathItem) -> {
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
                PathItem.HttpMethod method = entry.getKey();
                Operation operation = entry.getValue();
                path = path.equals("/") ? "." : path;
                String key = replaceContentWithinBrackets(path.replaceFirst("/", ""),
                        "XXX") + "_" + method.name().toLowerCase(Locale.ENGLISH);
                operationDetailsMap.put(key, new OperationDetails(operation.getOperationId(),
                        operation, path, method.name()));
            }
        });
    }

    private  FunctionDefinitionNode updateDocCommentsForFunctionNode(
            HashMap<String, OperationDetails> operationDetailsMap, FunctionDefinitionNode funcDef, String key) {
        OperationDetails operationDetails = operationDetailsMap.get(key);
        if (operationDetails != null) {
            List<Node> docs = new ArrayList<>();
            List<AnnotationNode> annotations = new ArrayList<>();
            Operation operation = operationDetails.operation();
            //function main comment
            if (operation.getSummary() != null) {
                docs.addAll(createAPIDescriptionDoc(operation.getSummary(), true));
            } else if (operation.getDescription() != null) {
                docs.addAll(createAPIDescriptionDoc(operation.getDescription(), true));
            }
            //function display annotation
            if (operation.getExtensions() != null) {
                extractDisplayAnnotation(operation.getExtensions(), annotations);
            }
            FunctionSignatureNode functionSignatureNode = funcDef.functionSignature();
            //function parameters
            if (operation.getParameters() != null) {
                SeparatedNodeList<ParameterNode> parameters = functionSignatureNode.parameters();
                List<Node> updatedParamsRequired = new ArrayList<>();
                List<Node> updatedParamsDefault = new ArrayList<>();

                HashMap<String, ParameterNode> collection = getParameterNodeHashMap(parameters);
                //todo parameter reference
                updateParameterNodes(docs, openAPI, operation, updatedParamsRequired, updatedParamsDefault,
                        collection);
                updatedParamsRequired.addAll(updatedParamsDefault);
                if (!updatedParamsRequired.isEmpty()) {
                    updatedParamsRequired.remove(updatedParamsRequired.size() - 1);
                    functionSignatureNode = functionSignatureNode.modify(
                            functionSignatureNode.openParenToken(),
                            createSeparatedNodeList(updatedParamsRequired),
                            functionSignatureNode.closeParenToken(),
                            functionSignatureNode.returnTypeDesc().orElse(null));

                }
            }
            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                requestBodyDoc(docs, requestBody);
            }
            //todo response
            if (operation.getResponses() != null) {
                ApiResponses responses = operation.getResponses();
                Set<Map.Entry<String, ApiResponse>> entrySet = responses.entrySet();
                if (entrySet.size() > 1) {
                    MarkdownParameterDocumentationLineNode returnDoc = createAPIParamDoc("return",
                            "returns can be any of following types");
                    docs.add(returnDoc);
                    for (Map.Entry<String, ApiResponse> response : entrySet) {
                        String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                        Optional<String> responseDescription = getResponseDescription(response.getValue(),
                                openAPI.getComponents());
                        if (responseDescription.isPresent()) {
                            if (code == null) {
                                code = "Response";
                            }
                            MarkdownCodeLineNode returnDocLine = createMarkdownCodeLineNode(
                                    createToken(SyntaxKind.HASH_TOKEN), createIdentifierToken(String
                                            .format("http:%s (%s)", code, responseDescription.get()
                                                    .replaceAll("\n", "\n# "))));
                            docs.add(returnDocLine);
                        }
                    }
                } else if (entrySet.size() == 1) {
                    Map.Entry<String, ApiResponse> response = responses.entrySet().iterator().next();
                    Optional<String> responseDescription = getResponseDescription(response.getValue(),
                            openAPI.getComponents());
                    if (responseDescription.isPresent()) {
                        MarkdownParameterDocumentationLineNode returnDoc = createAPIParamDoc("return",
                                responseDescription.get());
                        docs.add(returnDoc);
                    }
                }
            }

            MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(docs));
            Optional<MetadataNode> metadata = funcDef.metadata();
            MetadataNode metadataNode;
            if (metadata.isEmpty()) {
                metadataNode = createMetadataNode(documentationNode, createNodeList(annotations));
            } else {
                metadataNode = metadata.get();
                metadataNode = createMetadataNode(documentationNode,
                        metadataNode.annotations().isEmpty() ? createNodeList(annotations) :
                                metadataNode.annotations().addAll(annotations));
            }
            funcDef = funcDef.modify(
                    funcDef.kind(),
                    metadataNode,
                    funcDef.qualifierList(),
                    funcDef.functionKeyword(),
                    funcDef.functionName(),
                    funcDef.relativeResourcePath(),
                    functionSignatureNode,
                    funcDef.functionBody());
        }
        return funcDef;
    }

    private void requestBodyDoc(List<Node> docs, RequestBody requestBody) {
        if (requestBody.get$ref() != null) {
            try {
                requestBody = openAPI.getComponents().getRequestBodies().get(
                        extractReferenceType(requestBody.get$ref()));
            } catch (BallerinaOpenApiException e) {
                requestBody = new RequestBody();
            }
        }
        Content content = requestBody.getContent();
        final String[] paramName = {"http:Request"};
        if (content != null) {
            content.entrySet().stream().findFirst().ifPresent(mediaType -> {
                paramName[0] = getBallerinaMediaType(mediaType.getKey(), true);
            });
        } else {
            paramName[0] = "http:Request";
        }

        Optional<String> requestBodyDescription = getRequestBodyDescription(requestBody, openAPI.getComponents());
        if (requestBodyDescription.isPresent()) {
            String description = requestBodyDescription.get().split("\n")[0];
            docs.add(createAPIParamDoc(paramName[0].equals("http:Request") ? "request" : "payload", description));
        }
    }

    private static void updateParameterNodes(List<Node> docs, OpenAPI openAPI, Operation operation,
                                             List<Node> updatedParamsRequired, List<Node> updatedParamsDefault,
                                             HashMap<String, ParameterNode> collection) {
        List<Node> deprecatedParamDocComments = new ArrayList<>();
        operation.getParameters().forEach(parameter -> {
            List<AnnotationNode> paramAnnot = new ArrayList<>();
            String parameterName = parameter.getName();
            if (parameter.getIn() != null && (parameter.getIn().equals("path") || parameter.getIn().equals("header") ||
                    (parameter.getIn().equals("query")))) {
                parameterName = escapeIdentifier(parameter.getName());
                // add deprecated annotation
                if (parameter.getDeprecated() != null && parameter.getDeprecated()) {
                    extractDeprecatedAnnotationDetails(deprecatedParamDocComments, parameter, paramAnnot);
                }

                if (parameter.getExtensions() != null) {
                    extractDisplayAnnotation(parameter.getExtensions(), paramAnnot);
                    ParameterNode parameterNode = collection.get(parameterName);
                    updatedDisplayAnnotationInParameterNode(updatedParamsRequired, updatedParamsDefault,
                            paramAnnot, parameterNode);
                }

                ParameterNode parameterNode = collection.get(parameterName);
                updatedAnnotationInParameterNode(updatedParamsRequired, updatedParamsDefault,
                        paramAnnot, parameterNode);
            }
            Optional<String> parameterDescription = getParameterDescription(parameter, openAPI.getComponents());
            if (parameterDescription.isPresent()) {
                docs.add(createAPIParamDocFromString(parameterName, parameterDescription.get()));
            }
        });
    }

    private static HashMap<String, ParameterNode> getParameterNodeHashMap(SeparatedNodeList<ParameterNode> parameters) {
        HashMap<String, ParameterNode> collection = new HashMap<>();
        for (ParameterNode node : parameters) {
            if (node instanceof RequiredParameterNode reParam) {
                collection.put(reParam.paramName().get().toString(), reParam);
            } else if (node instanceof DefaultableParameterNode deParam) {
                collection.put(deParam.paramName().get().toString(), deParam);
            }
        }
        return collection;
    }

    private static void updatedDisplayAnnotationInParameterNode(List<Node> updatedParamsRequired,
                                                                List<Node> updatedParamsDefault,
                                                                List<AnnotationNode> paramAnnot,
                                                                ParameterNode parameterNode) {
        if (parameterNode != null) {
            if (parameterNode instanceof RequiredParameterNode reParam) {
                updatedParamsRequired.add(reParam.modify(
                        reParam.annotations().isEmpty() ? createNodeList(paramAnnot) :
                                reParam.annotations().addAll(paramAnnot),
                        reParam.typeName(),
                        reParam.paramName().orElse(null)));
                updatedParamsRequired.add(createToken(SyntaxKind.COMMA_TOKEN));
            } else if (parameterNode instanceof DefaultableParameterNode deParam) {
                updatedParamsDefault.add(deParam.modify(
                        deParam.annotations().isEmpty() ? createNodeList(paramAnnot) :
                                deParam.annotations().addAll(paramAnnot),
                        deParam.typeName(),
                        deParam.paramName().orElse(null),
                        deParam.equalsToken(),
                        deParam.expression()));
                updatedParamsDefault.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }
    }
}
