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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.swagger.v3.oas.models.examples.Example;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.example.CommonUtils.extractOpenApiExampleValues;

/**
 * This {@link ExamplesMapper} class represents the abstraction of OpenAPI example and examples mapper.
 *
 * @since 2.1.0
 */
public abstract class ExamplesMapper extends ExampleMapper {

    protected ExamplesMapper(SemanticModel semanticModel) {
        super(semanticModel);
    }

    public abstract void setExamples();

    public Optional<Map<String, Example>> extractExamples(List<AnnotationAttachmentSymbol> annotations)
            throws JsonProcessingException {
        if (annotations == null) {
            return Optional.empty();
        }
        return extractOpenApiExampleValues(annotations, this.getSemanticModel());
    }
}
