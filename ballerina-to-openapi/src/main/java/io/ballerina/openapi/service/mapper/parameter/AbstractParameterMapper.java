/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractParameterMapper {
    final OperationInventory operationInventory;

    abstract Parameter getParameterSchema();

    protected AbstractParameterMapper(OperationInventory operationInventory) {
        this.operationInventory = operationInventory;
    }

    public List<Parameter> getParameterSchemaList() {
        Parameter parameter = getParameterSchema();
        if (Objects.nonNull(parameter)) {
            return List.of(parameter);
        }
        return new ArrayList<>();
    }

    public void setParameter() {
        List<Parameter> parameterList = getParameterSchemaList();
        parameterList.forEach(operationInventory::setParameter);
    }

    static Object getDefaultValue(DefaultableParameterNode parameterNode) {
        Node defaultValueExpression = parameterNode.expression();
        if (MapperCommonUtils.isNotSimpleValueLiteralKind(defaultValueExpression.kind())) {
            return null;
        }
        return MapperCommonUtils.parseBalSimpleLiteral(defaultValueExpression.toString().trim());
    }

    static boolean hasObjectType(SemanticModel semanticModel, TypeSymbol parameterType) {
        TypeSymbol mapOfAnydata = semanticModel.types().builder().MAP_TYPE.withTypeParam(
                semanticModel.types().ANYDATA).build();
        TypeSymbol arrayOfMapOfAnydata = semanticModel.types().builder().ARRAY_TYPE.withType(
                mapOfAnydata).build();
        return parameterType.subtypeOf(semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                mapOfAnydata,
                arrayOfMapOfAnydata,
                semanticModel.types().NIL).build()
        );
    }
}
