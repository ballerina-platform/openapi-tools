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
import 'service.representations as rep;

# A fake mountain resort
@http:ServiceConfig { mediaTypeSubtypePrefix: "vnd.snowpeak.reservation"}
service /payloadV on new http:Listener(9090) {

    # Snowpeak payment resource
    #
    # + id - Unique identification of reservation
    # + payment - Payment representation
    # + return - `PaymentCreated`, `PaymentConflict` or `SnowpeakError` representation
    resource function put payment/[string id](@http:Payload rep:Payment payment) returns string {
        return "hello";
    }
}
