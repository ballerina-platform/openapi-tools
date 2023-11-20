package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.CommonData;
import io.ballerina.openapi.service.mapper.type.ComponentMapper;
import io.ballerina.openapi.service.mapper.type.UnionTypeMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.utils.MapperCommonUtils.unescapeIdentifier;

public class HeaderParameterMapper implements ParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final boolean isRequired;
    private final String description;
    private final boolean treatNilableAsOptional;
    private final SemanticModel semanticModel;
    private final List<OpenAPIMapperDiagnostic> diagnostics;

    public HeaderParameterMapper(ParameterSymbol parameterSymbol, Map<String, String> apiDocs,
                                 boolean treatNilableAsOptional, SemanticModel semanticModel,
                                 List<OpenAPIMapperDiagnostic> diagnostics) {
        this.type = parameterSymbol.typeDescriptor();
        this.name = getNameFromHeaderParam(parameterSymbol);
        this.isRequired = parameterSymbol.paramKind().equals(ParameterKind.REQUIRED);
        this.description = apiDocs.get(name);
        this.treatNilableAsOptional = treatNilableAsOptional;
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    String getNameFromHeaderParam(ParameterSymbol parameterSymbol) {
        List<AnnotationSymbol> annotations = parameterSymbol.annotations();
        if (!annotations.isEmpty()) {
            for (AnnotationSymbol annotation : annotations) {
                if (annotation.typeDescriptor().get().nameEquals("HttpHeader")) {
                    return unescapeIdentifier(parameterSymbol.getName().get());
                }
            }
        }
        return unescapeIdentifier(parameterSymbol.getName().get());
    }

    @Override
    public Parameter getParameterSchema(Components components) {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setName(name);
        if (isRequired && (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(type))) {
            headerParameter.setRequired(true);
        }
        Map<String, Schema> componentSchemas = components.getSchemas();
        if (Objects.isNull(componentSchemas)) {
            componentSchemas = new HashMap<>();
        }
        CommonData commonData = new CommonData(semanticModel, null, diagnostics);
        headerParameter.setSchema(ComponentMapper.getTypeSchema(type, componentSchemas, commonData));
        if (!componentSchemas.isEmpty()) {
            components.setSchemas(componentSchemas);
        }
        headerParameter.setDescription(description);
        return headerParameter;
    }
}
