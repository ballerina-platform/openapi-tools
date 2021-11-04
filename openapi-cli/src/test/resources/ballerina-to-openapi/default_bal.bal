import ballerina/http;

service /payloadV on new http:Listener(9090) {
    resource function 'default passthrough() returns error? {
        return;
    }
}
