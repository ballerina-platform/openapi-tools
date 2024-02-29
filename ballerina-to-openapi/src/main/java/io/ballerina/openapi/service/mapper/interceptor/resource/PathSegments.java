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
package io.ballerina.openapi.service.mapper.interceptor.resource;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;

import java.util.List;

/**
 * This {@link PathSegments} represents the path segments in the resource path as a linked list.
 *
 * @since 1.9.0
 */
public class PathSegments {

    private PathSegmentNode firstSegment;
    private PathSegmentNode lastSegment;

    private PathSegments(PathSegmentNode firstSegment) {
        this.firstSegment = firstSegment;
        this.lastSegment = firstSegment;
    }

    private PathSegments(PathSegmentNode firstSegment, PathSegmentNode lastSegment) {
        this.firstSegment = firstSegment;
        this.lastSegment = lastSegment;
    }

    public static PathSegments build(ResourcePath resourcePath) {
        switch (resourcePath.kind()) {
            case DOT_RESOURCE_PATH -> {
                return new PathSegments(new DotPathSegmentNode());
            }
            case PATH_REST_PARAM -> {
                TypeSymbol restPathParamType = ((PathRestParam) resourcePath).parameter().typeDescriptor();
                return new PathSegments(new PathRestParameterSegmentNode(restPathParamType));
            }
            default -> { //PATH_SEGMENT_LIST
                List<PathSegment> pathSegments = ((PathSegmentList) resourcePath).list();
                return build(pathSegments);
            }
        }
    }

    private static PathSegments build(List<PathSegment> pathSegments) {
        if (pathSegments.isEmpty()) {
            return new PathSegments(null);
        }
        PathSegmentNode firstSegment = buildPathSegmentNode(pathSegments.get(0));
        PathSegmentNode currentSegment = firstSegment;
        for (int i = 1; i < pathSegments.size(); i++) {
            PathSegmentNode nextSegment = buildPathSegmentNode(pathSegments.get(i));
            currentSegment.setNext(nextSegment);
            currentSegment = nextSegment;
        }
        return new PathSegments(firstSegment, currentSegment);
    }

    private static PathSegmentNode buildPathSegmentNode(PathSegment pathSegment) {
        switch (pathSegment.pathSegmentKind()) {
            case NAMED_SEGMENT -> {
                return new NamedPathSegmentNode(pathSegment.signature());
            }
            case PATH_REST_PARAMETER -> {
                TypeSymbol restPathParamType = ((PathParameterSymbol) pathSegment).typeDescriptor();
                return new PathRestParameterSegmentNode(restPathParamType);
            }
            default -> { //PATH_PARAMETER
                TypeSymbol parameterType = ((PathParameterSymbol) pathSegment).typeDescriptor();
                return new PathParameterSegmentNode(parameterType);
            }
        }
    }

    public boolean matches(PathSegments refPathSegments, SemanticModel semanticModel) {
        PathSegmentNode targetPathSegNode = firstSegment;
        PathSegmentNode refPathSegNode = refPathSegments.firstSegment;
        while (refPathSegNode != null && targetPathSegNode != null) {
            if (!targetPathSegNode.matches(refPathSegNode, semanticModel)) {
                return false;
            }
            // This condition will not be covered since we are not generating operation for
            // target resources with the rest path parameter.
            if (refPathSegNode instanceof PathRestParameterSegmentNode
                    && targetPathSegNode instanceof PathRestParameterSegmentNode) {
                return true;
            }
            refPathSegNode = refPathSegNode.next();
            targetPathSegNode = targetPathSegNode.next();
        }
        return (refPathSegNode == null && targetPathSegNode == null) ||
                (targetPathSegNode == null && refPathSegments.lastSegment instanceof PathRestParameterSegmentNode);
    }
}
