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

public isolated function getLocations() returns rep:Locations {
    return {
        locations: [
            {
                name: "Alps",
                id: "l1000",
                address: "NC 29384, some place, switzerland",
                _links: {
                    "room1" : {
                        rel: "room",
                        href: "/snowpeak/locations/l1000/rooms",
                        types: ["applicaion/vnd.snowpeak.resort+json"],
                        methods: [http:GET]
                    }
                }
            },
            {
                name: "Pilatus",
                id: "l2000",
                address: "NC 29444, some place, switzerland",
                _links: {
                    "room2": {
                        rel: "room",
                        href: "/snowpeak/locations/l2000/rooms",
                        types: ["applicaion/vnd.snowpeak.resort+json"],
                        methods: [http:GET]
                    }
                }
            }
        ]
    };
}

public isolated function getRooms(string startDate, string endDate) returns rep:Rooms {
    return {
        rooms: [
            {
                id: "r1000",
                category: rep:DELUXE,
                capacity: 5,
                wifi: true,
                status: rep:AVAILABLE,
                currency: "USD",
                price: 200.00,
                count: 3
            }
        ],
        _links: {
            "room4" : {
                rel: "reservation",
                href: "/snowpeak/reservation",
                types: ["applicaion/vnd.snowpeak.resort+json"],
                methods: [http:POST]
            }
        }
    };
}

public isolated function createReservation(rep:Reservation reservation) returns rep:ReservationCreated {
    return {
        headers: {
            location: "/snowpeak/reservation/r1000"
        },
        body: {
            id: "re1000",
            expiryDate: "2021-07-01",
            lastUpdated: "2021-06-29T13:01:30Z",
            currency: "USD",
            total: 400.00,
            reservation: {
                reserveRooms: [
                    {
                        id: "r1000",
                        count: 2
                    }
                ],
                startDate: "2021-08-01",
                endDate: "2021-08-03"
            },
            _links: {
               "room1": {
                    rel: "cancel",
                    href: "/snowpeak/reservation/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:DELETE]
                },
                "room2": {
                    rel: "edit",
                    href: "/snowpeak/reservation/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:PUT]
                },
                "room3":{
                    rel: "payment",
                    href: "/snowpeak/payment/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:POST]
                }
            }
        }
    };
}

public isolated function updateReservation(rep:Reservation reservation) returns rep:ReservationUpdated {
    return {
        body: {
            id: "re1000",
            expiryDate: "2021-07-01",
            lastUpdated: "2021-07-05T13:01:30Z",
            currency: "USD",
            total: 400.00,
            reservation: {
                reserveRooms: [
                    {
                        id: "r1000",
                        count: 1
                    }
                ],
                startDate: "2021-08-01",
                endDate: "2021-08-03"
            },
            _links: {
                "room1":{
                    rel: "cancel",
                    href: "/snowpeak/reservation/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:DELETE]
                },
                "room2": {
                    rel: "edit",
                    href: "/snowpeak/reservation/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:PUT]
                },
                "room3": {
                    rel: "payment",
                    href: "/snowpeak/payment/re1000",
                    types: ["applicaion/vnd.snowpeak.resort+json"],
                    methods: [http:POST]
                }
            }
        }
    };
}

public isolated function createPayment(string id, rep:Payment payment) returns rep:PaymentCreated {
    return {
        headers: {
            location: "/snowpeak/reservation/p1000"
        },
        body: {
            id: "p1000",
            currency: "USD",
            total: 400.00,
            lastUpdated: "2021-06-29T13:01:30Z",
            rooms: [
                    {
                    id: "r1000",
                    category: rep:DELUXE,
                    capacity: 5,
                    wifi: true,
                    status: rep:RESERVED,
                    currency: "USD",
                    price: 200.00,
                    count: 1
                }
            ]
        }
    };
}

