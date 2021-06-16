// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new(9090);

@openapi:ServiceInfo {
    contract: "/var/folders/mz/xmjfm34s1n99v74_jtsbsdcw0000gn/T/openapi-cmd5033409960939893222/src/inlineModule/resources/inline-request-body.yaml"
}
service / on ep0 {

    resource function post user(http:Caller caller, http:Request req, @http:Payload record {  string userName; string
     userPhone; }  body) returns error? {

    }

}
