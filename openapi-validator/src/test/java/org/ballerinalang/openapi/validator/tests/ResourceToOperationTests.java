package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.ballerinalang.openapi.validator.Filters;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ResourceMethod;
import org.ballerinalang.openapi.validator.ResourceValidator;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.openapi.validator.error.ValidationError;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ResourceToOperationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/modules/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private Project project;
    private Operation operation;
    private ResourceMethod resourceMethod;
    private SyntaxTree syntaxTree;
    private SemanticModel semanticModel;
    private List<ValidationError> validationError = new ArrayList<>();
    private DiagnosticSeverity kind;
    private DiagnosticLog dLog;
    private Filters filters;

    @Test(description = "valid test for Path parameter")
    public void testPathParameterValid() throws OpenApiValidatorException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore_path_parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/valid/petstore_path_parameter.bal"));
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project,"/pets/{petId}", "get" );
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                ,syntaxTree);

    }

}
