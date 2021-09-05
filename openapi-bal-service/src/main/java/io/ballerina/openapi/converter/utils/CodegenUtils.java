/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.openapi.converter.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Utilities used by ballerina openapi code generator.
 */
public final class CodegenUtils {
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
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    /**
     * Copy content of a file/directory into another location.
     *
     * @param inputStream stream from which the data is read
     * @param outStream stream to which the data is written
     * @throws IOException if there is any error while reading from a file or writing to a file
     */
    public static <T extends InputStream, E extends OutputStream> void copyContent(T inputStream, E outStream)
            throws IOException {
        byte[] data = new byte[1024];
        int bytesRead = inputStream.read(data);
        while (bytesRead != -1) {
            outStream.write(data, 0, bytesRead);
            bytesRead = inputStream.read(data);
        }
    }

    // Cache config utils


}
