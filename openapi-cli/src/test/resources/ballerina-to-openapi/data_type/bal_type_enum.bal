import ballerina/http;

enum Action {
    DELIVERED,
    RECEIVED,
    PROCESS
}

type Name Action;
service /payloadV on new http:Listener(9090) {
    resource function post reservation(OrderType ird, @http:Payload Link link) returns Name {
        return DELIVERED;
    }
}
