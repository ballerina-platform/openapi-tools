/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.constraint.model;

import java.util.Optional;

/**
 * This {@link ConstraintAnnotation} class represents the constraint annotations.
 *
 * @since 1.9.0
 */
public class ConstraintAnnotation {
    private final String minValue;
    private final String maxValue;
    private final String length;
    private final String minLength;
    private final String maxLength;
    private final String minValueExclusive;
    private final String maxValueExclusive;
    private final String pattern;

    public ConstraintAnnotation(ConstraintAnnotationBuilder builder) {
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.length = builder.length;
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.minValueExclusive = builder.minValueExclusive;
        this.maxValueExclusive = builder.maxValueExclusive;
        this.pattern = builder.pattern;
    }

    public boolean hasConstraints() {
        return minValue != null || maxValue != null || length != null || minLength != null || maxLength != null
                || minValueExclusive != null || maxValueExclusive != null || pattern != null;
    }

    public Optional<String> getMinValue() {
        return Optional.ofNullable(minValue);
    }

    public Optional<String> getMaxValue() {
        return Optional.ofNullable(maxValue);
    }

    public Optional<String> getLength() {
        return Optional.ofNullable(length);
    }

    public Optional<String> getMinLength() {
        return Optional.ofNullable(minLength);
    }

    public Optional<String> getMaxLength() {
        return Optional.ofNullable(maxLength);
    }

    public Optional<String> getMinValueExclusive() {
        return Optional.ofNullable(minValueExclusive);
    }

    public Optional<String> getMaxValueExclusive() {
        return Optional.ofNullable(maxValueExclusive);
    }

    public Optional<String> getPattern() {
        return Optional.ofNullable(pattern);
    }

    /**
     * This is the builder class for the {@link ConstraintAnnotation}.
     */
    public static class ConstraintAnnotationBuilder {
        private String minValue;
        private String maxValue;
        private String length;
        private String minLength;
        private String maxLength;
        private String minValueExclusive;
        private String maxValueExclusive;
        private String pattern;

        public void withMinValue(String minValue) {
            this.minValue = minValue;
        }

        public void withLength(String length) {
            this.length = length;
        }

        public void withMaxValue(String maxValue) {
            this.maxValue = maxValue;
        }

        public void withMinLength(String minLength) {
            this.minLength = minLength;
        }

        public void withMaxLength(String maxLength) {
            this.maxLength = maxLength;
        }

        public void withMinValueExclusive(String minValueExclusive) {
            this.minValueExclusive = minValueExclusive;
        }

        public void withMaxValueExclusive(String maxValueExclusive) {
            this.maxValueExclusive = maxValueExclusive;
        }

        public void withPattern(String pattern) {
            this.pattern = pattern;
        }

        public ConstraintAnnotation build() {
            return new ConstraintAnnotation(this);
        }
    }
}
