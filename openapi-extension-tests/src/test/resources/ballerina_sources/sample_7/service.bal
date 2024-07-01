// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Create an endpoint with port 8080 for the mock backend services.
listener http:Listener backendEP = check new(8080);

// Define the load balance client endpoint to call the backend services.
http:LoadBalanceClient lbBackendEP = check new ({
        // Define the set of HTTP clients that need to be load balanced.
        targets: [
            {url: "http://localhost:8080/mock1"},
            {url: "http://localhost:8080/mock2"},
            {url: "http://localhost:8080/mock3"}
        ],

        timeout: 5
});

service / on new http:Listener(9090) {
    resource function 'default lb() returns string|error {
        string payload = check lbBackendEP->get("/");
        return payload;
    }
}

// Define the mock backend services, which are called by the load balancer.
service /mock1 on backendEP {
    resource function get .() returns string {
        return "Mock1 resource was invoked.";
    }
}

service /mock2 on backendEP {
    resource function get .() returns string {
        return "Mock2 resource was invoked.";
    }
}

service /mock3 on backendEP {
    resource function get .() returns string {
        return "Mock3 resource was invoked.";
    }
}
