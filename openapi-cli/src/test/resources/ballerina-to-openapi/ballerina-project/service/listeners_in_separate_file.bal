import ballerina/http;

public listener http:Listener ep_without_host = new (80);

service /payloadV on ep_without_host, ep_with_host {
    resource function get pets() returns string {
            return "done";
    }
}
