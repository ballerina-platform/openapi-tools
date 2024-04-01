package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FunctionStatusCodeReturnTypeGenerator extends FunctionReturnTypeGeneratorImp {

    public FunctionStatusCodeReturnTypeGenerator(Operation operation, OpenAPI openAPI) {
        super(operation, openAPI);
    }

    @Override
    protected boolean populateReturnType(String statusCode, ApiResponse response, List<TypeDescriptorNode> returnTypes) {
        boolean noContentResponseFoundSuper = super.populateReturnType(statusCode, response, returnTypes);
        Schema headersTypeSchema = getHeadersTypeSchema(response);
        Schema bodyTypeSchema = getBodyTypeSchema(response);
        if (Objects.isNull(headersTypeSchema) && Objects.isNull(bodyTypeSchema)) {
            return noContentResponseFoundSuper;
        }
        returnTypes.add(TypeHandler.getInstance()
                .createStatusCodeRecord(GeneratorConstants.HTTP_CODES_DES.get(statusCode), bodyTypeSchema, headersTypeSchema, operation.getOperationId()));
        return noContentResponseFoundSuper;
    }

    private static Schema getBodyTypeSchema(ApiResponse apiResponse) {
        Content responseContent = apiResponse.getContent();
        if (Objects.isNull(responseContent)) {
            return null;
        }
        Set<Map.Entry<String, MediaType>> contentEntries = responseContent.entrySet();
        if (contentEntries.size() > 1) {
            List<Schema> schemas = contentEntries.stream()
                    .map(entry -> entry.getValue().getSchema()).distinct().toList();
            if (schemas.isEmpty()) {
                return null;
            } else if (schemas.size() == 1) {
                return schemas.get(0);
            } else {
                return new ComposedSchema().oneOf(schemas);
            }
        } else {
            Map.Entry<String, MediaType> mediaTypeEntry = contentEntries.iterator().next();
            return mediaTypeEntry.getValue().getSchema();
        }
    }

    private static Schema getHeadersTypeSchema(ApiResponse apiResponse) {
        Map<String, Header> headers = apiResponse.getHeaders();
        if (Objects.isNull(headers)) {
            return null;
        }

        Map<String, Schema> properties = new HashMap<>();
        List<String> requiredField = new ArrayList<>();
        for (Map.Entry<String, Header> headerEntry : headers.entrySet()) {
            String headerName = headerEntry.getKey();
            Header header = headerEntry.getValue();
            Schema headerTypeSchema = header.getSchema();
            properties.put(headerName, headerTypeSchema);
            if (header.getRequired()) {
                requiredField.add(headerName);
            }
        }

        if (properties.isEmpty()|| requiredField.isEmpty()) {
            return null;
        }

        ObjectSchema headersSchema = new ObjectSchema();
        headersSchema.setProperties(properties);
        headersSchema.setRequired(requiredField);
        headersSchema.setAdditionalProperties(false);
        return headersSchema;
    }
}
