package io.ballerina.openapi.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.ballerina.openapi.generators.schema.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.openapi.generators.schema.TypeGeneratorUtils.getTypeGenerator;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for anyOf and oneOf schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *    schemas:
 *     Employee:
 *       anyOf:
 *       - $ref: "#/components/schemas/InternalEmployee"
 *       - $ref: "#/components/schemas/ExternalEmployee"
 *  </pre>
 * Generated Ballerina type for the anyOf schema `Employee` :
 * <pre>
 *  public type Employee InternalEmployee|ExternalEmployee
 * </pre>
 *
 * @since 2.0.0
 */
public class UnionTypeGenerator extends TypeGenerator {

    public UnionTypeGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }
    private final List<TypeDefinitionNode> typeDefinitionNodeList = new ArrayList<>();
    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }


    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        assert schema instanceof ComposedSchema;
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Schema> schemas;
        if (composedSchema.getOneOf() != null) {
            schemas = composedSchema.getOneOf();
        } else {
            schemas = composedSchema.getAnyOf();
        }
        TypeDescriptorNode unionTypeDesc = getUnionType(schemas, typeName);
        return TypeGeneratorUtils.getNullableType(schema, unionTypeDesc);
    }

    /**
     * Creates the UnionType string to generate bal type for a given oneOf or anyOf type schema.
     *
     * @param schemas  List of schemas included in the anyOf or oneOf schema
     * @param typeName This is parameter or variable name that used to populate error message meaningful
     * @return Union type
     * @throws BallerinaOpenApiException when unsupported combination of schemas found
     */
    private TypeDescriptorNode getUnionType(List<Schema> schemas, String typeName)
            throws BallerinaOpenApiException {
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        for (Schema schema : schemas) {
            TypeGenerator typeGenerator = getTypeGenerator(schema, typeName, null);
            TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
            if (typeDescriptorNode instanceof OptionalTypeDescriptorNode &&
                    GeneratorMetaData.getInstance().isNullable()) {
                Node internalTypeDesc = ((OptionalTypeDescriptorNode) typeDescriptorNode).typeDescriptor();
                typeDescriptorNode = (TypeDescriptorNode) internalTypeDesc;
            }
            typeDescriptorNodes.add(typeDescriptorNode);
            if (typeGenerator instanceof ArrayTypeGenerator &&
                    ((ArrayTypeGenerator) typeGenerator).getArrayItemWithConstraint() != null) {
                TypeDefinitionNode arrayItemWithConstraint =
                        ((ArrayTypeGenerator) typeGenerator).getArrayItemWithConstraint();
                typeDefinitionNodeList.add(arrayItemWithConstraint);
            }
        }
        if (typeDescriptorNodes.size() > 1) {
            UnionTypeDescriptorNode unionTypeDescriptorNode = null;
            TypeDescriptorNode leftTypeDesc = typeDescriptorNodes.get(0);
            for (int i = 1; i < typeDescriptorNodes.size(); i++) {
                TypeDescriptorNode rightTypeDesc = typeDescriptorNodes.get(i);
                unionTypeDescriptorNode = createUnionTypeDescriptorNode(leftTypeDesc, createToken(PIPE_TOKEN),
                        rightTypeDesc);
                leftTypeDesc = unionTypeDescriptorNode;
            }
            return unionTypeDescriptorNode;
        } else {
            return typeDescriptorNodes.get(0);
        }
    }
}
