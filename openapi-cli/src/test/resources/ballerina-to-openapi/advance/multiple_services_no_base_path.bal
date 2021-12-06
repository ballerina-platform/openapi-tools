import ballerina/http;

listener http:Listener ep1 = new (443, config = {host: "petstore.swagger.io"});

service on ep1 {
    resource function post hi(@http:Payload json payload) {
    }
}

service on new http:Listener(9090) {
    resource function get hi() {
    }
}
