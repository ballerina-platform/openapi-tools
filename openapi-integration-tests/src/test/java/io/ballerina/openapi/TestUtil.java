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

package io.ballerina.openapi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.ballerina.openapi.idl.client.IDLClientGenPluginTests.DISTRIBUTION_FILE_NAME;
import static io.ballerina.openapi.idl.client.IDLClientGenPluginTests.TEST_RESOURCE;

/**
 * This class for storing the test utils for integration tests.
 */
public class TestUtil {
    public static final PrintStream OUT = System.out;
    public static final Path TARGET_DIR = Paths.get(System.getProperty("target.dir"));
    public static final Path DISTRIBUTIONS_DIR = Paths.get(System.getProperty("distributions.dir"));
    public static final Path TEST_DISTRIBUTION_PATH = TARGET_DIR.resolve("test-distribution");
    public static final Path RESOURCES_PATH = TARGET_DIR.resolve("resources/test");
    public static final Path RESOURCE = Paths.get(System.getProperty("user.dir")).resolve("build/resources/test");
    private static String balFile = "bal";

    /**
     * Log the output of an input stream.
     *
     * @param inputStream The stream.
     * @throws IOException Error reading the stream.
     */
    private static void logOutput(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.lines().forEach(OUT::println);
        }
    }

    /**
     * Execute ballerina build command with openAPI annotation.
     *
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return inputStream with log outputs
     * @throws IOException          Error executing build command.
     * @throws InterruptedException Interrupted error executing build command.
     */
    public static InputStream executeOpenapiBuild(String distributionName, Path sourceDirectory,
                                                  List<String> args) throws IOException {
        args.add(0, "build");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        return process.getErrorStream();
    }

    /**
     * Ballerina build command.
     */
    public static boolean executeBuild(String distributionName, Path sourceDirectory,
                                              List<String> args) throws IOException, InterruptedException {
        args.add(0, "build");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode == 0;
    }

    /**
     * Ballerina run command.
     */
    public static Process executeRun(String distributionName, Path sourceDirectory,
                                       List<String> args) throws IOException, InterruptedException {
        args.add(0, "run");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        int exitCode = process.waitFor();
//        logOutput(process.getInputStream());
//        logOutput(process.getErrorStream());
//        return exitCode == 0;
        return process;
    }
    /**
     * Execute ballerina openapi command.
     *
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return True if build is successful, else false.
     * @throws IOException          Error executing build command.
     * @throws InterruptedException Interrupted error executing build command.
     */
    public static boolean executeOpenAPI(String distributionName, Path sourceDirectory, List<String> args) throws
            IOException, InterruptedException {
        args.add(0, "openapi");
        Process process = getProcessBuilderResults(distributionName, sourceDirectory, args);
        int exitCode = process.waitFor();
        logOutput(process.getInputStream());
        logOutput(process.getErrorStream());
        return exitCode == 0;
    }

    /**
     *  Get Process from given arguments.
     * @param distributionName The name of the distribution.
     * @param sourceDirectory  The directory where the sources files are location.
     * @param args             The arguments to be passed to the build command.
     * @return process
     * @throws IOException          Error executing build command.
     */
    public static Process getProcessBuilderResults(String distributionName, Path sourceDirectory, List<String> args)
            throws IOException {

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        args.add(0, TEST_DISTRIBUTION_PATH.resolve(distributionName).resolve("bin").resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(args, ' '));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(sourceDirectory.toFile());
        return pb.start();
    }

    /**
     * Delete the temporary directory used to extract distributions.
     *
     * @throws IOException If temporary directory does not exist.
     */
    public static void cleanDistribution() throws IOException {
        FileUtils.deleteDirectory(TEST_DISTRIBUTION_PATH.toFile());
    }

    /**
     * Delete openapi generated files.
     *
     * @param generatedFileName file name that need to delete.
     */
    public static void deleteGeneratedFiles(String generatedFileName) throws IOException {
        Path resourcesPath = RESOURCES_PATH.resolve("ballerina_source");
        if (Files.exists(resourcesPath)) {
            List<File> listFiles = Arrays.asList(
                    Objects.requireNonNull(new File(String.valueOf(resourcesPath)).listFiles()));
            for (File existsFile: listFiles) {
                String fileName = existsFile.getName();
                if (fileName.equals(generatedFileName) || fileName.equals(generatedFileName + "_service.bal") ||
                        fileName.equals("client.bal") || fileName.equals("types.bal")) {
                    existsFile.delete();
                }
            }
            Path directoryPath = resourcesPath.resolve("tests");
            if (Files.isDirectory(directoryPath)) {
                File file = new File(directoryPath.toString());
                FileUtils.deleteDirectory(file);
            }
        }
    }

    public static File[] getMatchingFiles(String project) throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        //TODO: Change this function after fixing module name with client declaration alias.
        Process successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve(project), buildArgs);
        Assert.assertEquals(successful.waitFor(), 0);
        File dir = new File(RESOURCE.resolve("client-idl-projects/" + project + "/generated/").toString());
        final String id = "openapi_client";
        File[] matchingFiles = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains(id);
            }
        });
        return matchingFiles;
    }
}
