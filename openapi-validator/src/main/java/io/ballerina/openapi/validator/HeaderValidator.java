/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.MetaData;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.ballerina.openapi.validator.Constants.HEADER_NAME;
import static io.ballerina.openapi.validator.Constants.HTTP_HEADER;
import static io.ballerina.openapi.validator.Constants.SQUARE_BRACKETS;
import static io.ballerina.openapi.validator.TypeValidatorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.validator.ValidatorUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.getNumberFormatType;
import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;
import static io.ballerina.openapi.validator.ValidatorUtils.unescapeIdentifier;

/**
 * This contains the header validation.
 *
 * @since 1.1.0
 */
public class HeaderValidator extends AbstractMetaData implements SectionValidator, Validator {
    private final Map<String, Node> balHeaders;
    private final List<Parameter> oasParameters;

    public HeaderValidator(MetaData metaData, Map<String, Node> balHeaders, List<Parameter> oasParameters) {
        super(metaData.getContext(), metaData.getOpenAPI(), metaData.getPath(), metaData.getMethod(),
                metaData.getSeverity(), metaData.getLocation());
        this.balHeaders = balHeaders;
        this.oasParameters = oasParameters;
    }

    @Override
    public void validate() {
        //Ballerina to openAPI header validation.
        validateBallerina();
        //OAS->Ballerina header validation
        if (oasParameters == null) {
            return;
        }
        validateOpenAPI();
    }


    /**
     * This function is used to validate the ballerina resource header against to openapi header.
     *
     */
    @Override
    public void validateBallerina() {
        for (Map.Entry<String, Node> balHeader: balHeaders.entrySet()) {
            //TODO: Nullable and default value assign scenarios

            // Available header types string|int|boolean|string[]|int[]|boolean[]|? headers
            // Need to remove `\`  while checking header name x\-client\-header
            Node headerNode = balHeader.getValue();
            String headerName = unescapeIdentifier(balHeader.getKey());
            String ballerinaType;
            NodeList<AnnotationNode> annotations;
            if (headerNode instanceof RequiredParameterNode) {
                RequiredParameterNode requireParam = (RequiredParameterNode) headerNode;
                ballerinaType = requireParam.typeName().toString().trim();
                annotations = requireParam.annotations();
            } else {
                DefaultableParameterNode defaultParam = (DefaultableParameterNode) headerNode;
                ballerinaType = defaultParam.typeName().toString().trim();
                annotations = defaultParam.annotations();
            }
            if (!annotations.isEmpty()) {
                List<String> headers = extractAnnotationFieldDetails(HTTP_HEADER, HEADER_NAME, annotations,
                        context.semanticModel());
                if (!headers.isEmpty()) {
                    headerName = headers.get(0);
                }
            }
            boolean isHeaderDocumented = false;
            if (oasParameters != null) {
                for (Parameter oasParameter: oasParameters) {
                    if (!(oasParameter instanceof HeaderParameter)) {
                        continue;
                    }
                    if (headerName.equals(oasParameter.getName())) {
                        isHeaderDocumented = true;
                        Schema<?> schema = oasParameter.getSchema();
                        String headerType = getNumberFormatType(schema);
                        if (schema instanceof ArraySchema) {
                            ArraySchema arraySchema = (ArraySchema) schema;
                            String items = arraySchema.getItems().getType();
                            Optional<String> arrayItemType = convertOpenAPITypeToBallerina(items);
                            //Array mapping
                            if (arrayItemType.isEmpty() || !ballerinaType.equals(arrayItemType.get() +
                                    SQUARE_BRACKETS)) {
                                // This special concatenation is used to check the array header parameters
                                reportDiagnostic(context, CompilationError.TYPE_MISMATCH_HEADER_PARAMETER,
                                        headerNode.location(), severity, items + SQUARE_BRACKETS,
                                        ballerinaType, headerName, method, getNormalizedPath(path));
                                break;
                            }
                        }

                        Optional<String> type = convertOpenAPITypeToBallerina(headerType);
                        if (type.isEmpty() || !ballerinaType.equals(type.get())) {
                            reportDiagnostic(context, CompilationError.TYPE_MISMATCH_HEADER_PARAMETER,
                                    headerNode.location(), severity, headerType, ballerinaType, headerName,
                                    method, getNormalizedPath(path));
                            break;
                        }
                    }
                }
            }

            if (!isHeaderDocumented) {
                // undefined header
                reportDiagnostic(context, CompilationError.UNDEFINED_HEADER, balHeader.getValue().location(),
                        severity, headerName, method, getNormalizedPath(path));
            }
        }
    }

    /**
     * Validate header from OpenAPI headers to Ballerina Headers.
     */
    @Override
    public void validateOpenAPI() {
        oasParameters.forEach(parameter -> {
            if (parameter.get$ref() != null) {
                Optional<String> parameterName = extractReferenceType(parameter.get$ref());
                if (parameterName.isEmpty()) {
                    return;
                }
                parameter = openAPI.getComponents().getParameters().get(parameterName.get());
            }
            if (parameter instanceof HeaderParameter || parameter.getIn() != null &&
                    parameter.getIn().equals("header")) {
                AtomicBoolean isHeaderExist = new AtomicBoolean(false);
                Parameter finalParameter = parameter;
                balHeaders.forEach((header, headerNode) -> {
                    if (finalParameter.getName().trim().equals(header)) {
                        isHeaderExist.set(true);
                    }
                });
                if (!isHeaderExist.get()) {
                    reportDiagnostic(context, CompilationError.MISSING_HEADER,
                            location, severity, parameter.getName(), method,
                            path);
                }
            }
        });
    }
}
