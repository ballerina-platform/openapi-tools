package io.ballerina.openapi.converter.model;
/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

import java.util.Locale;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.SPLIT_PATTERN;

/**
 * This {@code OpenAPIInfo} contains details related to openAPI info section.
 */
public class OpenAPIInfo {
    private String title = null;
    private String version = null;
    private String contractPath = null;

    public OpenAPIInfo() {
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(normalizedTitle(this.title));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(this.version);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // Generate Title
    private static String normalizedTitle(String serviceName) {
        if (serviceName != null) {
            String[] splits = (serviceName.replaceFirst("/", "")).split(SPLIT_PATTERN);
            StringBuilder stringBuilder = new StringBuilder();
            String title = serviceName;
            if (splits.length > 1) {
                for (String piece : splits) {
                    if (piece.isBlank()) {
                        continue;
                    }
                    stringBuilder.append(piece.substring(0, 1).toUpperCase(Locale.ENGLISH) + piece.substring(1));
                    stringBuilder.append(" ");
                }
                title = stringBuilder.toString().trim();
            } else if (splits.length == 1 && !splits[0].isBlank()) {
                stringBuilder.append(splits[0].substring(0, 1).toUpperCase(Locale.ENGLISH) + splits[0].substring(1));
                title = stringBuilder.toString().trim();
            }
            return title;
        }
        return null;
    }
    public Optional<String> getContractPath() {
        return Optional.ofNullable(contractPath);
    }

    public void setContractPath(String contractPath) {
        this.contractPath = contractPath;
    }

}
