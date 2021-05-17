import ballerina/http;

listener http:Listener ep1 = new (443, config = {host: "petstore.swagger.io"});

service /hello on ep1 {
    resource function post hi(http:Caller caller, http:Request request, @http:Payload json payload) {

    }
}

service /hello02 on ep1 {
    resource function post hi(http:Caller caller, http:Request request, int id) {

    }
}
