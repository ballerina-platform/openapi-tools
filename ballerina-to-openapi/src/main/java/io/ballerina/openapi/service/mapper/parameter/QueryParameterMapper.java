// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.service.mapper.model.OperationAdaptor;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.UnionTypeMapper;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.removeStartingSingleQuote;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

public class QueryParameterMapper extends AbstractParameterMapper {

    private TypeSymbol type = null;
    private String name = null;
    private boolean isRequired = false;
    private String description = null;
    private boolean treatNilableAsOptional = false;
    private TypeMapper typeMapper = null;
    private Object defaultValue = null;

    public QueryParameterMapper(ParameterNode parameterNode, Map<String, String> apiDocs,
                                OperationAdaptor operationAdaptor, boolean treatNilableAsOptional,
                                TypeMapper typeMapper) {
        super(operationAdaptor);
        Symbol parameterSymbol = typeMapper.getComponentMapperData().
                semanticModel().symbol(parameterNode).orElse(null);
        if (Objects.nonNull(parameterSymbol) && (parameterSymbol instanceof ParameterSymbol queryParameter)) {
            this.type = queryParameter.typeDescriptor();
            this.name = unescapeIdentifier(queryParameter.getName().get());
            this.isRequired = queryParameter.paramKind().equals(ParameterKind.REQUIRED);
            this.description = apiDocs.get(removeStartingSingleQuote(queryParameter.getName().get()));
            this.treatNilableAsOptional = treatNilableAsOptional;
            this.typeMapper = typeMapper;
            if (parameterNode instanceof DefaultableParameterNode defaultableQueryParam) {
                this.defaultValue = AbstractParameterMapper.getDefaultValue(defaultableQueryParam);
            }
        }
    }

    @Override
    public QueryParameter getParameterSchema() {
        if (Objects.isNull(type)) {
            return null;
        }
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setName(name);
        if (isRequired && (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(type))) {
            queryParameter.setRequired(true);
        }
        Schema typeSchema = typeMapper.getTypeSchema(type);
        if (Objects.nonNull(defaultValue)) {
            TypeMapper.setDefaultValue(typeSchema, defaultValue);
        }
        if (AbstractParameterMapper.hasObjectType(typeMapper.getComponentMapperData().semanticModel(), type)) {
            Content content = new Content();
            content.put("application/json", new MediaType().schema(typeSchema));
            queryParameter.setContent(content);
        } else {
            queryParameter.setSchema(typeSchema);
        }
        queryParameter.setDescription(description);
        return queryParameter;
    }
}
