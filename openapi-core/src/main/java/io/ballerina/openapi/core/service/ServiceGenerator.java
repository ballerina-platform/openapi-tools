package io.ballerina.openapi.core.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.resource.ResourceGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

public abstract class ServiceGenerator {

    final OASServiceMetadata oasServiceMetadata;

    public ServiceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static List<GenSrcFile> generateServiceFiles(OASServiceMetadata oasServiceMetadata) throws
            FormatterException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String mainContent;
        if (oasServiceMetadata.isGenerateOnlyServiceType()) {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata);
            serviceTypeGenerator.generateSyntaxTree();
            String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
        } else {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata);
            String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
            ServiceDeclarationGenerator serviceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
            mainContent = Formatter.format(serviceGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    oasServiceMetadata.getSrcFile(),
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + mainContent));
        }
        return sourceFiles;
    }

    public abstract SyntaxTree generateSyntaxTree();

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
                        functions.add(resourceFunction);
                    }
                }
            } else {
                FunctionDefinitionNode resourceFunction = resourceGenerator
                        .generateResourceFunction(operation, path);
                functions.add(resourceFunction);
            }
        }
        return functions;
    }
}
