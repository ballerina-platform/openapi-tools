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

# Service validation code.
# + contract - OpenAPI contract link
# + tags - OpenAPI tags
# + operations - OpenAPI operations
# + excludeTags - Disable the OpenAPI validator for these tags
# + excludeOperations - Disable the OpenAPI validator for these operations
# + failOnErrors - Enable the OpenAPI validator
# + embed - Enable auto-inject of OpenAPI documentation to current service
# + title - Title for generated OpenAPI contract
# + version - Version for generated OpenAPI contract
public type ServiceInformation record {|
    string contract = "";
    string[]? tags = [];
    string[]? operations = [];
    string[]? excludeTags = [];
    string[]? excludeOperations = [];
    boolean failOnErrors = true;
    boolean embed = false;
    string title?;
    string 'version?;
|};

# Client information code.
# + tags - OpenAPI tags
# + operations - OpenAPI operations
# + nullable -
# + withTests -
# + isResource -
# + license -
public type ClientInformation record {|
    string[]? tags = [];
    string[]? operations = [];
    boolean nullable = true;
    boolean withTests = false;
    boolean isResource = true;
    string license?;
|};

# Annotation for additional OpenAPI information of a Ballerina service.
public annotation ServiceInformation ServiceInfo on service;
# Annotation for additional OpenAPI information of a Ballerina client.
public const annotation ClientInformation ClientInfo on source client;
