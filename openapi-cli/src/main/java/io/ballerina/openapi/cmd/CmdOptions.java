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

import java.util.List;

/**
 * This builder class for contains the all the option for use CLI.
 */
public class CmdOptions {
    private final String input;
    private final String outputModule;
    private final String id;
    private final String packagePath;
    private final List<String> tags;
    private final List<String>  operations;
    private final String mode;
    private final String clientMethod;
    private final boolean nullable;
    private final String licensePath;

    private CmdOptions(CmdOptionsBuilder builder) {
        this.input = builder.input;
        this.outputModule = builder.outputModule;
        this.id = builder.id;
        this.packagePath = builder.packagePath;
        this.operations = builder.operations;
        this.tags = builder.tags;
        this.mode = builder.mode;
        this.clientMethod = builder.clientMethod;
        this.nullable = builder.nullable;
        this.licensePath = builder.licensePath;
    }

    public String getInput() {
        return input;
    }

    public String getOutputModule() {
        return outputModule;
    }

    public String getId() {
        return id;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getOperations() {
        return operations;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getMode() {
        return mode;
    }

    public String getClientMethod() {
        return clientMethod;
    }


    public String getLicensePath() {
        return licensePath;
    }
    /**
     * CMD options builder class.
     */
    public static class CmdOptionsBuilder {
        private String outputModule;
        private String id;
        private String packagePath;
        private List<String> tags;
        private List<String> operations;
        private String mode;
        private String clientMethod;
        private boolean nullable;
        private String input;
        private String licensePath;

        public CmdOptionsBuilder withOutputModule(String outputModule) {
            this.outputModule = outputModule;
            return this;
        }

        public CmdOptionsBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public CmdOptionsBuilder withPackagePath(String packagePath) {
            this.packagePath = packagePath;
            return this;
        }

        public CmdOptionsBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public CmdOptionsBuilder withOperations(List<String> operations) {
            this.operations = operations;
            return this;
        }

        public CmdOptionsBuilder withMode(String mode) {
            this.mode = mode;
            return this;
        }

        public CmdOptionsBuilder withClientMethod(String clientMethod) {
            this.clientMethod = clientMethod;
            return this;
        }

        public CmdOptionsBuilder withNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public CmdOptionsBuilder withInput(String input) {
            this.input = input;
            return this;
        }

        public CmdOptionsBuilder withLicensePath(String licensePath) {
            this.licensePath = licensePath;
            return this;
        }
        public CmdOptions build() {
            return new CmdOptions(this);
        }
    }
}
