package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

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

    public void generateConstraintForTypes(OpenAPI openAPI, SyntaxTree syntaxTree) {
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        schemas.forEach((key, value) -> {
            if (GeneratorUtils.hasConstraints(value)) {
                if (hasConstraints(value)) {
                    generateConstraintForType(value, key, syntaxTree);
                }
            }
            syntaxTree.rootNode().accept(new ModuleMemberVisitor());
        });
    }

    private void generateConstraintForType(Schema value, String key, SyntaxTree syntaxTree) {
    }

    private class ModuleMemberVisitor extends NodeVisitor {
        @Override
        public void visit(TypeDefinitionNode typeDefinitionNode) {
            Token token = typeDefinitionNode.typeName();
        }
    }

    /**
     * This util is to check if the given schema contains any constraints.
     */
    public static boolean hasConstraints(Schema<?> value) {
        if (value.getProperties() != null) {
            boolean constraintExists = value.getProperties().values().stream()
                    .anyMatch(GeneratorUtils::hasConstraints);
            if (constraintExists) {
                return true;
            }
        } else if (isComposedSchema(value)) {
            List<Schema> allOf = value.getAllOf();
            List<Schema> oneOf = value.getOneOf();
            List<Schema> anyOf = value.getAnyOf();
            boolean constraintExists = false;
            if (allOf != null) {
                constraintExists = allOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (oneOf != null) {
                constraintExists = oneOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (anyOf != null) {
                constraintExists = anyOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            }
            if (constraintExists) {
                return true;
            }
        } else if (isArraySchema(value)) {
            if (!isConstraintExists(value)) {
                return isConstraintExists(value.getItems());
            }
        }
        return isConstraintExists(value);
    }

    private static boolean isConstraintExists(Schema<?> propertyValue) {
        return propertyValue.getMaximum() != null ||
                propertyValue.getMinimum() != null ||
                propertyValue.getMaxLength() != null ||
                propertyValue.getMinLength() != null ||
                propertyValue.getMaxItems() != null ||
                propertyValue.getMinItems() != null ||
                propertyValue.getExclusiveMinimum() != null ||
                propertyValue.getExclusiveMaximum() != null ||
                propertyValue.getPattern() != null;
    }

    public static boolean isArraySchema(Schema schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), ARRAY);
    }

    public static boolean isMapSchema(Schema schema) {
        return schema.getAdditionalProperties() != null;
    }

    public static boolean isObjectSchema(Schema schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), OBJECT);
    }

    public static boolean isComposedSchema(Schema schema) {
        return schema.getAnyOf() != null || schema.getOneOf() != null ||
                schema.getAllOf() != null;
    }

    public static boolean isStringSchema(Schema<?> schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), STRING);
    }

    public static boolean isBooleanSchema(Schema<?> schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), BOOLEAN);
    }

    public static boolean isIntegerSchema(Schema<?> fieldSchema) {
        return Objects.equals(GeneratorUtils.getOpenAPIType(fieldSchema), INTEGER);
    }

    public static boolean isNumberSchema(Schema<?> fieldSchema) {
        return Objects.equals(GeneratorUtils.getOpenAPIType(fieldSchema), NUMBER);
    }

    public static String getOpenAPIType(Schema<?> schema) {
        if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            for (String type : schema.getTypes()) {
                // TODO: https://github.com/ballerina-platform/openapi-tools/issues/1497
                // this returns the first non-null type in the list
                if (!type.equals(NULL)) {
                    return type;
                }
            }
        } else if (schema.getType() != null) {
            return schema.getType();
        }
        return null;
    }
}
