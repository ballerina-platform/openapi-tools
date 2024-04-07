package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
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
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_BALLERINA_DEPRECATED_REASON;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.replaceContentWithinBrackets;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIDescriptionDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDoc;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.createAPIParamDocFromString;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDeprecatedAnnotation;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDeprecatedAnnotationDetails;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.extractDisplayAnnotation;
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
                HashMap<String, Node> updatedList = new HashMap<>();
                NodeList<Node> classMembers = classDef.members();
                //sort these members according to .toString();
                List<String> storeMembers = new ArrayList<>();
                List<Node> clientInitNodes = new ArrayList<>();
                classMembers.forEach(classMember -> {
                    String sortKey = "";
                    if (classMember.kind().equals(SyntaxKind.OBJECT_METHOD_DEFINITION) ||
                            classMember.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                        FunctionDefinitionNode funcDef = (FunctionDefinitionNode) classMember;

                        //remote : operationId
                        if (isResource) {
                            sortKey = funcDef.functionSignature().toString();
                            storeMembers.add(sortKey);
                            NodeList<Node> nodes = funcDef.relativeResourcePath();
                            String path = "";
                            for (Node node: nodes) {
                                path = path + node.toString().replace("\"", "");
                            }
                            String key = replaceContentWithinBrackets(path, "XXX") + "_" + funcDef.functionName().text();
                            funcDef = updateDocCommentsForFunctionNode(operationDetailsMap, funcDef, key);
                        } else {
                            sortKey = funcDef.functionName().text();
                            storeMembers.add(sortKey);
                            String key = funcDef.functionName().text();
                            funcDef = updateDocCommentsForFunctionNode(operationDetailsMap, funcDef, key);
                        }
                        classMember = funcDef;
                    } else {
                        clientInitNodes.add(classMember);
                    }
                    updatedList.put(sortKey, classMember);
                });
                //sort the members
                List<Node> sortedNodes = new ArrayList<>();
                sortedNodes.addAll(clientInitNodes);
                storeMembers.sort(String::compareTo);
                for (String memberStr: storeMembers) {
                     sortedNodes.add(updatedList.get(memberStr));
                }
                classDef = classDef.modify(
                        classDef.metadata().orElse(null),
                        classDef.visibilityQualifier().orElse(null),
                        classDef.classTypeQualifiers(),
                        classDef.classKeyword(),
                        classDef.className(),
                        classDef.openBrace(),
                        updatedList.isEmpty()? classDef.members() : createNodeList(sortedNodes),
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
                    operationDetailsMap.put(operation.getOperationId(), new OperationDetails(operation.getOperationId(), operation, path, method.name()));
                } else {
                    path = path.equals("/") ? "." : path;
                    String key = replaceContentWithinBrackets(path.replaceFirst("/", ""), "XXX") + "_" + method.name().toLowerCase(Locale.ENGLISH);
                    operationDetailsMap.put(key, new OperationDetails(operation.getOperationId(), operation, path, method.name()));
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
            if (operation.getExtensions() != null ) {
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
                updateParameterNodes(docs, operation, updatedParamsRequired, updatedParamsDefault, collection);
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
                RequestBodyDoc(docs, requestBody);
            }
            //todo response
            if (operation.getResponses() != null) {
                ApiResponses responses = operation.getResponses();
                Collection<ApiResponse> values = responses.values();
                Iterator<ApiResponse> iteratorRes = values.iterator();
                if (iteratorRes.hasNext()) {
                    ApiResponse response = iteratorRes.next();
                    if (response.getDescription() != null && !response.getDescription().isBlank()) {
                        MarkdownParameterDocumentationLineNode returnDoc = createAPIParamDoc("return",
                                response.getDescription());
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
                        metadataNode.annotations().isEmpty()? createNodeList(annotations) :
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

    private void RequestBodyDoc(List<Node> docs, RequestBody requestBody) {
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
                paramName[0] = getBallerinaMediaType(mediaType.getKey(),true);
            });
        } else {
            paramName[0] = "http:Request";
        }

        if (requestBody.getDescription() != null) {
            String description = requestBody.getDescription().split("\n")[0];
            docs.add(createAPIParamDoc(paramName[0].equals("http:Request")? "request": "payload", description));
        }
    }

    private static void updateParameterNodes(List<Node> docs, Operation operation, List<Node> updatedParamsRequired,
                                             List<Node> updatedParamsDefault, HashMap<String,
            ParameterNode> collection) {
        List<Node> deprecatedParamDocComments = new ArrayList<>();
        operation.getParameters().forEach(parameter -> {
            List<AnnotationNode> paramAnnot = new ArrayList<>();
            String parameterDescription;
            String parameterName = parameter.getName();

            if (parameter.getIn().equals("path") || parameter.getIn().equals("header") ||
                    (parameter.getIn().equals("query"))) {
                parameterName = getValidName(parameter.getName(), false);
                // add deprecated annotation
                if (parameter.getDeprecated() != null && parameter.getDeprecated()) {
                    extractDepractedAnnotation(deprecatedParamDocComments, parameter, paramAnnot);
                }
                if (parameter.getExtensions() != null) {
                    extractDisplayAnnotation(parameter.getExtensions(), paramAnnot);

                }
                ParameterNode parameterNode = collection.get(parameterName);
                updatedAnnotationInParameterNode(updatedParamsRequired, updatedParamsDefault,
                        paramAnnot, parameterNode);
            }
            if (parameter.getDescription() != null) {
                parameterDescription = parameter.getDescription();
                docs.add(createAPIParamDocFromString(parameterName, parameterDescription));
            }
        });
        docs.addAll(deprecatedParamDocComments);
    }

    private static void extractDepractedAnnotation(List<Node> deprecatedParamDocComments, Parameter parameter, List<AnnotationNode> paramAnnot) {
        deprecatedParamDocComments.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(
                "# Deprecated parameters", false));

        String deprecatedDescription = "";
        if (parameter.getExtensions() != null) {
            for (Map.Entry<String, Object> extension : parameter.getExtensions().entrySet()) {
                if (extension.getKey().trim().equals(X_BALLERINA_DEPRECATED_REASON)) {
                    deprecatedDescription = extension.getValue().toString();
                    break;
                }
            }
        }
        MarkdownParameterDocumentationLineNode paramAPIDoc =
                DocCommentsGeneratorUtil.createAPIParamDoc(getValidName(
                        parameter.getName(), false), deprecatedDescription);
        deprecatedParamDocComments.add(paramAPIDoc);
        paramAnnot.add(createAnnotationNode(createToken(AT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("deprecated")), null));
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

    private static void updatedAnnotationInParameterNode(List<Node> updatedParamsRequired,
                                                         List<Node> updatedParamsDefault,
                                                         List<AnnotationNode> paramAnnot,
                                                         ParameterNode parameterNode) {
        if (parameterNode != null) {
            if (parameterNode instanceof RequiredParameterNode reParam) {
                updatedParamsRequired.add(reParam.modify(
                        reParam.annotations().isEmpty()? createNodeList(paramAnnot) :
                                reParam.annotations().addAll(paramAnnot),
                        reParam.typeName(),
                        reParam.paramName().orElse(null)));
                updatedParamsRequired.add(createToken(SyntaxKind.COMMA_TOKEN));
            } else if (parameterNode instanceof DefaultableParameterNode deParam) {
                updatedParamsDefault.add(deParam.modify(
                        deParam.annotations().isEmpty()? createNodeList(paramAnnot) :
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
