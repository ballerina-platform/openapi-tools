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

annotation Annotation Annot on type;

type Annotation record {
    string name;
};

@Annot {
    name: "test"
}
type AnnotatedString string;

@constraint:String {
    length: 10,
    pattern: re `^[a-zA-Z0-9_]+$`
}
public type St_ID string;

@constraint:String {
    minLength: 5
}
public type St_Roll_No St_ID;

public type StudentRecord record {
    St_ID id;
    St_Roll_No rollNo;
    @constraint:String { pattern: re `^[a-zA-Z]+$`}
    string name?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload StudentRecord body, AnnotatedString annotStr) returns error? {
        return;
    }
}
