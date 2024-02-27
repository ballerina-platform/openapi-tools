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
package io.ballerina.openapi.service.mapper.interceptor.resource_matcher;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

/**
 * This {@link ResourceMatcher} class represents the resource matcher. This class provides functionalities to compare
 * the HTTP resource A with the HTTP resource B to find whether the requests to resource B can be dispatched
 * to resource A.
 *
 * @since 1.9.0
 */
public final class ResourceMatcher {

    private ResourceMatcher() {
    }

    public static boolean match(ResourceMethodSymbol refResource, ResourceMethodSymbol targetResource,
                                SemanticModel semanticModel) {
        String targetResourceMethod = MapperCommonUtils.unescapeIdentifier(targetResource.getName().orElse(""));
        String refResourceMethod = MapperCommonUtils.unescapeIdentifier(refResource.getName().orElse(""));

        if (refResourceMethod.equalsIgnoreCase("default") ||
                targetResourceMethod.equalsIgnoreCase(refResourceMethod)) {
            return matchResourcePath(refResource.resourcePath(), targetResource.resourcePath(), semanticModel);
        }
        return false;
    }

    private static boolean matchResourcePath(ResourcePath refResourcePath, ResourcePath targetResourcePath,
                                             SemanticModel semanticModel) {
        PathSegments targetPathSegments = PathSegments.build(targetResourcePath);
        PathSegments refPathSegments = PathSegments.build(refResourcePath);

        return targetPathSegments.matches(refPathSegments, semanticModel);
    }
}
