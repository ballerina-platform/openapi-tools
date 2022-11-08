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

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.executeRun;
import static io.ballerina.openapi.TestUtil.getMatchingFiles;

/**
 * This class is for client IDL integration tests.
 */
public class IDLClientGenPluginTests extends OpenAPITest {

    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client-idl-projects");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test
    public void testValidSwaggerContract() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("bar");
        File[] matchingFiles = getMatchingFiles("project_01", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test
    public void testClientDeclarationWithOutAnnotation() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("foo");
        File[] matchingFiles = getMatchingFiles("project_02", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test
    public void testClientDeclarationWithAnnotation() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("bar");
        File[] matchingFiles = getMatchingFiles("project_03", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test
    public void testModuleLevelClientDeclarationNode() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("bar");
        File[] matchingFiles = getMatchingFiles("project_05", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "When multiple client declarations have same annotation")
    public void testMultipleClientsWithSameAnnotation() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("foo");
        File[] matchingFiles = getMatchingFiles("project_06", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test
    public void testInvokeAPIFromGeneratedClient() throws IOException, InterruptedException {
        Process successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_08"),
                new ArrayList<>());
        Assert.assertEquals(successful.waitFor(), 0);
    }

    @Test(description = "test client declarations with local YAML contract paths")
    public void testWithLocalPathYAML() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("bar");
        File[] matchingFiles = getMatchingFiles("project_09", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }

    @Test(description = "test client declarations with local JSON contract paths")
    public void testWithLocalPathJSON() throws IOException, InterruptedException {
        List<String> ids = new LinkedList<>();
        ids.add("bar");
        File[] matchingFiles = getMatchingFiles("project_14", ids);
        Assert.assertNotNull(matchingFiles);
        Assert.assertEquals(matchingFiles.length, 1);
    }
}
