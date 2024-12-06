// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

@http:ServiceConfig {basePath: "/v1"}
type OASServiceType service object {
    *http:ServiceContract;
    @openapi:ResourceInfo {
        tags: ["user"],
        examples: {
            response: {
                "201": {
                    "examples": {
                        "application/json": {
                            "Jessica": {
                                "value": {
                                    "id" : "123",
                                    "name": "Jessica"
                                }
                            }
                        }
                    }
                }
            },
            requestBody: {
                "application/json": {
                    "user01": {
                        "value": {
                            "id": "123",
                            "name": "Jessica"
                        }
                    }
                }
            }
        }
    }
    resource function post user(User user) returns User;
};

public type User record {|
    int id;
    string name;
|};
