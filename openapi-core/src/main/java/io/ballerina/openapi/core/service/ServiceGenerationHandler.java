package io.ballerina.openapi.core.service;

import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

public class ServiceGenerationHandler {

    private final List<ServiceDiagnostic> diagnostics = new ArrayList<>();

    public List<GenSrcFile> generateServiceFiles(OASServiceMetadata oasServiceMetadata) throws
            FormatterException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String mainContent;
        if (oasServiceMetadata.isGenerateOnlyServiceType()) {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata);
            serviceTypeGenerator.generateSyntaxTree();
            String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
            diagnostics.addAll(serviceTypeGenerator.getDiagnostics());
        } else {
            ServiceTypeGenerator serviceTypeGenerator = new ServiceTypeGenerator(oasServiceMetadata);
            String serviceType = Formatter.format(serviceTypeGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
            ServiceDeclarationGenerator serviceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
            mainContent = Formatter.format(serviceGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    oasServiceMetadata.getSrcFile(),
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + mainContent));
            diagnostics.addAll(serviceTypeGenerator.getDiagnostics());
            diagnostics.addAll(serviceGenerator.getDiagnostics());
        }
        return sourceFiles;
    }

    public List<ServiceDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
