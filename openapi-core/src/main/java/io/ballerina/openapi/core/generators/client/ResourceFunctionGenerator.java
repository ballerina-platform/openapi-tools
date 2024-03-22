package io.ballerina.openapi.core.generators.client;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Operation;
import org.ballerinalang.model.tree.FunctionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceFunctionGenerator implements FunctionGenerator {
    List<Diagnostic> diagnostics = new ArrayList<>();
    Map.Entry<String, Operation> operations;
    String path;

    ResourceFunctionGenerator(Map.Entry<String, Operation> operations, String path){
        this.operations = operations;
        this.path = path;
    }
    @Override
    public FunctionNode generateFunction() {
        //create resource signature
        //create resource body
        //create resource return type

        return null;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
