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

package io.ballerina.openapi.service.mapper.metainfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.service.mapper.model.ResourceFunction;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDeclaration;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDefinition;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.EXAMPLES;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_RESOURCE_INFO;
import static io.ballerina.openapi.service.mapper.Constants.OPERATION_ID;
import static io.ballerina.openapi.service.mapper.Constants.RESPONSE_ATTRIBUTE;
import static io.ballerina.openapi.service.mapper.Constants.SUMMARY;
import static io.ballerina.openapi.service.mapper.Constants.TAGS;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOperationId;

/**
 * This class is for updating meta details into openAPI spec.
 *
 * @since 2.0.1
 */
public class MetaInfoMapperImpl implements MetaInfoMapper {

    public void setResourceMetaData(OpenAPI openAPI, ServiceNode serviceNode) {
        NodeList<Node> functions = serviceNode.members();
        Map<String, ResourceMetaInfoAnnotation> resourceMetaData = new HashMap<>();
        for (Node function : functions) {
            Optional<ResourceFunction> resourceFunction = getResourceFunction(function);
            if (resourceFunction.isPresent()) {
                Optional<MetadataNode> optMetadata = resourceFunction.get().metadata();
                if (optMetadata.isEmpty()) {
                    continue;
                }
                String operationId = getOperationId(resourceFunction.get());
                ResourceMetaInfoAnnotation.Builder resMetaInfoBuilder = new ResourceMetaInfoAnnotation.Builder();
                MetadataNode metadataNode = optMetadata.get();
                NodeList<AnnotationNode> annotations = metadataNode.annotations();
                //check annotation
                for (AnnotationNode annotation : annotations) {
                    if (annotation.annotReference().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                        QualifiedNameReferenceNode ref = (QualifiedNameReferenceNode) annotation.annotReference();
                        String annotationName = ref.modulePrefix().text() + ":" + ref.identifier().text();
                        if (annotationName.equals(OPENAPI_RESOURCE_INFO)) {
                            Optional<MappingConstructorExpressionNode> optExpressionNode = annotation.annotValue();
                            if (optExpressionNode.isEmpty()) {
                                continue;
                            }
                            MappingConstructorExpressionNode mappingConstructorExpressionNode = optExpressionNode.get();
                            SeparatedNodeList<MappingFieldNode> fields = mappingConstructorExpressionNode.fields();
                            for (MappingFieldNode field : fields) {
                                String fieldName = ((SpecificFieldNode) field).fieldName().toString().trim();
                                Optional<ExpressionNode> value = ((SpecificFieldNode) field).valueExpr();
                                String fieldValue;
                                if (value.isEmpty()) {
                                    continue;
                                }
                                ExpressionNode expressionNode = value.get();
                                if (expressionNode.toString().trim().isBlank()) {
                                    continue;
                                }
                                fieldValue = expressionNode.toString().trim().replaceAll("\"", "");
                                switch (fieldName) {
                                    case OPERATION_ID -> resMetaInfoBuilder.operationId(fieldValue);
                                    case SUMMARY -> resMetaInfoBuilder.summary(fieldValue);
                                    case TAGS -> {
                                        if (expressionNode instanceof ListConstructorExpressionNode listNode) {
                                            List<String> values = extractListItems(listNode);
                                            resMetaInfoBuilder.tags(values);
                                        }
                                    }
                                    case EXAMPLES -> handleExamples(resMetaInfoBuilder, expressionNode);
                                    default -> { }
                                }
                            }
                        }
                    }
                }
                resourceMetaData.put(operationId, resMetaInfoBuilder.build());
            }
        }

        Paths paths = openAPI.getPaths();
        updateOASWithMetaData(resourceMetaData, paths);
    }

