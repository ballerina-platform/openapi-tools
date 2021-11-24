package io.ballerina.openapi.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

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

    public UnionTypeGenerator(Schema schema) {
        super(schema);
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
        String unionTypeCont = TypeGeneratorUtils.getUnionType(schemas);
        unionTypeCont = TypeGeneratorUtils.getNullableType(schema, unionTypeCont);
        return createSimpleNameReferenceNode(createIdentifierToken(unionTypeCont));
    }
}
