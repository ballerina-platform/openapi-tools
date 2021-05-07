/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.generators;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for openapi code generator.
 */
public class GeneratorConstants {

    /**
     * Enum to select the code generation mode.
     * Ballerina service, mock and client generation is available
     */
    public enum GenType {
        GEN_SERVICE("gen_service"),
        GEN_CLIENT("gen_client"),
        GEN_BOTH("gen_both");

        private String name;

        GenType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static final String CLIENT_TEMPLATE_NAME = "client-ep";
    public static final String SCHEMA_FILE_NAME = "schema.bal";
    public static final String TEST_FILE_NAME = "test.bal";

    public static final String TEMPLATES_SUFFIX = ".mustache";
    public static final String TEMPLATES_DIR_PATH_KEY = "templates.dir.path";
    public static final String DEFAULT_TEMPLATE_DIR = "/templates";
    public static final String DEFAULT_CLIENT_DIR = DEFAULT_TEMPLATE_DIR + "/client";
    public static final String DEFAULT_CLIENT_PKG = "client";
    public static final String DEFAULT_MOCK_PKG = "mock";
    public static final String PATH_SEPARATOR = "/";
    public static final String USER_DIR = "user.dir";
    public static final String UNTITLED_SERVICE = "UntitledAPI";
    public static final List<String> RESERVED_KEYWORDS = Collections.unmodifiableList(
            Arrays.asList("abort", "aborted", "abstract", "all", "annotation",
                    "any", "anydata", "boolean", "break", "byte", "catch", "channel", "check", "checkpanic", "client",
                    "committed", "const", "continue", "decimal", "else", "error", "external", "fail", "final",
                    "finally", "float", "flush", "fork", "function", "future", "handle", "if", "import", "in", "int",
                    "is", "join", "json", "listener", "lock", "match", "new", "object", "OBJECT_INIT", "onretry",
                    "parameter", "panic", "private", "public", "record", "remote", "resource", "retries", "retry",
                    "return", "returns", "service", "source", "start", "stream", "string", "table", "transaction",
                    "try", "type", "typedesc", "typeof", "trap", "throw", "wait", "while", "with", "worker", "var",
                    "version", "xml", "xmlns", "BOOLEAN_LITERAL", "NULL_LITERAL", "ascending", "descending", "foreach",
                    "map", "group", "from", "default", "field", "limit", "as", "on", "isolated", "readonly",
                    "distinct", "where", "select", "do", "transactional", "commit", "enum", "base16", "base64",
                    "rollback", "configurable",  "class", "module", "never", "outer", "order", "null", "key", "let",
                    "by"));

    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~`*-=^+();:\\_{}\\s|.$])";
    //ClientCode generator
    public static final String HTTP = "http";
    public static final String MODULE_TEST  = "test";
    public static final String BALLERINA = "ballerina";
    public static final String PUBLIC = "public";
    public static final String PUBLIC_ISOLATED = "public isolated";
    public static final String CLIENT = "client";
    public static final String CLIENT_CLASS = "Client";
    public static final String CLIENT_EP = "clientEp";
    public static final String CLASS = "class";
    public static final String OPEN_BRACE = "{";
    public static final String OPEN_PRAN = "(";
    public static final String COLON = ":";
    public static final String FUNCTION = "function";
    public static final String RETURN = "returns";
    public static final String PREFIX_TEST = " test";
    public static final String ANNOT_TEST = "test:Config";
    public static final String TEST_DIR = "tests";
}
