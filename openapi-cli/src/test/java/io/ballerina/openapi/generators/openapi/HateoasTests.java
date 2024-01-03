/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.generators.openapi;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HateoasTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();

    @Test(description = "Automatic linking between resoruce functions")
    public void testHateoasAutomaticLinking() throws IOException {
        Path ballerinafilePath = RES_DIR.resolve("hateoas/hateoas_automatic_linking.bal");
        TestUtils.compareWithGeneratedFile(ballerinafilePath, "hateoas/hateoas_automatic_linking.yaml");
    }
}
