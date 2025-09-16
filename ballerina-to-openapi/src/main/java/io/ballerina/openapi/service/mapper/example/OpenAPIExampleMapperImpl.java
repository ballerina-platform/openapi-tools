/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.service.mapper.example;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.field.RecordFieldExampleMapper;
import io.ballerina.openapi.service.mapper.example.parameter.DefaultParamExampleMapper;
import io.ballerina.openapi.service.mapper.example.parameter.PathExampleMapper;
import io.ballerina.openapi.service.mapper.example.parameter.RequestExampleMapper;
import io.ballerina.openapi.service.mapper.example.type.TypeExampleMapper;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.PackageMemberVisitor;
import io.ballerina.openapi.service.mapper.model.ResourceFunction;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.ballerina.openapi.service.mapper.type.extension.BallerinaPackage;
import io.ballerina.openapi.service.mapper.type.extension.BallerinaTypeExtensioner;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HEADER;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_HEADER_TYPE;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_PAYLOAD_TYPE;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_QUERY_TYPE;
import static io.ballerina.openapi.service.mapper.Constants.PATH;
import static io.ballerina.openapi.service.mapper.Constants.QUERY;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getHeaderName;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOperationId;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getResourceFunction;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

/**
 * This {@link OpenAPIExampleMapper} class is the implementation of the {@link OpenAPIExampleMapper} interface.
 * This class is responsible for mapping all the examples in the OpenAPI specification.
 *
 * @since 2.1.0
 */
public class OpenAPIExampleMapperImpl implements OpenAPIExampleMapper {

    private final OpenAPI openAPI;
    private final List<OpenAPIMapperDiagnostic> diagnostics;
    private final ServiceNode serviceDeclarationNode;
    private final SemanticModel semanticModel;
    private final PackageMemberVisitor packageMemberVisitor;
    private final String currentModuleName;

    private enum ParameterType {
        PAYLOAD,
        QUERY,
        HEADER,
        OTHER
    }

    public OpenAPIExampleMapperImpl(OpenAPI openAPI, ServiceNode serviceNode, AdditionalData additionalData) {
        this.openAPI = openAPI;
        this.diagnostics = additionalData.diagnostics();
        this.serviceDeclarationNode = serviceNode;
        this.semanticModel = additionalData.semanticModel();
        this.packageMemberVisitor = additionalData.packageMemberVisitor();
        this.currentModuleName = additionalData.currentModuleName();
    }

    @Override
    public void setExamples() {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            return;
        }
        Map<String, Schema> schemas = components.getSchemas();
        schemas.forEach(this::setExamplesForTypes);

