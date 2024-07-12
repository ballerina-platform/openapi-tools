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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.EXAMPLES;
import static io.ballerina.openapi.service.mapper.Constants.FILE_PATH;
import static io.ballerina.openapi.service.mapper.Constants.JSON_EXTENSION;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_RESOURCE_INFO;
import static io.ballerina.openapi.service.mapper.Constants.OPERATION_ID;
import static io.ballerina.openapi.service.mapper.Constants.REQUEST_BODY_ATTRIBUTE;
import static io.ballerina.openapi.service.mapper.Constants.RESPONSE_ATTRIBUTE;
import static io.ballerina.openapi.service.mapper.Constants.SUMMARY;
import static io.ballerina.openapi.service.mapper.Constants.TAGS;
import static io.ballerina.openapi.service.mapper.Constants.VALUE;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOperationId;

/**
 * This class is for updating meta details into openAPI spec.
 *
 * @since 2.0.1
 */
public class MetaInfoMapperImpl implements MetaInfoMapper {
    static List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();


    @Override
    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    @Override
    public void setResourceMetaData(ServiceDeclarationNode serviceNode, OpenAPI openAPI, Path ballerinaFilePath) {
        NodeList<Node> functions = serviceNode.members();
        Map<String, ResourceMetaInfoAnnotation> resourceMetaData = new HashMap<>();
        for (Node function : functions) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                FunctionDefinitionNode resourceNode = (FunctionDefinitionNode) function;
                Optional<MetadataNode> optMetadata = resourceNode.metadata();
                if (optMetadata.isEmpty()) {
                    continue;
                }
                String operationId = getOperationId(resourceNode);
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
                                    case EXAMPLES -> handleExamples(resMetaInfoBuilder, expressionNode,
                                            ballerinaFilePath);
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

    private static void handleExamples(ResourceMetaInfoAnnotation.Builder resMetaInfoBuilder,
                                       ExpressionNode expressionNode, Path ballerinFilePath) {
        if (expressionNode instanceof MappingConstructorExpressionNode mapNode) {
            SeparatedNodeList<MappingFieldNode> fields = mapNode.fields();
            for (MappingFieldNode resultField : fields) {
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
                        Map<?, ?> objectMap = objectMapper.readValue(sourceCode, Map.class);
                        setResponseExamples(resMetaInfoBuilder, objectMap, ballerinFilePath, expressValue.location());
                    } catch (JsonProcessingException e) {
                        DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_130;
                        ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, mapNode.location(),
                                e.getOriginalMessage());
                        diagnostics.add(diagnostic);
                    }
                } else if (fName.equals(REQUEST_BODY_ATTRIBUTE)) {
                    Optional<ExpressionNode> optExamplesValue = resultField1.valueExpr();
                    if (optExamplesValue.isEmpty()) {
                        continue;
                    }
                    ExpressionNode expressValue = optExamplesValue.get();
                    String mediaType = expressValue.toSourceCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        Map<?, ?> objectMap = objectMapper.readValue(mediaType, Map.class);
                        setRequestExamples(resMetaInfoBuilder, objectMap, ballerinFilePath, expressValue.location());
                    } catch (JsonProcessingException e) {
                        DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_130;
                        ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, mapNode.location(),
                                e.getOriginalMessage());
                        diagnostics.add(diagnostic);
                    }
                }
            }
        }
    }

    /**
     * This is for mapping response example in OAS.
     */
    private static void setResponseExamples(ResourceMetaInfoAnnotation.Builder resMetaInfoBuilder, Map<?, ?> objectMap,
                                            Path ballerinaFilePath, Location location) {
        if (objectMap instanceof LinkedHashMap<?, ?> responseSet) {
            //<statusCode, <MediaType, Map<name, Object>>>
            Map<String, Map<String, Map<String, Object>>> responseExamples = new HashMap<>();
            for (Map.Entry<?, ?> statusCodeValuePair : responseSet.entrySet()) {
                Object key = statusCodeValuePair.getKey();
                if (!(key instanceof String)) {
                    continue;
                }
                String statusCode = key.toString().trim();
                Object valuePairForMediaType = statusCodeValuePair.getValue();
                if (valuePairForMediaType instanceof LinkedHashMap<?, ?> responseMap) {
                    Set<? extends Map.Entry<? , ?>> mediaTypeExampleEntries = responseMap.entrySet();
                    for (Map.Entry<? , ?> entry : mediaTypeExampleEntries) {
                        if (entry.getKey().equals("examples")) {
                            Map<String, Map<String, Object>> mediaTypeExampleMap = extractExamples(entry.getValue(),
                                    ballerinaFilePath, location);
                            responseExamples.put(statusCode, mediaTypeExampleMap);
                        }
                        //todo: headers with response
                    }
                }
            }
            resMetaInfoBuilder.responseExamples(responseExamples);
        }
    }

    /**
     * This is for mapping request example in OAS.
     */
    private static void setRequestExamples(ResourceMetaInfoAnnotation.Builder resMetaInfoBuilder, Map<?, ?> objectMap,
                                            Path ballerinaFilePath, Location location) {
        Map<String, Map<String, Object>> mediaTypeExampleMap = extractExamples(objectMap,
                ballerinaFilePath, location);
            resMetaInfoBuilder.requestExamples(mediaTypeExampleMap);

    }

    private static Map<String, Map<String, Object>> extractExamples(Object exampleValues, Path ballerinaFilePath,
                                                                    Location location) {
        //Map format: <key:mediaType ,value: <key:name, value>>
        Map<String, Map<String, Object>> mediaTypeExampleMap = new HashMap<>();
        if (exampleValues instanceof LinkedHashMap<?, ?> exampleValueMap) {
            Set<? extends Map.Entry<?, ?>> exampleSets = exampleValueMap.entrySet();

            for (Map.Entry<?, ?> valuePair : exampleSets) {
                String mediaType = valuePair.getKey().toString().trim();
                Object resExamples = valuePair.getValue();
                if (resExamples instanceof LinkedHashMap<?, ?> resExampleMaps) {
                    Map<String, Object> modifiedExample = new HashMap<>();
                    for (Map.Entry<?, ?> example: resExampleMaps.entrySet()) {
                        String exampleName = (String) example.getKey();
                        if (example.getValue() instanceof LinkedHashMap<?, ?> exampleValue) {
                            if (!exampleValue.containsKey(VALUE) && !exampleValue.containsKey(FILE_PATH)) {
                                break;
                            }
                            Object value = exampleValue.get(VALUE);
                            if (value == null) {
                                value = exampleValue.get(FILE_PATH);
                            }

                            if (value instanceof LinkedHashMap<?, ?>) {
                                modifiedExample.put(exampleName, example.getValue());
                            } else if (value instanceof String stringNode) {
                                String jsonFilePath = stringNode.replaceAll("\"", "").trim();
                                Path relativePath = resolveExampleFilePath(ballerinaFilePath, jsonFilePath, location,
                                        exampleName);
                                if (relativePath == null) {
                                    continue;
                                }
                                if (!Files.exists(relativePath)) {
                                    DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_128;
                                    ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, location,
                                            jsonFilePath, exampleName);
                                    diagnostics.add(diagnostic);
                                    continue;
                                }
                                String content;
                                try {
                                    content = Files.readString(relativePath);
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    Map<String, Object> objectMap = objectMapper.readValue(content, Map.class);
                                    Map<String, Object> valueMap = new HashMap<>(objectMap);
                                    modifiedExample.put(exampleName, valueMap);
                                } catch (IOException e) {
                                    DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_130;
                                    ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, location,
                                            e.toString());
                                    diagnostics.add(diagnostic);
                                }
                            }
                        }
                    }
                    mediaTypeExampleMap.put(mediaType, modifiedExample);
                }
            }
        }
        return mediaTypeExampleMap;
    }

    private static Path resolveExampleFilePath(Path ballerinaFilePath, String jsonFilePath, Location location,
                                               String exampleName) {
        if (jsonFilePath.isBlank()) {
            DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_131;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, location, exampleName);
            diagnostics.add(diagnostic);
            return null;
        }
        if (!(jsonFilePath.endsWith(JSON_EXTENSION))) {
            DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_129;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, location, jsonFilePath, exampleName);
            diagnostics.add(diagnostic);
            return null;
        }

        Path path = java.nio.file.Paths.get(jsonFilePath);
        if (path.isAbsolute()) {
            return path;
        } else {
            File file = new File(ballerinaFilePath.toString());
            File parentFolder = new File(file.getParent());
            File jsonFile = new File(parentFolder, jsonFilePath);
            try {
                return java.nio.file.Paths.get(jsonFile.getCanonicalPath());
            } catch (IOException e) {
                DiagnosticMessages messages = DiagnosticMessages.OAS_CONVERTOR_128;
                ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(messages, location,
                        jsonFilePath, exampleName);
                diagnostics.add(diagnostic);
                return null;
            }
        }
    }

    /**
     * Update OAS with the metadata by traversing each operation.
     */
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

    /**
     * Update OAS operation with the given resource metadata.
     */
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
                    if (oasMediaType == null) {
                        continue;
                    }
                    Map<String, Example> exampleMap = new HashMap<>();
                    Map<String, Object> examples = entry.getValue();
                    for (Map.Entry<String, Object> example : examples.entrySet()) {
                        Object value = example.getValue();
                        if (value instanceof LinkedHashMap<?, ?> exampleValue) {
                            value = exampleValue.get(VALUE);
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

            // Request body example payload mapping
            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                updateRequestBodyExample(resourceMetaInfo, requestBody);
                operation.setRequestBody(requestBody);
            }
        }
    }

    /**
     * Update the OAS operation with given requestBody examples.
     */
    private static void updateRequestBodyExample(ResourceMetaInfoAnnotation resourceMetaInfo, RequestBody requestBody) {
        Content requestBodyContent = requestBody.getContent();
        Map<String, Map<String, Object>> requestExamples = resourceMetaInfo.getRequestExamples();

        for (Map.Entry<String, Map<String, Object>> example: requestExamples.entrySet()) {
            MediaType oasMediaType = requestBodyContent.get(example.getKey());
            if (oasMediaType == null) {
                continue;
            }
            Map<String, Example> exampleMap = new HashMap<>();
            for (Map.Entry<String, Object> exampleValuePair: example.getValue().entrySet()) {
                Object value = exampleValuePair.getValue();
                if (value instanceof LinkedHashMap<?, ?> exampleValue) {
                    value = exampleValue.get(VALUE);
                }
                Example oasExample = new Example();
                oasExample.setValue(value);
                exampleMap.put(exampleValuePair.getKey(), oasExample);
            }
            oasMediaType.setExamples(exampleMap);
            requestBodyContent.put(example.getKey().trim(), oasMediaType);
        }
        requestBody.setContent(requestBodyContent);
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
