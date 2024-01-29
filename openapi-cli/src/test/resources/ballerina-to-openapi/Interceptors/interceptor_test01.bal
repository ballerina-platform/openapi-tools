// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

service class RequestInterceptor {
    *http:RequestInterceptor;
    resource function 'default [string... path](
            http:RequestContext ctx,
            @http:Header {name: "x-api-version"} string xApiVersion)
        returns http:NotImplemented|http:NextService|error? {
        // Checks the API version header.
        if xApiVersion != "v1" {
            return http:NOT_IMPLEMENTED;
        }
        return ctx.next();
    }
}

service class RequestErrorInterceptor {
    *http:RequestErrorInterceptor;
    resource function 'default [string... path](error err) returns http:BadRequest {
        return {
            mediaType: "application/org+json",
            body: {message: err.message()}
        };
    }
}

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [RequestInterceptor, RequestErrorInterceptor] {
        return [new RequestInterceptor(), new RequestErrorInterceptor()];
    }
}
