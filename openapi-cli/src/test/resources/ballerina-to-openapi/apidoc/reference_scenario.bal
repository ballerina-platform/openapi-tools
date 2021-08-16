import ballerina/http;

enum Action {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH
}

type Link record {|
    string rel;
    string href;
    string[] mediaTypes?;
    Action[] actions?;
|};
type Links record {|
    Link[] links;
|};

# Represents locations
type Location record {|
    *Links;
    # Name of the location
    string name;
    # Unique identification
    string id;
    # Address of the location
    string address;
|};

enum RoomCategory {
    DELUXE,
    KING,
    FAMILY
}

enum RoomStatus {
    AVAILABLE,
    RESERVED,
    BOOKED
}

# Represents resort room
type Room record {|
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
type Rooms record {|
    *Links;
    # Array of rooms
    Room[] rooms;
|};

# Represents rooms be reserved
type ReserveRoom record {|
    # Unique identification of the room
    string id;
    # Number of rooms
    int count;
|};
# Represents a reservation of rooms
type Reservation record {|
    # Rooms to be reserved
    ReserveRoom[] reserveRooms;
    # Start date in yyyy-mm-dd
    string startDate;
    # End date in yyyy-mm-dd
    string endDate;
|};
# Represents a receipt for the reservation
type ReservationReceipt record {|
    *Links;
    # Unique identification
    string id;
    # Expiry date in yyyy-mm-dd
    string expiryDate;
    # Last updated time stamp
    string lastUpdated;
    # Reservation
    Reservation reservation;
|};
type ReservationUpdated record {|
    *http:Ok;
    ReservationReceipt body;
|};
type ReservationCreated record {|
    *http:Created;
    ReservationReceipt body;
|};
type ReservationConflict record {|
    *http:Conflict;
    string body = "Error occurred while updating the reservation";
|};

# Reperesents payement for rooms
type Payment record {|
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
type PaymentReceipt record {|
    # Unique identification
    string id;
    # Total amount paid
    decimal total;
    # Last updated time stamp
    string lastUpdated;
    # Booked rooms
    Room[] rooms;
|};
type PaymentCreated record {|
    *http:Created;
    PaymentReceipt body;
|};
type PaymentConflict record {|
    *http:Conflict;
    string body = "Error occurred while updating the payment";
|};

service /payloadV on new http:Listener(9090) {

    # Represents Snowpeak location resource
    #
    # + return - `Location` representation
    resource function get locations() returns Location[] {
        Location[] locations = getLocation();
        return locations;
    }

    # Reperesents Snowpeak room collection resource
    #
    # + id - Unique identification of location
    # + startDate - Start date in format yyyy-mm-dd
    # + endDate - End date in format yyyy-mm-dd
    # + return - `Rooms` representation
    resource function get locations/[string id]/rooms(string startDate, string endDate) returns Rooms {
        Rooms rooms = getRooms(startDate, endDate);
        return rooms;
    }

    # Represents Snowpeak reservation resource
    #
    # + reservation - Reservation representation
    # + return - `ReservationCreated` or ReservationConflict representation
    resource function post reservation(@http:Payload Reservation reservation)
                returns ReservationCreated|ReservationConflict {
        ReservationCreated created = createReservation(reservation);
        return created;
    }

    # Represents Snowpeak reservation resource
    #
    # + reservation - Reservation representation
    # + return - `ReservationCreated` or ReservationConflict representation
    resource function put reservation(@http:Payload Reservation reservation)
                returns ReservationUpdated|ReservationConflict {
        ReservationUpdated updated = updateReservation(reservation);
        return updated;
    }

    # Represents Snowpeak payment resource
    #
    # + id - Unique identification of payment
    # + payment - Payment representation
    # + return - `PaymentCreated` or `PaymentConflict` representation
    resource function post payment/[string id](@http:Payload Payment payment)
                returns PaymentCreated|PaymentConflict {
        PaymentCreated paymentCreated = createPayment(id, payment);
        return paymentCreated;
    }
}

function getLocation() returns Location[] {
    return [
        {
            name: "Alps",
            id: "l1000",
            address: "NC 29384, some place, switzerland",
            links: [
                {
                    rel: "room",
                    href: "http://localhost:9090/snowpeak/locations/l1000/rooms",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [GET]
                }
            ]
        },
        {
            name: "Pilatus",
            id: "l2000",
            address: "NC 29444, some place, switzerland",
            links: [
                {
                    rel: "room",
                    href: "http://localhost:9090/snowpeak/locations/l2000/rooms",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [GET]
                }
            ]
        }
    ];
}

function getRooms(string startDate, string endDate) returns Rooms {
    return {
        rooms: [
            {
                id: "r1000",
                category: DELUXE,
                capacity: 5,
                wifi: true,
                status: AVAILABLE,
                currency: "USD",
                price: 200.00,
                count: 3
            }
        ],
        links: [
            {
                rel: "reservation",
                href: "http://localhost:9090/rooms/reservation",
                mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                actions: [POST]
            }
        ]
    };
}

function createReservation(Reservation reservation) returns ReservationCreated {
    return {
        headers: {
            location: "http://localhost:9090/reservation/r1000"
        },
        body: {
            id: "re1000",
            expiryDate: "2021-07-01",
            lastUpdated: "2021-06-29T13:01:30Z",
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
            links: [
                {
                    rel: "cancel",
                    href: "http://localhost:9090/reservation/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [DELETE]
                },
                {
                    rel: "edit",
                    href: "http://localhost:9090/reservation/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [PUT]
                },
                {
                    rel: "payment",
                    href: "http://localhost:9090/payment/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [POST]
                }
            ]
        }
    };
}

function updateReservation(Reservation reservation) returns ReservationUpdated {
    return {
        body: {
            id: "re1000",
            expiryDate: "2021-07-01",
            lastUpdated: "2021-07-05T13:01:30Z",
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
            links: [
                {
                    rel: "cancel",
                    href: "http://localhost:9090/reservation/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [DELETE]
                },
                {
                    rel: "edit",
                    href: "http://localhost:9090/reservation/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [PUT]
                },
                {
                    rel: "payment",
                    href: "http://localhost:9090/payment/re1000",
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"],
                    actions: [POST]
                }
            ]
        }
    };
}

function createPayment(string id, Payment payment) returns PaymentCreated {
    return {
        headers: {
            location: "http://localhost:9090/reservation/p1000"
        },
        body: {
            id: "p1000",
            total: 400.00,
            lastUpdated: "2021-06-29T13:01:30Z",
            rooms: [
                    {
                    id: "r1000",
                    category: DELUXE,
                    capacity: 5,
                    wifi: true,
                    status: RESERVED,
                    currency: "USD",
                    price: 200.00,
                    count: 1
                }
            ]
        }
    };
}
