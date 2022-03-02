package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.ATTRIBUTE_CONTRACT_PATH;
import static io.ballerina.openapi.validator.Constants.ATTRIBUTE_EXCLUDE_OPERATIONS;
import static io.ballerina.openapi.validator.Constants.ATTRIBUTE_EXCLUDE_TAGS;
import static io.ballerina.openapi.validator.Constants.ATTRIBUTE_FAIL_ON_ERRORS;
import static io.ballerina.openapi.validator.Constants.ATTRIBUTE_OPERATIONS;
import static io.ballerina.openapi.validator.Constants.EMBED;
import static io.ballerina.openapi.validator.Constants.OPENAPI_ANNOTATION;
import static io.ballerina.openapi.validator.ValidatorUtils.isHttpService;
import static io.ballerina.openapi.validator.ValidatorUtils.parseOpenAPIFile;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;
import static io.ballerina.openapi.validator.error.CompilationError.EMPTY_CONTRACT_PATH;
import static io.ballerina.openapi.validator.error.CompilationError.NON_HTTP_SERVICE;
import static io.ballerina.openapi.validator.error.CompilationError.UNEXPECTED_EXCEPTIONS;

public class PreValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private Filter filter;

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public Filter getFilter() {
        return filter;
    }

    public void initialize(SyntaxNodeAnalysisContext context) {
        this.context = context;
    }

    public void preValidation() {
        // 1. Checking receive service node has compilation issue
        SemanticModel semanticModel = context.semanticModel();
        List<Diagnostic> diagnostics = semanticModel.diagnostics();
        boolean erroneousCompilation = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR == d.diagnosticInfo().severity());
        if (erroneousCompilation) {
            return;
        }

        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) this.context.node();
        Path ballerinaFilePath = getBallerinaFilePath();
        Optional<MetadataNode> metadata = serviceNode.metadata();
        boolean validatorEnable = false;
        Location location = serviceNode.location();

        // 2. Preprocessing - return OpenAPI and filter

        if (metadata.isEmpty()) {
            return;
        }
        // 1. Check openapi annotation is available -- done
        // 2. Check contract path is available and exist -- done
        MetadataNode serviceMetadata = metadata.orElseThrow();
        NodeList<AnnotationNode> annotations = serviceMetadata.annotations();
        if (annotations.isEmpty()) {
            return;
        }
        for (AnnotationNode annotation : annotations) {
            Node node = annotation.annotReference();
            if (node.toString().trim().equals(OPENAPI_ANNOTATION)) {
                SeparatedNodeList<MappingFieldNode> fields =
                        extractAnnotationAttributeList(serviceNode, location, annotation);
                if (fields == null) {
                    return;
                }
                // This condition for check the annotation has `embed` field only, if there, then validation
                // disable.
                boolean isEmbed = (fields.size() == 1 &&
                        (((SpecificFieldNode) fields.get(0)).fieldName().toString().trim().equals(EMBED)));
                if (isEmbed) {
                    return;
                }
                Filter.FilterBuilder filterBuilder = new Filter.FilterBuilder();
                for (MappingFieldNode field : fields) {
                    if (!(field instanceof SpecificFieldNode)) {
                        return;
                    }
                    SpecificFieldNode specificField = (SpecificFieldNode) field;
                    Optional<ExpressionNode> expressionNode = specificField.valueExpr();
                    if (expressionNode.isEmpty()) {
                        return;
                    }
                    ExpressionNode expression = expressionNode.get();
                    ListConstructorExpressionNode list;
                    List<String> values = new ArrayList<>();
                    if (expression instanceof ListConstructorExpressionNode) {
                        list = (ListConstructorExpressionNode) expression;
                        values = setFilters(list);
                    }
                    Node fieldName = specificField.fieldName();
                    String attributeName = ((Token) fieldName).text();
                    switch (attributeName) {
                        case ATTRIBUTE_CONTRACT_PATH:
                            Path openAPIPath = Paths.get(expression.toString().replaceAll("\"",
                                    "").trim());
                            this.openAPI = getOpenAPIContract(ballerinaFilePath, location, openAPIPath);
                            if (openAPI != null) {
                                validatorEnable = true;
                            }
                            break;
                        case ATTRIBUTE_FAIL_ON_ERRORS:
                            filterBuilder.kind(DiagnosticSeverity.WARNING);
                            break;
                        case Constants.ATTRIBUTE_TAGS:
                            filterBuilder.tag(values);
                            break;
                        case ATTRIBUTE_OPERATIONS:
                            filterBuilder.operation(values);
                            break;
                        case ATTRIBUTE_EXCLUDE_TAGS:
                            filterBuilder.excludeTag(values);
                            break;
                        case ATTRIBUTE_EXCLUDE_OPERATIONS:
                            filterBuilder.excludeOperation(values);
                            break;
                        default:
                            break;
                    }
                }
                this.filter = filterBuilder.build();
            }
            if (validatorEnable) {
                break;
            }
        }

    }

    private SeparatedNodeList<MappingFieldNode> extractAnnotationAttributeList(ServiceDeclarationNode serviceNode,
                                                                               Location location, AnnotationNode annotation) {
        // Test given service is http service, if not this will return WARNING and execute the ballerina
        // file.
        if (!isHttpService(serviceNode, context.semanticModel())) {
            updateContext(context, NON_HTTP_SERVICE, location);
            return null;
        }
        Optional<MappingConstructorExpressionNode> mappingNode = annotation.annotValue();
        if (mappingNode.isEmpty()) {
            return null;
        }
        MappingConstructorExpressionNode exprNode = mappingNode.get();
        SeparatedNodeList<MappingFieldNode> fields = exprNode.fields();
        // Annotation fields extract details
        // Check the annotation doesn't include any fields, then it will skip validating
        if (fields.isEmpty()) {
            return null;
        }
        return fields;
    }

    private Path getBallerinaFilePath() {
        Package aPackage = context.currentPackage();
        DocumentId documentId = context.documentId();
        Optional<Path> path = aPackage.project().documentPath(documentId);
        return path.orElse(null);
    }

    private OpenAPI getOpenAPIContract(Path ballerinaFilePath, Location location, Path openAPIPath) {
        Path relativePath = null;
        try {
            if (openAPIPath.toString().isBlank()) {
                updateContext(context, EMPTY_CONTRACT_PATH, location);
            } else if (Paths.get(openAPIPath.toString()).isAbsolute()) {
                relativePath = Paths.get(openAPIPath.toString());
            } else {
                File file = new File(ballerinaFilePath.toString());
                File parentFolder = new File(file.getParent());
                File openapiContract = new File(parentFolder, openAPIPath.toString());
                relativePath = Paths.get(openapiContract.getCanonicalPath());
            }
            if (relativePath != null && Files.exists(relativePath)) {
                return parseOpenAPIFile(context, relativePath.toString(), location);

            }
        } catch (IOException e) {
            updateContext(context, UNEXPECTED_EXCEPTIONS, location, e.getMessage());
        }
        return null;
    }

    private static List<String> setFilters(ListConstructorExpressionNode list) {
        SeparatedNodeList<Node> expressions = list.expressions();
        Iterator<Node> iterator = expressions.iterator();
        List<String> values = new ArrayList<>();
        while (iterator.hasNext()) {
            Node item = iterator.next();
            if (item.kind() == SyntaxKind.STRING_LITERAL) {
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
