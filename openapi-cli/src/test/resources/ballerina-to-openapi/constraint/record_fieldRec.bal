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

@constraint:String {minLength: 5}
public type Address string;

public type Person record {
    @constraint:String {
        maxLength: {
            value: 14,
            message: "Max Length Exceeded!"
        }
    }
    string name?;
    @constraint:Array {
        maxLength: 5,
        minLength: {
            value: 2,
            message: "Min Length Exceeded!"
        }
    }
    string[] hobby?;
    @constraint:Int {
        maxValueExclusive: {
            value: 5,
            message: "Max Value Exceeded!"
        },
        minValue: 0
    }
    int id;
    Address address?;
    @constraint:Float {
        maxValueExclusive: {
            value: 100000,
            message: "Min Value Exceeded!"
        },
        minValue: 1000
    }
    float salary?;
    @constraint:Number {
        minValueExclusive: 500000,
        maxValue: {
            value: 1000000,
            message: "Max Value Exceeded!"
        }
    }
    decimal net?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Person body) returns error? {
        return;
    }
}
