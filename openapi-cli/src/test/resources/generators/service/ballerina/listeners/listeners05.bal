import ballerina/http;

listener http:Listener ep0 = new (9080, config = {host: "localhost"});

service / on ep0 {
    resource function post 'handle\-request(@http:Payload {} HandlerequestRequestbody payload) returns HandleresponseRequestbody {
    }
}
