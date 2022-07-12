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
    Action actions?;
|};

type Order record {|
    string rel;
    OrderType actions?;
|};
const SIZE = "size";

enum OrderType {
    FULL = "full",
    HALF = "Half \"Portion\"",
    CUSTOM = "custom " + SIZE
}

service /payloadV on new http:Listener(9090) {

    # Represents Snowpeak reservation resource
    #
    # + link - Reservation representation
    resource function post reservation(@http:Payload Link link) {
    }

    resource function post getOrder(@http:Payload Order link) {
    }
}
