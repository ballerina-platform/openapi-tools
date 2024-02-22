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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

import java.util.List;
import java.util.Objects;

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

    public enum PathParamType {
        STRING, INT, FLOAT, BOOLEAN, DECIMAL
    }

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
                // TODO: Need to handle enum, union of singletons and literal types
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
        if (semanticModel.types().INT.subtypeOf(restPathParamType)) {
            return matchType(PathParamType.INT, targetResourcePath, semanticModel);
        } else if (semanticModel.types().FLOAT.subtypeOf(restPathParamType)) {
            return matchType(PathParamType.FLOAT, targetResourcePath, semanticModel);
        } else if (semanticModel.types().BOOLEAN.subtypeOf(restPathParamType)) {
            return matchType(PathParamType.BOOLEAN, targetResourcePath, semanticModel);
        } else if (semanticModel.types().DECIMAL.subtypeOf(restPathParamType)) {
            return matchType(PathParamType.DECIMAL, targetResourcePath, semanticModel);
        } else { // STRING
            return true;
        }
    }

    private static boolean matchType(PathParamType pathParamType, ResourcePath targetResourcePath,
                                     SemanticModel semanticModel) {
        switch (targetResourcePath.kind()) {
            case PATH_SEGMENT_LIST -> {
                List<PathSegment> pathSegments = ((PathSegmentList) targetResourcePath).list();
                return matchPathSegListWithType(pathParamType, pathSegments, semanticModel);
            }
            case PATH_REST_PARAM -> {
                PathParameterSymbol pathRestParam = ((PathRestParam) targetResourcePath).parameter();
                TypeSymbol targetRestPathParamType = pathRestParam.typeDescriptor();
                return targetRestPathParamType.subtypeOf(getTypeSymbol(pathParamType, semanticModel));
            }
            default -> { // DOT_RESOURCE_PATH
                return false;
            }
        }
    }

    private static TypeSymbol getTypeSymbol(PathParamType pathParamType, SemanticModel semanticModel) {
        return switch (pathParamType) {
            case STRING -> semanticModel.types().STRING;
            case INT -> semanticModel.types().INT;
            case FLOAT -> semanticModel.types().FLOAT;
            case BOOLEAN -> semanticModel.types().BOOLEAN;
            case DECIMAL -> semanticModel.types().DECIMAL;
        };
    }

    private static boolean matchPathSegListWithType(PathParamType pathParamType, List<PathSegment> pathSegments,
                                                    SemanticModel semanticModel) {
        for (PathSegment pathSegment : pathSegments) {
            switch (pathSegment.pathSegmentKind()) {
                case NAMED_SEGMENT -> {
                    if (matchNamedSegWithType(pathParamType, pathSegment)) {
                        return false;
                    }
                }
                case PATH_REST_PARAMETER -> {
                    TypeSymbol targetRestPathParamType = ((PathRestParam) pathSegment).
                            parameter().typeDescriptor();
                    if (!targetRestPathParamType.subtypeOf(getTypeSymbol(pathParamType, semanticModel))) {
                        return false;
                    }
                }
                case PATH_PARAMETER -> {
                    TypeSymbol targetPathParamType = ((PathParameterSymbol) pathSegment).typeDescriptor();
                    if (!targetPathParamType.subtypeOf(getTypeSymbol(pathParamType, semanticModel))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean matchNamedSegWithType(PathParamType pathParamType, PathSegment pathSegment) {
        return Objects.nonNull(pathSegment.signature()) &&
                pathSegment.signature().matches(getRegex(pathParamType));
    }

    private static String getRegex(PathParamType pathParamType) {
        return switch (pathParamType) {
            case STRING -> "[^/]+";
            case INT -> "\\d+";
            case FLOAT, DECIMAL -> "\\d+\\.\\d+";
            case BOOLEAN -> "true|false";
        };
    }

    private static boolean matchPathSegList(List<PathSegment> pathSegments, ResourcePath targetResourcePath,
                                            SemanticModel semanticModel) {
        switch (targetResourcePath.kind()) {
            case DOT_RESOURCE_PATH -> {
                return false;
            }
            case PATH_REST_PARAM -> {
                TypeSymbol restPathParamType = ((PathRestParam) targetResourcePath).parameter().typeDescriptor();
                return matchPathSegListWithType(getPathParamType(restPathParamType, semanticModel), pathSegments,
                        semanticModel);
            }
            default -> { // PATH_SEGMENT_LIST
                List<PathSegment> targetPathSegments = ((PathSegmentList) targetResourcePath).list();
                return matchPathSegLists(pathSegments, targetPathSegments, semanticModel);
            }
        }
    }

    private static PathParamType getPathParamType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        if (semanticModel.types().INT.subtypeOf(typeSymbol)) {
            return PathParamType.INT;
        } else if (semanticModel.types().FLOAT.subtypeOf(typeSymbol)) {
            return PathParamType.FLOAT;
        } else if (semanticModel.types().BOOLEAN.subtypeOf(typeSymbol)) {
            return PathParamType.BOOLEAN;
        } else if (semanticModel.types().DECIMAL.subtypeOf(typeSymbol)) {
            return PathParamType.DECIMAL;
        } else { // STRING
            return PathParamType.STRING;
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
                return matchNamedSegWithType(getPathParamType(pathParamType, semanticModel), targetSeg);
            } else { // PATH_REST_PARAMETER
                TypeSymbol restPathParamType = ((PathParameterSymbol) pathSeg).typeDescriptor();
                return matchNamedSegWithType(getPathParamType(restPathParamType, semanticModel), targetSeg);
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
