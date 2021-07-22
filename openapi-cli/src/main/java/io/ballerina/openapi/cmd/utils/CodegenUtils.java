/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.cmd.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities used by ballerina openapi code generator.
 */
public class CodegenUtils {
    private static String licenseHeader = "";
    private static final String CONFIG_FILE_NAME = "Config.toml";
    /**
     * Resolves path to write generated implementation source files.
     *
     * @param pkg     module
     * @param srcPath resolved path for main source files
     * @return path to write generated source files
     */
    public static Path getImplPath(String pkg, Path srcPath) {
        return (pkg == null || pkg.isEmpty()) ? srcPath : srcPath.getParent();
    }

    /**
     * Resolves path to write generated implementation source files.
     *
     * @param licenseFilePath     path of the file which contains the content of license header
     */
    public static void setLicenseHeader(String licenseFilePath) throws IOException {
        licenseHeader = "";
        if (licenseFilePath != null && !licenseFilePath.isBlank()) {
            Path filePath = Paths.get((new File(licenseFilePath).getCanonicalPath()));
            licenseHeader = Files.readString(Paths.get(filePath.toString()));
            if (!licenseHeader.endsWith("\n")) {
                licenseHeader = licenseHeader + "\n\n";
            } else if (!licenseHeader.endsWith("\n\n")) {
                licenseHeader = licenseHeader + "\n";
            }
        }
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        if (!filePath.endsWith(CONFIG_FILE_NAME)) {
            content = licenseHeader + content;
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filePath.toString(), "UTF-8");
            writer.print(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
