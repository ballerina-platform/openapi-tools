package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.openapi.service.mapper.CommonData;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ErrorTypeMapper extends TypeMapper {

    public ErrorTypeMapper(TypeReferenceTypeSymbol typeSymbol, CommonData commonData) {
        super(typeSymbol, commonData);
    }

    @Override
    Schema getReferenceTypeSchema(Map<String, Schema> components) {
        ErrorTypeSymbol errorTypeSymbol = (ErrorTypeSymbol) typeSymbol.typeDescriptor();
        return getSchema(errorTypeSymbol, components, commonData);
    }

    public static Schema getSchema(ErrorTypeSymbol typeSymbol, Map<String, Schema> components, CommonData commonData) {
        Optional<Symbol> optErrorPayload = commonData.semanticModel().types().getTypeByName("ballerina", "http",
                "", "ErrorPayload");
        if (optErrorPayload.isPresent() && optErrorPayload.get() instanceof TypeDefinitionSymbol errorPayload) {
            Schema schema = ComponentMapper.getTypeSchema(errorPayload.typeDescriptor(), components, commonData);
            if (Objects.nonNull(schema)) {
                components.put("ErrorPayload", schema);
                return new ObjectSchema().$ref("ErrorPayload");
            }
        }
        return new StringSchema();
    }
}
