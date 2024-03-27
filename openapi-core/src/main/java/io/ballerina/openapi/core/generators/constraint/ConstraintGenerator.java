package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NULL;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;

public class ConstraintGenerator {

    private final HashMap<String, Schema> constraintSchema = new HashMap<>();

    public void generateConstraintForTypes(OpenAPI openAPI, SyntaxTree syntaxTree) {
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        schemas.forEach((key, value) -> {
            if (GeneratorUtils.hasConstraints(value)) {
                constraintSchema.put(key, value);
            }
        });
        syntaxTree.rootNode().accept(new ModuleMemberVisitor());
    }

    private TypeDefinitionNode generateConstraintForType(Schema value, String key, TypeDefinitionNode syntaxTree) {
        return null;
    }

    private class ModuleMemberVisitor extends NodeVisitor {
        @Override
        public void visit(TypeDefinitionNode typeDefinitionNode) {
            if (constraintSchema.containsKey(typeDefinitionNode.typeName().text())) {
                TypeDefinitionNode nodeWithConstraint = generateConstraintForType(constraintSchema.get(
                        typeDefinitionNode.typeName().text()), typeDefinitionNode.typeName().text(),
                        typeDefinitionNode);
            }
        }
    }
}
