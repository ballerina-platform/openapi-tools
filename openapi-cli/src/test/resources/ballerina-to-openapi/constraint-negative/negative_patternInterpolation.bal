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

int i = 123;
int j = 20;
@constraint:String {
    length: 21,
    pattern: re `^${i}[a-zA-Z]+$`
}
public type PosID string;

@constraint:String {
    pattern: re `^[\${2}a-z]+$`
}
public type EmpID string;

@constraint:String {
    pattern: re `^[a-z${2}]+$`
}
public type Name string;

public type Emp record {
    PosID posId;
    @constraint:String { pattern: re `^[A-Z]${j}+$`}
    string code?;
    EmpID empID?;
    Name name?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Emp body) returns error? {
        return;
    }
}