    private static Optional<ResourceFunction> getResourceFunction(Node node) {
        if (node.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            return Optional.of(new ResourceFunctionDefinition((FunctionDefinitionNode) node));
        } else if (node.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION)) {
            return Optional.of(new ResourceFunctionDeclaration((MethodDeclarationNode) node));
        }
        return Optional.empty();
    }

    private static void handleExamples(ResourceMetaInfoAnnotation.Builder resMetaInfoBuilder,
                                       ExpressionNode expressionNode) {
        if (expressionNode instanceof MappingConstructorExpressionNode mapNode) {
            SeparatedNodeList<MappingFieldNode> fields1 = mapNode.fields();
            for (MappingFieldNode resultField : fields1) {
                //parse as json object
                SpecificFieldNode resultField1 = (SpecificFieldNode) resultField;
                String fName = resultField1.fieldName().toSourceCode().trim().replaceAll("\"", "");
                if (fName.equals(RESPONSE_ATTRIBUTE)) {
                    Optional<ExpressionNode> optExamplesValue = resultField1.valueExpr();
                    if (optExamplesValue.isEmpty()) {
                        continue;
                    }
                    ExpressionNode expressValue = optExamplesValue.get();
                    String sourceCode = expressValue.toSourceCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Map<String, Object> objectMap = objectMapper.readValue(sourceCode, Map.class);
                        // Handle response
                        if (objectMap instanceof LinkedHashMap<?, ?> responseSet) {
                            //<statusCode, <MediaType, Map<name, Object>>>
                            Map<String, Map<String, Map<String, Object>>> responseExamples = new HashMap<>();
                            for (Map.Entry<?, ?> statusCodeValuePair : responseSet.entrySet()) {
                                Object key = statusCodeValuePair.getKey();
                                if (!(key instanceof String)) {
                                    continue;
                                }
                                String statusCode = key.toString().trim();
                                Object valuePairValue = statusCodeValuePair.getValue();
                                if (valuePairValue instanceof LinkedHashMap<?, ?> responseMap) {
                                    Set<? extends Map.Entry<? , ?>> entries = responseMap.entrySet();
                                    for (Map.Entry<? , ?> entry : entries) {
                                        if (entry.getKey().equals("examples")) {
                                            extractResponseExamples(responseExamples, statusCode, entry);
                                        }
                                        //todo: headers
                                    }
                                }
                            }
                            resMetaInfoBuilder.responseExamples(responseExamples);
                        }
                        //todo: request body, parameters
                    } catch (JsonProcessingException e) {
                        //ignore;
                        //todo will handle this with future design
                    }
                }
            }
        }
    }

    private static void extractResponseExamples(Map<String, Map<String, Map<String, Object>>> responseExamples,
                                                String statusCode, Map.Entry<? , ?> entry) {
        Object exampleValues = entry.getValue();
        if (exampleValues instanceof LinkedHashMap<?, ?> exampleValueMap) {
            Set<? extends Map.Entry<?, ?>> sets = exampleValueMap.entrySet();
            //<mediaType , <name, value>>
            Map<String, Map<String, Object>> mediaTypeExampleMap = new HashMap<>();

            for (Map.Entry<?, ?> valuePair : sets) {
                String mediaType = valuePair.getKey().toString();
                Object responesExamples = valuePair.getValue();
                if (responesExamples instanceof LinkedHashMap<?, ?> resExampleMaps) {
                    Map<String, Object> examples = (Map<String, Object>) resExampleMaps;
                    mediaTypeExampleMap.put(mediaType, examples);
                }
            }
            responseExamples.put(statusCode, mediaTypeExampleMap);
        }
    }

    private static void updateOASWithMetaData(Map<String, ResourceMetaInfoAnnotation> resourceMetaData, Paths paths) {
        if (paths != null) {
            paths.forEach((path, pathItem) -> {
                if (pathItem.getGet() != null) {
                    Operation operation = pathItem.getGet();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setGet(operation);
                }
                if (pathItem.getPost() != null) {
                    Operation operation = pathItem.getPost();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setPost(operation);
                }
                if (pathItem.getPut() != null) {
                    Operation operation = pathItem.getPut();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setPut(operation);
                }
                if (pathItem.getDelete() != null) {
                    Operation operation = pathItem.getDelete();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setDelete(operation);
                }
                if (pathItem.getPatch() != null) {
                    Operation operation = pathItem.getPatch();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setPatch(operation);
                }
                if (pathItem.getOptions() != null) {
                    Operation operation = pathItem.getOptions();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setOptions(operation);
                }
                if (pathItem.getHead() != null) {
                    Operation operation = pathItem.getHead();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setHead(operation);
                }
                if (pathItem.getTrace() != null) {
                    Operation operation = pathItem.getTrace();
                    updateOASOperationWithMetaData(resourceMetaData, operation);
                    pathItem.setTrace(operation);
                }
            });
        }
    }

    private static void updateOASOperationWithMetaData(Map<String, ResourceMetaInfoAnnotation> resourceMetaData,
                                                       Operation operation) {
        String operationId = operation.getOperationId();
        if (!resourceMetaData.isEmpty() && resourceMetaData.containsKey(operationId)) {
            ResourceMetaInfoAnnotation resourceMetaInfo = resourceMetaData.get(operationId);
            String userProvideOperationId = resourceMetaInfo.getOperationId();
            if (userProvideOperationId != null && !userProvideOperationId.isBlank()) {
                operation.setOperationId(userProvideOperationId);
            }
            if (resourceMetaInfo.getTags() != null && !resourceMetaInfo.getTags().isEmpty()) {
                operation.setTags(resourceMetaInfo.getTags());
            }
            if (resourceMetaInfo.getSummary() != null && !resourceMetaInfo.getSummary().isBlank()) {
                operation.setSummary(resourceMetaInfo.getSummary());
            }
            ApiResponses responses = operation.getResponses();
            Map<String, Map<String, Map<String, Object>>> responseExamples = resourceMetaInfo.getResponseExamples();
            for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
                String statusCode = response.getKey();
                Map<String, Map<String, Object>> mediaTypeExampleMap = responseExamples.get(statusCode);
                if (mediaTypeExampleMap == null) {
                    continue;
                }
                ApiResponse oasApiResponse = response.getValue();
                Content oasContent = oasApiResponse.getContent();
                for (Map.Entry<String, Map<String, Object>> entry : mediaTypeExampleMap.entrySet()) {
                    String mediaTypeKey = entry.getKey();
                    MediaType oasMediaType = oasContent.get(mediaTypeKey);
                    Map<String, Example> exampleMap = new HashMap<>();
                    Map<String, Object> examples = entry.getValue();
                    for (Map.Entry<String, Object> example : examples.entrySet()) {
                        Object value = example.getValue();
                        if (value instanceof LinkedHashMap<?, ?> exampleValue) {
                            value = exampleValue.get("value");
                        }
                        Example oasExample = new Example();
                        oasExample.setValue(value);
                        exampleMap.put(example.getKey(), oasExample);
                    }
                    oasMediaType.setExamples(exampleMap);
                    oasContent.put(mediaTypeKey, oasMediaType);
                }
                oasApiResponse.setContent(oasContent);
                responses.put(response.getKey(), oasApiResponse);
            }
            operation.setResponses(responses);
        }
    }

    private static List<String> extractListItems(ListConstructorExpressionNode list) {
        SeparatedNodeList<Node> expressions = list.expressions();
        Iterator<Node> iterator = expressions.iterator();
        List<String> values = new ArrayList<>();
        while (iterator.hasNext()) {
            Node item = iterator.next();
            if (item.kind() == SyntaxKind.STRING_LITERAL && !item.toString().isBlank()) {
                Token stringItem = ((BasicLiteralNode) item).literalToken();
                String text = stringItem.text();
                // Here we need to do some preprocessing by removing '"' from the given values.
                if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
                    text = text.substring(1, text.length() - 1);
                } else {
                    // Missing end quote case
                    text = text.substring(1);
                }
                values.add(text);
            }
        }
        return values;
    }
}
