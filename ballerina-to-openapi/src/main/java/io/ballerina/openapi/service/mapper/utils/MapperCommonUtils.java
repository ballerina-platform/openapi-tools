/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.service.mapper.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.DistinctTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.service.mapper.Constants;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.openapi.service.mapper.model.ResourceFunction;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDeclaration;
import io.ballerina.openapi.service.mapper.model.ResourceFunctionDefinition;
import io.ballerina.projects.ModuleName;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.FilenameUtils;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.BOOLEAN_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LIST_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NUMERIC_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNARY_EXPRESSION;
import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_HEADER;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_QUERY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_SERVICE_CONTRACT;
import static io.ballerina.openapi.service.mapper.Constants.HYPHEN;
import static io.ballerina.openapi.service.mapper.Constants.JSON_EXTENSION;
import static io.ballerina.openapi.service.mapper.Constants.SLASH;
import static io.ballerina.openapi.service.mapper.Constants.SPECIAL_CHAR_REGEX;
import static io.ballerina.openapi.service.mapper.Constants.UNDERSCORE;
import static io.ballerina.openapi.service.mapper.Constants.YAML_EXTENSION;

/**
 * Utilities used in Ballerina  to OpenAPI converter.
 */
public class MapperCommonUtils {

    private static final SyntaxKind[] validExpressionKind = {STRING_LITERAL, NUMERIC_LITERAL, BOOLEAN_LITERAL,
            LIST_CONSTRUCTOR, MAPPING_CONSTRUCTOR, UNARY_EXPRESSION};

