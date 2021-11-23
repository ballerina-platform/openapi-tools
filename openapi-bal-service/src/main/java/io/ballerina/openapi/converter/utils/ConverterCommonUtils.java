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

package io.ballerina.openapi.converter.utils;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.SPECIAL_CHAR_REGEX;

/**
 * Utilities used in Ballerina  to OpenAPI converter.
 */
public class ConverterCommonUtils {

    /**
     * Retrieves a matching OpenApi {@link Schema} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return OpenApi {@link Schema} for type defined by {@code type}
     */
    public static Schema getOpenApiSchema(String type) {
        Schema schema;
        switch (type) {
            case Constants.STRING:
            case Constants.PLAIN:
                schema = new StringSchema();
                break;
            case Constants.BOOLEAN:
                schema = new BooleanSchema();
                break;
            case Constants.ARRAY:
                schema = new ArraySchema();
                break;
            case Constants.INT:
            case Constants.INTEGER:
                schema = new IntegerSchema();
                break;
            case Constants.BYTE_ARRAY:
            case Constants.OCTET_STREAM:
                schema = new StringSchema();
                schema.setFormat("uuid");
                break;
            case Constants.NUMBER:
            case Constants.DECIMAL:
                schema = new NumberSchema();
                schema.setFormat(Constants.DOUBLE);
                break;
            case Constants.FLOAT:
                schema = new NumberSchema();
                schema.setFormat(Constants.FLOAT);
                break;
            case Constants.MAP_JSON:
                schema = new ObjectSchema();
                break;
            case Constants.TYPE_REFERENCE:
            case Constants.TYPEREFERENCE:
            case Constants.XML:
            case Constants.JSON:
            default:
                schema = new Schema();
                break;
        }
        return schema;
    }

    /**
     * Generate operationId by removing special characters.
     *
     * @param identifier input function name, record name or operation Id
     * @return string with new generated name
     */
    public static String getValidName(String identifier) {
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(SPECIAL_CHAR_REGEX);
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
            identifier = validName.toString();
        }
        return identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1);
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
            Node annotReference = annotation.annotReference();
            if (annotReference.toString().trim().equals(annotationReference) && annotation.annotValue().isPresent()) {
                MappingConstructorExpressionNode listOfAnnotValue = annotation.annotValue().get();
                for (MappingFieldNode field : listOfAnnotValue.fields()) {
                    SpecificFieldNode fieldNode = (SpecificFieldNode) field;
                    if ((fieldNode).fieldName().toString().trim().equals(annotationField)
                            && fieldNode.valueExpr().isPresent()) {
                        ExpressionNode expressionNode = fieldNode.valueExpr().get();
                        return Optional.of(expressionNode.toString().trim().replaceAll("\"", ""));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * This function uses to take the service declaration node from given required node and return all the annotation
     * nodes that attached to service node.
     */
    public static NodeList<AnnotationNode> getAnnotationNodesFromServiceNode(RequiredParameterNode headerParam) {
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createEmptyNodeList();
        NonTerminalNode parent = headerParam.parent();
        while (parent.kind() != SyntaxKind.SERVICE_DECLARATION) {
            parent = parent.parent();
        }
        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) parent;
        if (serviceNode.metadata().isPresent()) {
            MetadataNode metadataNode = serviceNode.metadata().get();
            annotations = metadataNode.annotations();
        }
        return annotations;
    }

    /**
     * This function for taking the specific media-type subtype prefix from http service configuration annotation.
     * <pre>
     *     @http:ServiceConfig {
     *          mediaTypeSubtypePrefix : "vnd.exm.sales"
     *  }
     * </pre>
     */
    public static Optional<String> extractCustomMediaType(FunctionDefinitionNode functionDefNode) {
        ServiceDeclarationNode serviceDefNode = (ServiceDeclarationNode) functionDefNode.parent();
        if (serviceDefNode.metadata().isPresent()) {
            MetadataNode metadataNode = serviceDefNode.metadata().get();
            NodeList<AnnotationNode> annotations = metadataNode.annotations();
            if (!annotations.isEmpty()) {
                return ConverterCommonUtils.extractServiceAnnotationDetails(annotations,
                        "http:ServiceConfig", "mediaTypeSubtypePrefix");
            }
        }
        return Optional.empty();
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
     * @param definitionURI     URI for the OpenAPI contract
     * @return {@link OASResult}  OpenAPI model
     */
    public static OASResult parseOpenAPIFile(String definitionURI) {
        List<OpenAPIConverterDiagnostic> diagnostics = new ArrayList<>();
        Path contractPath = Paths.get(definitionURI);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);

        if (!Files.exists(contractPath)) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_110;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                    error.getDescription(), null);
            diagnostics.add(diagnostic);
        }
        if (!(definitionURI.endsWith(Constants.YAML_EXTENSION) || definitionURI.endsWith(Constants.JSON_EXTENSION)
                || definitionURI.endsWith(Constants.YML_EXTENSION))) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_110;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                    error.getDescription(), null);
            diagnostics.add(diagnostic);
        }
        String openAPIFileContent = null;
        try {
            openAPIFileContent = Files.readString(Paths.get(definitionURI));
        } catch (IOException e) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_108;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode()
                    , error.getDescription(), null, e.toString());
            diagnostics.add(diagnostic);
        }
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent, null,
                parseOptions);
        if (!parseResult.getMessages().isEmpty()) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_112;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode()
                    , error.getDescription(), null);
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
                stringBuilder.append(path.substring(0, 1).toUpperCase(Locale.ENGLISH) + path.substring(1));
                stringBuilder.append(" ");
            }
            title = stringBuilder.toString().trim();
        } else if (urlPaths.length == 1 && !urlPaths[0].isBlank()) {
            stringBuilder.append(urlPaths[0].substring(0, 1).toUpperCase(Locale.ENGLISH) + urlPaths[0].substring(1));
            title = stringBuilder.toString().trim();
        }
        return title;
    }

    public static boolean isHttpService(ModuleSymbol moduleSymbol) {
        Optional<String> moduleNameOpt = moduleSymbol.getName();
        return moduleNameOpt.isPresent() && Constants.HTTP.equals(moduleNameOpt.get())
                && Constants.BALLERINA.equals(moduleSymbol.id().orgName());
    }
}
