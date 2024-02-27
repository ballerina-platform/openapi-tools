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
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;

import java.util.Optional;

/**
 * This {@link PathSegmentNode} represents a segment in the resource path as a node.
 *
 * @since 1.9.0
 */
public abstract class PathSegmentNode {

    public enum Type {
        DOT_SEGMENT, NAMED_SEGMENT, PARAMETER_SEGMENT
    }

    private PathSegmentNode next;

    public abstract Type getType();

    public abstract boolean matches(PathSegmentNode refPathSegmentNode, SemanticModel semanticModel);

    public PathSegmentNode next() {
        return next;
    }

    public void setNext(PathSegmentNode next) {
        this.next = next;
    }

    protected static String getRegex(TypeSymbol pathParamType, SemanticModel semanticModel) {
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
                    .map(typeSymbol -> {
                        String signature = typeSymbol.signature();
                        if (((SingletonTypeSymbol) typeSymbol).originalType().typeKind().equals(TypeDescKind.STRING)) {
                            signature = signature.substring(1, signature.length() - 1);
                        }
                        return signature;
                    })
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
}
