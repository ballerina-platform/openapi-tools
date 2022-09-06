/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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

package io.ballerina.openapi.core.model;

/**
 * Model class to hold generated source file information.
 *
 * @since 1.3.0
 */
public class GenSrcFile {
    private String content;
    private String fileName;
    private String pkgName;
    private GenFileType type;

    /**
     * Type specifier for generated source files.
     */
    public enum GenFileType {
        GEN_SRC,
        MODEL_SRC,
        IMPL_SRC,
        TEST_SRC,
        RES,
        UTIL_SRC;

        public boolean isOverwritable() {
            if (this == GEN_SRC || this == RES || this == MODEL_SRC) {
                return true;
            }

            return false;
        }
    }

    public GenSrcFile(GenFileType type, String pkgName, String fileName, String content) {
        this.type = type;
        this.fileName = fileName;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public GenFileType getType() {
        return type;
    }

    public void setType(GenFileType type) {
        this.type = type;
    }
}
