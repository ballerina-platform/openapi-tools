/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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

package io.ballerina.openapi.idl.client;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCE;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.cmd.TestUtil.executeRun;

/**
 * Client IDL import integration tests.
 */
public class IDLClientGenPluginTests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client-idl-projects");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Provide valid swagger path")
    public void validSwaggerContract() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_01");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "When client declaration without annotation")
    public void withOutAnnotation() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_02");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 2);
    }

    @Test(description = "Provide client annotation symbol")
    public void withAnnotation() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_03");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "When client declaration inside the function")
    public void withClientDeclarationNode() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_04");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "When client declaration in module level the function")
    public void withModuleClientDeclarationNode() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_05");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 2);
    }

    @Test(description = "When multiple client declarations have same annotation")
    public void sameAnnotation() throws IOException, InterruptedException {
        File[] matchingFiles = getMatchingFiles("project_06");
        assert matchingFiles != null;
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "When client declaration has graphQl yaml")
    public void graphQLYaml() throws IOException, InterruptedException {
        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_07"), new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_07/generated/").toString());
        Assert.assertFalse(dir.exists());
    }

    private static File[] getMatchingFiles(String project)
            throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();

        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve(project), buildArgs);
        File dir = new File(RESOURCE.resolve("client-idl-projects/" + project + "/generated/").toString());
        final String id = "openapi-client";
        File[] matchingFiles = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().contains(id);
            }
        });
        return matchingFiles;
    }
}
