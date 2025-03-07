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

package io.ballerina.openapi.extension;

/**
 * The extended service for the OpenAPIConverter endpoint.
 *
 * @since 2.0.0
 */
public class OpenAPIConverterRequest {

    private String documentFilePath;
    private boolean enableBalExtension;

    public OpenAPIConverterRequest(String documentFilePath) {
        this.documentFilePath = documentFilePath;
        this.enableBalExtension = false;
    }

    public OpenAPIConverterRequest(String documentFilePath, boolean enableBalExtension) {
        this.documentFilePath = documentFilePath;
        this.enableBalExtension = enableBalExtension;
    }

    public String getDocumentFilePath() {
        return documentFilePath;
    }

    public boolean isEnableBalExtension() {
        return enableBalExtension;
    }

    public void setDocumentFilePath(String documentFilePath) {
        this.documentFilePath = documentFilePath;
    }

    public void setEnableBalExtension(boolean enableBalExtension) {
        this.enableBalExtension = enableBalExtension;
    }
}
