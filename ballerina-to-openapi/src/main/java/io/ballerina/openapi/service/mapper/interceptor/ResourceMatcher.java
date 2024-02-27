/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.resourcepath.ResourcePath.Kind.DOT_RESOURCE_PATH;
import static io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment.Kind.NAMED_SEGMENT;
import static io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment.Kind.PATH_PARAMETER;
import static io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment.Kind.PATH_REST_PARAMETER;

/**
 * This {@link ResourceMatcher} class represents the resource matcher.
 * This class provides functionalities to compare the HTTP resource A with the HTTP resource B to find whether the
 * requests to resource B can be dispatched to resource A.
 *
 * @since 1.9.0
 */
public final class ResourceMatcher {

    private ResourceMatcher() {
    }

    public static boolean match(ResourceMethodSymbol resource, ResourceMethodSymbol targetResource,
                                SemanticModel semanticModel) {
        String resourceBMethod = MapperCommonUtils.unescapeIdentifier(targetResource.getName().orElse(""));
        String resourceAMethod = MapperCommonUtils.unescapeIdentifier(resource.getName().orElse(""));

        if (resourceAMethod.equalsIgnoreCase("default") ||
                resourceBMethod.equalsIgnoreCase(resourceAMethod)) {
            return matchResourcePath(resource.resourcePath(), targetResource.resourcePath(), semanticModel);
        }
        return false;
    }

    private static boolean matchResourcePath(ResourcePath resourcePath, ResourcePath targetResourcePath,
                                             SemanticModel semanticModel) {
        switch (resourcePath.kind()) {
            case DOT_RESOURCE_PATH -> {
                return resourcePath.kind().equals(DOT_RESOURCE_PATH);
            }
            case PATH_REST_PARAM -> {
                TypeSymbol restPathParamType = ((PathRestParam) resourcePath).parameter().typeDescriptor();
                return matchRestPath(restPathParamType, targetResourcePath, semanticModel);
            }
            default -> { // PATH_SEGMENT_LIST
                List<PathSegment> pathSegments = ((PathSegmentList) resourcePath).list();
                return matchPathSegList(pathSegments, targetResourcePath, semanticModel);
            }
        }
    }

    private static boolean matchRestPath(TypeSymbol restPathParamType, ResourcePath targetResourcePath,
                                         SemanticModel semanticModel) {
        if (semanticModel.types().STRING.subtypeOf(restPathParamType)) {
            return true;
        }
        return matchType(restPathParamType, targetResourcePath, semanticModel);
    }

    private static boolean matchType(TypeSymbol pathParamType, ResourcePath targetResourcePath,
                                     SemanticModel semanticModel) {
        switch (targetResourcePath.kind()) {
            case PATH_SEGMENT_LIST -> {
                List<PathSegment> pathSegments = ((PathSegmentList) targetResourcePath).list();
                return matchPathSegListWithType(pathParamType, pathSegments, semanticModel);
            }
            case PATH_REST_PARAM -> {
                PathParameterSymbol pathRestParam = ((PathRestParam) targetResourcePath).parameter();
                TypeSymbol targetRestPathParamType = pathRestParam.typeDescriptor();
                return targetRestPathParamType.subtypeOf(pathParamType);
            }
            default -> { // DOT_RESOURCE_PATH
                return false;
            }
        }
    }

