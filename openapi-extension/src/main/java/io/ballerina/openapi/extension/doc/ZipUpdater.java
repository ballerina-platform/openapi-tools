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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * {@code ZipUpdater} updates a compressed file with the contents of provided directory.
 */
public class ZipUpdater extends SimpleFileVisitor<Path> {
    private final ZipOutputStream outStream;
    private final Path resources;

    public ZipUpdater(ZipOutputStream outStream, Path resources) {
        this.outStream = outStream;
        this.resources = resources;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // only copy files, no symbolic links
        if (attrs.isSymbolicLink()) {
            return FileVisitResult.CONTINUE;
        }

        try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
            // add `resources` path segment to target file path
            Path targetFile = Paths.get(Constants.RESOURCES_DIR_NAME, resources.relativize(file).toString());
            outStream.putNextEntry(new ZipEntry(targetFile.toString()));
            CodegenUtils.copyContent(inputStream, outStream);
            outStream.closeEntry();
        }

        // if current file is processed continue to next file
        return FileVisitResult.CONTINUE;
    }
}
