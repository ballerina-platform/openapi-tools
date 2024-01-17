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

package io.ballerina.openapi.service.mapper.hateoas;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service {
    private final String packageId;
    private final int serviceId;
    private final Map<String, List<Resource>> hateoasResourceMapping = new HashMap<>();

    public Service(String packageId, int serviceId) {
        this.packageId = packageId;
        this.serviceId = serviceId;
    }

    public void addResource(String resourceName, Resource resource) {
        if (hateoasResourceMapping.containsKey(resourceName)) {
            hateoasResourceMapping.get(resourceName).add(resource);
            return;
        }
        hateoasResourceMapping.put(resourceName, Arrays.asList(resource));
    }

    public String getPackageId() {
        return packageId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public Map<String, List<Resource>> getHateoasResourceMapping() {
        return hateoasResourceMapping;
    }
}
