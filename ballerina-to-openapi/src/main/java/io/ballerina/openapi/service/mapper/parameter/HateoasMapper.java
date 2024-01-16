/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.service.mapper.hateoas.Resource;
import io.ballerina.openapi.service.mapper.response.model.HateoasLink;
import io.swagger.v3.oas.models.links.Link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_LINK_DEFAULT_REL;
import static io.ballerina.openapi.service.mapper.hateoas.HateoasContextHolder.getHateoasContextHolder;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getResourceConfigAnnotation;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getValueForAnnotationFields;

public class HateoasMapper {

    public Map<String, Link> mapHateoasLinksToSwaggerLinks(int serviceId,
                                                           FunctionDefinitionNode resourceFunction) {
        Optional<String> linkedTo = getResourceConfigAnnotation(resourceFunction)
                .flatMap(resourceConfig -> getValueForAnnotationFields(resourceConfig, "linkedTo"));
        if (linkedTo.isEmpty()) {
            return Collections.emptyMap();
        }
        List<HateoasLink> links = getLinks(linkedTo.get());
        Map<String, Link> hateoasLinks = new HashMap<>();
        for (HateoasLink link : links) {
            Optional<Resource> resource = getHateoasContextHolder()
                    .getHateoasResource(serviceId, link.getResourceName(), link.getResourceMethod());
            if (resource.isPresent()) {
                Link swaggerLink = new Link();
                String operationId = resource.get().operationId();
                swaggerLink.setOperationId(operationId);
                hateoasLinks.put(link.getRel(), swaggerLink);
            }
        }
        return hateoasLinks;
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

    public static HateoasLink parseHateoasLink(String input) {
        HateoasLink hateoasLink = new HateoasLink();
        HashMap<String, String> keyValueMap = new HashMap<>();
        String[] keyValuePairs = input.replaceAll("[{}]", "").split(",\\s*");
        for (String pair : keyValuePairs) {
            String[] parts = pair.split(":\\s*");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].replaceAll("\"", "").trim();
                keyValueMap.put(key, value);
            }
        }
        hateoasLink.setResourceName(keyValueMap.get("name"));
        if (keyValueMap.get("relation") == null) {
            hateoasLink.setRel(OPENAPI_LINK_DEFAULT_REL);
        } else {
            hateoasLink.setRel(keyValueMap.get("relation"));
        }
        hateoasLink.setResourceMethod(keyValueMap.get("method"));
        return hateoasLink;
    }
}
