// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Service validation codee
# + contract - OpenApi Contract link
# + tags - OpenApi Tags
# + operations - OpenApi Operations
# + excludeTags - Openapi Validator Off for these tags
# + excludeOperations - Openapi Validator Off for these operations
# + failOnErrors - OpenApi Validator Enable
public type ServiceInformation record {|
    string contract = "";
    string[]? tags = [];
    string[]? operations = [];
    string[]? excludeTags = [];
    string[]? excludeOperations = [];
    boolean failOnErrors = true;
|};

# Annotation for additional OpenAPI information of a Ballerina service.
public annotation ServiceInformation ServiceInfo on service;