    public static String generateRelativePath(ResourceFunction resourceFunction) {
        StringBuilder relativePath = new StringBuilder();
        relativePath.append("/");
        if (!resourceFunction.relativeResourcePath().isEmpty()) {
            for (Node node: resourceFunction.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode pathNode) {
                    relativePath.append("{");
                    relativePath.append(pathNode.paramName().get());
                    relativePath.append("}");
                } else if ((resourceFunction.relativeResourcePath().size() == 1)
                        && (node.toString().trim().equals("."))) {
                    return relativePath.toString();
                } else {
                    relativePath.append(node.toString().trim());
                }
            }
        }
        return relativePath.toString();
    }

    /**
     * Generate operationId by removing special characters.
     *
     * @param resourceFunction resource function definition
     * @return string with a unique operationId
     */
    public static String getOperationId(ResourceFunction resourceFunction) {
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !operationID.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        String relativePath = MapperCommonUtils.generateRelativePath(resourceFunction);
        String cleanResourcePath = MapperCommonUtils.unescapeIdentifier(relativePath);
        String resName = (resourceFunction.functionName() + "_" + cleanResourcePath).replace("{}", "_");
        if (cleanResourcePath.equals("/")) {
            resName = resourceFunction.functionName();
        }
        if (resName.matches("\\b[0-9]*\\b")) {
            return resName;
        }
        String[] split = resName.split(Constants.SPECIAL_CHAR_REGEX);
        StringBuilder validName = new StringBuilder();
        for (String part : split) {
            if (!part.isBlank()) {
                if (split.length > 1) {
                    part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                            part.substring(1).toLowerCase(Locale.ENGLISH);
                }
                validName.append(part);
            }
        }
        resName = validName.toString();
        return resName.substring(0, 1).toLowerCase(Locale.ENGLISH) + resName.substring(1);
    }

    /**
     * This util function uses to take the field value from annotation field.
     *
     * @param annotations         - Annotation node list
     * @param annotationReference - Annotation reference that needs to extract
     * @param annotationField     - Annotation field name that uses to take value
     * @return - string value of field
     */
    public static Optional<String> extractServiceAnnotationDetails(NodeList<AnnotationNode> annotations,
                                                                   String annotationReference, String annotationField) {
        for (AnnotationNode annotation : annotations) {
            List<String> expressionNode = extractAnnotationFieldDetails(annotationReference, annotationField,
                    annotation, null);
            if (!expressionNode.isEmpty()) {
                return Optional.of(expressionNode.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * This util functions is used to extract the details of annotation field.
     *
     * @param annotationReference Annotation reference name that need to extract
     * @param annotationField     Annotation field name that need to extract details.
     * @param annotation          Annotation node
     * @return List of string
     */

    public static List<String> extractAnnotationFieldDetails(String annotationReference, String annotationField,
                                                             AnnotationNode annotation, SemanticModel semanticModel) {
        List<String> mediaTypes = new ArrayList<>();
        Node annotReference = annotation.annotReference();
        if (annotReference.toString().trim().equals(annotationReference) && annotation.annotValue().isPresent()) {
            MappingConstructorExpressionNode listOfAnnotValue = annotation.annotValue().get();
            for (MappingFieldNode field : listOfAnnotValue.fields()) {
                SpecificFieldNode fieldNode = (SpecificFieldNode) field;
                if (!((fieldNode).fieldName().toString().trim().equals(annotationField)) ||
                        fieldNode.valueExpr().isEmpty()) {
                    continue;
                }
                ExpressionNode expressionNode = fieldNode.valueExpr().get();
                if (expressionNode instanceof ListConstructorExpressionNode) {
                    SeparatedNodeList<Node> mimeList = ((ListConstructorExpressionNode) expressionNode).expressions();
                    for (Object mime : mimeList) {
                        if (!(mime instanceof BasicLiteralNode)) {
                            continue;
                        }
                        mediaTypes.add(((BasicLiteralNode) mime).literalToken().text().trim().replaceAll("\"", ""));
                    }
                } else if (expressionNode instanceof QualifiedNameReferenceNode moduleRef && semanticModel != null) {
                    Optional<Symbol> refSymbol = semanticModel.symbol(moduleRef);
                    if (refSymbol.isPresent() && (refSymbol.get().kind() == SymbolKind.CONSTANT)
                            && ((ConstantSymbol) refSymbol.get()).resolvedValue().isPresent()) {
                        String mediaType = ((ConstantSymbol) refSymbol.get()).resolvedValue().get();
                        mediaTypes.add(mediaType.replaceAll("\"", ""));
                    }
                } else {
                    mediaTypes.add(expressionNode.toString().trim().replaceAll("\"", ""));
                }
            }
        }
        return mediaTypes;
    }

    /**
     * This {@code NullLocation} represents the null location allocation for scenarios which has not location.
     */
    public static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }

    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI URI for the OpenAPI contract
     * @return {@link OASResult}  OpenAPI model
     */
    public static OASResult parseOpenAPIFile(String definitionURI) {
        List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
        Path contractPath = Paths.get(definitionURI);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);

        if (!Files.exists(contractPath)) {
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_110);
            diagnostics.add(diagnostic);
        }
        if (!(definitionURI.endsWith(Constants.YAML_EXTENSION) || definitionURI.endsWith(Constants.JSON_EXTENSION)
                || definitionURI.endsWith(Constants.YML_EXTENSION))) {
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_110);
            diagnostics.add(diagnostic);
        }
        String openAPIFileContent = null;
        try {
            openAPIFileContent = Files.readString(contractPath);
        } catch (IOException e) {
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_108,
                    e.toString());
            diagnostics.add(diagnostic);
        }
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent, null, parseOptions);
        if (!parseResult.getMessages().isEmpty()) {
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_112);
            diagnostics.add(diagnostic);
            return new OASResult(null, diagnostics);
        }
        OpenAPI api = parseResult.getOpenAPI();
        return new OASResult(api, diagnostics);
    }

    public static String normalizeTitle(String serviceName) {
        if (serviceName == null) {
            return null;
        }
        String[] urlPaths = (serviceName.replaceFirst(Constants.SLASH, "")).split(SPECIAL_CHAR_REGEX);
        StringBuilder stringBuilder = new StringBuilder();
        String title = serviceName;
        if (urlPaths.length > 1) {
            for (String path : urlPaths) {
                if (path.isBlank()) {
                    continue;
                }
                stringBuilder.append(path.substring(0, 1).toUpperCase(Locale.ENGLISH));
                stringBuilder.append(path.substring(1));
                stringBuilder.append(" ");
            }
            title = stringBuilder.toString().trim();
        } else if (urlPaths.length == 1 && !urlPaths[0].isBlank()) {
            stringBuilder.append(urlPaths[0].substring(0, 1).toUpperCase(Locale.ENGLISH));
            stringBuilder.append(urlPaths[0].substring(1));
            title = stringBuilder.toString().trim();
        }
        return title;
    }

    /**
     * This util function is to check the given service is http service.
     *
     * @param serviceNode   Service node for analyse
     * @param semanticModel Semantic model
     * @return boolean output
     */
    public static boolean isHttpService(ServiceDeclarationNode serviceNode, SemanticModel semanticModel) {
        Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceNode);
        if (serviceSymbol.isEmpty()) {
            return false;
        }

        ServiceDeclarationSymbol serviceNodeSymbol = (ServiceDeclarationSymbol) serviceSymbol.get();
        List<TypeSymbol> listenerTypes = (serviceNodeSymbol).listenerTypes();
        for (TypeSymbol listenerType : listenerTypes) {
            if (isHttpListener(listenerType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHttpServiceContract(Node typeNode, SemanticModel semanticModel) {
        if (!(typeNode instanceof ObjectTypeDescriptorNode serviceObjType) || !isServiceObjectType(serviceObjType)) {
            return false;
        }

        Optional<Symbol> serviceObjSymbol = semanticModel.symbol(serviceObjType.parent());
        if (serviceObjSymbol.isEmpty() ||
                (!(serviceObjSymbol.get() instanceof TypeDefinitionSymbol serviceObjTypeDef))) {
            return false;
        }

        Optional<Symbol> serviceContractType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                HTTP_SERVICE_CONTRACT);
        if (serviceContractType.isEmpty() ||
                !(serviceContractType.get() instanceof TypeDefinitionSymbol serviceContractTypeDef)) {
            return false;
        }

        return serviceObjTypeDef.typeDescriptor().subtypeOf(serviceContractTypeDef.typeDescriptor());
    }

    private static boolean isServiceObjectType(ObjectTypeDescriptorNode typeNode) {
        return typeNode.objectTypeQualifiers().stream().anyMatch(
                qualifier -> qualifier.kind().equals(SyntaxKind.SERVICE_KEYWORD));
    }

    private static boolean isHttpListener(TypeSymbol listenerType) {
        if (listenerType.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) listenerType).memberTypeDescriptors().stream()
                    .filter(typeDescriptor -> typeDescriptor instanceof TypeReferenceTypeSymbol)
                    .map(typeReferenceTypeSymbol -> (TypeReferenceTypeSymbol) typeReferenceTypeSymbol)
                    .anyMatch(typeReferenceTypeSymbol -> isHttpModule(typeReferenceTypeSymbol.getModule().get()));
        }

        if (listenerType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            return isHttpModule(((TypeReferenceTypeSymbol) listenerType).typeDescriptor().getModule().get());
        }
        return false;
    }

    private static boolean isHttpModule(ModuleSymbol moduleSymbol) {
        if (moduleSymbol.getName().isPresent()) {
            return HTTP.equals(moduleSymbol.getName().get()) && BALLERINA.equals(moduleSymbol.id().orgName());
        } else {
            return false;
        }
    }

    /**
     * Generate file name with service basePath.
     */
    public static String getOpenApiFileName(String servicePath, String serviceName, boolean isJson) {
        String openAPIFileName;
        if (serviceName.isBlank() || serviceName.equals(SLASH) || serviceName.startsWith(SLASH + HYPHEN)) {
            String[] fileName = serviceName.split(SLASH);
            // This condition is to handle `service on ep1 {} ` multiple scenarios
            if (fileName.length > 0 && !serviceName.isBlank()) {
                openAPIFileName = FilenameUtils.removeExtension(servicePath) + fileName[1];
            } else {
                openAPIFileName = FilenameUtils.removeExtension(servicePath);
            }
        } else if (serviceName.startsWith(HYPHEN)) {
            // serviceName -> service on ep1 {} has multiple service ex: "-33456"
            openAPIFileName = FilenameUtils.removeExtension(servicePath) + serviceName;
        } else {
            // Remove starting path separate if exists
            if (serviceName.startsWith(SLASH)) {
                serviceName = serviceName.substring(1);
            }
            // Replace rest of the path separators with underscore
            openAPIFileName = serviceName.replaceAll(SLASH, "_");
        }

        return getNormalizedFileName(openAPIFileName) + Constants.OPENAPI_SUFFIX +
                (isJson ? JSON_EXTENSION : YAML_EXTENSION);
    }

    /**
     * Remove special characters from the given file name.
     */
    public static String getNormalizedFileName(String openAPIFileName) {
        String[] splitNames = openAPIFileName.split("[^a-zA-Z0-9]");
        if (splitNames.length > 0) {
            return Arrays.stream(splitNames)
                    .filter(namePart -> !namePart.isBlank())
                    .collect(Collectors.joining(UNDERSCORE));
        }
        return openAPIFileName;
    }

    public static boolean containErrors(List<Diagnostic> diagnostics) {
        return diagnostics != null && diagnostics.stream().anyMatch(diagnostic ->
                diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR);
    }

    public static String unescapeIdentifier(String parameterName) {
        String unescapedParamName = IdentifierUtils.unescapeBallerina(parameterName);
        return unescapedParamName.replaceAll("\\\\", "").replaceAll("'", "");
    }

    public static String getHeaderName(ParameterNode parameterNode, String defaultName) {
        return getParamName(HTTP_HEADER, parameterNode, defaultName);
    }

    public static String getQueryName(ParameterNode parameterNode, String defaultName) {
        return getParamName(HTTP_QUERY, parameterNode, defaultName);
    }

    public static String getParamName(String paramTypeName, ParameterNode parameterNode, String defaultName) {
        NodeList<AnnotationNode> annotations = null;
        if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            annotations = requiredParameterNode.annotations();
        } else if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            annotations = defaultableParameterNode.annotations();
        }
        if (Objects.isNull(annotations) || annotations.isEmpty()) {
            return defaultName;
        }
        for (AnnotationNode annotation : annotations) {
            if (annotation.annotReference().toString().trim().equals(paramTypeName)) {
                String valueExpression = getNameFromParamAnnotation(annotation);
                if (Objects.nonNull(valueExpression)) {
                    return valueExpression;
                }
            }
        }
        return defaultName;
    }

    private static String getNameFromParamAnnotation(AnnotationNode annotation) {
        Optional<MappingConstructorExpressionNode> annotationRecord = annotation.annotValue();
        if (annotationRecord.isEmpty()) {
            return null;
        }
        SeparatedNodeList<MappingFieldNode> fields = annotationRecord.get().fields();
        for (MappingFieldNode field : fields) {
            if (field instanceof SpecificFieldNode specificFieldNode &&
                    specificFieldNode.fieldName().toString().trim().equals("name")) {
                Optional<ExpressionNode> expressionNode = specificFieldNode.valueExpr();
                if (expressionNode.isPresent()) {
                    ExpressionNode valueExpression = expressionNode.get();
                    if (valueExpression.kind().equals(STRING_LITERAL)) {
                        return valueExpression.toString().trim().replaceAll("\"", "");
                    }
                }
            }
        }
        return null;
    }

    public static String getTypeDescription(TypeReferenceTypeSymbol typeSymbol) {
        Symbol definition = typeSymbol.definition();
        if (definition instanceof Documentable) {
            Optional<Documentation> documentation = ((Documentable) definition).documentation();
            if (documentation.isPresent() && documentation.get().description().isPresent()) {
                return documentation.get().description().get().trim();
            }
        }
        return null;
    }

    public static String getRecordFieldTypeDescription(RecordFieldSymbol typeSymbol) {
        Optional<Documentation> documentation = typeSymbol.documentation();
        if (documentation.isPresent() && documentation.get().description().isPresent()) {
            return documentation.get().description().get().trim();
        }
        return null;
    }

    public static String getTypeName(TypeSymbol typeSymbol) {
        String typeName = typeSymbol.getName().orElse(typeSymbol.signature());
        return unescapeIdentifier(typeName);
    }

    public static Object parseBalSimpleLiteral(String literal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            return mapper.readValue(literal, Object.class);
        } catch (JsonProcessingException e) {
            return literal;
        }
    }

    public static boolean isNotSimpleValueLiteralKind(SyntaxKind valueExpressionKind) {
        return Arrays.stream(validExpressionKind).noneMatch(syntaxKind -> syntaxKind ==
                valueExpressionKind);
    }

    public static Optional<AnnotationNode> getResourceConfigAnnotation(ResourceFunction resourceFunction) {
        Optional<MetadataNode> metadata = resourceFunction.metadata();
        if (metadata.isEmpty()) {
            return Optional.empty();
        }
        MetadataNode metaData = metadata.get();
        NodeList<AnnotationNode> annotations = metaData.annotations();
        return annotations.stream()
                .filter(ann -> "http:ResourceConfig".equals(ann.annotReference().toString().trim()))
                .findFirst();
    }

    public static Optional<String> getValueForAnnotationFields(AnnotationNode resourceConfigAnnotation,
                                                               String fieldName) {
        return resourceConfigAnnotation
                .annotValue()
                .map(MappingConstructorExpressionNode::fields)
                .flatMap(fields ->
                        fields.stream()
                                .filter(fld -> fld instanceof SpecificFieldNode)
                                .map(fld -> (SpecificFieldNode) fld)
                                .filter(fld -> fieldName.equals(fld.fieldName().toString().trim()))
                                .findFirst()
                ).flatMap(SpecificFieldNode::valueExpr)
                .map(en -> en.toString().trim());
    }

    public static Optional<ResourceFunction> getResourceFunction(Node function) {
        SyntaxKind kind = function.kind();
        if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            return Optional.of(new ResourceFunctionDefinition((FunctionDefinitionNode) function));
        } else if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION)) {
            return Optional.of(new ResourceFunctionDeclaration((MethodDeclarationNode) function));
        }
        return Optional.empty();
    }

    public static Optional<Object> getConstantValues(Optional<Symbol> symbol) {
        if (symbol.isPresent() && symbol.get() instanceof ConstantSymbol constantSymbol) {
            Object constValue = constantSymbol.constValue();
            if (constValue instanceof ConstantValue value) {
                return Optional.of(value.value());
            }
        }
        return Optional.empty();
    }

    public static Node getTypeDescriptor(TypeDefinitionNode typeDefinitionNode) {
        Node node = typeDefinitionNode.typeDescriptor();
        if (node instanceof DistinctTypeDescriptorNode distinctTypeDescriptorNode) {
            return distinctTypeDescriptorNode.typeDescriptor();
        }
        return node;
    }

    public static String getNameFromAnnotation(String packageName, String annotationName, String annotationFieldName,
                                               SemanticModel semanticModel, String defaultName,
                                               RecordFieldSymbol recordField) {
        Optional<AnnotationAttachmentSymbol> annotationAttachment = getAnnotationAttachment(packageName,
                annotationName, semanticModel, recordField.annotAttachments());
        if (annotationAttachment.isPresent() && annotationAttachment.get().isConstAnnotation() &&
                annotationAttachment.get().attachmentValue().isPresent()) {
            Object value = annotationAttachment.get().attachmentValue().get().value();
            Optional<String> name = getNameFromValue(annotationFieldName, value);
            if (name.isPresent()) {
                return name.get();
            }
        }
        return defaultName;
    }

    private static Optional<AnnotationAttachmentSymbol> getAnnotationAttachment(String packageName,
            String annotationName, SemanticModel semanticModel, List<AnnotationAttachmentSymbol> annotations) {
        return annotations.stream()
                .filter(annotAttachment -> isMatchingAnnotation(packageName, annotationName,
                        semanticModel, annotAttachment))
                .findFirst();
    }

    private static boolean isMatchingAnnotation(String packageName, String annotationName, SemanticModel semanticModel,
                                                AnnotationAttachmentSymbol annotAttachment) {
        if (annotAttachment.typeDescriptor().typeDescriptor().isEmpty()) {
            return false;
        }
        Optional<Symbol> exampleValueSymbol = semanticModel.types().getTypeByName(BALLERINA, packageName, EMPTY,
                annotationName);
        if (exampleValueSymbol.isEmpty() ||
                !(exampleValueSymbol.get() instanceof TypeDefinitionSymbol serviceContractInfoType)) {
            return false;
        }
        return annotAttachment.typeDescriptor().typeDescriptor().get()
                .subtypeOf(serviceContractInfoType.typeDescriptor());
    }

    private static Optional<String> getNameFromValue(String annotationFieldName, Object value) {
        if (value instanceof ConstantValue constantNameValue) {
            value = constantNameValue.value();
        } else if (value instanceof BLangConstantValue constantNameValue) {
            value = constantNameValue.value;
        }

        if (!(value instanceof HashMap<?, ?> nameMap)) {
            return Optional.empty();
        }

        Object name = nameMap.get(annotationFieldName);
        if (Objects.isNull(name) || !(name instanceof ConstantValue constantName) ||
                !(constantName.value() instanceof String nameFromAnnotation)) {
            return Optional.empty();
        }

        return Optional.of(nameFromAnnotation);
    }

    public static String getModuleName(SemanticModel semanticModel, Node node) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        if (symbol.isEmpty()) {
            return "";
        }
        return getModuleName(symbol.get());
    }

    public static String getModuleName(Symbol symbol) {
        Optional<ModuleSymbol> module = symbol.getModule();
        if (module.isEmpty()) {
            return "";
        }
        return module.get().id().moduleName();
    }

    public static String getModuleNameString(ModuleName moduleName) {
        String packageName = moduleName.packageName().value();
        if (Objects.isNull(moduleName.moduleNamePart())) {
            return packageName;
        }
        return String.format("%s.%s", packageName, moduleName.moduleNamePart());
    }
}
