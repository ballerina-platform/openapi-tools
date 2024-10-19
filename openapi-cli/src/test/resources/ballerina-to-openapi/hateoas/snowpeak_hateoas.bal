// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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

public type Location record {
    string name;
    string id;
    string address;
};

public type Room record {
    string id;
    string category;
    boolean wifi;
    string status;
    string currency;
    int price;
    int count;
};

service /payloadV on new http:Listener(9090) {
    @http:ResourceConfig {
        name: "Locations",
        linkedTo: [ {name: "Rooms", relation: "room", method: "get"}]
    }
    resource function get locations() returns Location[] {
       return [{name: "Alps", id: "l1000", address: "NC 29384, some place, switzerland"},
               {name: "Pilatus", id: "l2000", address: "NC 29444, some place, switzerland"}];
    }

    @http:ResourceConfig {
        name: "Rooms",
        linkedTo: [{name: "Reservations", relation: "reservation", method: "post"}]
    }
    resource function get locations/[string id]/rooms(string startDate, string endDate) returns Room[] {
        return [{
                "id": "r1000",
                "category": "DELUXE",
                "capacity": 5,
                "wifi": true,
                "status": "AVAILABLE",
                "currency": "USD",
                "price": 200,
                "count": 3
                }];
    }

    @http:ResourceConfig {
        name: "Reservations",
        linkedTo: [ {name: "DeleteReservation", relation: "cancel", method: "delete"},
                    {name: "UpdateReservation", relation: "edit", method: "put"},
                    {name: "PayReservation", relation: "payment", method: "post"}]
    }
    resource function post reservations() returns string {
        return "Room Reserved!";
    }

    @http:ResourceConfig {
        name: "DeleteReservation"
    }
    resource function delete reservations/[string id]() returns string {
        return "Reservation Deleted!";
    }

    @http:ResourceConfig {
        name: "UpdateReservation"
    }
    resource function put reservations/[string id]() returns string {
        return "Reservation Updated!";
    }

    @http:ResourceConfig {
        name: "PayReservation"
    }
    resource function post reservations/[string id]() returns string {
        return "Payment Successful!";
    }
}
