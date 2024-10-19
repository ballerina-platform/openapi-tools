/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RequestBodyHeaderParameter {
    Map.Entry<String, Header> header;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();

    RequestBodyHeaderParameter(Map.Entry<String, Header> header) {
        this.header = header;
    }

    public Optional<Parameter> generateParameterSchema() {
        Schema schema = getSchemaWithDescription(header.getValue());
        Parameter parameter = new Parameter();
        parameter.setSchema(schema);
        parameter.setName(header.getKey());
        parameter.setIn("header");
        parameter.setRequired(header.getValue().getRequired());
        return Optional.of(parameter);
    }

    private Schema getSchemaWithDescription(Header header) {
        Schema schema = header.getSchema();
        schema.setDescription(header.getDescription());
        if (!Boolean.TRUE.equals(header.getRequired())) {
            schema.setNullable(true);
        }
        return schema;
    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

}
