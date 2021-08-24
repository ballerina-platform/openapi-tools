import ballerina/http;

# Link details
type Link record {|
    # linnk rel
    string rel;
    # link href
    string href;
    # link mediatype
    string[] mediaTypes?;
|};

# Link details
type Links record {|
    # Array links
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
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"]

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
                    mediaTypes: ["applicaion/vnd.snowpeak.resort+json"]
                }
            ]
        }
    ];
}