    private static boolean matchPathSegListWithType(TypeSymbol pathParamType, List<PathSegment> pathSegments,
                                                    SemanticModel semanticModel) {
        for (PathSegment pathSegment : pathSegments) {
            switch (pathSegment.pathSegmentKind()) {
                case NAMED_SEGMENT -> {
                    if (matchNamedSegWithType(pathParamType, pathSegment, semanticModel)) {
                        return false;
                    }
                }
                case PATH_REST_PARAMETER -> {
                    TypeSymbol targetRestPathParamType = ((PathRestParam) pathSegment).
                            parameter().typeDescriptor();
                    if (!targetRestPathParamType.subtypeOf(pathParamType)) {
                        return false;
                    }
                }
                case PATH_PARAMETER -> {
                    TypeSymbol targetPathParamType = ((PathParameterSymbol) pathSegment).typeDescriptor();
                    if (!targetPathParamType.subtypeOf(pathParamType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean matchNamedSegWithType(TypeSymbol pathParamType, PathSegment pathSegment,
                                                 SemanticModel semanticModel) {
        return Objects.nonNull(pathSegment.signature()) &&
                pathSegment.signature().matches(getRegex(pathParamType, semanticModel));
    }

    private static String getRegex(TypeSymbol pathParamType, SemanticModel semanticModel) {
        if (semanticModel.types().INT.subtypeOf(pathParamType)) {
            return "\\d+";
        } else if (semanticModel.types().FLOAT.subtypeOf(pathParamType)) {
            return "\\d+\\.\\d+";
        } else if (semanticModel.types().BOOLEAN.subtypeOf(pathParamType)) {
            return "true|false";
        } else if (semanticModel.types().DECIMAL.subtypeOf(pathParamType)) {
            return "\\d+\\.\\d+";
        }
        Optional<UnionTypeSymbol> unionType = getUnionType(pathParamType);
        if (unionType.isPresent() && unionType.get().memberTypeDescriptors().stream().allMatch(
                typeSymbol -> typeSymbol instanceof SingletonTypeSymbol)) {
            return unionType.get().memberTypeDescriptors().stream()
                    .map(TypeSymbol::signature)
                    .reduce((s, s2) -> s + "|" + s2).orElse("[^/]+");
        }
        return "[^/]+";
    }

    private static Optional<UnionTypeSymbol> getUnionType(TypeSymbol pathParamType) {
        if (pathParamType instanceof TypeReferenceTypeSymbol typeReferenceTypeSymbol) {
            TypeSymbol referredType = typeReferenceTypeSymbol.typeDescriptor();
            if (referredType instanceof UnionTypeSymbol unionTypeSymbol) {
                return Optional.of(unionTypeSymbol);
            }
            return getUnionType(referredType);
        } else if (pathParamType instanceof UnionTypeSymbol unionTypeSymbol) {
            return Optional.of(unionTypeSymbol);
        }
        return Optional.empty();
    }

    private static boolean matchPathSegList(List<PathSegment> pathSegments, ResourcePath targetResourcePath,
                                            SemanticModel semanticModel) {
        switch (targetResourcePath.kind()) {
            case DOT_RESOURCE_PATH -> {
                return false;
            }
            case PATH_REST_PARAM -> {
                TypeSymbol restPathParamType = ((PathRestParam) targetResourcePath).parameter().typeDescriptor();
                return matchPathSegListWithType(restPathParamType, pathSegments, semanticModel);
            }
            default -> { // PATH_SEGMENT_LIST
                List<PathSegment> targetPathSegments = ((PathSegmentList) targetResourcePath).list();
                return matchPathSegLists(pathSegments, targetPathSegments, semanticModel);
            }
        }
    }

    private static boolean matchPathSegLists(List<PathSegment> pathSegments, List<PathSegment> targetPathSegments,
                                             SemanticModel semanticModel) {
        int pathSize = pathSegments.size();
        int targetSize = targetPathSegments.size();

        if (pathSize > targetSize) {
            return false;
        }

        for (int i = 0; i < targetSize; i++) {
            PathSegment  targetSeg = targetPathSegments.get(i);
            PathSegment pathSeg;

            if (i < pathSize) {
                pathSeg = pathSegments.get(i);
            } else {
                pathSeg = pathSegments.get(pathSize - 1);
                if (pathSeg.pathSegmentKind() != PATH_REST_PARAMETER) {
                    return false;
                }
            }

            if (!matchPathSegments(pathSeg, targetSeg, semanticModel)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchPathSegments(PathSegment pathSeg, PathSegment targetSeg, SemanticModel semanticModel) {
        if (targetSeg.pathSegmentKind().equals(NAMED_SEGMENT)) {
            if (pathSeg.pathSegmentKind().equals(NAMED_SEGMENT)) {
                return targetSeg.signature().equals(pathSeg.signature());
            } else if (pathSeg.pathSegmentKind().equals(PATH_PARAMETER)) {
                TypeSymbol pathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return matchNamedSegWithType(pathParamType, targetSeg, semanticModel);
            } else { // PATH_REST_PARAMETER
                TypeSymbol restPathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return matchNamedSegWithType(restPathParamType, targetSeg, semanticModel);
            }
        } else if (targetSeg.pathSegmentKind().equals(PATH_PARAMETER)) {
            if (pathSeg.pathSegmentKind().equals(NAMED_SEGMENT)) {
                return false;
            } else if (pathSeg.pathSegmentKind().equals(PATH_PARAMETER)) {
                TypeSymbol targetPathParamType = ((PathParameterSymbol) targetSeg).typeDescriptor();
                TypeSymbol pathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return semanticModel.types().STRING.subtypeOf(pathParamType)
                        || targetPathParamType.subtypeOf(pathParamType);
            } else if (pathSeg.pathSegmentKind().equals(PATH_REST_PARAMETER)) {
                TypeSymbol targetRestPathParamType = ((PathParameterSymbol) targetSeg).typeDescriptor();
                TypeSymbol pathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return semanticModel.types().STRING.subtypeOf(pathParamType)
                        || targetRestPathParamType.subtypeOf(pathParamType);
            }
        } else if (targetSeg.pathSegmentKind().equals(PATH_REST_PARAMETER)) {
            if (pathSeg.pathSegmentKind().equals(NAMED_SEGMENT) || pathSeg.pathSegmentKind().equals(PATH_PARAMETER)) {
                return false;
            } else if (pathSeg.pathSegmentKind().equals(PATH_REST_PARAMETER)) {
                TypeSymbol targetRestPathParamType = ((PathParameterSymbol) targetSeg).typeDescriptor();
                TypeSymbol pathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return semanticModel.types().STRING.subtypeOf(pathParamType)
                        || targetRestPathParamType.subtypeOf(pathParamType);
            }
        }
        return false;
    }
}
