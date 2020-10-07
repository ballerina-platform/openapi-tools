/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
open module io.ballerina.openapi {
    requires swagger.parser.v3;
    requires io.swagger.v3.oas.models;
    requires  swagger.parser.core;
    requires io.ballerina.lang;
    requires io.ballerina.tool;
    requires info.picocli;
    requires io.ballerina.language.server.compiler;
//    requires java.ws.rs;
    requires org.apache.commons.lang3;
    requires io.ballerina.ballerina.openapi.convertor;
    requires java.ws.rs;
//    requires java.xml.bind;
//    requires java.ws.rs;
//    requires io.ballerina.ballerina.openapi.convertor;

    exports org.ballerinalang.openapi;
    exports org.ballerinalang.openapi.cmd;
    exports org.ballerinalang.openapi.exception;
    exports org.ballerinalang.openapi.model;
    exports org.ballerinalang.openapi.typemodel;
    exports org.ballerinalang.openapi.utils;
}

