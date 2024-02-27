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

import java.util.Objects;

/**
 * This {@link NamedPathSegmentNode} represents the named path segment in the resource path.
 *
 * @since 1.9.0
 */
public class NamedPathSegmentNode extends PathSegmentNode {

    String name;

    public NamedPathSegmentNode(String name) {
        this.name = Objects.isNull(name) ? "" : name;
    }

    @Override
    public Type getType() {
        return Type.NAMED_SEGMENT;
    }

    @Override
    public boolean matches(PathSegmentNode refPathSegmentNode, SemanticModel semanticModel) {
        return switch (refPathSegmentNode.getType()) {
            case DOT_SEGMENT -> false;
            case NAMED_SEGMENT -> ((NamedPathSegmentNode) refPathSegmentNode).getName().equals(name);
            case PARAMETER_SEGMENT -> name.matches(getRegex(
                    ((PathParameterSegmentNode) refPathSegmentNode).getParameterType(), semanticModel));
        };
    }

    public String getName() {
        return name;
    }
}
