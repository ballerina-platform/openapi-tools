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

package io.ballerina.openapi.service.mapper.hateoas;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.Constants;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.hateoas.Constants.BALLERINA_LINKEDTO_KEYWORD;
import static io.ballerina.openapi.service.mapper.hateoas.Constants.OPENAPI_LINK_DEFAULT_REL;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.generateRelativePath;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getResourceConfigAnnotation;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getValueForAnnotationFields;

/**
 * This {@link HateoasMapperImpl} class represents the implementation of the {@link HateoasMapper}.
 *
 * @since 1.9.0
 */
public class HateoasMapperImpl implements HateoasMapper {

    @Override
    public void setOpenApiLinks(ServiceDeclarationNode serviceNode, OpenAPI openAPI) {
        Paths paths = openAPI.getPaths();
        Service hateoasService = extractHateoasMetaInfo(serviceNode);
        if (hateoasService.getHateoasResourceMapping().isEmpty()) {
            return;
        }
        for (Node node: serviceNode.members()) {
            if (!node.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                continue;
            }
            FunctionDefinitionNode resource = (FunctionDefinitionNode) node;
            Optional<ApiResponses> responses = getApiResponsesForResource(resource, paths);
            if (responses.isEmpty()) {
                continue;
            }
            setOpenApiLinksInApiResponse(hateoasService, resource, responses.get());
        }
    }

    private Service extractHateoasMetaInfo(ServiceDeclarationNode serviceNode) {
        Service service = new Service();
        for (Node child : serviceNode.children()) {
            if (SyntaxKind.RESOURCE_ACCESSOR_DEFINITION.equals(child.kind())) {
                FunctionDefinitionNode resourceFunction = (FunctionDefinitionNode) child;
                String resourceMethod = resourceFunction.functionName().text();
                String operationId = MapperCommonUtils.getOperationId(resourceFunction);
                Optional<String> resourceName = getResourceConfigAnnotation(resourceFunction)
                        .flatMap(resourceConfig -> getValueForAnnotationFields(resourceConfig, "name"));
                if (resourceName.isEmpty()) {
                    continue;
                }
                String cleanedResourceName = resourceName.get().replaceAll("\"", "");
                Resource hateoasResource = new Resource(resourceMethod, operationId);
                service.addResource(cleanedResourceName, hateoasResource);
            }
        }
        return service;
    }

    private Optional<ApiResponses> getApiResponsesForResource(FunctionDefinitionNode resource, Paths paths) {
        String resourcePath = MapperCommonUtils.unescapeIdentifier(generateRelativePath(resource));
        if (!paths.containsKey(resourcePath)) {
            return Optional.empty();
        }
        PathItem openApiResource = paths.get(resourcePath);
        String httpMethod = resource.functionName().toString().trim();
        Operation operation = getOperation(httpMethod, openApiResource);
        return Objects.isNull(operation) ? Optional.empty() : Optional.ofNullable(operation.getResponses());
    }

    private static Operation getOperation(String httpMethod, PathItem openApiResource) {
        return switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case Constants.GET -> openApiResource.getGet();
            case Constants.PUT -> openApiResource.getPut();
            case Constants.POST -> openApiResource.getPost();
            case Constants.DELETE -> openApiResource.getDelete();
            case Constants.OPTIONS -> openApiResource.getOptions();
            case Constants.PATCH -> openApiResource.getPatch();
            case Constants.HEAD -> openApiResource.getHead();
            default -> null;
        };
    }

    private void setOpenApiLinksInApiResponse(Service hateoasService, FunctionDefinitionNode resource,
                                              ApiResponses apiResponses) {
        Map<String, Link> swaggerLinks = mapHateoasLinksToOpenApiLinks(hateoasService, resource);
        if (swaggerLinks.isEmpty()) {
            return;
        }
        for (Map.Entry<String, ApiResponse> entry : apiResponses.entrySet()) {
            int statusCode = Integer.parseInt(entry.getKey());
            if (statusCode >= 200 && statusCode < 300) {
                entry.getValue().setLinks(swaggerLinks);
            }
        }
    }

    private List<HateoasLink> getLinks(String linkedTo) {
        List<HateoasLink> links = new ArrayList<>();
        String[] linkArray = linkedTo.replaceAll("[\\[\\]]", "").split("\\},\\s*");
        for (String linkString : linkArray) {
            HateoasLink link = parseHateoasLink(linkString);
            links.add(link);
        }
        return links;
    }

    private HateoasLink parseHateoasLink(String input) {
        HateoasLink hateoasLink = new HateoasLink();
        HashMap<String, String> keyValueMap = new HashMap<>();
        String[] keyValuePairs = input.replaceAll("[{}]", "").split(",\\s*");
        for (String pair : keyValuePairs) {
            String[] parts = pair.split(":\\s*");
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].trim();
            String value = parts[1].replaceAll("\"", "").trim();
            keyValueMap.put(key, value);
        }
        hateoasLink.setResourceName(keyValueMap.get("name"));
        hateoasLink.setRel(keyValueMap.getOrDefault("relation", OPENAPI_LINK_DEFAULT_REL));
        hateoasLink.setResourceMethod(keyValueMap.get("method"));
        return hateoasLink;
    }

    private Map<String, Link> mapHateoasLinksToOpenApiLinks(Service hateoasService,
                                                            FunctionDefinitionNode resourceFunction) {
        Optional<String> linkedTo = getResourceConfigAnnotation(resourceFunction)
                .flatMap(resourceConfig -> getValueForAnnotationFields(resourceConfig, BALLERINA_LINKEDTO_KEYWORD));
        if (linkedTo.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HateoasLink> links = getLinks(linkedTo.get());
        Map<String, Link> hateoasLinks = new HashMap<>();
        for (HateoasLink link : links) {
            Optional<Resource> resource = hateoasService.getHateoasResourceMapping().entrySet().stream()
                    .filter(resources -> link.getResourceName().equals(resources.getKey()))
                    .findFirst()
                    .flatMap(hateoasResourceMapping -> hateoasResourceMapping.getValue().stream()
                            .filter(hateoasRes -> link.getResourceMethod().equals(hateoasRes.resourceMethod()))
                            .findFirst());
            if (resource.isEmpty()) {
                continue;
            }
            Link openapiLink = new Link();
            String operationId = resource.get().operationId();
            openapiLink.setOperationId(operationId);
            hateoasLinks.put(link.getRel(), openapiLink);
        }
        return hateoasLinks;
    }
}