        serviceDeclarationNode.members().forEach(member -> {
            Optional<ResourceFunction> resourceFunctionOpt = getResourceFunction(member);
            if (resourceFunctionOpt.isPresent()) {
                ResourceFunction resourceFunction = resourceFunctionOpt.get();
                openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap()
                    .forEach((httpMethod, operation) -> {
                        if (getOperationId(resourceFunction).equals(operation.getOperationId())) {
                            setExamplesForResource(resourceFunction, operation);
                        }
                    })
                );
            }
        });
    }

    private void setExamplesForTypes(String name, Schema schema) {
        Optional<TypeDefinitionSymbol> ballerinaType = getTypeDefinitionNode(name, schema);
        if (ballerinaType.isEmpty()) {
            return;
        }

        TypeDefinitionSymbol typeDefSymbol = ballerinaType.get();
        // Map examples from types
        ExampleAnnotationMapper typeExampleMapper = new TypeExampleMapper(typeDefSymbol, schema, semanticModel,
                diagnostics);
        typeExampleMapper.setExample();

        if (typeDefSymbol.typeDescriptor() instanceof RecordTypeSymbol recordTypeSymbol
                && schema instanceof ObjectSchema objectSchema) {
            // Map examples from record fields
            ExampleAnnotationMapper recordFieldExampleMapper = new RecordFieldExampleMapper(name, recordTypeSymbol,
                    objectSchema, semanticModel, diagnostics);
            recordFieldExampleMapper.setExample();
        }
    }

    private Optional<TypeDefinitionSymbol> getTypeDefinitionNode(String name, Schema schema) {
        Optional<BallerinaPackage> ballerinaExt = BallerinaTypeExtensioner.getExtension(schema);
        Optional<Symbol> ballerinaType;
        if (ballerinaExt.isPresent()) {
            BallerinaPackage ballerinaPkg = ballerinaExt.get();
            String typeName = Objects.isNull(ballerinaPkg.name()) ? name : ballerinaPkg.name();
            ballerinaType = semanticModel.types().getTypeByName(ballerinaPkg.orgName(), ballerinaPkg.moduleName(),
                    ballerinaPkg.version(), typeName);
        } else {
            // Try to get it from the module member visitor
            // This only works for the types that are defined in the same module as the service
            Optional<TypeDefinitionNode> typeDefinitionNode = packageMemberVisitor
                    .getTypeDefinitionNode(currentModuleName, name);
            if (typeDefinitionNode.isEmpty()) {
                return Optional.empty();
            }
            ballerinaType = semanticModel.symbol(typeDefinitionNode.get());
        }

        if (ballerinaType.isEmpty() || !(ballerinaType.get() instanceof TypeDefinitionSymbol typeDefSymbol)) {
            return Optional.empty();
        }
        return Optional.of(typeDefSymbol);
    }

    private void setExamplesForResource(ResourceFunction resourceFunction, Operation operation) {
        SeparatedNodeList<ParameterNode> parameters = resourceFunction.functionSignature().parameters();
        parameters.forEach(parameter -> setExamplesForResourceSignatureParam(parameter, operation));

        List<ResourcePathParameterNode> pathParameters = resourceFunction.relativeResourcePath().stream()
                .filter(param -> param instanceof ResourcePathParameterNode)
                .map(param -> (ResourcePathParameterNode) param)
                .toList();
        pathParameters.forEach(pathParam -> setExamplesForResourcePathParam(pathParam, operation));
    }

    private void setExamplesForResourceSignatureParam(ParameterNode parameterNode, Operation operation) {
        Optional<Symbol> parameterSymbolOpt = semanticModel.symbol(parameterNode);
        if (parameterSymbolOpt.isEmpty() || !(parameterSymbolOpt.get() instanceof ParameterSymbol parameterSymbol)) {
            return;
        }

        ParameterType parameterType = getParameterType(parameterSymbol, semanticModel);
        switch (parameterType) {
            case PAYLOAD:
                RequestBody requestBody = operation.getRequestBody();
                ExamplesAnnotationMapper reqExampleMapper = new RequestExampleMapper(parameterSymbol, requestBody,
                        semanticModel, diagnostics);
                reqExampleMapper.setExample();
                reqExampleMapper.setExamples();
                break;
            case QUERY:
                Optional<Parameter> queryParam = operation.getParameters().stream()
                        .filter(param -> param.getIn().equals(QUERY) &&
                                param.getName().equals(unescapeIdentifier(parameterSymbol.getName().get())))
                        .findFirst();
                if (queryParam.isEmpty()) {
                    return;
                }
                ExamplesAnnotationMapper queryExampleMapper = new DefaultParamExampleMapper(QUERY, parameterSymbol,
                        queryParam.get(), semanticModel, diagnostics);
                queryExampleMapper.setExample();
                queryExampleMapper.setExamples();
                break;
            case HEADER:
                Optional<Parameter> headerParam = operation.getParameters().stream()
                        .filter(param -> param.getIn().equals(HEADER) && param.getName()
                                .equals(getHeaderName(parameterNode,
                                        unescapeIdentifier(parameterSymbol.getName().get()))))
                        .findFirst();
                if (headerParam.isEmpty()) {
                    return;
                }
                ExamplesAnnotationMapper headerExampleMapper = new DefaultParamExampleMapper(HEADER, parameterSymbol,
                        headerParam.get(), semanticModel, diagnostics);
                headerExampleMapper.setExample();
                headerExampleMapper.setExamples();
                break;
            default:
                break;
        }
    }

    private void setExamplesForResourcePathParam(ResourcePathParameterNode parameterNode, Operation operation) {
        Optional<Symbol> parameterSymbolOpt = semanticModel.symbol(parameterNode);
        if (parameterSymbolOpt.isEmpty() ||
                !(parameterSymbolOpt.get() instanceof PathParameterSymbol parameterSymbol)) {
            return;
        }

        Optional<Parameter> pathParam = operation.getParameters().stream()
                .filter(parameter -> parameter.getIn().equals(PATH) &&
                        parameter.getName().equals(unescapeIdentifier(parameterSymbol.getName().get())))
                .findFirst();
        if (pathParam.isEmpty()) {
            return;
        }

        ExamplesAnnotationMapper pathExampleMapper = new PathExampleMapper(parameterSymbol, pathParam.get(),
                semanticModel, diagnostics);
        pathExampleMapper.setExample();
        pathExampleMapper.setExamples();
    }

    private static ParameterType getParameterType(ParameterSymbol parameterSymbol, SemanticModel semanticModel) {
        TypeSymbol parameterTypeSymbol = parameterSymbol.typeDescriptor();
        if (!parameterTypeSymbol.subtypeOf(semanticModel.types().ANYDATA)) {
            return ParameterType.OTHER;
        }

        List<AnnotationSymbol> httpAnnotations = parameterSymbol.annotations().stream()
                .filter(annotation -> annotation.typeDescriptor().isPresent() &&
                        isHttpPackageAnnotationTypeDesc(annotation.typeDescriptor().get()))
                .toList();
        if (httpAnnotations.isEmpty()) {
            return ParameterType.QUERY;
        }

        for (AnnotationSymbol annotation : httpAnnotations) {
            TypeSymbol annotationType = annotation.typeDescriptor().get();
            if (isSubTypeOfHttpType(annotationType, HTTP_PAYLOAD_TYPE, semanticModel)) {
                return ParameterType.PAYLOAD;
            }
            if (isSubTypeOfHttpType(annotationType, HTTP_HEADER_TYPE, semanticModel)) {
                return ParameterType.HEADER;
            }
            if (isSubTypeOfHttpType(annotationType, HTTP_QUERY_TYPE, semanticModel)) {
                return ParameterType.QUERY;
            }
        }
        return ParameterType.QUERY;
    }

    private static boolean isHttpPackageAnnotationTypeDesc(TypeSymbol typeSymbol) {
        Optional<ModuleSymbol> module = typeSymbol.getModule();
        if (module.isEmpty()) {
            return false;
        }
        ModuleID id = module.get().id();
        return id.orgName().equals(BALLERINA) && id.moduleName().startsWith(HTTP);
    }

    private static boolean isSubTypeOfHttpType(TypeSymbol typeSymbol, String httpTypeName,
                                               SemanticModel semanticModel) {
        Optional<Symbol> httpType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY, httpTypeName);
        if (httpType.isEmpty() || !(httpType.get() instanceof TypeDefinitionSymbol httpTypeDef)) {
            return false;
        }
        return typeSymbol.subtypeOf(httpTypeDef.typeDescriptor());
    }
}
