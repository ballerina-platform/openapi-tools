/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.core.ErrorMessages;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.ballerina.openapi.core.GeneratorConstants.LINE_SEPARATOR;

/**
 * Contains all the util functions used for openapi commands.
 *
 * @since 2.0.0
 */
public class CmdUtils {

    /**
     * This util method is used to generate {@code Diagnostic} for openapi command errors.
     */
    public static OpenAPIDiagnostic constructOpenAPIDiagnostic(String code, String message, DiagnosticSeverity severity,
                                                               Location location, Object... args) {

        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, message, severity);
        if (location == null) {
            location = new MapperCommonUtils.NullLocation();
        }
        return new OpenAPIDiagnostic(diagnosticInfo, location, Collections.emptyList(), args);
    }

    /**
     * Util for take OpenApi spec from given yaml file.
     */
    public static OpenAPI getOpenAPIFromOpenAPIV3Parser(Path definitionPath) throws
            IOException, BallerinaOpenApiException {

        Path contractPath = java.nio.file.Paths.get(definitionPath.toString());
        if (!Files.exists(contractPath)) {
            throw new BallerinaOpenApiException(ErrorMessages.invalidFilePath(definitionPath.toString()));
        }
        if (!(definitionPath.toString().endsWith(".yaml") || definitionPath.toString().endsWith(".json") ||
                definitionPath.toString().endsWith(".yml"))) {
            throw new BallerinaOpenApiException(ErrorMessages.invalidFileType());
        }
        String openAPIFileContent = Files.readString(definitionPath);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setFlatten(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent, null, parseOptions);
        if (!parseResult.getMessages().isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: \n");
            for (String message : parseResult.getMessages()) {
                errorMessage.append(message).append(LINE_SEPARATOR);
            }
            throw new BallerinaOpenApiException(errorMessage.toString());
        }
        return parseResult.getOpenAPI();
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles      generated files
     * @param gFile          GenSrcFile object
     * @param duplicateCount add the tag with duplicate number if file already exist
     */
    public static void setGeneratedFileName(List<File> listFiles, GenSrcFile gFile, int duplicateCount) {

        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2) &&
                    (listFileName.split("\\.")[0].equals(gFile.getFileName().split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        gFile.setFileName(gFile.getFileName().split("\\.")[0] + "." + (duplicateCount) + "." +
                gFile.getFileName().split("\\.")[1]);
    }
}
