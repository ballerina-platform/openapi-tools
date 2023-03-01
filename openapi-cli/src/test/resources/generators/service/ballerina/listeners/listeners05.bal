import ballerina/http;

listener http:Listener ep0 = new (9080, config = {host: "localhost"});

service / on ep0 {
    # Handle request mediation
    #
    # + payload - Content of the request
    # + return - Successful operation
    resource function post 'handle\-request(@http:Payload HandleRequest_RequestBody payload) returns OkHandleResponse_RequestBody {
    }
}
