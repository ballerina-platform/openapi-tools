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

module io.ballerina.openapi.core {
    requires handlebars;
    requires info.picocli;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.cli;
    requires io.ballerina.tools.api;
    requires io.ballerina.runtime;
    requires io.ballerina.formatter.core;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires java.ws.rs;
    requires jsr305;
    requires org.apache.commons.io;
    requires org.slf4j;
    requires swagger.parser;
    requires swagger.parser.core;
    requires swagger.parser.v3;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.databind;

    exports io.ballerina.openapi.core.generators.client;
    exports io.ballerina.openapi.core.generators.client.diagnostic;
    exports io.ballerina.openapi.core.generators.client.exception;
    exports io.ballerina.openapi.core.generators.client.mime;
    exports io.ballerina.openapi.core.generators.client.mock;
    exports io.ballerina.openapi.core.generators.client.model;
    exports io.ballerina.openapi.core.generators.client.parameter;

    exports io.ballerina.openapi.core.generators.common;
    exports io.ballerina.openapi.core.generators.common.diagnostic;
    exports io.ballerina.openapi.core.generators.common.exception;
    exports io.ballerina.openapi.core.generators.common.model;

    exports io.ballerina.openapi.core.generators.service;
    exports io.ballerina.openapi.core.generators.service.model;
    exports io.ballerina.openapi.core.generators.service.parameter;
    exports io.ballerina.openapi.core.generators.service.diagnostic;
    exports io.ballerina.openapi.core.generators.service.response;
    exports io.ballerina.openapi.core.generators.service.exceptions;
    exports io.ballerina.openapi.core.generators.service.resource;
    exports io.ballerina.openapi.core.generators.service.signature;

    exports io.ballerina.openapi.core.generators.type;
    exports io.ballerina.openapi.core.generators.type.diagnostic;
    exports io.ballerina.openapi.core.generators.type.generators;
    exports io.ballerina.openapi.core.generators.type.exception;
    exports io.ballerina.openapi.core.generators.type.model;

    exports io.ballerina.openapi.core.generators.constraint;
    exports io.ballerina.openapi.core.generators.document;
}
