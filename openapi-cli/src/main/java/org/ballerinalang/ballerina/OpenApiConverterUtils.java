package org.ballerinalang.ballerina;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

public class OpenApiConverterUtils {

    /**
     * This util for generating files when not available with specific service name.
     * @param servicePath
     * @param outPath
     * @throws IOException
     * @throws OpenApiConverterException
     */
    public static void generateOAS3DefinitionsAllService(Path servicePath, Path outPath)
            throws IOException, OpenApiConverterException {

        // Load project instance for single ballerina file
        Project project = SingleFileProject.load(servicePath);
        //travers and filter service

        // Take package name for project
        Package packageName = project.currentPackage();
        // Take module instance for traversing the sysntax tree
        Module currentModule = packageName.getDefaultModule();
        Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
        while (documentIterator.hasNext()) {

        }






    }


}
