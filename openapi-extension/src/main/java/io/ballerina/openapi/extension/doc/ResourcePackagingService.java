/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package io.ballerina.openapi.extension.doc;

import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.extension.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipOutputStream;

/**
 * {@code ResourcePackagingService} updates generated ballerina executable jar.
 */
public class ResourcePackagingService {
    private static final String TARGET_FILE_NAME = "target_exec.jar";
    private static final PrintStream ERR = System.err;

    public void updateExecutableJar(Path targetBinPath, String srcFileName) throws IOException {
        Path srcFile = targetBinPath.resolve(srcFileName);
        Path targetFile = targetBinPath.resolve(TARGET_FILE_NAME);
        generateUpdatedJar(srcFile, targetFile, targetBinPath);
        boolean successful = deleteOldFile(srcFile);
        if (successful) {
            renameGeneratedFile(targetFile, srcFile);
        }
    }

    private void generateUpdatedJar(Path srcFile, Path targetFile, Path targetBinPath) throws IOException {
        try (JarFile srcJar = new JarFile(srcFile.toString())) {
            try (ZipOutputStream outStream = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(targetFile.toString())))) {
                // add generated open-api doc related resources to target jar
                Path resources = targetBinPath.resolve(Constants.RESOURCES_DIR_NAME);
                Files.walkFileTree(resources, new ZipUpdater(outStream, resources));

                // copy all the content in the src-jar to the new jar
                for (Enumeration<JarEntry> entries = srcJar.entries(); entries.hasMoreElements();) {
                    JarEntry element = entries.nextElement();
                    outStream.putNextEntry(element);
                    try (InputStream inputStream = srcJar.getInputStream(element)) {
                        CodegenUtils.copyContent(inputStream, outStream);
                        outStream.closeEntry();
                    }
                }
            }
        }
    }

    private boolean deleteOldFile(Path sourceFilePath) {
        // delete old jar file
        File sourceFile = sourceFilePath.toFile();
        if (sourceFile.exists()) {
            return sourceFile.delete();
        }
        return true;
    }

    private void renameGeneratedFile(Path targetFilePath, Path sourceFilePath) {
        File targetFile = targetFilePath.toFile();
        boolean success = targetFile.renameTo(sourceFilePath.toFile());
        if (!success) {
            ERR.println("error [open-api extension]: could not rename the generated executable jar");
        }
    }
}
