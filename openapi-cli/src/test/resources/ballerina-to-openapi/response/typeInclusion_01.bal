import ballerina/http;

type ReservationConflict record {|
    *http:Conflict;
    string body = "Error occurred while updating the reservation";
|};

// By default, Ballerina exposes an HTTP service via HTTP/1.1.
service /payloadV on new http:Listener(9090) {

    // Resource functions are invoked with the HTTP caller and the incoming request as arguments.
    resource function get sayhello() returns ReservationConflict {
        ReservationConflict ReservationConflict = {};
        return ReservationConflict;
    }
}
