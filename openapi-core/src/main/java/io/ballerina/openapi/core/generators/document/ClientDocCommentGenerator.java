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
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
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
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADERS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERIES;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.replaceContentWithinBrackets;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIDescriptionDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDocFromString;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDeprecatedAnnotation;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDeprecatedAnnotationDetails;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDisplayAnnotation;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getParameterDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getRequestBodyDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getResponseDescription;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.updatedAnnotationInParameterNode;

public class ClientDocCommentGenerator implements DocCommentsGenerator {
    OpenAPI openAPI;
    SyntaxTree syntaxTree;
    boolean isResource;

    public ClientDocCommentGenerator(SyntaxTree syntaxTree, OpenAPI openAPI, boolean isResource) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
        this.isResource = isResource;
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
            if (member.kind().equals(SyntaxKind.CLASS_DEFINITION)) {
                ClassDefinitionNode classDef = (ClassDefinitionNode) member;
                List<Node> updatedList = new LinkedList<>();
                NodeList<Node> classMembers = classDef.members();
                classMembers.forEach(classMember -> {
                    if (classMember.kind().equals(SyntaxKind.OBJECT_METHOD_DEFINITION) ||
                            classMember.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                        FunctionDefinitionNode funcDef = (FunctionDefinitionNode) classMember;

                        //remote : operationId
                        if (isResource) {
                            NodeList<Node> nodes = funcDef.relativeResourcePath();
                            StringBuilder path = new StringBuilder();
                            for (Node node: nodes) {
                                path.append(DocCommentsGeneratorUtil.unescapeIdentifier(node.toString()
                                        .replace("\"", "")));
                            }
                            String key = replaceContentWithinBrackets(path.toString(), "XXX") + "_" +
                                    funcDef.functionName().text();
                            funcDef = updateDocCommentsForFunctionNode(operationDetailsMap, funcDef, key);
                        } else {
                            String key = funcDef.functionName().text();
                            funcDef = updateDocCommentsForFunctionNode(operationDetailsMap, funcDef, key);
                        }
                        classMember = funcDef;
                    }
                    updatedList.add(classMember);
                });
                classDef = classDef.modify(
                        classDef.metadata().orElse(null),
                        classDef.visibilityQualifier().orElse(null),
                        classDef.classTypeQualifiers(),
                        classDef.classKeyword(),
                        classDef.className(),
                        classDef.openBrace(),
                        updatedList.isEmpty() ? classDef.members() : createNodeList(updatedList),
                        classDef.closeBrace(),
                        classDef.semicolonToken().orElse(null));
                member = classDef;
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
                if (!isResource) {
                    operationDetailsMap.put(operation.getOperationId(),
                            new OperationDetails(operation.getOperationId(), operation, path, method.name()));
                } else {
                    path = path.equals("/") ? "." : path;
                    String key = replaceContentWithinBrackets(path.replaceFirst("/", ""),
                            "XXX") + "_" + method.name().toLowerCase(Locale.ENGLISH);
                    operationDetailsMap.put(key, new OperationDetails(operation.getOperationId(),
                            operation, path, method.name()));
                }
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
                List<Node> updatedIncludedParam = new ArrayList<>();

                HashMap<String, ParameterNode> collection = getParameterNodeHashMap(parameters);
                //todo parameter reference
                updateParameterNodes(docs, openAPI, operation, updatedParamsRequired, updatedParamsDefault,
                        collection);
                if (collection.size() > 0) {
                    collection.forEach((keyParam, value) -> {
                        if (value instanceof RequiredParameterNode reParam) {
                            updatedParamsRequired.add(reParam);
                            updatedParamsRequired.add(createToken(SyntaxKind.COMMA_TOKEN));
                        } else if (value instanceof DefaultableParameterNode deParam) {
                            updatedParamsDefault.add(deParam);
                            updatedParamsDefault.add(createToken(SyntaxKind.COMMA_TOKEN));
                        } else if (value instanceof IncludedRecordParameterNode incParam) {
                            updatedIncludedParam.add(incParam);
                            updatedIncludedParam.add(createToken(SyntaxKind.COMMA_TOKEN));
                        }
                    });
                }
                updatedParamsRequired.addAll(updatedParamsDefault);
                updatedParamsRequired.addAll(updatedIncludedParam);
                if (!updatedParamsRequired.isEmpty()) {
                    if (updatedParamsRequired.get(updatedParamsRequired.size() - 1) instanceof Token) {
                        updatedParamsRequired.remove(updatedParamsRequired.size() - 1);
                    }
                    functionSignatureNode = functionSignatureNode.modify(
                            functionSignatureNode.openParenToken(),
                            createSeparatedNodeList(updatedParamsRequired),
                            functionSignatureNode.closeParenToken(),
                            functionSignatureNode.returnTypeDesc().orElse(null));

                }
            } else {
                docs.add(createAPIParamDoc(HEADERS, "Headers to be sent with the request"));
            }
            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                addRequestBodyDoc(docs, requestBody);
            }
            //todo response
            if (operation.getResponses() != null) {
                ApiResponses responses = operation.getResponses();
                Collection<ApiResponse> values = responses.values();
                Iterator<ApiResponse> iteratorRes = values.iterator();
                if (iteratorRes.hasNext()) {
                    ApiResponse response = iteratorRes.next();
                    Optional<String> responseDescription = getResponseDescription(response, openAPI.getComponents());
                    if (responseDescription.isPresent()) {
                        MarkdownParameterDocumentationLineNode returnDoc = createAPIParamDoc("return",
                                responseDescription.get());
                        docs.add(returnDoc);
                    }
                }
            }
            if (operation.getDeprecated() != null && operation.getDeprecated()) {
                extractDeprecatedAnnotation(operation.getExtensions(),
                        docs, annotations);
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

    private void addRequestBodyDoc(List<Node> docs, RequestBody requestBody) {
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
        boolean hasQueryParams = operation.getParameters().stream().anyMatch(
                parameter -> parameter.getIn().equals("query"));
        operation.getParameters().forEach(parameter -> {
            if (parameter.get$ref() != null) {
                try {
                    parameter = GeneratorMetaData.getInstance().getOpenAPI().getComponents()
                            .getParameters().get(extractReferenceType(parameter.get$ref()));
                } catch (InvalidReferenceException e) {
                    return;
                }
            }

            if (Objects.isNull(parameter.getIn()) || !parameter.getIn().equals("path")) {
                return;
            }

            List<AnnotationNode> paramAnnot = new ArrayList<>();
            String parameterName = escapeIdentifier(parameter.getName());

            // add deprecated annotation
            if (parameter.getDeprecated() != null && parameter.getDeprecated()) {
                extractDeprecatedAnnotationDetails(deprecatedParamDocComments, parameter, paramAnnot);
            }
            if (parameter.getExtensions() != null) {
                extractDisplayAnnotation(parameter.getExtensions(), paramAnnot);

            }
            ParameterNode parameterNode = collection.get(parameterName);
            updatedAnnotationInParameterNode(updatedParamsRequired, updatedParamsDefault,
                    paramAnnot, parameterNode);
            collection.remove(parameterName);

            getParameterDescription(parameter, openAPI.getComponents())
                    .ifPresent(s -> docs.add(createAPIParamDocFromString(parameterName, s)));
        });
        docs.add(createAPIParamDoc(HEADERS, "Headers to be sent with the request"));
        if (hasQueryParams) {
            docs.add(createAPIParamDoc(QUERIES, "Queries to be sent with the request"));
        }
        docs.addAll(deprecatedParamDocComments);
    }


    private static HashMap<String, ParameterNode> getParameterNodeHashMap(SeparatedNodeList<ParameterNode> parameters) {
        HashMap<String, ParameterNode> collection = new HashMap<>();
        for (ParameterNode node : parameters) {
            if (node instanceof RequiredParameterNode reParam) {
                collection.put(reParam.paramName().get().toString(), reParam);
            } else if (node instanceof DefaultableParameterNode deParam) {
                collection.put(deParam.paramName().get().toString(), deParam);
            } else if (node instanceof IncludedRecordParameterNode incParam) {
                collection.put(incParam.paramName().get().toString(), incParam);
            }
        }
        return collection;
    }
}
