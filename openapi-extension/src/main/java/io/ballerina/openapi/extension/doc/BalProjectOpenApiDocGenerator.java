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

import io.ballerina.openapi.extension.Constants;
import io.ballerina.projects.ProjectKind;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code BalProjectOpenApiDocGenerator} generates open-api related docs for HTTP service defined in ballerina projects.
 */
public class BalProjectOpenApiDocGenerator extends AbstractOpenApiDocGenerator {
    @Override
    public boolean isSupported(ProjectKind projectType) {
        return ProjectKind.BUILD_PROJECT.equals(projectType);
    }

    // for ballerina-project, intermediate `resources` directory will be created inside `<project-root>/target/bin`
    @Override
    protected Path retrieveResourcePath(Path projectRoot) {
        return projectRoot
                .resolve(Constants.TARGET_DIR_NAME)
                .resolve(Paths.get(Constants.BIN_DIR_NAME, Constants.RESOURCES_DIR_NAME))
                .resolve(Constants.PACKAGE_ORG).resolve(Constants.PACKAGE_NAME);
    }
}
