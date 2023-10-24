// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
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
import ballerina/constraint;

@constraint:Float {
    maxValueExclusive: 10.5,
    minValue: 2.5
}
public type Rating float;

public type Hotel record {
    Rating rate;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Hotel body) returns error? {
        return;
    }
}
