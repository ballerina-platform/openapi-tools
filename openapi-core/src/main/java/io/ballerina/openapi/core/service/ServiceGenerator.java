package io.ballerina.openapi.core.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.resource.ResourceGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ServiceGenerator {

    final OASServiceMetadata oasServiceMetadata;
    boolean isNullableRequired;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public ServiceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public abstract SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException;

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) {
        List<Node> functions = new ArrayList<>();
        if (!openApi.getPaths().isEmpty()) {
            Paths paths = openApi.getPaths();
            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
            for (Map.Entry<String, PathItem> path : pathsItems) {
                if (!path.getValue().readOperationsMap().isEmpty()) {
                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                    functions.addAll(applyFiltersForOperations(filter, path.getKey(), operationMap));
                }
            }
        }
        return functions;
    }

    private List<Node> applyFiltersForOperations(Filter filter, String path,
                                                 Map<PathItem.HttpMethod, Operation> operationMap) {
        List<Node> functions = new ArrayList<>();
        for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
            //Add filter availability
            //1.Tag filter
            //2.Operation filter
            //3. Both tag and operation filter
            List<String> filterTags = filter.getTags();
            List<String> operationTags = operation.getValue().getTags();
            List<String> filterOperations = filter.getOperations();
            ResourceGenerator resourceGenerator = ResourceGenerator.createResourceGenerator(oasServiceMetadata);
            if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                if (operationTags != null || ((!filterOperations.isEmpty())
                        && (operation.getValue().getOperationId() != null))) {
                    if ((operationTags != null && GeneratorUtils.hasTags(operationTags, filterTags)) ||
                            ((operation.getValue().getOperationId() != null) &&
                                    filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                        FunctionDefinitionNode resourceFunction = resourceGenerator
                                .generateResourceFunction(operation, path);
                        diagnostics.addAll(resourceGenerator.getDiagnostics());
                        functions.add(resourceFunction);
                    }
                }
            } else {
                FunctionDefinitionNode resourceFunction = resourceGenerator
                        .generateResourceFunction(operation, path);
                diagnostics.addAll(resourceGenerator.getDiagnostics());
                functions.add(resourceFunction);
            }
            if (resourceGenerator.isNullableRequired())
                isNullableRequired = true;
            }
        return functions;
    }
}
