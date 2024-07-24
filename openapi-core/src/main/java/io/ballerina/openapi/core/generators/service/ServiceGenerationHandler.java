/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.type.GeneratorConstants.TYPE_FILE_NAME;

public class ServiceGenerationHandler {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public List<GenSrcFile> generateServiceFiles(OASServiceMetadata oasServiceMetadata, boolean singleFile) throws
            FormatterException, BallerinaOpenApiException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        ServiceDeclarationGenerator serviceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        SyntaxTree syntaxTree = serviceGenerator.generateSyntaxTree();
        if (!singleFile) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    oasServiceMetadata.getSrcFile(),
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + Formatter.format(syntaxTree).toSourceCode()));
        }
        if (oasServiceMetadata.isServiceTypeRequired()) {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata,
                    serviceGenerator.getFunctionsList());
            if (singleFile) {
                syntaxTree = serviceTypeGenerator.appendSyntaxTree(syntaxTree);
            } else {
                String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SERVICE_TYPE,
                        oasServiceMetadata.getSrcPackage(), "service_type.bal", serviceType));
            }
        }
        diagnostics.addAll(serviceGenerator.getDiagnostics());

        if (!oasServiceMetadata.generateWithoutDataBinding()) {
            if (singleFile) {
                syntaxTree = TypeHandler.getInstance().appendTypeSyntaxTree(syntaxTree);
            } else {
                String schemaSyntaxTree = Formatter.format(TypeHandler.getInstance()
                        .generateTypeSyntaxTree()).toSourceCode();
                if (!schemaSyntaxTree.isBlank()) {
                    sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                            TYPE_FILE_NAME, oasServiceMetadata.getLicenseHeader() + schemaSyntaxTree));
                }
            }
        }
        if (singleFile) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    oasServiceMetadata.getSrcFile(),
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + Formatter.format(syntaxTree).toSourceCode()));
        }
        return sourceFiles;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
