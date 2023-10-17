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

@constraint:String {maxLength: 23}
public type HobbyItemsString string;

@constraint:String {minLength: 7}
public type PersonDetailsItemsString string;

@constraint:Float {maxValue: 445.4}
public type PersonFeeItemsNumber float;

@constraint:Int {maxValue: 67 }
public type PersonLimitItemsInteger int;

@constraint:Array {maxLength: 5, minLength: { value: 2, message: "Min Length Exceeded!"}}
public type Hobby HobbyItemsString[];

public type Person record {
    Hobby hobby?;
    @constraint:Array {maxLength: 5}
    PersonDetailsItemsString[] Details?;
    int id;
    PersonFeeItemsNumber[] fee?;
    # The maximum number of items in the response (as set in the query or by default).
    PersonLimitItemsInteger[] 'limit?;
};

service /payloadV on new http:Listener(9090) {
    resource function post pet(@http:Payload Person body) returns error? {
        return;
    }
}
