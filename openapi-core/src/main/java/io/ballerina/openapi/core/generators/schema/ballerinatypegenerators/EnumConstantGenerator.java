package io.ballerina.openapi.core.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static io.ballerina.openapi.core.generators.schema.TypeGeneratorUtils.PRIMITIVE_TYPE_LIST;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for schemas with enums.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *    components:
 *     schemas:
 *       MeetingType:
 *         type: string
 *         default: live
 *         enum:
 *         - "scheduled"
 *         - "live"
 *         - "upcoming"
 *  </pre>
 * Generated Ballerina type for the schema `MeetingTypes` :
 * <pre>
 *     public type MeetingTypes "scheduled"|"live"|"upcoming";
 * </pre>
 *
 * @since 1.3.0
 */
public class EnumConstantGenerator extends TypeGenerator {

    public EnumConstantGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }

    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        List<?> enumList = schema.getEnum();
        boolean isNull = false;
        StringBuilder enumBuilder = new StringBuilder();
        if (PRIMITIVE_TYPE_LIST.contains(schema.getType().trim())) {
            for (Object enumValue : enumList) {
                isNull = enumValue == null;
                if (isNull) {
                    continue;
                }
                if (enumValue instanceof String) {
                    enumBuilder.append("\"").append(enumValue).append("\"").append("|");
                } else {
                    enumBuilder.append(enumValue).append("|");
                }
            }
        }

        if (enumBuilder.length() > 0) {
            enumBuilder.deleteCharAt(enumBuilder.length() - 1);
            String enumString = isNull ? enumBuilder.toString() + "?" : enumBuilder.toString();
            return NodeParser.parseTypeDescriptor(enumString);
        }
        return null;
    }
}
