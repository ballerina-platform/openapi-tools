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

# Represents location
public type Location record {|
    *http:Links;
    # Name of the location
    string name;
    # Unique identification
    string id;
    # Address of the location
    string address;
|};
# Represents a collection of locations
public type Locations record {|
    # collection of locations
    Location[] locations;
|};

public enum RoomCategory {
    DELUXE,
    KING,
    FAMILY
}
public enum RoomStatus {
    AVAILABLE,
    RESERVED,
    BOOKED
}

# Represents resort room
public type Room record {|
    # Unique identification
    string id;
    #Types of rooms available
    RoomCategory category;
    # Number of people that can be accommodate
    int capacity;
    # Availability of wifi
    boolean wifi;
    # Availability of room
    RoomStatus status;
    # Currency used in price
    string currency;
    # Cost for the room
    decimal price;
    # Number of rooms as per the status
    int count;
|};
# Represents a collection of resort rooms
public type Rooms record {|
    *http:Links;
    # Array of rooms
    Room[] rooms;
|};

# Represents rooms be reserved
public type ReserveRoom record {|
    # Unique identification of the room
    string id;
    # Number of rooms
    int count;
|};
# Represents a reservation of rooms
public type Reservation record {|
    # Rooms to be reserved
    ReserveRoom[] reserveRooms;
    # Start date in yyyy-mm-dd
    string startDate;
    # End date in yyyy-mm-dd
    string endDate;
|};
# Represents a receipt for the reservation
public type ReservationReceipt record {|
    *http:Links;
    # Unique identification
    string id;
    # Expiry date in yyyy-mm-dd
    string expiryDate;
    # Last updated time stamp
    string lastUpdated;
    # Currency used in price
    string currency;
    # Total payable
    decimal total;
    # Reservation
    Reservation reservation;
|};
public type ReservationUpdated record {|
    *http:Ok;
    ReservationReceipt body;
|};
public type ReservationCreated record {|
    *http:Created;
    ReservationReceipt body;
|};
public type ReservationConflict record {|
    *http:Conflict;
    string body = "Error occurred while updating the reservation";
|};

# Reperesents payement for rooms
public type Payment record {|
    # Name of the card holder
    string cardholderName;
    # Card number
    int cardNumber;
    # Expiration month of the card in mm
    string expiryMonth;
    # Expiaration year of the card in yyyy
    string expiryYear;
|};

# Reperesents receipt for the payment
public type PaymentReceipt record {|
    # Unique identification
    string id;
    # Currency used in price
    string currency;
    # Total amount paid
    decimal total;
    # Last updated time stamp
    string lastUpdated;
    # Booked rooms
    Room[] rooms;
|};
public type PaymentCreated record {|
    *http:Created;
    PaymentReceipt body;
|};
public type PaymentConflict record {|
    *http:Conflict;
    string body = "Error occurred while updating the payment";
|};
