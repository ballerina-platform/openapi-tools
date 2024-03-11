/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.cmd;

import picocli.CommandLine;

/**
 * This class is to store the cli command options that are commonly used int parent and subcommands.
 *
 * @since 1.9.0
 */
public class BaseCmd {
    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    public boolean helpFlag;
    @CommandLine.Option(names = {"-i", "--input"}, description = "Generating the client and service both files")
    public String inputPath;

    @CommandLine.Option(names = {"--license"}, description = "Location of the file which contains the license header")
    public String licenseFilePath;

    @CommandLine.Option(names = {"--mode"}, description = "Generate only service file or client file according to the" +
            " given mode type")
    public String mode;

    @CommandLine.Option(names = {"-n", "--nullable"}, description = "Generate the code by setting nullable true")
    public boolean nullable;

    @CommandLine.Option(names = {"--tags"}, description = "Tag that need to write service")
    public String tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to write service")
    public String operations;

    @CommandLine.Option(names = {"--client-methods"}, hidden = true, description = "Generate the client methods" +
            " with provided type . Only \"resource\"(default) and \"remote\" options are supported.")
    public String generateClientMethods;

}
