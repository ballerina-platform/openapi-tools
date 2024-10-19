import ballerina/http;

service /payload on new http:Listener(0) {
    resource function get hello() {
    }

    resource function post path(@http:Payload string name) {
    }
    }
}
