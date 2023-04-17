import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /pizzashack/'1\.0\.0 on ep0 {
    resource function put 'order(@http:Payload json payload) returns http:Created {
    }
    resource function post 'order(@http:Payload json payload) returns http:Created {
    }
    resource function put order02(@http:Payload json payload) returns http:Ok {
    }
}
