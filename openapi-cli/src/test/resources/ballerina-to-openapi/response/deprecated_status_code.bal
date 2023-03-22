import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function post pet() returns http:NetworkAuthorizationRequired {
        return <http:NetworkAuthorizationRequired> {};
    }
}
